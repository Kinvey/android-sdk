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

import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.ContentValues;
import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonGenerator;
import com.google.gson.Gson;
import com.kinvey.android.Client;
import com.kinvey.android.offline.OfflineRequestInfo.OfflineMetaData;
import com.kinvey.android.store.AsyncAppData;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Logger;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.model.KinveyDeleteResponse;

/**
 * This class manages the necessary tables for offline to function associated with one specific {@link AsyncAppData} collection.
 * <p/>
 * Schema-less entities are stored in an `offline_MyCollection` table, which has two columns-- one for the _id of the entity and one for the json representation of the entity.
 * `queue_MyCollection` maintains an ordered list of queued requests, so that they be retrieved and executed when a connection is restored.  The queue associates an HTTP Verb with an _id of the entity to perform it on.
 * Another table is used to support queries, mapping the query string to a list of returned _ids.
 *
 * @author edwardf
 * @since 2.0
 */
public class OfflineTable<T extends GenericJson> {

    private final String TAG = Client.TAG + " " + this.getClass().getSimpleName();

    //every row must have an _id
    private static final String COLUMN_ID = "_id";
    //every row must have a json body
    private static final String COLUMN_JSON = "_json";
    //every row must have a user who made the initial request
    private static final String COLUMN_USER = "_user";
    //for lazy-delete support, add a deleted flag (0 false, 1 true)
    private static final String COLUMN_DELETED = "_deleted";
    //for the unique index on the collection store
    private static final String UNIQUE_INDEX_IDS = "SOME_INDEX";
    //for the unique index on the request queue
    private static final String UNIQUE_INDEX_QUEUE = "ANOTHER_INDEX";
    //for the queued action
    private static final String COLUMN_ACTION = "_action";
    //for query strings
    private static final String COLUMN_QUERY_STRING = "_queryString";
    //for results of request,(0 false, 1 true)
    private static final String COLUMN_RESULT = "_result";
    //unique key for queued requests, using Mongo db's algorithm
    private static final String COLUMN_UNIQUE_KEY = "_key";

    //Each collection has multiple tables, these are the prefixes of the table names
    public static final String PREFIX_OFFLINE = "offline_";
    public static final String PREFIX_QUEUE = "queue_";
    public static final String PREFIX_QUERY = "query_";
    public static final String PREFIX_RESULTS = "results_";

    private String TABLE_NAME;
    private String QUERY_NAME;
    private String QUEUE_NAME;
    private String RESULTS_NAME;


    public OfflineTable(String collection){
        this.TABLE_NAME = "[" + PREFIX_OFFLINE + collection + "]";
        this.QUEUE_NAME = "[" + PREFIX_QUEUE + collection + "]";
        this.QUERY_NAME = "[" + PREFIX_QUERY + collection + "]";
        this.RESULTS_NAME = "[" + PREFIX_RESULTS + collection + "]";
    }

