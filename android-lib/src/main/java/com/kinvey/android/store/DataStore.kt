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
package com.kinvey.android.store

import com.google.api.client.json.GenericJson
import com.google.common.base.Preconditions
import com.kinvey.android.AsyncClientRequest
import com.kinvey.android.async.AsyncBatchPushRequest
import com.kinvey.android.async.AsyncPullRequest
import com.kinvey.android.KinveyCallbackHandler
import com.kinvey.android.KinveyLiveServiceCallbackHandler
import com.kinvey.android.async.AsyncPushRequest
import com.kinvey.android.async.AsyncRequest
import com.kinvey.android.callback.KinveyCountCallback
import com.kinvey.android.callback.KinveyDeleteCallback
import com.kinvey.android.callback.KinveyReadCallback
import com.kinvey.android.callback.KinveyPurgeCallback
import com.kinvey.android.sync.KinveyPullCallback
import com.kinvey.android.sync.KinveyPushCallback
import com.kinvey.android.sync.KinveyPushResponse
import com.kinvey.android.sync.KinveySyncCallback
import com.kinvey.java.AbstractClient
import com.kinvey.java.Logger
import com.kinvey.java.Query
import com.kinvey.java.cache.KinveyCachedClientCallback
import com.kinvey.java.core.KinveyAggregateCallback
import com.kinvey.java.core.KinveyCachedAggregateCallback
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.model.*
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.query.MongoQueryFilter
import com.kinvey.java.store.BaseDataStore
import com.kinvey.java.store.KinveyDataStoreLiveServiceCallback
import com.kinvey.java.store.KinveyLiveServiceStatus
import com.kinvey.java.store.StoreType

import java.io.IOException
import java.lang.reflect.Method
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.Locale

/**
 * Wraps the [BaseDataStore] public methods in asynchronous functionality using native Android AsyncTask.
 *
 *
 *
 *
 * This functionality can be accessed through the [DataStore] convenience method.  BaseDataStore
 * gets and saves and sync entities that extend [com.google.api.client.json.GenericJson].  A class that extends GenericJson
 * can map class members to KinveyCollection properties using [com.google.api.client.util.Key] attributes.  For example,
 * the following will map a string "city" to a Kinvey collection attributed named "city":
 *
 *
 *
 * <pre>
 * @Key
 * private String city;
</pre> *
 *
 *
 *
 * The @Key attribute also can take an optional name, which will map the member to a different attribute name in the Kinvey
 * collection.
 *
 *
 *
 * <pre>
 * @Key("_id")
 * private String customerID;
</pre> *
 *
 *
 *
 * Methods in this API use either [KinveyReadCallback] for retrieving entity sets,
 * [KinveyDeleteCallback] for deleting appData, or  the general-purpose
 * [KinveyClientCallback] used for retrieving single entities or saving Entities.
 *
 *
 *
 *
 *
 * Entity Set sample:
 * <pre>
 * `DataStore<EventEntity> dataStore = DataStore.collection("myCollection",EventEntity.class, StoreType.SYNC, myClient);
 * dataStore.find(myClient.query(), new KinveyReadCallback<EventEntity> {
 * public void onFailure(Throwable t) { ... }
 * public void onSuccess({ KinveyReadResponse}<EventEntity> readResponse) { ... }
 * });
` *
</pre> *
 *
 *
 *
 *
 * @author mjsalinger
 * @author edwardf
 * @version $Id: $
 * @since 2.0
 */
open class DataStore<T : GenericJson> : BaseDataStore<T> {

