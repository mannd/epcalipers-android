package org.epstudios.epcalipers;

import android.graphics.PointF;

import org.junit.Test;

import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Copyright (C) 2019 EP Studios, Inc.
 * www.epstudiossoftware.com
 * <p>
 * Created by mannd on 2/19/19.
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
public class CaliperTest {

    @org.junit.Test
    public void getDirection() {
        Caliper c = new Caliper();
        assertTrue(c.getDirection() == Caliper.Direction.HORIZONTAL);
    }

    @Test
    public void isMarching() {
        Caliper c = new Caliper();
        assertFalse(c.isMarching());
    }

    @Test
    public void testCalperType() {
        Caliper c = new Caliper();
        assert(c.getCaliperType() == Caliper.CaliperType.Time);
        assertTrue(c.isTimeCaliper());
        c.setDirection(Caliper.Direction.VERTICAL);
        assert(c.getCaliperType() == Caliper.CaliperType.Amplitude);
        assertTrue(c.isAmplitudeCaliper());
        AngleCaliper ac = new AngleCaliper();
        assert(ac.getCaliperType() == Caliper.CaliperType.Angle);
        assertTrue(ac.isAngleCaliper());
    }

    @Test
    public void testNonNegativeBPM() {
        Caliper c = new Caliper();
        Calibration calibration = new Calibration(InstrumentationRegistry.getInstrumentation().getTargetContext());
        calibration.setOffset(new PointF(100.0f, 0));
        calibration.setOriginalCalFactor(1.f);
        calibration.setCalibrationString("1000 msec");
        calibration.setOriginalZoom(1.0f);
        calibration.setCurrentZoom(1.0f);
        calibration.setUnits("msec");
        calibration.setCalibrated(true);
        c.setCalibration(calibration);
        c.setRoundMsecRate(true);
        c.setBar1Position(1000.0f);
        c.setBar2Position(2000.0f);
        assertEquals("1000 msec", c.measurement());
        calibration.setDisplayRate(true);
        assertEquals("60 bpm", c.measurement());
        // make sure negative calipers don't show negative BPM
        c.setBar1Position(2000.0f);
        c.setBar2Position(1000.0f);
        calibration.setDisplayRate(false);
        assertEquals("-1000 msec", c.measurement());
        calibration.setDisplayRate(true);
        assertEquals("60 bpm", c.measurement());
    }
}