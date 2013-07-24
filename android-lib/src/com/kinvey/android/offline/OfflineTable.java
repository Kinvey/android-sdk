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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import com.google.api.client.json.JsonGenerator;
import com.kinvey.android.AndroidCredentialStoreException;
import com.kinvey.android.Client;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.core.AbstractKinveyJsonClient;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.offline.AbstractKinveyOfflineClientRequest;
import com.kinvey.java.offline.OfflineGenericJson;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a collection as a sqllite table, used for offline storage.
 *
 * Every row is an entity, with a sqllite column for the _id and one for the json body.
 *
 * @author edwardf
 * @since 2.0
 */
public class OfflineTable<T extends OfflineGenericJson> {

    private final String TAG = Client.TAG + " " + this.getClass().getSimpleName();

    //every row must have an _id
    public static final String COLUMN_ID = "_id";
    //every row must have a json body
    public static final String COLUMN_JSON = "_json";
    //every row must have a user who made the initial request
    public static final String COLUMN_USER = "_user";
    //for lazy-delete support, add a deleted flag (0 false, 1 true)
    public static final String COLUMN_DELETED = "_deleted";
    //for the unique index on the collection store
    public static final String UNIQUE_INDEX_IDS = "SOME_INDEX";
    //for the unique index on the request queue
    public static final String UNIQUE_INDEX_QUEUE = "ANOTHER_INDEX";
    //for the queued action
    public static final String COLUMN_ACTION = "_action";
    //for query strings
    public static final String COLUMN_QUERY_STRING = "_queryString";
    //for results of request,(0 false, 1 true)
    public static final String COLUMN_RESULT = "_result";
    //unique key for queued requests, using Mongo db's algorithm
    public static final String COLUMN_UNIQUE_KEY = "_key";

    //Each collection has multiple tables, these are the prefixes of the table names
    public static final String PREFIX_OFFLINE = "offline_";
    public static final String PREFIX_QUEUE = "queue_";
    public static final String PREFIX_QUERY = "query_";
    public static final String PREFIX_RESULTS = "results_";

    public String TABLE_NAME;
    public String QUERY_NAME;
    public String QUEUE_NAME;
    public String RESULTS_NAME;


    public OfflineTable(String collection){
        this.TABLE_NAME = PREFIX_OFFLINE + collection;
        this.QUEUE_NAME = PREFIX_QUEUE + collection;
        this.QUERY_NAME = PREFIX_QUERY + collection;
        this.RESULTS_NAME = PREFIX_RESULTS + collection;
    }

    public void onCreate(SQLiteDatabase database) {
        if (this.TABLE_NAME == null || this.TABLE_NAME == ""){
            Log.e(TAG, "cannot create a table without a name!");
            return;
        }

        //Create the local offline entity store
        String createCommand = "CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME
                + "("
                + COLUMN_ID + " TEXT not null, "
                + COLUMN_JSON + " TEXT not null, "
                + COLUMN_USER + " TEXT not null, "
                + COLUMN_DELETED + " INTEGER not null"     //TODO might not need this! (delete request is queued, entity doesn't care)
                + ");";


        runCommand(database, createCommand);
        //create the local action queue
        createCommand = "CREATE TABLE IF NOT EXISTS "
                + QUEUE_NAME
                + "("
                + COLUMN_UNIQUE_KEY + " TEXT not null, "
                + COLUMN_ID + " TEXT not null, "
                + COLUMN_ACTION + " TEXT not null"
                + COLUMN_DELETED + " INTEGER not null"     //TODO might not need this

                + ");";

        runCommand(database, createCommand);

        //create the local query store
        createCommand = "CREATE TABLE IF NOT EXISTS "
                + QUERY_NAME
                + "("
                + COLUMN_QUERY_STRING + " TEXT not null, "
                + COLUMN_ID + " TEXT not null"
                + ");";

        runCommand(database, createCommand);

        //create the local results store
        createCommand = "CREATE TABLE IF NOT EXISTS "
                + RESULTS_NAME
                + "("
                + COLUMN_ID + " TEXT not null, "
                + COLUMN_ACTION + " TEXT not null, "
                + COLUMN_JSON + " TEXT not null, "
                + COLUMN_RESULT + " INTEGER not null"
                + ");";

        runCommand(database, createCommand);

        //set a unique index on the local offline entity store
        String primaryKey = "CREATE UNIQUE INDEX " + UNIQUE_INDEX_IDS + " ON " + TABLE_NAME + " (" + COLUMN_ID + " ASC);";
        runCommand(database, primaryKey);
        //set a unique key on the queued request store
        primaryKey = "CREATE UNIQUE INDEX " + UNIQUE_INDEX_QUEUE + " ON " + QUEUE_NAME + " (" + COLUMN_UNIQUE_KEY + " ASC);";
        runCommand(database, primaryKey);


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

    public static void runCommand(SQLiteDatabase database, String command){
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

    public T insertEntity(OfflineHelper helper, AbstractClient client, OfflineGenericJson offlineEntity){

        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();


        values.put(COLUMN_ID, offlineEntity.get("_id").toString());


        String jsonResult = "";
        StringWriter writer = new StringWriter();
        try {
            JsonGenerator generator = client.getJsonFactory().createJsonGenerator(writer);
            generator.serialize(offlineEntity);
            generator.flush();
            jsonResult = writer.toString();
        } catch (Exception ex) {
            Log.e(TAG, "unable to serialize JSON! -> " + ex);
        }

        values.put(COLUMN_JSON, jsonResult);
        values.put(COLUMN_DELETED, 0);
        values.put(COLUMN_USER, client.user().getId());

        int change = db.updateWithOnConflict(TABLE_NAME, values, COLUMN_ID + "='" + offlineEntity.get("_id").toString()+"'", null, db.CONFLICT_REPLACE);
        if (change == 0){
            db.insert(TABLE_NAME, null, values);
            Log.v(TAG, "inserting new entity -> " + values.get(COLUMN_ID));
        }else{
            Log.v(TAG, "updating entity -> " + values.get(COLUMN_ID));
        }




        db.close();
        return (T) offlineEntity;
    }

    public T getEntity(OfflineHelper helper, AbstractClient client, String id, Class<T> responseClass){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[] {COLUMN_JSON}, COLUMN_ID + "=?",
                new String[] { id }, null, null, null, null);



        T ret = null;
        if (cursor != null){
            cursor.moveToFirst();
            try{
               ret =  client.getJsonFactory().fromString(cursor.getString(0), responseClass);
            }catch(Exception e){
                Log.e(TAG, "cannot parse json into object! -> " + e);
            }
            cursor.close();
        }
        db.close();
        return ret;
    }



    public T[] getQuery(OfflineHelper helper, AbstractClient client, String q, Class clazz){

        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor c =  db.query(QUERY_NAME, new String[]{COLUMN_ID}, COLUMN_QUERY_STRING + "=?", new String[]{q}, null, null, null);

        if (c.moveToFirst() && c.getColumnCount() > 0) {
            String[] resultIDs = c.getString(0).split(",");

            OfflineGenericJson[] ret = new OfflineGenericJson[resultIDs.length];

            for (int i = 0; i < resultIDs.length; i++) {
                ret[i] = getEntity(helper, client, resultIDs[i], clazz);
            }

            return (T[]) ret;
        }
        return null;

    }

    public void storeQueryResults(OfflineHelper helper, String queryString, List<String> resultIds){

        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_QUERY_STRING, queryString);

        String commaDelimitedIds = TextUtils.join(",", resultIds);

        values.put(COLUMN_ID, commaDelimitedIds);

        Log.v(TAG, "inserting query ");

        int change = db.updateWithOnConflict(QUERY_NAME, values, null, null, db.CONFLICT_REPLACE);
        if (change == 0){
            db.insert(QUERY_NAME, null, values);
            Log.v(TAG, "inserting new entity -> " + values.get(COLUMN_ID));
        }else{
            Log.v(TAG, "updating entity -> " + values.get(COLUMN_ID));
        }

        db.close();


    }

