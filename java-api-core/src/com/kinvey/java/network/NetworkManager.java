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

package com.kinvey.java.network;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.annotations.ReferenceHelper;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.deltaset.DeltaSetItem;
import com.kinvey.java.deltaset.DeltaSetMerge;
import com.kinvey.java.model.AggregateEntity;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.query.MongoQueryFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class for managing appData access to the Kinvey backend.
 *
 * @author mjsalinger
 * @author m0rganic
 * @author edwardf
 * @since 2.0.2
 */
public class NetworkManager<T extends GenericJson> {

    private String collectionName;
    private Class<T> myClass;
    private AbstractClient client;

    /**
     * static final String representing universal "_id" value, used to uniquely identify entites
     */
    public static final String ID_FIELD_NAME = "_id";
    /**
     * static final String representing universal "_geoloc" value, used for geoqueries
     */
    public static final String GEOLOC_FIELD_NAME = "_geoloc";


    private String clientAppVersion = null;
    
    private GenericData customRequestProperties = new GenericData();

    public void setClientAppVersion(String appVersion){
    	this.clientAppVersion = appVersion;	
    }
    
    public void setClientAppVersion(int major, int minor, int patch){
    	setClientAppVersion(major + "." + minor + "." + patch);
    }
    
    public void setCustomRequestProperties(GenericJson customheaders){
    	this.customRequestProperties = customheaders;
    }
    
    public void setCustomRequestProperty(String key, Object value){
    	if (this.customRequestProperties == null){
    		this.customRequestProperties = new GenericJson();
    	}
    	this.customRequestProperties.put(key, value);
    }
    
    public void clearCustomRequestProperties(){
    	this.customRequestProperties = new GenericJson();
    }
    
    


