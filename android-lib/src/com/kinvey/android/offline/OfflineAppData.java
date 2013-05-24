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
import android.util.Log;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * This class allows for managing a collection while the client is offline, and syncing with Kinvey when a connection is restored.
 * </p>
 * The OfflineStore class maintains the latest state of all Entities, as well as a queue of all REST requests made while offline.
 * </p>
 * When there are requests in the queue, an Android Service will be started to pull these requests and send them to Kinvey.
 * </p>
 * This class provides offline storage for data, queueing up REST requests, executing those requests, and updating the offline store with results.
 * </p>
 * </p>
 * NOTE while the offline data store is persistent, it will be empty when it is first created and needs to be seeded--
 * take this into consideration when designing an app to function offline.  This initial seeding can happen by calling save on offlineAppData
 * </p>
 *
 * @author edwardf
 * @since 2.0
 */
public class OfflineAppData<T> implements Observer {


    private String collectionName;
    private Class<T> myClass;
    private AbstractClient client;

    private Context context;
    private KinveySyncCallback callback;


    /**
     * Constructor to instantiate the Offline AppData class.
     *
     * @param collectionName Name of the appData collection
     * @param myClass        Class Type to marshall data between.
     */
    public OfflineAppData(String collectionName, Class myClass, AbstractClient client, Context context) {
        Log.v(Client.TAG, "OfflineAppData API Constructor called");
        this.collectionName = collectionName;
        this.myClass = myClass;
        this.client = client;
//        this.store = new OfflineStore(context, collectionName, myClass);
//        this.store.addObserver(this);
        this.context = context.getApplicationContext();

        OfflineStore.getStore(this.context, this.collectionName, this.myClass).addObserver(this);





    }

    /**
     * Gets the instance of the OfflineSettings singleotn class
     *
     * @return OfflineSettings instance
     */
    public OfflineSettings offlineSettings() {
        return OfflineSettings.getInstance(context);
    }

    /**
     * Get an entity or entities from an offline collection.  Pass null to entityID to return all entities
     * in a collection.
     *
     * @param entityID entityID to get
     * @return Get object
     * @throws java.io.IOException
     */
    public void getEntity(String entityID, KinveyClientCallback<T> callback) {

        OfflineStore.getStore(this.context, this.collectionName, this.myClass).getEntity(entityID, callback);

        startSync(this.context);

    }

    /**
     * Get entities by query from an offline collection.
     *
     *
     */
    public void get(Query q, KinveyListCallback<T> callback){
        String jsonQuery = q.getQueryFilterJson(this.client.getJsonFactory());  //this.client.getJsonFactory()


        OfflineStore.getStore(this.context, this.collectionName, this.myClass).get(q, jsonQuery, callback);

        startSync(this.context);
    }


    /**
     * Save (create or update) an entity to an offline collection.
     *
     * @param entity Entity to Save
     * @return Save object
     * @throws IOException
     */
    public void save(T entity, KinveyClientCallback<T> callback) {
//        this.store.save(entity, callback);
        OfflineStore.getStore(this.context, this.collectionName, this.myClass).save(entity, callback);

        startSync(this.context);


    }


    /**
     * Delete an entity from an offline collection.
     *
     * @param entityID entityID to delete
     * @return Delete object
     * @throws IOException
     */
    public void delete(String entityID, KinveyDeleteCallback callback) {
//        this.store.delete(entityID, callback);
        OfflineStore.getStore(this.context, this.collectionName, this.myClass).delete(entityID, callback);

       startSync(this.context);

    }

    /**
     * Get how many requests are queued up for execution when a connection is restored.
     * @return size of internal queue representing count of pending calls.
     */
    public int getQueueSize() {
//        return this.store.getRequestStoreCount();
        return OfflineStore.getStore(this.context, this.collectionName, this.myClass).getRequestStoreCount();

    }

    /**
     * Get how many entities are locally persisted to disk
     * @return the size of the internal store maintaining offline entities.
     */
    public int getEntityCount() {
//        return this.store.getEntityStoreCount();
        return OfflineStore.getStore(this.context, this.collectionName, this.myClass).getEntityStoreCount();

    }



    /**
     * Start the OfflineAppDataService with an intent for performing Sync
     */
    public static void startSync(Context context){
        Intent i = new Intent(context, OfflineAppDataService.class);
        i.setAction("com.kinvey.android.ACTION_OFFLINE_SYNC");
        context.startService(i);
        Log.v(Client.TAG, "sent intent for offline sync!");

    }
    /**
     * Set a callback to retrieve updates on live execution of requests in background
     *
     * @param callback instance of a callback to receive updates.
     */
    public void setCallback(KinveySyncCallback callback) {
        this.callback = callback;
    }

    /**
     * Get the set callback which retrieves live updates from execution
     *
     * @return {@code null} or current instance of callback
     */
    protected KinveySyncCallback getCallback() {
        return callback;
    }

    /**
     * Called by the {@code com.kinvey.android.offline.OfflineStore} when an update occurs.
     *
     * @param observable - the Offline Store
     * @param o - the Request Info of the update.
     */
    @Override
    public void update(Observable observable, Object o) {
        Log.v(Client.TAG, "Observerable changed! -> " + observable + " and " + o);
        if (callback == null) {
            return;
        }

        OfflineResponseInfo resp = (OfflineResponseInfo) o;

        if (resp.isSuccess()){
            callback.onSuccess(resp);
        }else{
            callback.onFailure(resp);
        }




    }

    /**
     * Get a list of requests executed by the background executor which were successful.
     * @return list of successful offline requests
     */
    public List<OfflineRequestInfo> getSuccessfulCalls() {
//        return this.store.getSuccessfulCalls();
        return OfflineStore.getStore(this.context, this.collectionName, this.myClass).getSuccessfulCalls();


    }

    /**
     * Get a list of requests executed by the background executor which failed.
     * @return list of failed offline requests
     */
    public List<OfflineRequestInfo> getFailedCalls() {
//        return this.store.getFailedCalls();
          return OfflineStore.getStore(this.context, this.collectionName, this.myClass).getFailedCalls();
    }

}



