/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
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
package com.kinvey.android.store;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.api.client.json.GenericJson;
import com.google.common.base.Preconditions;
import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.async.AsyncBatchPushRequest;
import com.kinvey.android.async.AsyncPullRequest;
import com.kinvey.android.KinveyCallbackHandler;
import com.kinvey.android.KinveyLiveServiceCallbackHandler;
import com.kinvey.android.async.AsyncPushRequest;
import com.kinvey.android.async.AsyncRequest;
import com.kinvey.android.callback.KinveyCountCallback;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyReadCallback;
import com.kinvey.android.callback.KinveyPurgeCallback;
import com.kinvey.android.sync.KinveyPullCallback;
import com.kinvey.java.model.KinveyPullResponse;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.android.sync.KinveyPushResponse;
import com.kinvey.android.sync.KinveySyncCallback;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Logger;
import com.kinvey.java.Query;
import com.kinvey.java.cache.KinveyCachedClientCallback;
import com.kinvey.java.core.KinveyAggregateCallback;
import com.kinvey.java.core.KinveyCachedAggregateCallback;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.AggregateType;
import com.kinvey.java.model.Aggregation;
import com.kinvey.java.model.KinveyReadResponse;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.query.MongoQueryFilter;
import com.kinvey.java.store.BaseDataStore;
import com.kinvey.java.store.KinveyDataStoreLiveServiceCallback;
import com.kinvey.java.store.KinveyLiveServiceStatus;
import com.kinvey.java.store.StoreType;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Wraps the {@link BaseDataStore} public methods in asynchronous functionality using native Android AsyncTask.
 * <p/>
 * <p>
 * This functionality can be accessed through the {@link DataStore} convenience method.  BaseDataStore
 * gets and saves and sync entities that extend {@link com.google.api.client.json.GenericJson}.  A class that extends GenericJson
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
 * Methods in this API use either {@link KinveyReadCallback} for retrieving entity sets,
 * {@link KinveyDeleteCallback} for deleting appData, or  the general-purpose
 * {@link KinveyClientCallback} used for retrieving single entities or saving Entities.
 * </p>
 * <p/>
 * <p>
 * Entity Set sample:
 * <pre>
 * {@code
 *     DataStore<EventEntity> dataStore = DataStore.collection("myCollection",EventEntity.class, StoreType.SYNC, myClient);
 *     dataStore.find(myClient.query(), new KinveyReadCallback<EventEntity> {
 *         public void onFailure(Throwable t) { ... }
 *         public void onSuccess({@link KinveyReadResponse}<EventEntity> readResponse) { ... }
 *     });
 * }
 * </pre>
 * </p>
 * <p/>
 *
 * @author mjsalinger
 * @author edwardf
 * @version $Id: $
 * @since 2.0
 */
public class DataStore<T extends GenericJson> extends BaseDataStore<T> {

    private static final int PAGINATION_IS_NOT_USED = 0;
    private static final int MIN_PAGE_SIZE = 0;
    private static final int MAX_MULTI_INSERT_SIZE = 100;

    //Every AbstractClient Request wrapper provided by the core NetworkManager gets a KEY here.
    //The below declared methodMap will map this key to a an appropriate method wrapper in the core NetworkManager.
    //This makes it very easy to add new wrappers, and allows for a single implementation of an async client request.
    private static final String KEY_GET_BY_ID = "KEY_GET_BY_ID";
    private static final String KEY_GET_BY_QUERY = "KEY_GET_BY_QUERY";
    private static final String KEY_GET_ALL = "KEY_GET_ALL";
    private static final String KEY_GET_BY_IDS = "KEY_GET_BY_IDS";

    private static final String KEY_GET_COUNT = "KEY_GET_COUNT";

    private static final String KEY_DELETE_BY_ID = "KEY_DELETE_BY_ID";
    private static final String KEY_DELETE_BY_QUERY = "KEY_DELETE_BY_QUERY";
    private static final String KEY_DELETE_BY_IDS = "KEY_DELETE_BY_IDS";

    private static final String KEY_PURGE = "KEY_PURGE";
    private static final String KEY_PURGE_BY_QUERY = "KEY_PURGE_BY_QUERY";
    private static final String KEY_SUBSCRIBE = "KEY_SUBSCRIBE";
    private static final String KEY_UNSUBSCRIBE = "KEY_UNSUBSCRIBE";

    private static final int KINVEY_API_VERSION_5 = 5;

    /*private static final String KEY_GET_BY_ID_WITH_REFERENCES = "KEY_GET_BY_ID_WITH_REFERENCES";
    private static final String KEY_GET_QUERY_WITH_REFERENCES = "KEY_GET_QUERY_WITH_REFERENCES";
    private static final String KEY_GET_BY_ID_WITH_REFERENCES_WRAPPER = "KEY_GET_BY_ID_WITH_REFERENCES_WRAPPER";
    private static final String KEY_GET_BY_QUERY_WITH_REFERENCES_WRAPPER = "KEY_GET_BY_QUERY_WITH_REFERENCES_WRAPPER";*/


