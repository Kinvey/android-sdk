/*
 * Copyright (c) 2014, Kinvey, Inc.
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
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.AppData;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.offline.AbstractKinveyOfflineClientRequest;
import com.kinvey.java.offline.OfflineStore;

import java.net.URLDecoder;
import java.util.List;

/**
 * This class is an implementation of an {@link OfflineStore}
 * <p/>
 * This class delegates requests to an appropriate {@link OfflineTable}, which is associated with the current collection.
 * <p/>
 * It also enqueues requests in that same {@link OfflineTable}, and can start an Android Service to begin background sync.
 *
 *
 * @author edwardf
 */
public abstract class AbstractSqliteOfflineStore<T> implements OfflineStore<T> {

    private Context context = null;
    private static final String TAG = "Kinvey - Sqliteoffline store";


    public AbstractSqliteOfflineStore(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Execute a get request against this offline store.  This will method will expand the target URI into a String, and then grab the index of character right after "<CollectionName>/".
     * This character represents either:
     * an empty string, which is an empty query,
     * an _id, so the GET request is by _id
     * or a query string, beginning with "?query="
     * <p/>
     * Dependant on which case, this method will execute the apppriate GET method on the set {@link DatabaseHandler}.
     * <p/>
     * @param client - an instance of a client
     * @param appData - an instance of AppData
     * @param request - an Offline Client Request to be executed (must be a GET)
     * @return the entity or null
     */
    @Override
    public T executeGet(AbstractClient client, AppData<T> appData, AbstractKinveyOfflineClientRequest request) {

        if (this.context == null){
            Log.e(TAG, "Context is invalid, cannot access sqllite!");
            return null;
        }

        DatabaseHandler handler = getDatabaseHandler(client.user().getId());

        //expand the URI from the template and grab the index of where the get paremeters will be
        String targetURI = UriTemplate.expand(client.getBaseUrl(), request.getUriTemplate(), request, true);
        int idIndex = targetURI.indexOf(appData.getCollectionName()) + appData.getCollectionName().length() + 1;
        T ret;
        //is it a query?  (12 is magic number for decoding empty url)
        if (targetURI.contains("query") && (targetURI.indexOf("query") + 12) != targetURI.length()){


       //     Log.i(TAG, "it's a GET query " + targetURI.indexOf("query") + " and " + targetURI.length());
            //Since it's a query, pull the actual query string out and get rid of the "?query"
            String query = targetURI.substring(idIndex, targetURI.length());
            query = query.replace("?query=","");
            try{
                query = URLDecoder.decode(query, "UTF-8");
            }catch (Exception e){}//if this happens it will also happen with online mode.

            ret = (T) handler.getTable(appData.getCollectionName()).getQuery(handler, client, query, appData.getCurrentClass());

            handler.getTable(appData.getCollectionName()).enqueueRequest(handler, "QUERY", query);

        }else if (idIndex == targetURI.length() || targetURI.contains("query")) {
            //is it a get all?

        //    Log.i(TAG, "it's a GET all");
            ret = (T) handler.getTable(appData.getCollectionName()).getAll(handler, client, appData.getCurrentClass());

        }else{
        //    Log.i(TAG, "it's a GET by id");
            //it's get by id
            String targetID = targetURI.substring(idIndex, targetURI.length());
            ret = (T) handler.getTable(appData.getCollectionName()).getEntity(handler, client, targetID, appData.getCurrentClass());

            handler.getTable(appData.getCollectionName()).enqueueRequest(handler, "GET", targetURI.substring(idIndex, targetURI.length()));


        }

        kickOffSync();
        return ret;

    }

    /**
     *
     * Execute a delete against this offline store
     *
     * @param client - an instance of a client
     * @param appData - an instance of AppData
     * @param request - an Offline Client Request to be executed (must be a DELETE)
     * @return a delete response containing the count of entities deleted
     */
    @Override
    public KinveyDeleteResponse executeDelete(AbstractClient client, AppData<T> appData, AbstractKinveyOfflineClientRequest request) {

        if (this.context == null){
            Log.e(TAG, "Context is invalid, cannot access sqllite!");
            return null;
        }

        DatabaseHandler handler = getDatabaseHandler(client.user().getId());

        //set deleted flag in table
        //expand the URI from the template
        String targetURI = UriTemplate.expand(client.getBaseUrl(), request.getUriTemplate(), request, false);
        //find the index after {collectionName}/
        int idIndex = targetURI.indexOf(appData.getCollectionName()) + appData.getCollectionName().length() + 1;

        String targetID = targetURI.substring(idIndex, targetURI.length());
        KinveyDeleteResponse ret = handler.getTable(appData.getCollectionName()).delete(handler,client, targetID);
        handler.getTable(appData.getCollectionName()).enqueueRequest(handler, "DELETE", targetURI.substring(idIndex, targetURI.length()));

        kickOffSync();
        return ret;

    }


    /**
     *
     * Execute a save against this offline store
     *
     * @param client - an instance of a client
     * @param appData - an instance of AppData
     * @param request - an Offline Client Request to be executed (must be a PUT or POST)
     * @return the entity saved
     */
    @Override
    public T executeSave(AbstractClient client, AppData<T> appData, AbstractKinveyOfflineClientRequest request) {

        if (this.context == null){
            Log.e(TAG, "Context is invalid, cannot access sqllite!");
            return null;
        }


        DatabaseHandler handler = getDatabaseHandler(client.user().getId());

        //grab json content and put it in the store
        GenericJson jsonContent = (GenericJson) request.getJsonContent();
        T ret = (T) handler.getTable(appData.getCollectionName()).insertEntity(handler, client, jsonContent);

        handler.getTable(appData.getCollectionName()).enqueueRequest(handler, "PUT", ((GenericJson)request.getJsonContent()).get("_id").toString());

        kickOffSync();

        return ret;
    }

    @Override
    public void insertEntity(AbstractClient client, AppData<T> appData, T entity) {

        DatabaseHandler handler = getDatabaseHandler(client.user().getId());
        GenericJson jsonContent = (GenericJson) entity;

        handler.getTable(appData.getCollectionName()).insertEntity(handler, client, jsonContent);
    }

    @Override
    public void clearStorage(String userid) {

        DatabaseHandler handler = getDatabaseHandler(userid);

        List<String> collections = handler.getCollectionTables();

        for (String collection : collections){
            handler.delete(OfflineTable.PREFIX_QUEUE + collection, null, null);
            handler.delete(OfflineTable.PREFIX_OFFLINE + collection, null, null);
            handler.delete(OfflineTable.PREFIX_QUERY + collection, null, null);
            handler.delete(OfflineTable.PREFIX_RESULTS + collection, null, null);
        }

        //db.close();
    }





    protected abstract DatabaseHandler getDatabaseHandler(String userid);

    public Context getContext(){
        return this.context;
    }

}
