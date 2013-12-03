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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.kinvey.sample.contentviewr.core.ContentFragment;
import com.kinvey.sample.contentviewr.model.ContentItem;
import com.kinvey.sample.contentviewr.windows.InfographViewer;

import java.util.ArrayList;

/**
 * @author edwardf
 */
public class ContentList extends ContentFragment implements AdapterView.OnItemClickListener {

    private ListView contentList;
    private ContentListAdapter adapter;


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

        ArrayList<ContentItem> content = new ArrayList<ContentItem>();
        ContentItem ok = new ContentItem();
        ok.setName("BaaS EcoSystem Map");
        ok.setBlurb("The Backend as a Service Ecosystem Map Update: A Growing Market ");
        ok.setLocation("http://www.kinvey.com/blog/images/2013/01/kinvey_backend-as-a-service_mobileecosystem_jan-14-2013_2100px.png");
        content.add(ok);

        adapter = new ContentListAdapter(getSherlockActivity(), content,
                (LayoutInflater) getSherlockActivity().getSystemService(
                        Activity.LAYOUT_INFLATER_SERVICE));

        contentList.setAdapter(adapter);
        contentList.setOnItemClickListener(this);

    }

    @Override
    public String getTitle() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        replaceFragment(InfographViewer.newInstance(adapter.getItem(position)), true);
    }
}
