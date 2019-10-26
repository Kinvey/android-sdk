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

package com.kinvey.android.cache

import android.content.Context
import android.net.Uri
import com.google.api.client.json.GenericJson
import com.kinvey.android.Client
import com.kinvey.java.AbstractClient
import com.kinvey.java.Constants
import com.kinvey.java.Constants.ACCESS_ERROR
import com.kinvey.java.KinveyException
import com.kinvey.java.cache.ICache
import com.kinvey.java.cache.ICacheManager
import com.kinvey.java.model.KinveyMetaData
import com.kinvey.java.model.KinveyMetaData.AccessControlList
import io.realm.*
import io.realm.RealmConfiguration.Builder
import io.realm.exceptions.RealmFileException
import java.io.File
import java.util.*

/**
 * Created by Prots on 1/26/16.
 */
class RealmCacheManager : ICacheManager {

    var client: AbstractClient<*>? = null
    private var context: Context? = null
    private var encryptionKey: ByteArray? = null
    private val prefix = ""

    private var realmConfiguration: RealmConfiguration? = null
        get() {
            if (field == null) {
                field = if (encryptionKey != null) {
                    Builder().name(prefix + Constants.UNDERSCORE + clientHash)
                        .encryptionKey(encryptionKey)
                        .build()
                } else {
                    Builder().name(prefix + Constants.UNDERSCORE + clientHash).build()
                }
            }
            return field
        }

    private val mCacheMap = HashMap<String, RealmCache<*>>()

    constructor(encryptionKey: ByteArray?, client: Client<*>) {
        this.encryptionKey = encryptionKey
        this.client = client
        context = client.context
    }

    constructor(encryptionKey: ByteArray?, prefix: String?, client: Client<*>) {
        this.encryptionKey = encryptionKey
        this.client = client
        context = client.context
    }

    constructor(client: Client<*>) {
        this.client = client
        context = client.context
    }

    constructor(prefix: String?, client: Client<*>) {
        this.client = client
        context = client.context
    }

    override fun <T : GenericJson> getCache(collection: String?, collectionItemClass: Class<T>?, ttl: Long?): ICache<T>? {

        synchronized(LOCK) {
            val mRealm = dynamicRealm
            var cache: RealmCache<T>?
            try {
                val cacheKey = clientHash + File.separator + collection
                cache = mCacheMap[cacheKey] as RealmCache<T>?
                if (cache == null) {
                    cache = RealmCache<T>(collection ?: "", this,
                            collectionItemClass ?: GenericJson::class.java as Class<T>, ttl ?: 0)
                    mRealm.beginTransaction()
                    val isMigrationNeeded = TableNameManager.getShortName(collection, mRealm) == null && mRealm.schema.contains(collection)
                    mRealm.commitTransaction()
                    if (cache.hash != getTableHash(collection, mRealm)) {
                        mRealm.beginTransaction()
                        try {
                            removeSchemas(prepareSchemasToRemove(collection, mRealm), mRealm)
                        } finally {
                            mRealm.commitTransaction()
                        }
                        if (collection != SYNC_ITEMS_COLLECTION
                            && collection != SYNC_COLLECTION
                            && client?.syncManager?.getCount(collection) ?: 0 > 0) {
                            client?.syncManager?.clear(collection)
                            KinveyException("Not synced items were deleted from $collection collection").printStackTrace()
                        }
                        //split table remove and create
                        mRealm.beginTransaction()
                        try {
                            //create table scheme
                            cache.createRealmTable(mRealm)
                            //store table hash for further usage
                            setTableHash(collection, cache.hash, mRealm)
                        } finally {
                            mRealm.commitTransaction()
                        }
                    } else if (isMigrationNeeded) {
                        mRealm.beginTransaction()
                        cache.migration(mRealm)
                        mRealm.commitTransaction()
                    } else {
                        // check and remove unnecessary tables like ..._acl_kmd
                        mRealm.beginTransaction()
                        val schema: RealmSchema = mRealm.schema
                        if (schema.contains(collection + Constants.UNDERSCORE + AccessControlList.ACL + Constants.UNDERSCORE + KinveyMetaData.KMD)) {
                            cache.checkAclKmdFields(mRealm)
                        }
                        mRealm.commitTransaction()
                    }
                    mRealm.beginTransaction()
                    // Check that ACL and KMD fields don't exist in embedded objects (like sync_meta_kmd)
                    val isRemoveMetadata = mRealm.schema.contains(TableNameManager.getShortName(
                        TableNameManager.getShortName(
                        TableNameManager.getShortName(SYNC_COLLECTION, mRealm) + KinveyMetaData.META, mRealm)
                                + Constants.UNDERSCORE + KinveyMetaData.KMD, mRealm))
                    mRealm.commitTransaction()
                    if (isRemoveMetadata) {
                        mRealm.beginTransaction()
                        val collections: RealmResults<DynamicRealmObject> = mRealm.where(TABLE_HASH_NAME).findAll()
                        val tablesToRemove = collections.flatMap { obj -> //ACL and KMD embedded table names
                            prepareToRemoveMetadataSchemasFromEmbeddedObjects(
                                    obj.get<CharArray>(Constants.COLLECTION).toString()
                                    , mRealm, false)
                        }.toList()
                        removeSchemas(tablesToRemove, mRealm)
                        mRealm.commitTransaction()
                    }
                    mCacheMap[cacheKey] = cache
                } else {
                    if (collectionItemClass?.isAssignableFrom(cache.collectionItemClass) == false &&
                        !cache.collectionItemClass.isAssignableFrom(collectionItemClass)) {
                        throw KinveyException("Class implementation for collection have been changed during runtime",
                                "Please review the BaseDataStore usage, parameter should remain the same for same collection",
                                "Seems like you have used different classes for same collection in AsyncAppDataCreation")
                    }
                    //create new instance because ttl values differs for different store types and
                    cache = RealmCache<T>(collection ?: "", this,
                            collectionItemClass ?: GenericJson::class.java as Class<T>, ttl ?: 0)
                }
                cache.ttl = ttl ?: 0
            } finally {
                mRealm.close()
            }
            return cache
        }
    }

