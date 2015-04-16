package org.epstudios.epcalipers;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void test() throws Exception {
        final int expected = 1;
        final int reality = 1;
        assertEquals(expected, reality);
    }

    public void testCanDisplayRate() {
        Context context = new Activity();
        Calibration cal = new Calibration(context);
        cal.setCalibrated(true);
        cal.setUnits("msec");
        assertTrue(cal.canDisplayRate());
        cal.setUnits("milliseconds");
        assertTrue(cal.canDisplayRate());
        cal.setUnits("sec");
        assertTrue(cal.canDisplayRate());
        cal.setUnits("secs");
        assertTrue(cal.canDisplayRate());
        cal.setUnits("Msec");
        assertTrue(cal.canDisplayRate());
        cal.setUnits("ms");
        assertTrue(cal.canDisplayRate());
        cal.setUnits("mm");
        assertFalse(cal.canDisplayRate());
        cal.setUnits("mSecs");
        assertTrue(cal.canDisplayRate());
        cal.setDirection(Caliper.Direction.VERTICAL);
        assertFalse(cal.canDisplayRate());

    }

    public void testCurrentHorizontalCalFactor() {
        Context context = new Activity();
        Calibration cal = new Calibration(context);
        cal.setOriginalZoom(1.0f);
        cal.setOriginalCalFactor(0.5f);
        cal.setCurrentZoom(1.0f);
        assertEquals(cal.getCurrentCalFactor(), 0.5f);
        cal.setCurrentZoom(2.0f);
        assertEquals(cal.getCurrentCalFactor(), 0.25f);
    }


}