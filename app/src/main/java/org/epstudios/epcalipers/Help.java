package org.epstudios.epcalipers;

import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.webkit.WebView;

/**
 * Copyright (C) 2015 EP Studios, Inc.
 * www.epstudiossoftware.com
 * <p/>
 * Created by mannd on 4/26/15.
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

public class Help extends AppCompatActivity {

    // Logic here is that passed URL is null, use the Help URL and add the anchor,
    // otherwise use the passed URL and ignore anchor.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        Bundle extras = getIntent().getExtras();
        String url = "";
        if (extras != null) {
            url = extras.getString(getString(R.string.url_extra_key));
            String anchor = extras.getString(getString(R.string.anchor_extra_key));
            if (url == null) {
                String lang = getString(R.string.lang);
                url = getString(R.string.help_url, lang, anchor);
            }
        }
        final WebView webView = findViewById(R.id.webView);
        // Anchors don't work properly off the shelf in Android.  Need to add
        // a delay for page rendering so that the anchors work.
        // See https://stackoverflow.com/questions/3039555/android-webview-anchor-link-jump-link-not-working
        Handler handler = new Handler();
        final String finalUrl = url;
        // Variables inside closure must be final.
        handler.postDelayed(new Runnable() {
            public void run() {
                webView.loadUrl(finalUrl);} }, 400);

        Toolbar actionBar = findViewById(R.id.action_bar);
        setSupportActionBar(actionBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                // NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
