/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.samples.statusshare.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.kinvey.android.Client;
import com.kinvey.samples.statusshare.StatusShareApplication;


/**
 *
 *   This abstract class provides hooks for some fragment boilerplate.
 *
 *   It also offers access to the current instance of the Kinvey Client as well as the Roboto font
 *
 * @author edwardf
 * @since 2.0
 */
public abstract class KinveyFragment extends SherlockFragment{

    private Typeface roboto;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        roboto = Typeface.createFromAsset(getSherlockActivity().getAssets(), "Roboto-Thin.ttf");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
        View v = inflater.inflate(getViewID(), group, false);
        bindViews(v);
        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        populateViews();
    }

    /**
     *
     * @return an instance of a Kinvey Client
     */
    public Client getClient(){
        return ((StatusShareApplication) getSherlockActivity().getApplicationContext()).getClient();
    }

    /**
     * If you are adding a new fragment, add a new layout.xml file and reference it here.
     * @return the ID defined as R.layout.* for this specific use case fragment.
     */
    public abstract int getViewID();


    /**
     * In this method establish all references to View widgets within the layout.
     *
     * For example:
     *
     * TextView mytext = (TextView) v.findViewById(R.id.mytextview);
     *
     * This is called once from onCreateView.
     *
     * @param v  the View object inflated by the Fragment, this will be the parent of any View within the fragment.
     */
    public abstract void bindViews(View v);

    /**
     * In this method populate the view objects.  This is called from onResume, to ensure that the data displayed is at least refreshed when the fragment is resumed.
     *
     * This method is optional.
     *
     * For example:
     *
     * mytext.setText("hello" + user.getName());
     */
    public void populateViews(){}


    public Typeface getRoboto() {
        return roboto;
    }
}