    private static final String KEY_GROUP = "KEY_GROUP";

    private static Map<String, Method> methodMap;

    private final String kinveyApiVersion = AbstractClient.KINVEY_API_VERSION;

    public Integer getKinveyApiVersion() {
        int version = 0;
        try {
            if (kinveyApiVersion != null) {
                version =  Integer.valueOf(kinveyApiVersion);
            }
        } catch (Throwable t) {
            Logger.ERROR(t.getMessage());
        }
        return version;
    }

    /**
     * Constructor to instantiate the DataStore class.
     *
     * @param collectionName    Name of the appData collection
     * @param myClass   Class Type to marshall data between
     * @param isDeltaSetCachingEnabled  Delta Cache switcher
     * @param client    Kinvey client
     * @param storeType storeType.
     */
    protected DataStore(String collectionName, Class<T> myClass, boolean isDeltaSetCachingEnabled, AbstractClient client, StoreType storeType) {
        super(client, collectionName, myClass, storeType);
        loadMethodMap();
    }

    /**
     * Constructor to instantiate the DataStore class.
     *
     * @param collectionName    Name of the appData collection
     * @param myClass   Class Type to marshall data between
     * @param client    Kinvey client
     * @param storeType StoreType parameter
     * @param networkManager    NetworkManager object.
     */
    public DataStore(String collectionName, Class<T> myClass, AbstractClient client, StoreType storeType, NetworkManager<T> networkManager) {
        super(client, collectionName, myClass, storeType, networkManager);
        loadMethodMap();
    }

    @NonNull
    public static <T extends GenericJson> DataStore<T> collection(String collectionName, Class<T> myClass, StoreType storeType, AbstractClient client) {
        Preconditions.checkNotNull(collectionName, "collectionName cannot be null.");
        Preconditions.checkNotNull(storeType, "storeType cannot be null.");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        return new DataStore<T>(collectionName, myClass, client.isUseDeltaCache(), client, storeType);
    }

