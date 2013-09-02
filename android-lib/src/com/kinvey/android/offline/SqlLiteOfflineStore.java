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
import android.content.Intent;
import android.util.Log;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.AppData;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.offline.AbstractKinveyOfflineClientRequest;
import com.kinvey.java.offline.OfflineStore;

import java.net.URLDecoder;

/**
 * This class is an implementation of an {@link OfflineStore}, which provides methods to execute requests locally.
 * <p/>
 * This class delegates requests to an appropriate {@link OfflineTable}, which is associated with the current collection.
 * <p/>
 * It also enqueues requests in that same {@link OfflineTable}, and can start an Android Service to begin background sync.
 *
 *
 * @author edwardf
 */
public class SqlLiteOfflineStore<T> implements OfflineStore<T> {

    private static final String TAG = "Kinvey - SQLLite Offline Store";

    private Context context = null;

    public SqlLiteOfflineStore(Context context){
        this.context = context.getApplicationContext();
    }


    /**
     * Execute a get request against this offline store
     *
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


        //ensure table exists, if not, create it   <- done by constructor of offlinehelper (oncreate will delegate)
        OfflineHelper dbHelper = new OfflineHelper(context);

        //expand the URI from the template
        String targetURI = UriTemplate.expand(client.getBaseUrl(), request.getUriTemplate(), request, true);
        //find the index after {collectionName}/
        int idIndex = targetURI.indexOf(appData.getCollectionName()) + appData.getCollectionName().length() + 1;


        T ret;
        //determine if it is a query or get by id
        if (targetURI.contains("query") || idIndex == targetURI.length()){
            //it's a query
            String query = targetURI.substring(idIndex, targetURI.length());
   
            query = query.replace("?query=","");
            try{
                query = URLDecoder.decode(query, "UTF-8");
            }catch (Exception e){}
//
//            Log.e("Offline", "targeturi string is: " + targetURI);
//            Log.e("Offline", "idIndex  is: " + idIndex );
//            Log.e("Offline", "query string is: " + query);

            ret = (T) dbHelper.getTable(appData.getCollectionName()).getQuery(dbHelper, client, query, appData.getCurrentClass());

            dbHelper.getTable(appData.getCollectionName()).enqueueRequest(dbHelper, "QUERY", query);


        }else{
            //it's get by id
            String targetID = targetURI.substring(idIndex, targetURI.length());
            ret = (T) dbHelper.getTable(appData.getCollectionName()).getEntity(dbHelper, client, targetID, appData.getCurrentClass());

            dbHelper.getTable(appData.getCollectionName()).enqueueRequest(dbHelper, "GET", targetURI.substring(idIndex, targetURI.length()));


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


        //ensure table exists, if not, create it   <- done by constructor of offlinehelper (oncreate will delegate)
        OfflineHelper dbHelper = new OfflineHelper(context);

        //set deleted flag in table
        //expand the URI from the template
        String targetURI = UriTemplate.expand(client.getBaseUrl(), request.getUriTemplate(), request, false);
        //find the index after {collectionName}/
        int idIndex = targetURI.indexOf(appData.getCollectionName()) + appData.getCollectionName().length() + 1;

        String targetID = targetURI.substring(idIndex, targetURI.length());
        KinveyDeleteResponse ret = dbHelper.getTable(appData.getCollectionName()).delete(dbHelper,client, targetID);
        dbHelper.getTable(appData.getCollectionName()).enqueueRequest(dbHelper, "DELETE", targetURI.substring(idIndex, targetURI.length()));

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


        //ensure table exists, if not, create it   <- done by constructor of offlinehelper (oncreate will delegate)
        OfflineHelper dbHelper = new OfflineHelper(context);

        //grab json content and put it in the store
        GenericJson jsonContent = (GenericJson) request.getJsonContent();
        T ret = (T) dbHelper.getTable(appData.getCollectionName()).insertEntity(dbHelper, client, jsonContent);

        dbHelper.getTable(appData.getCollectionName()).enqueueRequest(dbHelper, "PUT", ((GenericJson)request.getJsonContent()).get("_id").toString());

        kickOffSync();

        return ret;
    }

    @Override
    public void insertEntity(AbstractClient client, AppData<T> appData, AbstractKinveyOfflineClientRequest request) {

        OfflineHelper dbHelper = new OfflineHelper(context);
        GenericJson jsonContent = (GenericJson) request.getJsonContent();

        dbHelper.getTable(appData.getCollectionName()).insertEntity(dbHelper, client, jsonContent);
        //To change body of implemented methods use File | Settings | File Templates.
    }


    private void kickOffSync(){
        Intent syncIt = new Intent(this.context, KinveySyncService.class);
        syncIt.setAction(KinveySyncService.ACTION_OFFLINE_SYNC);
        this.context.startService(syncIt);

    }



}
