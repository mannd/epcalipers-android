package org.epstudios.epcalipers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Copyright (C) 2015 EP Studios, Inc.
 * www.epstudiossoftware.com
 * <p/>
 * Created by mannd on 4/17/15.
 * <p/>
 * This file is part of org.epstudios.epcalipers.
 * <p/>
 * org.epstudios.epcalipers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * org.epstudios.epcalipers is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with org.epstudios.epcalipers.  If not, see <http://www.gnu.org/licenses/>.
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
        gestureDetector.setIsLongpressEnabled(false);
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
            // must be implemented and return true;
            setTouchedCaliper(event);
            return true;
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
            move(-distanceX, -distanceY);
            return true;
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
                else if (touchedCaliper.pointNearBar(p, touchedCaliper.getBar1Position())) {
                    touchedCaliper.setTouchedBar(Caliper.TouchedBar.BAR1);
                }
                else if (touchedCaliper.pointNearBar(p, touchedCaliper.getBar2Position())) {
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

    public void singleTap(PointF pointF) {
        boolean selectionMade = false;
        for (int i = calipersCount() - 1; i >= 0; i--) {
            if (calipers.get(i).pointNearCaliper(pointF)
                    && !selectionMade) {
                if (calipers.get(i).isSelected() &&
                        !isLocked()) {
                    calipers.remove(i);
                    invalidate();
                    return;
                } else {
                    selectCaliper(calipers.get(i));
                    selectionMade = true;
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
        for (int i = calipersCount() - 1; i >= 0; i--) {
            if (calipers.get(i).pointNearCaliper(pointF)) {
                if (!isLocked()) {
                    calipers.remove(i);
                    invalidate();
                    return;
                }
            }
        }
    }

    public void move(float distanceX, float distanceY) {
        if (touchedCaliper == null) {
            return;
        }
        if (touchedCaliper.getDirection() == Caliper.Direction.VERTICAL) {
            float tmp = distanceX;
            distanceX = distanceY;
            distanceY = tmp;
        }
        if (touchedCaliper.getTouchedBar() == Caliper.TouchedBar.CROSSBAR) {
            touchedCaliper.setBar1Position(touchedCaliper.getBar1Position() + distanceX);
            touchedCaliper.setBar2Position(touchedCaliper.getBar2Position() + distanceX);
            touchedCaliper.setCrossbarPosition(touchedCaliper.getCrossbarPosition() + distanceY);
        }
        else if (touchedCaliper.getTouchedBar() == Caliper.TouchedBar.BAR1) {
            touchedCaliper.setBar1Position(touchedCaliper.getBar1Position() + distanceX);
        }
        else if (touchedCaliper.getTouchedBar() == Caliper.TouchedBar.BAR2) {
            touchedCaliper.setBar2Position(touchedCaliper.getBar2Position() + distanceX);
        }
        invalidate();
    }
}