    private val kinveyApiVersion: Int
        get() {
            var version = DEFAULT_KINVEY_API_VERSION
            try {
                val versionStr = AbstractClient.kinveyApiVersion
                if (versionStr.isNotEmpty()) {
                    version = Integer.valueOf(versionStr)
                }
            } catch (t: Throwable) {
                Logger.ERROR(t.message)
            }
            return version
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
    protected constructor(collectionName: String, myClass: Class<T>, isDeltaSetCachingEnabled: Boolean, client: AbstractClient<*>, storeType: StoreType) : super(client, collectionName, myClass, storeType) {
        loadMethodMap()
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
    constructor(collectionName: String, myClass: Class<T>, client: AbstractClient<*>, storeType: StoreType, networkManager: NetworkManager<T>) : super(client, collectionName, myClass, storeType, networkManager) {
        loadMethodMap()
    }

    private fun loadMethodMap() {
        val tempMap = HashMap<String, Method>()
        try {
            tempMap[KEY_GET_BY_ID] = BaseDataStore::class.java.getMethod(BaseDataStore.FIND, String::class.java, KinveyCachedClientCallback::class.java)
            tempMap[KEY_GET_BY_QUERY] = BaseDataStore::class.java.getMethod(BaseDataStore.FIND, Query::class.java, KinveyCachedClientCallback::class.java)
            tempMap[KEY_GET_ALL] = BaseDataStore::class.java.getMethod(BaseDataStore.FIND, KinveyCachedClientCallback::class.java)
            tempMap[KEY_GET_BY_IDS] = BaseDataStore::class.java.getMethod(BaseDataStore.FIND, Iterable::class.java, KinveyCachedClientCallback::class.java)

            tempMap[KEY_GET_COUNT] = BaseDataStore::class.java.getMethod(BaseDataStore.COUNT)
            tempMap[KEY_GET_COUNT] = BaseDataStore::class.java.getMethod(BaseDataStore.COUNT, KinveyCachedClientCallback::class.java)

            tempMap[KEY_DELETE_BY_ID] = BaseDataStore::class.java.getMethod(BaseDataStore.DELETE, String::class.java)
            tempMap[KEY_DELETE_BY_QUERY] = BaseDataStore::class.java.getMethod(BaseDataStore.DELETE, Query::class.java)
            tempMap[KEY_DELETE_BY_IDS] = BaseDataStore::class.java.getMethod(BaseDataStore.DELETE, Iterable::class.java)

            tempMap[KEY_PURGE] = BaseDataStore::class.java.getMethod(BaseDataStore.PURGE)
            tempMap[KEY_PURGE_BY_QUERY] = BaseDataStore::class.java.getMethod(BaseDataStore.PURGE, Query::class.java)

            tempMap[KEY_GROUP] = BaseDataStore::class.java.getMethod(BaseDataStore.GROUP, AggregateType::class.java, ArrayList::class.java, String::class.java, Query::class.java, KinveyCachedAggregateCallback::class.java)

            tempMap[KEY_GROUP] = BaseDataStore::class.java.getMethod("group", AggregateType::class.java, ArrayList::class.java, String::class.java, Query::class.java, KinveyCachedAggregateCallback::class.java)
            tempMap[KEY_SUBSCRIBE] = BaseDataStore::class.java.getMethod("subscribe", KinveyDataStoreLiveServiceCallback::class.java)
            tempMap[KEY_UNSUBSCRIBE] = BaseDataStore::class.java.getMethod("unsubscribe")

            /*tempMap.put(KEY_GET_BY_ID_WITH_REFERENCES, NetworkManager.class.getMethod("getEntityBlocking", new Class[]{String.class, String[].class, int.class, boolean.class}));
            tempMap.put(KEY_GET_QUERY_WITH_REFERENCES, NetworkManager.class.getMethod("getBlocking", new Class[]{Query.class, String[].class, int.class, boolean.class}));
            tempMap.put(KEY_GET_BY_ID_WITH_REFERENCES_WRAPPER, NetworkManager.class.getMethod("getEntityBlocking", new Class[]{String.class, String[].class} ));
            tempMap.put(KEY_GET_BY_QUERY_WITH_REFERENCES_WRAPPER, NetworkManager.class.getMethod("getBlocking", new Class[]{Query.class, String[].class}));*/


        } catch (e: NoSuchMethodException) {
            Logger.ERROR("CHECK METHOD MAP, no such method is declared in NetworkManager!")
            //            e.printStackTrace();
        }

        methodMap = Collections.unmodifiableMap(tempMap)
    }


    /**
     * Asynchronous request to fetch a single Entity by ID with additional callback with data from cache.
     *
     *
     * Constructs an asynchronous request to fetch a single Entity by its Entity ID.  Returns an instance of that Entity
     * via KinveyClientCallback<T>
    </T> *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.CACHE, myClient).find("123",
     * new KinveyClientCallback<EventEntity> {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(EventEntity entity) { ... }
     * }, new KinveyCachedClientCallback<EventEntity>(){
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(EventEntity entity) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param entityID entityID to fetch
     * @param callback either successfully returns list of resolved entities or an error
     * @param cachedCallback either successfully returns list of resolved entities from cache or an error
     */
    @JvmOverloads
    fun find(entityID: String, callback: KinveyClientCallback<T>, cachedCallback: KinveyCachedClientCallback<T>? = null) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        Preconditions.checkNotNull(entityID, "entityID must not be null.")
        AsyncRequest(this, methodMap!![KEY_GET_BY_ID], callback, entityID, getWrappedCacheCallback(cachedCallback)).execute()
    }

    /**
     * Asynchronous request to fetch an list of Entities using an list of _ids.
     *
     *
     * Constructs an asynchronous request to fetch an List of Entities, filtering by the provided list of _ids.  Uses
     * KinveyReadCallback<T> to return an List of type T.  This method uses a Query [Query].
    </T> *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.CACHE, myClient);
     * myAppData.find(Lists.asList(new String[]{"189472023", "10193583"}), new KinveyReadCallback<EventEntity> {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(List<EventEntity> entities) { ... }
     * }, new KinveyCachedListCallback<EventEntity>(){
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(List<EventEntity> entity) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param ids A list of _ids to query by.
     * @param callback either successfully returns list of resolved entities or an error
     * @param cachedCallback either successfully returns list of resolved entities from cache or an error
     */
    @JvmOverloads
    fun find(ids: Iterable<String>, callback: KinveyReadCallback<T>, cachedCallback: KinveyCachedClientCallback<KinveyReadResponse<T>?>? = null) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        Preconditions.checkNotNull(ids, "ids must not be null.")
        AsyncRequest(this, methodMap!![KEY_GET_BY_IDS], callback, ids,
                getWrappedCacheCallback(cachedCallback)).execute()
    }


