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
package com.kinvey.android;

import android.content.Context;
import android.content.SharedPreferences;

import com.kinvey.java.auth.ClientUser;

/**
 * @author mjsalinger
 * @since 2.0
 */
class AndroidUserStore implements ClientUser {
    private String userID;
    private static AndroidUserStore _instance;
    SharedPreferences userPreferences;
    Context appContext;

    private AndroidUserStore(Context context) {
        appContext = context.getApplicationContext();
        userPreferences = appContext.getSharedPreferences(
                appContext.getPackageName(), Context.MODE_PRIVATE);

        userID = userPreferences.getString("userID","");
    }

    private void persistData() {
        SharedPreferences.Editor editor = userPreferences.edit();
        editor.putString("userID",userID);
        editor.commit();
    }

    static AndroidUserStore getUserStore(Context context) {
        if (_instance == null) {
            _instance = new AndroidUserStore(context);
        }
        return _instance;
    }

    /** {@inheritDoc} */
    @Override
    public void setUser(String userID) {
        this.userID = userID;
        persistData();
    }

    /** {@inheritDoc} */
    @Override
    public String getUser() {
        return userID;
    }

    @Override
    public void clear() {
        userID = null;
    }

}
