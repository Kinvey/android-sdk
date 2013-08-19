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
package com.kinvey.sample.kitchensink.push;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.kinvey.sample.kitchensink.R;
import com.kinvey.sample.kitchensink.UseCaseFragment;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class PushFragment extends UseCaseFragment implements View.OnClickListener {

    Button bRegister;
    TextView pushStatus;

    @Override
    public int getViewID() {
        return R.layout.feature_push;
    }

    @Override
    public void bindViews(View v) {
        bRegister = (Button) v.findViewById(R.id.push_register_button);
        bRegister.setOnClickListener(this);
        pushStatus = (TextView) v.findViewById(R.id.push_status);
        pushStatus.setText(Boolean.toString(getApplicationContext().getClient().push().isPushEnabled()));
    }

    @Override
    public void onClick(View v) {
        if (v == bRegister) {
            registerPush();
        }
    }

    public void registerPush() {

        getApplicationContext().getClient().push().initialize(getApplicationContext());


        //Not going to hook up intents for this sample, so just wait for five seconds before redrawing
        new CountDownTimer(5000,1000){

            @Override
            public void onTick(long miliseconds){}

            @Override
            public void onFinish(){
                //after 5 seconds update the status
                if (getApplicationContext() != null)
                pushStatus.setText(Boolean.toString(getApplicationContext().getClient().push().isPushEnabled()));
            }
        }.start();


    }



    @Override
    public String getTitle() {
        return "Push!";
    }
}
