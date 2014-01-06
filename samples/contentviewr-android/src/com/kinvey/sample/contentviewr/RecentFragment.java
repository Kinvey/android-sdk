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
import android.widget.AdapterView;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.offline.SqlLiteOfflineStore;
import com.kinvey.java.Query;
import com.kinvey.java.offline.OfflinePolicy;
import com.kinvey.java.query.AbstractQuery;
import com.kinvey.sample.contentviewr.model.ContentItem;
import com.kinvey.sample.contentviewr.model.ContentType;
import com.kinvey.sample.contentviewr.windows.Viewer;
import com.kinvey.sample.contentviewr.windows.WindowFactory;

import java.util.Arrays;

import static com.kinvey.sample.contentviewr.Contentviewr.CONTENT_COLLECTION;

/**
 * @author edwardf
 */
public class RecentFragment extends ContentListFragment {


    @Override
    public void refresh(){
        if (loading == null){
            return;
        }
        loading.setVisibility(View.VISIBLE);
        Query q = new Query().setLimit(10);
        q.addSort("_kmd.ect", AbstractQuery.SortOrder.DESC).equals("target", getContentViewr().getSelectedTarget());
        AsyncAppData<ContentItem> app = getClient().appData(CONTENT_COLLECTION, ContentItem.class);
        //app.setOffline(OfflinePolicy.LOCAL_FIRST, new SqlLiteOfflineStore(getSherlockActivity().getApplicationContext()));

        app.get(q, new KinveyListCallback<ContentItem>() {
            @Override
            public void onSuccess(ContentItem[] result) {
                if (getSherlockActivity() == null) {
                    return;
                }
                loading.setVisibility(View.GONE);
                content = Arrays.asList(result);

                adapter = new ContentListAdapter(getSherlockActivity(), content,
                        (LayoutInflater) getSherlockActivity().getSystemService(
                                Activity.LAYOUT_INFLATER_SERVICE));

                contentList.setAdapter(adapter);

                for (ContentItem c : content) {
                    c.loadThumbnail(getClient(), adapter);
                }

//                click(0);



            }

            @Override
            public void onFailure(Throwable error) {
                if (getSherlockActivity() == null) {
                    return;
                }
                loading.setVisibility(View.GONE);
                Util.Error(RecentFragment.this, error);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        click(position);

    }

    public void click(int position){
        ContentType type = null;

        for (ContentType t : getContentType().values()){
            if (t.getName().equals(adapter.getItem(position).getType())){
                type = t;
            }

        }

        ContentItem item = adapter.getItem(position);
        Viewer viewer = new WindowFactory().getViewer(item.getSource());
        viewer.loadContent(adapter.getItem(position));
        showWindow(viewer);

    }

    @Override
    public String getTitle(){
        return "Recent";
    }
}
