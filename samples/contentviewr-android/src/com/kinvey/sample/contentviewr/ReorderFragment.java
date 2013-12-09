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

import android.app.Activity;
import android.content.Context;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.kinvey.sample.contentviewr.core.ContentFragment;
import com.kinvey.sample.contentviewr.dslv.DragSortController;
import com.kinvey.sample.contentviewr.dslv.DragSortListView;
import com.kinvey.sample.contentviewr.model.ContentType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author edwardf
 */
public class ReorderFragment extends ContentFragment {

    private DragSortListView list;
    private DragSortController controller;
    private DraggableTypeAdapter adapter;

    private ContentTypePager pager;



    public ReorderFragment(ContentTypePager pager){
        this.pager = pager;
    }


    @Override
    public int getViewID() {
        return R.layout.fragment_reorder;
    }

    @Override
    public void bindViews(View v) {

        list = (DragSortListView) v.findViewById(R.id.list);

        controller = new DragSortController(list);
        controller.setDragHandleId(R.id.drag_handle);
        controller.setRemoveEnabled(false);
        controller.setSortEnabled(true);
        controller.setDragInitMode(DragSortController.ON_DOWN);
        controller.setRemoveMode(DragSortController.FLING_REMOVE);

        list.setFloatViewManager(controller);
        list.setOnTouchListener(controller);
        list.setDragEnabled(true);
        list.setDropListener(onDrop);
        setAdapter();
    }

    private void setAdapter(){

        List<String> order = (List<String>) getClient().user().get("ordering");
        List<ContentType> contents = new ArrayList<ContentType>();

        for (String s : order){
            contents.add(getContentType().get(s));
        }

        adapter = new DraggableTypeAdapter(getSherlockActivity(), contents, (LayoutInflater) getSherlockActivity().getSystemService(
                Activity.LAYOUT_INFLATER_SERVICE));
        list.setAdapter(adapter);
    }

    @Override
    public String getTitle() {
        return "Reorder";
    }
    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {


            Log.i("OK", "wtf, " + from + " + " + to);
            List<String> order = (List<String>) getClient().user().get("ordering");

            String current =  order.get(from);
            order.remove(from);
            order.add(to, current);

            getClient().user().put("ordering", order);
            getClient().user().update(null);

            pager.loadOrderInAdapter();
            //adapter.notifyDataSetChanged();
            setAdapter();





        }
    };

    public class DraggableTypeAdapter extends ArrayAdapter<ContentType> {

        private LayoutInflater mInflater;

        public DraggableTypeAdapter(Context context, List<ContentType> objects, LayoutInflater inf) {
            // NOTE: I pass an arbitrary textViewResourceID to the super
            // constructor-- Below I override
            // getView(...), which causes the underlying adapter to ignore this
            // field anyways, it is just needed in the constructor.
            super(context, 0, objects);
            this.mInflater = inf;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            FilterViewHolder holder = null;

            TextView name = null;
            TextView subtext = null;
            // TextView value = null;

            ContentType rowData = getItem(position);

            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.row_content_type, null);
                holder = new FilterViewHolder(convertView);
                convertView.setTag(holder);
            }
            holder = (FilterViewHolder) convertView.getTag();

//            if (!rowData.isLabel() && !rowData.isSetting()){
//                holder.getDrag().setVisibility(View.GONE);
//            }

            name = holder.getName();
            subtext = holder.getSubtext();

            name.setText(rowData.getDisplayName());
            subtext.setText("");

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
        private class FilterViewHolder {
            private View mRow;

            private TextView name = null;
            private TextView subtext = null;
            private ImageView drag = null;

            public FilterViewHolder(View row) {
                mRow = row;
            }

            public TextView getName() {
                if (name == null) {
                    name = (TextView) mRow.findViewById(R.id.row_type_name);
                }
                return name;
            }

            public TextView getSubtext() {
                if (subtext == null) {
                    subtext = (TextView) mRow.findViewById(R.id.row_type_details);
                }
                return subtext;
            }

            public ImageView getDrag(){
                if (drag == null){
                    drag = (ImageView) mRow.findViewById(R.id.drag_handle);
                }
                return drag;
            }
        }

    }

}
