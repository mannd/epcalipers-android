package org.epstudios.epcalipers;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

/**
 * Copyright (C) 2019 EP Studios, Inc.
 * www.epstudiossoftware.com
 * <p>
 * Created by mannd on 2/14/19.
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
public class HelpTopics extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_topics);
        RecyclerView recyclerView = findViewById(R.id.help_topics_recyclerview);

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        final String[] helpTopics = getResources().getStringArray(R.array.help_topics);
        final String[] helpAnchors = getResources().getStringArray(R.array.help_anchors);
        RecyclerView.Adapter adapter = new HelpTopicAdapter(helpTopics);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new HelpRecyclerTouchListener(getApplicationContext(),
                recyclerView, new HelpRecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                String text = helpTopics[position];
                Log.i("EPS", text);
                if (helpAnchors[position].equals("acknowledgments-id")) {
                    showHelp("https://mannd.github.io/epcalipers/"
                            + getString(R.string.lang) + ".lproj/EPCalipers-help/acknowledgments_android.html", null);
                }
                else {
                    showHelp(null, helpAnchors[position]);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                Log.i("EPS", "long click");
            }
        }));

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

    private void showHelp(String url, String anchor) {
        Intent intent = new Intent(this, Help.class);
        intent.putExtra("URL", url);
        intent.putExtra("Anchor", anchor);
        startActivity(intent);
    }
}



