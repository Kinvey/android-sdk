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
import android.view.LayoutInflater;
import android.view.View;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.Query;
import com.kinvey.java.query.AbstractQuery;
import com.kinvey.sample.contentviewr.model.ContentItem;

import java.util.Arrays;

import static com.kinvey.sample.contentviewr.Contentviewr.CONTENT_COLLECTION;

/**
 * @author edwardf
 */
public class RecentFragment extends ContentListFragment {


    @Override
    public void reset(){
        loading.setVisibility(View.VISIBLE);
        Query q = new Query().setLimit(10);
        q.addSort("_kmd.ect", AbstractQuery.SortOrder.DESC);
        getClient().appData(CONTENT_COLLECTION, ContentItem.class).get(q, new KinveyListCallback<ContentItem>() {
            @Override
            public void onSuccess(ContentItem[] result) {
                if (getSherlockActivity() == null){
                    return;
                }
                loading.setVisibility(View.GONE);
                content = Arrays.asList(result);

                adapter = new ContentListAdapter(getSherlockActivity(), content,
                        (LayoutInflater) getSherlockActivity().getSystemService(
                                Activity.LAYOUT_INFLATER_SERVICE));

                contentList.setAdapter(adapter);



            }

            @Override
            public void onFailure(Throwable error) {
                loading.setVisibility(View.GONE);
                Util.Error(RecentFragment.this, error);
            }
        });
    }

    @Override
    public String getTitle(){
        return "Recent";
    }
}
