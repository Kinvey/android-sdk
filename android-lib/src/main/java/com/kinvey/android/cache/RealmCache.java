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
import com.kinvey.java.KinveyException;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.model.AggregateType;
import com.kinvey.java.model.Aggregation;
import com.kinvey.java.query.AbstractQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmFieldType;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Prots on 1/26/16.
 */
public class RealmCache<T extends GenericJson> implements ICache<T> {

    private static final String ACL = "__acl";
    private static final String KMD = "__kmd";
    private static final String ID = "_id";

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
            RealmQuery<DynamicRealmObject> realmQuery = mRealm.where(TableNameManager.getShortName(mCollection, mRealm))
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
            RealmQuery<DynamicRealmObject> query = mRealm.where(TableNameManager.getShortName(mCollection, mRealm))
                    .greaterThanOrEqualTo(ClassHash.TTL, Calendar.getInstance().getTimeInMillis())
                    .beginGroup();
            Iterator<String> iterator = ids.iterator();
            if (iterator.hasNext()) {
                query.equalTo(ID, iterator.next());
                while (iterator.hasNext()) {
                    String id = iterator.next();
                    query.or().equalTo(ID, id);
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
            DynamicRealmObject obj = mRealm.where(TableNameManager.getShortName(mCollection, mRealm))
                    .equalTo(ID, id)
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
            RealmQuery<DynamicRealmObject> query = mRealm.where(TableNameManager.getShortName(mCollection, mRealm))
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
                item.put(ID, insertOrUpdate(item, mRealm));
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
        try{
            mRealm.beginTransaction();
            item.put(ID, insertOrUpdate(item, mRealm));
            mRealm.commitTransaction();
        } finally {
            mRealm.close();
        }
        return item;
    }

    @Override
    public int delete(Query query) {
        DynamicRealm realm = mCacheManager.getDynamicRealm();
        int i;
        try {
            List<T> items = get(query);
            delete(realm, getACLIds(items), TableNameManager.getShortName(mCollection, realm) + ACL);
            delete(realm, getKMDIds(items), TableNameManager.getShortName(mCollection, realm) + KMD);
            i = delete(realm, query, mCollection);
        } finally {
            realm.close();
        }
        return i;
    }

    private List<String> getACLIds(List<T> items) {
        List<String> ACLIds = new ArrayList<>();
        for (T item : items) {
            ACLIds.add((String)((GenericJson) item.get("_acl")).get("_id"));
        }
        return ACLIds;
    }

    private List<String> getKMDIds(List<T> items) {
        List<String> ACLIds = new ArrayList<>();
        for (T item : items) {
            ACLIds.add((String)((GenericJson) item.get("_kmd")).get("_id"));
        }
        return ACLIds;
    }

    private int delete(DynamicRealm realm, Query query, String tableName) {

        int ret;
            if (!isQueryContainsInOperator(query.getQueryFilterMap())) {
                realm.beginTransaction();

                RealmQuery<DynamicRealmObject> realmQuery = realm.where(TableNameManager.getShortName(tableName, realm));
                QueryHelper.prepareRealmQuery(realmQuery, query.getQueryFilterMap());

                RealmResults result = realmQuery.findAll();

                ret = result.size();
                int limit = query.getLimit();
                int skip = query.getSkip();

                if (limit > 0) {
                    // limit modifier has been applied, so take a subset of the Realm result set
                    int endIndex = Math.min(ret, (skip+limit));
                    List<DynamicRealmObject> subresult = result.subList(skip, endIndex);
                    List<String> ids = new ArrayList<String>();
                    ret = subresult.size();
                    for (DynamicRealmObject id : subresult) {
                        ids.add((String)id.get(ID));
                    }
                    realm.commitTransaction();
                    if (ids.size() > 0) {
                        this.delete(realm, ids, tableName);
                    }
                } else if (skip > 0) {
                    // only skip modifier has been applied, so take a subset of the Realm result set
                    if (skip < result.size()) {
                        List<DynamicRealmObject> subresult = result.subList(skip, result.size());
                        List<String> ids = new ArrayList<String>();
                        ret = subresult.size();
                        for (DynamicRealmObject id : subresult) {
                            ids.add((String) id.get(ID));
                        }
                        realm.commitTransaction();
                        this.delete(realm, ids, tableName);
                    } else {
                        realm.commitTransaction();
                        ret = 0;
                    }
                } else {
                    // no skip or limit applied to query, so delete all results from Realm
                    result.deleteAllFromRealm();
                    realm.commitTransaction();
                }

            } else {
                List<T> list = get(query);
                ret = list.size();
                List<String> ids = new ArrayList<>();
                for (T id : list) {
                    ids.add((String)id.get(ID));
                }
                delete(realm, ids, tableName);
                return ret;
            }

        return ret;
    }

    @Override
    public int delete(Iterable<String> ids) {
        DynamicRealm realm = mCacheManager.getDynamicRealm();
        int i;
        try {
            delete(realm, getACLIds(ids), TableNameManager.getShortName(mCollection, realm) + ACL);
            delete(realm, getKMDIds(ids), TableNameManager.getShortName(mCollection, realm) + KMD);
            i = delete(realm, ids, mCollection);
        } finally {
            realm.close();
        }
        return i;
    }

    private List<String> getACLIds(Iterable<String> ids) {
        List<T> items = get(ids);
        List<String> ACLIds = new ArrayList<>();
        for (T item : items) {
            ACLIds.add((String)((GenericJson) item.get("_acl")).get("_id"));
        }
        return ACLIds;
    }

    private List<String> getKMDIds(Iterable<String> ids) {
        List<T> items = get(ids);
        List<String> ACLIds = new ArrayList<>();
        for (T item : items) {
            ACLIds.add((String)((GenericJson) item.get("_kmd")).get("_id"));
        }
        return ACLIds;
    }

    private int delete(DynamicRealm realm, Iterable<String> ids, String tableName) {
        int ret = 0;
        if (ids.iterator().hasNext()) {
            realm.beginTransaction();
            RealmQuery<DynamicRealmObject> query = realm.where(TableNameManager.getShortName(tableName, realm))
                    .beginGroup();
            Iterator<String> iterator = ids.iterator();
            if (iterator.hasNext()) {
                query.equalTo(ID, iterator.next());
                for (; iterator.hasNext(); ) {
                    query.or().equalTo(ID, iterator.next());
                }
            }
            query.endGroup();
            RealmResults result = query.findAll();
            ret = result.size();
            result.deleteAllFromRealm();
            realm.commitTransaction();
        }
        return ret;
    }

    @Override
    public int delete(String id) {
        DynamicRealm realm = mCacheManager.getDynamicRealm();
        int i;
        try {
            delete(realm, getACLId(id), TableNameManager.getShortName(mCollection, realm) + ACL);
            delete(realm, getKMDId(id), TableNameManager.getShortName(mCollection, realm) + KMD);
            i = delete(realm, id, mCollection);
        } finally {
            realm.close();
        }
        return i;
    }

    private String getACLId(String id) {
        T item = get(id);
        return (String)((GenericJson) item.get("_acl")).get("_id");
    }

    private String getKMDId(String id) {
        T item = get(id);
        return (String)((GenericJson) item.get("_kmd")).get("_id");
    }

    private int delete(DynamicRealm realm, String id, String tableName) {
        int ret = 0;
        realm.beginTransaction();
        RealmQuery<DynamicRealmObject> query = realm.where(TableNameManager.getShortName(tableName, realm))
                .equalTo(ID, id);
        RealmResults realmResults = query.findAll();
        ret = realmResults.size();
        realmResults.deleteAllFromRealm();
        realm.commitTransaction();
        return ret;
    }

    public String getCollection() {
        return mCollection;
    }

    public void clear(){
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();
        try {
            mRealm.beginTransaction();
            mRealm.where(TableNameManager.getShortName(mCollection, mRealm))
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
            DynamicRealmObject obj = mRealm.where(TableNameManager.getShortName(mCollection, mRealm)).findFirst();
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
                RealmQuery<DynamicRealmObject> query = mRealm.where(TableNameManager.getShortName(mCollection, mRealm));
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
                RealmQuery<DynamicRealmObject> query = mRealm.where(TableNameManager.getShortName(mCollection, mRealm));
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

        item.set(ID, clone.get(ID));

        return item.get(ID).toString();
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
                    objects = mRealm.where(TableNameManager.getShortName(mCollection, mRealm))
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
                                    if ((oldItem.get(ID)).equals(item.get(ID))) {
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

    private Aggregation.Result[] calculation(AggregateType type, ArrayList<String> fields, String reduceField, Query q) {
        DynamicRealm mRealm = mCacheManager.getDynamicRealm();

        List<Aggregation.Result> results = new ArrayList<>();


        try {
            mRealm.beginTransaction();
            RealmQuery<DynamicRealmObject> query = mRealm.where(TableNameManager.getShortName(mCollection, mRealm));
            QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
            RealmFieldType fieldType;
            Number ret = null;
            Aggregation.Result result;
            for (String field : fields) {
                RealmResults<DynamicRealmObject> realmObjects = query.findAllSorted(field);
                for (DynamicRealmObject d : realmObjects) {
                    result = new Aggregation.Result();
                    query = realmObjects.where();
                    for (String fieldToQuery : fields) {
                        fieldType = d.getFieldType(fieldToQuery);
                        switch (fieldType) {
                            case STRING:
                                query = query.equalTo(fieldToQuery, String.valueOf(d.get(fieldToQuery)));
                                break;
                            case INTEGER:
                                query = query.equalTo(fieldToQuery, (Long) (d.get(fieldToQuery)));
                                break;
                            case BOOLEAN:
                                query = query.equalTo(field, (Boolean) (d.get(field)));
                                break;
                            case DATE:
                                query = query.equalTo(field, (Date) (d.get(field)));
                                break;
                            case FLOAT:
                                query = query.equalTo(field, (Float) (d.get(field)));
                                break;
                            case DOUBLE:
                                query = query.equalTo(field, (Double) (d.get(field)));
                                break;
                            default:
                                throw new KinveyException("Current fieldType doesn't support. Supported types: STRING, INTEGER, BOOLEAN, DATE, FLOAT, DOUBLE");

                        }
                        result.put(fieldToQuery, d.get(fieldToQuery));

                    }

                    switch (type) {
                        case SUM:
                            ret = query.sum(reduceField);
                            break;
                        case MIN:
                            ret = query.min(reduceField);
                            break;
                        case MAX:
                            ret = query.max(reduceField);
                            break;
                        case AVERAGE:
                            ret = query.average(reduceField);
                            break;
                        case COUNT:
                            ret = query.count();
                            break;
                    }

                    if (ret != null) {
                        result.put("_result", ret);
                        if (results.contains(result)) {
                            continue;
                        }
                        results.add(result);
                    }
                }
            }
            mRealm.commitTransaction();
        } finally {
            mRealm.close();
        }

        Aggregation.Result[] resultsArray = new Aggregation.Result[results.size()];

        return results.toArray(resultsArray);
    }

    @Override
    public Aggregation.Result[] group(AggregateType aggregateType, ArrayList<String> fields, String reduceField, Query q) {
        return calculation(aggregateType, fields, reduceField, q);
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
