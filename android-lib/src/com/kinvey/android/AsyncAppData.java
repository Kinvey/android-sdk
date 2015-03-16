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
package com.kinvey.android;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.common.base.Preconditions;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.AppData;
import com.kinvey.java.Query;
import com.kinvey.java.cache.AbstractKinveyCachedClientRequest;
import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.offline.OfflinePolicy;
import com.kinvey.java.offline.OfflineStore;

/**
 * Wraps the {@link com.kinvey.java.AppData} public methods in asynchronous functionality using native Android AsyncTask.
 * <p/>
 * <p>
 * This functionality can be accessed through the {@link com.kinvey.android.Client#appData} convenience method.  AppData
 * gets and saves entities that extend {@link com.google.api.client.json.GenericJson}.  A class that extends GenericJson
 * can map class members to KinveyCollection properties using {@link com.google.api.client.util.Key} attributes.  For example,
 * the following will map a string "city" to a Kinvey collection attributed named "city":
 * </p>
 * <p>
 * <pre>
 *     {@literal @}Key
 *     private String city;
 * </pre>
 * </p>
 * <p>
 * The @Key attribute also can take an optional name, which will map the member to a different attribute name in the Kinvey
 * collection.
 * </p>
 * <p>
 * <pre>
 *     {@literal @}Key("_id")
 *     private String customerID;
 * </pre>
 * </p>
 * <p>
 * Methods in this API use either {@link com.kinvey.android.callback.KinveyListCallback} for retrieving entity sets,
 * {@link com.kinvey.android.callback.KinveyDeleteCallback} for deleting appData, or  the general-purpose
 * {@link com.kinvey.java.core.KinveyClientCallback} used for retrieving single entites or saving Entities.
 * </p>
 * <p/>
 * <p>
 * Entity Set sample:
 * <pre>
 * {@code
 *     AppData<EventEntity> myAppData = kinveyClient.appData("myCollection",EventEntity.class);
 *     myAppData.get(appData().query, new KinveyListCallback<EventEntity> {
 *         public void onFailure(Throwable t) { ... }
 *         public void onSuccess(EventEntity[] entities) { ... }
 *     });
 * }
 * </pre>
 * </p>
 * <p/>
 *
 * @author mjsalinger
 * @author edwardf
 * @since 2.0
 * @version $Id: $
 */
public class AsyncAppData<T> extends AppData<T> {



    //Every AbstractClient Request wrapper provided by the core AppData gets a KEY here.
    //The below declared methodMap will map this key to a an appropriate method wrapper in the core AppData.
    //This makes it very easy to add new wrappers, and allows for a single implementation of an async client request.
    private static final String KEY_GET_BY_ID = "KEY_GET_BY_ID";
    private static final String KEY_GET_BY_QUERY = "KEY_GET_BY_QUERY";
    private static final String KEY_GET_ALL = "KEY_GET_ALL";
    private static final String KEY_GET_BY_ID_WITH_REFERENCES = "KEY_GET_BY_ID_WITH_REFERENCES";
    private static final String KEY_GET_QUERY_WITH_REFERENCES = "KEY_GET_QUERY_WITH_REFERENCES";
    private static final String KEY_GET_BY_ID_WITH_REFERENCES_WRAPPER = "KEY_GET_BY_ID_WITH_REFERENCES_WRAPPER";
    private static final String KEY_GET_BY_QUERY_WITH_REFERENCES_WRAPPER = "KEY_GET_BY_QUERY_WITH_REFERENCES_WRAPPER";
    private static final String KEY_DELETE_BY_ID ="KEY_DELETE_BY_ID";
    private static final String KEY_DELETE_BY_QUERY = "KEY_DELETE_BY_QUERY";
    private static final String KEY_COUNT = "KEY_COUNT";
    private static final String KEY_SUM = "KEY_SUM";
    private static final String KEY_MAX = "KEY_MAX";
    private static final String KEY_MIN = "KEY_MIN";
    private static final String KEY_AVERAGE = "KEY_AVERAGE";

    private static Map<String, Method> methodMap;







    /** Constructor to instantiate the AppData class.
     *
     * @param collectionName Name of the appData collection
     * @param myClass        Class Type to marshall data between.
     */
    AsyncAppData(String collectionName, Class myClass, AbstractClient client) {
        super(collectionName, myClass, client);
        loadMethodMap();
    }

