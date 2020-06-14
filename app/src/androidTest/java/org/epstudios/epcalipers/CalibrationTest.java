package org.epstudios.epcalipers;


import org.junit.Test;

import java.util.List;

import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;

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
public class CalibrationTest {
    @Test
    public void testParsing() {
        List<String> chunks = CalibrationProcessor.parse("Hello World!");
        assertEquals("Hello", chunks.get(0));
        assertEquals("World", chunks.get(1));
        chunks = CalibrationProcessor.parse("1000 msec");
        assertEquals("1000", chunks.get(0));
        assertEquals("msec", chunks.get(1));
        chunks = CalibrationProcessor.parse("1.5 sec");
        assertEquals("1.5", chunks.get(0));
        assertEquals("sec", chunks.get(1));
        chunks = CalibrationProcessor.parse("400 миллисекунды");
        assertEquals("400", chunks.get(0));
        assertEquals("миллисекунды", chunks.get(1));
        chunks = CalibrationProcessor.parse("1.5 sec xxx");
        assertEquals("1.5", chunks.get(0));
        assertEquals("sec", chunks.get(1));
        assertEquals("xxx", chunks.get(2));
        chunks = CalibrationProcessor.parse("msec sec xxx");
        assertEquals("msec", chunks.get(0));
        assertEquals("sec", chunks.get(1));
        assertEquals("xxx", chunks.get(2));
        chunks = CalibrationProcessor.parse("");
        assertEquals(0, chunks.size());
    }

    @Test
    public void testProcessCalibrationString() {
        final double delta = 0.00001;
        CalibrationResult result = CalibrationProcessor.processCalibrationString("");
        assertEquals(result.success, false);
        result = CalibrationProcessor.processCalibrationString("   ");
        assertEquals(result.success, false);
        // illegal chars
        result = CalibrationProcessor.processCalibrationString("&& ! *   ");
        assertEquals(result.success, false);
        result = CalibrationProcessor.processCalibrationString("10.10.10");
        assertEquals(result.success, true);
        assertEquals(result.value, 10.1, delta);
        result = CalibrationProcessor.processCalibrationString("1000");
        assertEquals(result.success, true);
        assertEquals(result.value, 1000, delta);
        assertEquals(result.units, "");
        result = CalibrationProcessor.processCalibrationString("1000 msec");
        assertEquals(result.success, true);
        assertEquals(result.value, 1000, 0.0001);
        assertEquals(result.units, "msec");
        result = CalibrationProcessor.processCalibrationString("1000 1000");
        assertEquals(result.success, true);
        assertEquals(result.value, 1000, 0.0001);
        assertEquals(result.units, "1000");
        result = CalibrationProcessor.processCalibrationString("0 sec");
        assertEquals(result.success, false);
        assertEquals(result.value, 0, 0.0001);
        assertEquals(result.units, "sec");
        result = CalibrationProcessor.processCalibrationString("msec sec");
        assertEquals(result.success, false);
        assertEquals(result.value, 0, 0.0001);
        assertEquals(result.units, "");
    }

    @Test
    public void validateCalibrationTest() {
        CalibrationProcessor.Validation validation;
        validation = CalibrationProcessor.validate("");
        assertEquals(true, validation.noInput);
        assertEquals(false, validation.isValid());
        validation = CalibrationProcessor.validate("msec");
        assertEquals(true, validation.noNumber);
        assertEquals(true, validation.noUnits);
        assertEquals(false, validation.isValid());
        validation = CalibrationProcessor.validate("1000");
        assertEquals(true, validation.noUnits);
        assertEquals(false, validation.isValid());
        validation = CalibrationProcessor.validate("0");
        assertEquals(true, validation.invalidNumber);
        assertEquals(true, validation.noUnits);
        assertEquals(false, validation.isValid());
        validation = CalibrationProcessor.validate("1000 sec");
        assertEquals(true, validation.isValid());
        validation = CalibrationProcessor.validate("1000 msec");
        assertEquals(true, validation.isValid());
        validation = CalibrationProcessor.validate("1 mV");
        assertEquals(true, validation.isValid());
        validation = CalibrationProcessor.validate("10 mm");
        assertEquals(true, validation.isValid());
    }

    @Test
    public void matcherTest() {
        Calibration calibration = new Calibration(InstrumentationRegistry.getInstrumentation().getTargetContext());
        calibration.setUnits("");
        // Empty strings never match...
        assertEquals(false, CalibrationProcessor.matchesMM(""));
        assertEquals(false, CalibrationProcessor.matchesMV(""));
        assertEquals(false, CalibrationProcessor.matchesMsecs(""));
        assertEquals(false, CalibrationProcessor.matchesSeconds(""));

        // calibration unit checks check for empty string, probably unnecessarily.
        assertEquals(false, calibration.unitsAreMM());
        assertEquals(false, calibration.unitsAreSeconds());
        assertEquals(false, calibration.unitsAreMsec());

        calibration.setUnits("mm");
        assertEquals(false, calibration.unitsAreMM());
        // At present, unitsAreMM checks direction of caliper as well as units.
        calibration.setDirection(Caliper.Direction.VERTICAL);
        assertEquals(true, calibration.unitsAreMM());
        calibration.setUnits("millimeters");
        assertEquals(true, calibration.unitsAreMM());
        // But msec and seconds don't check caliper direction...??
        calibration.setUnits("msec");
        assertEquals(true, calibration.unitsAreMsec());
        calibration.setUnits("millisecs");
        assertEquals(true, calibration.unitsAreMsec());
        calibration.setUnits("sec");
        assertEquals(true, calibration.unitsAreSeconds());
        calibration.setUnits("Seconds");
        assertEquals(true, calibration.unitsAreSeconds());
        calibration.setDirection(Caliper.Direction.HORIZONTAL);
        calibration.setUnits("msec");
        assertEquals(true, calibration.unitsAreMsec());
        calibration.setUnits("sec");
        assertEquals(true, calibration.unitsAreSeconds());
    }
}
