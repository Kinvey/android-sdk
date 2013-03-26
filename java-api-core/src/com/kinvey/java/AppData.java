/*
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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
package com.kinvey.java;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.kinvey.java.cache.AbstractKinveyCachedClientRequest;
import com.kinvey.java.cache.Cache;
import com.kinvey.java.cache.CachePolicy;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.model.AggregateEntity;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.query.MongoQueryFilter;

/**
 * Class for managing appData access to the Kinvey backend.
 *
 * @author mjsalinger
 * @author m0rganic
 * @author edwardf
 * @since 2.0.2
 */
public class AppData<T> {


    private String collectionName;
    private Class<T> myClass;
    private AbstractClient client;

    public static final String ID_FIELD_NAME = "_id";

    private Cache<String , T> cache = null;
    private CachePolicy policy = CachePolicy.NOCACHE;
    private Object cacheLock = new Object();

    /**
     * Constructor to instantiate the AppData class.
     *
     * @param collectionName Name of the appData collection
     * @param myClass Class Type to marshall data between.
     */
    protected AppData(String collectionName, Class<T> myClass, AbstractClient client) {
        Preconditions.checkNotNull(collectionName, "collectionName must not be null.");
        Preconditions.checkNotNull(client, "client must not be null.");

        this.collectionName = collectionName;
        this.myClass = myClass;
        this.client = client;
    }

    /**
     * Sets the collectionName
     * @param collectionName Name of the appData collection.
     */
    public void setCollectionName(String collectionName) {
        Preconditions.checkNotNull(collectionName,"collectionName must not be null.");
        this.collectionName = collectionName;
    }

    /**
     * Gets the current collectionName
     * @return Name of appData collection
     */
    protected String getCollectionName() {
        return collectionName;
    }

    /**
     * Gets current class that this AppData instance references.
     * @return Current appData class for marshalling data
     */
    protected Class<T> getCurrentClass() {
        return myClass;
    }

    /**
     * Gets current client for this AppData
     * @return current client instance
     */
    protected AbstractClient getClient(){
        return this.client;
    }




    /**
     * Define a cache as well as the policy to use when interacting with the cache
     *
     * @param cache an implementation of the Cache interface, the cache itself
     * @param policy the policy defining behavior of the cache.
     */
    public void setCache(Cache cache, CachePolicy policy) {
        synchronized (cacheLock) {
            this.cache = cache;
            this.policy = policy;
        }
    }

    /**
     * Creates a new instance of {@link Query}
     *
     * @return New instance of Query object.
     */
    public Query query() {
        return new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
    }

    /**
     * Method to get an entity or entities.  Pass null to entityID to return all entities
     * in a collection.
     *
     * @param entityID entityID to get
     * @return Get object
     * @throws java.io.IOException
     */
    public GetEntity getEntityBlocking(String entityID) throws IOException {
        GetEntity getEntity = new GetEntity(entityID, myClass);
        client.initializeRequest(getEntity);
        return getEntity;
    }


    /**
     * Method to get an entity or entities.  Pass null to entityID to return all entities
     * in a collection.
     *
     * @param entityID entityID to get
     * @param resolves list of KinveyReference fields to resolve
     * @param resolve_depth the depth of KinveyReferences fields to resolve
     * @param retain should resolved KinveyReferences be retained
     * @return Get object
     * @throws java.io.IOException
     */
    public GetEntity getEntityBlocking(String entityID, String[] resolves, int resolve_depth, boolean retain) throws IOException {
        GetEntity getEntity = new GetEntity(entityID, myClass, resolves, resolve_depth, retain);
        client.initializeRequest(getEntity);
        return getEntity;
    }


    /**
     * Method to get an entity or entities.  Pass null to entityID to return all entities
     * in a collection.
     *
     * @param entityID entityID to get
     * @return Get object
     * @throws java.io.IOException
     * @deprecated Renamed to {@link #getEntityBlocking(String)}
     */
    @Deprecated
    public GetEntity getEntity(String entityID) throws IOException {
        GetEntity getEntity = new GetEntity(entityID, myClass);
        client.initializeRequest(getEntity);
        return getEntity;
    }

    /**
     * Method to get a query of entities.  Pass an empty query to return all entities
     * in a collection.
     *
     * @param query Query to get
     * @return Get object
     * @throws java.io.IOException
     */
    public Get getBlocking(Query query) throws IOException {
        Preconditions.checkNotNull(query);
        Get get = new Get(query, Array.newInstance(myClass,0).getClass());
        client.initializeRequest(get);
        return get;
    }

