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
}
