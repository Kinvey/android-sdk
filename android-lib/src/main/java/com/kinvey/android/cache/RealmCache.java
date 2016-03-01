package com.kinvey.android.cache;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.query.AbstractQuery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmObjectSchema;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import io.realm.Sort;

/**
 * Created by Prots on 1/26/16.
 */
public class RealmCache<T extends GenericJson> implements ICache<T> {
    private String mCollection;
    private RealmCacheManager mCacheManager;
    private Class<T> mCollectionItemClass;
    private long ttl;


    public RealmCache(String collection, RealmCacheManager cacheManager, Class<T> collectionItemClass, long ttl) {
        this.mCollection = collection;
        this.mCacheManager = cacheManager;
        this.mCollectionItemClass = collectionItemClass;
        this.ttl = ttl > 0 ? ttl : 0;
    }

    @Override
    public List<T> get(Query query) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();
        RealmQuery<DynamicRealmObject> realmQuery = mRealm.where(mCollection)
                .greaterThanOrEqualTo(ClassHash.TTL_FIELD, Calendar.getInstance().getTimeInMillis());
        QueryHelper.prepareRealmQuery(realmQuery, query.getQueryFilterMap());

        RealmResults<DynamicRealmObject> objects = null;

        Map<String, AbstractQuery.SortOrder> sortingOrders = query.getSort();
        if (sortingOrders != null && sortingOrders.size() > 0) {
            String[] fields = new String[sortingOrders.size()];
            Sort[] sort = new Sort[sortingOrders.size()];
            int i = 0;
            for (Map.Entry<String, AbstractQuery.SortOrder> sorting : sortingOrders.entrySet()){
                fields[i] = sorting.getKey();
                switch (sorting.getValue()){
                    case ASC:
                        sort[i] = Sort.ASCENDING;
                        break;

                    case DESC:
                        sort[i] = Sort.DESCENDING;
                        break;
                }
                i++;
            }
            objects = realmQuery.findAllSorted(fields, sort);
        } else {
            objects = realmQuery.findAll();
        }

        List<T> ret = new ArrayList<T>();

        for (Iterator<DynamicRealmObject> iterator = objects.iterator(); iterator.hasNext(); ){
            DynamicRealmObject obj = iterator.next();
            ret.add(ClassHash.realmToObject(obj, mCollectionItemClass));
        }
        mRealm.close();
        return ret;
    }

    @Override
    public List<T> get(Iterable<String> ids) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection)
                .greaterThanOrEqualTo(ClassHash.TTL_FIELD, Calendar.getInstance().getTimeInMillis())
                .beginGroup();
        Iterator<String> iterator = ids.iterator();
        if (iterator.hasNext()){
            query.equalTo("_id", iterator.next());
            while (iterator.hasNext()){
                String id = iterator.next();
                query.or().equalTo("_id", id);
            }
        }
        query.endGroup();

        RealmResults<DynamicRealmObject> objects = query.findAll();

        List<T> ret = new ArrayList<T>();

        for (Iterator<DynamicRealmObject> objIter = objects.iterator(); objIter.hasNext(); ){
            DynamicRealmObject obj = objIter.next();
            ret.add(ClassHash.realmToObject(obj, mCollectionItemClass));
        }
        mRealm.close();
        return ret;
    }

    @Override
    public T get(String id) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        DynamicRealmObject obj = mRealm.where(mCollection)
                .equalTo("_id", id)
                .greaterThanOrEqualTo(ClassHash.TTL_FIELD, Calendar.getInstance().getTimeInMillis())
                .findFirst();
        T ret = obj == null ? null : ClassHash.realmToObject(obj, mCollectionItemClass);
        mRealm.close();
        return ret;
    }


    @Override
    public List<T> get() {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection)
                .greaterThanOrEqualTo(ClassHash.TTL_FIELD, Calendar.getInstance().getTimeInMillis());

        RealmResults<DynamicRealmObject> objects = query
                .findAll();

        List<T> ret = new ArrayList<T>();

        for (Iterator<DynamicRealmObject> iterator = objects.iterator(); iterator.hasNext(); ){
            DynamicRealmObject obj = iterator.next();
            ret.add(ClassHash.realmToObject(obj, mCollectionItemClass));
        }
        mRealm.close();
        return ret;
    }



    @Override
    public List<T> save(Iterable<T> items) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        mRealm.beginTransaction();
        List<T> ret = new ArrayList<T>();
        try{
            for (T item : items){
                item.put("_id", insertOrUpdate(item, mRealm));
                ret.add(item);
            }
        } finally {
            mRealm.commitTransaction();
        }
        mRealm.close();
        return ret;
    }



    @Override
    public T save(T item) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        String ret = null;
        mRealm.beginTransaction();
        try{
            item.put("_id", insertOrUpdate(item, mRealm));

        } finally {
            mRealm.commitTransaction();
        }
        mRealm.close();
        return item;
    }

    @Override
    public int delete(Query query) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        RealmQuery<DynamicRealmObject> realmQuery = mRealm.where(mCollection);
        QueryHelper.prepareRealmQuery(realmQuery, query.getQueryFilterMap());

        int ret = 0;
        RealmResults result = realmQuery.findAll();

        ret = result.size();
        result.clear();
        mRealm.close();

        return ret;

    }

    @Override
    public int delete(Iterable<String> ids) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        int ret = 0;

        mRealm.beginTransaction();
        try{
            RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection)
                    .beginGroup();
            Iterator<String> iterator = ids.iterator();
            if (iterator.hasNext()){
                query.equalTo("_id", iterator.next());
                for ( ; iterator.hasNext(); ){
                    query.or().equalTo("_id", iterator.next());
                }

            }
            query.endGroup();

            RealmResults result = query.findAll();
            ret = result.size();

            result.clear();
        } finally {
            mRealm.commitTransaction();
        }
        mRealm.close();
        return ret;
    }

    @Override
    public int delete(String id) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        mRealm.beginTransaction();

        int ret = 0;

        try{
            RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection)
                    .equalTo("_id", id);
            RealmResults realmResults = query.findAll();
            ret = realmResults.size();
            realmResults.clear();
        } finally {
            mRealm.commitTransaction();
        }
        mRealm.close();
        return ret;
    }

    public String getCollection() {
        return mCollection;
    }

    public void clear(){
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        mRealm.beginTransaction();
        try {
            mRealm.where(mCollection)
                    .findAll()
                    .clear();
        } finally {
            mRealm.commitTransaction();
        }
        mRealm.close();
    }

    public Class<T> getCollectionItemClass() {
        return mCollectionItemClass;
    }

    public String getHash(){
        return ClassHash.getClassHash(getCollectionItemClass());
    }

    public void createRealmTable(DynamicRealm realm){
        ClassHash.createScheme(mCollection, realm, mCollectionItemClass);
    }

    private String insertOrUpdate(T item, DynamicRealm mRealm){
        item.put(ClassHash.TTL_FIELD, getItemExpireTime());
        ClassHash.saveData(mCollection, mRealm, mCollectionItemClass, item);

        return item.get("_id").toString();
    }



    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl > 0 ? ttl : 0;
    }

    private long getItemExpireTime(){
        long currentTime = Calendar.getInstance().getTimeInMillis();
        return currentTime + ttl < 0 ? Long.MAX_VALUE : currentTime + ttl;
    }
}
