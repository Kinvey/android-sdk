/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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
import com.kinvey.java.offline.OfflineStore;

/**
 * This class is an implementation of an {@link AbstractSqliteOfflineStore}, which provides a native android sqlite3 database
 * <p/>
 * This class delegates requests to an appropriate {@link OfflineTable}, which is associated with the current collection.
 * <p/>
 * It also enqueues requests in that same {@link OfflineTable}, and can start an Android Service to begin background sync.
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
    protected DatabaseHandler getDatabaseHandler() {
        return OfflineHelper.getInstance(getContext());
    }


}
