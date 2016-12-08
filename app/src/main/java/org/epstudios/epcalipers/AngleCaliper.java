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

import android.graphics.PointF;
import android.graphics.Rect;

public class AngleCaliper extends Caliper {

    static float differential = 0.0f;


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
    public boolean pointNearBar(PointF p, float barPosition) {
        return false;
    }

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

    private  double calibratedBaseResult(double lengthInPoints) {
        lengthInPoints *= getCalibration().multiplier();
        if (isRoundMsecRate() && getCalibration().unitsAreMsec()) {
            lengthInPoints = Math.round(lengthInPoints);
        }
        return lengthInPoints;
    }

//        - (NSString *)baseMeasurement:(double)lengthInPoints {
//        NSString *s = [NSString stringWithFormat:@"%.4g %@", [self calibratedBaseResult:lengthInPoints], self.calibration.units];
//        return s;
//
//        }
//
//        - (BOOL)pointNearBar:(CGPoint)p forBarAngle:(double)barAngle {
//        double theta = [self relativeTheta:p];
//        return theta < barAngle + ANGLE_DELTA && theta > barAngle - ANGLE_DELTA;
//        }
//
//        - (double)relativeTheta:(CGPoint)p {
//        float x = p.x - self.bar1Position;
//        float y = p.y - self.crossBarPosition;
//        return atan2(y, x);
//        }
//
//        - (BOOL)pointNearBar1:(CGPoint)p {
//        return [self pointNearBar:p forBarAngle:self.angleBar1];
//        }
//
//        - (BOOL)pointNearBar2:(CGPoint)p {
//        return [self pointNearBar:p forBarAngle:self.angleBar2];
//        }
//
//        - (BOOL)pointNearCrossBar:(CGPoint)p {
//        float delta = 40.0f;
//        return (p.x > self.bar1Position - delta && p.x < self.bar1Position + delta && p.y > self.crossBarPosition - delta && p.y < self.crossBarPosition + delta);
//        }
//
//        - (BOOL)pointNearCaliper:(CGPoint)p {
//        return [self pointNearCrossBar:p] || [self pointNearBar1:p]
//        || [self pointNearBar2:p];
//        }
//
//        - (CGPoint)endPointForPosition:(CGPoint)p forAngle:(double)angle andLength:(CGFloat)length {
//        double endX = cos(angle) * length + p.x;
//        double endY = sin(angle) * length + p.y;
//        CGPoint endPoint = CGPointMake(endX, endY);
//        return endPoint;
//        }
//
//        - (NSString *)measurement {
//        double angle = self.angleBar1 - self.angleBar2;
//        double degrees = [AngleCaliper radiansToDegrees:angle];
//        NSString *text = [NSString stringWithFormat:@"%.1f°", degrees];
//        return text;
//        }
//
//// override intervalResult to give angle in radians to calling functions
//        - (double)intervalResult {
//        return self.angleBar1 - self.angleBar2;
//        }
//
//        - (NSString *)alphaAngle {
//        // the angle between bar2 and a vertical
//        double angle = 0.5 * M_PI - self.angleBar2;
//        double degrees = [AngleCaliper radiansToDegrees:angle];
//        NSString *text = [NSString stringWithFormat:@"%.1f°", degrees];
//        return text;
//        }
//
//// provide this a utility to calling classes
//        + (double)radiansToDegrees:(double)radians {
//        return radians * 180.0 / M_PI;
//        }
//
//        - (void)moveBar1:(CGPoint)delta forLocation:(CGPoint)location {
//        self.angleBar1 = [self moveBarAngle:delta forLocation:location];
//        }
//
//        - (void)moveBar2:(CGPoint)delta forLocation:(CGPoint)location {
//        self.angleBar2 = [self moveBarAngle:delta forLocation:location];
//        }
//
//        - (double)moveBarAngle:(CGPoint)delta forLocation:(CGPoint)location {
//        CGPoint newPosition = CGPointMake(location.x + delta.x, location.y + delta.y);
//        return [self relativeTheta:newPosition];
//        }
//
//        - (BOOL)requiresCalibration {
//        return NO;
//        }
//
//        - (BOOL)isAngleCaliper {
//        return YES;
//        }
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