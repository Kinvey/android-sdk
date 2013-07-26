/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.sample.kitchensink;

import android.app.Application;

import com.kinvey.android.Client;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class KitchenSinkApplication extends Application {

    private Client myClient;

    // NOTE: When configuring push notifications you have to change the android package name of this app
    private String pushAppKey = "your_push_key";
    private String pushAppSecret = "your_push_secret";

    @Override
    public void onCreate() {
        super.onCreate();
        myClient = new Client.Builder(this.getApplicationContext()).build();

    }


    public void setsClient(Client myClient) {
        this.myClient = myClient;
    }

    public Client getClient(){
        return this.myClient;
    }
}
