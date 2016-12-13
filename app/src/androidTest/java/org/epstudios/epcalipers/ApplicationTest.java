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
        assertEquals(c.getBar1Position(), 200.0, 0.001);
        assertEquals(c.getBar2Position(), 400.0, 0.001);
        assertEquals(c.getCrossbarPosition(), 300.0, 0.001);
        c.setInitialPosition(new Rect(0, 0, 600, 600));
        c.setDirection(Caliper.Direction.VERTICAL);
        assertEquals(c.getBar1Position(), 215.0f, 0.001);
        assertEquals(c.getBar2Position(), 415.0f, 0.001);
        assertEquals(c.getCrossbarPosition(), 315.0f, 0.001);
    }

    public void testBarCoord() {
        Caliper c = new Caliper();
        assertEquals(c.getBar1Position(), 0, 0.001);
        assertEquals(c.getBar2Position(), 0, 0.001);
        assertEquals(c.getCrossbarPosition(), 100.0, 0.001);
        PointF p = new PointF(100, 50);
        assertEquals(c.barCoord(p), 100.0f, 0.001);
        c.setDirection(Caliper.Direction.VERTICAL);
        assertEquals(c.barCoord(p),50.0f, 0.001);
    }

    public void testUnitsAreMM() {
        Calibration cal = new Calibration();
        cal.setCalibrated(true);
        cal.setDirection(Caliper.Direction.VERTICAL);
        cal.setUnits("mm");
        assertTrue(cal.unitsAreMM());
        cal.setUnits("millimeters");
        assertTrue(cal.unitsAreMM());
        cal.setUnits("Millimeter");
        assertTrue(cal.unitsAreMM());
        cal.setUnits("MM");
        assertTrue(cal.unitsAreMM());
        cal.setUnits("milliM");
        assertTrue(cal.unitsAreMM());
        cal.setUnits("milliVolts");
        assertFalse(cal.unitsAreMM());
        cal.setUnits("mV");
        assertFalse(cal.unitsAreMM());
        cal.setUnits("msec");
        assertFalse(cal.unitsAreMM());
    }

    public void testRadiansToDegrees() {
        double angle = 0;
        assert(AngleCaliper.radiansToDegrees(angle) == 0.0);
        angle = Math.PI / 2.0;
        assert(AngleCaliper.radiansToDegrees(angle) == 90.0);
        angle = Math.PI;
        assert(AngleCaliper.radiansToDegrees(angle) == 180.0);
    }

    public void testIsAngleCaliper() {
        Caliper caliper = new Caliper();
        assertTrue(caliper.requiresCalibration());
        assertFalse(caliper.isAngleCaliper());
        Caliper angleCaliper = new AngleCaliper();
        assertFalse(angleCaliper.requiresCalibration());
        assertTrue(angleCaliper.isAngleCaliper());
    }

}