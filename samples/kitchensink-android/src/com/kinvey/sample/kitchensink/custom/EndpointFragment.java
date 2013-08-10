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
package com.kinvey.sample.kitchensink.custom;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncCustomEndpoints;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.sample.kitchensink.R;
import com.kinvey.sample.kitchensink.UseCaseFragment;


/**
 * @author edwardf
 * @since 2.0
 */
public class EndpointFragment  extends UseCaseFragment implements View.OnClickListener {
    private Button hitIt;
    private TextView results;

    @Override
    public void onClick(View v) {
        if (v == hitIt){
            hitTheEndpoint();
        }

    }

    @Override
    public int getViewID() {
        return R.layout.feature_endpoint_basic;
    }

    @Override
    public void bindViews(View v) {
        hitIt = (Button) v.findViewById(R.id.endpoint_basic_hitit);
        results = (TextView) v.findViewById(R.id.endpoint_basic_result);

        hitIt.setOnClickListener(this);

    }

    @Override
    public String getTitle() {
        return "Endpoints";
    }

    private void hitTheEndpoint(){

        AsyncCustomEndpoints endpoints = getClient().customEndpoints();
        endpoints.callEndpoint("doit", new GenericJson(), new KinveyListCallback<GenericJson>() {
            @Override
            public void onSuccess(GenericJson[] result) {
                if (result == null){
                    results.setText("nope, got null back!");
                }else{
                    results.setText(result[0].toString()) ;

                }
            }
            @Override
            public void onFailure(Throwable error) {
                results.setText("Uh oh -> " + error);
            }
        });

    }
}
