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

package com.kinvey.android.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView

import com.kinvey.android.Client
import com.kinvey.android.R
import com.kinvey.android.store.UserStore

/***
 * Provides a WebView for easy logging into MIC.
 */
class MICLoginActivity : Activity() {

    private var micView: WebView? = null
    private var clientId: String? = null

    public override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        setContentView(R.layout.activity_miclogin)

        val i = intent
        val loginURL = i.getStringExtra(KEY_LOGIN_URL)
        clientId = i.getStringExtra(KEY_CLIENT_ID)

        if (loginURL == null) {
            onNewIntent(this.intent)
            return
        }


        micView = findViewById(R.id.mic_loginview)
        loadLoginPage(loginURL)
    }

    private fun loadLoginPage(url: String) {

        micView!!.loadUrl(url)
    }


    public override fun onNewIntent(intent: Intent) {

        super.onNewIntent(intent)
        if (clientId != null && !clientId!!.isEmpty()) {
            UserStore.onOAuthCallbackReceived(intent, clientId, Client.sharedInstance)
        } else {
            UserStore.onOAuthCallbackReceived(intent, null, Client.sharedInstance)
        }
        this.finish()
    }

    companion object {

        val KEY_LOGIN_URL = "loginURL"
        val KEY_CLIENT_ID = "clientId"
    }
}
