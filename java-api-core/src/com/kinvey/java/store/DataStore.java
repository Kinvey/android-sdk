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

package com.kinvey.java.store;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.requests.data.PushRequest;
import com.kinvey.java.store.requests.data.ReadRequest;
import com.kinvey.java.store.requests.data.delete.DeleteIdsRequest;
import com.kinvey.java.store.requests.data.delete.DeleteQueryRequest;
import com.kinvey.java.store.requests.data.delete.DeleteSingleRequest;
import com.kinvey.java.store.requests.data.read.ReadSingleRequest;
import com.kinvey.java.store.requests.data.save.SaveListRequest;
import com.kinvey.java.store.requests.data.save.SaveRequest;
import com.kinvey.java.store.requests.data.read.ReadAllRequest;
import com.kinvey.java.store.requests.data.read.ReadIdsRequest;
import com.kinvey.java.store.requests.data.read.ReadQueryRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class DataStore<T extends GenericJson> {

    private static final long HOUR = 1* 60 * 60 * 1000l;

    protected final AbstractClient client;
    private final String collection;
    private StoreType storeType;
    private Class<T> storeItemType;
    private ICache<T> cache;
    NetworkManager<T> networkManager;
    private String collectionName;




    /**
     * Constructor for creating DataStore for given collection that will be mapped to itemType class
     * @param client Kinvey client instance to work with
     * @param collection collection name
     * @param itemType class that data should be mapped to
     * @param storeType type of storage that client want to use
     */
    public DataStore(AbstractClient client, String collection, Class<T> itemType, StoreType storeType){

        this(client, collection, itemType, storeType, new NetworkManager<T>(collection, itemType, client));



    }

    protected  DataStore(AbstractClient client, String collection, Class<T> itemType, StoreType storeType,
                         NetworkManager<T> networkManager){
        this.storeType = storeType;
        this.client = client;
        this.collection = collection;
        this.storeItemType = itemType;
        cache = client.getCacheManager().getCache(collection, itemType, storeType.ttl);
        this.networkManager = networkManager;
    }

    /**
     * Look up for data with given id
     * @param id the id of object we need to find
     * @return null or object that matched given id
     */
    public T find (String id){
        T ret = null;
        try {
            ret = new ReadSingleRequest<T>(cache, id, this.storeType.readPolicy, networkManager).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Look up for object that have id in given collection of ids
     * @param ids collection of strings that identify a set of ids we have to look for
     * @return List of object found for given ids
     */
    public List<T> find (Iterable<String> ids){
        List<T> ret = null;
        try {
            ret = new ReadIdsRequest<T>(cache, networkManager, this.storeType.readPolicy, ids).execute();
        } catch (IOException e){

        }
        return ret;
    }


    /**
     * Lookup objects in given collection by given query
     * @param query prepared query we have to look with
     * @return list of objects that are found
     */
    public List<T> find (Query query) {
        // perform request based on policy
        List<T> ret = null;
        try {
            ret = new ReadQueryRequest<T>(cache, networkManager, this.storeType.readPolicy, query).execute();
        } catch (IOException e){

        }
        return ret;
    }


    /**
     * get all objects for given collections
     * @return all objects in given collection
     */
    public List<T> find () {
        // perform request based on policy
        List<T> ret = null;
        try {
            ret = new ReadAllRequest<T>(cache, this.storeType.readPolicy, networkManager).execute();
        } catch (IOException e){

        }
        return ret;
    }


    /**
     * Save multiple objects for collections
     * @param objects list of objects to be saved
     * @return updated list of object that will contain ids if they was not present in moment of saving
     * @throws IOException
     */
    public List<T> save (Iterable<T> objects) throws IOException {
        return new SaveListRequest<T>(cache, networkManager, this.storeType.writePolicy, objects, client.getSycManager()).execute();
    }


    /**
     * Save single object into collection
     * @param object Object to be saved in given collection
     * @return updated object with filled some required fields
     * @throws IOException
     */
    public T save (T object) throws IOException {
        return new SaveRequest<T>(cache, networkManager, this.storeType.writePolicy, object, client.getSycManager()).execute();
    }

    /**
     * Remove object from from given collection with given id
     * @param id id of object to be deleted
     * @return count of object that was deleted
     * @throws IOException
     */
    public Integer delete (String id) throws IOException {
        return new DeleteSingleRequest<T>(cache, networkManager, this.storeType.writePolicy, id, client.getSycManager()).execute();
    }

    /**
     * Remove objects from given query that matches given query
     * @param query query to lookup objects for given collection
     * @return cound of objects that was removed
     * @throws IOException
     */
    public Integer delete (Query query) throws IOException {
        return new DeleteQueryRequest<T>(cache, networkManager, this.storeType.writePolicy, query, client.getSycManager()).execute();
    }

    /**
     * Remove objects from given collections with list of ids
     * @param ids identifiers of objects to be deleted
     * @return count of objects that was deleted bu given call
     * @throws IOException
     */
    public Integer delete (Iterable<String> ids) throws IOException {
        return new DeleteIdsRequest<T>(cache, networkManager, this.storeType.writePolicy, ids, client.getSycManager()).execute();
    }

    /**
     * Push local changes to network
     * should be user with {@link StoreType#SYNC}
     */
    public void pushBlocking() {
        new PushRequest<T>(collection, client).execute();
    }

    /**
     * Pull network data with given query into local storage
     * should be user with {@link StoreType#SYNC}
     */
    public void pullBlocking(Query query) {
        try {
            new ReadRequest<T>(cache, query, ReadPolicy.FORCE_NETWORK, Long.MAX_VALUE, networkManager).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Run sync operation to sync local and network storages
     * @param query query to pull the objects
     */
    public void syncBlocking(Query query) {
        pushBlocking();
        pullBlocking(query);
    }


    /**
     * Set store type for current DataStore
     * @param storeType
     */
    public void setStoreType(StoreType storeType) {
        this.storeType = storeType;
    }

    /**
     * Getter for client
     * @return Client instance for given DataStore
     */
    public AbstractClient getClient() {
        return client;
    }

    public Class<T> getCurrentClass() {
        return storeItemType;
    }

    public String getCollectionName() {
        return collectionName;
    }


}
