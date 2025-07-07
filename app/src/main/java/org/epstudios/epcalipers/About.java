package org.epstudios.epcalipers;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class About extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        TextView versionTextView = findViewById(R.id.version);

        String versionNumberString = ""; // Default to empty string
        long appVersionCode = 0; // Use long for versionCode

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionNumberString = packageInfo.versionName;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                appVersionCode = packageInfo.getLongVersionCode();
            } else {
                // This is the deprecated way but necessary for API < 28
                @SuppressWarnings("deprecation")
                long tempVersionCode = packageInfo.versionCode;
                appVersionCode = tempVersionCode;
            }

        } catch (PackageManager.NameNotFoundException e) {
            // This should not happen if getPackageName() is correct
            Log.e("AboutActivity", "Could not get package info", e);
            // Optionally set versionNumberString to an error message or leave as default
            versionNumberString = "N/A";
        }

        // Construct the final version string
        String displayVersion = versionNumberString;
        if (BuildConfig.DEBUG) {
            // Only append versionCode in debug builds if it was successfully retrieved
            if (appVersionCode > 0) {
                displayVersion = versionNumberString + "+" + appVersionCode;
            } else {
                // If appVersionCode is still 0 (e.g., due to an error, though unlikely here)
                // you might choose to not append it or handle it differently
                displayVersion = versionNumberString + "+debug"; // Fallback for debug
            }
        }

        versionTextView.setText(String.format(getString(R.string.app_version), displayVersion));
    }
}
