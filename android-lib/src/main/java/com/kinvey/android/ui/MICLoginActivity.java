/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 *
 */

package com.kinvey.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import com.kinvey.android.Client;
import com.kinvey.android.R;
import com.kinvey.android.store.AsyncUserStore;
import com.kinvey.java.dto.User;

/***
 * Provides a WebView for easy logging into MIC.
 */
public class MICLoginActivity extends Activity {

    public static final String KEY_LOGIN_URL = "loginURL";

    private WebView micView;

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_miclogin);

        Intent i = getIntent();
        String loginURL = i.getStringExtra(KEY_LOGIN_URL);
        
        if (loginURL == null){
        	onNewIntent(this.getIntent());
        	return;
        }
        
        
        micView = (WebView) findViewById(R.id.mic_loginview);
        loadLoginPage(loginURL);
    }

    private void loadLoginPage(String url){
    	
        micView.loadUrl(url);
    }


    @Override
    public void onNewIntent(Intent intent){

        super.onNewIntent(intent);
        //TODO User.class ?
        AsyncUserStore.onOAuthCallbackRecieved(intent, Client.sharedInstance(), User.class);
        this.finish();
    }
}
