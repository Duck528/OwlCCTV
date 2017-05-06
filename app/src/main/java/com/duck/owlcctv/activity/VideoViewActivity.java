package com.duck.owlcctv.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import com.duck.owlcctv.R;


public class VideoViewActivity extends FragmentActivity {
    private static final String TAG = "[VideoViewActivity]";
    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_videoview);

        Intent intent = getIntent();
        String videoPath = intent.getStringExtra("videoPath");

        final MediaController controller = new MediaController(this);

        VideoView videoView = (VideoView)findViewById(R.id.videoView);
        videoView.setMediaController(controller);
        videoView.setVideoPath(videoPath);
        videoView.start();
    }
}
