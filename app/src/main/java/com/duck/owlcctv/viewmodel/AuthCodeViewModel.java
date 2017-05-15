package com.duck.owlcctv.viewmodel;


import android.content.Intent;
import android.databinding.ObservableField;
import android.support.v7.app.AppCompatActivity;

import com.duck.owlcctv.activity.MenuActivity;

public class AuthCodeViewModel implements BaseViewModel {
    private final AppCompatActivity activity;
    private static final String TAG = "[AuthCodeViewModel]";

    public ObservableField<String> authCode = new ObservableField<>();

    public AuthCodeViewModel(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void doAuth() {
        // API 서버로 인증코드를 전송한다 (로그인 확인 코드와 함께)
        // 인증에 성공하면 MenuActivity로 이동하고 인증코드를 로컬에 저장한다 (암호화해서)
        // 일단은 메뉴로 이동한다
        Intent intent = new Intent(this.activity, MenuActivity.class);
        this.activity.startActivity(intent);
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
