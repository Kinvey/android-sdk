/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.samples.statusshare;


import android.app.Application;
import android.util.Log;
import com.google.api.client.http.HttpTransport;
import com.kinvey.android.Client;
import com.kinvey.java.cache.CachePolicy;
import com.kinvey.java.cache.InMemoryLRUCache;
import com.kinvey.samples.statusshare.model.UpdateEntity;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author edwardf
 * @since 2.0
 */
public class StatusShareApplication extends Application{

    private static final Level LOGGING_LEVEL = Level.FINEST;


    private Client client = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // run the following comamnd to turn on verbose logging:
        //
        // adb shell setprop log.tag.HttpTransport DEBUG
        //
        Logger.getLogger(HttpTransport.class.getName()).setLevel(LOGGING_LEVEL);



    }



    public Client getClient() {
        if (client == null){
            client = new Client.Builder(getApplicationContext()).build();
        }
        return client;
    }


}
