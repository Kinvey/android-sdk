/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
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
package com.kinvey.android.offline;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 *
 * This Android Service listens for intents and uses the {@link com.kinvey.android.AsyncAppData} API to execute requests
 *
 * @author edwardf
 * @since 2.0
 */
public class KinveySyncService extends AbstractSyncService {

    //allows clients to bind
    private final IBinder mBinder = new KBinder();


    public KinveySyncService(String name) {
        super(name);
    }

    @Override
    protected DatabaseHandler getDatabaseHandler(String userid) {
        return OfflineHelper.getInstance(getApplicationContext());
    }

    public KinveySyncService() {
        super("Kinvey Sync Service");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void pingService() {
        Log.i(TAG, "\"Hi!\" said the Kinvey Sync Service");
    }

    /**
     * Binder coupled with this Service
     *
     */
    public class KBinder extends Binder {
        public KinveySyncService getService(){
            return KinveySyncService.this;
        }
    }

}
