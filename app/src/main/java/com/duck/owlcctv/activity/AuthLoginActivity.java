package com.duck.owlcctv.activity;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.duck.owlcctv.R;
import com.duck.owlcctv.databinding.ActivityAuthLoginBinding;
import com.duck.owlcctv.viewmodel.AuthLoginViewModel;

public class AuthLoginActivity extends AppCompatActivity {
    private static final String TAG = "AuthLoginActivity";
    private final AuthLoginViewModel viewModel = new AuthLoginViewModel(this);

    @Override
    protected  void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        ActivityAuthLoginBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_auth_login);
        viewModel.onCreate();
        binding.setViewModel(viewModel);
    }
}
