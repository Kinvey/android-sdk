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
    private DynamicRealm mRealm;
    private Class<T> mCollectionItemClass;
    private long ttl;


    public RealmCache(String collection, DynamicRealm realm, Class<T> collectionItemClass, long ttl) {
        this.mCollection = collection;
        this.mRealm = realm;
        this.mCollectionItemClass = collectionItemClass;
        this.ttl = ttl;
    }

    @Override
    public List<T> get(Query query) {
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
            ret.add(realmToObject(obj));
        }

        return ret;
    }

    @Override
    public List<T> get(Iterable<String> ids) {
        RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection)
                .greaterThanOrEqualTo(ClassHash.TTL_FIELD, Calendar.getInstance().getTimeInMillis())
                .beginGroup();
        Iterator<String> iterator = ids.iterator();
        if (iterator.hasNext()){
            query.equalTo("_id", iterator.next());
            while (iterator.hasNext()){
                query.or().equalTo("_id", iterator.next());
            }
        }
        query.endGroup();

        RealmResults<DynamicRealmObject> objects = query.findAll();

        List<T> ret = new ArrayList<T>();

        for (Iterator<DynamicRealmObject> objIter = objects.iterator(); iterator.hasNext(); ){
            DynamicRealmObject obj = objIter.next();
            ret.add(realmToObject(obj));
        }
        return ret;
    }

    @Override
    public T get(String id) {
        DynamicRealmObject obj = mRealm.where(mCollection)
                .equalTo("_id", id)
                .greaterThanOrEqualTo(ClassHash.TTL_FIELD, Calendar.getInstance().getTimeInMillis())
                .findFirst();

        return obj == null ? null : realmToObject(obj);
    }


    @Override
    public List<T> get() {
        RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection)
                .greaterThanOrEqualTo(ClassHash.TTL_FIELD, Calendar.getInstance().getTimeInMillis());

        RealmResults<DynamicRealmObject> objects = query
                .findAll();

        List<T> ret = new ArrayList<T>();

        for (Iterator<DynamicRealmObject> iterator = objects.iterator(); iterator.hasNext(); ){
            DynamicRealmObject obj = iterator.next();
            ret.add(realmToObject(obj));
        }
        return ret;
    }



    @Override
    public List<T> save(Iterable<T> items) {
        mRealm.beginTransaction();
        List<T> ret = new ArrayList<T>();
        try{
            for (T item : items){
                item.put("_id", insertOrUpdate(item));
                ret.add(item);
            }
        } finally {
            mRealm.commitTransaction();
        }
        return ret;
    }



    @Override
    public T save(T item) {
        String ret = null;
        mRealm.beginTransaction();
        try{
            item.put("_id", insertOrUpdate(item));

        } finally {
            mRealm.commitTransaction();
        }

        return item;
    }

    @Override
    public void delete(Query query) {
        RealmQuery<DynamicRealmObject> realmQuery = mRealm.where(mCollection);
        QueryHelper.prepareRealmQuery(realmQuery, query.getQueryFilterMap());

        realmQuery.findAll().clear();
    }

    @Override
    public void delete(Iterable<String> ids) {
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

            query.findAll().clear();
        } finally {
            mRealm.commitTransaction();
        }
    }

    @Override
    public void delete(String id) {
        mRealm.beginTransaction();
        try{
            RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection)
                    .equalTo("_id", id);
            query.endGroup();

            query.findAll().clear();
        } finally {
            mRealm.commitTransaction();
        }
    }

    public String getCollection() {
        return mCollection;
    }

    public DynamicRealm getRealm() {
        return mRealm;
    }

    public void clear(){
        mRealm.beginTransaction();
        try {
            mRealm.where(mCollection)
                    .findAll()
                    .clear();
        } finally {
            mRealm.commitTransaction();
        }
    }

    public Class<T> getCollectionItemClass() {
        return mCollectionItemClass;
    }

    public String getHash(){
        return ClassHash.getClassHash(getCollectionItemClass());
    }

    public void createRealmTable(RealmSchema schema){

        Map<String, Class> declaredFields = ClassHash.getAllowedFields(mCollectionItemClass);
        RealmObjectSchema table = schema.create(this.mCollection);
        for (Map.Entry<String, Class> entry : declaredFields.entrySet()){
            if (entry.getKey().equals("_id")) {
                table.addField(entry.getKey(), entry.getValue(), FieldAttribute.PRIMARY_KEY);
            } else {
                table.addField(entry.getKey(), entry.getValue());
            }
        }

    }

    private String insertOrUpdate(T item){
        Map<String, Object> data = ClassHash.getData(mCollectionItemClass, item);
        DynamicRealmObject obj = mRealm.where(mCollection)
                .equalTo("_id", (String)data.get("_id"))
                .findFirst();

        if (obj == null){
            obj = mRealm.createObject(mCollection, data.get("_id"));
        } else {
            data.remove("_id");
        }

        for (Map.Entry<String, Object> entry: data.entrySet()){
            obj.set(entry.getKey(), entry.getValue());
        }
        long calculated = Calendar.getInstance().getTimeInMillis() + ttl;

        obj.set(ClassHash.TTL_FIELD, calculated < 0 ? Long.MAX_VALUE : calculated);
        return obj.get("_id").toString();
    }

    private T realmToObject(DynamicRealmObject dynamic){
        T ret = null;
        try {
            ret = mCollectionItemClass.newInstance();
            Map<String, Class> fields = ClassHash.getAllowedFields(mCollectionItemClass);
            for (Map.Entry<String, Class> entry : fields.entrySet()){
                Object o = dynamic.get(entry.getKey());

                if (Number.class.isAssignableFrom(entry.getValue())){
                    Number n = (Number)dynamic.get(entry.getKey());
                    if (Long.class.isAssignableFrom(entry.getValue())){
                        ret.put(entry.getKey(), n.longValue());
                    } else if (Byte.class.isAssignableFrom(entry.getValue())){
                        ret.put(entry.getKey(), n.byteValue());
                    } else if (Integer.class.isAssignableFrom(entry.getValue())){
                        ret.put(entry.getKey(), n.intValue());
                    } else if (Short.class.isAssignableFrom(entry.getValue())){
                        ret.put(entry.getKey(), n.shortValue());
                    } if (Float.class.isAssignableFrom(entry.getValue())){
                        ret.put(entry.getKey(), n.floatValue());
                    } if (Double.class.isAssignableFrom(entry.getValue())){
                        ret.put(entry.getKey(), n.doubleValue());
                    }

                } else {
                    ret.put(entry.getKey(), o);
                }

            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }
}
