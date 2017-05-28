package com.duck.owlcctv.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.duck.owlcctv.R;


public class SettingsActivity extends PreferenceActivity {
    private static final String TAG = "[SettingsActivity]";

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        /*
        만약, 파일(settings.xml)을 읽는 도중에 에러가 발생해 다 읽어들이지 못한 경우
        전체 파일이 다 셋팅되지 못하는 경우가 발생할 수 있다.
        트랜잭션을 적용하면, 위와 같은 문제가 발생하였을때
        전체를 롤백하기 때문에 이 문제를 해결할 수 있다
        (일반 Layout과는 다르게 Preference는 지속적으로 파일에 값을 쓰고 입력해서 이런 방법을 취하는 건가?)
         */
        super.getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsPreferenceFragment()).commit();
    }

    /**
     * Preference를 초기화한다
     */
    public static final class SettingsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstance) {
            super.onCreate(savedInstance);
            super.addPreferencesFromResource(R.xml.settings);

            SharedPreferences sharedPref = getActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
            boolean isAuthed = sharedPref.getBoolean("isAuthed", false);

            Preference authPref = this.findPreference("prefAuthUser");
            if (isAuthed) {
                authPref.setSummary(R.string.settings_auth_app_checked);
            } else {
                authPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    final Activity activity = getActivity();

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(activity, AuthLoginActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                        return true;
                    }
                });
                authPref.setSummary(R.string.settings_auth_app_unchecked);
            }



        }
    }
}
