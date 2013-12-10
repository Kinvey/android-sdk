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
import android.view.View;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.kinvey.sample.contentviewr.core.ContentFragment;
import com.kinvey.sample.contentviewr.model.ContentType;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author edwardf
 */
public class ContentTypePager extends ContentFragment {

    private ViewPager pager;
    private ContentTypeAdapter adapter;
    private List<ContentFragment> fragments;
    private TitlePageIndicator mIndicator;


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



        adapter = new ContentTypeAdapter(getChildFragmentManager());

        fragments = new ArrayList<ContentFragment>();
        fragments.add(new ReorderFragment(this));
        fragments.add(new RecentFragment());
        loadOrderInAdapter();


        pager.setAdapter(adapter);
        mIndicator.setViewPager(pager);
        mIndicator.setFooterIndicatorStyle(TitlePageIndicator.IndicatorStyle.Triangle);
        mIndicator.setTextColor(R.color.ebony);
        mIndicator.setSelectedColor(R.color.ghost_white);
        pager.setCurrentItem(1);
        pager.setOffscreenPageLimit(3);
    }

    @Override
    public String getTitle() {
        return "Content Pager";
    }

    public void loadOrderInAdapter(){

        fragments = fragments.subList(0,2);

        List<String> order = (List<String>) getClient().user().get("ordering");
        for (String s : order){
        //for (ContentType c : getContentType()){
            fragments.add(ContentListFragment.newInstance(getContentType().get(s)));
        }
        adapter.notifyDataSetChanged();
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
                if (pager.getCurrentItem() != 0){
                    ((ContentFragment)adapter.getItem(pager.getCurrentItem())).refresh();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class ContentTypeAdapter extends FragmentPagerAdapter {

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
