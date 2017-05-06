package com.duck.owlcctv.viewmodel;

/**
 * 모든 ViewModel은 아래 BaseViewModel을 상속받는다
 */

public interface BaseViewModel {
    void onCreate();
    void onResume();
    void onPause();
    void onDestroy();
}
