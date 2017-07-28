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
import com.google.api.client.util.Key;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.LinkedResources.GetLinkedResourceClientRequest;
import com.kinvey.java.LinkedResources.LinkedGenericJson;
import com.kinvey.java.LinkedResources.SaveLinkedResourceClientRequest;
import com.kinvey.java.MimeTypeFinder;
import com.kinvey.java.Query;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.network.NetworkManager;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

/**
 * Subset of the NetworkManager API, offering support for downloading and uploading associated files with an entity.
 * <p>
 * Files are automatically downloaded and uploaded when the entity is saved or retrieved.  To enable this functionality
 * ensure your Entity extends {@code LinkedGenericJson} instead of the usual {@code GenericJson}
 * </p>
 *
 * @author edwardf
 */
public class LinkedNetworkManager<T extends LinkedGenericJson> extends NetworkManager<T> {

    MimeTypeFinder mimetypeFinder;

    /**
     * Constructor to instantiate the LinkedNetworkManager class.
     *
     * @param collectionName Name of the LinkedNetworkManager collection
     * @param myClass        Class Type to marshall data between.
     * @param client        Instance of a Client which manages this API
     */
    public LinkedNetworkManager(String collectionName, Class<T> myClass, AbstractClient client) {
        super(collectionName, myClass, client);
    }

