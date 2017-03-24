package org.epstudios.epcalipers;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;

/**
 * Copyright (C) 2015 EP Studios, Inc.
 * www.epstudiossoftware.com
 * <p/>
 * Created by mannd on 4/17/15.
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
public class CalipersView extends View {

    private GestureDetectorCompat gestureDetector;
    private final static String EPS = "EPS";

    private Caliper touchedCaliper;

    public ArrayList<Caliper> getCalipers() {
        return calipers;
    }

    public void setCalipers(ArrayList<Caliper> calipers) {
        this.calipers = calipers;
    }

    private ArrayList<Caliper> calipers;

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    boolean locked;


    public CalipersView(Context context) {
        super(context);
        init(context);
    }

    public CalipersView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CalipersView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        locked = false;
        calipers = new ArrayList<>();
        touchedCaliper = null;
        MyGestureListener listener = new MyGestureListener();
        gestureDetector = new GestureDetectorCompat(context, listener);
        gestureDetector.setIsLongpressEnabled(true);
        View.OnTouchListener gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
            }
        };

        setOnTouchListener(gestureListener);


    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            // must be implemented and return true for other events to work;
            Log.d(EPS, "onDown");
            for (int i = calipersCount() - 1; i >= 0; i--) {
                if (calipers.get(i).pointNearCaliper(new PointF(event.getX(), event.getY()))) {
                    setTouchedCaliper(event);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            singleTap(new PointF(event.getX(), event.getY()));
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            return true;
        }


        @Override
        public boolean onDoubleTap(MotionEvent event) {
            doubleTap(new PointF(event.getX(), event.getY()));
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY) {
            move(e2, -distanceX, -distanceY);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            for (Caliper c : calipers) {
                if (c.pointNearCrossBar(new PointF(e.getX(), e.getY()))) {
                    final Caliper selectedCaliper = c;
                    Log.d(EPS, "Longpress detected near caliper");
                    // https://github.com/QuadFlask/colorpicker
                    ColorPickerDialogBuilder
                            .with(getContext())
                            .setTitle("Choose color")
                            .initialColor(selectedCaliper.getUnselectedColor())
                            .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                            .density(12)
                            .setOnColorSelectedListener(new OnColorSelectedListener() {
                                @Override
                                public void onColorSelected(int selectedColor) {
                                    Log.d(EPS, "onColorSelected: 0x" + Integer.toHexString(selectedColor));

                                }
                            })
                            .setPositiveButton("ok", new ColorPickerClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                    Log.d(EPS, "Selected color is " + selectedColor);
                                    selectedCaliper.setColor(selectedColor);
                                    selectedCaliper.setUnselectedColor(selectedColor);
                                    invalidate();
                                }
                            })
                            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .build()
                            .show();
                }
            }
        }


    }

    public void setPaint(int caliperColor, int highlightColor, int lineWidth) {
        if (calipersCount() <= 0) {
            return;
        }
        for (Caliper c : calipers) {
            c.setUnselectedColor(caliperColor);
            c.setSelectedColor(highlightColor);
            c.setLineWidth(lineWidth);
        }
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (calipers.size() == 0)
            return;
        for (Caliper c : calipers) {
            c.draw(canvas);
        }
    }

    private void setTouchedCaliper(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        touchedCaliper = null;
        PointF p = new PointF(x, y);
        for (int i = calipersCount() - 1; i >= 0; i--) {
            if (calipers.get(i).pointNearCaliper(p) && touchedCaliper == null) {
                touchedCaliper = calipers.get(i);
                touchedCaliper.setTouchedBar(Caliper.TouchedBar.NONE);
                if (touchedCaliper.pointNearCrossBar(p)) {
                    touchedCaliper.setTouchedBar(Caliper.TouchedBar.CROSSBAR);
                }
                else if (touchedCaliper.pointNearBar1(p)) {
                    touchedCaliper.setTouchedBar(Caliper.TouchedBar.BAR1);
                }
                else if (touchedCaliper.pointNearBar2(p)) {
                    touchedCaliper.setTouchedBar(Caliper.TouchedBar.BAR2);
                }
            }
        }
    }

    public void selectCaliper(Caliper c) {
        c.setColor(c.getSelectedColor());
        c.setSelected(true);
        invalidate();
    }

    public void unselectCaliper(Caliper c) {
        c.setColor(c.getUnselectedColor());
        c.setSelected(false);
        invalidate();
    }

    public void selectCaliperIfNoneSelected() {
        if (calipers.size() > 0 && noCaliperIsSelected()) {
            selectCaliper(calipers.get(calipers.size() - 1));
        }
    }

    public boolean noCaliperIsSelected() {
        boolean noneSelected = true;
        for (int i = calipers.size() - 1; i >= 0; i--) {
            if (calipers.get(i).isSelected()) {
                noneSelected = false;
            }
        }
        return noneSelected;
    }

    public Caliper activeCaliper() {
        if (calipers.size() <= 0) {
            return null;
        }
        Caliper c = null;
        for (int i = calipers.size() - 1; i >= 0; i--) {
            if (calipers.get(i).isSelected()) {
                c = calipers.get(i);
            }
        }
        return c;
    }

    public int calipersCount() {
        return calipers.size();
    }

    public void addCaliper(Caliper c) {
        calipers.add(c);
    }

    public void removeCaliper(Caliper c) {
        calipers.remove(c);
    }

    // Single tap initially highlights (selects) caliper,
    // second tap unselects it.  Quick double tap is used
    // to delete caliper.  This is new behavior with v2.0+.
    public void singleTap(PointF pointF) {
        if (isLocked()) {
            return;
        }
        boolean caliperToggled = false;
        for (int i = calipersCount() - 1; i >= 0; i--) {
            if (calipers.get(i).pointNearCaliper(pointF) && !caliperToggled) {
                caliperToggled = true;
                if (calipers.get(i).isSelected()) {
                    unselectCaliper(calipers.get(i));
                } else {
                    selectCaliper(calipers.get(i));
                }
            }
            else {
                unselectCaliper(calipers.get(i));
            }
        }
    }

    // Need a separate procedure for doubleTap, unlike in iOS,
    // since two fast taps won't count as two singleTaps.
    public void doubleTap(PointF pointF) {
        if (isLocked()) {
            return;
        }
        for (int i = calipersCount() - 1; i >= 0; i--) {
            if (calipers.get(i).pointNearCaliper(pointF)) {
                calipers.remove(i);
                invalidate();
                return;
            }
        }
    }

    public void move(MotionEvent event, float distanceX, float distanceY) {
        if (touchedCaliper == null) {
            return;
        }
        if (touchedCaliper.getDirection() == Caliper.Direction.VERTICAL) {
            float tmp = distanceX;
            distanceX = distanceY;
            distanceY = tmp;
        }
        if (touchedCaliper.getTouchedBar() == Caliper.TouchedBar.CROSSBAR) {
            touchedCaliper.moveCrossBar(distanceX, distanceY, event);
        }
        else if (touchedCaliper.getTouchedBar() == Caliper.TouchedBar.BAR1) {
            touchedCaliper.moveBar1(distanceX, distanceY, event);
        }
        else if (touchedCaliper.getTouchedBar() == Caliper.TouchedBar.BAR2) {
            touchedCaliper.moveBar2(distanceX, distanceY, event);
        }
        invalidate();
    }

}