    private void loadMethodMap(){
        Map<String, Method> tempMap = new HashMap<String, Method>();
        try{
            tempMap.put(KEY_GET_BY_ID, AppData.class.getMethod("getEntityBlocking", new Class[]{String.class}));
            tempMap.put(KEY_GET_BY_QUERY, AppData.class.getMethod("getBlocking", new Class[]{Query.class}));
            tempMap.put(KEY_GET_ALL, AppData.class.getMethod("getBlocking", new Class[]{}));
            tempMap.put(KEY_DELETE_BY_ID, AppData.class.getMethod("deleteBlocking", new Class[]{String.class}));
            tempMap.put(KEY_DELETE_BY_QUERY, AppData.class.getMethod("deleteBlocking", new Class[]{Query.class}));
            tempMap.put(KEY_COUNT, AppData.class.getMethod("countBlocking", new Class[]{ArrayList.class, Query.class}));
            tempMap.put(KEY_SUM, AppData.class.getMethod("sumBlocking", new Class[]{ArrayList.class, String.class, Query.class}));
            tempMap.put(KEY_MAX, AppData.class.getMethod("maxBlocking", new Class[]{ArrayList.class, String.class, Query.class}));
            tempMap.put(KEY_MIN, AppData.class.getMethod("minBlocking", new Class[]{ArrayList.class, String.class, Query.class}));
            tempMap.put(KEY_AVERAGE, AppData.class.getMethod("averageBlocking", new Class[]{ArrayList.class, String.class, Query.class}));
            tempMap.put(KEY_GET_BY_ID_WITH_REFERENCES, AppData.class.getMethod("getEntityBlocking", new Class[]{String.class, String[].class, int.class, boolean.class}));
            tempMap.put(KEY_GET_QUERY_WITH_REFERENCES, AppData.class.getMethod("getBlocking", new Class[]{Query.class, String[].class, int.class, boolean.class}));
            tempMap.put(KEY_GET_BY_ID_WITH_REFERENCES_WRAPPER, AppData.class.getMethod("getEntityBlocking", new Class[]{String.class, String[].class} ));
            tempMap.put(KEY_GET_BY_QUERY_WITH_REFERENCES_WRAPPER, AppData.class.getMethod("getBlocking", new Class[]{Query.class, String[].class}));


        }catch (NoSuchMethodException e){
            System.out.println("CHECK METHOD MAP, no such method is declared in AppData!");
            e.printStackTrace();
        }

        methodMap = Collections.unmodifiableMap(tempMap);
    }