    /**
     * Method to get an entity or entities.  Pass null to entityID to return all entities
     * in a collection.
     *
     * @param query Query to get
     * @param resolves list of KinveyReference fields to resolve
     * @param resolve_depth the depth of KinveyReferences fields to resolve
     * @param retain should resolved KinveyReferences be retained
     * @return Get object
     * @throws java.io.IOException
     */
    public Get getBlocking(Query query, String[] resolves, int resolve_depth, boolean retain) throws IOException {
        Get getEntity = new Get(query, myClass, resolves, resolve_depth, retain);
        client.initializeRequest(getEntity);
        return getEntity;
    }

    /**
     * Method to get a query of entities.  Pass an empty query to return all entities
     * in a collection.
     *
     * @param query Query to get
     * @return Get object
     * @deprecated Renamed to {@link #getBlocking(Query)}
     * @throws java.io.IOException
     */
    @Deprecated
    public Get get(Query query) throws IOException {
        Preconditions.checkNotNull(query);
        Get get = new Get(query, Array.newInstance(myClass,0).getClass());
        client.initializeRequest(get);
        return get;
    }

    /**
     * Method to get all entities in a collection.
     *
     * @return Get Object
     * @throws IOException
     */

    public Get getBlocking() throws IOException {
        return getBlocking(new Query());
    }

    /**
     * Method to get all entities in a collection.
     *
     * @return Get Object
     * @throws IOException
     * @deprecated Renamed to {@link #getBlocking()}
     */
    @Deprecated
    public Get get() throws IOException {
        return getBlocking(new Query());
    }

    /**
     * Save (create or update) an entity to a collection.
     *
     * @param entity Entity to Save
     * @return Save object
     * @throws IOException
     */
    public Save saveBlocking(T entity) throws IOException {

        Save save;
        String sourceID;

        GenericJson jsonEntity = (GenericJson) entity;
        sourceID = (String) jsonEntity.get(ID_FIELD_NAME);

        if (sourceID != null) {
            save = new Save(entity, myClass, sourceID, SaveMode.PUT);
        } else {
            save = new Save(entity, myClass, SaveMode.POST);
        }
        client.initializeRequest(save);
        return save;
    }

    /**
     * Save (create or update) an entity to a collection.
     *
     * @param entity Entity to Save
     * @return Save object
     * @throws IOException
     * @deprecated Renamed to {@link #saveBlocking(Object)}
     */
    @Deprecated
    public Save save(T entity) throws IOException {

        Save save;
        String sourceID;

        GenericJson jsonEntity = (GenericJson) entity;
        sourceID = (String) jsonEntity.get(ID_FIELD_NAME);

        if (sourceID != null) {
            save = new Save(entity, myClass, sourceID, SaveMode.PUT);
        } else {
            save = new Save(entity, myClass, SaveMode.POST);
        }
        client.initializeRequest(save);
        return save;
    }

    /**
     * Delete an entity from a collection by ID.
     *
     * @param entityID entityID to delete
     * @return Delete object
     * @throws IOException
     */
    public Delete deleteBlocking(String entityID) throws IOException {
        Preconditions.checkNotNull(entityID);
        Delete delete = new Delete(entityID);
        client.initializeRequest(delete);
        return delete;
    }

    /**
     * Delete an entity from a collection by ID.
     *
     * @param entityID entityID to delete
     * @return Delete object
     * @throws IOException
     * @deprecated Renamed to {@link #deleteBlocking(String)}
     */
    @Deprecated
    public Delete delete(String entityID) throws IOException {
        Preconditions.checkNotNull(entityID);
        Delete delete = new Delete(entityID);
        client.initializeRequest(delete);
        return delete;
    }

    /**
     * Delete an entity from a collection by Query.
     *
     * @param query query for entities to delete
     * @return Delete object
     * @throws IOException
     */
    public Delete deleteBlocking(Query query) throws IOException {
        Preconditions.checkNotNull(query);
        Delete delete = new Delete(query);
        client.initializeRequest(delete);
        return delete;
    }

    /**
     * Delete an entity from a collection by Query.
     *
     * @param query query for entities to delete
     * @return Delete object
     * @throws IOException
     * @deprecated Renamed to {@link #deleteBlocking(String)}
     */
    @Deprecated
    public Delete delete(Query query) throws IOException {
        Preconditions.checkNotNull(query);
        Delete delete = new Delete(query);
        client.initializeRequest(delete);
        return delete;
    }

