/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.samples.citywatch;

import android.app.Application;
import com.google.api.client.http.HttpTransport;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.kinvey.android.Client;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class CityWatchApplication extends Application {

    public static final String TAG = "Kinvey - CityWatch";
    private static final Level LOGGING_LEVEL = Level.FINEST;

    private Client kinveyClient;

    @Override
    public void onCreate() {
        super.onCreate();

        // run the following comamnd to turn on verbose logging:
        //
        // adb shell setprop log.tag.HttpTransport DEBUG
        //
        Logger.getLogger(HttpTransport.class.getName()).setLevel(LOGGING_LEVEL);

        kinveyClient = new Client.Builder(this).build();
    }

    public Client getClient() {
        return kinveyClient;
    }
}
