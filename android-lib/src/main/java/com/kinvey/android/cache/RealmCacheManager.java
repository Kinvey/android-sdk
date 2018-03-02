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

package com.kinvey.android.cache;

import android.content.Context;
import android.net.Uri;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.Client;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.KinveyException;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.cache.ICacheManager;
import com.kinvey.java.model.KinveyMetaData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import io.realm.exceptions.RealmFileException;

/**
 * Created by Prots on 1/26/16.
 */
public class RealmCacheManager implements ICacheManager {

    private static final String TABLE_HASH_NAME = "__KinveyTables__";
    private static final String SYNC_ITEMS_COLLECTION = "syncitems";
    private static final String SYNC_COLLECTION = "sync";
    
    private byte[] encryptionKey;
    private final AbstractClient client;
    private final Context context;
    private String prefix = "";
    private RealmConfiguration realmConfiguration;


    private HashMap<String, RealmCache> mCacheMap = new HashMap<String, RealmCache>();
    private static final Object LOCK = new Object();


    public RealmCacheManager(byte[] encryptionKey, Client client) {
        this.encryptionKey = encryptionKey;
        this.client = client;
        this.context = client.getContext();
    }

    public RealmCacheManager(byte[] encryptionKey, String prefix, Client client) {
        this.encryptionKey = encryptionKey;
        this.client = client;
        this.context = client.getContext();
    }

    public RealmCacheManager(Client client) {
        this.client = client;
        this.context = client.getContext();
    }

    public RealmCacheManager(String prefix, Client client) {
        this.client = client;
        this.context = client.getContext();
    }


    @Override
    public <T extends GenericJson> ICache<T> getCache(String collection, Class<T> collectionItemClass, Long ttl) {
        synchronized (LOCK){
            DynamicRealm mRealm = getDynamicRealm();
            RealmCache<T> cache;
            try {
                String cacheKey = getClientHash() + File.separator + collection;
                cache = (RealmCache<T>) mCacheMap.get(cacheKey);
                if (cache == null) {
                    cache = new RealmCache<T>(collection, this, collectionItemClass, ttl);

                    mRealm.beginTransaction();
                    boolean isMigrationNeeded = TableNameManager.getShortName(collection, mRealm) == null && mRealm.getSchema().contains(collection);
                    mRealm.commitTransaction();

                    if (!cache.getHash().equals(getTableHash(collection, mRealm))) {
                        mRealm.beginTransaction();
                        try {
                            removeSchemas(prepareSchemasToRemove(collection, mRealm), mRealm);
                        } finally {
                            mRealm.commitTransaction();
                        }

                        if (!collection.equals(SYNC_ITEMS_COLLECTION) && !collection.equals(SYNC_COLLECTION) && client.getSyncManager().getCount(collection) > 0) {
                            client.getSyncManager().clear(collection);
                            new KinveyException("Not synced items were deleted from " + collection + " collection").printStackTrace();
                        }

                        //split table remove and create
                        mRealm.beginTransaction();
                        try {
                            //create table scheme
                            cache.createRealmTable(mRealm);
                            //store table hash for further usage
                            setTableHash(collection, cache.getHash(), mRealm);
                        } finally {
                            mRealm.commitTransaction();
                        }

                    } else if (isMigrationNeeded) {
                        mRealm.beginTransaction();
                        cache.migration(mRealm);
                        mRealm.commitTransaction();
                    } else {
                        // check and remove unnecessary tables like ..._acl_kmd
                        mRealm.beginTransaction();
                        RealmSchema schema = mRealm.getSchema();
                        if (schema.contains(collection + "_" + KinveyMetaData.AccessControlList.ACL + "_" + KinveyMetaData.KMD)) {
                            cache.checkAclKmdFields(mRealm);
                        }
                        mRealm.commitTransaction();
                    }

                    mRealm.beginTransaction();
                    // Check that ACL and KMD fields don't exist in embedded objects (like sync_meta_kmd)
                    boolean isNeedDeleteEbbeddedSupportTables = mRealm.getSchema().contains(TableNameManager.getShortName(TableNameManager.getShortName(TableNameManager.getShortName(SYNC_COLLECTION, mRealm) + "_meta", mRealm) + "_" + KinveyMetaData.KMD, mRealm));
                    mRealm.commitTransaction();
                    if (isNeedDeleteEbbeddedSupportTables) {
                        mRealm.beginTransaction();
                        RealmResults<DynamicRealmObject> collections = mRealm.where(TABLE_HASH_NAME).findAll();
                        List<String> tablesToRemove = new ArrayList<>();//ACL and KMD embedded table names
                        for (DynamicRealmObject realmObject : collections) {
                            tablesToRemove.addAll(prepareACLAndKMDSchemasFromEmbeddedObjectToRemoving(String.valueOf(realmObject.get("collection")), mRealm, false));
                        }
                        removeSchemas(tablesToRemove, mRealm);
                        mRealm.commitTransaction();
                    }

                    mCacheMap.put(cacheKey, cache);
                } else {
                    if (!collectionItemClass.isAssignableFrom(cache.getCollectionItemClass()) &&
                            !cache.getCollectionItemClass().isAssignableFrom(collectionItemClass)) {
                        throw new KinveyException("Class implementation for collection have been changed during runtime",
                                "Please review the BaseDataStore usage, parameter should remain the same for same collection",
                                "Seems like you have used different classes for same collection in AsyncAppDataCreation");
                    }
                    //create new instance because ttl values differs for different store types and
                    cache = new RealmCache<T>(collection, this, collectionItemClass, ttl);

                }
                cache.setTtl(ttl);
            } finally {
                mRealm.close();
            }

            return cache;
        }
    }

