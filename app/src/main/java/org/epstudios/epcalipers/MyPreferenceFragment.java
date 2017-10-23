
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

    private final static Map<Integer, String> names = createMap();

    private static Map<Integer, String> createMap() {
        Map<Integer, String> map = new HashMap<>();
        // caliper names
        map.put(-16777216, "Black");
        map.put(-65281, "Magenta");
        map.put(-3355444, "Light Gray");
        map.put(-16776961, "Blue");
        map.put(-16711936, "Green");
        map.put(-1, "White");
        // highlight names
        map.put(-65536, "Red");
        map.put(-256, "Yellow");
        map.put(-12303292, "Dark Gray");
        // line widths
        map.put(1, "1 point");
        map.put(2, "2 points");
        map.put(3, "3 points");
        map.put(4, "4 points");
        map.put(5, "5 points");
        map.put(6, "6 points");
        map.put(7, "7 points");
        map.put(8, "8 points");

        return map;
    }

    //keys
    private String defaultTimeCalibrationKey;
    private String defaultAmplitudeCalibrationKey;
    private String defaultCaliperColorKey;
    private String defaultHighlightColorKey;
    private String defaultLineWidthKey;
    private String defaultQtcFormulaKey;


    private String defaultCaliperColor;
    private String defaultHighlightColor;
    private String defaultLineWidth;
    private String defaultTimeCalibration;
    private String defaultAmplitudeCalibration;
    private String defaultQtcFormula;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        defaultTimeCalibrationKey = activity.getString(R.string.default_time_calibration_key);
        defaultAmplitudeCalibrationKey = activity.getString(R.string.default_amplitude_calibration_key);
        defaultCaliperColorKey = activity.getString(R.string.default_caliper_color_key);
        defaultHighlightColorKey = activity.getString(R.string.default_highlight_color_key);
        defaultLineWidthKey = activity.getString(R.string.default_line_width_key);
        defaultQtcFormulaKey = activity.getString(R.string.default_qtc_formula_key);
        defaultCaliperColor = activity.getString(R.string.default_caliper_color);
        defaultHighlightColor = activity.getString(R.string.default_highlight_color);
        defaultLineWidth = activity.getString(R.string.default_line_width);
        defaultTimeCalibration = activity.getString(R.string.default_time_calibration_value);
        defaultAmplitudeCalibration = activity.getString(R.string.default_amplitude_calibration_value);
        defaultQtcFormula = activity.getString(R.string.default_qtc_formula_value);
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
        String defaultCaliperColorName = names.get(Integer.parseInt(defaultCaliperColorValue));
        defaultCaliperColorPreference.setSummary(defaultCaliperColorName);

        Preference defaultHighlightColorPreference = findPreference(defaultHighlightColorKey);
        String defaultHighlightColorValue = getPreferenceScreen()
                .getSharedPreferences()
                .getString(defaultHighlightColorKey, defaultHighlightColor);
        String defaultHighlightColorName = names.get(Integer.parseInt(defaultHighlightColorValue));
        defaultHighlightColorPreference.setSummary(defaultHighlightColorName);

        Preference defaultLineWidthPreference = findPreference(defaultLineWidthKey);
        String defaultLineWidthValue = getPreferenceScreen()
                .getSharedPreferences()
                .getString(defaultLineWidthKey, defaultLineWidth);
        String defaultLineWidthName = names.get(Integer.parseInt(defaultLineWidthValue));
        defaultLineWidthPreference.setSummary(defaultLineWidthName);

        Preference defaultQtcFormulaPreference = findPreference(defaultQtcFormulaKey);
        String defaultQtcFormulaValue = getPreferenceScreen()
                .getSharedPreferences()
                .getString(defaultQtcFormulaKey, defaultQtcFormula);
        // Below is sneaky code, taking advantage of QTc key is just lower case version
        // of QTc value, where first letter is capitalized.  If the QTc formula arrays are
        // changed, this will break.
        String defaultQtcFormulaName = capitalize(defaultQtcFormulaValue);
        defaultQtcFormulaPreference.setSummary(defaultQtcFormulaName);
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
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
            pref.setSummary(getNameFromKey(sharedPreferences, key, defaultCaliperColor));
        }
        else if (key.equals(defaultHighlightColorKey)) {
            pref.setSummary(getNameFromKey(sharedPreferences, key, defaultHighlightColor));
        }
        else if (key.equals(defaultLineWidthKey)) {
            pref.setSummary(getNameFromKey(sharedPreferences, key, defaultLineWidth));
        }
        else if (key.equals(defaultQtcFormulaKey)) {
            String formulaName = sharedPreferences.getString(key, defaultQtcFormula);
            formulaName = capitalize(formulaName);
            pref.setSummary(formulaName);
        }
    }

    private String getNameFromKey(SharedPreferences sharedPreferences, String key, String defaultName) {
        try {
            Integer value = Integer.parseInt(sharedPreferences.getString(key, defaultName));
            return names.get(value);
        } catch (Exception ex) {
            //noinspection SuspiciousMethodCalls
            return names.get(defaultName);
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