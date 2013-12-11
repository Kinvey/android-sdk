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

import static com.kinvey.sample.contentviewr.Contentviewr.CONTENT_COLLECTION;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.offline.SqlLiteOfflineStore;
import com.kinvey.java.Query;
import com.kinvey.java.offline.OfflinePolicy;
import com.kinvey.sample.contentviewr.core.ContentFragment;
import com.kinvey.sample.contentviewr.model.ContentItem;
import com.kinvey.sample.contentviewr.model.ContentType;
import com.kinvey.sample.contentviewr.windows.ImageViewer;
import com.kinvey.sample.contentviewr.windows.Viewer;
import com.kinvey.sample.contentviewr.windows.WindowFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author edwardf
 */
public class ContentListFragment extends ContentFragment implements AdapterView.OnItemClickListener {

    protected ListView contentList;
    protected ContentListAdapter adapter;
    protected ContentType type;
    protected LinearLayout loading;

    protected List<ContentItem> content;

    public static ContentListFragment newInstance(ContentType type){
        ContentListFragment ret = new ContentListFragment();
        ret.setType(type);
        return ret;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getSherlockActivity().invalidateOptionsMenu();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
        View v = inflater.inflate(R.layout.fragment_content_list, group, false);
        bindViews(v);
        return v;
    }

    @Override
    public int getViewID() {
        return R.layout.fragment_content_list;
    }


    public void bindViews(View v){
        contentList = (ListView) v.findViewById(R.id.content_list);
        loading = (LinearLayout) v.findViewById(R.id.content_loadingbox);

        refresh();
        contentList.setOnItemClickListener(this);

    }


    public void refresh(){
        Log.i(Client.TAG, "refresh on: " + type.getDisplayName());
        if (loading == null){
            return;
        }
        loading.setVisibility(View.VISIBLE);
        Query q = new Query().equals("type", type.getName()).equals("target", getContentViewr().getSelectedTarget());
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

                //Lazy load images
                for (ContentItem c : content) {
                    c.loadThumbnail(getClient(), adapter);
                }
            }

            @Override
            public void onFailure(Throwable error) {
                if (getSherlockActivity() == null) {
                    return;
                }
                loading.setVisibility(View.GONE);
                Util.Error(ContentListFragment.this, error);
            }
        });
    }

    public String getTitle(){
        return type.getDisplayName();
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ContentItem item = adapter.getItem(position);
        Viewer viewer = new WindowFactory().getViewer(item.getSource());
        viewer.loadContent(adapter.getItem(position));
        replaceFragment(viewer, true);

    }

    public ContentType getType() {
        return type;
    }

    public void setType(ContentType type) {
        this.type = type;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
