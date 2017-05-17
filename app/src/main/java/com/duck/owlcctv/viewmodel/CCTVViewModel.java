package com.duck.owlcctv.viewmodel;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.duck.owlcctv.R;
import com.duck.owlcctv.activity.RecordedAvtivity;
import com.duck.owlcctv.util.OwlSettings;
import com.duck.owlcctv.util.VerticalTextView;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.videoio.VideoWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class CCTVViewModel implements BaseViewModel, CameraBridgeViewBase.CvCameraViewListener2 {
    public final ObservableField<String> cctvStatus = new ObservableField<>();
    public final ObservableField<String> networkStatus = new ObservableField<>();
    public final ObservableBoolean isRecordingNow = new ObservableBoolean();
    public final ObservableBoolean isTurnOnSaveEnergy = new ObservableBoolean();
    public final ObservableField<String> netStatus = new ObservableField<>();

    private static final int FRAME_COUNT = 30;
    private AppCompatActivity activity = null;
    private static final String TAG = "[CCTVViewModel]";
    private CameraBridgeViewBase camView = null;
    private BackgroundSubtractorMOG2 mog2 = null;
    private static final int FPS = 6;
    // 움직임이 감지되었을 때 얼마나 많은 프레임을 쌓을지 결정한다
    // 사용자가 설정하도록 만든다
    private int counterFrame = FRAME_COUNT;
    private BroadcastReceiver netConnReceiver = null;
    // 동영상 변환을 위한 FFmpeg
    private FFmpeg ffmpeg = null;
    // 순차적인 동영상 변환에 사용할 작업 큐
    private Queue<String> taskQueue = new ArrayBlockingQueue<>(30);

    private boolean stopWorker = false;

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(activity) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mog2 = Video.createBackgroundSubtractorMOG2();
                }
            }
        }
    };

    private interface ChangeState {
        void change();
    }

    private class RecorderObject {
        Mat outFrame = null;
        boolean isDetected = false;

        RecorderObject(Mat outFrame, boolean isDetected) {
            this.outFrame = outFrame;
            this.isDetected = isDetected;
        }
    }

    // 생성자
    public CCTVViewModel(AppCompatActivity activity) {
        this.activity = activity;
    }

    /**
     * 스테이트 패턴과 템플릿 메서드 패턴을 혼합
     * 움직임이 감지된 상태 - detected
     * 움직임이 감지되지 않은 상태 - unDetected
     */
    private abstract class RecorderBehavior {
        public abstract RecorderObject detectMoving(Mat in, int sensitivity);
        /**
         * @param in: 카메라로 부터 입력되는 프레임
         * @param sensitivity: 움직임 감지 감도 (사용자 설정값을 따라야 한다)
         * @return 만약, 움직임이 검출되었다면 true, 그렇지 않다면 false
         * 만약, UnDetected 상태에서 움직임이 감지되면 Detected 상태로 변경한다
         */
        RecorderObject detectMoving(Mat in, int sensitivity, ChangeState action) {
            Mat grayFrame = null;
            Mat fgMask = null;
            Mat elem = null;
            Mat temp = null;
            List<MatOfPoint> contours = null;

            try{
                // 흑백 이미지로 전환
                grayFrame = new Mat();
                Imgproc.cvtColor(in, grayFrame, Imgproc.COLOR_RGB2GRAY);
                // 배경 추출
                fgMask = new Mat();
                mog2.apply(in, fgMask);
                Imgproc.threshold(fgMask, fgMask, 25, 225, Imgproc.ADAPTIVE_THRESH_MEAN_C);
                // 에로드를 통한 노이즈 제거
                elem = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(11, 11));
                Imgproc.erode(fgMask, fgMask, elem);
                // 윤곽선을 검출하여 일정한 길이 이상이 나오면 영상이 검출되었다 판단한다
                contours = new ArrayList<>();
                temp = new Mat();
                Imgproc.findContours(fgMask, contours, temp, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
                boolean isDetected = false;
                for (MatOfPoint c : contours) {
                    double size = Imgproc.contourArea(c);
                    // 사용자가 직접 민감도를 설정할 수 있도록 한다 (일단은 하드코딩)
                    // 움직임이 감지가 되면 특정 구간동안 프레임을 저장하여 동영상을 만든다
                    if (size > sensitivity) {
                        /*
                        여기 아래 코드는 검출된 움직임을 표시해주는 부분
                        Rect r = Imgproc.boundingRect(c);
                        Imgproc.rectangle(in, new Point(r.x, r.y),
                                new Point(r.x + r.width, r.y + r.height), new Scalar(255, 10, 10));
                        */
                        cctvStatus.set("Detected");
                        isDetected = true;
                        action.change();
                        break;
                    }
                }
                return new RecorderObject(in, isDetected);
            } finally {
                if (grayFrame != null)
                    grayFrame.release();
                if (elem != null)
                    elem.release();
                if (temp != null)
                    temp.release();
                if (fgMask != null)
                    fgMask.release();
                if (contours != null) {
                    for (MatOfPoint c : contours)
                        c.release();
                }
            }
        }

        /**
         * @param detected: detectMoving의 결과 진리값
         * 만약, Detected 상태에서 특정 값 이하로 떨어지면 UnDetected 상태로 변경한다
         */
        public abstract void updateCounter(boolean detected);

        public abstract void clear();
    }

    private RecorderBehavior detected = new RecorderBehavior() {
        List<Mat> frames = new ArrayList<>();

        private String saveVideo() {
            // 비디오 저장을 위한 설정을 한다
            VideoWriter videoWriter = null;
            File file = null;
            try{
                videoWriter = new VideoWriter();
                String fileName = File.createTempFile("[owlcctv]", ".avi").getName();
                file = new File(OwlSettings.saveDir, fileName);
                // 여기 아래에서 Index out of range exception이 발생할 수 있을까?
                Mat frame = frames.get(0);
                Size size = frame.size();
                videoWriter.open(file.getPath(), VideoWriter.fourcc('M', 'J', 'P', 'G'), FPS, size);
                for (Mat f : frames) {
                    videoWriter.write(f);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 동영상 저장이 마무리되면 저장한 프레임의 메모리를 해제한다
                for (Mat f : frames) {
                    f.release();
                }
                frames.clear();
                if (videoWriter != null)
                    videoWriter.release();
            }
            if (file != null) {
                // 수정됨
                taskQueue.add(file.getPath());
                return file.getParent();
            } else {
                return null;
            }
        }

        public void clear() {
            if (frames != null) {
                for (Mat f : frames)
                    f.release();
                frames.clear();
            }
        }

        @Override
        public RecorderObject detectMoving(Mat in, int sensitivity) {
            RecorderObject d = super.detectMoving(in, sensitivity, new ChangeState() {
                @Override
                public void change() { }
            });
            // 움직임이 검출된 프레임을 저장한다 (detected인 경우 모두 저장한다)
            Mat saveFrame = new Mat();
            d.outFrame.copyTo(saveFrame);
            frames.add(saveFrame);

            // 만약 움직임이 검출된 경우라면 프레임을 저장한다
            if (d.isDetected) {
                Log.d(TAG, "frame size: " + frames.size());
                counterFrame = FRAME_COUNT;
            }
            return d;
        }

        @Override
        public void updateCounter(boolean detected) {
            if (counterFrame == 0) {
                counterFrame = FRAME_COUNT;
                // 상태를 변경한다
                recorderBehavior = unDetected;
                // 세이브 시퀀스를 실행한다 (백그라운드로)
                this.saveVideo();
                cctvStatus.set("");
            }

            counterFrame -= 1;
            Log.d(TAG, "detected - updateCounter / counter: " + counterFrame);
        }
    };

    private RecorderBehavior unDetected = new RecorderBehavior() {
        @Override
        public RecorderObject detectMoving(Mat in, int sensitivity) {
            RecorderObject d = super.detectMoving(in, sensitivity, new ChangeState() {
                @Override
                public void change() {
                    recorderBehavior = detected;
                }
            });
            // 움직임이 검출되지 않았다면 잠시간 wait 한다
            if (d != null && !d.isDetected) {
                try {
                    Thread.sleep(700);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return d;
        }

        @Override
        public void clear() { }

        @Override
        public void updateCounter(boolean detected) {
            Log.d(TAG, "unDetected - updateCounter");
        }
    };

    private RecorderBehavior recorderBehavior = null;

    private void showToast(String message) {
        VerticalTextView textView = new VerticalTextView(activity);
        textView.setText(message);
        textView.setHeight(300);
        textView.setTextColor(ContextCompat.getColor(activity, R.color.cctv_toast_color));
        Toast toast = new Toast(activity);
        toast.setView(textView);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void startOrStopCCTV() {
        if (isRecordingNow.get()) {
            // 현재 동영상을 촬영중이었다면, 촬영을 종료한다 (frame은 모두 비운다)
            // 스케일 업 이벤트를 적용한다
            final Animation scaleUp = AnimationUtils.loadAnimation(activity, R.anim.small_to_big);
            final Button camButton = (Button) activity.findViewById(R.id.btnStartCam);
            scaleUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    camButton.setBackgroundResource(R.drawable.red_circle);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    /*
                    아래 코드는 이 사이트를 참조하여 작성하였다1
                    http://stackoverflow.com/questions/11623203/how-to-resize-the-button-progmmatically-in-android
                     */
                    ViewGroup.LayoutParams params = camButton.getLayoutParams();
                    float scale = activity.getApplicationContext().getResources().getDisplayMetrics().density;
                    params.width = (int)(60 * scale + 0.5f);
                    params.height = (int)(60 * scale + 0.5f);
                    camButton.setLayoutParams(params);
                    camButton.requestLayout();
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
            camButton.startAnimation(scaleUp);
            isRecordingNow.set(false);
            camView.disableView();
            detected.clear();
        } else {
            // 스케일 다운 이벤트를 적용한다
            final Animation scaleDown = AnimationUtils.loadAnimation(activity, R.anim.big_to_small);
            final Button camButton = (Button) activity.findViewById(R.id.btnStartCam);
            scaleDown.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    /*
                    아래 코드는 이 사이트를 참조하여 작성하였다
                    http://stackoverflow.com/questions/11623203/how-to-resize-the-button-progmmatically-in-android
                     */
                    camButton.setBackgroundResource(R.drawable.red_rect);
                    ViewGroup.LayoutParams params = camButton.getLayoutParams();
                    float scale = activity.getApplicationContext().getResources().getDisplayMetrics().density;
                    params.width = (int)(35 * scale + 0.5f);
                    params.height = (int)(35 * scale + 0.5f);
                    camButton.setLayoutParams(params);
                    camButton.requestLayout();
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
            camButton.startAnimation(scaleDown);
            isRecordingNow.set(true);
            // 카메라를 활성화시킨다
            camView.enableView();
        }
        Log.d(TAG, "startOrStopCCTV Finished");
    }

    public void navToRecordList() {
        Intent intent = new Intent(activity, RecordedAvtivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    public void toggleOnSaveEnergy() {
        if (this.isTurnOnSaveEnergy.get()) {
            final Animation toSmall = AnimationUtils.loadAnimation(activity, R.anim.big_to_small);
            final ImageButton btnSaveEnergy = (ImageButton)activity.findViewById(R.id.btnSaveEnergy);
            toSmall.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    btnSaveEnergy.setBackgroundResource(R.drawable.circle_button);
                }

                @Override
                public void onAnimationEnd(Animation animation) { }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
            btnSaveEnergy.startAnimation(toSmall);
            this.isTurnOnSaveEnergy.set(false);
            this.showToast("절약모드 해제");
        } else {
            final Animation toSmall = AnimationUtils.loadAnimation(activity, R.anim.big_to_small);
            final ImageButton btnSaveEnergy = (ImageButton)activity.findViewById(R.id.btnSaveEnergy);
            toSmall.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    btnSaveEnergy.setBackgroundResource(R.drawable.circle_button_filled_color);
                }

                @Override
                public void onAnimationEnd(Animation animation) { }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            btnSaveEnergy.startAnimation(toSmall);
            this.isTurnOnSaveEnergy.set(true);
            this.showToast("절약모드 설정");
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, " onCreate start");
        // 설정 변수를 초기화한다
        this.isRecordingNow.set(false);
        this.isTurnOnSaveEnergy.set(false);
        this.netStatus.set("네트워크 연결되지 않음");
        // 리시버 설정을 한다
        this.netConnReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    ConnectivityManager conManager =
                            (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mobNetInfo = conManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                    ImageView netStatus = (ImageView)activity.findViewById(R.id.ivNetStat);
                    if ("DISCONNECTED".equals(mobNetInfo.getDetailedState().toString())) {
                        netStatus.setImageResource(R.drawable.net_off);
                    } else {
                        netStatus.setImageResource(R.drawable.net_on);
                    }
                    networkStatus.set(mobNetInfo.getDetailedState().toString());
                }
            }
        };
        IntentFilter netFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        activity.registerReceiver(this.netConnReceiver, netFilter);

        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 카메라를 활성화 시키고 카메라 영상 입력 리스너를 설정한다
        camView = (CameraBridgeViewBase) activity.findViewById(R.id.surfaceView);
        camView.setVisibility(SurfaceView.VISIBLE);
        camView.setCvCameraViewListener(this);
        // FFmpeg 설정
        this.ffmpeg = FFmpeg.getInstance(this.activity);
        try {
            this.ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    super.onFailure();
                }

                @Override
                public void onSuccess() {
                    super.onSuccess();
                }

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
        // 동영상 변환 스레드 생성 및 실행
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stopWorker) {
                    try {
                        // 태스크가 큐에 존재하고,
                        if (taskQueue.size() != 0) {
                            // FFmpeg 커맨드가 실행중이지 않는다면 동영상 변환을 실행하라
                            if (!ffmpeg.isFFmpegCommandRunning()) {
                                final String inPath = taskQueue.poll();
                                final String outPath = inPath.replace(".avi", ".mp4");
                                final String[] commands = {"-y", "-i", inPath, outPath};
                                Log.d(TAG, inPath + " task entered");
                                ffmpeg.execute(commands, new ExecuteBinaryResponseHandler() {
                                    @Override
                                    public void onSuccess(String message) {
                                        super.onSuccess(message);
                                    }

                                    @Override
                                    public void onProgress(String message) {
                                        super.onProgress(message);
                                    }

                                    @Override
                                    public void onFailure(String message) {
                                        super.onFailure(message);
                                    }

                                    @Override
                                    public void onStart() {
                                        super.onStart();
                                    }

                                    @Override
                                    public void onFinish() {
                                        // 원본 파일을 삭제한다
                                        super.onFinish();
                                        File f = new File(inPath);
                                        if (!f.delete()) {
                                            Log.d(TAG, "file is not deleted");
                                        }
                                        // 썸네일 이미지를 생성하여 로컬에 저장한다
                                        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(
                                                outPath,
                                                MediaStore.Images.Thumbnails.MICRO_KIND);
                                    }
                                });
                            }
                        } else {
                            Thread.sleep(1000);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        worker.start();
        Log.d(TAG, "onCreate Finished");
    }

    @Override
    public void onResume() {
        // OpenCV 라이브러리를 로드한다.
        // 내부 패키지에 없다면 OpenCV Manager를 사용한다
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, activity, loaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        this.recorderBehavior = unDetected;
        Log.d(TAG, "onResume Finished");
    }

    @Override
    public void onPause() {
        if (camView != null) {
            camView.disableView();
        }
        if (this.netConnReceiver != null) {
            activity.unregisterReceiver(this.netConnReceiver);
        }
        if (detected != null) {
            detected.clear();
        }
        Log.d(TAG, "onPause Finished");
    }

    @Override
    public void onDestroy() {
        stopWorker = true;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "onCameraViewStarted Fired");
    }

    @Override
    public void onCameraViewStopped() {
        Log.d(TAG, "onCameraViewStopped Fired");
        cctvStatus.set("Stopped");
    }

    /**
     * @param srcFrame: 90도 회전할 이미지
     * @return Mat: 90도 회전된 이미지
     */
    private Mat rotateDegree(Mat srcFrame, double angle) {
        // 아래 코드는 이 사이트를 참조하여 작성되었다
        // http://stackoverflow.com/questions/12949793/rotate-videocapture-in-opencv-on-android
        int centerX = Math.round(srcFrame.width()/2);
        int centerY = Math.round(srcFrame.height()/2);
        Point center = new Point(centerX, centerY);
        double scale = 1;

        int rotatedHeight = Math.round(srcFrame.height());
        int rotatedWidth = Math.round(srcFrame.width());

        Mat mapMat = Imgproc.getRotationMatrix2D(center, angle, scale);
        Size rotatedSize = new Size(rotatedWidth, rotatedHeight);
        Mat rotatedMat = new Mat(rotatedSize, srcFrame.type());

        Imgproc.warpAffine(srcFrame, rotatedMat, mapMat, rotatedMat.size(), Imgproc.INTER_LINEAR);
        return rotatedMat;
    }

    /**
     * @param inFrame: 카메라로 들어오는 프레임 이미지
     * @return Mat: camView에 연결된 화면에 출력될 이미지
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inFrame) {
        try {
            Mat in = inFrame.rgba();
            RecorderObject d = this.recorderBehavior.detectMoving(in, 300);
            this.recorderBehavior.updateCounter(d.isDetected);
            if (this.isTurnOnSaveEnergy.get()) {
                d.outFrame.setTo(new Scalar(0, 0, 0));
            }
            return d.outFrame;
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
