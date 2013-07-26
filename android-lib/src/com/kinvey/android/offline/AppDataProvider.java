/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.android.offline;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.kinvey.android.Client;
import android.provider.Settings.System;


/**
 *
 * @author edwardf
 * @since 2.0
 */
public class AppDataProvider extends ContentProvider {

    static final String AUTHORITY = "content://com.kinvey.android.content";

    private static final String TAG = Client.TAG + " - AppDataProvider";

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");
        return true;
    }

    @Override
    public String getType(Uri uri) {
        String ret = getContext().getContentResolver().getType(System.CONTENT_URI);
        Log.d(TAG, "getType returning: " + ret);
        return ret;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert uri: " + uri.toString());

        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d(TAG, "update uri: " + uri.toString());
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete uri: " + uri.toString());
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query with uri: " + uri.toString());
        return null;
    }
}
