package org.epstudios.epcalipers;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Copyright (C) 2020 EP Studios, Inc.
 * www.epstudiossoftware.com
 * <p>
 * Created by mannd on 6/14/20.
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
public class Alerts {
    public static void simpleAlert(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton(R.string.ok_title, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public static void simpleAlert(Context context, int title, int message) {
        simpleAlert(context, context.getString(title), context.getString(message));
    }

}
