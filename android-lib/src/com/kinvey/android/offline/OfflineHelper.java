/*
 * Copyright (c) 2013 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.android.offline;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.kinvey.android.AsyncAppData;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages a set of {@link OfflineTable}s.
 *
 * This class is used by the OS to access sqllite, and delegates control to the appropriate OfflineTable.
 *
 *
 * @author edwardf
 * @since 2.0
 */
public class OfflineHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;


    private Object lock = new Object();
    private ConcurrentHashMap<String, OfflineTable> tableCache;

    /**
     *
     * @param context
     * @param collectionName
     */
    public OfflineHelper(Context context, String collectionName) {
        super(context, collectionName, null, DATABASE_VERSION);
        if(tableCache == null){
            tableCache = new ConcurrentHashMap<String, OfflineTable>();
        }
        if (!tableCache.containsKey(collectionName)){
            tableCache.put(collectionName, new OfflineTable());
        }

    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        //OfflineTable.onCreate(database);
        //TODO
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
//        OfflineTable.onUpgrade(database, oldVersion, newVersion);
        //TODO
    }
}
