
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
    private final int DEFAULT_LINE_WIDTH = 2;

    public static final String BAZETT = "bazett";
    public static final String FRAMINGHAM = "framingham";
    public static final String HODGES = "hodges";
    public static final String FRIDERICIA = "fridericia";
    public static final String ALL = "all";

    private SparseArray<String> lineWidthNames = null;
    private SparseArray<String> createLineWidthMap() {
        SparseArray<String> map = new SparseArray<>();
        // line widths
        map.put(1, getString(R.string.one_point));
        map.put(2, getString(R.string.two_points));
        map.put(3, getString(R.string.three_points));
        map.put(4, getString(R.string.four_points));
        map.put(5, getString(R.string.five_points));
        map.put(6, getString(R.string.six_points));

        return map;
    }

    private Map<String, String> formulaNames = null;
    private Map<String, String> createFormulaNamesMap() {
        Map<String, String> map = new HashMap<>();
        map.put(BAZETT, getString(R.string.bazett_formula));
        map.put(FRAMINGHAM, getString(R.string.framingham_formula));
        map.put(HODGES, getString(R.string.hodges_formula));
        map.put(FRIDERICIA, getString(R.string.fridericia_formula));
        map.put(ALL, getString(R.string.all_formulas));
        return map;
    }

    private Map<String, String> textPositionNames = null;
    private Map<String, String> createTextPositionNamesMap() {
        Map<String, String> map = new HashMap<>();
        map.put("centerAbove", getString(R.string.center_above));
        map.put("centerBelow", getString(R.string.center_below));
        map.put("left", getString(R.string.left));
        map.put("right", getString(R.string.right));
        map.put("top", getString(R.string.top));
        map.put("bottom", getString(R.string.bottom));
        return map;
    }

    //keys
    private String defaultTimeCalibrationKey;
    private String defaultAmplitudeCalibrationKey;
    private String defaultLineWidthKey;
    private String defaultQtcFormulaKey;
    private String defaultTimeCaliperTextPositionKey;
    private String defaultAmplitudeCaliperTextPositionKey;

    private String defaultLineWidthName;
    private String defaultTimeCalibrationName;
    private String defaultAmplitudeCalibrationName;
    private String defaultQtcFormula;
    private String defaultTimeCaliperTextPosition;
    private String defaultAmplitudeCaliperTextPosition;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        lineWidthNames = createLineWidthMap();
        formulaNames = createFormulaNamesMap();
        textPositionNames = createTextPositionNamesMap();
        defaultTimeCalibrationKey = activity.getString(R.string.time_calibration_key);
        defaultAmplitudeCalibrationKey = activity.getString(R.string.amplitude_calibration_key);
        defaultLineWidthKey = activity.getString(R.string.line_width_key);
        defaultQtcFormulaKey = activity.getString(R.string.qtc_formula_key);
        defaultTimeCaliperTextPositionKey = activity.getString(R.string.time_caliper_text_position_key);
        defaultAmplitudeCaliperTextPositionKey = activity.getString(R.string.amplitude_caliper_text_position_key);
        defaultLineWidthName = activity.getString(R.string.default_line_width);
        defaultTimeCalibrationName = activity.getString(R.string.default_time_calibration_value);
        defaultAmplitudeCalibrationName = activity.getString(R.string.default_amplitude_calibration_value);
        defaultQtcFormula = activity.getString(R.string.qtc_formula_value);
        defaultTimeCaliperTextPosition = activity.getString(R.string.time_caliper_text_position_value);
        defaultAmplitudeCaliperTextPosition = activity.getString(R.string.amplitude_caliper_text_position_value);

        addPreferencesFromResource(R.xml.settings);

        Preference defaultTimeCalibrationPreference = findPreference(defaultTimeCalibrationKey);
        defaultTimeCalibrationPreference.setSummary(getPreferenceScreen()
                .getSharedPreferences()
                .getString(defaultTimeCalibrationKey, defaultTimeCalibrationName));

        Preference defaultAmplitudeCalibrationPreference = findPreference(defaultAmplitudeCalibrationKey);
        defaultAmplitudeCalibrationPreference.setSummary(getPreferenceScreen()
                .getSharedPreferences()
                .getString(defaultAmplitudeCalibrationKey, defaultAmplitudeCalibrationName));

        Preference defaultLineWidthPreference = findPreference(defaultLineWidthKey);
        String defaultLineWidthValue = getPreferenceScreen()
                .getSharedPreferences()
                .getString(defaultLineWidthKey, defaultLineWidthName);
        int lineWidth = DEFAULT_LINE_WIDTH;
        try {
            if (defaultLineWidthValue != null) {
                lineWidth = Integer.parseInt(defaultLineWidthValue);
            }
        }
        catch (Exception ex) {
            // just leave lineWidth as DEFAULT_LINE_WIDTH
        }
        String defaultLineWidthName = lineWidthNames.get(lineWidth);
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
            pref.setSummary(sharedPreferences.getString(key, defaultTimeCalibrationName));
        }
        else if (key.equals(defaultAmplitudeCalibrationKey)) {
            pref.setSummary(sharedPreferences.getString(key, defaultAmplitudeCalibrationName));
        }
        else if (key.equals(defaultLineWidthKey)) {
            pref.setSummary(getNameFromKey(sharedPreferences, key, defaultLineWidthName));
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
            String lineWidthString = sharedPreferences.getString(key, defaultName);
            int value = DEFAULT_LINE_WIDTH;
            if (lineWidthString != null) {
                value = Integer.parseInt(lineWidthString);
            }
            return lineWidthNames.get(value);
        } catch (NumberFormatException ex) {
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