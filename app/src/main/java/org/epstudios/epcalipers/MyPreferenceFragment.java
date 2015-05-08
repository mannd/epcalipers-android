
package org.epstudios.epcalipers;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.util.HashMap;
import java.util.Map;


public class MyPreferenceFragment extends PreferenceFragment implements
        OnSharedPreferenceChangeListener {

    private final static Map<Integer, String> colors = createMap();

    private static Map<Integer, String> createMap() {
        Map<Integer, String> map = new HashMap<>();
        // caliper colors
        map.put(-16777216, "Black");
        map.put(-65281, "Magenta");
        map.put(-3355444, "Light Gray");
        map.put(-16776961, "Blue");
        map.put(-16711936, "Green");
        map.put(-1, "White");
        // highlight colors
        map.put(-65536, "Red");
        map.put(-256, "Yellow");
        map.put(-12303292, "Dark Gray");
        // line widths
        map.put(1, "1 point");
        map.put(2, "2 points");
        map.put(3, "3 points");


        return map;
    }

    //keys
    String defaultTimeCalibrationKey;
    String defaultAmplitudeCalibrationKey;
    String defaultCaliperColorKey;
    String defaultHighlightColorKey;
    String defaultLineWidthKey;


    private String defaultCaliperColor;
    private String defaultHighlightColor;
    private String defaultLineWidth;
    private String defaultTimeCalibration;
    private String defaultAmplitudeCalibration;
    private boolean showStartImage;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        defaultTimeCalibrationKey = activity.getString(R.string.default_time_calibration_key);
        defaultAmplitudeCalibrationKey = activity.getString(R.string.default_amplitude_calibration_key);
        defaultCaliperColorKey = activity.getString(R.string.default_caliper_color_key);
        defaultHighlightColorKey = activity.getString(R.string.default_highlight_color_key);
        defaultLineWidthKey = activity.getString(R.string.default_line_width_key);
        defaultCaliperColor = activity.getString(R.string.default_caliper_color);
        defaultHighlightColor = activity.getString(R.string.default_highlight_color);
        defaultLineWidth = activity.getString(R.string.default_line_width);
        showStartImage = true;
        defaultTimeCalibration = activity.getString(R.string.default_time_calibration_value);
        defaultAmplitudeCalibration = activity.getString(R.string.default_amplitude_calibration_value);
        addPreferencesFromResource(R.xml.settings);

        Preference defaultTimeCalibrationPreference = findPreference(defaultTimeCalibrationKey);
        defaultTimeCalibrationPreference.setSummary(getPreferenceScreen()
                .getSharedPreferences()
                .getString(defaultTimeCalibrationKey, defaultTimeCalibration));

        Preference defaultAmplitudeCalibrationPreference = findPreference(defaultAmplitudeCalibrationKey);
        defaultAmplitudeCalibrationPreference.setSummary(getPreferenceScreen()
                .getSharedPreferences()
                .getString(defaultAmplitudeCalibrationKey, defaultAmplitudeCalibration));

        Preference defaultCaliperColorPreference = findPreference(defaultCaliperColorKey);
        String defaultCaliperColorValue = getPreferenceScreen()
                .getSharedPreferences()
                .getString(defaultCaliperColorKey, defaultCaliperColor);
        String defaultCaliperColorName = colors.get(Integer.parseInt(defaultCaliperColorValue));
        defaultCaliperColorPreference.setSummary(defaultCaliperColorName);

        Preference defaultHighlightColorPreference = findPreference(defaultHighlightColorKey);
        String defaultHighlightColorValue = getPreferenceScreen()
                .getSharedPreferences()
                .getString(defaultHighlightColorKey, defaultHighlightColor);
        String defaultHighlightColorName = colors.get(Integer.parseInt(defaultHighlightColorValue));
        defaultHighlightColorPreference.setSummary(defaultHighlightColorName);

        Preference defaultLineWidthPreference = findPreference(defaultLineWidthKey);
        String defaultLineWidthValue = getPreferenceScreen()
                .getSharedPreferences()
                .getString(defaultLineWidthKey, defaultLineWidth);
        String defaultLineWidthName = colors.get(Integer.parseInt(defaultLineWidthValue));
        defaultLineWidthPreference.setSummary(defaultLineWidthName);


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference pref = findPreference(key);
        if (key.equals(defaultTimeCalibrationKey)) {
            pref.setSummary(sharedPreferences.getString(key, defaultTimeCalibration));
        }
        else if (key.equals(defaultAmplitudeCalibrationKey)) {
            pref.setSummary(sharedPreferences.getString(key, defaultTimeCalibration));
        }
        else if (key.equals(defaultCaliperColorKey)) {
            pref.setSummary(colors.get(Integer.parseInt(sharedPreferences.getString(key, defaultCaliperColor))));
        }
        else if (key.equals(defaultHighlightColorKey)) {
            pref.setSummary(colors.get(Integer.parseInt(sharedPreferences.getString(key, defaultHighlightColor))));
        }
        else if (key.equals(defaultLineWidthKey)) {
            pref.setSummary(colors.get(Integer.parseInt(sharedPreferences.getString(key, defaultLineWidth))));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

}