    /**
     * Asynchronous request to fetch an list of Entities using a Query object.
     *
     *
     * Constructs an asynchronous request to fetch an List of Entities, filtering by a Query object.  Uses
     * KinveyReadCallback<T> to return an List of type T.  Queries can be constructed with [Query].
     * An empty Query object will return all items in the collection.
    </T> *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.CACHE, myClient);
     * Query myQuery = myAppData.query();
     * myQuery.equals("age",21);
     * myAppData.find(myQuery, new KinveyReadCallback<EventEntity> {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(List<EventEntity> entities) { ... }
     * }, new KinveyCachedListCallback<EventEntity>(){
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(List<EventEntity> entity) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param query [Query] to filter the results.
     * @param callback either successfully returns list of resolved entities or an error
     * @param cachedCallback either successfully returns list of resolved entities from cache or an error
     */
    @JvmOverloads
    fun find(query: Query, callback: KinveyReadCallback<T>, cachedCallback: KinveyCachedClientCallback<KinveyReadResponse<T>?>? = null) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        Preconditions.checkNotNull(query, "Query must not be null.")
        AsyncRequest(this, methodMap!![KEY_GET_BY_QUERY], callback, query,
                getWrappedCacheCallback(cachedCallback)).execute()
    }

    /**
     * Asynchronous request to fetch an list of all Entities in a collection.
     *
     *
     * Constructs an asynchronous request to fetch an List of all entities in a collection.  Uses
     * KinveyReadCallback<T> to return an List of type T.
    </T> *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * myAppData.find(new KinveyReadCallback<EventEntity> {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(EventEntity[] entities) { ... }
     * }, new KinveyCachedListCallback<EventEntity>(){
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(List<EventEntity> entity) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param callback either successfully returns list of resolved entities or an error
     * @param cachedCallback either successfully returns list of resolved entities from cache or an error
     */
    @JvmOverloads
    fun find(callback: KinveyReadCallback<T>, cachedCallback: KinveyCachedClientCallback<KinveyReadResponse<T>?>? = null) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        AsyncRequest(this, methodMap!![KEY_GET_ALL], callback, getWrappedCacheCallback(cachedCallback)).execute()
    }

    /**
     * Get items count in collection
     * @param callback return items count in collection
     */
    fun count(callback: KinveyCountCallback) {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        count(callback, null)
    }

    /**
     * Get items count in collection
     * @param callback return items count in collection
     * @param cachedCallback is using with StoreType.CACHE to get items count in collection
     */
    fun count(callback: KinveyCountCallback, cachedCallback: KinveyCachedClientCallback<Int>?) {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE")
        AsyncRequest(this, methodMap!![KEY_GET_COUNT], callback, cachedCallback).execute()
    }

    /**
     * Asynchronous request to save or update an entity to a collection.
     *
     *
     * Constructs an asynchronous request to save an entity of type T to a collection.  Creates the entity if it doesn't exist, updates it if it does exist.
     * If an "_id" property is not present, the Kinvey backend will generate one.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * myAppData.save(entityID, new KinveyClientCallback<EventEntity> {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(EventEntity[] entities) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param entity The entity to save
     * @param callback KinveyClientCallback<T>
    </T> */
    fun save(entity: T, callback: KinveyClientCallback<T>) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        Preconditions.checkNotNull(entity, "Entity cannot be null.")
        Logger.INFO("Calling DataStore#save(object)")
        SaveRequest(this, entity, callback).execute()
    }

    /**
     * Asynchronous request to create an list of entities to a collection.
     *
     *
     * Constructs an asynchronous request to save a list of entities <T> to a collection.
     * Creates the entity if it doesn't exist, return error in error list of response for entity if it does exist.
     * If an "_id" property is not present, the Kinvey backend will generate one.
    </T> *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * myAppData.create(entities, new KinveyClientCallback<KinveySaveBatchResponse<EventEntity>> {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(KinveySaveBatchResponse<EventEntity> entities) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param entities The list of entities to create
     * @param callback KinveyClientCallback<KinveySaveBatchResponse<T>>
    </T> */
    fun create(entities: List<T>, callback: KinveyClientCallback<KinveySaveBatchResponse<T>>) {
        createBatch(entities, callback)
    }

    private fun createBatch(entities: List<T>, callback: KinveyClientCallback<KinveySaveBatchResponse<T>>) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        Preconditions.checkNotNull(entities, "Entity cannot be null.")
        Preconditions.checkState(entities.size > 0, "Entity list cannot be empty.")
        Preconditions.checkState(kinveyApiVersion == KINVEY_API_VERSION_5, "Kinvey api version cannot be less than 5.")
        Logger.INFO("Calling DataStore#createBatch(listObjects)")
        CreateListBatchRequest(this, entities, callback).execute()
    }

    /**
     * Asynchronous request to save or update an list of entities to a collection.
     *
     *
     * Constructs an asynchronous request to save a list of entities <T> to a collection.
     * Creates the entity if it doesn't exist, updates it if it does exist.
     * If an "_id" property is not present, the Kinvey backend will generate one.
    </T> *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * myAppData.save(entities, new KinveyClientCallback<List<EventEntity>> {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(List<EventEntity> entities) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param entities The list of entities to save
     * @param callback KinveyClientCallback<List></List><T>>
    </T> */
    @Deprecated("use {@link DataStore#create(List<T>, KinveyClientCallback<KinveySaveBatchResponse<T>>)}")
    fun save(entities: List<T>, callback: KinveyClientCallback<List<T>>) {
        if (kinveyApiVersion >= KINVEY_API_VERSION_5) {
            saveBatch(entities, callback)
        } else {
            saveV4(entities, callback)
        }
    }

    private fun saveV4(entities: List<T>, callback: KinveyClientCallback<List<T>>) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        Preconditions.checkNotNull(entities, "Entity cannot be null.")
        Logger.INFO("Calling DataStore#save(listObjects)")
        SaveListRequest(this, entities, callback).execute()
    }

    private fun saveBatch(entities: List<T>, callback: KinveyClientCallback<List<T>>) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        Preconditions.checkNotNull(entities, "Entity cannot be null.")
        Preconditions.checkState(entities.size > 0, "Entity list cannot be empty.")
        Logger.INFO("Calling DataStore#saveBatch(listObjects)")
        SaveListBatchRequest(this, entities, callback).execute()
    }

    /**
     * Asynchronous request to delete an entity to a collection.
     *
     *
     * Creates an asynchronous request to delete a group of entities from a collection based on a Query object.  Uses KinveyDeleteCallback to return a
     * [com.kinvey.java.model.KinveyDeleteResponse].  Queries can be constructed with [Query].
     * An empty Query object will delete all items in the collection.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * myAppData.delete(myQuery, new KinveyDeleteCallback {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(EventEntity[] entities) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param entityID the ID to delete
     * @param callback KinveyDeleteCallback
     */
    fun delete(entityID: String?, callback: KinveyDeleteCallback) {
        AsyncRequest(this, methodMap!![KEY_DELETE_BY_ID], callback, entityID).execute()
    }


    /**
     * Asynchronous request to delete an entities from a collection.
     *
     *
     * Creates an asynchronous request to delete a group of entities from a collection based on a passed entities ids.  Uses KinveyDeleteCallback to return a
     * [com.kinvey.java.model.KinveyDeleteResponse].
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = kDataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * List<String> ids = ...
     * myAppData.delete(ids, new KinveyDeleteCallback {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(EventEntity[] entities) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param entityIDs the ID to delete
     * @param callback KinveyDeleteCallback
     */
    fun delete(entityIDs: Iterable<String>, callback: KinveyDeleteCallback) {
        AsyncRequest(this, methodMap!![KEY_DELETE_BY_IDS], callback, entityIDs).execute()
    }

    /**
     * Asynchronous request to delete a collection of entities from a collection by Query.
     *
     *
     * Creates an asynchronous request to delete an entity from a  collection by Entity ID.  Uses KinveyDeleteCallback to return a
     * [com.kinvey.java.model.KinveyDeleteResponse].
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * Query myQuery = client.query();
     * myQuery.equals("age",21);
     * myAppData.delete(myQuery, new KinveyDeleteCallback {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(EventEntity[] entities) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param query [Query] to filter the results.
     * @param callback KinveyDeleteCallback
     */
    fun delete(query: Query, callback: KinveyDeleteCallback) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        Preconditions.checkNotNull(query, "query cannot be null.")
        AsyncRequest(this, methodMap!![KEY_DELETE_BY_QUERY], callback, query).execute()

    }

    /**
     * Asynchronous request to push a collection of entities to backend.
     *
     *
     * Creates an asynchronous request to push a collection of entities.  Uses KinveyPushCallback to return a
     * [KinveyPushResponse].
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * myAppData.push(new KinveyPushCallback() {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(KinveyPushResponse kinveyPushResponse) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param callback KinveyPushCallback
     */
    fun push(callback: KinveyPushCallback) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        if (kinveyApiVersion >= KINVEY_API_VERSION_5) {
            pushBatch(callback)
        } else {
            pushV4(callback)
        }
    }

    private fun pushV4(callback: KinveyPushCallback) {
        AsyncPushRequest(collectionName, client?.syncManager, client, storeType, networkManager, currentClass, callback).execute()
    }

    private fun pushBatch(callback: KinveyPushCallback) {
        AsyncBatchPushRequest(collectionName, client?.syncManager, client, storeType, networkManager, currentClass, callback).execute()
    }

    /**
     * Asynchronous request to pull a collection of entities from backend.
     *
     *
     * Creates an asynchronous request to pull an entity from backend.  Uses KinveyPullCallback to return a
     * [KinveyPullResponse].
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * myAppData.pull(new KinveyPullCallback {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(KinveyPullResponse kinveyPullResponse) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param callback KinveyPullCallback
     */
    fun pull(callback: KinveyPullCallback) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        this.pull(null, PAGINATION_IS_NOT_USED, callback)
    }

    /**
     * Asynchronous request to pull a collection of entities from backend using auto-pagination.
     *
     *
     * Creates an asynchronous request to pull an entity from backend.  Uses KinveyPullCallback to return a
     * [KinveyPullResponse].
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * Query myQuery = client.query();
     * myQuery.equals("age",21);
     * myAppData.pull(myQuery, true, new KinveyPullCallback {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(KinveyPullResponse kinveyPullResponse) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param query [Query] to filter the results.
     * @param isAutoPagination true if auto-pagination is used
     * @param callback KinveyPullCallback
     */
    fun pull(query: Query?, isAutoPagination: Boolean, callback: KinveyPullCallback?) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        AsyncPullRequest(this, query, isAutoPagination, callback).execute()
    }

    /**
     * Asynchronous request to pull a collection of entities from backend.
     *
     *
     * Creates an asynchronous request to pull all entity from backend.  Uses KinveyPullCallback to return a
     * [KinveyPullResponse].
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * myAppData.pull(5000, new KinveyPullCallback {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(KinveyPullResponse kinveyPullResponse) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param pageSize Page size for auto-pagination
     * @param callback KinveyPullCallback
     */
    fun pull(pageSize: Int, callback: KinveyPullCallback?) {
        Preconditions.checkArgument(pageSize >= MIN_PAGE_SIZE, "pageSize mustn't be less than $MIN_PAGE_SIZE")
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        this.pull(null, pageSize, callback)
    }

    /**
     * Asynchronous request to pull a collection of entities from backend.
     *
     *
     * Creates an asynchronous request to pull all entity from backend.  Uses KinveyPullCallback to return a
     * [KinveyPullResponse].
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * myAppData.pull(true, new KinveyPullCallback {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(KinveyPullResponse kinveyPullResponse) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param isAutoPagination true if auto-pagination is used
     * @param callback KinveyPullCallback
     */
    fun pull(isAutoPagination: Boolean, callback: KinveyPullCallback) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        this.pull(null, isAutoPagination, callback)
    }


    /**
     * Asynchronous request to pull a collection of entities from backend using auto-pagination.
     *
     *
     * Creates an asynchronous request to pull an entity from backend.  Uses KinveyPullCallback<T> to return a
     * [KinveyPullResponse].
    </T> *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * Query myQuery = client.query();
     * myQuery.equals("age", 21);
     * myAppData.pull(myQuery, 5000, new KinveyPullCallback {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(KinveyPullResponse kinveyPullResponse) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param query [Query] to filter the results.
     * @param pageSize Page size for auto-pagination
     * @param callback KinveyPullCallback
     */
    fun pull(query: Query?, pageSize: Int, callback: KinveyPullCallback?) {
        Preconditions.checkArgument(pageSize >= MIN_PAGE_SIZE, "pageSize mustn't be less than $MIN_PAGE_SIZE")
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        AsyncPullRequest(this, query, pageSize, callback).execute()
    }


    /**
     * Asynchronous request to pull a collection of entities from backend.
     *
     *
     * Creates an asynchronous request to pull an entity from backend.  Uses KinveyPullCallback<T> to return a
     * [KinveyPullResponse].
    </T> *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * Query myQuery = client.query();
     * myQuery.equals("age", 21);
     * myAppData.pull(myQuery, new KinveyPullCallback {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(KinveyPullResponse kinveyPullResponse) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param query [Query] to filter the results.
     * @param callback KinveyPullCallback
     */
    fun pull(query: Query?, callback: KinveyPullCallback) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        pull(query, PAGINATION_IS_NOT_USED, callback)
    }

    /**
     * Asynchronous request to clear all the pending requests from the sync storage
     *
     *
     * Creates an asynchronous request to clear all the pending requests from the sync storage.
     * Uses KinveyPullCallback to return a [KinveyPurgeCallback].
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * myAppData.purge(new KinveyPurgeCallback {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(Void result) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param callback KinveyPurgeCallback
     */
    fun purge(callback: KinveyPurgeCallback) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        AsyncRequest(this, methodMap!![KEY_PURGE], callback).execute()
    }

    /**
     * Asynchronous request to clear all the pending requests from the sync storage by query.
     * @param query query to filter pending requests for deleting
     * @param callback KinveyPurgeCallback
     */
    fun purge(query: Query, callback: KinveyPurgeCallback) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        AsyncRequest(this, methodMap!![KEY_PURGE_BY_QUERY], callback, query).execute()
    }


    /**
     * Asynchronous request to sync a collection of entities from a network collection by Query.
     *
     *
     * Creates an asynchronous request to sync local entities and network entries matched query from
     * a given collection by Query.  Uses KinveySyncCallback to return a
     * [com.kinvey.android.sync.KinveySyncCallback].
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * Query myQuery = client.query();
     * myQuery.equals("age",21);
     * myAppData.sync(myQuery, new KinveySyncCallback {
     * public void onSuccess(KinveyPushResponse kinveyPushResponse,
     * KinveyPullResponse kinveyPullResponse) {...}
     * void onSuccess(){...};
     * void onPullStarted(){...};
     * void onPushStarted(){...};
     * void onPullSuccess(){...};
     * void onPushSuccess(){...};
     * void onFailure(Throwable t){...};
     *
     * });
    ` *
    </pre> *
     *
     *
     * @param query [Query] to filter the results or null if you don't want to query.
     * @param callback KinveyDeleteCallback
     */
    fun sync(query: Query?, callback: KinveySyncCallback) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized")
        callback.onPushStarted()
        push(object : KinveyPushCallback {
            override fun onSuccess(pushResult: KinveyPushResponse?) {
                callback.onPushSuccess(pushResult)
                callback.onPullStarted()
                this@DataStore.pull(query, object : KinveyPullCallback {

                    override fun onSuccess(pullResult: KinveyPullResponse?) {
                        callback.onPullSuccess(pullResult)
                        callback.onSuccess(pushResult, pullResult)
                    }

                    override fun onFailure(error: Throwable?) {
                        callback.onFailure(error)

                    }
                })
            }

            override fun onFailure(error: Throwable?) {

                callback.onFailure(error)
            }

            override fun onProgress(current: Long, all: Long) {

            }
        })
    }

    /**
     * Asynchronous request to sync a collection of entities from a network collection by Query.
     *
     *
     * Creates an asynchronous request to sync local entities and network entries matched query from
     * a given collection by Query.  Uses KinveySyncCallback to return a
     * [com.kinvey.android.sync.KinveySyncCallback].
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * Query myQuery = client.query();
     * myQuery.equals("age",21);
     * myAppData.sync(myQuery, true, new KinveySyncCallback {
     * public void onSuccess(KinveyPushResponse kinveyPushResponse,
     * KinveyPullResponse kinveyPullResponse) {...}
     * void onSuccess(){...};
     * void onPullStarted(){...};
     * void onPushStarted(){...};
     * void onPullSuccess(){...};
     * void onPushSuccess(){...};
     * void onFailure(Throwable t){...};
     *
     * });
    ` *
    </pre> *
     *
     *
     * @param query [Query] to filter the results or null if you don't want to query.
     * @param callback KinveyDeleteCallback
     */
    fun sync(query: Query?, isAutoPagination: Boolean, callback: KinveySyncCallback) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized")
        callback.onPushStarted()
        push(object : KinveyPushCallback {
            override fun onSuccess(pushResult: KinveyPushResponse?) {
                callback.onPushSuccess(pushResult)
                callback.onPullStarted()
                this@DataStore.pull(query, isAutoPagination, object : KinveyPullCallback {

                    override fun onSuccess(pullResult: KinveyPullResponse?) {
                        callback.onPullSuccess(pullResult)
                        callback.onSuccess(pushResult, pullResult)
                    }

                    override fun onFailure(error: Throwable?) {
                        callback.onFailure(error)
                    }
                })
            }

            override fun onFailure(error: Throwable?) {
                callback.onFailure(error)
            }

            override fun onProgress(current: Long, all: Long) {
            }
        })
    }

    /**
     * Asynchronous request to sync a collection of entities from a network collection by Query.
     *
     *
     * Creates an asynchronous request to sync local entities and network entries matched query from
     * a given collection by Query.  Uses KinveySyncCallback to return a
     * [com.kinvey.android.sync.KinveySyncCallback].
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
     * Query myQuery = client.query();
     * myQuery.equals("age",21);
     * myAppData.sync(myQuery, 5000, new KinveySyncCallback<> {
     * public void onSuccess(KinveyPushResponse kinveyPushResponse,
     * KinveyPullResponse<T> kinveyPullResponse) {...}
     * void onSuccess(){...};
     * void onPullStarted(){...};
     * void onPushStarted(){...};
     * void onPullSuccess(){...};
     * void onPushSuccess(){...};
     * void onFailure(Throwable t){...};
     *
     * });
    ` *
    </pre> *
     *
     *
     * @param query [Query] to filter the results or null if you don't want to query.
     * @param pageSize Page size for auto-pagination
     * @param callback KinveyDeleteCallback
     */
    fun sync(query: Query?, pageSize: Int, callback: KinveySyncCallback) {
        Preconditions.checkArgument(pageSize >= MIN_PAGE_SIZE, "pageSize mustn't be less than $MIN_PAGE_SIZE")
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
        callback.onPushStarted()
        push(object : KinveyPushCallback {
            override fun onSuccess(pushResult: KinveyPushResponse?) {
                callback.onPushSuccess(pushResult)
                callback.onPullStarted()
                this@DataStore.pull(query, pageSize, object : KinveyPullCallback {

                    override fun onSuccess(pullResult: KinveyPullResponse?) {
                        callback.onPullSuccess(pullResult)
                        callback.onSuccess(pushResult, pullResult)
                    }

                    override fun onFailure(error: Throwable?) {
                        callback.onFailure(error)
                    }
                })
            }

            override fun onFailure(error: Throwable?) {
                callback.onFailure(error)
            }

            override fun onProgress(current: Long, all: Long) {

            }
        })
    }

    /**
     * Alias for [.sync] where query equals null
     *
     * @param callback callback to notify working thread on operation status update
     */
    fun sync(callback: KinveySyncCallback) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized")
        sync(null, callback)
    }

    /**
     * Alias for [.sync] where query equals null
     *
     * @param pageSize Page size for auto-pagination
     * @param callback callback to notify working thread on operation status update
     */
    fun sync(pageSize: Int, callback: KinveySyncCallback) {
        Preconditions.checkNotNull(client, "client must not be null")
        Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized")
        sync(null, pageSize, callback)
    }

    fun query(): Query {
        return Query(MongoQueryFilter.MongoQueryFilterBuilder())
    }

    /**
     * This methods gets a count of entities modified locally and pending a push to the backend
     * @return the count of sync objects for given collection
     */
    fun syncCount(): Long {
        return client?.syncManager?.getCount(collectionName) ?: 0
    }

    /**
     * Asynchronous request to collect all entities with the same value for fields,
     * and then apply a reduce function (such as count or average) on all those items.
     * @param aggregateType [AggregateType] (such as min, max, sum, count, average)
     * @param fields fields for group by
     * @param reduceField field for apply reduce function
     * @param query query to filter results
     */
    fun group(aggregateType: AggregateType, fields: ArrayList<String>, reduceField: String?, query: Query,
              callback: KinveyAggregateCallback, cachedCallback: KinveyCachedAggregateCallback?) {
        AsyncRequest(this, methodMap!![KEY_GROUP], callback, aggregateType, fields, reduceField, query, cachedCallback).execute()
    }

    /**
     * Subscribe the specified callback.
     * @param storeLiveServiceCallback [KinveyDataStoreLiveServiceCallback]
     * @param callback [<]
     */
    fun subscribe(storeLiveServiceCallback: KinveyDataStoreLiveServiceCallback<T>, callback: KinveyClientCallback<Boolean>) {
        AsyncRequest(this, methodMap!![KEY_SUBSCRIBE], callback, getWrappedLiveServiceCallback(storeLiveServiceCallback)).execute()
    }

    /**
     * Unsubscribe this instance.
     * @param callback [<]
     */
    fun unsubscribe(callback: KinveyClientCallback<Void>) {
        AsyncRequest(this, methodMap!![KEY_UNSUBSCRIBE], callback).execute()
    }

    class SaveRequest<T: GenericJson>(val store: DataStore<T>, var entity: T, callback: KinveyClientCallback<T>?) : AsyncClientRequest<T>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): T? {
            Logger.INFO("Calling SaveRequest#executeAsync()")
            return store.save(entity)
        }
    }

    class SaveListRequest<T: GenericJson>(val store: DataStore<T>, var entities: List<T>, callback: KinveyClientCallback<List<T>>?) : AsyncClientRequest<List<T>>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): List<T>? {
            Logger.INFO("Calling SaveListRequest#executeAsync()")
            return store.save(entities)
        }
    }

    class CreateListBatchRequest<T: GenericJson>(val store: DataStore<T>, var entities: List<T>, callback: KinveyClientCallback<KinveySaveBatchResponse<T>>?)
        : AsyncClientRequest<KinveySaveBatchResponse<T>>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): KinveySaveBatchResponse<T> {
            Logger.INFO("Calling CreateListBatchRequest#executeAsync()")
            return store.createBatch(entities)
        }
    }

    class SaveListBatchRequest<T: GenericJson>(val store: DataStore<T>, var entities: List<T>, callback: KinveyClientCallback<List<T>>?) : AsyncClientRequest<List<T>>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): List<T> {
            Logger.INFO("Calling SaveListRequest#executeAsync()")
            return store.saveBatch(entities)
        }
    }

    private class ThreadedKinveyCachedClientCallback<T>(private val callback: KinveyCachedClientCallback<T>) : KinveyCachedClientCallback<T> {
        var handler: KinveyCallbackHandler<T>
        init {
            handler = KinveyCallbackHandler()
        }

        override fun onSuccess(result: T?) {
            handler.onResult(result, callback)
        }

        override fun onFailure(error: Throwable?) {
            handler.onFailure(error, callback)
        }
    }

    private class ThreadedKinveyLiveService<T> internal constructor(private val callback: KinveyDataStoreLiveServiceCallback<T>) : KinveyDataStoreLiveServiceCallback<T> {
        internal var handler: KinveyLiveServiceCallbackHandler<T>

        init {
            handler = KinveyLiveServiceCallbackHandler()
        }

        override fun onNext(next: T) {
            handler.onNext(next, callback)
        }

        override fun onError(e: Exception) {
            handler.onError(e, callback)
        }

        override fun onStatus(status: KinveyLiveServiceStatus) {
            handler.onStatus(status, callback)
        }
    }

    companion object {

        private const val PAGINATION_IS_NOT_USED = 0
        private const val MIN_PAGE_SIZE = 0
        private const val MAX_MULTI_INSERT_SIZE = 100

        //Every AbstractClient Request wrapper provided by the core NetworkManager gets a KEY here.
        //The below declared methodMap will map this key to a an appropriate method wrapper in the core NetworkManager.
        //This makes it very easy to add new wrappers, and allows for a single implementation of an async client request.
        private const val KEY_GET_BY_ID = "KEY_GET_BY_ID"
        private const val KEY_GET_BY_QUERY = "KEY_GET_BY_QUERY"
        private const val KEY_GET_ALL = "KEY_GET_ALL"
        private const val KEY_GET_BY_IDS = "KEY_GET_BY_IDS"

        private const val KEY_GET_COUNT = "KEY_GET_COUNT"

        private const val KEY_DELETE_BY_ID = "KEY_DELETE_BY_ID"
        private const val KEY_DELETE_BY_QUERY = "KEY_DELETE_BY_QUERY"
        private const val KEY_DELETE_BY_IDS = "KEY_DELETE_BY_IDS"

        private const val KEY_PURGE = "KEY_PURGE"
        private const val KEY_PURGE_BY_QUERY = "KEY_PURGE_BY_QUERY"
        private const val KEY_SUBSCRIBE = "KEY_SUBSCRIBE"
        private const val KEY_UNSUBSCRIBE = "KEY_UNSUBSCRIBE"

        private const val KINVEY_API_VERSION_5 = 5

        private const val DEFAULT_KINVEY_API_VERSION = 4

        private val KEY_GROUP = "KEY_GROUP"

        private var methodMap: Map<String, Method>? = null

        @JvmStatic
        fun <T : GenericJson, C: AbstractClient<*>> collection(collectionName: String, myClass: Class<T>, storeType: StoreType, client: C?): DataStore<T> {
            Preconditions.checkNotNull(collectionName, "collectionName cannot be null.")
            Preconditions.checkNotNull(storeType, "storeType cannot be null.")
            Preconditions.checkArgument(client?.isInitialize ?: false, "client must be initialized.")
            return DataStore(collectionName, myClass, client!!.isUseDeltaCache, client!!, storeType)
        }

        private fun <T> getWrappedCacheCallback(callback: KinveyCachedClientCallback<T>?): KinveyCachedClientCallback<T>? {
            var ret: KinveyCachedClientCallback<T>? = null
            if (callback != null) {
                ret = ThreadedKinveyCachedClientCallback(callback)
            }
            return ret
        }

        private fun <T> getWrappedLiveServiceCallback(callback: KinveyDataStoreLiveServiceCallback<T>?): KinveyDataStoreLiveServiceCallback<T>? {
            var ret: ThreadedKinveyLiveService<T>? = null
            if (callback != null) {
                ret = ThreadedKinveyLiveService(callback)
            }
            return ret
        }
    }

}
/**
 * Asynchronous request to fetch a single Entity by ID.
 *
 *
 * Constructs an asynchronous request to fetch a single Entity by its Entity ID.  Returns an instance of that Entity
 * via KinveyClientCallback<T>
</T> *
 *
 *
 * Sample Usage:
 * <pre>
 * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient).find("123",
 * new KinveyClientCallback<EventEntity> {
 * public void onFailure(Throwable t) { ... }
 * public void onSuccess(EventEntity entity) { ... }
 * });
` *
</pre> *
 *
 *
 * @param entityID entityID to fetch
 * @param callback either successfully returns list of resolved entities or an error
 */
