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
package com.kinvey.android.network;

import java.io.IOException;

import com.kinvey.android.AndroidMimeTypeFinder;
import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.dto.User;
import com.kinvey.java.network.LinkedNetworkManager;
import com.kinvey.java.Query;
import com.kinvey.java.LinkedResources.LinkedGenericJson;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.store.StoreType;

import java.util.List;

/**
 * Wraps the {@link LinkedNetworkManager} public methods in asynchronous functionality using native Android AsyncTask.
 *
 * <p>
 * This functionality can be accessed through the {@link com.kinvey.android.Client#linkedData(String, Class, StoreType)}} convenience method.
 * The first String parameter is the name of the Collection, and the Class is the expected Response Class.
 * </p>
 * <p>
 * The methods provided in this class take two (optional) callbacks, a {@code KinveyClientCallback} for the NetworkManager request as well as a {@code UploaderProgressListener} or {@code DownloaderProgressListener} for updates on the NetworkFileManager status
 * </p>
 * <p>
 * The functionality of this class is provided by both the {@code com.kinvey.java.network.NetworkManager} API as well as the {@code com.kinvey.java.network.NetworkFileManager} API.
 * </p>
 *
 *
 * @auther edwardf
 *
 */
public class AsyncLinkedNetworkManager<T extends LinkedGenericJson> extends LinkedNetworkManager<T> {
    /**
     * Constructor to instantiate the NetworkManager class.
     *
     * @param collectionName Name of the appData collection
     * @param myClass        Class Type to marshall data between.
     */
    public AsyncLinkedNetworkManager(String collectionName, Class<T> myClass, AbstractClient client) {
        super(collectionName, myClass, client);
        super.setMimeTypeManager(new AndroidMimeTypeFinder());
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
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    public void getEntity(String entityID, KinveyClientCallback<T> callback,  DownloaderProgressListener download){
        new GetEntity(entityID, callback, download, null).execute();

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
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    public void get(Query query, KinveyListCallback<T> callback, DownloaderProgressListener download) {
        new Get(query, callback, download, null, null, 0 , false).execute();
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
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    public void get(Query query, KinveyListCallback<T> callback, DownloaderProgressListener download, String[] resolves, int resolve_depth, boolean retain) {
        new Get(query, callback, download, null, resolves, resolve_depth , retain).execute();
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
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    public void get(KinveyListCallback<T> callback, DownloaderProgressListener download) {
        new Get(new Query(), callback, download, null, null, 0, false).execute();

    }


    /**
     * Save (create or update) an entity to a collection and upload ALL associated Linked Resources.
     * <p>
     * This method will only upload Linked Resources for the fields declared in the resources array.
     * These Strings must match the strings used as keys in the entity.
     * </p>
     *
     * @param entity Entity to Save
     * @param upload - Listener for uploading Linked Resources, can be null.
     * @return Save object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    public void save(T entity, KinveyClientCallback<T> callback, UploaderProgressListener upload) {
        new Save(entity, callback, upload, null).execute();
    }



    private class Get extends AsyncClientRequest<List<T>> {

        Query query = null;
        String[] attachments;
        DownloaderProgressListener progress;
        String[] resolves;
        int resolve_depth;
        boolean retain;

        public Get(Query query, KinveyListCallback<T> callback, DownloaderProgressListener progress, String[] attachments, String[] resolves, int resolve_depth, boolean retain) {
            super(callback);
            this.query = query;
            this.progress = progress;
            this.attachments = attachments;
            this.resolves = resolves;
            this.resolve_depth = resolve_depth;
            this.retain = retain;
        }

        @Override
        protected List<T> executeAsync() throws IOException {
            return AsyncLinkedNetworkManager.this.getBlocking(this.query, this.progress, this.attachments, this.resolves, this.resolve_depth, this.retain).execute();
        }
    }
    private class GetEntity extends AsyncClientRequest<T> {

        String entityID = null;
        String[] attachments;
        DownloaderProgressListener progress;

        public GetEntity(String entityID, KinveyClientCallback<T> callback, DownloaderProgressListener progress, String[] attachments) {
            super(callback);
            this.entityID = entityID;
            this.progress = progress;
            this.attachments = attachments;
        }

        @Override
        protected T executeAsync() throws IOException {
            return AsyncLinkedNetworkManager.this.getEntityBlocking(this.entityID, this.progress, this.attachments).execute();
        }
    }
    private class Save extends AsyncClientRequest<T> {

        T entity = null;
        String[] attachments;
        UploaderProgressListener progress;

        public Save(T entity, KinveyClientCallback<T> callback, UploaderProgressListener progress, String[] attachments) {
            super(callback);
            this.entity = entity;
            this.progress = progress;
            this.attachments = attachments;
        }

        @Override
        protected T executeAsync() throws IOException {
            return AsyncLinkedNetworkManager.this.saveBlocking(this.entity, this.progress, this.attachments).execute();
        }
    }






}
