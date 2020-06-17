package org.epstudios.epcalipers;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

/**
 * Copyright (C) 2020 EP Studios, Inc.
 * www.epstudiossoftware.com
 * <p>
 * Created by mannd on 6/11/20.
 * <p>
 * This file is part of epcalipers-android.
 * <p>
 * epcalipers-android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * epcalipers-android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with epcalipers-android.  If not, see <http://www.gnu.org/licenses/>.
 */
public class CalibrationProcessor {
    // TODO: regex below includes Cyrillic and must be updated with new alphabets
    @SuppressWarnings("HardCodedStringLiteral")
    private static final String calibrationRegex = "[.,0-9]+|[a-zA-ZА-яЁё]+";
    private static final Pattern VALID_PATTERN = Pattern.compile(calibrationRegex);

    // Valid regexes for units.
    @SuppressWarnings("HardCodedStringLiteral")
    private static final String secRegex = "(?:^sec|^сек|^s$|^с$)";
    @SuppressWarnings("HardCodedStringLiteral")
    private static final String msecRegex = "(?:^msec|^millis|^мсек|^миллис|^ms$|^мс$)";
    @SuppressWarnings("HardCodedStringLiteral")
    private static final String mmRegex = "(?:^millim|^миллим|^mm$|^мм$)";
    @SuppressWarnings("HardCodedStringLiteral")
    private static final String mvRegex = "(?:^milliv|^mv$|^миллив|^мв$)";

    final private static int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    private static final Pattern secPattern = Pattern.compile(secRegex, flags);
    private static final Pattern msecPattern = Pattern.compile(msecRegex, flags);
    private static final Pattern mmPattern = Pattern.compile(mmRegex, flags);
    private static final Pattern mvPattern = Pattern.compile(mvRegex, flags);

    static List<String> parse(String toParse) {
        List<String> chunks = new LinkedList<>();
        Matcher matcher = VALID_PATTERN.matcher(toParse);
        while (matcher.find()) {
            chunks.add( matcher.group() );
        }
        return chunks;
    }

    public static CalibrationResult processCalibrationString(String in) {
        CalibrationResult calibrationResult = new CalibrationResult();
        if (in.length() < 1) {
            return calibrationResult;
        }
        List<String> chunks = parse(in);
        if (chunks.size() < 1) {
            return calibrationResult;
        }
        NumberFormat format = NumberFormat.getInstance();
        try {
            Number number = format.parse(chunks.get(0));
            calibrationResult.value = Objects.requireNonNull(number).floatValue();
        } catch (NullPointerException | ParseException ex) {
            EPSLog.log("Exception = " + ex.toString());
            return calibrationResult;
        }
        if (chunks.size() > 1) {
            calibrationResult.units = chunks.get(1);
        }
        // all calibration values must be positive
        calibrationResult.value = Math.abs(calibrationResult.value);
        // check for other badness
        if (calibrationResult.value <= 0.0f) {
            return calibrationResult;
        }
        calibrationResult.success = true;
        return calibrationResult;
    }

    public static class Validation {
        public boolean noInput = false;
        public boolean noNumber = false;
        public boolean noUnits = false;
        public boolean invalidNumber = false;
        public boolean invalidUnits = false;

        public boolean isValid() {
            return !(noInput || noNumber || noUnits || invalidNumber || invalidUnits );
        }

        @NonNull
        @Override
        public String toString() {
            if (isValid()) {
                return "Valid input";
            }
            String s = "";
            s += noInput ? "noInput " : "";
            s += noNumber ? "noNumber " : "";
            s += noUnits ? "noUnits " : "";
            s += invalidNumber ? "invalidNumber " : "";
            s += invalidUnits ? "invalidUnits " : "";
            return s;
        }
    }

    @NonNull
    public static Validation validate(String s, Caliper.Direction direction) {
        Validation validation = new Validation();
        List<String> chunks = parse(s);
        if (chunks.size() < 1) {
             validation.noInput = true;
             return validation;
        }
        // is it a number?
        NumberFormat format = NumberFormat.getInstance();
        try {
            Number number = format.parse(chunks.get(0));
            if (Objects.requireNonNull(number).floatValue() <= 0.0) {
                validation.invalidNumber = true;
            }
        } catch (NullPointerException | ParseException ex) {
            validation.noNumber = true;
        }
        if (chunks.size() == 1) {
            validation.noUnits = true;
        }
        else { // chunk.siz() > 1 {
            validation.invalidUnits = !validateUnits(chunks.get(1), direction);
        }
        return validation;
    }

    private static boolean validateUnits(String s, Caliper.Direction direction) {
        if (direction == Caliper.Direction.HORIZONTAL) {
            return matchesMsecs(s) || matchesSeconds(s);
        }
        else { // vertical caliper
            return matchesMM(s) || matchesMV(s);
        }
    }

    public static boolean matchesSeconds(String s) {
        Matcher matcher = secPattern.matcher(s);
        return matcher.lookingAt();
    }

    public static boolean matchesMsecs(String s) {
        Matcher matcher = msecPattern.matcher(s);
        return matcher.lookingAt();
    }

    public static boolean matchesMM(String s) {
        Matcher matcher = mmPattern.matcher(s);
        return matcher.lookingAt();
    }

    public static boolean matchesMV(String s) {
        Matcher matcher = mvPattern.matcher(s);
        return matcher.lookingAt();
    }

    public static boolean unitsAreValidForCaliperDirection(String units, Caliper.Direction direction) {
        // Don't check a angle caliper with this method...
        if (direction == Caliper.Direction.HORIZONTAL) {
            return matchesSeconds(units) || matchesMsecs(units);
        }
        else {  // only other direction is VERTICAL
            return matchesMM(units) || matchesMV(units);
        }
    }
}