    /**
     * Retrieve a group by COUNT on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param query  optional query to filter by (null for all records in a collection)
     * @return Aggregate object
     * @throws IOException
     */
    public Aggregate countBlocking(ArrayList<String> fields, Query query) throws IOException {
        Preconditions.checkNotNull(fields);
        return aggregate(fields, AggregateEntity.AggregateType.COUNT, null, query);
    }

    /**
     * Retrieve a group by COUNT on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param query  optional query to filter by (null for all records in a collection)
     * @return Aggregate object
     * @throws IOException
     * @deprecated Renamed to {@link #countBlocking(java.util.ArrayList, Query)}
     */
    @Deprecated
    public Aggregate count(ArrayList<String> fields, Query query) throws IOException {
        Preconditions.checkNotNull(fields);
        return aggregate(fields, AggregateEntity.AggregateType.COUNT, null, query);
    }

    /**
     * Retrieve a group by SUM on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param sumField field to sum
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     */
    public Aggregate sumBlocking(ArrayList<String> fields, String sumField, Query query) throws IOException {
        Preconditions.checkNotNull(fields);
        Preconditions.checkNotNull(sumField);
        return aggregate(fields, AggregateEntity.AggregateType.SUM, sumField, query);
    }

    /**
     * Retrieve a group by SUM on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param sumField field to sum
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     * @deprecated Renamed to {@link #sumBlocking(java.util.ArrayList, String, Query)}
     */
    @Deprecated
    public Aggregate sum(ArrayList<String> fields, String sumField, Query query) throws IOException {
        Preconditions.checkNotNull(fields);
        Preconditions.checkNotNull(sumField);
        return aggregate(fields, AggregateEntity.AggregateType.SUM, sumField, query);
    }

    /**
     * Retrieve a group by MAX on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param maxField field to obtain max value from
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     */
    public Aggregate maxBlocking(ArrayList<String> fields, String maxField, Query query) throws IOException {
        Preconditions.checkNotNull(fields);
        Preconditions.checkNotNull(maxField);
        return aggregate(fields, AggregateEntity.AggregateType.MAX, maxField, query);
    }

    /**
     * Retrieve a group by MAX on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param maxField field to obtain max value from
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     * @deprecated Renamed to {@link #maxBlocking(java.util.ArrayList, String, Query)}
     */
    @Deprecated
    public Aggregate max(ArrayList<String> fields, String maxField, Query query) throws IOException {
        Preconditions.checkNotNull(fields);
        Preconditions.checkNotNull(maxField);
        return aggregate(fields, AggregateEntity.AggregateType.MAX, maxField, query);
    }

    /**
     * Retrieve a group by MIN on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param minField field to obtain MIN value from
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     */
    public Aggregate minBlocking(ArrayList<String> fields, String minField, Query query) throws IOException {
        Preconditions.checkNotNull(fields);
        Preconditions.checkNotNull(minField);
        return aggregate(fields, AggregateEntity.AggregateType.MIN, minField, query);
    }

    /**
     * Retrieve a group by MIN on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param minField field to obtain MIN value from
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     * @deprecated Renamed to {@link #minBlocking(java.util.ArrayList, String, Query)}
     */
    public Aggregate min(ArrayList<String> fields, String minField, Query query) throws IOException {
        Preconditions.checkNotNull(fields);
        Preconditions.checkNotNull(minField);
        return aggregate(fields, AggregateEntity.AggregateType.MIN, minField, query);
    }

    /**
     * Retrieve a group by AVERAGE on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param averageField field to average
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     */
    public Aggregate averageBlocking(ArrayList<String> fields, String averageField, Query query) throws IOException {
        Preconditions.checkNotNull(fields);
        Preconditions.checkNotNull(averageField);
        return aggregate(fields, AggregateEntity.AggregateType.AVERAGE, averageField, query);
    }

    /**
     * Retrieve a group by AVERAGE on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param averageField field to average
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     * @deprecated Renamed to {@link #averageBlocking(java.util.ArrayList, String, Query)}
     */
    @Deprecated
    public Aggregate average(ArrayList<String> fields, String averageField, Query query) throws IOException {
        Preconditions.checkNotNull(fields);
        Preconditions.checkNotNull(averageField);
        return aggregate(fields, AggregateEntity.AggregateType.AVERAGE, averageField, query);
    }