    /**
     * Find all schemas (table names) to removing by collection name
     * @param collection collection name
     * @param realm Realm object
     * @return list of schemas to removing
     */
    private List<String> prepareSchemasToRemove(String collection, DynamicRealm realm) {
        RealmSchema currentSchema = realm.getSchema();
        String originalName;
        String className;
        List<String> schemasToDelete = new ArrayList<>();
        Set<RealmObjectSchema> schemas = currentSchema.getAll();
        for (RealmObjectSchema schema : schemas) {
            className = schema.getClassName();
            //search class
            if (className.equals(TableNameManager.getShortName(collection, realm))) {
                schemasToDelete.add(className);
                //search sub-classes
                for (RealmObjectSchema subClassSchema : schemas) {
                    originalName = TableNameManager.getOriginalName(subClassSchema.getClassName(), realm);
                    if (originalName != null && originalName.startsWith(className + "_")) {
                        schemasToDelete.addAll(prepareSchemasToRemove(originalName, realm));
                    }
                }
            }
        }
        return schemasToDelete;
    }

    /**
     * Get all schemas (table names) for _kmd and _acl fields in embedded objects to removing
     * @param tableName name of the table (class) where searching
     * @param realm Realm object
     * @param isEmbedded true if it's embedded object
     * @return list of schemas to removing
     */
    private List<String> prepareACLAndKMDSchemasFromEmbeddedObjectToRemoving(String tableName, DynamicRealm realm, boolean isEmbedded) {
        RealmSchema currentSchema = realm.getSchema();
        String originalName;
        String className;
        List<String> schemasToDelete = new ArrayList<>();
        Set<RealmObjectSchema> schemas = currentSchema.getAll();
        for (RealmObjectSchema schema : schemas) {
            className = schema.getClassName();
            if (className.equals(TableNameManager.getShortName(tableName, realm))) {
                for (RealmObjectSchema subClassSchema : schemas) {
                    originalName = TableNameManager.getOriginalName(subClassSchema.getClassName(), realm);
                    if (originalName == null) {
                        continue;
                    }
                    if (isEmbedded && (originalName.equals(className + "_" + KinveyMetaData.KMD))) {
                        schema.removeField(KinveyMetaData.KMD);
                        schemasToDelete.add(subClassSchema.getClassName());
                    } else if (isEmbedded && (originalName.equals(className + "_" + KinveyMetaData.AccessControlList.ACL))) {
                        schema.removeField(KinveyMetaData.AccessControlList.ACL);
                        schemasToDelete.add(subClassSchema.getClassName());
                    } else if (originalName.startsWith(className + "_")) {
                        schemasToDelete.addAll(prepareACLAndKMDSchemasFromEmbeddedObjectToRemoving(originalName, realm, true));
                    }
                }
            }
        }
        return schemasToDelete;
    }

