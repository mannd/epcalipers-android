package org.epstudios.epcalipers;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
public class HelpTopicAdapter extends RecyclerView.Adapter<HelpTopicAdapter.TopicViewHolder> {
    private String[] helpTopics;

    public static class TopicViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;

        public TopicViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.help_topic_textView);
        }
    }

    public HelpTopicAdapter(String[] topics) {
        helpTopics = topics;
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.help_topic_layout, parent, false);

        TopicViewHolder vh = new TopicViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder myViewHolder, int i) {
        myViewHolder.textView.setText(helpTopics[i]);
    }

    @Override
    public int getItemCount() {
        return helpTopics.length;
    }

}

