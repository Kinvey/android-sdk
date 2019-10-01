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

package com.kinvey.java.network

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key
import com.google.common.base.Joiner
import com.google.common.base.Preconditions
import com.kinvey.java.AbstractClient
import com.kinvey.java.MimeTypeFinder
import com.kinvey.java.Query
import com.kinvey.java.core.DownloaderProgressListener
import com.kinvey.java.core.UploaderProgressListener
import com.kinvey.java.linkedResources.GetLinkedResourceClientRequest
import com.kinvey.java.linkedResources.LinkedGenericJson
import com.kinvey.java.linkedResources.SaveLinkedResourceClientRequest
import com.kinvey.java.model.SaveMode
import java.io.IOException

/**
 * Subset of the NetworkManager API, offering support for downloading and uploading associated files with an entity.
 *
 *
 * Files are automatically downloaded and uploaded when the entity is saved or retrieved.  To enable this functionality
 * ensure your Entity extends `LinkedGenericJson` instead of the usual `GenericJson`
 *
 *
 * @author edwardf
 */
open class LinkedNetworkManager<T : LinkedGenericJson>
/**
 * Constructor to instantiate the LinkedNetworkManager class.
 *
 * @param collectionName Name of the LinkedNetworkManager collection
 * @param myClass        Class Type to marshall data between.
 * @param client        Instance of a Client which manages this API
 */(collectionName: String?, myClass: Class<T>, client: AbstractClient<*>) : NetworkManager<T>(collectionName, myClass, client) {
    internal var mimetypeFinder: MimeTypeFinder? = null
    /**
     * Method to get an entity or entities and download ALL associated Linked Resources.
     *
     *
     * Pass null to entityID to return all entities in a collection.  Use the `DownloaderProgressListener`
     * to retrieve callback information about the NetworkFileManager downloads.
     *
     *
     *
     * This method will download all associated Linked Resources and could take a long time.  For more control when handling
     * a large number of Linked Resources, try using an overloaded variation of this method.
     *
     *
     * @param entityID entityID to get
     * @param download - used for progress updates as associated files are downloaded.
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    @Throws(IOException::class)
    fun getEntityBlocking(entityID: String?, download: DownloaderProgressListener?): GetEntity {
        val getEntity = GetEntity(entityID ?: "", currentClass, null)
        getEntity.downloadProgressListener = download
        client.initializeRequest(getEntity)
        return getEntity
    }

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
     * @param attachments - array of JSON keys of resources to retrieve
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    @Throws(IOException::class)
    open fun getEntityBlocking(entityID: String?, download: DownloaderProgressListener?, attachments: Array<String>?): GetEntity {
        val getEntity = GetEntity(entityID ?: "", currentClass, attachments)
        getEntity.downloadProgressListener = download
        client.initializeRequest(getEntity)
        return getEntity
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
     * This method will download all associated Linked Resources and could take a long time.  For more control when handling
     * a large number of Linked Resources, try using an overloaded variation of this method.
     *
     *
     * @param query query for entities to retrieve
     * @param download - used for progress updates as associated files are downloaded.
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    @Throws(IOException::class)
    fun getBlocking(query: Query, download: DownloaderProgressListener): Get {
        Preconditions.checkNotNull(query)
        val javaClass = currentClass as Class<List<T>>
        val get = Get(query, javaClass, null)
        get.downloadProgressListener = download
        client.initializeRequest(get)
        return get
    }

    /**
     * Method to get an entity or entities and download ALL associated Linked Resources, with complete control over KinveyReferencess
     *
     *
     * Pass null to entityID to return all entities in a collection.  Use the `DownloaderProgressListener`
     * to retrieve callback information about the NetworkFileManager downloads.
     *
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
    @Throws(IOException::class)
    fun getBlocking(query: Query, download: DownloaderProgressListener, attachments: Array<String>, resolves: Array<String?>?, resolve_depth: Int, retain: Boolean): Get {
        Preconditions.checkNotNull(query)
        val javaClass = currentClass as Class<List<T>>
        val get = Get(query, javaClass, attachments, resolves, resolve_depth, retain)
        get.downloadProgressListener = download
        client.initializeRequest(get)
        return get
    }

    /**
     * Method to get entities by query and download ALL associated Linked Resources, with wrapped control over KinveyReferencess
     *
     *
     * Use the `DownloaderProgressListener`
     * to retrieve callback information about the NetworkFileManager downloads.
     *
     *
     * @param query query for entities to retrieve
     * @param download - used for progress updates as associated files are downloaded.
     * @param attachments which json fields are linked resources that should be resolved
     * @param resolves a string array of json field names to resolve as kinvey references
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    @Throws(IOException::class)
    fun getBlocking(query: Query, download: DownloaderProgressListener, attachments: Array<String>, resolves: Array<String?>?): Get {
        return getBlocking(query, download, attachments, resolves, 1, true)
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
     * @param attachments - array of JSON keys of resources to retrieve
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    @Throws(IOException::class)
    fun getBlocking(query: Query, download: DownloaderProgressListener?, attachments: Array<String>?): Get {
        Preconditions.checkNotNull(query)
        val javaClass = currentClass as Class<List<T>>
        val get = Get(query, javaClass, attachments)
        get.downloadProgressListener = download
        client.initializeRequest(get)
        return get
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
     * This method will download all associated Linked Resources and could take a long time.  For more control when handling
     * a large number of Linked Resources, try using an overloaded variation of this method.
     *
     *
     * @param download - used for progress updates as associated files are downloaded.
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    @Throws(IOException::class)
    fun getBlocking(download: DownloaderProgressListener): Get {
        return getBlocking(Query(), download)
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
     * @param attachments - array of JSON keys of resources to retrieve
     * @return Get object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    @Throws(IOException::class)
    fun getBlocking(download: DownloaderProgressListener?, attachments: Array<String>?): Get {
        val javaClass = currentClass as Class<List<T>>
        val get = Get(Query(), javaClass, attachments)
        get.downloadProgressListener = download
        client.initializeRequest(get)
        return get
    }

    /**
     * Save (create or update) an entity to a collection and upload ALL associated Linked Resources.
     *
     *
     * This method will download all associated Linked Resources and could take a long time.  For more control when handling
     * a large number of Linked Resources, try using an overloaded variation of this method.
     *
     *
     * @param entity Entity to Save
     * @param upload - Listener for uploading Linked Resources, can be null.
     *
     * @return Save object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    @Throws(IOException::class)
    fun saveBlocking(entity: T, upload: UploaderProgressListener?): Save {
        val save: Save
        val sourceID: String
        val jsonEntity = entity as GenericJson
        sourceID = jsonEntity[ID_FIELD_NAME] as String
        save = if (sourceID != null) {
            Save(entity, currentClass, sourceID, SaveMode.PUT)
        } else {
            Save(entity, currentClass, SaveMode.POST)
        }
        save.upload = upload
        client.initializeRequest(save)
        return save
    }

    /**
     * Save (create or update) an entity to a collection and upload ALL associated Linked Resources.
     *
     *
     * This method will only upload Linked Resources for the fields declared in the attachments array.
     * These Strings must match the strings used as keys in the entity.
     *
     *
     * @param entity Entity to Save
     * @param upload - Listener for uploading Linked Resources, can be null.
     * @param attachments - array of JSON keys of resources to retrieve
     * @return Save object
     * @throws java.io.IOException - if there is an issue executing the client requests
     */
    @Throws(IOException::class)
    fun saveBlocking(entity: T, upload: UploaderProgressListener?, attachments: Array<String?>?): Save {
        val save: Save
        val sourceID: String
        val jsonEntity = entity as GenericJson
        sourceID = jsonEntity[ID_FIELD_NAME] as String
        save = if (sourceID != null) {
            Save(entity, currentClass, sourceID, SaveMode.PUT)
        } else {
            Save(entity, currentClass, SaveMode.POST)
        }
        save.upload = upload
        client.initializeRequest(save)
        return save
    }

    protected fun setMimeTypeManager(finder: MimeTypeFinder?) {
        mimetypeFinder = finder
    }

    /**
     * Generic Get class, extends AbstractKinveyJsonClientRequest<T></T>[]>.  Constructs the HTTP request object for Get
     * requests.
     *
     */
    inner class Get : GetLinkedResourceClientRequest<List<T>> {
        private var attachments: Array<String>?
        @Key
        private var collectionName: String?
        @Key("query")
        private var queryFilter: String?
        @Key("sort")
        private var sortFilter: String?
        @Key("limit")
        private var limit: String?
        @Key("skip")
        private var skip: String?
        @Key("resolve")
        private var resolve: String? = null
        @Key("resolve_depth")
        private var resolve_depth: String? = null
        @Key("retainReferences")
        private var retainReferences: String? = null

        constructor(query: Query, myClass: Class<List<T>>, attachments: Array<String>?) : super(client, GET_LIST_REST_PATH, null, myClass) {
            this.attachments = attachments
            this.collectionName = this@LinkedNetworkManager.collectionName
            queryFilter = query.getQueryFilterJson(client.jsonFactory)
            val queryLimit = query.limit
            val querySkip = query.skip
            limit = if (queryLimit > 0) queryLimit.toString() else null
            skip = if (querySkip > 0) querySkip.toString() else null
            sortFilter = query.sortString
        }

        constructor(query: Query, myClass: Class<List<T>>, attachments: Array<String>, resolves: Array<String?>?, resolve_depth: Int, retain: Boolean)
            : super(client, GET_LIST_REST_PATH, null, myClass) {
            this.attachments = attachments
            this.collectionName = this@LinkedNetworkManager.collectionName
            queryFilter = query.getQueryFilterJson(client.jsonFactory)
            val queryLimit = query.limit
            val querySkip = query.skip
            limit = if (queryLimit > 0) Integer.toString(queryLimit) else null
            skip = if (querySkip > 0) Integer.toString(querySkip) else null
            sortFilter = query.sortString
            if (resolves != null) {
                this.resolve = Joiner.on(",").join(resolves)
                this.resolve_depth = if (resolve_depth > 0) Integer.toString(resolve_depth) else null
                retainReferences = retain.toString()
            }
        }

        @Throws(IOException::class)
        override fun execute(): List<T>? {
            return super.execute()
        }
    }

    /**
     * Generic Get class, extends AbstractKinveyJsonClientRequest<T>.  Constructs the HTTP request object for Get
     * requests.
     *
     */
    open inner class GetEntity : GetLinkedResourceClientRequest<T> {
        private var attachments: Array<String>?
        @Key
        private var entityID: String
        @Key
        private var collectionName: String?
        @Key("resolve")
        private var resolve: String? = null
        @Key("resolve_depth")
        private var resolve_depth: String? = null
        @Key("retainReferences")
        private var retainReferences: String? = null

        internal constructor(entityID: String, myClass: Class<T>, attachments: Array<String>?) : super(client, GET_ENTITY_REST_PATH, null, myClass) {
            this.attachments = attachments
            this.collectionName = this@LinkedNetworkManager.collectionName
            this.entityID = entityID
        }

        internal constructor(entityID: String, myClass: Class<T>, attachments: Array<String>, resolves: Array<String?>?, resolve_depth: Int, retain: Boolean)
            : super(client, GET_ENTITY_REST_PATH, null, myClass) {
            this.attachments = attachments
            this.collectionName = this@LinkedNetworkManager.collectionName
            this.entityID = entityID
            if (resolves != null) {
                this.resolve = Joiner.on(",").join(resolves)
                this.resolve_depth = if (resolve_depth > 0) Integer.toString(resolve_depth) else null
                retainReferences = retain.toString()
            }
        }

        @Throws(IOException::class)
        override fun execute(): T? {
            return super.execute()
        }
    }

    /**
     * Generic Save<T> class, extends AbstractKinveyJsonClientRequest<T>.  Constructs the HTTP request object for
     * Create / Update requests.
     *
     */
    inner class Save internal constructor(entity: T, myClass: Class<T>, entityID: String?, update: SaveMode)
        : SaveLinkedResourceClientRequest<T>(client, update.toString(), SAVE_REST_PATH, entity, myClass) {
        @Key
        private val collectionName: String?
        @Key
        private var entityID: String? = null

        internal constructor(entity: T, myClass: Class<T>, update: SaveMode) : this(entity, myClass, null, update) {}

        init {
            setMimeTypeFinder(mimetypeFinder)
            this.collectionName = this@LinkedNetworkManager.collectionName
            if (update == SaveMode.PUT) {
                this.entityID = entityID
            }
        }
    }

    companion object {
        const val SAVE_REST_PATH = "appdata/{appKey}/{collectionName}/{entityID}"

        const val GET_ENTITY_REST_PATH = "appdata/{appKey}/{collectionName}/{entityID}" +
                "{resolve,resolve_depth,retainReference}"

        const val GET_LIST_REST_PATH = "appdata/{appKey}/{collectionName}" +
                "{?query,sort,limit,skip,resolve,resolve_depth,retainReference}"
    }
}