/**
 * Asynchronous request to fetch an list of Entities using an list of _ids.
 *
 *
 * Constructs an asynchronous request to fetch an List of Entities, filtering by the provided list of _ids.  Uses
 * KinveyReadCallback<T> to return an List of type T.  This method uses a Query [Query].
</T> *
 *
 *
 * Sample Usage:
 * <pre>
 * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", StoreType.SYNC, EventEntity.class, myClient);
 * myAppData.find(Lists.asList(new String[]{"189472023", "10193583"}), new KinveyReadCallback<EventEntity> {
 * public void onFailure(Throwable t) { ... }
 * public void onSuccess(List<EventEntity> entities) { ... }
 * });
` *
</pre> *
 *
 *
 * @param ids A list of _ids to query by.
 * @param callback either successfully returns list of resolved entities or an error
 */
/**
 * Asynchronous request to fetch an list of Entities using a Query object.
 *
 *
 * Constructs an asynchronous request to fetch an List of Entities, filtering by a Query object.  Uses
 * KinveyReadCallback<T> to return an List of type T.  Queries can be constructed with [Query].
 * An empty Query object will return all items in the collection.
</T> *
 *
 *
 * Sample Usage:
 * <pre>
 * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
 * Query myQuery = myAppData.query();
 * myQuery.equals("age",21);
 * myAppData.find(myQuery, new KinveyReadCallback<EventEntity> {
 * public void onFailure(Throwable t) { ... }
 * public void onSuccess(EventEntity[] entities) { ... }
 * });
` *
</pre> *
 *
 *
 * @param query [Query] to filter the results.
 * @param callback either successfully returns list of resolved entities or an error
 */
/**
 * Asynchronous request to fetch an list of all Entities in a collection.
 *
 *
 * Constructs an asynchronous request to fetch an List of all entities in a collection.  Uses
 * KinveyReadCallback<T> to return an List of type T.
</T> *
 *
 *
 * Sample Usage:
 * <pre>
 * `DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, StoreType.SYNC, myClient);
 * myAppData.find(new KinveyReadCallback<EventEntity> {
 * public void onFailure(Throwable t) { ... }
 * public void onSuccess(List<EventEntity> entities) { ... }
 * });
` *
</pre> *
 *
 *
 * @param callback either successfully returns list of resolved entities or an error
 */