    /**
     * Private helper method to create AggregateEntity and return an intialize Aggregate Request Object
     * @param fields fields to group by
     * @param type Type of aggregation
     * @param aggregateField Field to aggregate on
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     */
    private Aggregate aggregate(ArrayList<String> fields, AggregateEntity.AggregateType type, String aggregateField,
                                   Query query) throws IOException {
        AggregateEntity entity = new AggregateEntity(fields, type, aggregateField, query, client);
        Aggregate aggregate = new Aggregate(entity, myClass);
        client.initializeRequest(aggregate);
        return aggregate;
    }


    /**
     * Create and return a new synchronous App Data Request Builder associated with *this* instance of AppData.
     *
     * @return a new request builder for a blocking GET (by query) operation
     */
    public AppDataOperation.BlockingGetBuilder  blockingGetBuilder(){
        return new AppDataOperation.BlockingGetBuilder(this.collectionName, this.myClass, this);
    }

    /**
     * Create and return a new synchronous App Data Request Builder associated with *this* instance of AppData.
     *
     * @return a new request builder for a blocking GET (single) Entity operation
     */
    public AppDataOperation.BlockingGetEntityBuilder  blockingGetEntityBuilder(){
        return new AppDataOperation.BlockingGetEntityBuilder(this.collectionName, this.myClass, this);
    }

    /**
     * Create and return a new synchronous App Data Request Builder associated with *this* instance of AppData.
     *
     * @return a new request builder for a blocking SAVE (put or post) operation
     */
    public AppDataOperation.BlockingSaveBuilder  blockingSaveBuilder(){
        return new AppDataOperation.BlockingSaveBuilder(this.collectionName, this.myClass, this);
    }

    /**
     * Create and return a new synchronous App Data Request Builder associated with *this* instance of AppData.
     *
     * @return a new request builder for a blocking DELETE operation
     */
    public AppDataOperation.BlockingDeleteBuilder  blockingDeleteBuilder(){
        return new AppDataOperation.BlockingDeleteBuilder(this.collectionName, this.myClass, this);
    }

    /**
     * Generic Get class, extends AbstractKinveyJsonClientRequest<T[]>.  Constructs the HTTP request object for Get
     * requests.
     *
     */
    public class Get extends AbstractKinveyCachedClientRequest<T[]> {

        private static final String REST_PATH = "appdata/{appKey}/{collectionName}/" +
                "{?query,sort,limit,skip,resolve,resolve_depth,retainReference}";


        @Key
        private String collectionName;
        @Key("query")
        private String queryFilter;
        @Key("sort")
        private String sortFilter;
        @Key("limit")
        private String limit;
        @Key("skip")
        private String skip;

        @Key("resolve")
        private String resolve;
        @Key("resolve_depth")
        private String resolve_depth;
        @Key("retainReferences")
        private String retainReferences;


        Get(Query query, Class myClass) {
            super(client, "GET", REST_PATH, null, myClass);
            super.setCache(cache, policy);
            this.collectionName= AppData.this.collectionName;
            this.queryFilter = query.getQueryFilterJson(client.getJsonFactory());
            int queryLimit = query.getLimit();
            int querySkip = query.getSkip();
            this.limit = queryLimit > 0 ? Integer.toString(queryLimit) : null;
            this.skip = querySkip > 0 ? Integer.toString(querySkip) : null;
            String sortString = query.getSortString();
            this.sortFilter = sortString != "" ? sortString : null;

        }


        Get(Query query, Class myClass, String[] resolves, int resolve_depth, boolean retain){
            super(client, "GET", REST_PATH, null, myClass);
            super.setCache(cache, policy);
            this.collectionName= AppData.this.collectionName;
            this.queryFilter = query.getQueryFilterJson(client.getJsonFactory());
            int queryLimit = query.getLimit();
            int querySkip = query.getSkip();
            this.limit = queryLimit > 0 ? Integer.toString(queryLimit) : null;
            this.skip = querySkip > 0 ? Integer.toString(querySkip) : null;
            String sortString = query.getSortString();
            this.sortFilter = sortString != "" ? sortString : null;

            this.resolve = Joiner.on(",").join(resolves);
            this.resolve_depth = resolve_depth > 0 ? Integer.toString(resolve_depth) : null;
            this.retainReferences = Boolean.toString(retain);


        }

        @Override
        public T[] execute() throws IOException {
            return super.execute();
        }
    }


