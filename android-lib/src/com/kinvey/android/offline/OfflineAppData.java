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
import com.kinvey.java.AbstractClient;
import com.kinvey.java.core.KinveyClientCallback;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/** This class allows for managing a collection while the client is offline, and syncing with Kinvey when a connection is restored.
 * </p>
 * The OfflineStore class maintains the latest state of all Entities, as well as a queue of all REST requests made while offline.
 * </p>
 * When there are requests in the queue, an Android Service will be started to pull these requests and send them to Kinvey.
 * </p>
 * provides offline storage for data, queueing up REST requests, executing those requests, and updating the offline store with results.
 * </p>
 * </p>
 * NOTE while the offline data store is persistent, it will be empty when it is first created and needs to be seeded--
 * take this into consideration when designing an app to function offline.  This initial seeding can happen by calling save on offlineAppData
 * </p>
 * @author edwardf
 * @since 2.0
 */
public class OfflineAppData<T> implements Observer{

    private OfflineStore store;

    private String collectionName;
    private Class<T> myClass;
    private AbstractClient client;

    private Context context;
    private KinveyOfflineCallback callback;


    /**
     * Constructor to instantiate the Offline AppData class.
     *
     * @param collectionName Name of the appData collection
     * @param myClass        Class Type to marshall data between.
     */
    public OfflineAppData(String collectionName, Class myClass, AbstractClient client, Context context) {
        Log.v(Client.TAG,  "OfflineAppData API Constructor called");
        this.collectionName = collectionName;
        this.myClass = myClass;
        this.client = client;
        this.store = new OfflineStore(context, collectionName);
        this.store.addObserver(this);
        this.context = context;
    }


    /**Get an entity or entities from an offline collection.  Pass null to entityID to return all entities
     * in a collection.
     *
     * @param entityID entityID to get
     * @return Get object
     * @throws java.io.IOException
     */
    public void getEntity(String entityID, KinveyClientCallback<T> callback){
        //TODO revist this
        this.store.get(entityID, callback);

    }



    /** Save (create or update) an entity to an offline collection.
     *
     * @param entity Entity to Save
     *
     * @return Save object
     * @throws IOException
     */
    public void save(T entity, KinveyClientCallback<T> callback)  {
        this.store.save(entity, callback);



        Intent i = new Intent(this.context, OfflineAppDataService.class);
        i.setAction("com.kinvey.android.ACTION_OFFLINE_SYNC");
        this.context.startService(i);
        Log.v(Client.TAG, "sent broadcast for offline sync!");

    }


    /** Delete an entity from an offline collection.
     *
     * @param entityID entityID to delete
     * @return Delete object
     * @throws IOException
     */
    public void delete(String entityID, KinveyDeleteCallback callback) {
        this.store.delete(entityID, callback);

    }

    public int getQueueSize(){
        return this.store.getRequestStoreCount();
    }

    public int getEntityCount(){
        return this.store.getEntityStoreCount();
    }


    public void setCallback(KinveyOfflineCallback callback) {
        this.callback = callback;
    }

    public KinveyOfflineCallback getCallback() {
        return callback;
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.v(Client.TAG, "Observerable changed! -> " + observable + " and " + o);
    }
}



