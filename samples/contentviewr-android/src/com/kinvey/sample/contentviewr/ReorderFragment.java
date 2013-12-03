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

import android.view.View;
import com.actionbarsherlock.app.SherlockFragment;
import com.kinvey.sample.contentviewr.core.ContentFragment;
import com.kinvey.sample.contentviewr.dslv.DragSortController;
import com.kinvey.sample.contentviewr.dslv.DragSortListView;

/**
 * @author edwardf
 */
public class ReorderFragment extends ContentFragment {

    private DragSortListView list;
    private DragSortController controller;


    @Override
    public int getViewID() {
        return R.layout.fragment_reorder;
    }

    @Override
    public void bindViews(View v) {

    }

    @Override
    public String getTitle() {
        return "reorder";
    }
}
