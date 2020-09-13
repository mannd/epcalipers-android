package org.epstudios.epcalipers;

import android.graphics.PointF;
import android.graphics.Rect;

import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.*;

public class ApplicationTest {

    @org.junit.Test
    public void test() {
        final int expected = 1;
        final int reality = 1;
        assertEquals(expected, reality);
    }

    @org.junit.Test
    public void testCanDisplayRate() {
        Calibration cal = new Calibration(InstrumentationRegistry.getInstrumentation().getTargetContext());
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
        cal.setUnits("сек");
        assertTrue(cal.canDisplayRate());
        cal.setUnits("СЕк");
        assertTrue(cal.canDisplayRate());
        cal.setDirection(Caliper.Direction.VERTICAL);
        assertFalse(cal.canDisplayRate());

    }

    @org.junit.Test
    public void testCurrentHorizontalCalFactor() {
        Calibration cal = new Calibration(InstrumentationRegistry.getInstrumentation().getTargetContext());
        cal.setOriginalZoom(1.0f);
        cal.setOriginalCalFactor(0.5f);
        cal.setCurrentZoom(1.0f);
        assertEquals(cal.getCurrentCalFactor(), 0.5f, 0.001);
        cal.setCurrentZoom(2.0f);
        assertEquals(cal.getCurrentCalFactor(), 0.25f, 0.001);
    }

    @org.junit.Test
    public void testInitialCaliperPosition() {
        Caliper c = new Caliper();
        c.setInitialPosition(new Rect(0, 0, 600, 600));
        assertEquals(c.getBar1Position(), 200.0, 0.001);
        assertEquals(c.getBar2Position(), 400.0, 0.001);
        assertEquals(c.getCrossBarPosition(), 300.0, 0.001);
        c.setInitialPosition(new Rect(0, 0, 600, 600));
        c.setDirection(Caliper.Direction.VERTICAL);
        assertEquals(c.getBar1Position(), 215.0f, 0.001);
        assertEquals(c.getBar2Position(), 415.0f, 0.001);
        assertEquals(c.getCrossBarPosition(), 315.0f, 0.001);
    }

    @org.junit.Test
    public void testBarCoord() {
        Caliper c = new Caliper();
        assertEquals(c.getBar1Position(), 0, 0.001);
        assertEquals(c.getBar2Position(), 0, 0.001);
        assertEquals(c.getCrossBarPosition(), 100.0, 0.001);
        PointF p = new PointF(100, 50);
        assertEquals(c.barCoord(p), 100.0f, 0.001);
        c.setDirection(Caliper.Direction.VERTICAL);
        assertEquals(c.barCoord(p),50.0f, 0.001);
    }

