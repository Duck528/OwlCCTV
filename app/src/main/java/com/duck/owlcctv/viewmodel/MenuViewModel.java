package com.duck.owlcctv.viewmodel;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.duck.owlcctv.R;
import com.duck.owlcctv.activity.CCTVActivity;
import com.duck.owlcctv.activity.RecordedAvtivity;
import com.duck.owlcctv.model.Recorded;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;


public class MenuViewModel implements BaseViewModel {
    private static final String TAG = "[BaseViewModel]";
    private AppCompatActivity activity = null;

    public MenuViewModel(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void navToCCTV() {
        // 흔들기 효과를 준다
        LinearLayout cctvButton = (LinearLayout) activity.findViewById(R.id.btn01);
        final Animation shakeAnim = AnimationUtils.loadAnimation(activity, R.anim.shake);
        shakeAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 해당 페이지로 이동한다
                Log.d(TAG, "navToCCTV Fired");
                Intent intent = new Intent(activity, CCTVActivity.class);
                activity.startActivity(intent);
            }
             @Override
             public void onAnimationRepeat(Animation animation) { }
        });
        cctvButton.startAnimation(shakeAnim);
    }

    public void navToRecorded() {
        // 흔들기 효과를 준다
        LinearLayout cctvButton = (LinearLayout) activity.findViewById(R.id.btn02);
        final Animation shakeAnim = AnimationUtils.loadAnimation(activity, R.anim.shake);
        shakeAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 해당 페이지로 이동한다
                Log.d(TAG, "navToRecorded Fired");
                Intent intent = new Intent(activity, RecordedAvtivity.class);
                activity.startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        cctvButton.startAnimation(shakeAnim);
    }

    public void navToSettings() {
        // 흔들기 효과를 준다
        LinearLayout cctvButton = (LinearLayout) activity.findViewById(R.id.btn03);
        final Animation shakeAnim = AnimationUtils.loadAnimation(activity, R.anim.shake);
        shakeAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 해당 페이지로 이동한다
                Log.d(TAG, "navToRecorded Fired");
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        cctvButton.startAnimation(shakeAnim);
    }

    public void quitApp() {
        // 흔들기 효과를 준다
        LinearLayout cctvButton = (LinearLayout) activity.findViewById(R.id.btn04);
        final Animation shakeAnim = AnimationUtils.loadAnimation(activity, R.anim.shake);
        shakeAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 앱 사용 기록을 제거하고 종료한다
                Log.d(TAG, "navToRecorded Fired");
                activity.finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        cctvButton.startAnimation(shakeAnim);
    }

    @Override
    public void onCreate() { }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }
}
