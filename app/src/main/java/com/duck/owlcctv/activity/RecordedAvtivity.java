package com.duck.owlcctv.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.duck.owlcctv.R;
import com.duck.owlcctv.databinding.ActivityRecordedBinding;
import com.duck.owlcctv.viewmodel.RecordedViewModel;


public class RecordedAvtivity extends AppCompatActivity {
    private final RecordedViewModel model = new RecordedViewModel(this);
    private static final String TAG = "[RecordedActivity]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityRecordedBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_recorded);
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
