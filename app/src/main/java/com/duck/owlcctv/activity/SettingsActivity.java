package com.duck.owlcctv.activity;


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
        super.getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsPreferenceFragment()).commit();
    }

    public static final class SettingsPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstance) {
            super.onCreate(savedInstance);
            super.addPreferencesFromResource(R.xml.settings);
        }
    }
}
