/** 
 * Copyright (c) 2013 Kinvey Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.kinvey.sample.kitchensink.appData;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.kinvey.android.callback.KinveyListCallback;
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

    private void queryForCurrent(){
        Query q = new Query();
        q.equals("_acl.creator", getClient().user().getId());
        executeQueryAndUpdateView(q);

    }

    private void queryForNotCurrent(){
        Query q = new Query();
        q.notEqual("_acl.creator", getClient().user().getId());
        executeQueryAndUpdateView(q);

    }

    private void executeQueryAndUpdateView(Query q){

        getClient().appData(KitchenSink.collectionName, MyEntity.class).get(q, new KinveyListCallback<MyEntity>() {
            @Override
            public void onSuccess(MyEntity[] result) {
                Toast.makeText(getSherlockActivity(), "got " + result.length + " results!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(getSherlockActivity(), "something went wrong -> " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }
}
