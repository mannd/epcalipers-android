/*
* Copyright (C) 2016 EP Studios, Inc.
* www.epstudiossoftware.com
* <p/>
* Created by mannd on 12/7/2016.
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

package org.epstudios.epcalipers;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

import java.text.DecimalFormat;

public class AngleCaliper extends Caliper {

    static float differential = 0.0f;
    private final double angle_delta = 0.15;
    private final float delta = 20.0f;
    // this constant is number of mm for height of Brugada triangle
    private final double brugada_triangle_height = 5.0;

    public double getBar1Angle() {
        return bar1Angle;
    }

    public void setBar1Angle(double bar1Angle) {
        this.bar1Angle = bar1Angle;
    }

    public double getBar2Angle() {
        return bar2Angle;
    }

    public void setBar2Angle(double bar2Angle) {
        this.bar2Angle = bar2Angle;
    }

    public Calibration getVerticalCalibration() {
        return verticalCalibration;
    }

    public void setVerticalCalibration(Calibration verticalCalibration) {
        this.verticalCalibration = verticalCalibration;
    }

    private double bar1Angle;
    private double bar2Angle;
    private Calibration verticalCalibration;
    private DecimalFormat degreeDecimalFormat;

    public AngleCaliper() {
        super();
        bar1Angle = Math.PI * 0.5;
        bar2Angle = Math.PI * 0.25;
        // bar1Position and bar2Position are equal and are the x coordinates of the vertex of the angle.
        // crossBarPosition is the y coordinate.
        setBar1Position(100.0f);
        setBar2Position(100.0f);
        setCrossbarPosition(100.0f);
        setVerticalCalibration(null);
        degreeDecimalFormat = new DecimalFormat("#.#");

    }

    @Override
    public void setInitialPosition(Rect rect) {
        setBar1Position(rect.width() / 3 + differential);
        setBar2Position(getBar1Position());
        setCrossbarPosition(rect.height() / 3 + differential * 1.5f);
        differential += 20.0f;
        if (differential > 100.0f) {
            differential = 0.0f;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        //        CGFloat length = MAX(rect.size.height, rect.size.height) * 2;
        float length = Math.max(canvas.getHeight(), canvas.getHeight() * 2);

        setCrossbarPosition(Math.min(getCrossbarPosition(), canvas.getHeight() - delta));
        setCrossbarPosition(Math.max(getCrossbarPosition(), delta));
        setBar1Position(Math.min(getBar1Position(), canvas.getWidth() - delta));
        setBar1Position(Math.max(getBar1Position(), delta));
        setBar2Position(getBar1Position());

        PointF endPointBar1 = endPointForPosition(new PointF(getBar1Position(), getCrossbarPosition()),
                bar1Angle, length);
        canvas.drawLine(getBar1Position(), getCrossbarPosition(), endPointBar1.x, endPointBar1.y,
                getPaint());

        PointF endPointBar2 = endPointForPosition(new PointF(getBar2Position(), getCrossbarPosition()),
                bar2Angle, length);
        canvas.drawLine(getBar2Position(), getCrossbarPosition(), endPointBar2.x, endPointBar2.y,
                getPaint());
        caliperText(canvas);

        // triangle base for Brugadometer
        if (getVerticalCalibration() != null && getVerticalCalibration().isCalibrated() && getVerticalCalibration().unitsAreMM()) {
            if (angleInSouthernHemisphere(bar1Angle) && angleInSouthernHemisphere(bar2Angle)) {
                double pointsPerMM = 1.0 / getVerticalCalibration().multiplier();
                drawTriangleBase(canvas, brugada_triangle_height * pointsPerMM);
            }
        }
    }

    // note: height is in points
    private void drawTriangleBase(Canvas canvas, double height) {
        PointF point1 = getBasePoint1ForHeight(height);
        PointF point2 = getBasePoint2ForHeight(height);
        double lengthInPoints = point2.x - point1.x;
        canvas.drawLine(point1.x, point1.y, point2.x, point2.y, getPaint());
        canvas.drawText(baseMeasurement(lengthInPoints), point1.x + (point2.x - point1.x) / 2,
                point1.y - 10, getPaint());

    }

    private PointF getBasePoint1ForHeight(double height) {
        double pointY = getCrossbarPosition() + height;
        double pointX = 0;
        pointX = height * (Math.sin(bar1Angle - Math.PI / 2) / Math.sin(Math.PI - bar1Angle));
        pointX = getBar1Position() - pointX;
        return new PointF((float)pointX, (float)pointY);
    }

    private PointF getBasePoint2ForHeight(double height) {
        double pointY = getCrossbarPosition() + height;
        double pointX = 0;
        pointX = height * (Math.sin(Math.PI / 2 - bar2Angle) / Math.sin(bar2Angle));
        pointX += getBar1Position();
        return new PointF((float)pointX, (float)pointY);
    }



    // test if angle is in inferior half of unit circle
    // these are the only angles relevant for Brugada triangle base measurement
    private boolean angleInSouthernHemisphere(double angle) {
        return 0.0 < angle && angle < Math.PI;
    }

    private String calibratedBaseResult(double lengthInPoints) {
        String result;
        lengthInPoints *= getCalibration().multiplier();
        if (isRoundMsecRate() && getCalibration().unitsAreMsec()) {
            result = String.valueOf((int)Math.round(lengthInPoints));
        }
        else {
            result = getDecimalFormat().format(lengthInPoints);
        }
        return result;
    }

    private String baseMeasurement(double lengthInPoints) {
        return calibratedBaseResult(lengthInPoints) + " " + getCalibration().getUnits();
    }

    private boolean pointNearBar(PointF p, double barAngle) {
        double theta = relativeTheta(p);
        return theta < barAngle + angle_delta && theta > barAngle - angle_delta;
    }

    private double relativeTheta(PointF p) {
        float x = p.x - getBar1Position();
        float y = p.y - getCrossbarPosition();
        return Math.atan2(y,x);
    }

    @Override
    public boolean pointNearBar1(PointF p) {
        return pointNearBar(p, bar1Angle);
    }

    @Override
    public boolean pointNearBar2(PointF p) {
        return pointNearBar(p, bar2Angle);
    }

    public boolean pointNearCrossBar(PointF p) {
        float delta = 40.0f;
        return (p.x > getBar1Position() - delta && p.x < getBar1Position() + delta &&
                p.y > getCrossbarPosition() - delta && p.y < getCrossbarPosition() + delta);
    }

    public boolean pointNearCaliper(PointF p) {
        return pointNearCrossBar(p) || pointNearBar1(p) || pointNearBar2(p);
    }

    public PointF endPointForPosition(PointF p, double angle, float length) {
        float endX = (float)Math.cos(angle) * length + p.x;
        float endY = (float)Math.sin(angle) * length + p.y;
        PointF endPoint = new PointF(endX, endY);
        return endPoint;
    }

    @Override
    protected String measurement() {
        double angle = bar1Angle - bar2Angle;
        double degrees = radiansToDegrees(angle);
        return degreeDecimalFormat.format(degrees) + "°";
    }

    static public double radiansToDegrees(double radians) {
        return radians * 180.0 / Math.PI;
    }

    static public double degreesToRadians(double degrees) {
        return (degrees * Math.PI) / 180.0;
    }

    @Override
    public void moveBar1(float deltaX, float deltaY, MotionEvent event) {
        bar1Angle = moveBarAngle(deltaX, deltaY, event);
    }

    @Override
    public void moveBar2(float deltaX, float deltaY, MotionEvent event) {
        bar2Angle = moveBarAngle(deltaX, deltaY, event);
    }

    private double moveBarAngle(float deltaX, float deltaY, MotionEvent event) {
        PointF newPosition = new PointF(event.getX() + deltaX, event.getY() + deltaY);
        return relativeTheta(newPosition);
    }

    @Override
    public boolean requiresCalibration() {
        return false;
    }

    @Override
    public boolean isAngleCaliper() {
        return true;
    }

    @Override
    public void moveBar(float delta, Component component, MovementDirection direction) {
        switch (component) {
            case Bar1:
                bar1Angle -= degreesToRadians(delta);
                break;
            case Bar2:
                bar2Angle -= degreesToRadians(delta);
                break;
            case Crossbar:
                super.moveCrossbarDirectionally(delta, direction);
            default:
                break;
        }
    }


}