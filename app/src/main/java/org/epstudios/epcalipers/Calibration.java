package org.epstudios.epcalipers;

import android.content.Context;

import java.util.regex.Pattern;

/**
 * Copyright (C) 2015 EP Studios, Inc.
 * www.epstudiossoftware.com
 * <p/>
 * Created by mannd on 4/16/15.
 * <p/>
 * This file is part of EP Calipers.
 * <p/>
 * EP Calipers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * EP Calipers is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with EP Calipers.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Calibration {
    private Caliper.Direction direction;
    private String units;
    private String calibrationString;
    private final Context context;

    public Caliper.Direction getDirection() {
        return direction;
    }

    public void setDirection(Caliper.Direction direction) {
        this.direction = direction;
    }

    public boolean getDisplayRate() {
        return displayRate;
    }

    private boolean displayRate;

    public float getOriginalZoom() {
        return originalZoom;
    }

    public void setOriginalZoom(float originalZoom) {
        this.originalZoom = originalZoom;
    }

    private float originalZoom;

    public float getCurrentZoom() {
        return currentZoom;
    }

    public void setCurrentZoom(float currentZoom) {
        this.currentZoom = currentZoom;
    }

    private float currentZoom;

    public float getOriginalCalFactor() {
        return originalCalFactor;
    }

    private float originalCalFactor;

    public boolean isCalibrated() {
        return calibrated;
    }

    public void setCalibrated(boolean calibrated) {
        this.calibrated = calibrated;
    }

    private boolean calibrated;

    public Calibration(Caliper.Direction direction, Context context) {
        this.direction = direction;
        this.context = context;
        final int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        reset();
    }

    public Calibration(Context context) {
        this(Caliper.Direction.HORIZONTAL, context);
    }

    public void reset() {
        units = context.getString(R.string.points);
        displayRate = false;
        originalZoom = 1.0f;
        currentZoom = 1.0f;
        calibrated = false;
        calibrationString = "";
    }

    public void setDisplayRate(boolean value) {
        displayRate = value;
    }

    public String getUnits() {
        if (calibrated) {
            if (displayRate) {
                return context.getString(R.string.bpm);
            } else {
                return units;
            }
        } else {
            return context.getString(R.string.points);
        }
    }

    public void setUnits(String value) {
        this.units = value;
    }

    public String getRawUnits() {
        return units;
    }

    public double multiplier() {
        if (calibrated) {
            return getCurrentCalFactor();
        } else {
            return 1.0;
        }
    }

    public boolean canDisplayRate() {
        if (direction == Caliper.Direction.VERTICAL) {
            return false;
        } else if (!calibrated) {
            return false;
        }
        return unitsAreMsec() || unitsAreSeconds();
    }

    public float getCurrentCalFactor() {
        return (originalZoom * originalCalFactor) / currentZoom;
    }

    public String getCalibrationString() {
        return calibrationString;
    }

    public void setCalibrationString(String calibrationString) {
        this.calibrationString = calibrationString;
    }

    public void setOriginalCalFactor(float originalCalFactor) {
        this.originalCalFactor = originalCalFactor;
    }

    public boolean unitsAreMM() {
        return (direction == Caliper.Direction.VERTICAL) && CalibrationProcessor.matchesMM(units);
    }

    public boolean unitsAreSeconds() {
        return CalibrationProcessor.matchesSeconds(units);
    }

    public boolean unitsAreMsec() {
        return CalibrationProcessor.matchesMsecs(units);
    }

}
