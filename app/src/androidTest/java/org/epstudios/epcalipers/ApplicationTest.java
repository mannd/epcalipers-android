package org.epstudios.epcalipers;

import android.app.Application;
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
        Calibration cal = new Calibration(getContext());
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

    public void testCurrentHorizontalCalFactor() {
        Calibration cal = new Calibration(getContext());
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
        Calibration cal = new Calibration(getContext());
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
//    func testQTc() {
//        let qtcResult = QTcResult()
//        var result = qtcResult.calculateFromQt(inSec: 0.4, rrInSec: 1.0, formula: .Bazett, convertToMsec: false, units: "sec")
//        XCTAssertEqual(result, "Mean RR = 1 sec\nQT = 0.4 sec\nQTc = 0.4 sec (Bazett formula)")
//        result = qtcResult.calculateFromQt(inSec: 0.4, rrInSec: 1.0, formula: .Hodges, convertToMsec: false, units: "sec")
//        XCTAssertEqual(result, "Mean RR = 1 sec\nQT = 0.4 sec\nQTc = 0.4 sec (Hodges formula)")
//        // test a real calculation using these formulas
//        let qtcTable: [(formula: QTcFormulaPreference, value: Double, name: String)] = [(.Bazett, 0.3367, "Bazett"), (.Fridericia, 0.3159, "Fridericia"), (.Framingham, 0.327, "Framingham"), (.Hodges, 0.327, "Hodges")]
//        for (formula, value, name) in qtcTable {
//            result = qtcResult.calculateFromQt(inSec: 0.278, rrInSec: 0.6818, formula: formula, convertToMsec: false, units: "sec")
//            XCTAssertEqual(result, "Mean RR = 0.6818 sec\nQT = 0.278 sec\nQTc = \(value) sec (\(name) formula)")
//        }
//
//        let qtcTable2: [(formula: QTcFormulaPreference, value: Double, name: String)] = [(.Bazett, 456.3, "Bazett"), (.Fridericia, 411.2, "Fridericia"), (.Framingham, 405.5, "Framingham"), (.Hodges, 425, "Hodges")]
//        for (formula, value, name) in qtcTable2 {
//            result = qtcResult.calculateFromQt(inSec: 0.334, rrInSec: 0.5357, formula: formula, convertToMsec: true, units: "msec")
//            XCTAssertEqual(result, String.localizedStringWithFormat("Mean RR = 535.7 msec\nQT = 334 msec\nQTc = %.4g msec (%@ formula)", value, name))
//        }
//    }

    public void testQTc() {
        QtcCalculator calc = new QtcCalculator(QtcCalculator.QtcFormula.qtcBzt, getContext());
        String result = calc.calculate(0.278, 0.6818, false, "sec");
        assertEquals(result, "Mean RR = 0.6818 sec\nQT = 0.278 sec\nQTc = 0.33668 sec (Bazett formula)");
        QtcCalculator calc2 = new QtcCalculator(QtcCalculator.QtcFormula.qtcFrd, getContext());
        result = calc2.calculate(0.278, 0.6818, false, "sec");
        assertEquals(result, "Mean RR = 0.6818 sec\nQT = 0.278 sec\nQTc = 0.31586 sec (Fridericia formula)");
        // TODO: add the other formulas here
        calc2.setFormula(QtcCalculator.QtcFormula.qtcHdg);
        result = calc2.calculate(0.278, 0.6818, false, "sec");
        assertEquals(result, "Mean RR = 0.6818 sec\nQT = 0.278 sec\nQTc = 0.327 sec (Hodges formula)");
        calc2.setFormula(QtcCalculator.QtcFormula.qtcFrm);
        result = calc2.calculate(0.278, 0.6818, true, "msec");
        assertEquals(result, "Mean RR = 681.8 msec\nQT = 278 msec\nQTc = 327 msec (Framingham formula)");
    }

}
