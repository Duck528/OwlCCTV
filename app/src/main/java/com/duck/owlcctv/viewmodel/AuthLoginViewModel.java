package com.duck.owlcctv.viewmodel;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.ObservableField;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.duck.owlcctv.activity.AuthCodeActivity;
import com.duck.owlcctv.util.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthLoginViewModel implements BaseViewModel {
    private static final String TAG = "[AuthLoginViewModel]";
    private final AppCompatActivity activity;

    public static final ObservableField<String> userId = new ObservableField<>();
    public static final ObservableField<String> userPw = new ObservableField<>();

    private static final String loginUri = "http://owlapi.azurewebsites.net/owlapi/auth";
    /**
     * ObjectMapper는 Configuration이 변경되지 않는 한 Thread Safe를 보장한다
     * 참고: https://stackoverflow.com/questions/3907929/should-i-declare-jacksons-objectmapper-as-a-static-field
     * 글 작성자: StaxMan (Jackson 라이브러리 개발자)
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    public AuthLoginViewModel(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void doLogin() {
        // API 서버로 사용자에게 입력받은 ID와 PW를 전송한다
        Thread httpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Map<String, Object> m = new HashMap<>();
                    m.put("email", "asa9105@naver.com");
                    m.put("password", "1234");

                    String resp = HttpUtil.post(loginUri, m);
                    if (resp == null) {
                        return;
                    }
                    Map<String, Object> resMap = mapper.readValue(
                            resp,
                            new TypeReference<HashMap<String, Object>>() {});
                    Integer code = (Integer)resMap.get("code");
                    if (code == 200) {
                        String token = (String)resMap.get("token");

                        // 인증 정보를 저장한다
                        SharedPreferences sharedPref = activity.getSharedPreferences("auth", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("email", userId.get());
                        editor.putString("password", userId.get());
                        editor.putString("token", token);
                        editor.apply();

                        Intent intent = new Intent(activity, AuthCodeActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        httpThread.start();
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