    /**
     * Method to get an entity or entities and download ALL associated Linked Resources.
     * <p>
     * Pass null to entityID to return all entities in a collection.  Use the {@code DownloaderProgressListener}
     * to retrieve callback information about the NetworkFileManager downloads.
     * </p>
     * <p>
     * This method will download all associated Linked Resources and could take a long time.  For more control when handling
     * a large number of Linked Resources, try using an overloaded variation of this method.
     * </p>
     *
     * @param entityID entityID to get
     * @param download - used for progress updates as associated files are downloaded.
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    public GetEntity getEntityBlocking(String entityID, DownloaderProgressListener download) throws IOException {
        GetEntity getEntity = new GetEntity(entityID, getCurrentClass(), null);
        getEntity.setDownloadProgressListener(download);
        getClient().initializeRequest(getEntity);
        return getEntity;
    }

    /**
     * Method to get an entity or entities and download a subset of associated Linked Resources.
     * <p>
     * Pass null to entityID to return all entities in a collection.  Use the {@code DownloaderProgressListener}
     * to retrieve callback information about the NetworkFileManager downloads.
     * </p>
     * <p>
     * This method will only download Linked Resources for the fields declared in the resources array.
     * These Strings must match the strings used as keys in the entity.
     * </p>
     *
     * @param entityID entityID to get
     * @param download - used for progress updates as associated files are downloaded.
     * @param attachments - array of JSON keys of resources to retrieve
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    public GetEntity getEntityBlocking(String entityID, DownloaderProgressListener download, String[] attachments) throws IOException {
        GetEntity getEntity = new GetEntity(entityID, getCurrentClass(), attachments);
        getEntity.setDownloadProgressListener(download);
        getClient().initializeRequest(getEntity);
        return getEntity;
    }

    /**
     * Method to get an entity or entities and download ALL associated Linked Resources.
     * <p>
     * Pass null to entityID to return all entities in a collection.  Use the {@code DownloaderProgressListener}
     * to retrieve callback information about the NetworkFileManager downloads.
     * </p>
     * <p>
     * This method will download all associated Linked Resources and could take a long time.  For more control when handling
     * a large number of Linked Resources, try using an overloaded variation of this method.
     * </p>
     *
     * @param query query for entities to retrieve
     * @param download - used for progress updates as associated files are downloaded.
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    public Get getBlocking(Query query, DownloaderProgressListener download) throws IOException {
        Preconditions.checkNotNull(query);
        Get get = new Get(query, Array.newInstance(getCurrentClass(), 0).getClass(), null);
        get.setDownloadProgressListener(download);
        getClient().initializeRequest(get);
        return get;
    }

    /**
     * Method to get an entity or entities and download ALL associated Linked Resources, with complete control over KinveyReferencess
     * <p>
     * Pass null to entityID to return all entities in a collection.  Use the {@code DownloaderProgressListener}
     * to retrieve callback information about the NetworkFileManager downloads.
     * </p>
     *
     * @param query query for entities to retrieve
     * @param attachments which json fields are linked resources that should be resolved
     * @param resolves a string array of json field names to resolve as kinvey references
     * @param resolve_depth how many levels of kinvey references to resolve
     * @param retain should the resolved values be retained?
     * @param download - used for progress updates as associated files are downloaded.
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    public Get getBlocking(Query query,  DownloaderProgressListener download, String[] attachments, String[] resolves, int resolve_depth, boolean retain) throws IOException {
        Preconditions.checkNotNull(query);
        Get get = new Get(query, Array.newInstance(getCurrentClass(), 0).getClass(), attachments, resolves, resolve_depth, retain);
        get.setDownloadProgressListener(download);
        getClient().initializeRequest(get);
        return get;
    }

    /**
     * Method to get entities by query and download ALL associated Linked Resources, with wrapped control over KinveyReferencess
     * <p>
     * Use the {@code DownloaderProgressListener}
     * to retrieve callback information about the NetworkFileManager downloads.
     * </p>
     *
     * @param query query for entities to retrieve
     * @param download - used for progress updates as associated files are downloaded.
     * @param attachments which json fields are linked resources that should be resolved
     * @param resolves a string array of json field names to resolve as kinvey references
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    public Get getBlocking(Query query, DownloaderProgressListener download, String[] attachments, String[] resolves) throws IOException{
        return getBlocking(query, download, attachments, resolves, 1, true);
    }

    /**
     * Method to get an entity or entities and download ALL associated Linked Resources.
     * <p>
     * Pass null to entityID to return all entities in a collection.  Use the {@code DownloaderProgressListener}
     * to retrieve callback information about the NetworkFileManager downloads.
     * </p>
     * <p>
     * This method will only download Linked Resources for the fields declared in the resources array.
     * These Strings must match the strings used as keys in the entity.
     * </p>
     *
     * @param query query for entities to retrieve
     * @param download - used for progress updates as associated files are downloaded.
     * @param attachments - array of JSON keys of resources to retrieve
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    public Get getBlocking(Query query, DownloaderProgressListener download, String[] attachments) throws IOException {
        Preconditions.checkNotNull(query);
        Get get = new Get(query, Array.newInstance(getCurrentClass(), 0).getClass(), attachments);
        get.setDownloadProgressListener(download);
        getClient().initializeRequest(get);
        return get;
    }

    /**
     * Method to get an entity or entities and download ALL associated Linked Resources.
     * <p>
     * Pass null to entityID to return all entities in a collection.  Use the {@code DownloaderProgressListener}
     * to retrieve callback information about the NetworkFileManager downloads.
     * </p>
     * <p>
     * This method will download all associated Linked Resources and could take a long time.  For more control when handling
     * a large number of Linked Resources, try using an overloaded variation of this method.
     * </p>
     *
     * @param download - used for progress updates as associated files are downloaded.
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    public Get getBlocking(DownloaderProgressListener download) throws IOException {
        return getBlocking(new Query(), download);
    }

    /**
     * Method to get an entity or entities and download ALL associated Linked Resources.
     * <p>
     * Pass null to entityID to return all entities in a collection.  Use the {@code DownloaderProgressListener}
     * to retrieve callback information about the NetworkFileManager downloads.
     * </p>
     * <p>
     * This method will only download Linked Resources for the fields declared in the resources array.
     * These Strings must match the strings used as keys in the entity.
     * </p>
     *
     * @param download - used for progress updates as associated files are downloaded.
     * @param attachments - array of JSON keys of resources to retrieve
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    public Get getBlocking(DownloaderProgressListener download, String[] attachments) throws IOException {
        Get get = new Get(new Query(), Array.newInstance(getCurrentClass(), 0).getClass(), attachments);
        get.setDownloadProgressListener(download);
        getClient().initializeRequest(get);
        return get;
    }

    /**
     * Save (create or update) an entity to a collection and upload ALL associated Linked Resources.
     * <p>
     * This method will download all associated Linked Resources and could take a long time.  For more control when handling
     * a large number of Linked Resources, try using an overloaded variation of this method.
     * </p>
     *
     * @param entity Entity to Save
     * @param upload - Listener for uploading Linked Resources, can be null.
     *
     * @return Save object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    public Save saveBlocking(T entity, UploaderProgressListener upload) throws IOException {

        Save save;
        String sourceID;

        GenericJson jsonEntity = (GenericJson) entity;
        sourceID = (String) jsonEntity.get(ID_FIELD_NAME);

        if (sourceID != null) {
            save = new Save(entity, getCurrentClass(), sourceID, SaveMode.PUT);
        } else {
            save = new Save(entity, getCurrentClass(), SaveMode.POST);
        }
        save.setUpload(upload);
        getClient().initializeRequest(save);
        return save;
    }

    /**
     * Save (create or update) an entity to a collection and upload ALL associated Linked Resources.
     * <p>
     * This method will only upload Linked Resources for the fields declared in the attachments array.
     * These Strings must match the strings used as keys in the entity.
     * </p>
     *
     * @param entity Entity to Save
     * @param upload - Listener for uploading Linked Resources, can be null.
     * @param attachments - array of JSON keys of resources to retrieve
     * @return Save object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    public Save saveBlocking(T entity, UploaderProgressListener upload, String[] attachments) throws IOException {

        Save save;
        String sourceID;

        GenericJson jsonEntity = (GenericJson) entity;
        sourceID = (String) jsonEntity.get(ID_FIELD_NAME);

        if (sourceID != null) {
            save = new Save(entity, getCurrentClass(), sourceID, SaveMode.PUT);
        } else {
            save = new Save(entity, getCurrentClass(), SaveMode.POST);
        }
        save.setUpload(upload);
        getClient().initializeRequest(save);
        return save;
    }

    protected void setMimeTypeManager(MimeTypeFinder finder){
        this.mimetypeFinder = finder;
    }

    /**
     * Generic Get class, extends AbstractKinveyJsonClientRequest<T[]>.  Constructs the HTTP request object for Get
     * requests.
     *
     */
    public class Get extends GetLinkedResourceClientRequest<List<T>> {

