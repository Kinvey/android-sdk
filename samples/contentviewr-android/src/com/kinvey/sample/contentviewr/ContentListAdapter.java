/*
 * Copyright (c) 2013 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.sample.contentviewr;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.kinvey.sample.contentviewr.model.ContentItem;

import java.util.List;

/**
 * @author edwardf
 */
public class ContentListAdapter extends ArrayAdapter<ContentItem> {

    private LayoutInflater mInflater;
    private Typeface roboto;


    public ContentListAdapter(Context context, List<ContentItem> objects,
                          LayoutInflater inf) {
        // NOTE: I pass an arbitrary textViewResourceID to the super
        // constructor-- Below I override
        // getView(...), which causes the underlying adapter to ignore this
        // field anyways, it is just needed in the constructor.
        super(context, R.id.content_row_name, objects);
        this.mInflater = inf;
        roboto = Typeface.createFromAsset(context.getAssets(), "Roboto-Thin.ttf");

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FeatureViewHolder holder = null;

        TextView name = null;
        TextView blurb = null;

        ContentItem rowData = getItem(position);

        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.content_list_item, null);
            holder = new FeatureViewHolder(convertView);
            convertView.setTag(holder);
        }
        holder = (FeatureViewHolder) convertView.getTag();

        name = holder.getName();
        name.setText(rowData.getName());
        blurb = holder.getBlurb();
        blurb.setText(rowData.getBlurb());

        return convertView;
    }

    /**
     * This pattern is used as an optimization for Android ListViews.
     *
     * Since every row uses the same layout, the View object itself can be
     * recycled, only the data/content of the row has to be updated.
     *
     * This allows for Android to only inflate enough Row Views to fit on
     * screen, and then they are recycled. This allows us to avoid creating
     * a new view for every single row, which can have a negative effect on
     * performance (especially with large lists on large screen devices).
     *
     */
    private class FeatureViewHolder {
        private View row;

        private TextView name = null;
        private TextView blurb = null;

        public FeatureViewHolder(View row) {
            this.row = row;
        }

        public TextView getName() {
            if (null == name) {
                name = (TextView) row.findViewById(R.id.content_row_name);
            }
            name.setTypeface(roboto);
            return name;
        }

        public TextView getBlurb() {
            if (null == blurb) {
                blurb = (TextView) row.findViewById(R.id.content_row_blurb);
            }
            blurb.setTypeface(roboto);
            return blurb;
        }



    }
}