    /**
     * Find all schemas (table names) to removing by collection name
     * @param collection collection name
     * @param realm Realm object
     * @return list of schemas to removing
     */
    private fun prepareSchemasToRemove(collection: String?, realm: DynamicRealm): List<String> {
        val currentSchema: RealmSchema = realm.schema
        var originalName: String?
        var className: String
        val schemasToDelete: MutableList<String> = ArrayList()
        val schemas: Set<RealmObjectSchema> = currentSchema.all
        for (schema in schemas) {
            className = schema.className
            //search class
            if (className == TableNameManager.getShortName(collection, realm)) {
                schemasToDelete.add(className)
                //search sub-classes
                for (subClassSchema in schemas) {
                    originalName = TableNameManager.getOriginalName(subClassSchema.className, realm)
                    if (originalName != null && originalName.startsWith(className + Constants.UNDERSCORE)) {
                        schemasToDelete.addAll(prepareSchemasToRemove(originalName, realm))
                    }
                }
            }
        }
        return schemasToDelete
    }

    /**
     * Get all schemas (table names) for _kmd and _acl fields in embedded objects to removing
     * @param tableName name of the table (class) where searching
     * @param realm Realm object
     * @param isEmbedded true if it's embedded object
     * @return list of schemas to removing
     */
    private fun prepareToRemoveMetadataSchemasFromEmbeddedObjects(tableName: String, realm: DynamicRealm, isEmbedded: Boolean): List<String> {
        val currentSchema: RealmSchema = realm.schema
        var originalName: String?
        var className: String
        val schemasToDelete: MutableList<String> = ArrayList()
        val schemas: Set<RealmObjectSchema> = currentSchema.all
        for (schema in schemas) {
            className = schema.className
            if (className == TableNameManager.getShortName(tableName, realm)) {
                for (subClassSchema in schemas) {
                    originalName = TableNameManager.getOriginalName(subClassSchema.className, realm)
                    if (originalName == null) {
                        continue
                    }
                    if (isEmbedded && originalName == className + Constants.UNDERSCORE + KinveyMetaData.KMD) {
                        schema.removeField(KinveyMetaData.KMD)
                        schemasToDelete.add(subClassSchema.className)
                    } else if (isEmbedded && originalName == className + Constants.UNDERSCORE + AccessControlList.ACL) {
                        schema.removeField(AccessControlList.ACL)
                        schemasToDelete.add(subClassSchema.className)
                    } else if (originalName.startsWith(className + Constants.UNDERSCORE)) {
                        schemasToDelete.addAll(prepareToRemoveMetadataSchemasFromEmbeddedObjects(originalName, realm, true))
                    }
                }
            }
        }
        return schemasToDelete
    }

    /**
     * Remove realm tables by names
     * @param tableNames table names to removing
     * @param realm Realm object
     */
    private fun removeSchemas(tableNames: List<String>, realm: DynamicRealm) {
        val realmSchema: RealmSchema = realm.schema
        for (tableName in tableNames) {
            if (realmSchema.get(tableName)?.hasPrimaryKey() == true) {
                realmSchema.get(tableName)?.removePrimaryKey()
            }
            realmSchema.remove(tableName)
            TableNameManager.removeShortName(tableName, realm)
        }
    }

