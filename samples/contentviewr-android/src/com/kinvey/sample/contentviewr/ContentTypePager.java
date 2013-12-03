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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.kinvey.sample.contentviewr.core.ContentFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author edwardf
 */
public class ContentTypePager extends ContentFragment {

    private ViewPager pager;
    private ContentTypeAdapter adapter;
    private List<ContentFragment> fragments;

    @Override
    public int getViewID() {
        return R.layout.fragment_content_type_pager;
    }

    @Override
    public void bindViews(View v) {
        pager = (ViewPager) v.findViewById(R.id.content_type_pager);


        fragments = new ArrayList<ContentFragment>();

        adapter = new ContentTypeAdapter(getSherlockActivity().getSupportFragmentManager());
        pager.setAdapter(adapter);
    }

    @Override
    public String getTitle() {
        return "Content Type Pager";
    }


    private class ContentTypeAdapter extends FragmentPagerAdapter {

        public ContentTypeAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public String getPageTitle(int position){
            return fragments.get(position).getTitle();
        }

    }
}
