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

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.sample.kitchensink.KitchenSink;
import com.kinvey.sample.kitchensink.MyEntity;
import com.kinvey.sample.kitchensink.R;
import com.kinvey.sample.kitchensink.UseCaseFragment;

import java.util.ArrayList;

/**
 * @author edwardf
 * @since 2.0
 */
public class AggregateFragment extends UseCaseFragment implements View.OnClickListener {

    private Button tryIt;

    @Override
    public int getViewID() {
        return R.layout.feature_appdata_aggregate;
    }

    @Override
    public void bindViews(View v) {
        tryIt = (Button) v.findViewById(R.id.appdata_agg_perform);
        tryIt.setOnClickListener(this);
    }

    @Override
    public String getTitle() {
        return "Aggregates";
    }

    private void performAggregation(){

        AsyncAppData<GenericJson[]> aggregate = getClient().appData(KitchenSink.collectionName, GenericJson[].class);

        ArrayList <String> fields = new ArrayList<String>();
        fields.add("_acl.creator");
        Query q = new Query();
        aggregate.count(fields,q, new KinveyListCallback<GenericJson>() {
            @Override
            public void onSuccess(GenericJson[] res) {
                Toast.makeText(getSherlockActivity(), "got: " + res[0].get("_result"), Toast.LENGTH_SHORT ).show();


            }

            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(getSherlockActivity(), "something went wrong -> " + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == tryIt){
            performAggregation();
        }
    }
}
