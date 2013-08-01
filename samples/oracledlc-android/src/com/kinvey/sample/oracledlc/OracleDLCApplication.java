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

import android.app.Application;

import com.kinvey.android.Client;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class OracleDLCApplication extends Application {

    private Client myClient;

//    // NOTE: When configuring push notifications you have to change the android package name of this app
//    private String pushAppKey = "";
//    private String pushAppSecret = "";

    @Override
    public void onCreate() {
        super.onCreate();
        myClient = new Client.Builder(this.getApplicationContext()).build();
    }

    public void registerPush() {
//        PushOptions options = myClient.push().getPushOptions(pushAppKey, pushAppSecret,
//                false);
//      //  myClient.push().setIntentReceiver(com.kinvey.sample.oracledlc.push.CustomPushReceiver.class);
//        myClient.push().initialize(options, this);

    }

    public void setsClient(Client myClient) {
        this.myClient = myClient;
    }

    public Client getClient(){
        return this.myClient;
    }
}