    override fun clear() {
        synchronized(LOCK) { Realm.deleteRealm(realmConfiguration) }
    }

    override fun <T : GenericJson> clearCollection(collection: String?, collectionItemClass: Class<T>?, ttl: Long?) {
        synchronized(LOCK) {
            val realm = dynamicRealm
            try {
                realm.beginTransaction()
                val schemas: MutableList<String> = ArrayList()
                val table = TableNameManager.getShortName(collection, realm)
                table?.let { schemas.add(it) }
                schemas.addAll(getEmbeddedObjectSchemas(collection, realm))
                removeSchemas(schemas, realm)
                val cacheKey = clientHash + File.separator + collection
                val cache = mCacheMap[cacheKey] as RealmCache<T>
                cache?.createRealmTable(realm)
                realm.commitTransaction()
            } finally {
                realm.close()
            }
        }
    }

    private fun getEmbeddedObjectSchemas(tableName: String?, realm: DynamicRealm): List<String> {
        val currentSchema: RealmSchema = realm.schema
        var originalName: String?
        var className: String
        val embeddedObjectSchemas: MutableList<String> = ArrayList()
        val schemas: Set<RealmObjectSchema> = currentSchema.all
        for (schema in schemas) {
            className = schema.className
            if (className == TableNameManager.getShortName(tableName, realm)) {
                for (subClassSchema in schemas) {
                    originalName = TableNameManager.getOriginalName(subClassSchema.className, realm)
                    if (originalName == null) {
                        continue
                    }
                    if (originalName.startsWith(className + "_")) {
                        embeddedObjectSchemas.add(subClassSchema.className)
                        embeddedObjectSchemas.addAll(getEmbeddedObjectSchemas(originalName, realm))
                    }
                }
            }
        }
        return embeddedObjectSchemas
    }

    private fun init(mRealm: DynamicRealm) {
        val schema: RealmSchema = mRealm.schema
        val tableHashScheme = schema.get(TABLE_HASH_NAME)
        if (tableHashScheme == null) {
            mRealm.beginTransaction()
            createTableHashScheme(schema)
            mRealm.commitTransaction()
        }
    }

    private fun createTableHashScheme(dbSchema: RealmSchema) {
        val tableHashScheme: RealmObjectSchema = dbSchema.create(TABLE_HASH_NAME)
        tableHashScheme.addField(Constants.COLLECTION, String::class.java, FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED)
        tableHashScheme.addField("hash", String::class.java, FieldAttribute.REQUIRED)
    }

    private fun getTableHash(collection: String?, mRealm: DynamicRealm): String {
        val res = mRealm.where(TABLE_HASH_NAME)
                .equalTo(Constants.COLLECTION, collection)
                .findFirst()
        return if (res != null) res.getString("hash") else ""
    }

    /**
     * Create hash record for collection to track changes
     * NOTE: this method should be called within existing realm transaction
     * @param collection Collection name
     * @param hash Computed hash of the table
     */
    private fun setTableHash(collection: String?, hash: String?, mRealm: DynamicRealm) {
        var obj = mRealm.where(TABLE_HASH_NAME)
                .equalTo(Constants.COLLECTION, collection).findFirst()
        if (obj == null) {
            obj = mRealm.createObject(TABLE_HASH_NAME, collection)
        }
        obj?.set("hash", hash)
    }

    private val clientHash: String
        private get() {
            val server: Uri = Uri.parse(client?.baseUrl)
            return server.host + Constants.UNDERSCORE + server.port
        }

    /**
     * get Prepared DynamicRealm since realm object can not be shared between threads
     */
    val dynamicRealm: DynamicRealm
        get() = synchronized(LOCK) {
            val realm: DynamicRealm
            try {
                realm = DynamicRealm.getInstance(realmConfiguration)
            } catch (exception: RealmFileException) {
                if (exception.kind != null && exception.kind.name == "ACCESS_ERROR") {
                    throw KinveyException(ACCESS_ERROR, "Use correct encryption key", "You are using wrong encryption key")
                } else {
                    throw exception
                }
            }
            init(realm)
            return realm
        }

    companion object {
        private const val TABLE_HASH_NAME = "__KinveyTables__"
        private const val SYNC_ITEMS_COLLECTION = "syncitems"
        private const val SYNC_COLLECTION = "sync"
        private val LOCK = Any()
    }
}