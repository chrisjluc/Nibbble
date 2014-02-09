package com.chrisjluc.nibbble;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

/**
 * Created by chrisjluc on 2/4/2014.
 */
public class SettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_PREF_REPETITION_TIME = "pref_repetition_interval_ms";
    public static final String KEY_PREF_NUMBER_OF_IMAGES = "pref_number_images_to_cycle";
    public static final String KEY_PREF_PHOTOS_FROM_FOLLOWERS = "pref_from_following";
    public static final String KEY_PREF_USERNAME = "pref_username";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_REPETITION_TIME)) {
            ListPreference repetitionPref = (ListPreference) findPreference(key);
            repetitionPref.setSummary(sharedPreferences.getString(key, ""));
        } else if (key.equals(KEY_PREF_NUMBER_OF_IMAGES)) {
            ListPreference numberImagePref = (ListPreference) findPreference(key);
            numberImagePref.setSummary(sharedPreferences.getString(key, ""));
        } else if (key.equals(KEY_PREF_USERNAME)) {
            EditTextPreference usernamePref = (EditTextPreference) findPreference(key);
                usernamePref.setSummary(sharedPreferences.getString(key, ""));
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        ListPreference repetitionPref = (ListPreference) findPreference(KEY_PREF_REPETITION_TIME);
        repetitionPref.setSummary(getPreferenceScreen().getSharedPreferences()
                .getString(KEY_PREF_REPETITION_TIME, ""));
        ListPreference numberImagePref = (ListPreference) findPreference(KEY_PREF_NUMBER_OF_IMAGES);
        numberImagePref.setSummary(getPreferenceScreen().getSharedPreferences().getString(KEY_PREF_NUMBER_OF_IMAGES, ""));
        EditTextPreference usernamePref = (EditTextPreference) findPreference(KEY_PREF_USERNAME);
        String user = getPreferenceScreen().getSharedPreferences().getString(KEY_PREF_USERNAME, "");
        if(!user.isEmpty())
            usernamePref.setSummary(getPreferenceScreen().getSharedPreferences().getString(KEY_PREF_USERNAME, ""));
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
