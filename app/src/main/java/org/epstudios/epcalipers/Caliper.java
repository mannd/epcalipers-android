package org.epstudios.epcalipers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

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
        Stationary
    }

    public enum Direction {
        HORIZONTAL,
        VERTICAL
    }

    public enum TextPosition {
        CenterAbove,
        CenterBelow,
        Left,
        Right,
        Top,
        Bottom
    }

    private static int differential = 0;
    private static final float DELTA = 30.0f;

    public void setFontSize(float fontSize) {
        paint.setTextSize(fontSize);
    }

    private final float MIN_DISTANCE_FOR_MARCH = 20.0f;
    private final int MAX_MARCHING_CALIPERS = 20;

    public Component getTouchedBar() {
        return touchedBar;
    }
    public void setTouchedBar(Component touchedBar) {
        this.touchedBar = touchedBar;
    }
    private Component touchedBar;

    // This is the component chosen during tweaking.  It is highlighted visually.
    public void setChosenComponent(Component chosenComponent) {
        this.chosenComponent = chosenComponent;
    }
    public Component getChosenComponent() {
        return chosenComponent;
    }
    private Component chosenComponent;

    public boolean isChosen() {
        return chosen;
    }
    public void setChosen(boolean chosen) {
        this.chosen = chosen;
    }
    private boolean chosen;  // Caliper is chosen for tweaking

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


    private float bar1Position;
    private float bar2Position;
    private float crossBarPosition;
    private Direction direction;

    public void setTextPosition(TextPosition textPosition) {
        this.textPosition = textPosition;
    }

    public TextPosition getTextPosition() {
        return textPosition;
    }

    private TextPosition textPosition;

    public Boolean getAutoPositionText() {
        return autoPositionText;
    }

    public void setAutoPositionText(Boolean autoPositionText) {
        this.autoPositionText = autoPositionText;
    }

    private Boolean autoPositionText;

    private Boolean useAltLabelPosition = true;

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
    private final Paint marchingPaint;

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

    public boolean isMarching() {
        return marching;
    }

    public void setMarching(boolean marching) {
        this.marching = marching;
    }

    private boolean marching;


    public Caliper(Direction direction, float bar1Position, float bar2Position,
                   float crossBarPosition, float fontSize) {
        this.direction = direction;
        this.bar1Position = bar1Position;
        this.bar2Position = bar2Position;
        this.crossBarPosition = crossBarPosition;
        this.unselectedColor = Color.BLUE;
        this.selectedColor = Color.RED;
        this.selected = false;
        this.touchedBar = Component.None;
        this.chosenComponent = Component.None;
        this.marching = false;
        this.textPosition = TextPosition.Right;
        this.autoPositionText = true;

        // below uses default local decimal separator
        decimalFormat = new DecimalFormat("@@@##");
        paint = new Paint();
        paint.setColor(this.unselectedColor);
        paint.setStrokeWidth(3.0f);
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.DEFAULT);
        // We will use CENTER alignment for all text position calculations.
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(fontSize);
        marchingPaint = new Paint(paint);
        setMarchingPaintStrokeWidth(paint);
    }

    public Caliper() {
        this(Direction.HORIZONTAL, 0, 0, 100, 16);
    }

    private void setMarchingPaintStrokeWidth(Paint paint) {
        // seems like difference of 2 in strokewidth works well for marching calipers
        marchingPaint.setStrokeWidth(Math.max(paint.getStrokeWidth() - 2, 1));
    }

    public void setColor(int color) {
        paint.setColor(color);
        marchingPaint.setColor(color);
    }

    public int getColor() {
        return paint.getColor();
    }

    public void setLineWidth(float lineWidth) {
        paint.setStrokeWidth(lineWidth);
        setMarchingPaintStrokeWidth(paint);
    }

    public float getLineWidth() {
        return paint.getStrokeWidth();
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
        if (marching && direction == Direction.HORIZONTAL) {
            drawMarchingCalipers(canvas);
        }
        caliperText(canvas, textPosition, true);

        drawChosenComponent(canvas);
    }

    private void drawMarchingCalipers(Canvas canvas) {
        float difference = Math.abs(bar1Position - bar2Position);
        if (difference < MIN_DISTANCE_FOR_MARCH) {
            return;
        }
        float greaterBar = Math.max(bar1Position, bar2Position);
        float lesserBar = Math.min(bar1Position, bar2Position);
        float[] biggerBars = new float[MAX_MARCHING_CALIPERS];
        float[] smallerBars = new float[MAX_MARCHING_CALIPERS];
        float point = greaterBar + difference;
        int index = 0;
        while (point < canvas.getWidth() && index < MAX_MARCHING_CALIPERS) {
            biggerBars[index] = point;
            point += difference;
            index++;
        }
        int maxBiggerBars = index;
        index = 0;
        point = lesserBar - difference;
        while (point > 0 && index < MAX_MARCHING_CALIPERS) {
            smallerBars[index] = point;
            point -= difference;
            index++;
        }
        int maxSmallerBars = index;
        // draw them, using marchingPaint, which is similar to Paint but with narrower lines
        for (int i = 0; i < maxBiggerBars; i++) {
            canvas.drawLine(biggerBars[i], 0, biggerBars[i], canvas.getHeight(), marchingPaint);
        }
        for (int i = 0; i < maxSmallerBars; i++) {
            canvas.drawLine(smallerBars[i], 0, smallerBars[i], canvas.getHeight(), marchingPaint);
        }
    }

    /**
     * Draw caliper text, using textPosition.  Auto positioning of text is handled both by
     * autoPositionText field and by passed parameter optimizeTextPosition.  The latter is needed
     * by angle calipers because the position of the main text of an angle caliper is always
     * centered above the caliper.  Angle calipers then use the textPosition parameter to
     * handle the text for the triangle base label.
     *
     * @param canvas the canvas drawn upon
     * @param textPosition a TextPosition used to position the label
     * @param optimizeTextPosition normally true, to allow optimization of the text position
     */
    public void caliperText(Canvas canvas, TextPosition textPosition, Boolean optimizeTextPosition) {
        String text = measurement();
        Rect bounds = getTextBounds(text);
        PointF textPositionPoint = caliperTextPosition(Math.min(bar1Position, bar2Position),
                Math.max(bar1Position, bar2Position), crossBarPosition, bounds,
                canvas, textPosition, optimizeTextPosition);
        // Note x and y for draw text depend of the alignment property of paint
        canvas.drawText(text, textPositionPoint.x, textPositionPoint.y, paint);
    }

    private void drawChosenComponent(Canvas canvas) {
        if (chosenComponent == Component.None) {
            return;
        }
        // chosenComponent has opposite color from rest of caliper.
        int chosenComponentColor = selected ? unselectedColor : selectedColor;
        paint.setColor(chosenComponentColor);
        switch (chosenComponent) {
            case Bar1:
                if (direction == Direction.HORIZONTAL) {
                    canvas.drawLine(bar1Position, 0, bar1Position, canvas.getHeight(), paint);
                }
                else {
                    canvas.drawLine(0, bar1Position, canvas.getWidth(), bar1Position, paint);
                }
                break;
            case Bar2:
                if (direction == Direction.HORIZONTAL) {
                    canvas.drawLine(bar2Position, 0, bar2Position, canvas.getHeight(), paint);
                }
                else {
                    canvas.drawLine(0, bar2Position, canvas.getWidth(), bar2Position, paint);
                }
                break;
            case Crossbar:
                if (direction == Direction.HORIZONTAL) {
                    canvas.drawLine(bar2Position, crossBarPosition, bar1Position, crossBarPosition, paint);
                }
                else {
                    canvas.drawLine(crossBarPosition, bar2Position, crossBarPosition, bar1Position, paint);
                }
                break;
            case None:
            default:
                break;
        }
        // reset paint color
        paint.setColor(selected ? selectedColor : unselectedColor);
    }

    /**
     * Calculates caliper text position and optimizes text position for horizontal type calipers as
     * needed to avoid caliper text going off screen.  A lot of parameters are passed to allow
     * the same method to be used for both time calipers and the triangle base of angle calipers.
     *
     * @param left left-most caliper bar position, i.e. minimum x position of caliper
     * @param right right-most caliper bar position, i.e. maximum x position of caliper
     * @param center crossbar position, i.e. y position
     * @param bounds bounds of the text block
     * @param canvas the canvas you are drawing on
     * @param textPosition TextPosition of text
     * @param optimizeTextPosition allow optimization of the label to occur if true
     * @return
     */
    //
    protected PointF caliperTextPosition(float left, float right, float center,
                                         Rect bounds, Canvas canvas, TextPosition textPosition,
                                         Boolean optimizeTextPosition) {
        // Position of our text, based on Paint.Align.CENTER.
        // This assumes X is the center of the text block, and Y is the text baseline.
        PointF textOrigin = new PointF();
        // This is the point used as the origin from which to calculate the textOrigin.
        // This will vary depending on the TextPosition.
        PointF origin = new PointF();
        float textHeight = bounds.height();
        float textWidth = bounds.width();
        float yOffset = (float)ApplicationContextProvider.getContext().getResources().getDimension(R.dimen.caliper_text_offset);
        float xOffset = (float)ApplicationContextProvider.getContext().getResources().getDimension(R.dimen.caliper_text_offset);
        if (direction == Direction.HORIZONTAL) {
            // Guard against the margin obscuring left and right labels.
            TextPosition optimizedPosition = getOptimizedTextPosition(left, right, center,
                    canvas, textPosition, textWidth, textHeight, optimizeTextPosition);
            origin.y = center;
            switch (optimizedPosition) {
                case CenterAbove:
                    origin.x = left + (right - left) / 2;
                    textOrigin.x = origin.x;
                    textOrigin.y = origin.y - yOffset;
                    break;
                case CenterBelow:
                    origin.x = left + (right - left) / 2;
                    textOrigin.x = origin.x;
                    textOrigin.y = origin.y + yOffset + textHeight;
                    break;
                case Left:
                    origin.x = left;
                    textOrigin.x = origin.x - xOffset - textWidth / 2;
                    textOrigin.y = origin.y - yOffset;
                    break;
                case Right:
                    origin.x = right;
                    textOrigin.x = origin.x + xOffset + textWidth / 2;
                    textOrigin.y = origin.y - yOffset;
                    break;
                default:
                    if (BuildConfig.DEBUG) {
                        throw new AssertionError("Invalid TextPosition.");
                    }
                    break;
            }
        }
        else {  // Vertical (amplitude) caliper
            textOrigin.y = textHeight / 2 + left + (right - left) / 2;
            TextPosition optimizedPosition = getOptimizedTextPosition(left, right, center, canvas,
                    textPosition, textWidth, textHeight, optimizeTextPosition);
            switch (optimizedPosition) {
                case Left:
                    textOrigin.x = center - xOffset - textWidth / 2;
                    break;
                case Right:
                    textOrigin.x = center + xOffset + textWidth / 2;
                    break;
                case Top:
                    textOrigin.y = left - yOffset;
                    textOrigin.x = center;
                    break;
                case Bottom:
                    textOrigin.y = right + yOffset + textHeight;
                    textOrigin.x = center;
                    break;
                default:
                    if (BuildConfig.DEBUG) {
                        throw new AssertionError("Invalid TextPosition.");
                    }
                    break;
            }
        }
        return textOrigin;
    }

    @NonNull
    private TextPosition getOptimizedTextPosition(float left, float right, float center, Canvas canvas,
                                                  TextPosition textPosition, float textWidth,
                                                  float textHeight, Boolean optimizeTextPosition) {
        // Just use textPosition if we're not auto-positioning the text.
        if (!autoPositionText || !optimizeTextPosition) {
            return textPosition;
        }
        // Allow a few pixels margin so that screen edge never obscures text.
        float offset = 4;
        TextPosition optimizedPosition = textPosition;
        if (direction == Direction.HORIZONTAL) {
            switch (optimizedPosition) {
                case CenterAbove:
                case CenterBelow:
                    // avoid squeezing label
                    if (textWidth + offset > right - left) {
                        if (textWidth + right + offset > canvas.getWidth()) {
                            optimizedPosition = TextPosition.Left;
                        }
                        else {
                            optimizedPosition = TextPosition.Right;
                        }
                    }
                    break;
                case Left:
                    if (textWidth + offset > left) {
                        if (textWidth + right + offset > canvas.getWidth()) {
                            optimizedPosition = TextPosition.CenterAbove;
                        } else {
                            optimizedPosition = TextPosition.Right;
                        }
                    }
                    break;
                case Right:
                    if (textWidth + right + offset > canvas.getWidth()) {
                        if (textWidth + offset > left) {
                            optimizedPosition = TextPosition.CenterAbove;
                        } else {
                            optimizedPosition = TextPosition.Left;
                        }
                    }
                    break;
                default:
                    optimizedPosition = textPosition;
            }
        }
        else if (direction == Direction.VERTICAL) {
            // watch for squeeze
            if ((optimizedPosition == TextPosition.Left || optimizedPosition == TextPosition.Right)
                    && (textHeight + offset > right - left)) {
                if (left - textHeight - offset < 0) {
                    optimizedPosition = TextPosition.Bottom;
                }
                else {
                    optimizedPosition = TextPosition.Top;
                }
            }
            else {
                switch (optimizedPosition) {
                    case Left:
                        if (textWidth + offset > center) {
                            optimizedPosition = TextPosition.Right;
                        }
                        break;
                    case Right:
                        if (textWidth + center + offset > canvas.getWidth()) {
                            optimizedPosition = TextPosition.Left;
                        }
                        break;
                    case Top:
                        if (left - textHeight - offset < 0) {
                            if (right + textHeight + offset > canvas.getHeight()) {
                                optimizedPosition = TextPosition.Right;
                            } else {
                                optimizedPosition = TextPosition.Bottom;
                            }
                        }
                        break;
                    case Bottom:
                        if (right + textHeight + offset > canvas.getHeight()) {
                            if (left - textHeight - offset < 0) {
                                optimizedPosition = TextPosition.Right;
                            } else {
                                optimizedPosition = TextPosition.Top;
                            }
                        }
                        break;
                    default:
                        optimizedPosition = textPosition;
                }
            }

        }
        return optimizedPosition;
    }

    protected Rect getTextBounds(String text) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds;
    }

    private float leftOrTopMostBarPosition() {
        return bar1Position <= bar2Position ? bar1Position : bar2Position;
    }

    private float rightOrBottomMostBarPosition() {
        return bar1Position > bar2Position ? bar1Position : bar2Position;
    }

    private Boolean textIsCentered() {
        return textPosition == TextPosition.CenterAbove || textPosition == TextPosition.CenterBelow;
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
        float delta = DELTA + 8.0f;  // cross bar delta a little bigger
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

    public boolean isTimeCaliper() {
        return direction == Direction.HORIZONTAL && !isAngleCaliper();
    }

    public void moveCrossBar(float deltaX, float deltaY) {
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

    private MovementDirection swapDirection(MovementDirection direction) {
        switch (direction) {
            case Left:
                return MovementDirection.Up;
            case Right:
                return MovementDirection.Down;
            case Up:
                return MovementDirection.Left;
            case Down:
                return MovementDirection.Right;
            default:
                return MovementDirection.Stationary;
        }
    }

    protected void moveCrossbarDirectionally(float delta, MovementDirection direction) {
        if (getDirection() == Direction.VERTICAL) {
            direction = swapDirection(direction);
        }
        if (direction == MovementDirection.Up || direction == MovementDirection.Down) {
            crossBarPosition += delta;
        }
        else {
            bar1Position += delta;
            bar2Position += delta;
        }
    }

}
