/*
 * Copyright (c) 2014, Kinvey, Inc.
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
import android.graphics.Point;
import android.util.Log;
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
import java.util.LinkedList;

import static com.kinvey.sample.contentviewr.Contentviewr.CONTENT_COLLECTION;

/**
 * @author edwardf
 */
public class RecentFragment extends ContentListFragment {


    @Override
    public void refresh(){
        if (loading == null || getContentViewr() == null || getClient() == null){
            return;
        }
        loading.setVisibility(View.VISIBLE);
        Query q = new Query()
                .equals("target", getContentViewr().getSelectedTarget())
                .equals("groups", getClient().user().get("group"));

                //.setLimit(10);


        AsyncAppData<ContentItem> app = getClient().appData(CONTENT_COLLECTION, ContentItem.class);
        app.setOffline(OfflinePolicy.LOCAL_FIRST, new SqlLiteOfflineStore(getSherlockActivity().getApplicationContext()));

        app.get(q, new KinveyListCallback<ContentItem>() {
            @Override
            public void onSuccess(ContentItem[] result) {
                if (getSherlockActivity() == null || getClient() == null) {
                    return;
                }
                loading.setVisibility(View.GONE);
                content = new LinkedList<ContentItem>();
                for (int i = result.length - 1; i >= 0; i-- ){
                    content.add(result[i]);

                }
                adapter = new ContentListAdapter(getSherlockActivity(), content,
                        (LayoutInflater) getSherlockActivity().getSystemService(
                                Activity.LAYOUT_INFLATER_SERVICE));

                contentList.setAdapter(adapter);

                for (ContentItem c : content) {
                    if (c != null && getClient() != null){
                        c.loadThumbnail(getClient(), adapter);
                    }
                }


                //TODO this won't work if first content item is PDF-- it will just pop the pdf viewing intent
//                if (content.size() >= 1 && content.get(0) != null){
//                    Point size = new Point(0,0);
//                    if (getSherlockActivity() != null){
//                        getSherlockActivity().getWindowManager().getDefaultDisplay().getSize(size);
//                        if (size.x > size.y){
//                            //it's landscape
//                            click(0);
//                        }
//
//
//                    }
//
//
//                }
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

        if (getSherlockActivity() == null){
            return;
        }
        ContentType type = null;

        for (ContentType t : getContentType().values()){
            if (t.getName().equals(adapter.getItem(position).getType())){
                type = t;
            }

        }

        if (type == null || adapter == null){
            return;
        }

        ContentItem item = adapter.getItem(position);
        Viewer viewer = new WindowFactory().getViewer(type.getWindowstyle());
        viewer.loadContent(adapter.getItem(position));
        showWindow(viewer);

    }

    @Override
    public String getTitle(){
        return "Recent";
    }
}
