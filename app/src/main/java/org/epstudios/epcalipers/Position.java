package org.epstudios.epcalipers;

/**
 * Copyright (C) 2020 EP Studios, Inc.
 * www.epstudiossoftware.com
 * <p>
 * Created by mannd on 9/12/20.
 * <p>
 * This file is part of epcalipers-android.
 * <p>
 * epcalipers-android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * epcalipers-android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with epcalipers-android.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Position {
    public static float translateToAbsolutePositionX(
            float scaledPositionX,
            float offsetX,
            float scale) {
        return (scaledPositionX - offsetX) / scale;
    }

    public static float translateToScaledPositionX(
            float absolutePositionX,
            float offsetX,
            float scale) {
        return scale * absolutePositionX + offsetX;
    }
}