    private void loadMethodMap() {
        Map<String, Method> tempMap = new HashMap<String, Method>();
        try {
            tempMap.put(KEY_GET_BY_ID, BaseDataStore.class.getMethod(FIND, String.class, KinveyCachedClientCallback.class));
            tempMap.put(KEY_GET_BY_QUERY, BaseDataStore.class.getMethod(FIND, Query.class, KinveyCachedClientCallback.class));
            tempMap.put(KEY_GET_ALL, BaseDataStore.class.getMethod(FIND, KinveyCachedClientCallback.class));
            tempMap.put(KEY_GET_BY_IDS, BaseDataStore.class.getMethod(FIND, Iterable.class, KinveyCachedClientCallback.class));

            tempMap.put(KEY_GET_COUNT, BaseDataStore.class.getMethod(COUNT));
            tempMap.put(KEY_GET_COUNT, BaseDataStore.class.getMethod(COUNT, KinveyCachedClientCallback.class));

            tempMap.put(KEY_DELETE_BY_ID, BaseDataStore.class.getMethod(DELETE, String.class));
            tempMap.put(KEY_DELETE_BY_QUERY, BaseDataStore.class.getMethod(DELETE, Query.class));
            tempMap.put(KEY_DELETE_BY_IDS, BaseDataStore.class.getMethod(DELETE, Iterable.class));

            tempMap.put(KEY_PURGE, BaseDataStore.class.getMethod(PURGE));
            tempMap.put(KEY_PURGE_BY_QUERY, BaseDataStore.class.getMethod(PURGE, Query.class));

            tempMap.put(KEY_GROUP, BaseDataStore.class.getMethod(GROUP, AggregateType.class, ArrayList.class, String.class, Query.class, KinveyCachedAggregateCallback.class));

            tempMap.put(KEY_GROUP, BaseDataStore.class.getMethod("group", AggregateType.class, ArrayList.class, String.class, Query.class, KinveyCachedAggregateCallback.class));
            tempMap.put(KEY_SUBSCRIBE, BaseDataStore.class.getMethod("subscribe", KinveyDataStoreLiveServiceCallback.class));
            tempMap.put(KEY_UNSUBSCRIBE, BaseDataStore.class.getMethod("unsubscribe"));

            /*tempMap.put(KEY_GET_BY_ID_WITH_REFERENCES, NetworkManager.class.getMethod("getEntityBlocking", new Class[]{String.class, String[].class, int.class, boolean.class}));
            tempMap.put(KEY_GET_QUERY_WITH_REFERENCES, NetworkManager.class.getMethod("getBlocking", new Class[]{Query.class, String[].class, int.class, boolean.class}));
            tempMap.put(KEY_GET_BY_ID_WITH_REFERENCES_WRAPPER, NetworkManager.class.getMethod("getEntityBlocking", new Class[]{String.class, String[].class} ));
            tempMap.put(KEY_GET_BY_QUERY_WITH_REFERENCES_WRAPPER, NetworkManager.class.getMethod("getBlocking", new Class[]{Query.class, String[].class}));*/


        } catch (NoSuchMethodException e) {
        	Logger.ERROR("CHECK METHOD MAP, no such method is declared in NetworkManager!");
//            e.printStackTrace();
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
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient).find("123",
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
    public void find(@NonNull String entityID, @NonNull KinveyClientCallback<T> callback)  {
        find(entityID, callback, null);
    }


    /**
     * Asynchronous request to fetch a single Entity by ID with additional callback with data from cache.
     * <p>
     * Constructs an asynchronous request to fetch a single Entity by its Entity ID.  Returns an instance of that Entity
     * via KinveyClientCallback<T>
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.CACHE, myClient).find("123",
     *             new KinveyClientCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity entity) { ... }
     *     }, new KinveyCachedClientCallback<EventEntity>(){
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity entity) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param entityID entityID to fetch
     * @param callback either successfully returns list of resolved entities or an error
     * @param cachedCallback either successfully returns list of resolved entities from cache or an error
     *
     */
    public void find(@NonNull String entityID, @NonNull KinveyClientCallback<T> callback, @Nullable final KinveyCachedClientCallback<T> cachedCallback)  {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        Preconditions.checkNotNull(entityID, "entityID must not be null.");
        new AsyncRequest<>(this, methodMap.get(KEY_GET_BY_ID), callback, entityID, getWrappedCacheCallback(cachedCallback)).execute();
    }

    /**
     * Asynchronous request to fetch an list of Entities using an list of _ids.
     * <p>
     * Constructs an asynchronous request to fetch an List of Entities, filtering by the provided list of _ids.  Uses
     * KinveyReadCallback<T> to return an List of type T.  This method uses a Query {@link Query}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", StoreType.SYNC, EventEntity.class, myClient);
     *     myAppData.find(Lists.asList(new String[]{"189472023", "10193583"}), new KinveyReadCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(List<EventEntity> entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param ids A list of _ids to query by.
     * @param callback either successfully returns list of resolved entities or an error
     */
    public void find(@NonNull Iterable<String> ids, @NonNull KinveyReadCallback<T> callback){
        find(ids, callback, null);
    }

    /**
     * Asynchronous request to fetch an list of Entities using an list of _ids.
     * <p>
     * Constructs an asynchronous request to fetch an List of Entities, filtering by the provided list of _ids.  Uses
     * KinveyReadCallback<T> to return an List of type T.  This method uses a Query {@link Query}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.CACHE, myClient);
     *     myAppData.find(Lists.asList(new String[]{"189472023", "10193583"}), new KinveyReadCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(List<EventEntity> entities) { ... }
     *     }, new KinveyCachedListCallback<EventEntity>(){
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(List<EventEntity> entity) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param ids A list of _ids to query by.
     * @param callback either successfully returns list of resolved entities or an error
     * @param cachedCallback either successfully returns list of resolved entities from cache or an error
     */
    public void find(@NonNull Iterable<String> ids, @NonNull KinveyReadCallback<T> callback, @Nullable KinveyCachedClientCallback<KinveyReadResponse<T>> cachedCallback){
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        Preconditions.checkNotNull(ids, "ids must not be null.");
        new AsyncRequest<>(this, methodMap.get(KEY_GET_BY_IDS), callback, ids,
                getWrappedCacheCallback(cachedCallback)).execute();
    }


    /**
     * Asynchronous request to fetch an list of Entities using a Query object.
     * <p>
     * Constructs an asynchronous request to fetch an List of Entities, filtering by a Query object.  Uses
     * KinveyReadCallback<T> to return an List of type T.  Queries can be constructed with {@link Query}.
     * An empty Query object will return all items in the collection.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     Query myQuery = myAppData.query();
     *     myQuery.equals("age",21);
     *     myAppData.find(myQuery, new KinveyReadCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param query {@link Query} to filter the results.
     * @param callback either successfully returns list of resolved entities or an error
     */
    public void find(@NonNull Query query, @NonNull KinveyReadCallback<T> callback){
        find(query, callback, null);
    }


    /**
     * Asynchronous request to fetch an list of Entities using a Query object.
     * <p>
     * Constructs an asynchronous request to fetch an List of Entities, filtering by a Query object.  Uses
     * KinveyReadCallback<T> to return an List of type T.  Queries can be constructed with {@link Query}.
     * An empty Query object will return all items in the collection.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.CACHE, myClient);
     *     Query myQuery = myAppData.query();
     *     myQuery.equals("age",21);
     *     myAppData.find(myQuery, new KinveyReadCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(List<EventEntity> entities) { ... }
     *     }, new KinveyCachedListCallback<EventEntity>(){
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(List<EventEntity> entity) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param query {@link Query} to filter the results.
     * @param callback either successfully returns list of resolved entities or an error
     * @param cachedCallback either successfully returns list of resolved entities from cache or an error
     */
    public void find(@NonNull Query query, @NonNull KinveyReadCallback<T> callback, @Nullable KinveyCachedClientCallback<KinveyReadResponse<T>> cachedCallback){
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        Preconditions.checkNotNull(query, "Query must not be null.");
        new AsyncRequest<>(this, methodMap.get(KEY_GET_BY_QUERY), callback, query,
                getWrappedCacheCallback(cachedCallback)).execute();
    }

    /**
     * Asynchronous request to fetch an list of all Entities in a collection.
     * <p>
     * Constructs an asynchronous request to fetch an List of all entities in a collection.  Uses
     * KinveyReadCallback<T> to return an List of type T.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     myAppData.find(new KinveyReadCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(List<EventEntity> entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param callback either successfully returns list of resolved entities or an error
     */
    public void find(@NonNull KinveyReadCallback<T> callback) {
        find(callback, null);
    }

    /**
     * Asynchronous request to fetch an list of all Entities in a collection.
     * <p>
     * Constructs an asynchronous request to fetch an List of all entities in a collection.  Uses
     * KinveyReadCallback<T> to return an List of type T.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     myAppData.find(new KinveyReadCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     }, new KinveyCachedListCallback<EventEntity>(){
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(List<EventEntity> entity) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param callback either successfully returns list of resolved entities or an error
     * @param cachedCallback either successfully returns list of resolved entities from cache or an error
     */
    public void find(@NonNull KinveyReadCallback<T> callback, @Nullable KinveyCachedClientCallback<KinveyReadResponse<T>> cachedCallback) {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        new AsyncRequest<>(this, methodMap.get(KEY_GET_ALL), callback, getWrappedCacheCallback(cachedCallback)).execute();
    }

    /**
     * Get items count in collection
     * @param callback return items count in collection
     */
    public void count(@NonNull KinveyCountCallback callback) {
        Preconditions.checkNotNull(client, "client must not be null.");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        count(callback, null);
    }

    /**
     * Get items count in collection
     * @param callback return items count in collection
     * @param cachedCallback is using with StoreType.CACHE to get items count in collection
     */
    public void count(@NonNull KinveyCountCallback callback, @Nullable KinveyCachedClientCallback<Integer> cachedCallback) {
        Preconditions.checkNotNull(client, "client must not be null.");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE");
        new AsyncRequest<Integer>(this, methodMap.get(KEY_GET_COUNT), callback, cachedCallback).execute();
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
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
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
    public void save(@NonNull T entity, @NonNull KinveyClientCallback<T> callback)  {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        Preconditions.checkNotNull(entity, "Entity cannot be null.");
        Logger.INFO("Calling DataStore#save(object)");
        new SaveRequest(entity, callback).execute();
    }

    /**
     * Asynchronous request to save or update an list of entities to a collection.
     * <p>
     * Constructs an asynchronous request to save a list of entities <T> to a collection.
     * Creates the entity if it doesn't exist, updates it if it does exist.
     * If an "_id" property is not present, the Kinvey backend will generate one.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     myAppData.save(entities, new KinveyClientCallback<List<EventEntity>> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(List<EventEntity> entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param entities The list of entities to save
     * @param callback KinveyClientCallback<List<T>>
     */
    public void save(@NonNull List<T> entities, @NonNull KinveyClientCallback<List<T>> callback)  {
         if (getKinveyApiVersion() >= KINVEY_API_VERSION_5) {
             saveBatch(entities, callback);
         } else {
             saveV4(entities, callback);
         }
    }

    private void saveV4(@NonNull List<T> entities, @NonNull KinveyClientCallback<List<T>> callback)  {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        Preconditions.checkNotNull(entities, "Entity cannot be null.");
        Logger.INFO("Calling DataStore#save(listObjects)");
        new SaveListRequest(entities, callback).execute();
    }

    private void saveBatch(@NonNull List<T> entities, @NonNull KinveyClientCallback<List<T>> callback)  {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        Preconditions.checkNotNull(entities, "Entity cannot be null.");
        Preconditions.checkState(entities.size() > 0,"Entity list cannot be empty.");
        //Preconditions.checkPositionIndex(entities.size(), MAX_MULTI_INSERT_SIZE,
        //     String.format(Locale.US, "Reached maximum of %d items per request.", MAX_MULTI_INSERT_SIZE));
        Logger.INFO("Calling DataStore#saveBatch(listObjects)");
        new SaveListBatchRequest(entities, callback).execute();
    }

    /**
     * Asynchronous request to delete an entity to a collection.
     * <p>
     * Creates an asynchronous request to delete a group of entities from a collection based on a Query object.  Uses KinveyDeleteCallback to return a
     * {@link com.kinvey.java.model.KinveyDeleteResponse}.  Queries can be constructed with {@link Query}.
     * An empty Query object will delete all items in the collection.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
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
    public void delete(@NonNull String entityID, @NonNull KinveyDeleteCallback callback) {
        new AsyncRequest<Integer>(this, methodMap.get(KEY_DELETE_BY_ID), callback, entityID).execute();
    }


    /**
     * Asynchronous request to delete an entities from a collection.
     * <p>
     * Creates an asynchronous request to delete a group of entities from a collection based on a passed entities ids.  Uses KinveyDeleteCallback to return a
     * {@link com.kinvey.java.model.KinveyDeleteResponse}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = kDataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     List<String> ids = ...
     *     myAppData.delete(ids, new KinveyDeleteCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param entityIDs the ID to delete
     * @param callback KinveyDeleteCallback
     */
    public void delete(@NonNull Iterable<String> entityIDs, @NonNull KinveyDeleteCallback callback) {
        new AsyncRequest<Integer>(this, methodMap.get(KEY_DELETE_BY_IDS), callback, entityIDs).execute();
    }

    /**
     * Asynchronous request to delete a collection of entities from a collection by Query.
     * <p>
     * Creates an asynchronous request to delete an entity from a  collection by Entity ID.  Uses KinveyDeleteCallback to return a
     * {@link com.kinvey.java.model.KinveyDeleteResponse}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     Query myQuery = client.query();
     *     myQuery.equals("age",21);
     *     myAppData.delete(myQuery, new KinveyDeleteCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param query {@link Query} to filter the results.
     * @param callback KinveyDeleteCallback
     */
    public void delete(@NonNull Query query, @NonNull KinveyDeleteCallback callback) {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        Preconditions.checkNotNull(query, "query cannot be null.");
        new AsyncRequest<Integer>(this, methodMap.get(KEY_DELETE_BY_QUERY), callback, query).execute();

    }

    /**
     * Asynchronous request to push a collection of entities to backend.
     * <p>
     * Creates an asynchronous request to push a collection of entities.  Uses KinveyPushCallback to return a
     * {@link KinveyPushResponse}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     myAppData.push(new KinveyPushCallback() {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(KinveyPushResponse kinveyPushResponse) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param callback KinveyPushCallback
     */
    public void push(@NonNull KinveyPushCallback callback) {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        if (getKinveyApiVersion() >= KINVEY_API_VERSION_5) {
            pushBatch(callback);
        } else {
            pushV4(callback);
        }
    }

    private void pushV4(@NonNull KinveyPushCallback callback) {
        new AsyncPushRequest<T>(getCollectionName(), client.getSyncManager(), client, storeType, networkManager, getCurrentClass(), callback).execute();
    }

    private void pushBatch(@NonNull KinveyPushCallback callback) {
        new AsyncBatchPushRequest<T>(getCollectionName(), client.getSyncManager(), client, storeType, networkManager, getCurrentClass(), callback).execute();
    }

    /**
     * Asynchronous request to pull a collection of entities from backend.
     * <p>
     * Creates an asynchronous request to pull an entity from backend.  Uses KinveyPullCallback to return a
     * {@link KinveyPullResponse}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     myAppData.pull(new KinveyPullCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(KinveyPullResponse kinveyPullResponse) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param callback KinveyPullCallback
     */
    public void pull(@NonNull KinveyPullCallback callback) {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        this.pull(null, PAGINATION_IS_NOT_USED, callback);
    }

    /**
     * Asynchronous request to pull a collection of entities from backend using auto-pagination.
     * <p>
     * Creates an asynchronous request to pull an entity from backend.  Uses KinveyPullCallback to return a
     * {@link KinveyPullResponse}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     Query myQuery = client.query();
     *     myQuery.equals("age",21);
     *     myAppData.pull(myQuery, true, new KinveyPullCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(KinveyPullResponse kinveyPullResponse) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param query {@link Query} to filter the results.
     * @param isAutoPagination true if auto-pagination is used
     * @param callback KinveyPullCallback
     */
    public void pull(@Nullable Query query, boolean isAutoPagination, @NonNull KinveyPullCallback callback) {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        new AsyncPullRequest(this, query, isAutoPagination, callback).execute();
    }

    /**
     * Asynchronous request to pull a collection of entities from backend.
     * <p>
     * Creates an asynchronous request to pull all entity from backend.  Uses KinveyPullCallback to return a
     * {@link KinveyPullResponse}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     myAppData.pull(5000, new KinveyPullCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(KinveyPullResponse kinveyPullResponse) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param pageSize Page size for auto-pagination
     * @param callback KinveyPullCallback
     */
    public void pull(int pageSize, @NonNull KinveyPullCallback callback) {
        Preconditions.checkArgument(pageSize >= MIN_PAGE_SIZE, "pageSize mustn't be less than " + MIN_PAGE_SIZE);
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        this.pull(null, pageSize,  callback);
    }

    /**
     * Asynchronous request to pull a collection of entities from backend.
     * <p>
     * Creates an asynchronous request to pull all entity from backend.  Uses KinveyPullCallback to return a
     * {@link KinveyPullResponse}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     myAppData.pull(true, new KinveyPullCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(KinveyPullResponse kinveyPullResponse) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param isAutoPagination true if auto-pagination is used
     * @param callback KinveyPullCallback
     */
    public void pull(boolean isAutoPagination, @NonNull KinveyPullCallback callback) {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        this.pull(null, isAutoPagination, callback);
    }


    /**
     * Asynchronous request to pull a collection of entities from backend using auto-pagination.
     * <p>
     * Creates an asynchronous request to pull an entity from backend.  Uses KinveyPullCallback<T> to return a
     * {@link KinveyPullResponse}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     Query myQuery = client.query();
     *     myQuery.equals("age", 21);
     *     myAppData.pull(myQuery, 5000, new KinveyPullCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(KinveyPullResponse kinveyPullResponse) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param query {@link Query} to filter the results.
     * @param pageSize Page size for auto-pagination
     * @param callback KinveyPullCallback
     */
    public void pull(@Nullable Query query, int pageSize, @NonNull KinveyPullCallback callback) {
        Preconditions.checkArgument(pageSize >= MIN_PAGE_SIZE, "pageSize mustn't be less than " + MIN_PAGE_SIZE);
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        new AsyncPullRequest(this, query, pageSize, callback).execute();
    }


    /**
     * Asynchronous request to pull a collection of entities from backend.
     * <p>
     * Creates an asynchronous request to pull an entity from backend.  Uses KinveyPullCallback<T> to return a
     * {@link KinveyPullResponse}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     Query myQuery = client.query();
     *     myQuery.equals("age", 21);
     *     myAppData.pull(myQuery, new KinveyPullCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(KinveyPullResponse kinveyPullResponse) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param query {@link Query} to filter the results.
     * @param callback KinveyPullCallback
     */
    public void pull(@Nullable Query query,@NonNull KinveyPullCallback callback) {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        pull(query, PAGINATION_IS_NOT_USED, callback);
    }

    /**
     * Asynchronous request to clear all the pending requests from the sync storage
     * <p>
     * Creates an asynchronous request to clear all the pending requests from the sync storage.
     * Uses KinveyPullCallback to return a {@link KinveyPurgeCallback}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     myAppData.purge(new KinveyPurgeCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(Void result) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param callback KinveyPurgeCallback
     */
    public void purge(@NonNull KinveyPurgeCallback callback){
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        new AsyncRequest<Void>(this, methodMap.get(KEY_PURGE), callback).execute();
    }

    /**
     * Asynchronous request to clear all the pending requests from the sync storage by query.
     * @param query query to filter pending requests for deleting
     * @param callback KinveyPurgeCallback
     */
    public void purge(@NonNull Query query, @NonNull KinveyPurgeCallback callback){
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        new AsyncRequest<Void>(this, methodMap.get(KEY_PURGE_BY_QUERY), callback, query).execute();
    }


    /**
     * Asynchronous request to sync a collection of entities from a network collection by Query.
     * <p>
     * Creates an asynchronous request to sync local entities and network entries matched query from
     * a given collection by Query.  Uses KinveySyncCallback to return a
     * {@link com.kinvey.android.sync.KinveySyncCallback}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     Query myQuery = client.query();
     *     myQuery.equals("age",21);
     *     myAppData.sync(myQuery, new KinveySyncCallback {
     *     public void onSuccess(KinveyPushResponse kinveyPushResponse,
     *         KinveyPullResponse kinveyPullResponse) {...}
     *         void onSuccess(){...};
     *         void onPullStarted(){...};
     *         void onPushStarted(){...};
     *         void onPullSuccess(){...};
     *         void onPushSuccess(){...};
     *         void onFailure(Throwable t){...};
     *
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param query {@link Query} to filter the results or null if you don't want to query.
     * @param callback KinveyDeleteCallback
     */
    public void sync(@Nullable final Query query, @NonNull final KinveySyncCallback callback) {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized");
        callback.onPushStarted();
        push(new KinveyPushCallback() {
            @Override
            public void onSuccess(final KinveyPushResponse pushResult) {
                callback.onPushSuccess(pushResult);
                callback.onPullStarted();
                DataStore.this.pull(query, new KinveyPullCallback() {

                    @Override
                    public void onSuccess(KinveyPullResponse pullResult) {
                        callback.onPullSuccess(pullResult);
                        callback.onSuccess(pushResult, pullResult);
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        callback.onFailure(error);

                    }
                });
            }

            @Override
            public void onFailure(Throwable error) {

                callback.onFailure(error);
            }

            @Override
            public void onProgress(long current, long all) {

            }
        });
    }

    /**
     * Asynchronous request to sync a collection of entities from a network collection by Query.
     * <p>
     * Creates an asynchronous request to sync local entities and network entries matched query from
     * a given collection by Query.  Uses KinveySyncCallback to return a
     * {@link com.kinvey.android.sync.KinveySyncCallback}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     Query myQuery = client.query();
     *     myQuery.equals("age",21);
     *     myAppData.sync(myQuery, true, new KinveySyncCallback {
     *     public void onSuccess(KinveyPushResponse kinveyPushResponse,
     *         KinveyPullResponse kinveyPullResponse) {...}
     *         void onSuccess(){...};
     *         void onPullStarted(){...};
     *         void onPushStarted(){...};
     *         void onPullSuccess(){...};
     *         void onPushSuccess(){...};
     *         void onFailure(Throwable t){...};
     *
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param query {@link Query} to filter the results or null if you don't want to query.
     * @param callback KinveyDeleteCallback
     */
    public void sync(@Nullable final Query query, final boolean isAutoPagination, @NonNull final KinveySyncCallback callback) {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized");
        callback.onPushStarted();
        push(new KinveyPushCallback() {
            @Override
            public void onSuccess(final KinveyPushResponse pushResult) {
                callback.onPushSuccess(pushResult);
                callback.onPullStarted();
                DataStore.this.pull(query, isAutoPagination, new KinveyPullCallback() {

                    @Override
                    public void onSuccess(KinveyPullResponse pullResult) {
                        callback.onPullSuccess(pullResult);
                        callback.onSuccess(pushResult, pullResult);
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        callback.onFailure(error);

                    }
                });
            }

            @Override
            public void onFailure(Throwable error) {

                callback.onFailure(error);
            }

            @Override
            public void onProgress(long current, long all) {

            }
        });
    }

    /**
     * Asynchronous request to sync a collection of entities from a network collection by Query.
     * <p>
     * Creates an asynchronous request to sync local entities and network entries matched query from
     * a given collection by Query.  Uses KinveySyncCallback to return a
     * {@link com.kinvey.android.sync.KinveySyncCallback}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     *     Query myQuery = client.query();
     *     myQuery.equals("age",21);
     *     myAppData.sync(myQuery, 5000, new KinveySyncCallback<> {
     *     public void onSuccess(KinveyPushResponse kinveyPushResponse,
     *         KinveyPullResponse<T> kinveyPullResponse) {...}
     *         void onSuccess(){...};
     *         void onPullStarted(){...};
     *         void onPushStarted(){...};
     *         void onPullSuccess(){...};
     *         void onPushSuccess(){...};
     *         void onFailure(Throwable t){...};
     *
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param query {@link Query} to filter the results or null if you don't want to query.
     * @param pageSize Page size for auto-pagination
     * @param callback KinveyDeleteCallback
     */
    public void sync(@Nullable final Query query, final int pageSize, @NonNull final KinveySyncCallback callback) {
        Preconditions.checkArgument(pageSize >= MIN_PAGE_SIZE, "pageSize mustn't be less than " + MIN_PAGE_SIZE);
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        callback.onPushStarted();
        push(new KinveyPushCallback() {
            @Override
            public void onSuccess(final KinveyPushResponse pushResult) {
                callback.onPushSuccess(pushResult);
                callback.onPullStarted();
                DataStore.this.pull(query, pageSize, new KinveyPullCallback() {

                    @Override
                    public void onSuccess(KinveyPullResponse pullResult) {
                        callback.onPullSuccess(pullResult);
                        callback.onSuccess(pushResult, pullResult);
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        callback.onFailure(error);

                    }
                });
            }

            @Override
            public void onFailure(Throwable error) {
                callback.onFailure(error);
            }

            @Override
            public void onProgress(long current, long all) {

            }
        });
    }

    /**
     * Alias for {@link #sync(Query, KinveySyncCallback)} where query equals null
     *
     * @param callback callback to notify working thread on operation status update
     */
    public void sync(@NonNull final KinveySyncCallback callback) {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized");
        sync(null, callback);
    }

    /**
     * Alias for {@link #sync(Query, KinveySyncCallback)} where query equals null
     *
     * @param pageSize Page size for auto-pagination
     * @param callback callback to notify working thread on operation status update
     */
    public void sync(final int pageSize, @NonNull final KinveySyncCallback callback) {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized");
        sync(null, pageSize, callback);
    }

    @NonNull
    public Query query() {
        return new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
    }

    /**
     * This methods gets a count of entities modified locally and pending a push to the backend
     * @return the count of sync objects for given collection
     */
    public long syncCount() {
        return client.getSyncManager().getCount(getCollectionName());
    }

    /**
     * Asynchronous request to collect all entities with the same value for fields,
     * and then apply a reduce function (such as count or average) on all those items.
     * @param aggregateType {@link AggregateType} (such as min, max, sum, count, average)
     * @param fields fields for group by
     * @param reduceField field for apply reduce function
     * @param query query to filter results
     */
    public void group(@NonNull AggregateType aggregateType, @NonNull ArrayList<String> fields, @NonNull String reduceField, @NonNull Query query,
                      @NonNull KinveyAggregateCallback callback, @Nullable KinveyCachedAggregateCallback cachedCallback) {
        new AsyncRequest<Aggregation>(this, methodMap.get(KEY_GROUP), callback, aggregateType, fields, reduceField, query, cachedCallback).execute();
    }

    /**
     * Subscribe the specified callback.
     * @param storeLiveServiceCallback {@link KinveyDataStoreLiveServiceCallback}
     * @param callback {@link KinveyClientCallback<Boolean>}
     */
    public void subscribe(@NonNull KinveyDataStoreLiveServiceCallback<T> storeLiveServiceCallback, @NonNull KinveyClientCallback<Boolean> callback) {
        new AsyncRequest<Boolean>(this, methodMap.get(KEY_SUBSCRIBE), callback, getWrappedLiveServiceCallback(storeLiveServiceCallback)).execute();
    }

    /**
     * Unsubscribe this instance.
     * @param callback {@link KinveyClientCallback<Void>}
     */
    public void unsubscribe(@NonNull KinveyClientCallback<Void> callback) {
        new AsyncRequest<Void>(this, methodMap.get(KEY_UNSUBSCRIBE), callback).execute();
    }

    private class SaveRequest extends AsyncClientRequest<T> {
        T entity;

        public SaveRequest(T entity, KinveyClientCallback<T> callback) {
            super(callback);
            this.entity = entity;
        }

        @Override
        protected T executeAsync() throws IOException {
            Logger.INFO("Calling SaveRequest#executeAsync()");
            return (DataStore.super.save(entity));
        }
    }

    private class SaveListRequest extends AsyncClientRequest<List<T>> {
        List<T> entities;

        SaveListRequest(List<T> entities, KinveyClientCallback<List<T>> callback) {
            super(callback);
            this.entities = entities;
        }

        @Override
        protected List<T> executeAsync() throws IOException {
            Logger.INFO("Calling SaveListRequest#executeAsync()");
            return (DataStore.super.save(entities));
        }
    }

    private class SaveListBatchRequest extends AsyncClientRequest<List<T>> {
        List<T> entities;

        SaveListBatchRequest(List<T> entities, KinveyClientCallback<List<T>> callback) {
            super(callback);
            this.entities = entities;
        }

        @Override
        protected List<T> executeAsync() throws IOException {
            Logger.INFO("Calling SaveListRequest#executeAsync()");
            return (DataStore.super.saveBatch(entities));
        }
    }

    private static <T> KinveyCachedClientCallback<T> getWrappedCacheCallback(KinveyCachedClientCallback<T> callback) {
        KinveyCachedClientCallback<T> ret = null;
        if (callback != null) {
            ret = new ThreadedKinveyCachedClientCallback<T>(callback);
        }
        return ret;
    }

    private static <T> KinveyDataStoreLiveServiceCallback<T> getWrappedLiveServiceCallback(KinveyDataStoreLiveServiceCallback<T> callback) {
        ThreadedKinveyLiveService<T> ret = null;
        if (callback != null) {
            ret = new ThreadedKinveyLiveService<T>(callback);
        }
        return ret;
    }

    private static class ThreadedKinveyCachedClientCallback<T> implements KinveyCachedClientCallback<T> {

        private KinveyCachedClientCallback<T> callback;
        KinveyCallbackHandler<T> handler;


        ThreadedKinveyCachedClientCallback(KinveyCachedClientCallback<T> callback) {
            handler = new KinveyCallbackHandler<T>();
            this.callback = callback;
        }

        @Override
        public void onSuccess(T result) {
            handler.onResult(result, callback);
        }

        @Override
        public void onFailure(Throwable error) {
            handler.onFailure(error, callback);

        }
    }

    private static class ThreadedKinveyLiveService<T> implements KinveyDataStoreLiveServiceCallback<T> {

        private KinveyDataStoreLiveServiceCallback<T> callback;
        KinveyLiveServiceCallbackHandler<T> handler;

        ThreadedKinveyLiveService(KinveyDataStoreLiveServiceCallback<T> callback) {
            handler = new KinveyLiveServiceCallbackHandler<T>();
            this.callback = callback;
        }

        @Override
        public void onNext(T next) {
            handler.onNext(next, callback);
        }

        @Override
        public void onError(Exception e) {
            handler.onError(e, callback);
        }

        @Override
        public void onStatus(KinveyLiveServiceStatus status) {
            handler.onStatus(status, callback);
        }
    }

}
