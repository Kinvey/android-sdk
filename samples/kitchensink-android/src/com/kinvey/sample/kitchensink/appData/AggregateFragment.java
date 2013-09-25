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

import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.kinvey.android.AsyncAppData;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyAggregateCallback;
import com.kinvey.java.model.Aggregation;
import com.kinvey.sample.kitchensink.KitchenSink;
import com.kinvey.sample.kitchensink.R;
import com.kinvey.sample.kitchensink.UseCaseFragment;

import java.util.ArrayList;

/**
 * @author edwardf
 * @since 2.0
 */
public class AggregateFragment extends UseCaseFragment implements View.OnClickListener {

    private Button count;
    private Button sum;
    private Button min;
    private Button max;
    private Button average;

    private AsyncAppData<Aggregation.Result[]> appdata;


    @Override
    public int getViewID() {
        return R.layout.feature_appdata_aggregate;
    }

    @Override
    public void bindViews(View v) {

        appdata = getClient().appData(KitchenSink.collectionName, Aggregation.Result[].class);

        count = (Button) v.findViewById(R.id.appdata_agg_count);
        count.setOnClickListener(this);
        sum = (Button) v.findViewById(R.id.appdata_agg_sum);
        sum.setOnClickListener(this);
        min = (Button) v.findViewById(R.id.appdata_agg_min);
        min.setOnClickListener(this);
        max = (Button) v.findViewById(R.id.appdata_agg_max);
        max.setOnClickListener(this);
        average = (Button) v.findViewById(R.id.appdata_agg_average);
        average.setOnClickListener(this);
    }

    @Override
    public String getTitle() {
        return "Aggregates";
    }

    private void performCount(ArrayList<String> fields, Query q){
        appdata.count(fields, q, new KinveyAggregateCallback() {
            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(getSherlockActivity(), "something went wrong -> " + error.getMessage(), Toast.LENGTH_SHORT).show();            }

            @Override
            public void onSuccess(Aggregation res) {

                Toast.makeText(getSherlockActivity(), "got: " +res.results[0].get("_result"), Toast.LENGTH_SHORT ).show();

            }
        });
    }

    private void performSum(ArrayList<String> fields, Query q){
        appdata.sum(fields, "aggregateField",  q, new KinveyAggregateCallback() {
            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(getSherlockActivity(), "something went wrong -> " + error.getMessage(), Toast.LENGTH_SHORT).show();            }

            @Override
            public void onSuccess(Aggregation res) {

                Toast.makeText(getSherlockActivity(), "got: " +res.results[0].get("_result"), Toast.LENGTH_SHORT ).show();

            }
        });
    }

    private void performMin(ArrayList<String> fields, Query q){
        appdata.min(fields, "aggregateField",  q, new KinveyAggregateCallback() {
            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(getSherlockActivity(), "something went wrong -> " + error.getMessage(), Toast.LENGTH_SHORT).show();            }

            @Override
            public void onSuccess(Aggregation res) {

                Toast.makeText(getSherlockActivity(), "got: " +res.results[0].get("_result"), Toast.LENGTH_SHORT ).show();

            }
        });
    }

    private void performMax(ArrayList<String> fields, Query q){
        appdata.max(fields, "aggregateField",  q, new KinveyAggregateCallback() {
            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(getSherlockActivity(), "something went wrong -> " + error.getMessage(), Toast.LENGTH_SHORT).show();            }

            @Override
            public void onSuccess(Aggregation res) {

                Toast.makeText(getSherlockActivity(), "got: " +res.results[0].get("_result"), Toast.LENGTH_SHORT ).show();

            }
        });
    }

    private void performAverage(ArrayList<String> fields, Query q){
        appdata.average(fields, "aggregateField",  q, new KinveyAggregateCallback() {
            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(getSherlockActivity(), "something went wrong -> " + error.getMessage(), Toast.LENGTH_SHORT).show();            }

            @Override
            public void onSuccess(Aggregation res) {

                Toast.makeText(getSherlockActivity(), "got: " +res.results[0].get("_result"), Toast.LENGTH_SHORT ).show();

            }
        });
    }



    @Override
    public void onClick(View view) {

        ArrayList <String> fields = new ArrayList<String>();
        fields.add("_acl.creator");
        Query q = new Query();

        if (view == count){
            performCount(fields, q);
        }else if (view == sum){
            performSum(fields, q);
        }else if (view == min){
            performMin(fields, q);
        }else if (view == max){
             performMax(fields, q);
        }else if (view == average){
            performAverage(fields, q);
        }
    }
}