    /**
     * Constructor to instantiate the NetworkManager class.
     *
     * @param collectionName Name of the appData collection
     * @param myClass Class Type to marshall data between.
     */
    public NetworkManager(String collectionName, Class<T> myClass, AbstractClient client) {
        Preconditions.checkNotNull(collectionName, "collectionName must not be null.");
        Preconditions.checkNotNull(client, "client must not be null.");
        this.collectionName = collectionName;
        this.myClass = myClass;
        this.client = client;
        this.clientAppVersion = client.getClientAppVersion();
        this.customRequestProperties = client.getCustomRequestProperties();
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
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Gets current class that this NetworkManager instance references.
     * @return Current appData class for marshalling data
     */
    public Class<T> getCurrentClass() {
        return myClass;
    }

    /**
     * Gets current client for this NetworkManager
     * @return current client instance
     */
    public AbstractClient getClient(){
        return this.client;
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
     * Method to resolve a raw query string
     *
     * @param queryString Query to get
     * @return Get object
     * @throws java.io.IOException
     */
    public Get getBlocking(String queryString) throws IOException{
    	Preconditions.checkNotNull(queryString);
        Get get = new Get(queryString, Array.newInstance(myClass,0).getClass());
        client.initializeRequest(get);
        return get;
    }

    /**
     * Method to get a query of entities.  Pass an array of entity _ids to return the entites.
     *
     * @param ids array of _ids to query for
     * @return Get object
     * @throws java.io.IOException
     */
    public Get getBlocking(String[] ids) throws IOException {
        Preconditions.checkNotNull(ids, "ids cannot be null.");
        Query q = new Query();
        q.in("_id", ids);
        return this.getBlocking(q);
    }


    /**
     * Method to execute a query and resolve KinveyReferences in the entities
     *
     * @param query Query to get
     * @param resolves list of KinveyReference fields to resolve
     * @param resolve_depth the depth of KinveyReferences fields to resolve
     * @param retain should resolved KinveyReferences be retained
     * @return Get object
     * @throws java.io.IOException
     */
    public Get getBlocking(Query query, String[] resolves, int resolve_depth, boolean retain) throws IOException {
        Get getEntity = new Get(query, Array.newInstance(myClass,0).getClass(), resolves, resolve_depth, retain);
        client.initializeRequest(getEntity);
        return getEntity;
    }


    /**
     * Convenience wrapper method to execute a query and resolve KinveyReferences in the entities
     *
     *
     * @param query - Query to get
     * @param resolves list of KinveyReference fields to resolve
     * @return Get Request object ready for execution
     * @throws IOException
     */
    public Get getBlocking(Query query, String[] resolves) throws IOException{
        return getBlocking(query, resolves, 1, true);

    }


    /**
     * Convenience wrapper method to get an entity and resolve KinveyReferences
     *
     * @param id the id of the entity to query for
     * @param resolves list of KinveyReference fields to resolve
     * @return Get Request object ready for execution
     * @throws IOException
     */
    public GetEntity getEntityBlocking(String id, String[] resolves) throws IOException{
        return getEntityBlocking(id, resolves, 1, true);

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
     * Method to get a query of entities that are changed on server side agains given ones.  Pass
     * an empty query and empty items to compare with to return all entities in a collection.
     *
     * @param query Query to get
     * @return Get object
     * @throws java.io.IOException
     */
    public Get getBlocking(Query query, List<T> cachedItems, boolean deltaSetCachingEnabled) throws IOException {
        Preconditions.checkNotNull(query);
        Get get;
        if (deltaSetCachingEnabled) {
            get = new DeltaGet(query, Array.newInstance(myClass,0).getClass(), cachedItems);
        } else {
            get = new Get(query, Array.newInstance(myClass,0).getClass());
        }

        client.initializeRequest(get);
        return get;
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

        //prepare entity relation data saving
        try {
            ReferenceHelper.processReferences(entity, new ReferenceHelper.ReferenceListener() {
                @Override
                public String onUnsavedReferenceFound(String collection, GenericJson object) {
                    if (object.containsKey("_id")){
                        return object.get("_id").toString();
                    }

                    NetworkManager<GenericJson> manager = new NetworkManager<GenericJson>(collection, GenericJson.class, client);
                    try {
                        GenericJson saved = manager.saveBlocking(object).execute();
                        return saved.get(ID_FIELD_NAME).toString();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            });
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

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
     * public helper method to create AggregateEntity and return an intialize Aggregate Request Object
     * @param fields fields to group by
     * @param type Type of aggregation
     * @param aggregateField Field to aggregate on
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     */
    public Aggregate aggregate(ArrayList<String> fields, AggregateEntity.AggregateType type, String aggregateField,
                                   Query query) throws IOException {
        AggregateEntity entity = new AggregateEntity(fields, type, aggregateField, query, client);
        Aggregate aggregate = new Aggregate(entity, myClass);
        client.initializeRequest(aggregate);
        return aggregate;
    }
    
    
    private class MetadataGet extends AbstractKinveyJsonClientRequest<DeltaSetItem[]>{
        private static final String REST_PATH = "appdata/{appKey}/{collectionName}" +
                "{?query,fields,tls,sort,limit,skip,resolve,resolve_depth,retainReference}";
        @Key
        private String collectionName;
        @Key("query")
        private String queryFilter;
        @Key("sort")
        private String sortFilter;

        @Key("limit")
        protected String limit;
        @Key("skip")
        protected String skip;
        

        @Key
        private String fields = "_id,_kmd";
        @Key
        private boolean tls = true;

        MetadataGet(DeltaGet getRequest){
            super(client, "GET", REST_PATH, null, DeltaSetItem[].class);
            this.queryFilter = getRequest.queryFilter;

            this.skip = getRequest.skip;
            this.limit = getRequest.limit;


            collectionName = getRequest.collectionName;
            sortFilter = getRequest.sortFilter;


            this.collectionName = NetworkManager.this.getCollectionName();
            //prevent caching and offline store for that request
            this.getRequestHeaders().put("X-Kinvey-Client-App-Version", client.getClientAppVersion());
            if (client.getCustomRequestProperties() != null && !client.getCustomRequestProperties().isEmpty()){
                this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(client.getCustomRequestProperties()) );
            }
        }


        public DeltaSetItem[] execute() throws IOException {
            return super.execute();
        }

    }

    /**
     * Generic DeltaGet class.  Constructs the HTTP request object for Get
     * requests with Delta set cache functionality.
     *
     */
    public class DeltaGet extends Get {

        private static final int IDS_PER_PAGE = 100;

        private static final String REST_PATH = "appdata/{appKey}/{collectionName}" +
                "{?query,sort,limit,skip,resolve,resolve_depth,retainReference}";
        private List<T> currentItems;

        DeltaGet(Query query, Class myClass, List<T> currentItems) {
            super(query, myClass);
            this.currentItems = currentItems;
        }


        @Override
        public T[] execute() throws IOException {
            T[] ret = null;
            if (currentItems != null && !currentItems.isEmpty()) {
                MetadataGet deltaRequest = new MetadataGet(this);
                client.initializeRequest(deltaRequest);
                DeltaSetItem[] itemsArray = deltaRequest.execute();

                //init empty array in case if there is no ids to update
                List<T> updatedOnline = new ArrayList<T>();

                List<DeltaSetItem> items = new ArrayList<DeltaSetItem>();
                Collections.addAll(items, itemsArray);

                List<String> ids = DeltaSetMerge.getIdsForUpdate(currentItems, items);
                if (ids.size() > 0) {
                    updatedOnline = fetchIdsWithPaging(ids);
                }

                ret = DeltaSetMerge.merge(items, currentItems, updatedOnline, client.getObjectParser(), myClass);
            }
            if (ret == null){
                ret = super.execute();
            }

            return ret;
        }

        private List<T> fetchIdsWithPaging(List<String> ids) throws IOException {

            List<T> ret = new ArrayList<T>();
            int pos = 0;
            while (ids.size() > 0){
                int chunkSize = ids.size() < IDS_PER_PAGE ? ids.size() : IDS_PER_PAGE;

                List<String> chunkItems = ids.subList(0, chunkSize);
                ids = ids.subList(chunkSize, ids.size());

                Query query = query().in("_id", chunkItems.toArray((String[])Array.newInstance(String.class, chunkSize)));
                Get pageGet = new Get(query,
                        getResponseClass(),
                        resolve != null ? resolve.split(",") : new String[]{},
                        resolve_depth != null ? Integer.parseInt(resolve_depth) : 0,
                        retainReferences != null && Boolean.parseBoolean(retainReferences));

                client.initializeRequest(pageGet);
                T[] pageGetResult = pageGet.execute();
                ret.addAll(Arrays.asList(pageGetResult));

            }

            return ret;
        }



    }


    /**
     * Generic Get class.  Constructs the HTTP request object for Get
     * requests.
     *
     */
    public class Get extends AbstractKinveyJsonClientRequest<T[]> {

        private static final String REST_PATH = "appdata/{appKey}/{collectionName}" +
                "{?query,sort,limit,skip,resolve,resolve_depth,retainReference}";

        @Key
        protected String collectionName;
        @Key("query")
        protected String queryFilter;
        @Key("sort")
        protected String sortFilter;
        @Key("limit")
        protected String limit;
        @Key("skip")
        protected String skip;

        @Key("resolve")
        protected String resolve;
        @Key("resolve_depth")
        protected String resolve_depth;
        @Key("retainReferences")
        protected String retainReferences;

        Get(Query query, Class myClass) {
            super(client, "GET", REST_PATH, null, myClass);
            this.collectionName= NetworkManager.this.collectionName;
            this.queryFilter = query.getQueryFilterJson(client.getJsonFactory());
            int queryLimit = query.getLimit();
            int querySkip = query.getSkip();
            this.limit = queryLimit > 0 ? Integer.toString(queryLimit) : null;
            this.skip = querySkip > 0 ? Integer.toString(querySkip) : null;
            String sortString = query.getSortString();
            this.sortFilter = !(sortString.equals("")) ? sortString : null;
            this.getRequestHeaders().put("X-Kinvey-Client-App-Version", NetworkManager.this.clientAppVersion);
            if (NetworkManager.this.customRequestProperties != null && !NetworkManager.this.customRequestProperties.isEmpty()){
            	this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(NetworkManager.this.customRequestProperties) );
            }
        }


        Get(Query query, Class myClass, String[] resolves, int resolve_depth, boolean retain){
            super(client, "GET", REST_PATH, null, myClass);
            this.collectionName= NetworkManager.this.collectionName;
            this.queryFilter = query.getQueryFilterJson(client.getJsonFactory());
            int queryLimit = query.getLimit();
            int querySkip = query.getSkip();
            this.limit = queryLimit > 0 ? Integer.toString(queryLimit) : null;
            this.skip = querySkip > 0 ? Integer.toString(querySkip) : null;
            String sortString = query.getSortString();
            this.sortFilter = !(sortString.equals("")) ? sortString : null;

            this.resolve = Joiner.on(",").join(resolves);
            this.resolve_depth = resolve_depth > 0 ? Integer.toString(resolve_depth) : null;
            this.retainReferences = Boolean.toString(retain);
            this.getRequestHeaders().put("X-Kinvey-Client-App-Version", NetworkManager.this.clientAppVersion);
            if (NetworkManager.this.customRequestProperties != null && !NetworkManager.this.customRequestProperties.isEmpty()){
            	this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(NetworkManager.this.customRequestProperties) );
            }
            
        }
        
        Get(String queryString, Class myClass){
        	super(client, "GET", REST_PATH, null, myClass);
        	this.collectionName= NetworkManager.this.collectionName;
        	this.queryFilter = !queryString.equals("{}") ? queryString : null;
        	this.setTemplateExpand(false);
        	this.getRequestHeaders().put("X-Kinvey-Client-App-Version", NetworkManager.this.clientAppVersion);
            if (NetworkManager.this.customRequestProperties != null && !NetworkManager.this.customRequestProperties.isEmpty()){
            	this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(NetworkManager.this.customRequestProperties) );
            }
        }

        @Override
        public T[] execute() throws IOException {
            return super.execute();
        }
    }


    /**
     * Generic Get class.  Constructs the HTTP request object for Get
     * requests.
     *
     */
    public class GetEntity extends AbstractKinveyJsonClientRequest<T> {

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
            this.collectionName= NetworkManager.this.collectionName;
            this.entityID = entityID;
            this.getRequestHeaders().put("X-Kinvey-Client-App-Version", NetworkManager.this.clientAppVersion);
            if (NetworkManager.this.customRequestProperties != null && !NetworkManager.this.customRequestProperties.isEmpty()){
            	this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(NetworkManager.this.customRequestProperties) );
            }
        }

        GetEntity(String entityID, Class<T> myClass, String[] resolves, int resolve_depth, boolean retain){
            super (client, "GET", REST_PATH, null, myClass);
            this.collectionName= NetworkManager.this.collectionName;
            this.entityID = entityID;

            this.resolve = Joiner.on(",").join(resolves);
            this.resolve_depth = resolve_depth > 0 ? Integer.toString(resolve_depth) : null;
            this.retainReferences = Boolean.toString(retain);
            this.getRequestHeaders().put("X-Kinvey-Client-App-Version", NetworkManager.this.clientAppVersion);
            if (NetworkManager.this.customRequestProperties != null && !NetworkManager.this.customRequestProperties.isEmpty()){
            	this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(NetworkManager.this.customRequestProperties) );
            }
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
     * Generic Save<T> class.  Constructs the HTTP request object for
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
            this.collectionName = NetworkManager.this.collectionName;
            if (update.equals(SaveMode.PUT)) {
                this.entityID = entityID;
            }
            this.getRequestHeaders().put("X-Kinvey-Client-App-Version", NetworkManager.this.clientAppVersion);
            if (NetworkManager.this.customRequestProperties != null && !NetworkManager.this.customRequestProperties.isEmpty()){
            	this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(NetworkManager.this.customRequestProperties) );
            }
        }

