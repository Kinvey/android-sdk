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
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;
import android.util.Log;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.json.GenericJson;
import com.kinvey.android.Client;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.AppData;
import com.kinvey.java.Query;
import com.kinvey.java.core.AbstractKinveyJsonClient;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.offline.AbstractKinveyOfflineClientRequest;
import com.kinvey.java.offline.OfflineGenericJson;
import com.kinvey.java.offline.OfflineStore;

/**
 * @author edwardf
 */
public class SqlLiteOfflineStore<T> implements OfflineStore<T> {

    private static final String TAG = "Kinvey - SQLLite Offline Store";

    private Context context = null;

    public SqlLiteOfflineStore(Context context){
        this.context = context.getApplicationContext();
    }



    @Override
    public T executeGet(AbstractClient client, AppData<T> appData, AbstractKinveyOfflineClientRequest request) {

        if (this.context == null){
            Log.e(TAG, "Context is invalid, cannot access sqllite!");
            return null;
        }


        //ensure table exists, if not, create it   <- done by constructor of offlinehelper (oncreate will delegate)
        OfflineHelper dbHelper = new OfflineHelper(context, appData.getCollectionName());
//        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //expand the URI from the template
        String targetURI = UriTemplate.expand(client.getBaseUrl(), request.getUriTemplate(), request, false);
        //find the index after {collectionName}/
        int idIndex = targetURI.indexOf(appData.getCollectionName()) + appData.getCollectionName().length() + 1;


        T ret;
        //determine if it is a query or get by id
        if (targetURI.contains("query") || idIndex == targetURI.length()){
            //it's a query
            String query = targetURI.substring(idIndex, targetURI.length());
            ret = (T) dbHelper.getTable(appData.getCollectionName()).getQuery(dbHelper, client, query, appData.getCurrentClass());
            dbHelper.getTable(appData.getCollectionName()).enqueueRequest(dbHelper, "QUERY", targetURI.substring(idIndex, targetURI.length()));


        }else{
            //it's get by id
            String targetID = targetURI.substring(idIndex, targetURI.length());
            ret = (T) dbHelper.getTable(appData.getCollectionName()).getEntity(dbHelper, client, targetID, appData.getCurrentClass());
            dbHelper.getTable(appData.getCollectionName()).enqueueRequest(dbHelper, "GET", targetURI.substring(idIndex, targetURI.length()));


        }
//        db.close();

        kickOffSync();
        return ret;

    }

    @Override
    public KinveyDeleteResponse executeDelete(AbstractClient client, AppData<T> appData, AbstractKinveyOfflineClientRequest request) {

        if (this.context == null){
            Log.e(TAG, "Context is invalid, cannot access sqllite!");
            return null;
        }


        //ensure table exists, if not, create it   <- done by constructor of offlinehelper (oncreate will delegate)
        OfflineHelper dbHelper = new OfflineHelper(context, appData.getCollectionName());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //set deleted flag in table
        //expand the URI from the template
        String targetURI = UriTemplate.expand(client.getBaseUrl(), request.getUriTemplate(), request, false);
        //find the index after {collectionName}/
        int idIndex = targetURI.indexOf(appData.getCollectionName()) + appData.getCollectionName().length() + 1;

        String targetID = targetURI.substring(idIndex, targetURI.length());
        KinveyDeleteResponse ret = dbHelper.getTable(appData.getCollectionName()).delete(dbHelper,client, targetID);
        db.close();
        dbHelper.getTable(appData.getCollectionName()).enqueueRequest(dbHelper, "DELETE", targetURI.substring(idIndex, targetURI.length()));

        kickOffSync();
        return ret;

    }

    @Override
    public T executeSave(AbstractClient client, AppData<T> appData, AbstractKinveyOfflineClientRequest request) {

        if (this.context == null){
            Log.e(TAG, "Context is invalid, cannot access sqllite!");
            return null;
        }


        //ensure table exists, if not, create it   <- done by constructor of offlinehelper (oncreate will delegate)
        OfflineHelper dbHelper = new OfflineHelper(context, appData.getCollectionName());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //grab json content and put it in the store

        OfflineGenericJson jsonContent = (OfflineGenericJson) request.getJsonContent();
        T ret = (T) dbHelper.getTable(appData.getCollectionName()).insertEntity(dbHelper, client, jsonContent);

        db.close();

        dbHelper.getTable(appData.getCollectionName()).enqueueRequest(dbHelper, "PUT", ((OfflineGenericJson)request.getJsonContent()).get("_id").toString());

        kickOffSync();

        return ret;
    }



    private void kickOffSync(){
        Intent syncIt = new Intent(this.context, KinveySyncService.class);
        syncIt.setAction(KinveySyncService.ACTION_OFFLINE_SYNC);
        this.context.startService(syncIt);

    }



}
