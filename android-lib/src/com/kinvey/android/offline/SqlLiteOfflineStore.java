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

import android.content.Context;
import android.content.Intent;
import com.kinvey.java.offline.OfflineStore;

/**
 * This class is an implementation of an {@link AbstractSqliteOfflineStore}, which provides a native android sqlite3 database
 *
 *
 * @author edwardf
 */
public class SqlLiteOfflineStore<T> extends AbstractSqliteOfflineStore{

    private static final String TAG = "Kinvey - SQLLite Offline Store";

    public SqlLiteOfflineStore(Context context){
        super(context);
    }

    @Override
    protected DatabaseHandler getDatabaseHandler(String userid) {
        return OfflineHelper.getInstance(getContext());
    }

    @Override
    public void kickOffSync(){
        Intent syncIt = new Intent(this.getContext(), KinveySyncService.class);
        syncIt.setAction(AbstractSyncService.ACTION_OFFLINE_SYNC);
        this.getContext().startService(syncIt);

    }
}
