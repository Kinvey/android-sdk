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
    //private ContentTypeAdapter adapter;
    private TitlePageIndicator indicator;

    private static final int STATIC = 2;//reorder, recent



    @Override
    public void onResume(){
        super.onResume();
        Log.i(Contentviewr.TAG, "pager got onresume");
        //setAdapter();
        //pager.setCurrentItem(1);
        pager.getAdapter().notifyDataSetChanged();
        //pager.getAdapter()./
    }

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
        Log.i(Contentviewr.TAG, "pager got bindviews");
        pager = (ViewPager) v.findViewById(R.id.content_type_pager);
        indicator = (TitlePageIndicator) v.findViewById(R.id.feature_indicator);



        setAdapter();
        pager.setCurrentItem(1);

    }

    private void setAdapter(){

//        if (adapter != null){
//            return;
//        }
     //   adapter = new ContentTypeAdapter(getChildFragmentManager());

        if (pager.getAdapter() == null){
            pager.setAdapter(new ContentTypeAdapter(getChildFragmentManager()));
        }
        pager.getAdapter().notifyDataSetChanged();;
        pager.setOffscreenPageLimit(5);
        indicator.setViewPager(pager);
        indicator.setFooterIndicatorStyle(TitlePageIndicator.IndicatorStyle.Triangle);
        indicator.setTextColor(R.color.ebony);
        indicator.setSelectedColor(R.color.ghost_white);
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
               ((ContentListFragment) ((ContentTypeAdapter)pager.getAdapter()).getItem(pager.getCurrentItem())).refresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refresh(){


        int index = pager.getCurrentItem();
        if (index - 1 > 0){
            ((ContentListFragment) ((ContentTypeAdapter)pager.getAdapter()).getItem(index - 1)).refresh();
        }
        if (index + 1 < pager.getAdapter().getCount() + STATIC){
            ((ContentListFragment) ((ContentTypeAdapter)pager.getAdapter()).getItem(index + 1)).refresh();
        }

        ((ContentListFragment) ((ContentTypeAdapter)pager.getAdapter()).getItem(index)).refresh();


    }

    public class ContentTypeAdapter extends FragmentPagerAdapter {

        private HashMap<Long, ContentFragment> mItems;// = new HashMap<Long, ContentFragment>();

        public ContentTypeAdapter(FragmentManager fm) {
            super(fm);
            mItems = new HashMap<Long, ContentFragment>();
            Log.i(Contentviewr.TAG, "constructor, item size: " + mItems.keySet().size());
        }


        @Override
        public int getCount() {
            return getContentType().keySet().size() + STATIC;
        }

        @Override
        public Fragment getItem(int position) {


            //if the fragment is cached, load it
            long id = getItemId(position);//() - STATIC);

            if(mItems.get(id) != null) {
                Log.i(Contentviewr.TAG, "returning cached fragment");
                return mItems.get(id);
            }
            //if haven't returned, have to create a new fragment
            Log.i(Contentviewr.TAG, "creating new fragment");

            ContentFragment f;
            if (position == 0){
                return new ReorderFragment();
            }else if (position == 1){
                f = new RecentFragment();
            }else{
                List<String> order = (List<String>) getClient().user().get("ordering");
                f = ContentListFragment.newInstance(getContentType().get(order.get(position - STATIC)));
            }


            //ContentFragment f = ContentListFragment.newInstance(getContentType().get(order.get(position - STATIC)));

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
            if (position >= ((List<String>) getClient().user().get("ordering")).size() + STATIC){
                return 0;
            }
            Log.i(Client.TAG, "content type pager, position is: " + position + " and ordered is: " + (position - STATIC));
            List<String> order = (List<String>) getClient().user().get("ordering");
            return getContentType().get(order.get(position - STATIC)).getUniqueID();

        }



        @Override
        public int getItemPosition(Object object) {
            Fragment f = (Fragment) object;

            for(int i = 0; i < getCount(); i++) {

                Fragment item =  getItem(i);
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

            Log.i(Contentviewr.TAG, "ok it's none");
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
