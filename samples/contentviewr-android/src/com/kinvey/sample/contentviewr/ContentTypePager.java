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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.kinvey.android.Client;
import com.kinvey.sample.contentviewr.core.ContentFragment;
import com.kinvey.sample.contentviewr.model.ContentType;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author edwardf
 */
public class ContentTypePager extends ContentFragment {

    private ViewPager pager;
    private ContentTypeAdapter adapter;
    private TitlePageIndicator mIndicator;

    private static final int STATIC = 2;//reorder, recent



    @Override
    public void onCreate(Bundle saved){
        super.onCreate(saved);
        setHasOptionsMenu(true);
        getSherlockActivity().invalidateOptionsMenu();
    }


    @Override
    public int getViewID() {
        return R.layout.fragment_content_type_pager;
    }

    @Override
    public void bindViews(View v) {
        pager = (ViewPager) v.findViewById(R.id.content_type_pager);
        mIndicator = (TitlePageIndicator) v.findViewById(R.id.feature_indicator);



        setAdapter();
        pager.setCurrentItem(1);

    }

    private void setAdapter(){
        adapter = new ContentTypeAdapter(getChildFragmentManager());


        pager.setAdapter(adapter);
        mIndicator.setViewPager(pager);
        mIndicator.setFooterIndicatorStyle(TitlePageIndicator.IndicatorStyle.Triangle);
        mIndicator.setTextColor(R.color.ebony);
        mIndicator.setSelectedColor(R.color.ghost_white);
    }

    @Override
    public String getTitle() {
        return "Content Pager";
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        if (pager.getCurrentItem() != 0){
            inflater.inflate(R.menu.menu_list, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                for (int i = 1; i < adapter.getCount(); i++){
                    ((ContentListFragment)adapter.getItem(i)).refresh();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refresh(){

    }

    public class ContentTypeAdapter extends FragmentPagerAdapter {

        private HashMap<Long, ContentFragment> mItems = new HashMap<Long, ContentFragment>();

        public ContentTypeAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return getContentType().keySet().size() + STATIC;
        }

        @Override
        public Fragment getItem(int position) {


            //if the fragment is cached, load it
            long id = getItemId(position - STATIC);

            if(mItems.get(id) != null) {
                return mItems.get(id);
            }
            //if haven't returned, have to create a new fragment
            Log.i(Contentviewr.TAG, "creating new fragment");

            if (position == 0){
                return new ReorderFragment(ContentTypePager.this);
            }

            if (position == 1){
                return new RecentFragment();
            }

            List<String> order = (List<String>) getClient().user().get("ordering");
            ContentFragment f = ContentListFragment.newInstance(getContentType().get(order.get(position - STATIC)));

            mItems.put(id, f);

            return f;
        }

        @Override
        public long getItemId(int position) {
            if (position == 0){
                return -1;
            }else if (position == 1){
                return -2;
            }

            List<String> order = (List<String>) getClient().user().get("ordering");
            return getContentType().get(order.get(position - STATIC)).getUniqueID();

        }

        @Override
        public int getItemPosition(Object object) {
            Fragment f = (Fragment) object;

            for(int i = 0; i < getCount(); i++) {

                Fragment item = (Fragment) getItem(i);
                if(item.equals(f)) {
                    Log.i(Contentviewr.TAG, "get item position it's in pager");
                    return i;
                }
            }
            for(Map.Entry<Long, ContentFragment> entry : mItems.entrySet()) {
                if(entry.getValue().equals(f)) {
                    Log.i(Contentviewr.TAG, "get item position removed from pager");

                    mItems.remove(entry.getKey());
                    break;
                }
            }


            return POSITION_NONE;
        }
        @Override
        public String getPageTitle(int position){

            if (position == 0){
                return "Reorder";
            }else if (position == 1){
                return "Recent";
            }

            List<String> order = (List<String>) getClient().user().get("ordering");

            if (order == null || getContentType() == null || getContentType().get(order.get(position - STATIC)) == null){
                return "";
                //This only happens if the app is being closed, so it won't be rendered anyways
            }

            return getContentType().get(order.get(position - STATIC)).getDisplayName();
        }


    }
}
