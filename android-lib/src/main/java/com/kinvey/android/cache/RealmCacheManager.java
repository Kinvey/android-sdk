package com.kinvey.android.cache;

import android.content.Context;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.KinveyException;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.cache.ICacheManager;

import java.util.HashMap;
import java.util.Set;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmConfiguration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by Prots on 1/26/16.
 */
public class RealmCacheManager implements ICacheManager {

    private static final String TABLE_HASH_NAME = "__KinveyTables__";

    private static RealmCacheManager _instance;

    private DynamicRealm mRealm;

    private HashMap<String, RealmCache> mCacheMap = new HashMap<String, RealmCache>();
    private static final Object LOCK = new Object();

    public synchronized static RealmCacheManager getInstance(Context context){

        synchronized (LOCK) {
            if (_instance == null) {

                _instance = new RealmCacheManager();
                _instance.mRealm = DynamicRealm.getInstance(new RealmConfiguration.Builder(context).build());
                _instance.init();
            }
            return _instance;
        }
    }

    @Override
    public <T extends GenericJson> ICache<T> getCache(String collection, Class<T> collectionItemClass, Long ttl) {
        synchronized (LOCK){
            RealmCache<T> cache = (RealmCache<T>)mCacheMap.get(collection);

            if (cache == null){
                cache = new RealmCache<T>(collection, mRealm, collectionItemClass, ttl);
                if (!cache.getHash().equals(getTableHash(collection))){
                    //Recreate table
                    mRealm.beginTransaction();
                    try {
                        //remove existing table if any
                        if (mRealm.getSchema().contains(collection)) {
                            //must be defined as soon as primary key stays even if we remove a table
                            mRealm.getSchema().get(collection).removePrimaryKey();
                            mRealm.getSchema().remove(collection);
                        }
                    } finally {
                        mRealm.commitTransaction();
                    }

                    //split table remove and ceate
                    mRealm.beginTransaction();
                    try{
                        //create table scheme
                        cache.createRealmTable(mRealm.getSchema());
                        //store table hash for futher usage
                        setTableHash(collection, cache.getHash());
                    } finally {
                        mRealm.commitTransaction();
                    }

                }
                mCacheMap.put(collection, cache);
            } else {
                if (!cache.getHash().equals(ClassHash.getClassHash(collectionItemClass))){
                    throw new KinveyException("Class implementation for collection have been changed during runtime",
                            "Please review the AsyncAppData usage, parameter should remain the same for same collection",
                            "Seems like you have used different classes for same colledtion in AsyncAppDataCreaton");
                }
            }
            cache.setTtl(ttl);
            return cache;
        }
    }

    @Override
    public void clear() {
        synchronized (LOCK) {
            Set<RealmObjectSchema> schemas = mRealm.getSchema().getAll();
            mRealm.beginTransaction();
            for (RealmObjectSchema schema : schemas) {
                schema.removePrimaryKey();
                mRealm.getSchema().remove(schema.getClassName());
            }
            mRealm.commitTransaction();
            _instance = null;
        }
    }

    private void init(){
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

    private String getTableHash(String collection){

        DynamicRealm realm = DynamicRealm.getInstance(mRealm.getConfiguration());
        DynamicRealmObject res = realm.where(TABLE_HASH_NAME)
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
    private void setTableHash(String collection, String hash){
        DynamicRealmObject obj = mRealm.where(TABLE_HASH_NAME)
                .equalTo("collection", collection).findFirst();
        if (obj == null){
            obj = mRealm.createObject(TABLE_HASH_NAME,collection);
        }
        obj.set("hash", hash);
    }

}
