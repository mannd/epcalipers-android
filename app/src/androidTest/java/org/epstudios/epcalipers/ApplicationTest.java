package org.epstudios.epcalipers;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
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
        Calibration cal = new Calibration();
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
        Calibration cal = new Calibration();
        cal.setOriginalZoom(1.0f);
        cal.setOriginalCalFactor(0.5f);
        cal.setCurrentZoom(1.0f);
        assertEquals(cal.getCurrentCalFactor(), 0.5f);
        cal.setCurrentZoom(2.0f);
        assertEquals(cal.getCurrentCalFactor(), 0.25f);
    }

    public void testInitialCaliperPosition() {
        Caliper c = new Caliper();
        c.setInitialPosition(new Rect(0, 0, 600, 600));
        assertEquals(c.getBar1Position(), 200);
        assertEquals(c.getBar2Position(), 400);
        assertEquals(c.getCrossbarPosition(), 300);
        c.setInitialPosition(new Rect(0, 0, 600, 600));
        c.setDirection(Caliper.Direction.VERTICAL);
        assertEquals(c.getBar1Position(), 215.0f);
        assertEquals(c.getBar2Position(), 415.0f);
        assertEquals(c.getCrossbarPosition(), 315.0f);
    }

    public void testBarCoord() {
        Caliper c = new Caliper();
        assertEquals(c.getBar1Position(), 0);
        assertEquals(c.getBar2Position(), 0);
        assertEquals(c.getCrossbarPosition(), 100);
        PointF p = new PointF(100, 50);
        assertEquals(c.barCoord(p), 100.0f);
        c.setDirection(Caliper.Direction.VERTICAL);
        assertEquals(c.barCoord(p),50.0f);
    }
}