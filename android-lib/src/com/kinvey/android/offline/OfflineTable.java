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

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.kinvey.android.Client;

/**
 * This class represents a collection as a sqllite table, used for offline storage.
 *
 * Every row is an entity, with a sqllite column for the _id and one for the json body.
 *
 * @author edwardf
 * @since 2.0
 */
public class OfflineTable {

    private final String TAG = Client.TAG + " " + this.getClass().getSimpleName();

    public String TABLE_NAME;
    //every row must have an _id
    public static final String COLUMN_ID = "_id";
    //every row must have a json body
    public static final String COLUMN_JSON = "_json";

    //TODO
    public static final String TABLE_TODO = "";
    public static final String COLUMN_CATEGORY = "";
    public static final String COLUMN_SUMMARY = "";
    public static final String COLUMN_DESCRIPTION = "";

    public void onCreate(SQLiteDatabase database) {
        if (this.TABLE_NAME == null || this.TABLE_NAME == ""){
            Log.e(TAG, "cannot create a table without a name!");
            return;
        }

        String createCommand = "create table "
                + TABLE_NAME
                + "("
                    + COLUMN_ID + " text not null, "
                    + COLUMN_JSON + " text not null"
                + ");";


        runCommand(database, createCommand);


    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (this.TABLE_NAME == null || this.TABLE_NAME == ""){
            Log.e(TAG, "cannot upgrade a table without a name!");
            return;
        }

        Log.w(TAG, String.format("Upgrading database from version %d to %d which will call drop table", oldVersion, newVersion));
        runCommand(database, "DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

    private static void runCommand(SQLiteDatabase database, String command){
        database.beginTransaction();
        try {
            database.execSQL(command);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }

    }
}


