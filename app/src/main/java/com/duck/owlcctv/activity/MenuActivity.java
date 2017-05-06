package com.duck.owlcctv.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.duck.owlcctv.R;
import com.duck.owlcctv.databinding.ActivityMenuBinding;
import com.duck.owlcctv.util.OwlSettings;
import com.duck.owlcctv.viewmodel.MenuViewModel;

import java.io.File;


public class MenuActivity extends AppCompatActivity {
    private static final String TAG = "[MenuActivity]";
    private MenuViewModel model = new MenuViewModel(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMenuBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_menu);
        binding.setModel(model);

        // owlcctv 디렉토리를 생성한다 (사용자가 지정한 경로)
        OwlSettings.saveDir = Environment.getExternalStorageDirectory().getPath() + "/owlcctv";
        Log.d(TAG, OwlSettings.saveDir);
        File file = new File(OwlSettings.saveDir);
        if (!file.exists()) {
            if (!file.mkdir()) {
                Log.d(TAG, OwlSettings.saveDir + " fail to create directory");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAnimation();
        model.onResume();
    }

    private void startAnimation() {
        LinearLayout out01 = (LinearLayout) findViewById(R.id.btn01);
        final Animation anim01 = AnimationUtils.loadAnimation(this, R.anim.from_topleft);

        LinearLayout out02 = (LinearLayout) findViewById(R.id.btn02);
        final Animation anim02 = AnimationUtils.loadAnimation(this, R.anim.from_topright);

        LinearLayout out03 = (LinearLayout) findViewById(R.id.btn03);
        final Animation anim03 = AnimationUtils.loadAnimation(this, R.anim.from_botleft);

        LinearLayout out04 = (LinearLayout) findViewById(R.id.btn04);
        final Animation anim04 = AnimationUtils.loadAnimation(this, R.anim.from_botright);

        out01.startAnimation(anim01);
        out02.startAnimation(anim02);
        out03.startAnimation(anim03);
        out04.startAnimation(anim04);
    }
}
