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

import com.google.api.client.json.GenericJson;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.query.AbstractQuery;
import com.kinvey.java.query.MongoQueryFilter;
import com.kinvey.java.sync.dto.SyncRequest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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

        final Map<String, AbstractQuery.SortOrder> sortingOrders = query.getSort();
        int limit = query.getLimit();
        int skip = query.getSkip();

        objects = realmQuery.findAll();

        List<T> ret = new ArrayList<T>();

        for (Iterator<DynamicRealmObject> iterator = objects.iterator(); iterator.hasNext(); ){
            DynamicRealmObject obj = iterator.next();
            ret.add(ClassHash.realmToObject(obj, mCollectionItemClass));
        }

        //own sorting implementation
        if (sortingOrders != null && sortingOrders.size() > 0) {
            Collections.sort(ret, new Comparator<T>() {
                @Override
                public int compare(T lhs, T rhs) {
                    int sortRet = 0;

                    for (Map.Entry<String, AbstractQuery.SortOrder> sortOrderEntry : sortingOrders.entrySet()){
                        String fieldName = sortOrderEntry.getKey();
                        String[] path = fieldName.split("\\.");
                        int pathStep = 0;
                        Map currentLhsPathObject = lhs;
                        Map currentRhsPathObject = rhs;
                        while (pathStep < path.length - 1){
                            if (currentLhsPathObject != null &&
                                    currentLhsPathObject.containsKey(path[pathStep]) &&
                                    Map.class.isAssignableFrom(currentLhsPathObject.get(path[pathStep]).getClass())){
                                currentLhsPathObject = (Map)currentLhsPathObject.get(path[pathStep]);
                            } else {
                                currentLhsPathObject = null;
                            }

                            if (currentRhsPathObject != null &&
                                    currentRhsPathObject.containsKey(path[pathStep]) &&
                                    Map.class.isAssignableFrom(currentRhsPathObject.get(path[pathStep]).getClass())){
                                currentRhsPathObject = (Map)currentRhsPathObject.get(path[pathStep]);
                            } else {
                                currentRhsPathObject = null;
                            }
                            pathStep++;
                        }


                        Object l = currentLhsPathObject != null ? currentLhsPathObject.get(path[path.length - 1]) : null;
                        Object r = currentRhsPathObject != null ? currentRhsPathObject.get(path[path.length - 1]) : null;

                        if (Comparable.class.isAssignableFrom(l.getClass())){
                            sortRet = (sortOrderEntry.getValue().equals(Sort.DESCENDING) ? 1 : -1) * ((Comparable)l).compareTo(r);
                        }

                        if (sortRet != 0){
                            break;
                        }

                    }

                    return sortRet;
                }
            });
        }


        //own skipping implementation
        if (skip > 0) {
            for (int i = 0; i < skip; i++) {
                if (ret.size() > 0) {
                    ret.remove(0);
                }
            }
        }


        //own limit implementation
        if (limit > 0 && ret.size() > limit) {
            ret = ret.subList(0, limit);
        }

        mRealm.close();
        return ret;
    }

    @Override
    public List<T> get(Iterable<String> ids) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        List<T> ret = new ArrayList<T>();
        mRealm.beginTransaction();
        try {
            RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection)
                    .greaterThanOrEqualTo(ClassHash.TTL_FIELD, Calendar.getInstance().getTimeInMillis())
                    .beginGroup();
            Iterator<String> iterator = ids.iterator();
            if (iterator.hasNext()) {
                query.equalTo("_id", iterator.next());
                while (iterator.hasNext()) {
                    String id = iterator.next();
                    query.or().equalTo("_id", id);
                }
            }
            query.endGroup();

            RealmResults<DynamicRealmObject> objects = query.findAll();

            for (DynamicRealmObject obj : objects) {
                ret.add(ClassHash.realmToObject(obj, mCollectionItemClass));
            }
        } finally {
            mRealm.commitTransaction();
        }
        mRealm.close();
        return ret;
    }

    @Override
    public T get(String id) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        T ret;
        mRealm.beginTransaction();
        try {
            DynamicRealmObject obj = mRealm.where(mCollection)
                    .equalTo("_id", id)
                    .greaterThanOrEqualTo(ClassHash.TTL_FIELD, Calendar.getInstance().getTimeInMillis())
                    .findFirst();
             ret = obj == null ? null : ClassHash.realmToObject(obj, mCollectionItemClass);
        } finally {
            mRealm.commitTransaction();
        }
        mRealm.close();
        return ret;
    }


    @Override
    public List<T> get() {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        List<T> ret = new ArrayList<T>();
        mRealm.beginTransaction();
        try {
            RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection)
                    .greaterThanOrEqualTo(ClassHash.TTL_FIELD, Calendar.getInstance().getTimeInMillis());

            RealmResults<DynamicRealmObject> objects = query
                    .findAll();

            for (DynamicRealmObject obj : objects) {
                ret.add(ClassHash.realmToObject(obj, mCollectionItemClass));
            }

        } finally {
            mRealm.commitTransaction();
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

        mRealm.beginTransaction();

        int ret = 0;
        try {
            RealmResults result = realmQuery.findAll();

            ret = result.size();
            result.deleteAllFromRealm();
        } finally {
            mRealm.commitTransaction();
        }
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
            result.deleteAllFromRealm();
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
            realmResults.deleteAllFromRealm();
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
                    .deleteAllFromRealm();
        } finally {
            mRealm.commitTransaction();
        }
        mRealm.close();
    }

    @Override
    public T getFirst() {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();;

        T ret = null;

        mRealm.beginTransaction();
        try{
            DynamicRealmObject obj = mRealm.where(mCollection).findFirst();
            if (obj != null){
                ret = ClassHash.realmToObject(obj, mCollectionItemClass);
            }
        } finally {
            mRealm.commitTransaction();
        }

        return ret;
    }

    @Override
    public T getFirst(Query q) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        T ret = null;

        mRealm.beginTransaction();
        try{
            RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection);
            QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
            DynamicRealmObject obj = query.findFirst();
            if (obj != null){
                ret = ClassHash.realmToObject(obj, mCollectionItemClass);
            }
        } finally {
            mRealm.commitTransaction();
        }

        return ret;
    }

    @Override
    public long count(Query q) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();
        long ret = 0;

        mRealm.beginTransaction();
        try{
            RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection);
            QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
            ret = query.count();

        } finally {
            mRealm.commitTransaction();
        }
        return ret;
    }

    @Override
    public Number sum(String sumField, Query q) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();
        Number ret = 0;

        mRealm.beginTransaction();
        try {
            RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection);
            QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
            ret = query.sum(sumField);
        } finally {
            mRealm.commitTransaction();
        }
        return ret;
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

        T clone = (T)item.clone();
        clone.put(ClassHash.TTL_FIELD, getItemExpireTime());

        ClassHash.saveData(mCollection, mRealm, mCollectionItemClass, clone);

        item.set("_id", clone.get("_id"));

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
