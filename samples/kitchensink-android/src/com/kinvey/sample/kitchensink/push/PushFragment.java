/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
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
        pushStatus.setText(getPushStatus());

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
                pushStatus.setText(getPushStatus());
            }
        }.start();


    }

    public String getPushStatus() {

        getApplicationContext().getClient().push().initialize(getActivity().getApplication());
        return Boolean.toString(getApplicationContext().getClient().push().isPushEnabled());

    }

    @Override
    public String getTitle() {
        return "Push!";
    }
}
