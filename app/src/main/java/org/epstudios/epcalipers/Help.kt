package org.epstudios.epcalipers

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.webkit.WebView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

/**
 * Copyright (C) 2015 EP Studios, Inc.
 * www.epstudiossoftware.com
 *
 *
 * Created by mannd on 4/26/15.
 *
 *
 * This file is part of org.epstudios.epcalipers.
 *
 *
 * org.epstudios.epcalipers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *
 * org.epstudios.epcalipers is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with org.epstudios.epcalipers.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
class Help : AppCompatActivity() {
    // Logic here is that passed URL is null, use the Help URL and add the anchor,
    // otherwise use the passed URL and ignore anchor.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.help)
        setupInsets(R.id.help_root_view)
        val extras = getIntent().getExtras()
        var url: String? = ""
        if (extras != null) {
            url = extras.getString(getString(R.string.url_extra_key))
            val anchor = extras.getString(getString(R.string.anchor_extra_key))
            if (url == null) {
                val lang = getString(R.string.lang)
                url = getString(R.string.help_url, lang, anchor)
            }
        }
        val webView = findViewById<WebView>(R.id.webView)

        // Anchors don't work properly off the shelf in Android.  Need to add
        // a delay for page rendering so that the anchors work.
        // See https://stackoverflow.com/questions/3039555/android-webview-anchor-link-jump-link-not-working
        // Original deprecated code:
        // Handler handler = new Handler();

        // Fixed code:
        val handler = Handler(Looper.getMainLooper())
        val finalUrl = url
        // See https://stackoverflow.com/questions/57449900/letting-webview-on-android-work-with-prefers-color-scheme-dark
        val nightModeFlags =
            getResources().getConfiguration().uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.getSettings(), true)
            }
        }
        // Variables inside closure must be final.
        handler.postDelayed(Runnable { webView.loadUrl(finalUrl!!) }, 400)

        val actionBar = findViewById<Toolbar?>(R.id.action_bar)
        setSupportActionBar(actionBar)
        if (getSupportActionBar() != null) {
            getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    protected fun setupInsets(@IdRes viewId: Int) {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(viewId)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = insets.bottom, top = insets.top)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) { // NavUtils.navigateUpFromSameTask(this);
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
