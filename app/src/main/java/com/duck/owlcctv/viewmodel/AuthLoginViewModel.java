package com.duck.owlcctv.viewmodel;


import android.content.Intent;
import android.databinding.ObservableField;
import android.support.v7.app.AppCompatActivity;

import com.duck.owlcctv.activity.AuthCodeActivity;

public class AuthLoginViewModel implements BaseViewModel {
    private static final String TAG = "[AuthLoginViewModel]";
    private final AppCompatActivity activity;

    public static final ObservableField<String> userId = new ObservableField<>();
    public static final ObservableField<String> userPw = new ObservableField<>();

    public AuthLoginViewModel(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void doLogin() {
        // API 서버로 사용자에게 입력받은 ID와 PW를 전송한다 (암호화)
        // 로그인에 성공하면 로컬에 데이터를 저장하고 인증 페이지로 이동한다
        // 로컬에 저장된 데이터는 나중에 다시 로그인하지 않도록 하기위해 사용된다
        Intent intent = new Intent(this.activity, AuthCodeActivity.class);
        this.activity.startActivity(intent);
        this.activity.finish();
    }

    @Override
    public void onCreate() { }

    @Override
    public void onResume() { }

    @Override
    public void onPause() { }

    @Override
    public void onDestroy() { }
}