    /**
     * Remove realm tables by names
     * @param tableNames table names to removing
     * @param realm Realm object
     */
    private void removeSchemas(List<String> tableNames, DynamicRealm realm) {
        RealmSchema realmSchema = realm.getSchema();
        for (String tableName : tableNames) {
            if (realmSchema.get(tableName).hasPrimaryKey()) {
                realmSchema.get(tableName).removePrimaryKey();
            }
            realmSchema.remove(tableName);
            TableNameManager.removeShortName(tableName, realm);
        }
    }

    @Override
    public void clear() {
        synchronized (LOCK) {
            Realm.deleteRealm(getRealmConfiguration());
        }
    }

    private void init(DynamicRealm mRealm){
        RealmSchema schema = mRealm.getSchema();
        RealmObjectSchema tableHashScheme = schema.get(TABLE_HASH_NAME);
        if (tableHashScheme == null){
            mRealm.beginTransaction();
            createTableHashScheme(schema);
            mRealm.commitTransaction();
        }
    }



    private void createTableHashScheme(RealmSchema dbSchema){
        RealmObjectSchema tableHashScheme = dbSchema.create(TABLE_HASH_NAME);
        tableHashScheme.addField("collection", String.class, FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED);
        tableHashScheme.addField("hash", String.class, FieldAttribute.REQUIRED);
    }

    private String getTableHash(String collection, DynamicRealm mRealm){
        DynamicRealmObject res = mRealm.where(TABLE_HASH_NAME)
                .equalTo("collection", collection)
                .findFirst();
        return res != null ? res.getString("hash") : "";
    }

    /**
     * Create hash record for collection to track changes
     * NOTE: this method should be called within existing realm transaction
     * @param collection Collection name
     * @param hash Computed hash of the table
     */
    private void setTableHash(String collection, String hash, DynamicRealm mRealm){
        DynamicRealmObject obj = mRealm.where(TABLE_HASH_NAME)
                .equalTo("collection", collection).findFirst();
        if (obj == null){
            obj = mRealm.createObject(TABLE_HASH_NAME,collection);
        }
        obj.set("hash", hash);
    }


    private String getClientHash(){
        Uri server = Uri.parse(client.getBaseUrl());
        return server.getHost()+"_"+server.getPort();
    }

    /**
     * get Prepared DynamicRealm since realm object can not be shared between threads
     */
    DynamicRealm getDynamicRealm(){
        synchronized (LOCK){
            DynamicRealm realm;
            try {
                realm = DynamicRealm.getInstance(getRealmConfiguration());
            } catch (RealmFileException exception) {
                if (exception.getKind() != null && exception.getKind().name().equals("ACCESS_ERROR")) {
                    throw new KinveyException("Access Error", "Use correct encryption key", "You are using wrong encryption key");
                } else {
                   throw exception;
                }
            }
            init(realm);
            return realm;
        }
    }

    private RealmConfiguration getRealmConfiguration() {
        if (realmConfiguration == null) {
            if (encryptionKey != null) {
                realmConfiguration = new RealmConfiguration.Builder()
                        .name(prefix + "_" + getClientHash())
                        .encryptionKey(encryptionKey)
                        .build();
            } else {
                realmConfiguration = new RealmConfiguration.Builder()
                        .name(prefix + "_" + getClientHash())
                        .build();
            }
        }
        return realmConfiguration;
    }

}
