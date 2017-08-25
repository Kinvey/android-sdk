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

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by Prots on 1/26/16.
 */
public class RealmCacheManager implements ICacheManager {

    private static final String TABLE_HASH_NAME = "__KinveyTables__";
    private byte[] encryptionKey;
    private final AbstractClient client;
    private final Context context;
    private String prefix = "";
    private RealmConfiguration realmConfiguration;


    private HashMap<String, RealmCache> mCacheMap = new HashMap<String, RealmCache>();
    private static final Object LOCK = new Object();


    public RealmCacheManager(byte[] encryptionKey, Client client){
        this.encryptionKey = encryptionKey;
        this.client = client;
        this.context = client.getContext();
    }

    public RealmCacheManager(byte[] encryptionKey, String prefix, Client client){
        this.encryptionKey = encryptionKey;
        this.client = client;
        this.context = client.getContext();
    }

    public RealmCacheManager(Client client){
        this.client = client;
        this.context = client.getContext();
    }

    public RealmCacheManager(String prefix, Client client){
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
                    if (!cache.getHash().equals(getTableHash(collection, mRealm))) {
                        //Recreate table
                        mRealm.beginTransaction();
                        try {
                            //remove existing table if any
                            RealmSchema currentSceme = mRealm.getSchema();
                            for (RealmObjectSchema schema : currentSceme.getAll()) {
                                if (schema.getClassName().equals(collection) || schema.getClassName().startsWith(collection + "_")) {
                                    String className = schema.getClassName();
                                    if (mRealm.getSchema().get(className).hasPrimaryKey()) {
                                        mRealm.getSchema().get(className).removePrimaryKey();
                                    }
                                    currentSceme.remove(className);
                                }
                            }
                        } finally {
                            mRealm.commitTransaction();
                        }

                        //split table remove and ceate
                        mRealm.beginTransaction();
                        try {
                            //create table scheme
                            cache.createRealmTable(mRealm);
                            //store table hash for futher usage
                            setTableHash(collection, cache.getHash(), mRealm);
                        } finally {
                            mRealm.commitTransaction();
                        }

                    }
                    mCacheMap.put(cacheKey, cache);
                } else {
                    if (!collectionItemClass.isAssignableFrom(cache.getCollectionItemClass()) &&
                            !cache.getCollectionItemClass().isAssignableFrom(collectionItemClass)) {
                        throw new KinveyException("Class implementation for collection have been changed during runtime",
                                "Please review the BaseDataStore usage, parameter should remain the same for same collection",
                                "Seems like you have used different classes for same colledtion in AsyncAppDataCreaton");
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
            Uri server = Uri.parse(client.getBaseUrl());
            DynamicRealm realm = DynamicRealm.getInstance(getRealmConfiguration());
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
