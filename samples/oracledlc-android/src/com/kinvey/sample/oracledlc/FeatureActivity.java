/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.sample.oracledlc;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.List;

/**
 * @author edwardf
 * @since 2.0
 */
public abstract class FeatureActivity extends SherlockFragmentActivity{

    private ViewPager mPager;
    private TitlePageIndicator mIndicator;
    private List<UseCaseFragment> mFragments;
    private FeatureAdapter mAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature);
        bindViews();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume()  {
        super.onResume();
        populateViews();
    }

    private void bindViews(){
        mPager = (ViewPager) findViewById(R.id.feature_pager);
        mIndicator = (TitlePageIndicator) findViewById(R.id.feature_indicator);
    }

    private void populateViews(){
        mFragments = getFragments();
        mAdapter = new FeatureAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mIndicator.setViewPager(mPager);
        mIndicator.setFooterIndicatorStyle(TitlePageIndicator.IndicatorStyle.Triangle);
        mIndicator.setTextColor(R.color.ebony);
        mIndicator.setSelectedColor(R.color.kinvey_orange);
        mPager.setOffscreenPageLimit(0);
    }

    public OracleDLCApplication getApplicationContext(){
        return (OracleDLCApplication) super.getApplicationContext();
    }


    public abstract List<UseCaseFragment> getFragments();

    private class FeatureAdapter extends FragmentPagerAdapter   {

        public FeatureAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public String getPageTitle(int position){
           return mFragments.get(position).getTitle();
        }

    }
}