    public void onCreate(DatabaseHandler handler) {
        if (this.TABLE_NAME == null || this.TABLE_NAME.isEmpty()){
        	Logger.ERROR("cannot create a table without a name!");
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


        handler.runCommand(createCommand);
        //create the local action queue
        createCommand = "CREATE TABLE IF NOT EXISTS "
                + QUEUE_NAME
                + "("
                + COLUMN_UNIQUE_KEY + " TEXT not null, "
                + COLUMN_ID + " TEXT not null, "
                + COLUMN_ACTION + " TEXT not null"


                + ");";

        handler.runCommand(createCommand);

        //create the local query store
        createCommand = "CREATE TABLE IF NOT EXISTS "
                + QUERY_NAME
                + "("
                + COLUMN_QUERY_STRING + " TEXT not null, "
                + COLUMN_ID + " TEXT not null"
                + ");";

        handler.runCommand(createCommand);

        //create the local results store
        createCommand = "CREATE TABLE IF NOT EXISTS "
                + RESULTS_NAME
                + "("
                + COLUMN_ID + " TEXT not null, "
                + COLUMN_ACTION + " TEXT not null, "
                + COLUMN_JSON + " TEXT not null, "
                + COLUMN_RESULT + " INTEGER not null"
                + ");";

        handler.runCommand(createCommand);

        //set a unique index on the local offline entity store
        String primaryKey = "CREATE UNIQUE INDEX IF NOT EXISTS " + UNIQUE_INDEX_IDS + " ON " + TABLE_NAME + " (" + COLUMN_ID + " ASC);";
        handler.runCommand(primaryKey);
        //set a unique key on the queued request store
        primaryKey = "CREATE UNIQUE INDEX IF NOT EXISTS " + UNIQUE_INDEX_QUEUE + " ON " + QUEUE_NAME + " (" + COLUMN_UNIQUE_KEY + " ASC);";
        handler.runCommand(primaryKey);


    }


    /**
     * Insert an entity into this offline table
     *
     *
     * @param helper
     * @param client
     * @param offlineEntity
     * @return
     */
    public T insertEntity(DatabaseHandler helper, AbstractClient client, GenericJson offlineEntity, AbstractKinveyJsonClientRequest<T> req){

//        SQLiteDatabase db = helper.getWritableDatabase();

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
        	Logger.ERROR("unable to serialize JSON! -> " + ex);
        }

        values.put(COLUMN_JSON, jsonResult);
        values.put(COLUMN_DELETED, 0);
        values.put(COLUMN_USER, client.userStore().getCurrentUser().getId());

        Logger.INFO("insert entity -> " + offlineEntity.get("_id").toString());

        long sqlid = -2;
        int change = helper.updateWithOnConflict(TABLE_NAME, values, COLUMN_ID + "='" + offlineEntity.get("_id").toString()+"'", null,  5); //5 is CONFLICT_REPLACE
        if (change == 0){
           sqlid =  helper.insert(TABLE_NAME, null, values);
            //Log.v(TAG, "offline inserting new entity -> " + values.get(COLUMN_JSON));
        }else{
            //Log.v(TAG, "offline updating entity -> " + values.get(COLUMN_JSON));
        }
        
        Logger.INFO("done insertion " + TABLE_NAME+ " -> " + (change !=  0 ? "update" : "!update") + ", " + (sqlid >= 0 ? "create" : "!create" ) + (sqlid == -1 ? " with error" : ""));

        return (T) offlineEntity;
    }