    /**
     * Asynchronous request to fetch a single Entity by ID.
     * <p>
     * Constructs an asynchronous request to fetch a single Entity by its Entity ID.  Returns an instance of that Entity
     * via KinveyClientCallback<T>
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     AppData<EventEntity> myAppData = kinveyClient.appData("myCollection", EventEntity.class).get("123",
     *             new KinveyClientCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity entity) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param entityID entityID to fetch
     * @param callback either successfully returns list of resolved entities or an error
     */
    public void getEntity(String entityID, KinveyClientCallback<T> callback)  {
        new AppDataRequest(methodMap.get(KEY_GET_BY_ID), callback, entityID).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Asynchronous request to fetch an array of Entities using an array of _ids.
     * <p>
     * Constructs an asynchronous request to fetch an Array of Entities, filtering by the provided list of _ids.  Uses
     * KinveyListCallback<T> to return an Array of type T.  This method uses a Query {@link com.kinvey.java.Query}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     AppData<EventEntity> myAppData = kinveyClient.appData("myCollection", EventEntity.class);
     *     myAppData.get(new String[]{"189472023", "10193583"}, new KinveyListCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param ids A list of _ids to query by.
     * @param callback either successfully returns list of resolved entities or an error
     */
    public void get(String[] ids, KinveyListCallback<T> callback){
        Preconditions.checkNotNull(ids, "ids must not be null.");
        Query q = new Query();
        q.in("_id", ids);
        this.get(q, callback);
    }



    /**
     * Asynchronous request to fetch an array of Entities using a Query object.
     * <p>
     * Constructs an asynchronous request to fetch an Array of Entities, filtering by a Query object.  Uses
     * KinveyListCallback<T> to return an Array of type T.  Queries can be constructed with {@link com.kinvey.java.Query}.
     * An empty Query object will return all items in the collection.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     AppData<EventEntity> myAppData = kinveyClient.appData("myCollection", EventEntity.class);
     *     Query myQuery = new Query();
     *     myQuery.equals("age",21);
     *     myAppData.get(myQuery, new KinveyListCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param query {@link com.kinvey.java.Query} to filter the results.
     * @param callback either successfully returns list of resolved entities or an error
     */
    public void get(Query query, KinveyListCallback<T> callback){
        Preconditions.checkNotNull(query, "Query must not be null.");
        new AppDataRequest(methodMap.get(KEY_GET_BY_QUERY), callback, query).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Asynchronous request to fetch an array of all Entities in a collection.
     * <p>
     * Constructs an asynchronous request to fetch an Array of all entities in a collection.  Uses
     * KinveyListCallback<T> to return an Array of type T.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     AppData<EventEntity> myAppData = kinveyClient.appData("myCollection", EventEntity.class);
     *     myAppData.get(new KinveyListCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param callback either successfully returns list of resolved entities or an error
     */
    public void get(KinveyListCallback<T> callback) {
        new AppDataRequest(methodMap.get(KEY_GET_ALL), callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);

    }


    /**
     * Asynchronous request to fetch a single entity by _id and resolve KinveyReferences.  The resolves array defines which json keys
     * are KinveyReferences that should be resolved.
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     * {@code
     *     AppData<EventEntity> myAppData = kinveyClient.appData("myCollection", EventEntity.class);
     *     myAppData.get(myEntityID, new String[]{"ReferencedField"}, new KinveyListCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param id - the _id of the entity to retrieve
     * @param resolves - an array of json keys maintaining KinveyReferences to be resolved
     * @param callback - results of execution, either success or failure
     */
    public void getEntity(String id, String[] resolves, KinveyClientCallback<T> callback){
        new AppDataRequest(methodMap.get(KEY_GET_BY_ID_WITH_REFERENCES_WRAPPER), callback, id, resolves).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Asynchronous request to execute a query and resolve KinveyReferences.  The Query will be used to determine which entities to retrieve,
     * and the resolves array defines which json keys are KinveyReferences that should be resolved.
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     Query q = new Query();
     *     q.equals("displayName", "myDisplayName");
     *     AppData<EventEntity> myAppData = kinveyClient.appData("myCollection", EventEntity.class);
     *     myAppData.get(q, new String[]{"myReferencedField"}, new KinveyListCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param q - the {@link com.kinvey.java.Query} to execute determining which entities to retrieve.
     * @param resolves - an array of json keys maintaining KinveyReferences to be resolved
     * @param callback - results of execution, either success or failure
     */
    public void get(Query q, String[] resolves, KinveyListCallback<T> callback){
        new AppDataRequest(methodMap.get(KEY_GET_BY_QUERY_WITH_REFERENCES_WRAPPER),callback, q, resolves).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }


    /**
     * Asynchronous request to save or update an entity to a collection.
     * <p>
     * Constructs an asynchronous request to save an entity of type T to a collection.  Creates the entity if it doesn't exist, updates it if it does exist.
     * If an "_id" property is not present, the Kinvey backend will generate one.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     AppData<EventEntity> myAppData = kinveyClient.appData("myCollection", EventEntity.class);
     *     myAppData.save(entityID, new KinveyClientCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param entity The entity to save
     * @param callback KinveyClientCallback<T>
     */
    public void save(T entity, KinveyClientCallback<T> callback)  {
        Preconditions.checkNotNull(entity, "Entity cannot be null.");
        new SaveRequest(entity, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);

    }

    /**
     * Asynchronous request to delete an entity to a collection.
     * <p>
     * Creates an asynchronous request to delete a group of entities from a collection based on a Query object.  Uses KinveyDeleteCallback to return a
     * {@link com.kinvey.java.model.KinveyDeleteResponse}.  Queries can be constructed with {@link com.kinvey.java.Query}.
     * An empty Query object will delete all items in the collection.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     AppData<EventEntity> myAppData = kinveyClient.appData("myCollection", EventEntity.class);
     *     myAppData.delete(myQuery, new KinveyDeleteCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param entityID the ID to delete
     * @param callback KinveyDeleteCallback
     */
    public void delete(String entityID, KinveyDeleteCallback callback) {
        new AppDataRequest(methodMap.get(KEY_DELETE_BY_ID), callback, entityID).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Asynchronous request to delete a collection of entites from a collection by Query.
     * <p>
     * Creates an asynchronous request to delete an entity from a  collection by Entity ID.  Uses KinveyDeleteCallback to return a
     * {@link com.kinvey.java.model.KinveyDeleteResponse}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     AppData<EventEntity> myAppData = kinveyClient.appData("myCollection", EventEntity.class);
     *     Query myQuery = new Query();
     *     myQuery.equals("age",21);
     *     myAppData.delete(myQuery, new KinveyDeleteCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param query {@link com.kinvey.java.Query} to filter the results.
     * @param callback KinveyDeleteCallback
     */
    public void delete(Query query, KinveyDeleteCallback callback) {
        new AppDataRequest(methodMap.get(KEY_DELETE_BY_QUERY), callback, query).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);

    }

    /**
     * Asynchronous request to retrieve a group by COUNT on a collection or filtered collection.
     *
     * <p>
     * Generates an asynchronous request to group a collection and provide a count of records based on a field or
     * groups of fields.  The aggregate will reduce an entire collection, or a collection filtered by a {@link com.kinvey.java.Query}
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     AppData<GenericJson> aggregate = kinveyClient.appData("events", EventEntity.class);
     *     ArrayList<String> fields = new ArrayList<String>();
     *     fields.add("userName");
     *     aggregate.count(fields, null, new KinveyClientCallback<EventEntity>() {
     *         public void onSuccess(EventEntity event) { ... }
     *         public void onFailure(Throwable T) {...}
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param fields ArrayList of fields to aggregate on
     * @param query Optional query object for filtering results to aggregate on.  Set to null for entire collection.
     * @param callback KinveyClientCallback
     */
    public void count(ArrayList<String> fields, Query query, KinveyClientCallback callback) {
        new AppDataRequest(methodMap.get(KEY_COUNT), callback, fields, query).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);

    }

    /**
     * Asynchronous request to retrieveBlocking a group by SUM on a collection or filtered collection
     * <p>
     * Generates an asynchronous request to group a collection and provide a sumBlocking of records based on a field or
     * groups of fields.  The aggregate will reduce an entire collection, or a collection filtered by a {@link com.kinvey.java.Query}
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     AppData<GenericJson> aggregate = kinveyClient.appData("events", EventEntity.class");
     *     ArrayList<String> fields = new ArrayList<String>();
     *     fields.add("userName");
     *     aggregate.sumBlocking(fields, "orderTotal", null, new KinveyClientCallback<EventEntity>() {
     *         public void onSuccess(EventEntity event) { ... }
     *         public void onFailure(Throwable T) {...}
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param fields ArrayList of fields to aggregate on
     * @param sumField Field to sumBlocking
     * @param query Optional query object for filtering results to aggregate on.  Set to null for entire collection.
     * @param callback KinveyClientCallback
     */
    public void sum(ArrayList<String> fields, String sumField, Query query, KinveyClientCallback callback) {
        new  AppDataRequest(methodMap.get(KEY_SUM), callback, fields, sumField, query).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Asynchronous request to retrieve a group by MAX on a collection or filtered collection
     *
     * <p>
     * Generates an asynchronous request to group a collection and provide the max value of records based on a field or
     * groups of fields.  The aggregate will reduce an entire collection, or a collection filtered by a {@link com.kinvey.java.Query}
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     AppData<GenericJson> aggregate = kinveyClient.appData("events", EventEntity.class");
     *     ArrayList<String> fields = new ArrayList<String>();
     *     fields.add("userName");
     *     aggregate.max(fields, "orderTotal", null, new KinveyClientCallback<EventEntity>() {
     *         public void onSuccess(EventEntity event) { ... }
     *         public void onFailure(Throwable T) {...}
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param fields ArrayList of fields to aggregate on
     * @param maxField Field to get the max value from
     * @param query Optional query object for filtering results to aggregate on.  Set to null for entire collection.
     * @param callback KinveyClientCallback
     */
    public void max(ArrayList<String> fields, String maxField, Query query, KinveyClientCallback callback)  {
        new AppDataRequest(methodMap.get(KEY_MAX), callback, fields, maxField, query).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Asynchronous request to retrieve a group by MIN on a collection or filtered collection
     * <p>
     * Generates an asynchronous request to group a collection and provide the min value of records based on a field or
     * groups of fields.  The aggregate will reduce an entire collection, or a collection filtered by a {@link com.kinvey.java.Query}
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     AppData<GenericJson> aggregate = kinveyClient.appData("events", EventEntity.class");
     *     ArrayList<String> fields = new ArrayList<String>();
     *     fields.add("userName");
     *     aggregate.min(fields, "orderTotal", null, new KinveyClientCallback<EventEntity>() {
     *         public void onSuccess(EventEntity event) { ... }
     *         public void onFailure(Throwable T) {...}
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param fields ArrayList of fields to aggregate on
     * @param minField Field to get the min value from
     * @param query Optional query object for filtering results to aggregate on.  Set to null for entire collection.
     * @param callback KinveyClientCallback
     */
    public void min(ArrayList<String> fields, String minField, Query query, KinveyClientCallback callback) {
        new AppDataRequest(methodMap.get(KEY_MIN), callback, fields, minField, query).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Asynchronous request to retrieve a group by AVERAGE on a collection or filtered collection
     * <p>
     * Generates an asynchronous request to group a collection and provide the average value of records based on a field or
     * groups of fields.  The aggregate will reduce an entire collection, or a collection filtered by a {@link com.kinvey.java.Query}
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     AppData<GenericJson> aggregate = kinveyClient.appData("events", EventEntity.class);
     *     ArrayList<String> fields = new ArrayList<String>();
     *     fields.add("userName");
     *     aggregate.average(fields, "orderTotal", null, new KinveyClientCallback<EventEntity>() {
     *         public void onSuccess(EventEntity event) { ... }
     *         public void onFailure(Throwable T) {...}
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param fields ArrayList of fields to aggregate on
     * @param averageField Field to get the maxBlocking value from
     * @param query Optional query object for filtering results to aggregate on.  Set to null for entire collection.
     * @param callback KinveyClientCallback
     */
    public void average(ArrayList<String> fields, String averageField, Query query, KinveyClientCallback callback) {
        new AppDataRequest(methodMap.get(KEY_AVERAGE), callback, fields, averageField, query).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }


    /**
     * Set an {@link OfflinePolicy} on AppData, allowing requests to be executed locally
     *
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     AsyncAppData<EventEntity> offline = kinveyClient.appData("events", EventEntity.class);
     *     offline.setOffline(OfflinePolicy.ALWAYS_ONLINE, new SqlLiteOfflineStore(getContext()));
     * </pre>
     * </p>
     *
     *
     * @param policy the policy defining behavior of offline sync
     * @param store the type of store to be used, commonly {@link com.kinvey.android.offline.SqlLiteOfflineStore}
     */
    public void setOffline(OfflinePolicy policy, OfflineStore store){
        super.setOffline(policy, store);

    }


    /**
     *
     *  This implementation of an AsyncClientRequest is used to wrap the core app data API.
     *  It provides the ability to execute a given method with a given arguments using reflection.
     *
     */
    private class AppDataRequest extends AsyncClientRequest<T> {

        Method mMethod;
        Object[] args;


        public AppDataRequest(Method method, KinveyClientCallback callback, Object ... args) {
            super(callback);
            this.mMethod = method;
            this.args = args;
        }

        @Override
        public T executeAsync() throws IOException, InvocationTargetException, IllegalAccessException {
            AbstractKinveyClientRequest<T> request = (AbstractKinveyClientRequest<T>) mMethod.invoke(AsyncAppData.this, args);
            request.setCallback(getCallback());
            if (request instanceof AbstractKinveyCachedClientRequest){
                ((AbstractKinveyCachedClientRequest) request).setExecutor(this);
            }
            return request.execute();

        }
    }

    private class SaveRequest extends AsyncClientRequest<T> {
        T entity;

        public SaveRequest(T entity, KinveyClientCallback<T> callback) {
            super(callback);
            this.entity = entity;
        }

        @Override
        protected T executeAsync() throws IOException {
            return (AsyncAppData.super.saveBlocking(entity)).execute();
        }
    }


    /**
     * Method to check if the current runtime environment has an active connection to the internet, this implementation is tightly coupled with the Android Operating System
     *
     * @return true if the device is connected or connecting
     */
    @Override
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager)((Client)getClient()).getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            Log.v(Client.TAG, "Device is online");
            return true;
        }
        Log.v(Client.TAG, "Device is offline");

        return false;

    }


}
