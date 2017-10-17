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
            RealmQuery<DynamicRealmObject> realmQuery = mRealm.where(TableNameManager.getInstance(mRealm).getShortName(mCollection, mRealm))
                    .greaterThanOrEqualTo(ClassHash.TTL, Calendar.getInstance().getTimeInMillis());
            boolean isIgnoreIn = isQueryContainsInOperator(query.getQueryFilterMap());
            QueryHelper.prepareRealmQuery(realmQuery, query.getQueryFilterMap(), isIgnoreIn);
            RealmResults<DynamicRealmObject> objects = null;

            final Map<String, AbstractQuery.SortOrder> sortingOrders = query.getSort();
            int limit = query.getLimit();
            int skip = query.getSkip();

            objects = realmQuery.findAll();

            for (Iterator<DynamicRealmObject> iterator = objects.iterator(); iterator.hasNext(); ) {
                DynamicRealmObject obj = iterator.next();
                ret.add(ClassHash.realmToObject(obj, mCollectionItemClass));
            }

            if (isIgnoreIn) {
                checkCustomInQuery(query.getQueryFilterMap(), ret);
            }

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
            RealmQuery<DynamicRealmObject> query = mRealm.where(TableNameManager.getInstance(mRealm).getShortName(mCollection, mRealm))
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
            DynamicRealmObject obj = mRealm.where(TableNameManager.getInstance(mRealm).getShortName(mCollection, mRealm))
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
            RealmQuery<DynamicRealmObject> query = mRealm.where(TableNameManager.getInstance(mRealm).getShortName(mCollection, mRealm))
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
            if (!isQueryContainsInOperator(query.getQueryFilterMap())) {
                mRealm.beginTransaction();
                
                RealmQuery<DynamicRealmObject> realmQuery = mRealm.where(TableNameManager.getInstance(mRealm).getShortName(mCollection, mRealm));
                QueryHelper.prepareRealmQuery(realmQuery, query.getQueryFilterMap());

                RealmResults result = realmQuery.findAll();

                ret = result.size();
                int limit = query.getLimit();
                int skip = query.getSkip();

                if (limit > 0)
                {
                    // limit modifier has been applied, so take a subset of the Realm result set
                    int endIndex = Math.min(ret, (skip+limit));
                    List<DynamicRealmObject> subresult = result.subList(skip, endIndex);
                    List<String> ids = new ArrayList<String>();
                    ret = subresult.size();
                    for (DynamicRealmObject id : subresult) {
                        ids.add((String)id.get("_id"));
                    }
                    mRealm.commitTransaction();
                    if (ids.size() > 0) {
                        this.delete(ids);
                    }
                } else if (skip > 0) {
                    // only skip modifier has been applied, so take a subset of the Realm result set
                    if (skip < result.size()) {
                        List<DynamicRealmObject> subresult = result.subList(skip, result.size());
                        List<String> ids = new ArrayList<String>();
                        ret = subresult.size();
                        for (DynamicRealmObject id : subresult) {
                            ids.add((String) id.get("_id"));
                        }
                        mRealm.commitTransaction();
                        this.delete(ids);
                    }
                    else {
                        ret = 0;
                    }
                } else {
                    // no skip or limit applied to query, so delete all results from Realm
                    result.deleteAllFromRealm();
                    mRealm.commitTransaction();
                }

            } else {
                List<T> list = get(query);
                ret = list.size();
                List<String> ids = new ArrayList<>();
                for (T id : list) {
                    ids.add((String)id.get("_id"));
                }
                delete(ids);
                return ret;
            }

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
            RealmQuery<DynamicRealmObject> query = mRealm.where(TableNameManager.getInstance(mRealm).getShortName(mCollection, mRealm))
                    .greaterThanOrEqualTo(ClassHash.TTL, Long.MAX_VALUE)
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
            RealmQuery<DynamicRealmObject> query = mRealm.where(TableNameManager.getInstance(mRealm).getShortName(mCollection, mRealm))
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
            mRealm.where(TableNameManager.getInstance(mRealm).getShortName(mCollection, mRealm))
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
            DynamicRealmObject obj = mRealm.where(TableNameManager.getInstance(mRealm).getShortName(mCollection, mRealm)).findFirst();
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
        try {
            if (!isQueryContainsInOperator(q.getQueryFilterMap())) {
                mRealm.beginTransaction();
                RealmQuery<DynamicRealmObject> query = mRealm.where(TableNameManager.getInstance(mRealm).getShortName(mCollection, mRealm));
                QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
                DynamicRealmObject obj = query.findFirst();
                if (obj != null) {
                    ret = ClassHash.realmToObject(obj, mCollectionItemClass);
                }
                mRealm.commitTransaction();
            } else {
                List<T> list = get(q);
                ret = list.get(0);
            }
        } finally {
            mRealm.close();
        }
        return ret;
    }


    @Override
    public long count(Query q) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();
        long ret = 0;
        try {
            if (q != null && !isQueryContainsInOperator(q.getQueryFilterMap())) {
                mRealm.beginTransaction();
                RealmQuery<DynamicRealmObject> query = mRealm.where(TableNameManager.getInstance(mRealm).getShortName(mCollection, mRealm));
                QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
                ret = query.count();
                mRealm.commitTransaction();
            } else {
                List<T> list;
                if (q != null) {
                    list = get(q);
                }
                else {
                    list = get();
                }
                ret = list.size();
            }
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

    private boolean isQueryContainsInOperator(Map<String, Object> queryMap) {
        for (Map.Entry<String, Object> entity : queryMap.entrySet()) {
            Object params = entity.getValue();
            String field = entity.getKey();

            if (field.equalsIgnoreCase("$or") || field.equalsIgnoreCase("$and")) {
                if (params.getClass().isArray()){
                    Map<String, Object>[] components = (Map<String, Object>[])params;
                    if (components.length > 0) {

                        for (Map<String, Object> component : components) {
                            if (isQueryContainsInOperator(component)) {
                                return true;
                            }
                        }
                    }
                }
            }
            if (field.contains(".")) {
                return false;
            }
            if (params instanceof Map) {
                for (Map.Entry<String, Object> paramMap : ((Map<String, Object>) params).entrySet()) {
                    if (paramMap.getKey().equalsIgnoreCase("$in")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private List<T> checkCustomInQuery(Map<String, Object> queryMap, List<T> ret) {
        //helper for ".in()" operator in List of primitives fields, because Realm doesn't support it
        for (Map.Entry<String, Object> entity : queryMap.entrySet()){
            Object params = entity.getValue();
            String field = entity.getKey();

            if (field.equalsIgnoreCase("$or")) {
                DynamicRealm mRealm = mCacheManager.getDynamicRealm();
                RealmResults<DynamicRealmObject> objects;
                //get all objects from realm. It need for make manual search in elements with "in" operator
                try {
                    objects = mRealm.where(TableNameManager.getInstance(mRealm).getShortName(mCollection, mRealm))
                            .greaterThanOrEqualTo(ClassHash.TTL, Calendar.getInstance().getTimeInMillis())
                            .findAll();

                } finally {
                    mRealm.close();
                }

                List<T> allItems = new ArrayList<>();
                for (DynamicRealmObject obj : objects) {
                    allItems.add(ClassHash.realmToObject(obj, mCollectionItemClass));
                }
                if (params.getClass().isArray()){
                    Map<String, Object>[] components = (Map<String, Object>[])params;
                    if (components.length > 0) {
                        List<T> newItems = new ArrayList<T>();

                        //get items from both sides of "or"
                        for (Map<String, Object> component : components) {
                            if (isQueryContainsInOperator(component)) {
                                newItems = checkCustomInQuery(component, allItems);
                            }
                        }
                        //merge items from left and right parts of "or"
                        if (newItems != null && ret != null) {
                            // "ret" - it's items from search with was made exclude "in" operator
                            // "newItems" - it's items from manual search with "in" operator
                            ArrayList<T> retCopy = new ArrayList<T>(ret);
                            boolean isItemExist;
                            for (T item : newItems) {
                                isItemExist = false;
                                for(T oldItem : retCopy) {
                                    if ((oldItem.get("_id")).equals(item.get("_id"))) {
                                        isItemExist = true;
                                        break;
                                    }
                                }
                                if (!isItemExist) {
                                    ret.add(item);
                                }
                            }

                        }

                    }
                }
            } else if (field.equalsIgnoreCase("$and")) {
                if (params.getClass().isArray()){
                    Map<String, Object>[] components = (Map<String, Object>[])params;
                    if (components.length > 0) {
                        for (Map<String, Object> component : components) {
                            ret = checkCustomInQuery(component, ret);
                        }
                    }
                }
            }

            if (params instanceof Map) {
                Class clazz;
                Types types;
                for (Map.Entry<String, Object> paramMap : ((Map<String, Object>) params).entrySet()) {
                    String operation = paramMap.getKey();
                    //paramMap.getValue() - contains operator's parameters
                    if (!ClassHash.isArrayOrCollection(paramMap.getValue().getClass())) {
                        return ret;
                    }
                    Object[] operatorParams = (Object[]) paramMap.getValue();
                    clazz = ((Object[]) paramMap.getValue())[0].getClass();
                    types = Types.getType(clazz);
                    if (operation.equalsIgnoreCase("$in")) {
                        ArrayList<T> retCopy = new ArrayList<T>(ret);
                        for (T t: retCopy) {
                            boolean isArray = t.get(field) instanceof ArrayList;
                            boolean isExist = false;
/*                            //check that search field is List (not primitives or object)
                            if (t.get(field) instanceof ArrayList) {*/
                            ArrayList arrayList = null;
                            if (isArray) {
                                arrayList = ((ArrayList) t.get(field));
                            } else {
                                // if search field is not LIST
                                switch (types) {
                                    case LONG:
                                        for (Long l : (Long[])operatorParams) {
                                            isExist = l.compareTo((Long)t.get(field)) == 0;
                                            if (isExist) {
                                                break;
                                            }
                                        }
                                        if (!isExist) {
                                            ret.remove(t);
                                        }
                                        break;
                                    case STRING:
                                        for (String s : (String[])operatorParams) {
                                            isExist = s.compareTo((String)t.get(field)) == 0;
                                            if (isExist) {
                                                break;
                                            }
                                        }
                                        if (!isExist) {
                                            ret.remove(t);
                                        }
                                        break;
                                    case BOOLEAN:
                                        for (Boolean b : (Boolean[])operatorParams) {
                                            isExist = b.compareTo((Boolean)t.get(field)) == 0;
                                            if (isExist) {
                                                break;
                                            }
                                        }
                                        if (!isExist) {
                                            ret.remove(t);
                                        }
                                        break;
                                    case INTEGER:
                                        for (Integer i : (Integer[])operatorParams) {
                                            isExist = i.compareTo((Integer)t.get(field)) == 0;
                                            if (isExist) {
                                                break;
                                            }
                                        }
                                        if (!isExist) {
                                            ret.remove(t);
                                        }
                                        break;
                                    case FLOAT:
                                        for (Float i : (Float[])operatorParams) {
                                            isExist = i.compareTo((Float)t.get(field)) == 0;
                                            if (isExist) {
                                                break;
                                            }
                                        }
                                        if (!isExist) {
                                            ret.remove(t);
                                        }
                                        break;
                                }
                            }
                            // if search field is LIST
                            if (isArray && arrayList.size() > 0 && operatorParams.length > 0) {
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
                                        ArrayList<String> listOfString = new ArrayList<String>(arrayList);
                                        for (String sValue : listOfString) {
                                            for (String s : (String[])operatorParams) {
                                                isExist = sValue.compareTo(String.valueOf(s)) == 0;
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
                                    case BOOLEAN:
                                        ArrayList<Boolean> listOfBoolean = new ArrayList<Boolean>(arrayList);
                                        for (Boolean bValue : listOfBoolean) {
                                            for (Boolean b : (Boolean[])operatorParams) {
                                                isExist = bValue.compareTo(b) == 0;
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