    public KinveyDeleteResponse delete(OfflineHelper helper, AbstractClient client, String id){
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
        values.put(COLUMN_DELETED, 1);

        // Setting delete flag on row
        KinveyDeleteResponse ret =  new KinveyDeleteResponse();

        ret.setCount(db.updateWithOnConflict(TABLE_NAME, values, null, null, db.CONFLICT_REPLACE));
        db.close();
        return ret;
    }

    public void enqueueRequest(OfflineHelper helper, String verb, String id){
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_UNIQUE_KEY, AbstractKinveyOfflineClientRequest.generateMongoDBID());
        values.put(COLUMN_ID, id);
        values.put(COLUMN_ACTION, verb);

        db.insert(QUEUE_NAME, null, values);
        db.close();

    }

    public List<OfflineRequestInfo> popQueued(OfflineHelper helper){
        ArrayList<OfflineRequestInfo> ret = new ArrayList<OfflineRequestInfo>();
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor c = db.query(QUEUE_NAME, new String[]{COLUMN_ID, COLUMN_ACTION}, null, null, null, null, null);
        while (c.moveToNext()){
            ret.add(new OfflineRequestInfo(c.getString(1), c.getString(0)));
        }


        c.close();
        db.close();
        return ret;
    }

    public OfflineRequestInfo popSingleQueue(OfflineHelper helper){
        OfflineRequestInfo ret = null;

        SQLiteDatabase db = helper.getReadableDatabase();

        String curKey = null;

        Cursor c = db.query(QUEUE_NAME, new String[]{COLUMN_ID, COLUMN_ACTION, COLUMN_UNIQUE_KEY}, null, null, null, null, null);
        if (c.moveToFirst()){
            ret = new OfflineRequestInfo(c.getString(1), c.getString(0));
            curKey = c.getString(2);
        }
        c.close();
        Log.e(TAG, "*** current key is -> " + curKey);
        //remove the popped request
        if (curKey != null){
            db.delete(QUEUE_NAME, COLUMN_UNIQUE_KEY + "='" + curKey +"'" ,null);
        }
        db.close();
        return ret;
    }

    public void storeCompletedRequestInfo(OfflineHelper helper, String collectionName, boolean success, OfflineRequestInfo info, String returnValue) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ID, info.getEntityID());
        values.put(COLUMN_ACTION, info.getHttpVerb());
        values.put(COLUMN_JSON, returnValue);
        values.put(COLUMN_RESULT, (success ? 1 : 0));

        db.insert(RESULTS_NAME, null, values);

        db.close();
    }

    public List<OfflineResponseInfo> getHistoricalRequests(OfflineHelper helper){

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(RESULTS_NAME, new String[]{COLUMN_ID, COLUMN_ACTION, COLUMN_JSON, COLUMN_RESULT}, null, null, null, null, null);

        ArrayList<OfflineResponseInfo> ret = new ArrayList<OfflineResponseInfo>();

        while (c.moveToNext()){
            ret.add(new OfflineResponseInfo(new OfflineRequestInfo(c.getString(1), c.getString(0)), c.getString(2), (c.getInt(3) == 1 ? true : false )));
        }

        c.close();
        db.close();
        return ret;

    }

}


