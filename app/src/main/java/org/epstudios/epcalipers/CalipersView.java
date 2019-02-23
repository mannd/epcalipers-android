package org.epstudios.epcalipers;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import androidx.core.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.flask.colorpicker.ColorPickerView;
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

    Context context;

    private GestureDetectorCompat gestureDetector;

    public Caliper getTouchedCaliper() {
        return touchedCaliper;
    }

    private Caliper touchedCaliper;
    private ArrayList<Caliper> calipers;

    public ArrayList<Caliper> getCalipers() {
        return calipers;
    }
    public void setCalipers(ArrayList<Caliper> calipers) {
        this.calipers = calipers;
    }


    public boolean isLocked() {
        return locked;
    }
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    private boolean locked;

    public void setLockImage(boolean lockImage) {
        this.lockImage = lockImage;
    }
    private boolean lockImage;

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    private MainActivity mainActivity;

    public Caliper.Component getPressedComponent() {
        return pressedComponent;
    }
    private Caliper.Component pressedComponent = Caliper.Component.None;

    public void setAllowColorChange(boolean allowColorChange) {
        this.allowColorChange = allowColorChange;
    }

    public void setAllowTweakPosition(boolean allowTweakPosition) {
        this.allowTweakPosition = allowTweakPosition;
    }

    private boolean allowColorChange = false;
    private boolean allowTweakPosition = false;

    public boolean isACaliperIsMarching() {
        return aCaliperIsMarching;
    }
    public void setACaliperIsMarching(boolean aCaliperIsMarching) {
        this.aCaliperIsMarching = aCaliperIsMarching;
    }
    private boolean aCaliperIsMarching = false;

    public void setTweakingOrColoring(boolean tweakingOrColoring) {
        this.tweakingOrColoring = tweakingOrColoring;
    }

    public boolean isTweakingOrColoring() {
        return tweakingOrColoring;
    }

    private boolean tweakingOrColoring = false;

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
        this.context = context;
        locked = false;
        lockImage = false;
        calipers = new ArrayList<>();
        touchedCaliper = null;
        MyGestureListener listener = new MyGestureListener();
        gestureDetector = new GestureDetectorCompat(context, listener);
        gestureDetector.setIsLongpressEnabled(true);
        View.OnTouchListener gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                return gestureDetector.onTouchEvent(event);
            }
        };

        setOnTouchListener(gestureListener);


    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            // must be implemented and return true for other events to work;
            for (int i = calipersCount() - 1; i >= 0; i--) {
                if (calipers.get(i).pointNearCaliper(new PointF(event.getX(), event.getY()))) {
                    setTouchedCaliper(event);
                    return true;
                }
            }
            // Note returning false here allows the event to pass to the imageView below.
            // However, not handling the event also triggers the onLongPress event in this
            // GestureListener.
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
            Log.i("EPS", "long click on caliper");
            PointF point = new PointF(e.getX(), e.getY());
            if (mainActivity.getCurrentActionMode() == null && !tweakingOrColoring && caliperPressed(point) != null) {
                startActionMode(mainActivity.calipersActionCallback);
            }
            if (allowColorChange) {
                changeColor(point);
            } else if (allowTweakPosition) {
                microMove(point);
            }
        }
    }

    // Returns caliper if point near it, otherwise returns null
    public Caliper caliperPressed(PointF point) {
        for (Caliper c : calipers) {
            if (c.pointNearCaliper(point)) {
               return c;
            }
        }
        return null;
    }

    private void changeColor(PointF point) {
        for (Caliper c : calipers) {
            if (c.pointNearCaliper(point)) {
                final Caliper pressedCaliper = c;
                // better if unselected color is shown after color change
                c.setSelected(false);
                // https://github.com/QuadFlask/colorpicker
                ColorPickerDialogBuilder
                        .with(getContext())
                        .setTitle(getContext().getString(R.string.choose_color_label))
                        .initialColor(pressedCaliper.getUnselectedColor())
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .density(12)
                        .setPositiveButton(getContext().getString(R.string.ok_title), new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int chosenColor, Integer[] allColors) {
                                if (!pressedCaliper.isSelected()) {
                                    pressedCaliper.setColor(chosenColor);
                                }
                                pressedCaliper.setUnselectedColor(chosenColor);
                                invalidate();
                            }
                        })
                        .setNegativeButton(getContext().getString(R.string.cancel_title), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();
                break;
            }
        }
    }

    private void microMove(PointF point) {
        for (Caliper c : calipers) {
            if (c.pointNearBar1(point)) {
                setupMicroMovements(c, Caliper.Component.Bar1);
                break;
            } else if (c.pointNearBar2(point)) {
                setupMicroMovements(c, Caliper.Component.Bar2);
                break;
            } else if (c.pointNearCrossBar(point)) {
                setupMicroMovements(c, Caliper.Component.Crossbar);
                break;
            }
        }
    }



    private void setupMicroMovements(Caliper c, Caliper.Component component) {
        unchooseAllCalipersAndComponents();
        pressedComponent = component;
        c.setChosen(true);
        c.setChosenComponent(component);
        mainActivity.selectMicroMovementMenu(c, component);
        invalidate();
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
        if (lockImage) {
            showLockWarning(canvas);
        }
    }

    private void showLockWarning(Canvas canvas) {
        String text = getContext().getString(R.string.image_locked);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(200);
        canvas.drawRect(0, 45, this.getWidth(), 95, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAlpha(255);
        paint.setStrokeWidth(3.0f);
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        paint.setTextSize(48.0f);

        canvas.drawText(text, (float) (this.getWidth() / 2.0), 88.0f, paint);
    }

    private void setTouchedCaliper(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        touchedCaliper = null;
        PointF p = new PointF(x, y);
        for (int i = calipersCount() - 1; i >= 0; i--) {
            if (calipers.get(i).pointNearCaliper(p) && touchedCaliper == null) {
                touchedCaliper = calipers.get(i);
                touchedCaliper.setTouchedBar(Caliper.Component.None);
                if (touchedCaliper.pointNearCrossBar(p)) {
                    touchedCaliper.setTouchedBar(Caliper.Component.Crossbar);
                } else if (touchedCaliper.pointNearBar1(p)) {
                    touchedCaliper.setTouchedBar(Caliper.Component.Bar1);
                } else if (touchedCaliper.pointNearBar2(p)) {
                    touchedCaliper.setTouchedBar(Caliper.Component.Bar2);
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

    public Caliper chosenCaliper() {
        for (Caliper c : calipers) {
            if (c.isChosen()) {
                return c;
            }
        }
        return null;
    }

    public void unchooseAllCalipers() {
        for (Caliper c : calipers) {
            c.setChosen(false);
        }
    }

    public void unchooseAllComponents() {
        for (Caliper c : calipers) {
            c.setChosenComponent(Caliper.Component.None);
        }
    }

    public void unchooseAllCalipersAndComponents() {
        unchooseAllCalipers();
        unchooseAllComponents();
    }

    public void selectCaliperAndUnselectOthers(Caliper c) {
        selectCaliper(c);
        unselectCalipersExcept(c);
    }

    private void unselectCalipersExcept(Caliper c) {
        // if only one caliper, no others can be selected
        if (calipersCount() > 1) {
            for (Caliper caliper : calipers) {
                if (caliper != c) {
                    unselectCaliper(caliper);
                }
            }
        }
    }

    public void unselectAllCalipers() {
        for (Caliper c : calipers) {
            c.setSelected(false);
        }
        invalidate();
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
            } else {
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
                if (thereAreNoTimeCalipers()) {
                    aCaliperIsMarching = false;
                }
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
            //noinspection SuspiciousNameCombination
            distanceX = distanceY;
            distanceY = tmp;
        }
        if (touchedCaliper.getTouchedBar() == Caliper.Component.Crossbar) {
            touchedCaliper.moveCrossBar(distanceX, distanceY);
        } else if (touchedCaliper.getTouchedBar() == Caliper.Component.Bar1) {
            touchedCaliper.moveBar1(distanceX, distanceY, event);
        } else if (touchedCaliper.getTouchedBar() == Caliper.Component.Bar2) {
            touchedCaliper.moveBar2(distanceX, distanceY, event);
        }
        invalidate();
    }

    public void toggleShowMarchingCaliper() {
        if (calipersCount() <= 0) {
            aCaliperIsMarching = false;
            return;
        }
        if (aCaliperIsMarching) {
            for (Caliper c : calipers) {
                c.setMarching(false);
            }
            aCaliperIsMarching = false;
            return;
        }
        // first try to find a selected Time caliper
        for (Caliper c : calipers) {
            if (c.isSelected() && c.isTimeCaliper()) {
                c.setMarching(true);
                aCaliperIsMarching = true;
                return;
            }
        }
        // if not, settle for the first Time caliper
        for (Caliper c : calipers) {
            if (c.isTimeCaliper()) {
                c.setMarching(true);
                aCaliperIsMarching = true;
                return;
            }
        }
        // otherwise give up
        aCaliperIsMarching = false;
    }

    private boolean thereAreNoTimeCalipers() {
        if (calipersCount() <= 0) {
            return true;
        }
        boolean noTimeCalipers = true;
        for (Caliper c : calipers) {
            if (c.isTimeCaliper()) {
                noTimeCalipers = false;
                break;
            }
        }
        return noTimeCalipers;
    }

}
