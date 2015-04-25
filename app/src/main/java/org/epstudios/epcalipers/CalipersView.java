package org.epstudios.epcalipers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
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
        init();

    }

    public CalipersView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CalipersView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        locked = false;
        calipers = new ArrayList<Caliper>();
        Caliper c = new Caliper();
        c.setBar1Position(300);
        c.setBar2Position(600);
        c.setCrossBarPosition(400);
        Calibration calibration = new Calibration();
        c.setCalibration(calibration);
        calipers.add(c);
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

    public void selectCaliper(Caliper c) {
        c.setColor(c.getSelectedColor());
        c.setSelected(true);
        // TODO force redisplay
    }

    public void unselectCaliper(Caliper c) {
        c.setColor(c.getUnselectedColor());
        c.setSelected(false);
        // TODO force redisplay
    }

    private void selectCaliperIfNoneSelected() {
        if (calipers.size() > 0 && noCaliperIsSelected()) {
            selectCaliper(calipers.get(calipers.size() - 1));
        }
    }

    private boolean noCaliperIsSelected() {
        boolean noneSelected = true;
        for (int i = calipers.size() - 1; i >= 0; i--) {
            if (calipers.get(i).isSelected()) {
                noneSelected = false;
            }
        }
        return noneSelected;
    }

    private Caliper activeCaliper() {
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

    // TODO private void singleTap(), dragging(),


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);



        return super.onTouchEvent(event);
    }
}