        Save(T entity, Class<T> myClass, SaveMode update) {
            this(entity, myClass, null, update);            
        }
    }

    /**
     * Generic Delete class.  Constructs the HTTP request object
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
            this.collectionName = NetworkManager.this.collectionName;
            this.getRequestHeaders().put("X-Kinvey-Client-App-Version", NetworkManager.this.clientAppVersion);
            if (NetworkManager.this.customRequestProperties != null && !NetworkManager.this.customRequestProperties.isEmpty()){
            	this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(NetworkManager.this.customRequestProperties) );
            }
        }

        Delete(Query query) {
            super(client, "DELETE", REST_PATH, null, KinveyDeleteResponse.class);
            this.collectionName= NetworkManager.this.collectionName;
            this.queryFilter = query.getQueryFilterJson(client.getJsonFactory());
            int queryLimit = query.getLimit();
            int querySkip = query.getSkip();
            this.limit = queryLimit > 0 ? Integer.toString(queryLimit) : null;
            this.skip = querySkip > 0 ? Integer.toString(querySkip) : null;
            String sortString = query.getSortString();
            this.sortFilter = !(sortString.equals("")) ? sortString : null;
            this.getRequestHeaders().put("X-Kinvey-Client-App-Version", NetworkManager.this.clientAppVersion);
            if (NetworkManager.this.customRequestProperties != null && !NetworkManager.this.customRequestProperties.isEmpty()){
            	this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(NetworkManager.this.customRequestProperties) );
            }

        }
    }

    /**
     * Generic Aggregate<T> class, constructs the HTTP request object for
     * Aggregate requests.
     *
     */
    public class Aggregate extends AbstractKinveyJsonClientRequest<T> {
        private static final String REST_PATH = "appdata/{appKey}/{collectionName}/_group";
        @Key
        private String collectionName;

        Aggregate(AggregateEntity entity, Class<T> myClass) {
            super(client, "POST", REST_PATH, entity, myClass);
            this.collectionName = NetworkManager.this.collectionName;
            this.getRequestHeaders().put("X-Kinvey-Client-App-Version", NetworkManager.this.clientAppVersion);
            if (NetworkManager.this.customRequestProperties != null && !NetworkManager.this.customRequestProperties.isEmpty()){
            	this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(NetworkManager.this.customRequestProperties) );
            }
        }
    }

    public boolean isOnline() {
        return true;
    }
}


