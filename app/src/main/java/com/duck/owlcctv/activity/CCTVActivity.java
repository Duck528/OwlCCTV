package com.duck.owlcctv.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.duck.owlcctv.R;
import com.duck.owlcctv.databinding.ActivityCctvBinding;
import com.duck.owlcctv.viewmodel.CCTVViewModel;


public class CCTVActivity extends AppCompatActivity {
    private static final String TAG = "[CCTVActivity]";
    private CCTVViewModel model = new CCTVViewModel(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCctvBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_cctv);
        binding.setModel(model);
        model.onCreate();
        Log.d(TAG, "onCreate Finished");
    }

    @Override
    protected void onResume() {
        super.onResume();
        model.onResume();
        Log.d(TAG, "onResume Finished");
    }

    @Override
    protected void onPause() {
        super.onPause();
        model.onPause();
        Log.d(TAG, "onPause Finished");
    }
}
