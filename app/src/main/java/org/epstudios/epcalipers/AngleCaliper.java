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
        setBar1Position(Math.min(getBar1Position(), canvas.getHeight() - delta));
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


//    public void caliperText(Canvas canvas) {
//        String text = measurement();
//        if (direction == Direction.HORIZONTAL) {
//            canvas.drawText(text, (bar1Position + (bar2Position - bar1Position)/ 2),
//                    crossBarPosition - 10, paint);
//        }
//        else {
//            canvas.drawText(text, crossBarPosition + 5,
//                    bar1Position + ((bar2Position - bar1Position) / 2), paint);
//        }
//    }

    //- (void)drawWithContext:(CGContextRef)context inRect:(CGRect)rect {
//
//        CGContextSetStrokeColorWithColor(context, [self.color CGColor]);
//        CGContextSetLineWidth(context, self.lineWidth);
//
//        // This ensures caliper always extends past the screen edges
//        CGFloat length = MAX(rect.size.height, rect.size.height) * 2;
//
//        // Make sure focal point never too close to screen edges
//        self.crossBarPosition = fminf(self.crossBarPosition, rect.size.height - DELTA);
//        self.crossBarPosition = fmaxf(self.crossBarPosition, DELTA);
//        self.bar1Position = fminf(self.bar1Position, rect.size.width - DELTA);
//        self.bar1Position = fmaxf (self.bar1Position, DELTA);
//        self.bar2Position = self.bar1Position;
//
//        CGPoint endPointBar1 = [self endPointForPosition:CGPointMake(self.bar1Position, self.crossBarPosition) forAngle:self.angleBar1 andLength:length];
//        CGContextMoveToPoint(context, self.bar1Position, self.crossBarPosition);
//        CGContextAddLineToPoint(context, endPointBar1.x, endPointBar1.y);
//
//        CGPoint endPointBar2 = [self endPointForPosition:CGPointMake(self.bar2Position, self.crossBarPosition) forAngle:self.angleBar2 andLength:length];
//        CGContextMoveToPoint(context, self.bar2Position, self.crossBarPosition);
//        CGContextAddLineToPoint(context, endPointBar2.x, endPointBar2.y);
//        CGContextStrokePath(context);
//        [self caliperText];
//
//        if (self.verticalCalibration.calibrated && self.verticalCalibration.unitsAreMM) {
//        if ([self angleInSouthernHemisphere:self.angleBar1] && [self angleInSouthernHemisphere:self.angleBar2]) {
//        double pointsPerMM = 1.0 / self.verticalCalibration.multiplier;
//        [self drawTriangleBase:context forHeight:5 * pointsPerMM];
//        // draw label
//        }
//        }
//        }
//
//        - (void)drawTriangleBase:(CGContextRef)context forHeight:(double)height {
//        CGPoint point1 = [self getBasePoint1ForHeight:height];
//        CGPoint point2 = [self getBasePoint2ForHeight:height];
//        double lengthInPoints = point2.x - point1.x;
//        CGContextMoveToPoint(context, point1.x, point1.y);
//        CGContextAddLineToPoint(context, point2.x, point2.y);
//        CGContextStrokePath(context);
//
//        NSString *text = [self baseMeasurement:lengthInPoints];
//        self.paragraphStyle.lineBreakMode = NSLineBreakByTruncatingTail;
//        self.paragraphStyle.alignment = NSTextAlignmentCenter;
//
//        [self.attributes setObject:self.textFont forKey:NSFontAttributeName];
//        [self.attributes setObject:self.paragraphStyle forKey:NSParagraphStyleAttributeName];
//        [self.attributes setObject:self.color forKey:NSForegroundColorAttributeName];
//
//        // same positioning as
//        [text drawInRect:CGRectMake((point2.x > point1.x ? point1.x - 25: point2.x - 25), point1.y - 20,  fmax(100.0, fabs(point2.x - point1.x) + 50), 20)  withAttributes:self.attributes];
//
//        }
//

    // test if angle is in inferior half of unit circle
    // these are the only angles relevant for Brugada triangle base measurement
    private boolean angleInSouthernHemisphere(double angle) {
        return 0.0 <= angle && angle <= Math.PI;
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

//        - (NSString *)baseMeasurement:(double)lengthInPoints {
//        NSString *s = [NSString stringWithFormat:@"%.4g %@", [self calibratedBaseResult:lengthInPoints], self.calibration.units];
//        return s;
//
//        }
//
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
//

//        - (NSString *)alphaAngle {
//        // the angle between bar2 and a vertical
//        double angle = 0.5 * M_PI - self.angleBar2;
//        double degrees = [AngleCaliper radiansToDegrees:angle];
//        NSString *text = [NSString stringWithFormat:@"%.1f°", degrees];
//        return text;
//        }
//
    static public double radiansToDegrees(double radians) {
        return radians * 180.0 / Math.PI;
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

//
//// height of triangle in points, angle1 is angle of bar1, angle2 of bar2, in radians
//// returns length of base of triangle in points
//        + (double)calculateBaseFromHeight:(double)height andAngle1:(double)angle1 andAngle2:(double)angle2 {
//        // alpha, beta, gamma are 3 angles of the triangle, starting at apex, going clockwise
//        // a, b, c are vertices of triangle
//        // m is intersection of height segment with base
//        // alpha = angle1 - angle2;
//        // alpha1 is angle between bar1 and height, alpha2 between height and bar2
//        double alpha1 = angle1 - M_PI_2;
//        double alpha2 = M_PI_2 - angle2;
//        double beta = M_PI_2 - alpha2;
//        double gamma = M_PI_2 - alpha1;
//        double mb = height * sin(alpha2) / sin(beta);
//        double cm = height * sin(alpha1) / sin(gamma);
//        double base = cm + mb;
//        return base;
//        }
//
//// Note all angles in radians
//        + (double)brugadaRiskV1ForBetaAngle:(double)betaAngle andBase:(double)base {
//        betaAngle = [AngleCaliper radiansToDegrees:betaAngle];
//        double numerator = pow(M_E, 6.297 + (-0.1714 * betaAngle) + (-0.0399 * base));
//        double denominator = 1 + numerator;
//        return numerator / denominator;
//        }
//
//        + (double)brugadaRiskV2ForBetaAngle:(double)betaAngle andBase:(double)base {
//        betaAngle = [AngleCaliper radiansToDegrees:betaAngle];
//        double numerator = pow(M_E, 5.9756 + (-0.3568 * betaAngle) + (-0.9332 * base));
//        double denominator = 1 + numerator;
//        return numerator / denominator;
//        }
//
//// figure out base coordinates
//        - (CGPoint)getBasePoint2ForHeight:(double)height {
//        double pointY = self.crossBarPosition + height;
//        double pointX = 0.0;
//        pointX = height * (sin(M_PI_2 - self.angleBar2) / sin(self.angleBar2));
//        pointX += self.bar1Position;
//        CGPoint point = CGPointMake(pointX, pointY);
//        return point;
//        }
//
//        - (CGPoint)getBasePoint1ForHeight:(double)height {
//        double pointY = self.crossBarPosition + height;
//        double pointX = 0.0;
//        pointX = height * (sin(self.angleBar1 - M_PI_2) / sin(M_PI - self.angleBar1));
//        pointX = self.bar1Position - pointX;
//        CGPoint point = CGPointMake(pointX, pointY);
//        return point;
//        }
//
//@end

}