        private static final String REST_PATH = "appdata/{appKey}/{collectionName}" +
                "{?query,sort,limit,skip,resolve,resolve_depth,retainReference}";

        private String[] attachments;

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


        Get(Query query, Class myClass, String[] attachments) {
            super(getClient(), REST_PATH, null, myClass);
//            super.setCache(cache, policy);
            this.attachments = attachments;
            this.collectionName= getCollectionName();
            this.queryFilter = query.getQueryFilterJson(getClient().getJsonFactory());
            int queryLimit = query.getLimit();
            int querySkip = query.getSkip();
            this.limit = queryLimit > 0 ? Integer.toString(queryLimit) : null;
            this.skip = querySkip > 0 ? Integer.toString(querySkip) : null;
            this.sortFilter = query.getSortString();

        }


        Get(Query query, Class myClass, String[] attachments, String[] resolves, int resolve_depth, boolean retain){
            super(getClient(), REST_PATH, null, myClass);
//            super.setCache(cache, policy);
            this.attachments = attachments;
            this.collectionName= getCollectionName();
            this.queryFilter = query.getQueryFilterJson(getClient().getJsonFactory());
            int queryLimit = query.getLimit();
            int querySkip = query.getSkip();
            this.limit = queryLimit > 0 ? Integer.toString(queryLimit) : null;
            this.skip = querySkip > 0 ? Integer.toString(querySkip) : null;
            this.sortFilter = query.getSortString();
            if (resolves != null){
                this.resolve = Joiner.on(",").join(resolves);
                this.resolve_depth = resolve_depth > 0 ? Integer.toString(resolve_depth) : null;
                this.retainReferences = Boolean.toString(retain);
            }



        }

        @Override
        public List<T> execute() throws IOException {
            return super.execute();
        }
    }


    /**
     * Generic Get class, extends AbstractKinveyJsonClientRequest<T>.  Constructs the HTTP request object for Get
     * requests.
     *
     */
    public class GetEntity extends GetLinkedResourceClientRequest<T> {

        private static final String REST_PATH = "appdata/{appKey}/{collectionName}/{entityID}" +
                "{resolve,resolve_depth,retainReference}";

        private String[] attachments;

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




        GetEntity(String entityID, Class<T> myClass, String[] attachments) {
            super(getClient(), REST_PATH, null, myClass);
//            super.setCache(cache, policy);
            this.attachments = attachments;
            this.collectionName = getCollectionName();
            this.entityID = entityID;
        }

        GetEntity(String entityID, Class<T> myClass, String[] attachments, String[] resolves, int resolve_depth, boolean retain){
            super(getClient(), REST_PATH, null, myClass);
//            super.setCache(cache, policy);
            this.attachments = attachments;
            this.collectionName = getCollectionName();
            this.entityID = entityID;
            if (resolves != null){
                this.resolve = Joiner.on(",").join(resolves);
                this.resolve_depth = resolve_depth > 0 ? Integer.toString(resolve_depth) : null;
                this.retainReferences = Boolean.toString(retain);
            }
        }



        @Override
        public T execute() throws IOException {
            T myEntity = super.execute();

            return myEntity;
        }


    }



    /**
     * Generic Save<T> class, extends AbstractKinveyJsonClientRequest<T>.  Constructs the HTTP request object for
     * Create / Update requests.
     *
     */
    public class Save extends SaveLinkedResourceClientRequest<T> {
        private static final String REST_PATH = "appdata/{appKey}/{collectionName}/{entityID}";
        @Key
        private String collectionName;
        @Key
        private String entityID;

        Save(T entity, Class<T> myClass, String entityID, SaveMode update) {
            super(getClient(), update.toString(), REST_PATH, entity, myClass);
            setMimeTypeFinder(mimetypeFinder);
            this.collectionName = getCollectionName();
            if (update.equals(SaveMode.PUT)) {
                this.entityID = entityID;
            }
        }

        Save(T entity, Class<T> myClass, SaveMode update) {
            this(entity, myClass, null, update);
        }
    }





}
