package com.duck.owlcctv.util;


import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;


public class VideoConverter {
    private static final String TAG = "[VideoConverter]";
    private static boolean isFirst = true;
    public static void fromAviToMp4(AppCompatActivity activity, final String inPath, String outPath) {
        if (isFirst) {
            initFFmpeg(activity);
        }
        FFmpeg ffmpeg = FFmpeg.getInstance(activity);
        try {
            String[] commands = {"-y", "-i", inPath, outPath};
            ffmpeg.execute(commands, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                    Log.d(TAG, "start");
                }

                @Override
                public void onProgress(String message) {
                    Log.d(TAG, "progress: " + message);
                }

                @Override
                public void onFailure(String message) {
                    Log.d(TAG, "fail: " + message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "success: " + message);
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "finished");
                    File f = new File(inPath);
                    if (!f.delete()) {
                        Log.d(TAG, "file is not deleted");
                    }
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private static void initFFmpeg(AppCompatActivity activity) {
        FFmpeg fFmpeg = FFmpeg.getInstance(activity);
        try {
            isFirst = false;
            fFmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() { }

                @Override
                public void onSuccess() { }

                @Override
                public void onStart() { }

                @Override
                public void onFinish() { }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }
}