    /**
     * Generic Get class, extends AbstractKinveyJsonClientRequest<T>.  Constructs the HTTP request object for Get
     * requests.
     *
     */
    public class GetEntity extends AbstractKinveyCachedClientRequest<T> {

        private static final String REST_PATH = "appdata/{appKey}/{collectionName}/{entityID}" +
                "{resolve,resolve_depth,retainReference}";


        @Key
        private String entityID;
        @Key
        private String collectionName;

        @Key("resolve")
        private String resolve;
        @Key("resolve_depth")
        private String resolve_depth;
        @Key("retainReferences")
        private String retainReferences;




        GetEntity(String entityID, Class<T> myClass) {
            super(client, "GET", REST_PATH, null, myClass);
            super.setCache(cache, policy);
            this.collectionName= AppData.this.collectionName;
            this.entityID = entityID;
        }

        GetEntity(String entityID, Class<T> myClass, String[] resolves, int resolve_depth, boolean retain){
            super (client, "GET", REST_PATH, null, myClass);
            super.setCache(cache, policy);
            this.collectionName= AppData.this.collectionName;
            this.entityID = entityID;

            this.resolve = Joiner.on(",").join(resolves);
            this.resolve_depth = resolve_depth > 0 ? Integer.toString(resolve_depth) : null;
            this.retainReferences = Boolean.toString(retain);
        }



        @Override
        public T execute() throws IOException {
            T myEntity = super.execute();

            return myEntity;
        }


    }

    /** used internally **/
    protected enum SaveMode {
        POST,
        PUT
    }

    /**
     * Generic Save<T> class, extends AbstractKinveyJsonClientRequest<T>.  Constructs the HTTP request object for
     * Create / Update requests.
     *
     */
    public class Save extends AbstractKinveyJsonClientRequest<T> {
        private static final String REST_PATH = "appdata/{appKey}/{collectionName}/{entityID}";
        @Key
        private String collectionName;
        @Key
        private String entityID;

        Save(T entity, Class<T> myClass, String entityID, SaveMode update) {
            super(client, update.toString(), REST_PATH, entity, myClass);
            this.collectionName = AppData.this.collectionName;
            if (update.equals(SaveMode.PUT)) {
                this.entityID = entityID;
            }
        }

        Save(T entity, Class<T> myClass, SaveMode update) {
            this(entity, myClass, null, update);
        }
    }

    /**
     * Generic Delete class, extends AbstractKinveyJsonClientRequest<T>.  Constructs the HTTP request object
     * for Delete requests.
     *
     */
    public class Delete extends AbstractKinveyJsonClientRequest<KinveyDeleteResponse> {
        private static final String REST_PATH = "appdata/{appKey}/{collectionName}/{entityID}" +
                "{?query,sort,limit,skip,resolve,resolve_depth,retainReference}";

        @Key
        private String entityID;
        @Key
        private String collectionName;
        @Key("query")
        private String queryFilter;
        @Key("sort")
        private String sortFilter;
        @Key("limit")
        private String limit;
        @Key("skip")
        private String skip;

        Delete(String entityID) {
            super(client, "DELETE", REST_PATH, null, KinveyDeleteResponse.class);
            this.entityID = entityID;
            this.collectionName = AppData.this.collectionName;
        }

        Delete(Query query) {
            super(client, "DELETE", REST_PATH, null, KinveyDeleteResponse.class);
            this.collectionName= AppData.this.collectionName;
            this.queryFilter = query.getQueryFilterJson(client.getJsonFactory());
            int queryLimit = query.getLimit();
            int querySkip = query.getSkip();
            this.limit = queryLimit > 0 ? Integer.toString(queryLimit) : null;
            this.skip = querySkip > 0 ? Integer.toString(querySkip) : null;
            this.sortFilter = query.getSortString();

        }
    }

    /**
     * Generic Aggregate<T> class, extends AbstractKinveyJsonClientRequest<T>.  Constructs the HTTP request object for
     * Aggregate requests.
     *
     */
    public class Aggregate extends AbstractKinveyJsonClientRequest<T> {
        private static final String REST_PATH = "appdata/{appKey}/{collectionName}/_group";
        @Key
        private String collectionName;

        Aggregate(AggregateEntity entity, Class<T> myClass) {
            super(client, "POST", REST_PATH, entity,myClass);
            this.collectionName = AppData.this.collectionName;
        }
    }




}


