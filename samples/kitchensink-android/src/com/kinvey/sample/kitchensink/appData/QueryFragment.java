/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.sample.kitchensink.appData;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.kinvey.java.Query;
import com.kinvey.sample.kitchensink.KitchenSink;
import com.kinvey.sample.kitchensink.MyEntity;
import com.kinvey.sample.kitchensink.R;
import com.kinvey.sample.kitchensink.UseCaseFragment;

/**
 * @author edwardf
 * @since 2.0
 */
public class QueryFragment extends UseCaseFragment implements View.OnClickListener{

    private Button current;
    private Button notCurrent;








    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getViewID() {
        return R.layout.feature_appdata_query;
    }

    @Override
    public void bindViews(View v) {
        current = (Button) v.findViewById(R.id.appdata_query_current);
        notCurrent = (Button) v.findViewById(R.id.appdata_query_not_current);

        current.setOnClickListener(this);
        notCurrent.setOnClickListener(this);

       // q.equals("_acl.creator", source.getAuthorID());
    }

    @Override
    public String getTitle() {
        return "Query";
    }

    @Override
    public void onClick(View v) {
        if (v == current){
            queryForCurrent();
        }else if (v == notCurrent){
            queryForNotCurrent();
        }
    }

    private void queryForCurrent(){}

    private void queryForNotCurrent(){}

    private void executeQueryAndUpdateView(Query q){

        getClient().appData(KitchenSink.collectionName, MyEntity.class).get(new Query(), null );



    }
}