    /**
     * Retrive an entity from this offline table
     *
     *
     * @param handler
     * @param client
     * @param id
     * @param responseClass
     * @return
     */
    public T getEntity(DatabaseHandler handler, AbstractClient client, String id, Class<T> responseClass, AbstractKinveyJsonClientRequest<T> req){
        Cursor cursor = handler.query(TABLE_NAME, new String[] {COLUMN_JSON},COLUMN_ID + "='" + id + "'",
               null, null, null, null, null);



        T ret = null;
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()){
            try{
                String s  = cursor.getString(0);
                Logger.INFO("get entity -> " + s);

                ret =  client.getJsonFactory().fromString(s, responseClass);
            }catch(Exception e){
            	Logger.ERROR("cannot parse json into object! -> " + e);
            }

            cursor.close();
        }
//        db.close();
        return ret;
    }


    /**
     * Retrieve the results of a query from this offline table
     *
     * @param handler
     * @param client
     * @param q
     * @param clazz
     * @return
     */
    public T[] getQuery(DatabaseHandler handler, AbstractClient client, String q, Class clazz, AbstractKinveyJsonClientRequest<T> req){

        //SQLiteDatabase db = helper.getReadableDatabase();

        Cursor c =  handler.query(QUERY_NAME, new String[]{COLUMN_ID}, COLUMN_QUERY_STRING + "=?", new String[]{q}, null, null, null, null);
        if (c.moveToFirst() && c.getColumnCount() > 0) {
            String s = c.getString(0);
            //Log.e(TAG, "get query entity -> " + s);
            T[] ret = null;

            String[] resultIDs = s.split(",");
            if (clazz != null) {
                Class singleClass = clazz.getComponentType();
                if (singleClass != null) {
                    ret = (T[]) Array.newInstance(singleClass, resultIDs.length);


                    for (int i = 0; i < resultIDs.length; i++) {
                        ret[i] = getEntity(handler, client, resultIDs[i], singleClass, req);
                    }
                }
            }

            return ret;
        }
        return null;

    }

    public T[] getAll(DatabaseHandler handler, AbstractClient client, Class<T> responseClass, AbstractKinveyJsonClientRequest<T> req){
    	Logger.ERROR("it's a get all");
        Cursor cursor = handler.query(TABLE_NAME, new String[] {COLUMN_JSON},null ,
                null, null, null, null, null);

        List<T> asList = new ArrayList<T>();
        Class singleClass = responseClass.getComponentType();
        if (cursor != null && cursor.getCount() > 0){
            while (cursor.moveToNext()){
            	Logger.ERROR("added one");
                try{
                   String s  = cursor.getString(0);
                   asList.add((T) client.getJsonFactory().fromString(s, singleClass));
                }catch(Exception e){
                	Logger.ERROR("cannot parse json into object! -> " + e);
                    //e.printStackTrace();
                }
            }

            cursor.close();
        }else{
        	Logger.ERROR("cursor is no good");
            return null;
        }
        T[] asArray = (T[]) Array.newInstance(singleClass, asList.size());
        for (int i = 0; i < asList.size(); i++){
            asArray[i] = asList.get(i);
        }

        return asArray;
    }


    /**
     * Store the results of a query
     *
     * @param handler
     * @param queryString
     * @param resultIds
     */
    public void storeQueryResults(DatabaseHandler handler, String queryString, List<String> resultIds){

        //SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_QUERY_STRING, queryString);

        String commaDelimitedIds = TextUtils.join(",", resultIds);

        values.put(COLUMN_ID, commaDelimitedIds);

       // Log.v(TAG, "inserting query: " + queryString);
        
        int change = handler.updateWithOnConflict(QUERY_NAME, values, COLUMN_QUERY_STRING+"='" + queryString +"'", null, 5); //5 is conflict_replace
        if (change == 0){
            handler.insert(QUERY_NAME, null, values);
           // Log.v(TAG, "inserting new query -> " + values.get(COLUMN_ID));
        }else{
           // Log.v(TAG, "updating query -> " + values.get(COLUMN_ID));
        }

//        db.close();


    }

    /**
     * Flag an entity for deletion
     *
     * @param handler
     * @param client
     * @param id
     * @return
     */
    public KinveyDeleteResponse delete(DatabaseHandler handler, AbstractClient client, String id, AbstractKinveyJsonClientRequest<T> req){
        //SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
        values.put(COLUMN_DELETED, 1);

        // Setting delete flag on row
        KinveyDeleteResponse ret =  new KinveyDeleteResponse();

        ret.setCount(handler.updateWithOnConflict(TABLE_NAME, values, COLUMN_ID+"='" + id +"'", null, 5)); //5 is conflict_replace
//        db.close();
        return ret;
    }
    
    /***
     * Remove an Entity from the entity table directly
     * 
     * @param handler
     * @param client
     * @param toRemove
     * @return true if removed
     */
    public boolean removeEntity(DatabaseHandler handler,  String IDtoRemove){
    	handler.delete(TABLE_NAME, COLUMN_ID + "='" + IDtoRemove +"'" ,null);
    	return false;
    }

    /**
     * enqueue a request for later execution
     *
     * @param handler
     * @param verb
     * @param id
     */
    public void enqueueRequest(DatabaseHandler handler, String verb, OfflineMetaData id, AbstractKinveyJsonClientRequest<T> req){

        Cursor c = handler.query(QUEUE_NAME, new String[]{COLUMN_ID, COLUMN_ACTION},  COLUMN_ID+"='" + id+"' AND "+COLUMN_ACTION+"='" + verb + "'", null, null, null, null, null);

        if (c.getCount() == 0){
        	Logger.INFO("offline queueing -> " + id);
            ContentValues values = new ContentValues();
            values.put(COLUMN_UNIQUE_KEY, UUID.randomUUID().toString());
            
            //OfflineRequestInfo.SuperID idInfo = new SuperID(id, req);
            values.put(COLUMN_ID, new Gson().toJson(id));
            values.put(COLUMN_ACTION, verb);
            handler.insert(QUEUE_NAME, null, values);
        }

        c.close();

//        db.close();

    }


    /**
     * Pop a queued request and remove it from the queue
     *
     * @param handler
     * @return
     */
    public OfflineRequestInfo popSingleQueue(DatabaseHandler handler){
        OfflineRequestInfo ret = null;

        String curKey = null;

        Cursor c = handler.query(QUEUE_NAME, new String[]{COLUMN_ID, COLUMN_ACTION, COLUMN_UNIQUE_KEY}, null, null, null, null, null, null);
            if (c.moveToFirst()){
            	String idValue = c.getString(0);
            	OfflineMetaData idInfo = new OfflineMetaData();
            	try{
            		idInfo = new Gson().fromJson(idValue, OfflineMetaData.class);
            	}catch(Exception e){
            		idInfo = new OfflineMetaData(idValue);
            	}
                ret = new OfflineRequestInfo(c.getString(1), idInfo);
                curKey = c.getString(2);
            }
        c.close();
        //remove the popped request
        if (curKey != null){
        	Logger.INFO("offline popped queue, current id is: " + ret.getEntityID());
            handler.delete(QUEUE_NAME, COLUMN_UNIQUE_KEY + "='" + curKey +"'" ,null);
        }
//        db.close();
        return ret;
    }


    /**
     * store the results of a request executed in the background
     *
     * @param helper
     * @param collectionName
     * @param success
     * @param info
     * @param returnValue
     * @deprecated removed, as table would grow infinitely and currently not needed
     */
    public void storeCompletedRequestInfo(OfflineHelper helper, String collectionName, boolean success, OfflineRequestInfo info, String returnValue) {
        //no-op haven't found a use for this yet
//        if (false) {
//            SQLiteDatabase db = helper.getWritableDatabase();
//            ContentValues values = new ContentValues();
//
//            values.put(COLUMN_ID, info.getEntityID());
//            values.put(COLUMN_ACTION, info.getHttpVerb());
//            values.put(COLUMN_JSON, returnValue);
//            values.put(COLUMN_RESULT, (success ? 1 : 0));
//
//            db.insert(RESULTS_NAME, null, values);
//
//        }
//        return;
    }


    /**
     * return a list of all historical offline requests
     *
     * @param handler
     * @return
     * @deprecated removed, as table would grow infinitely
     */
    public List<OfflineResponseInfo> getHistoricalRequests(DatabaseHandler handler){
    	return null;
//        if (false) {
//
//            Cursor c = handler.query(RESULTS_NAME, new String[]{COLUMN_ID, COLUMN_ACTION, COLUMN_JSON, COLUMN_RESULT}, null, null, null, null, null, null);
//
//            ArrayList<OfflineResponseInfo> ret = new ArrayList<OfflineResponseInfo>();
//
//            while (c.moveToNext()) {
//                ret.add(new OfflineResponseInfo(new OfflineRequestInfo(c.getString(1), c.getString(0)), c.getString(2), (c.getInt(3) == 1 ? true : false)));
//            }
//
//            c.close();
////            db.close();
//            return ret;
//        }
//        return new ArrayList<OfflineResponseInfo>();

    }


}


