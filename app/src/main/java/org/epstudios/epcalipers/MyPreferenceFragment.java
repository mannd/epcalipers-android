
package org.epstudios.epcalipers;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;


public class MyPreferenceFragment extends PreferenceFragment implements
        OnSharedPreferenceChangeListener {

    public static final String BAZETT = "bazett";
    public static final String FRAMINGHAM = "framingham";
    public static final String HODGES = "hodges";
    public static final String FRIDERICIA = "fridericia";
    public static final String ALL = "all";

    private SparseArray<String> names = null;

    private SparseArray<String> createMap(Activity activity) {
        SparseArray<String> map = new SparseArray<>();
        // caliper names
        map.put(-16777216, activity.getString(R.string.black_color));
        map.put(-65281, getString(R.string.magenta_color));
        map.put(-3355444, getString(R.string.light_gray_color));
        map.put(-16776961, getString(R.string.blue_color));
        map.put(-16711936, getString(R.string.green_color));
        map.put(-1, getString(R.string.white_color));
        // highlight names
        map.put(-65536, getString(R.string.red_color));
        map.put(-256, getString(R.string.yellow_color));
        map.put(-12303292, getString(R.string.dark_gray_color));
        // line widths
        map.put(1, getString(R.string.one_point));
        map.put(2, getString(R.string.two_points));
        map.put(3, getString(R.string.three_points));
        map.put(4, getString(R.string.four_points));
        map.put(5, getString(R.string.five_points));
        map.put(6, getString(R.string.six_points));
        map.put(7, getString(R.string.seven_points));
        map.put(8, getString(R.string.eight_points));

        return map;
    }

    private Map<String, String> formulaNames = null;
    private Map<String, String> createFormulaNamesMap(Activity activity) {
        Map<String, String> map = new HashMap<>();
        map.put(BAZETT, activity.getString(R.string.bazett_formula));
        map.put(FRAMINGHAM, activity.getString(R.string.framingham_formula));
        map.put(HODGES, activity.getString(R.string.hodges_formula));
        map.put(FRIDERICIA, activity.getString(R.string.fridericia_formula));
        map.put(ALL, activity.getString(R.string.all_formulas));
        return map;
    }

    private Map<String, String> textPositionNames = null;
    private Map<String, String> createTextPositionNamesMap(Activity activity) {
        Map<String, String> map = new HashMap<>();
        map.put("centerAbove", activity.getString(R.string.center_above));
        map.put("centerBelow", activity.getString(R.string.center_below));
        map.put("left", activity.getString(R.string.left));
        map.put("right", activity.getString(R.string.right));
        map.put("top", activity.getString(R.string.top));
        map.put("bottom", activity.getString(R.string.bottom));
        return map;
    }

    //keys
    private String defaultTimeCalibrationKey;
    private String defaultAmplitudeCalibrationKey;
    private String defaultCaliperColorKey;
    private String defaultHighlightColorKey;
    private String defaultLineWidthKey;
    private String defaultQtcFormulaKey;
    private String defaultTimeCaliperTextPositionKey;
    private String defaultAmplitudeCaliperTextPositionKey;

    private String defaultCaliperColor;
    private String defaultHighlightColor;
    private String defaultLineWidth;
    private String defaultTimeCalibration;
    private String defaultAmplitudeCalibration;
    private String defaultQtcFormula;
    private String defaultTimeCaliperTextPosition;
    private String defaultAmplitudeCaliperTextPosition;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        names = createMap(activity);
        formulaNames = createFormulaNamesMap(activity);
        textPositionNames = createTextPositionNamesMap(activity);
        defaultTimeCalibrationKey = activity.getString(R.string.time_calibration_key);
        defaultAmplitudeCalibrationKey = activity.getString(R.string.amplitude_calibration_key);
        defaultCaliperColorKey = activity.getString(R.string.caliper_color_key);
        defaultHighlightColorKey = activity.getString(R.string.highlight_color_key);
        defaultLineWidthKey = activity.getString(R.string.line_width_key);
        defaultQtcFormulaKey = activity.getString(R.string.qtc_formula_key);
        defaultTimeCaliperTextPositionKey = activity.getString(R.string.time_caliper_text_position_key);
        defaultAmplitudeCaliperTextPositionKey = activity.getString(R.string.amplitude_caliper_text_position_key);

        defaultCaliperColor = activity.getString(R.string.default_caliper_color);
        defaultHighlightColor = activity.getString(R.string.default_highlight_color);
        defaultLineWidth = activity.getString(R.string.default_line_width);
        defaultTimeCalibration = activity.getString(R.string.default_time_calibration_value);
        defaultAmplitudeCalibration = activity.getString(R.string.default_amplitude_calibration_value);
        defaultQtcFormula = activity.getString(R.string.qtc_formula_value);
        defaultTimeCaliperTextPosition = activity.getString(R.string.time_caliper_text_position_value);
        defaultAmplitudeCaliperTextPosition = activity.getString(R.string.amplitude_caliper_text_position_value);

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
        int lineWidth;
        try {
            lineWidth = Integer.parseInt(defaultLineWidthValue);
        }
        catch (Exception ex) {
            lineWidth = Integer.parseInt(defaultLineWidth);
        }
        String defaultLineWidthName = names.get(lineWidth);
        defaultLineWidthPreference.setSummary(defaultLineWidthName);

        Preference defaultQtcFormulaPreference = findPreference(defaultQtcFormulaKey);
        String defaultQtcFormulaValue = getPreferenceScreen()
                .getSharedPreferences()
                .getString(defaultQtcFormulaKey, defaultQtcFormula);
        String defaultQtcFormulaName = formulaNames.get(defaultQtcFormulaValue);
        defaultQtcFormulaPreference.setSummary(defaultQtcFormulaName);

        Preference defaultTimeCaliperTextPositionPreference = findPreference(defaultTimeCaliperTextPositionKey);
        String defaultTimeCaliperTextPositionValue = getPreferenceScreen()
                .getSharedPreferences()
                .getString(defaultTimeCaliperTextPositionKey, defaultTimeCaliperTextPosition);
        String defaultTimeCaliperTextPositionName = textPositionNames.get(defaultTimeCaliperTextPositionValue);
        defaultTimeCaliperTextPositionPreference.setSummary(defaultTimeCaliperTextPositionName);

        Preference defaultAmplitudeCaliperTextPositionPreference = findPreference(defaultAmplitudeCaliperTextPositionKey);
        String defaultAmplitudeCaliperTextPositionValue = getPreferenceScreen()
                .getSharedPreferences()
                .getString(defaultAmplitudeCaliperTextPositionKey, defaultAmplitudeCaliperTextPosition);
        String defaultAmplitudeCaliperTextPositionName = textPositionNames.get(defaultAmplitudeCaliperTextPositionValue);
        defaultAmplitudeCaliperTextPositionPreference.setSummary(defaultAmplitudeCaliperTextPositionName);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference pref = findPreference(key);
        if (key.equals(defaultTimeCalibrationKey)) {
            pref.setSummary(sharedPreferences.getString(key, defaultTimeCalibration));
        }
        else if (key.equals(defaultAmplitudeCalibrationKey)) {
            pref.setSummary(sharedPreferences.getString(key, defaultAmplitudeCalibration));
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
            formulaName = formulaNames.get(formulaName);
            pref.setSummary(formulaName);
        }
        else if (key.equals(defaultTimeCaliperTextPositionKey)) {
            String textPositionName = sharedPreferences.getString(key, defaultTimeCaliperTextPosition);
            textPositionName = textPositionNames.get(textPositionName);
            pref.setSummary(textPositionName);
        }
        else if (key.equals(defaultAmplitudeCaliperTextPositionKey)) {
            String textPositionName = sharedPreferences.getString(key, defaultAmplitudeCaliperTextPosition);
            textPositionName = textPositionNames.get(textPositionName);
            pref.setSummary(textPositionName);
        }
    }

    private String getNameFromKey(SharedPreferences sharedPreferences, String key, String defaultName) {
        try {
            int value = Integer.parseInt(sharedPreferences.getString(key, defaultName));
            return names.get(value);
        } catch (Exception ex) {
            //noinspection SuspiciousMethodCalls
            return getString(R.string.error);
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