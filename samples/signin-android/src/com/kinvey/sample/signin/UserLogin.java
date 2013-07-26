/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.sample.signin;

import com.kinvey.android.Client;

import android.app.Application;

/**
 * Global application class.  Instantiates the KCS Client and sets global constants.
 *
 */
public class UserLogin extends Application {
    private Client service;

    // Application Constants
    public static final String AUTHTOKEN_TYPE = "com.kinvey.myapplogin";
    public static final String ACCOUNT_TYPE = "com.kinvey.myapplogin";
    public static final String LOGIN_TYPE_KEY = "loginType";
    
    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }
   

    private void initialize() {
		// Enter your app credentials here
		service = new Client.Builder(this).build();
    }

    public Client getKinveyService() {
        return service;
    }
}
