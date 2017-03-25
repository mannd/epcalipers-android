package org.epstudios.epcalipers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;

/**
 * Copyright (C) 2015 EP Studios, Inc.
 * www.epstudiossoftware.com
 * <p/>
 * Created by mannd on 4/16/15.
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
public class Caliper {

    public enum Component {
        Bar1,
        Bar2,
        Crossbar,
        None
    }

    public enum MovementDirection {
        Up,
        Down,
        Left,
        Right,
        None
    }


    private static int differential = 0;
    private static final float DELTA = 30.0f;

    private final float SMALL_FONT = 32.0f;
    private final float LARGE_FONT = 48.0f;

    public enum TouchedBar {NONE, BAR1, BAR2, CROSSBAR}

    public TouchedBar getTouchedBar() {
        return touchedBar;
    }

    public void setTouchedBar(TouchedBar touchedBar) {
        this.touchedBar = touchedBar;
    }

    private TouchedBar touchedBar;

    public float getBar1Position() {
        return bar1Position;
    }

    public void setBar1Position(float bar1Position) {
        this.bar1Position = bar1Position;
    }

    public float getBar2Position() {
        return bar2Position;
    }

    public void setBar2Position(float bar2Position) {
        this.bar2Position = bar2Position;
    }

    public float getCrossbarPosition() {
        return crossBarPosition;
    }

    public void setCrossbarPosition(float crossBarPosition) {
        this.crossBarPosition = crossBarPosition;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public enum Direction { HORIZONTAL, VERTICAL }

    private float bar1Position;
    private float bar2Position;
    private float crossBarPosition;
    private Direction direction;

    public int getUnselectedColor() {
        return unselectedColor;
    }

    public void setUnselectedColor(int unselectedColor) {
        this.unselectedColor = unselectedColor;
    }

    private int unselectedColor;

    public int getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
    }

    private int selectedColor;
    private boolean selected;

    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

    private final DecimalFormat decimalFormat;

    public Paint getPaint() {
        return paint;
    }

    private final Paint paint;

    public void setRoundMsecRate(boolean roundMsecRate) {
        this.roundMsecRate = roundMsecRate;
    }

    public boolean isRoundMsecRate() {
        return roundMsecRate;
    }

    private boolean roundMsecRate;

    public Calibration getCalibration() {
        return calibration;
    }

    public void setCalibration(Calibration calibration) {
        this.calibration = calibration;
    }

    private Calibration calibration;


    public Caliper(Direction direction, float bar1Position, float bar2Position,
                   float crossBarPosition) {
        this.direction = direction;
        this.bar1Position = bar1Position;
        this.bar2Position = bar2Position;
        this.crossBarPosition = crossBarPosition;
        this.unselectedColor = Color.BLUE;
        this.selectedColor = Color.RED;
        this.selected = false;
        this.touchedBar = TouchedBar.NONE;
        // below uses default local decimal separator
        decimalFormat = new DecimalFormat("@@@##");
        paint = new Paint();
        paint.setColor(this.unselectedColor);
        paint.setStrokeWidth(3.0f);
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(direction == Direction.HORIZONTAL ? Paint.Align.CENTER :
                Paint.Align.LEFT);

        paint.setTextSize(SMALL_FONT);
    }

    public Caliper() {
        this(Direction.HORIZONTAL, 0, 0, 100);
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public int getColor() {
        return paint.getColor();
    }

    public void setLineWidth(float lineWidth) {
        paint.setStrokeWidth(lineWidth);
    }

    public float getLineWidth() {
        return paint.getStrokeWidth();
    }

    public void setUseLargeFont(boolean useLargeFont) {
        if (useLargeFont) {
            paint.setTextSize(LARGE_FONT);
        }
        else {
            paint.setTextSize(SMALL_FONT);
        }
    }

    public void setInitialPosition(Rect rect) {
        if (direction == Direction.HORIZONTAL) {
            bar1Position = (rect.width()/3) + differential;
            bar2Position = (2 * rect.width()/3) + differential;
            crossBarPosition = (rect.height()/2) + differential;
        }
        else {
            bar1Position = (rect.height()/3) + differential;
            bar2Position = ((2 * rect.height())/3) + differential;
            crossBarPosition = (rect.width()/2) + differential;
        }
        differential += 15;
        if (differential > 80) {
            differential = 0;
        }
    }

    public void draw(Canvas canvas) {
        if (direction == Direction.HORIZONTAL) {
            crossBarPosition = Math.min(crossBarPosition, canvas.getHeight() - DELTA);
            crossBarPosition = Math.max(crossBarPosition, DELTA);
            bar1Position = Math.min(bar1Position, canvas.getWidth() - DELTA);
            bar2Position = Math.max(bar2Position, DELTA);
            canvas.drawLine(bar1Position, 0, bar1Position, canvas.getHeight(), paint);
            canvas.drawLine(bar2Position, 0, bar2Position, canvas.getHeight(), paint);
            canvas.drawLine(bar2Position, crossBarPosition, bar1Position, crossBarPosition, paint);
        }
        else {  // draw vertical caliper
            crossBarPosition = Math.min(crossBarPosition, canvas.getWidth() - DELTA);
            crossBarPosition = Math.max(crossBarPosition, DELTA);
            bar1Position = Math.min(bar1Position, canvas.getHeight() - DELTA);
            bar2Position = Math.max(bar2Position, DELTA);
            canvas.drawLine(0, bar1Position, canvas.getWidth(), bar1Position, paint);
            canvas.drawLine(0, bar2Position, canvas.getWidth(), bar2Position, paint);
            canvas.drawLine(crossBarPosition, bar2Position, crossBarPosition, bar1Position, paint);
        }
        caliperText(canvas);
    }

    public void caliperText(Canvas canvas) {
        String text = measurement();
        if (direction == Direction.HORIZONTAL) {
            canvas.drawText(text, (bar1Position + (bar2Position - bar1Position)/ 2),
                    crossBarPosition - 12, paint);
        }
        else {
            canvas.drawText(text, crossBarPosition + 5,
                    bar1Position + ((bar2Position - bar1Position) / 2), paint);
        }
    }

    public float barCoord(PointF p) {
        return (direction == Direction.HORIZONTAL ? p.x : p.y);
    }

    protected String measurement() {
        String result;
        if (roundMsecRate && (calibration.getDisplayRate() || calibration.unitsAreMsec())) {
            result = String.valueOf((int) Math.round(calibratedResult()));
        }
        else {
            result = decimalFormat.format(calibratedResult());
        }
        result += " " + calibration.getUnits();
        return result;
    }

    private double calibratedResult() {
        double result = intervalResult();
        if (result != 0 && calibration.canDisplayRate() && calibration.getDisplayRate()) {
            result = rateResult(result);
        }
        return result;
    }

    public double intervalResult() {
        return points() * calibration.multiplier();
    }

    private float points() {
        return bar2Position - bar1Position;
    }

    public float getValueInPoints() {
        return points();
    }

    public double rateResult(double interval) {
        if (interval != 0) {
            if (calibration.unitsAreMsec()) {
                interval = 60000.0 / interval;
            }
            if (calibration.unitsAreSeconds()) {
                interval = 60.0 / interval;
            }
        }
        return interval;
    }

    public double intervalInSecs(double interval) {
        if (calibration.unitsAreSeconds()) {
            return interval;
        }
        else {
            return interval / 1000;
        }
    }

    private double intervalInMsec(double interval) {
        if (calibration.unitsAreMsec()) {
            return interval;
        }
        else {
            return 1000 * interval;
        }
    }

    private boolean pointNearBar(PointF p, float barPosition) {
        return barCoord(p) > barPosition - DELTA && barCoord(p) < barPosition + DELTA;
    }


//    +    // avoid overlapping deltas inside calipers that prevent crossbar touch when short interval
//            if (self.direction == Horizontal) {
//        -        nearBar = (p.x > fminf(self.bar1Position, self.bar2Position) + delta && p.x < fmaxf(self.bar2Position, self.bar1Position) - delta && p.y > self.crossBarPosition - delta && p.y < self.crossBarPosition + delta);
//        +        nearBar = (p.x > fminf(self.bar1Position, self.bar2Position) && p.x < fmaxf(self.bar2Position, self.bar1Position) && p.y > self.crossBarPosition - delta && p.y < self.crossBarPosition + delta);
//    } else {
//        -        nearBar = (p.y > fminf(self.bar1Position, self.bar2Position) + delta && p.y < fmaxf(self.bar2Position, self.bar1Position) - delta && p.x > self.crossBarPosition - delta && p.x < self.crossBarPosition + delta);
//        +        nearBar = (p.y > fminf(self.bar1Position, self.bar2Position) && p.y < fmaxf(self.bar2Position, self.bar1Position) && p.x > self.crossBarPosition - delta && p.x < self.crossBarPosition + delta);

    public boolean pointNearBar1(PointF p) {
        return pointNearBar(p, bar1Position);
    }

    public boolean pointNearBar2(PointF p) {
        return pointNearBar(p, bar2Position);
    }


    public boolean pointNearCrossBar(PointF p) {
        boolean nearBar;
        float delta = DELTA + 5.0f;  // cross bar delta a little bigger
        if (direction == Direction.HORIZONTAL) {
            nearBar = (p.x > Math.min(bar1Position, bar2Position)
                    && p.x < Math.max(bar2Position, bar1Position)
                    && p.y > crossBarPosition - delta
                    && p.y < crossBarPosition + delta);
        } else {
            nearBar = (p.y > Math.min(bar1Position, bar2Position)
                    && p.y < Math.max(bar2Position, bar1Position)
                    && p.x > crossBarPosition - delta
                    && p.x < crossBarPosition + delta);
        }
        return nearBar;
    }

    public boolean pointNearCaliper(PointF p) {
        return pointNearCrossBar(p)
                || pointNearBar(p, bar1Position)
                || pointNearBar(p, bar2Position);
    }

    public boolean requiresCalibration() {
        return true;
    }

    public boolean isAngleCaliper() {
        return false;
    }

    public void moveCrossBar(float deltaX, float deltaY, MotionEvent event) {
        bar1Position += deltaX;
        bar2Position += deltaX;
        crossBarPosition += deltaY;
    }

    public void moveBar1(float delta, float deltaY, MotionEvent event) {
        bar1Position += delta;
    }

    public void moveBar2(float delta, float deltaY, MotionEvent event) {
        bar2Position += delta;
    }

    public void moveBar(float delta, Component component, MovementDirection direction) {
        switch (component) {
            case Bar1:
                bar1Position += delta;
                break;
            case Bar2:
                bar2Position += delta;
                break;
            case Crossbar:
                moveCrossbarDirectionally(delta, direction);
                break;
            default:
                break;
        }
    }

    protected void moveCrossbarDirectionally(float delta, MovementDirection direction) {
        if (direction == MovementDirection.Up || direction == MovementDirection.Down) {
            crossBarPosition += delta;
        }
        else {
            bar1Position += delta;
            bar2Position += delta;
        }
    }


}
