package org.epstudios.epcalipers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Copyright (C) 2019 EP Studios, Inc.
 * www.epstudiossoftware.com
 * <p>
 * Created by mannd on 2/22/19.
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
public class Version {
    private Context context;
    private SharedPreferences prefs;

    public String getVersionName() {
        return versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    private String versionName;
    private int versionCode;

    Version(Context context, SharedPreferences prefs, String versionName, int versionCode) {
        this.context = context;
        this.prefs = prefs;
        this.versionName = versionName;
        this.versionCode = versionCode;
    }

    public String getPreviousAppVersion() {
	String previousVersion = prefs.getString(context.getString(R.string.app_version_key), null);
	return previousVersion;
    }

    public boolean isNewInstallation() {
	String previousVersion = getPreviousAppVersion();
	return (previousVersion != null);
    }

    public boolean isUpgrade() {
        if (isNewInstallation()) {
            return false;
        }
        return (!versionName.equals(getPreviousAppVersion()));
    }
}
