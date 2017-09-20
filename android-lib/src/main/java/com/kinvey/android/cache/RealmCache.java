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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
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
        List<T> ret = new ArrayList<T>();
        try {
            RealmQuery<DynamicRealmObject> realmQuery = mRealm.where(mCollection)
                    .greaterThanOrEqualTo(ClassHash.TTL, Calendar.getInstance().getTimeInMillis());
            QueryHelper.prepareRealmQuery(realmQuery, query.getQueryFilterMap());

            RealmResults<DynamicRealmObject> objects = null;

            final Map<String, AbstractQuery.SortOrder> sortingOrders = query.getSort();
            int limit = query.getLimit();
            int skip = query.getSkip();

            objects = realmQuery.findAll();

            

            for (Iterator<DynamicRealmObject> iterator = objects.iterator(); iterator.hasNext(); ) {
                DynamicRealmObject obj = iterator.next();
                ret.add(ClassHash.realmToObject(obj, mCollectionItemClass));
            }

            checkCustomInQuery(query, ret);

            //own sorting implementation
            if (sortingOrders != null && sortingOrders.size() > 0) {
                Collections.sort(ret, new Comparator<T>() {
                    @Override
                    public int compare(T lhs, T rhs) {
                        int sortRet = 0;

                        for (Map.Entry<String, AbstractQuery.SortOrder> sortOrderEntry : sortingOrders.entrySet()) {
                            String fieldName = sortOrderEntry.getKey();
                            String[] path = fieldName.split("\\.");
                            int pathStep = 0;
                            Map currentLhsPathObject = lhs;
                            Map currentRhsPathObject = rhs;
                            while (pathStep < path.length - 1) {
                                if (currentLhsPathObject != null &&
                                        currentLhsPathObject.containsKey(path[pathStep]) &&
                                        Map.class.isAssignableFrom(currentLhsPathObject.get(path[pathStep]).getClass())) {
                                    currentLhsPathObject = (Map) currentLhsPathObject.get(path[pathStep]);
                                } else {
                                    currentLhsPathObject = null;
                                }

                                if (currentRhsPathObject != null &&
                                        currentRhsPathObject.containsKey(path[pathStep]) &&
                                        Map.class.isAssignableFrom(currentRhsPathObject.get(path[pathStep]).getClass())) {
                                    currentRhsPathObject = (Map) currentRhsPathObject.get(path[pathStep]);
                                } else {
                                    currentRhsPathObject = null;
                                }
                                pathStep++;
                            }


                            Object l = currentLhsPathObject != null ? currentLhsPathObject.get(path[path.length - 1]) : null;
                            Object r = currentRhsPathObject != null ? currentRhsPathObject.get(path[path.length - 1]) : null;

                            if (Comparable.class.isAssignableFrom(l.getClass())) {
                                sortRet = (sortOrderEntry.getValue().equals(Sort.DESCENDING) ? 1 : -1) * ((Comparable) l).compareTo(r);
                            }

                            if (sortRet != 0) {
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
        } finally {
            mRealm.close();    
        }

        
        return ret;
    }

    @Override
    public List<T> get(Iterable<String> ids) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        List<T> ret = new ArrayList<T>();
        try {
            mRealm.beginTransaction();
            RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection)
                    .greaterThanOrEqualTo(ClassHash.TTL, Calendar.getInstance().getTimeInMillis())
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
            mRealm.commitTransaction();
        } finally {
            mRealm.close();
        }
        
        return ret;
    }

    @Override
    public T get(String id) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        T ret;
        try {
            mRealm.beginTransaction();
            DynamicRealmObject obj = mRealm.where(mCollection)
                    .equalTo("_id", id)
                    .greaterThanOrEqualTo(ClassHash.TTL, Calendar.getInstance().getTimeInMillis())
                    .findFirst();
             ret = obj == null ? null : ClassHash.realmToObject(obj, mCollectionItemClass);
            mRealm.commitTransaction();
        } finally {
            mRealm.close();
        }
        
        return ret;
    }


    @Override
    public List<T> get() {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        List<T> ret = new ArrayList<T>();
        try {
            mRealm.beginTransaction();
            RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection)
                    .greaterThanOrEqualTo(ClassHash.TTL, Calendar.getInstance().getTimeInMillis());

            RealmResults<DynamicRealmObject> objects = query
                    .findAll();

            for (DynamicRealmObject obj : objects) {
                ret.add(ClassHash.realmToObject(obj, mCollectionItemClass));
            }
            mRealm.commitTransaction();
        } finally {
            mRealm.close();
        }
        
        return ret;
    }



    @Override
    public List<T> save(Iterable<T> items) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        List<T> ret = new ArrayList<T>();
        try{
            mRealm.beginTransaction();
            for (T item : items){
                item.put("_id", insertOrUpdate(item, mRealm));
                ret.add(item);
            }
            mRealm.commitTransaction();
        } finally {
            mRealm.close();
        }
        
        return ret;
    }



    @Override
    public T save(T item) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        String ret = null;

        try{
            mRealm.beginTransaction();
            item.put("_id", insertOrUpdate(item, mRealm));
            mRealm.commitTransaction();
        } finally {
            mRealm.close();
        }
       
        return item;
    }

    @Override
    public int delete(Query query) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();
        int ret = 0;
        try {
            RealmQuery<DynamicRealmObject> realmQuery = mRealm.where(mCollection);
            QueryHelper.prepareRealmQuery(realmQuery, query.getQueryFilterMap());
            mRealm.beginTransaction();
            RealmResults result = realmQuery.findAll();
            ret = result.size();
            result.deleteAllFromRealm();
            mRealm.commitTransaction();
        } finally {
            mRealm.close();
        }
        return ret;
    }

    @Override
    public int delete(Iterable<String> ids) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        int ret = 0;

        try{
            mRealm.beginTransaction();
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
            mRealm.commitTransaction();
        } finally {
            mRealm.close();
        }
        
        return ret;
    }

    @Override
    public int delete(String id) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();


        int ret = 0;

        try{
            mRealm.beginTransaction();
            RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection)
                    .equalTo("_id", id);
            RealmResults realmResults = query.findAll();
            ret = realmResults.size();
            realmResults.deleteAllFromRealm();
            mRealm.commitTransaction();
        } finally {
            mRealm.close();
        }
        
        return ret;
    }

    public String getCollection() {
        return mCollection;
    }

    public void clear(){
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        try {
            mRealm.beginTransaction();
            mRealm.where(mCollection)
                    .findAll()
                    .deleteAllFromRealm();
            mRealm.commitTransaction();
        } finally {
            mRealm.close();
        }
    }

    @Override
    public T getFirst() {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();;

        T ret = null;

        try{
            mRealm.beginTransaction();
            DynamicRealmObject obj = mRealm.where(mCollection).findFirst();
            if (obj != null){
                ret = ClassHash.realmToObject(obj, mCollectionItemClass);
            }
            mRealm.commitTransaction();
        } finally {
            mRealm.close();
        }

        return ret;
    }

    @Override
    public T getFirst(Query q) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        T ret = null;

        try{
            mRealm.beginTransaction();
            RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection);
            QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
            DynamicRealmObject obj = query.findFirst();
            if (obj != null){
                ret = ClassHash.realmToObject(obj, mCollectionItemClass);
            }
            mRealm.commitTransaction();
        } finally {
            mRealm.close();
        }

        return ret;
    }

    @Override
    public long count(Query q) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();
        long ret = 0;

        try{
            mRealm.beginTransaction();
            RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection);
            QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
            ret = query.count();
            mRealm.commitTransaction();
        } finally {
            mRealm.close();
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
        clone.put(ClassHash.TTL, getItemExpireTime());

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

    private List<T> checkCustomInQuery(Query query, List<T> ret) {
        //helper for ".in()" operator in List of primitives fields, because Realm doesn't support it
        for (Map.Entry<String, Object> entity : query.getQueryFilterMap().entrySet()){
            Object params = entity.getValue();
            String field = entity.getKey();
            if (params instanceof Map) {
                Class clazz;
                Types types;
                for (Map.Entry<String, Object> paramMap : ((Map<String, Object>) params).entrySet()) {
                    String operation = paramMap.getKey();
                    Object[] operatorParams = (Object[]) paramMap.getValue();
                    clazz = ((Object[]) paramMap.getValue())[0].getClass();
                    types = Types.getType(clazz);
                    //check that it's list of primitives but not objects
                    if (types == Types.OBJECT) {
                        return ret;
                    }
                    if (operation.equalsIgnoreCase("$in")) {
                        ArrayList<T> retCopy = new ArrayList<T>(ret);
                        for (T t: retCopy) {
                            if (t.get(field) instanceof ArrayList) {

                                ArrayList arrayList = ((ArrayList) t.get(field));
                                if (arrayList.size() > 0 && operatorParams.length > 0) {

                                    boolean isExist = false;
                                    switch (types) {
                                        case LONG:
                                            ArrayList<Long> listOfLong = new ArrayList<Long>(arrayList);
                                            for (Long lValue : listOfLong) {
                                                for (Long l : (Long[])operatorParams) {
                                                    isExist = l.compareTo(lValue) == 0;
                                                    if (isExist) {
                                                        break;
                                                    }
                                                }
                                                if (isExist)
                                                    break;
                                            }
                                            if (!isExist) {
                                                ret.remove(t);
                                            }
                                            break;
                                        case STRING:
                                        case BOOLEAN:
                                            if (!arrayList.contains(operatorParams[0])) {
                                                ret.remove(t);
                                            }
                                            for (int i = 1; i < operatorParams.length; i++) {
                                                if (!arrayList.contains(operatorParams[i])) {
                                                    ret.remove(t);
                                                }
                                            }
                                            break;
                                        case INTEGER:
                                            ArrayList<Long> listOfInteger = new ArrayList<Long>(arrayList);
                                            for (Long lValue : listOfInteger) {
                                                for (Integer l : (Integer[])operatorParams) {
                                                    isExist = lValue.compareTo(Long.valueOf(l)) == 0;
                                                    if (isExist) {
                                                        break;
                                                    }
                                                }
                                                if (isExist)
                                                    break;
                                            }
                                            if (!isExist) {
                                                ret.remove(t);
                                            }
                                            break;
                                        case FLOAT:
                                            ArrayList<Float> listOfFloat = new ArrayList<Float>(arrayList);
                                            for (Float lValue : listOfFloat) {
                                                for (Float l : (Float[])operatorParams) {
                                                    isExist = lValue.compareTo(l) == 0;
                                                    if (isExist) {
                                                        break;
                                                    }
                                                }
                                                if (isExist)
                                                    break;
                                            }
                                            if (!isExist) {
                                                ret.remove(t);
                                            }
                                            break;

                                    }

                                }
                            }

                        }

                    }
                }
            }
        }
        return ret;
    }


    public enum Types{
        STRING,
        INTEGER,
        LONG,
        BOOLEAN,
        FLOAT,
        OBJECT;

        private static final String ALL_TYPES_STRING = Arrays.toString(Types.values());

        public static Types getType(Class<?> clazz) {
            String className = clazz.getSimpleName().toUpperCase();
            if (ALL_TYPES_STRING.contains(className)) {
                return Types.valueOf(className);
            } else {
                return Types.OBJECT;
            }
        }
    }



}
