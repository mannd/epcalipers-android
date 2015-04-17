package org.epstudios.epcalipers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

/**
 * Copyright (C) 2015 EP Studios, Inc.
 * www.epstudiossoftware.com
 * <p/>
 * Created by mannd on 4/16/15.
 * <p/>
 * This file is part of EP Mobile.
 * <p/>
 * EP Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * EP Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with EP Mobile.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Caliper {
    static int differential = 0;
    static final int DELTA = 0;

    public int getBar1Position() {
        return bar1Position;
    }

    public void setBar1Position(int bar1Position) {
        this.bar1Position = bar1Position;
    }

    public int getBar2Position() {
        return bar2Position;
    }

    public void setBar2Position(int bar2Position) {
        this.bar2Position = bar2Position;
    }

    public int getCrossBarPosition() {
        return crossBarPosition;
    }

    public void setCrossBarPosition(int crossBarPosition) {
        this.crossBarPosition = crossBarPosition;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public int getValueInPoints() {
        return valueInPoints;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public enum Direction { HORIZONTAL, VERTICAL };

    private int bar1Position;
    private int bar2Position;
    private int crossBarPosition;
    private Direction direction;
    private int color;
    private int unselectedColor;
    private int selectedColor;
    private int lineWidth;
    private int valueInPoints;
    private boolean selected;

    public Calibration getCalibration() {
        return calibration;
    }

    public void setCalibration(Calibration calibration) {
        this.calibration = calibration;
    }

    private Calibration calibration;


    public Caliper(Direction direction, int bar1Position, int bar2Position,
                   int crossBarPosition) {
        this.direction = direction;
        this.bar1Position = bar1Position;
        this.bar2Position = bar2Position;
        this.crossBarPosition = crossBarPosition;
        this.unselectedColor = Color.BLUE;
        this.selectedColor = Color.RED;
        this.color = Color.BLUE;
        this.lineWidth = 2;
        this.selected = false;
    }

    public Caliper() {
        this(Direction.HORIZONTAL, 0, 0, 100);
    }

    public void setInitialPosition(Rect rect) {
        Log.d("EPS", "Left = " + rect.left + " Right = " + rect.right +
                " Top = " + rect.top + " Bottom = " + rect.bottom);
        Log.d("EPS", "Width = " + rect.width() + "Height = " + rect.height());
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
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(lineWidth);
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
        String text = measurement();
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(direction == Direction.HORIZONTAL ? Paint.Align.CENTER :
                Paint.Align.LEFT);

        paint.setTextSize(18.0f);
       // paint.setTextScaleX(0.8f);
        if (direction == Direction.HORIZONTAL) {
            canvas.drawText(text, 200, 300, paint);

        }
        else {

        }

    }

    public int barCoord(Point p) {
        return (direction == Direction.HORIZONTAL ? p.x : p.y);
    }

    public Rect containerRect() {
        // TODO
        return new Rect();
    }

    public String measurement() {
        // TODO
        return "Test";
    }

    // TODO more methods

}
