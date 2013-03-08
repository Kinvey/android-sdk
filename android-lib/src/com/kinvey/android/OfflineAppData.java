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
package com.kinvey.android;

import android.content.Context;

import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.core.KinveyClientCallback;

import java.io.IOException;

/** This class allows for managing a collection while the client is offline, and syncing with Kinvey when a connection is restored.
 * </p>
 * The OfflineStore class maintains the latest state of all Entities, as well as a queue of all REST requests made while offline.
 * </p>
 * When there are requests in the queue, an Android Service will be started to pull these requests and send them to Kinvey.
 * </p>
 * provides offline storage for data, queueing up REST requests, executing those requests, and updating the offline store with results.
 * </p>
 * </p>
 * NOTE while the offline data store is persistant, it will be empty when it is first created and needs to be seeded--
 * take this into consideration when designing an app to function offline.  This initial seeding can happen by calling save on offlineAppData
 * </p>
 * @author edwardf
 * @since 2.0
 */
public class OfflineAppData<T> {

    private OfflineStore store;

    private String collectionName;
    private Class<T> myClass;
    private AbstractClient client;


    /**
     * Constructor to instantiate the Offline AppData class.
     *
     * @param collectionName Name of the appData collection
     * @param myClass        Class Type to marshall data between.
     */
    protected OfflineAppData(String collectionName, Class myClass, AbstractClient client, Context context) {
        this.collectionName = collectionName;
        this.myClass = myClass;
        this.client = client;
        this.store = new OfflineStore(context, collectionName);
    }


    /**Get an entity or entities from an offline collection.  Pass null to entityID to return all entities
     * in a collection.
     *
     * @param entityID entityID to get
     * @return Get object
     * @throws java.io.IOException
     */
    public <T> void get(String entityID, KinveyClientCallback<T> callback){
        this.store.get(entityID, callback);
    }



    /** Save (create or update) an entity to an offline collection.
     *
     * @param entity Entity to Save
     *
     * @return Save object
     * @throws IOException
     */
    public <T> void save(T entity, KinveyClientCallback<T> callback) throws IOException {
        this.store.save(entity, callback);

    }


    /** Delete an entity from an offline collection.
     *
     * @param entityID entityID to delete
     * @return Delete object
     * @throws IOException
     */
    public <T> void delete(String entityID, KinveyDeleteCallback callback) throws IOException {
        this.store.delete(entityID, callback);

    }





}



