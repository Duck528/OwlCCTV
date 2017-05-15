package com.duck.owlcctv.activity;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.duck.owlcctv.R;
import com.duck.owlcctv.databinding.ActivityAuthCodeBinding;
import com.duck.owlcctv.viewmodel.AuthCodeViewModel;

public class AuthCodeActivity extends AppCompatActivity {
    private final AuthCodeViewModel viewModel = new AuthCodeViewModel(this);
    private static final String TAG = "[AuthCodeActivity]";

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        ActivityAuthCodeBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_auth_code);
        binding.setViewModel(viewModel);
        this.viewModel.onCreate();
    }
}
