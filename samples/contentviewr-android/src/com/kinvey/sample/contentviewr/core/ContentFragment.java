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
package com.kinvey.sample.contentviewr.core;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.actionbarsherlock.app.SherlockFragment;
import com.kinvey.android.Client;
import com.kinvey.sample.contentviewr.Contentviewr;
import com.kinvey.sample.contentviewr.R;
import com.kinvey.sample.contentviewr.model.ContentType;
import com.kinvey.sample.contentviewr.model.Target;
import com.kinvey.sample.contentviewr.windows.InfographViewer;

import java.util.List;

/**
 * @author edwardf
 */
public abstract class ContentFragment extends SherlockFragment {



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
        View v = inflater.inflate(getViewID(), group, false);
        bindViews(v);
        return v;
    }





    /**
     * @return the ID defined as  R.layout.* for this specific fragment.  If you are adding a new fragment, add a new layout.xml file and reference it here.
     */
    public abstract int getViewID();


    /**
     * In this method establish all references to View widgets within the layout.
     *
     * For example:
     *
     * TextView mytext = (TextView) v.findViewById(R.id.mytextview);
     *
     * This is called once from onCreate.
     *
     * @param v  the View object inflated by the Fragment, this will be the parent of any View Widget within the fragment.
     */
    public abstract void bindViews(View v);

    public abstract String getTitle();

    public void replaceFragment(ContentFragment newOne, boolean backstack){

        FragmentTransaction tr = getSherlockActivity().getSupportFragmentManager().beginTransaction();
        tr.replace(android.R.id.content, newOne, "content");
        if (backstack){
            tr.addToBackStack("back");
        }
        tr.commit();

    }

    public Contentviewr getContentViewr(){
        return (Contentviewr) getSherlockActivity();
    }

    public List<Target> getTargets(){
        return getContentViewr().getTargets();
    }

    public List<ContentType> getContentType(){
        return getContentViewr().getContentTypes();
    }

    public Client getClient(){
        return getContentViewr().getClient();
    }

    public String getSelectedTarget(){
        return getContentViewr().getSelectedTarget();
    }

}
