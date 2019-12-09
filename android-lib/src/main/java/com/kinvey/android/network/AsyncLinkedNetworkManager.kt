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
package com.kinvey.android.network

import com.kinvey.android.AndroidMimeTypeFinder
import com.kinvey.android.AsyncClientRequest
import com.kinvey.android.Client
import com.kinvey.android.callback.KinveyListCallback
import com.kinvey.java.Query
import com.kinvey.java.core.DownloaderProgressListener
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.core.UploaderProgressListener
import com.kinvey.java.linkedResources.LinkedGenericJson
import com.kinvey.java.network.LinkedNetworkManager
import com.kinvey.java.store.StoreType
import java.io.IOException

/**
 * Wraps the [LinkedNetworkManager] public methods in asynchronous functionality using native Android AsyncTask.
 *
 *
 *
 * This functionality can be accessed through the [com.kinvey.android.Client.linkedData]} convenience method.
 * The first String parameter is the name of the Collection, and the Class is the expected Response Class.
 *
 *
 *
 * The methods provided in this class take two (optional) callbacks, a `KinveyClientCallback` for the NetworkManager request as well as a `UploaderProgressListener` or `DownloaderProgressListener` for updates on the NetworkFileManager status
 *
 *
 *
 * The functionality of this class is provided by both the `com.kinvey.java.network.NetworkManager` API as well as the `com.kinvey.java.network.NetworkFileManager` API.
 *
 *
 *
 * @auther edwardf
 */
open class AsyncLinkedNetworkManager<T : LinkedGenericJson>(collectionName: String?, myClass: Class<T>, client: Client<*>?)
    : LinkedNetworkManager<T>(collectionName, myClass, client) {
    /**
     * Method to get an entity or entities and download a subset of associated Linked Resources.
     *
     *
     * Pass null to entityID to return all entities in a collection.  Use the `DownloaderProgressListener`
     * to retrieve callback information about the NetworkFileManager downloads.
     *
     *
     *
     * This method will only download Linked Resources for the fields declared in the resources array.
     * These Strings must match the strings used as keys in the entity.
     *
     *
     * @param entityID entityID to get
     * @param download - used for progress updates as associated files are downloaded.
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    fun getEntity(entityID: String?, callback: KinveyClientCallback<T>?,
                  download: DownloaderProgressListener?, storeType: StoreType = StoreType.SYNC) {
        GetEntity(entityID, callback, download, null, storeType).execute()
    }

    /**
     * Method to get an entity or entities and download ALL associated Linked Resources.
     *
     *
     * Pass null to entityID to return all entities in a collection.  Use the `DownloaderProgressListener`
     * to retrieve callback information about the NetworkFileManager downloads.
     *
     *
     *
     * This method will only download Linked Resources for the fields declared in the resources array.
     * These Strings must match the strings used as keys in the entity.
     *
     *
     * @param query query for entities to retrieve
     * @param download - used for progress updates as associated files are downloaded.
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    operator fun get(query: Query?, callback: KinveyListCallback<T>?,
                     download: DownloaderProgressListener?, storeType: StoreType = StoreType.SYNC) {
        Get(query, callback, download, null, null, 0, false, storeType).execute()
    }

    /**
     * Method to get an entity or entities and download ALL associated Linked Resources.
     *
     *
     * Pass null to entityID to return all entities in a collection.  Use the `DownloaderProgressListener`
     * to retrieve callback information about the NetworkFileManager downloads.
     *
     *
     *
     * This method will only download Linked Resources for the fields declared in the resources array.
     * These Strings must match the strings used as keys in the entity.
     *
     *
     * @param query query for entities to retrieve
     * @param download - used for progress updates as associated files are downloaded.
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    operator fun get(query: Query?, callback: KinveyListCallback<T>?,
                     download: DownloaderProgressListener?, resolves: Array<String?>?,
                     resolve_depth: Int, retain: Boolean, storeType: StoreType = StoreType.SYNC) {
        Get(query, callback, download, null, resolves, resolve_depth, retain, storeType).execute()
    }

    /**
     * Method to get an entity or entities and download ALL associated Linked Resources.
     *
     *
     * Pass null to entityID to return all entities in a collection.  Use the `DownloaderProgressListener`
     * to retrieve callback information about the NetworkFileManager downloads.
     *
     *
     *
     * This method will only download Linked Resources for the fields declared in the resources array.
     * These Strings must match the strings used as keys in the entity.
     *
     *
     * @param download - used for progress updates as associated files are downloaded.
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    operator fun get(callback: KinveyListCallback<T>?, download: DownloaderProgressListener?, storeType: StoreType = StoreType.SYNC) {
        Get(Query(), callback, download, null, null, 0, false, storeType).execute()
    }

    /**
     * Save (create or update) an entity to a collection and upload ALL associated Linked Resources.
     *
     *
     * This method will only upload Linked Resources for the fields declared in the resources array.
     * These Strings must match the strings used as keys in the entity.
     *
     *
     * @param entity Entity to Save
     * @param upload - Listener for uploading Linked Resources, can be null.
     * @return Save object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    fun save(entity: T, callback: KinveyClientCallback<T>?, upload: UploaderProgressListener?, storeType: StoreType = StoreType.SYNC) {
        Save(entity, callback, upload, null, storeType).execute()
    }

    private inner class Get(val query: Query?,
                            callback: KinveyListCallback<T>?,
                            var progress: DownloaderProgressListener?,
                            var attachments: Array<String>?,
                            var resolves: Array<String?>?,
                            var resolve_depth: Int,
                            var retain: Boolean, val storeType: StoreType = StoreType.SYNC) : AsyncClientRequest<List<T>>(callback) {
        @Throws(IOException::class)
        override fun executeAsync(): List<T>? {
            return this@AsyncLinkedNetworkManager.getBlocking(this.query, progress, attachments,
                    resolves, resolve_depth, retain, storeType).execute()
        }
    }

    private inner class GetEntity(val entityID: String?, callback: KinveyClientCallback<T>?,
                                  val progress: DownloaderProgressListener?,
                                  val attachments: Array<String>?, val storeType: StoreType = StoreType.SYNC) : AsyncClientRequest<T>(callback) {
        @Throws(IOException::class)
        override fun executeAsync(): T? {
            return this@AsyncLinkedNetworkManager.getEntityBlocking(entityID ?: "",
                    progress, attachments, storeType).execute()
        }
    }

    private inner class Save(val entity: T, callback: KinveyClientCallback<T>?,
                             val progress: UploaderProgressListener?,
                             val attachments: Array<String?>?, val storeType: StoreType = StoreType.SYNC) : AsyncClientRequest<T>(callback) {
        @Throws(IOException::class)
        override fun executeAsync(): T? {
            return this@AsyncLinkedNetworkManager.saveBlocking(entity, progress, attachments, storeType).execute()
        }
    }

    /**
     * Constructor to instantiate the NetworkManager class.
     *jsonContent
     * @param collectionName Name of the appData collection
     * @param myClass        Class Type to marshall data between.
     */

    init {
        super.setMimeTypeManager(AndroidMimeTypeFinder())
    }
}