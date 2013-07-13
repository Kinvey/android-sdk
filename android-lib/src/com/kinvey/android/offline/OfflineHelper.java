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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.kinvey.android.AsyncAppData;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.AppData;
import com.kinvey.java.offline.*;

import java.util.ArrayList;
import java.util.List;
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


//    private Object lock = new Object();
//    private ConcurrentHashMap<String, OfflineTable> tableCache;

    String collectionName;

    private static final String DB_NAME = "kinveyOffline.db";

    private static final String COLLECTION_TABLE = "collections";
    private static final String COLUMN_NAME = "name";

    /**
     * Used for creating new database tables, collection name is used for generating table name
     *
     * @param context
     * @param collectionName
     */
    public OfflineHelper(Context context, String collectionName) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        this.collectionName = collectionName;
    }

    /**
     * used for accessing ALREADY existing tables
     *
     * @param context
     */
    public OfflineHelper(Context context){
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
       createCollectionTable();
       OfflineTable table = new OfflineTable(collectionName);
       table.onCreate(database);

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        OfflineTable table = new OfflineTable(collectionName);
        table.onUpgrade(database, oldVersion, newVersion);
    }

    public OfflineTable getTable(String collectionName){
        return new OfflineTable(collectionName);
    }

    public void addCollection(String collectionName){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, collectionName);

        int change = db.updateWithOnConflict(COLLECTION_TABLE, values, null, null, db.CONFLICT_REPLACE);
        if (change == 0){
            db.insert(COLLECTION_TABLE, null, values);
        }
        db.close();
    }

    public List<String> getCollectionTables(){
        ArrayList<String> ret = new ArrayList<String>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(COLLECTION_TABLE, new String[]{COLUMN_NAME}, null, null, null, null, null);
        while (c.moveToNext()){
            ret.add(c.getString(0));
        }
        c.close();
        db.close();
        return ret;



    }

    public OfflineGenericJson getEntity(AbstractClient client, AppData appData, String id) {
        //ensure table exists, if not, create it   <- done by constructor of offlinehelper (oncreate will delegate)
        SQLiteDatabase db = getWritableDatabase();

        OfflineGenericJson ret;

        ret = getTable(appData.getCollectionName()).getEntity(this, client, id, appData.getCurrentClass());

        return ret;
    }




    public void createCollectionTable(){
        SQLiteDatabase db = getWritableDatabase();

        String createCommand = "CREATE TABLE IF NOT EXISTS "
                + COLLECTION_TABLE
                + "("
                + COLUMN_NAME + " TEXT not null "
                + ");";

        OfflineTable.runCommand(db, createCommand);

        db.close();
    }
}