    @org.junit.Test
    public void testUnitsAreMM() {
        Calibration cal = new Calibration(InstrumentationRegistry.getInstrumentation().getTargetContext());
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

    @org.junit.Test
    public void testUnitsAreSec() {
        Calibration cal = new Calibration(InstrumentationRegistry.getInstrumentation().getTargetContext());
        cal.setUnits("sec");
        assertTrue(cal.unitsAreSeconds());
        cal.setUnits("seconds");
        assertTrue(cal.unitsAreSeconds());
        cal.setUnits("SEC");
        assertTrue(cal.unitsAreSeconds());
        cal.setUnits("s");
        assertTrue(cal.unitsAreSeconds());
        cal.setUnits("msec");
        assertFalse(cal.unitsAreSeconds());
        cal.setUnits("секунд");
        assertTrue(cal.unitsAreSeconds());
        cal.setUnits("сек");
        assertTrue(cal.unitsAreSeconds());
        cal.setUnits("с");
        assertTrue(cal.unitsAreSeconds());
        cal.setUnits("");
        assertFalse(cal.unitsAreSeconds());
        cal.setUnits("blah");
        assertFalse(cal.unitsAreSeconds());
        cal.setUnits("sec");
        assertTrue(cal.unitsAreSeconds());
    }

    @org.junit.Test
    public void testUnitsAreMsec() {
        Calibration cal = new Calibration(InstrumentationRegistry.getInstrumentation().getTargetContext());
        cal.setUnits("msec");
        assertTrue(cal.unitsAreMsec());
        cal.setUnits("Msec");
        assertTrue(cal.unitsAreMsec());
        cal.setUnits("MSECs");
        assertTrue(cal.unitsAreMsec());
        cal.setUnits("ms");
        assertTrue(cal.unitsAreMsec());
        cal.setUnits("millisec");
        assertTrue(cal.unitsAreMsec());
        cal.setUnits("millimeters");
        assertFalse(cal.unitsAreMsec());
        cal.setUnits("мсек");
        assertTrue(cal.unitsAreMsec());
        cal.setUnits("мс");
        assertTrue(cal.unitsAreMsec());
        cal.setUnits("миллисекунду");
        assertTrue(cal.unitsAreMsec());
        cal.setUnits("");
        assertFalse(cal.unitsAreMsec());
        cal.setUnits("с");
        assertFalse(cal.unitsAreMsec());
        cal.setUnits("");
        assertFalse(cal.unitsAreMsec());
        cal.setUnits("blah");
        assertFalse(cal.unitsAreMsec());
        cal.setUnits("msec");
        assertTrue(cal.unitsAreMsec());
    }

    @org.junit.Test
    public void testRadiansToDegrees() {
        double angle = 0;
        assert(AngleCaliper.radiansToDegrees(angle) == 0.0);
        angle = Math.PI / 2.0;
        assert(AngleCaliper.radiansToDegrees(angle) == 90.0);
        angle = Math.PI;
        assert(AngleCaliper.radiansToDegrees(angle) == 180.0);
    }

    @org.junit.Test
    public void testIsAngleCaliper() {
        Caliper caliper = new Caliper();
        assertTrue(caliper.requiresCalibration());
        assertFalse(caliper.isAngleCaliper());
        Caliper angleCaliper = new AngleCaliper();
        assertFalse(angleCaliper.requiresCalibration());
        assertTrue(angleCaliper.isAngleCaliper());
    }

    @org.junit.Test
    public void testQTc() {
        QtcCalculator calc = new QtcCalculator(QtcCalculator.QtcFormula.qtcBzt, InstrumentationRegistry.getInstrumentation().getTargetContext());
        String result = calc.calculate(0.278, 0.6818, false, "sec");
        assertEquals(result, "Mean RR = 0.6818 sec\nQT = 0.278 sec\nQTc = 0.33668 sec (Bazett formula)");
        QtcCalculator calc2 = new QtcCalculator(QtcCalculator.QtcFormula.qtcFrd, InstrumentationRegistry.getInstrumentation().getTargetContext());
        result = calc2.calculate(0.278, 0.6818, false, "sec");
        assertEquals(result, "Mean RR = 0.6818 sec\nQT = 0.278 sec\nQTc = 0.31586 sec (Fridericia formula)");
        calc2.setFormula(QtcCalculator.QtcFormula.qtcHdg);
        result = calc2.calculate(0.278, 0.6818, false, "sec");
        assertEquals(result, "Mean RR = 0.6818 sec\nQT = 0.278 sec\nQTc = 0.327 sec (Hodges formula)");
        calc2.setFormula(QtcCalculator.QtcFormula.qtcFrm);
        result = calc2.calculate(0.278, 0.6818, true, "msec");
        assertEquals(result, "Mean RR = 681.8 msec\nQT = 278 msec\nQTc = 327 msec (Framingham formula)");
    }

    @org.junit.Test
    public void testVersion() {
	String testVersionName = "2.1.3";
	int testVersionCode = 300;
	Version version = new Version(null, null, testVersionName, testVersionCode);
	assertEquals(version.getVersionName(), testVersionName);
	assertEquals(version.getVersionCode(), testVersionCode);
    }
	
	

    @org.junit.Test
    public void testMiscCaliperTests() {
        Caliper c = new Caliper();
        c.setBar1Position(100);
        c.setBar2Position(50);
        c.setCrossBarPosition(120);
        assertEquals(c.getValueInPoints(), -50, 0.001);
        assertTrue(c.pointNearBar1(new PointF(90, 130)));
        assertTrue(c.pointNearBar2(new PointF(45, 180)));
        assertTrue(c.pointNearCrossBar(new PointF(70, 130)));
        PointF closePoint = new PointF(110, 110);
        PointF farPoint = new PointF(130, 150);
        assertTrue(c.pointNearCaliper(closePoint));
        assertFalse(c.pointNearCaliper(farPoint));
        c.setDirection(Caliper.Direction.VERTICAL);
        assertTrue(c.pointNearCaliper(closePoint));
        assertFalse(c.pointNearCaliper(farPoint));
        PointF closePoint2 = new PointF(110, 75);
        assertTrue(c.pointNearCaliper(closePoint2));
        Calibration cal = new Calibration(InstrumentationRegistry.getInstrumentation().getTargetContext());
        c.setCalibration(cal);
        assertEquals(c.intervalInSecs(200), 0.2, 0.001);
        assertEquals(c.intervalInMsec(0.100), 100, 0.001);
        cal.setUnits("msec");
        assertEquals(c.intervalInMsec(100), 100, 0.001);
        cal.setUnits("SEC");
        assertEquals(c.intervalInSecs(0.314), 0.314, 0.001);
        assertFalse(c.isTimeCaliper());
        c.setDirection(Caliper.Direction.HORIZONTAL);
        assertTrue(c.isTimeCaliper());
    }
}
