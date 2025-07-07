package org.epstudios.epcalipers

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.epstudios.epcalipers.HelpRecyclerTouchListener.ClickListener
import org.epstudios.epcalipers.HelpTopicAdapter.TopicViewHolder

/**
 * Copyright (C) 2019 EP Studios, Inc.
 * www.epstudiossoftware.com
 *
 *
 * Created by mannd on 2/14/19.
 *
 *
 * This file is part of epcalipers-android.
 *
 *
 * epcalipers-android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *
 * epcalipers-android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with epcalipers-android.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
class HelpTopics : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.help_topics)
        setupInsets(R.id.help_topics_root_view)
        val recyclerView = findViewById<RecyclerView>(R.id.help_topics_recyclerview)

        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))


        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView.setLayoutManager(layoutManager)

        val helpTopics = getResources().getStringArray(R.array.help_topics)
        val helpAnchors = getResources().getStringArray(R.array.help_anchors)
        val adapter: RecyclerView.Adapter<TopicViewHolder?> = HelpTopicAdapter(helpTopics)
        recyclerView.setAdapter(adapter)

        recyclerView.addOnItemTouchListener(
            HelpRecyclerTouchListener(
                getApplicationContext(),
                recyclerView, object : ClickListener {
                    @Suppress("unused")
                    override fun onClick(view: View?, position: Int) {
                        if (helpAnchors[position] == "acknowledgments-id") {
                            val lang = getString(R.string.lang)
                            showHelp(getString(R.string.acknowledgments_url, lang), null)
                            EPSLog.log(getString(R.string.acknowledgments_url, lang))
                        } else {
                            showHelp(null, helpAnchors[position])
                        }
                    }

                    @Suppress("unused")
                    override fun onLongClick(view: View?, position: Int) {
                    }
                })
        )

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

    private fun showHelp(url: String?, anchor: String?) {
        val intent = Intent(this, Help::class.java)
        intent.putExtra(getString(R.string.url_extra_key), url)
        intent.putExtra(getString(R.string.anchor_extra_key), anchor)
        startActivity(intent)
    }
}



