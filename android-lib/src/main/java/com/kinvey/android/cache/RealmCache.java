package com.kinvey.android.cache;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;

import java.util.ArrayList;
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

/**
 * Created by Prots on 1/26/16.
 */
public class RealmCache<T extends GenericJson> implements ICache<T> {
    private String mCollection;
    private DynamicRealm mRealm;
    private Class<T> mCollectionItemClass;

    public RealmCache(String collection, DynamicRealm realm, Class<T> collectionItemClass) {
        this.mCollection = collection;
        this.mRealm = realm;
        this.mCollectionItemClass = collectionItemClass;
    }

    @Override
    public List<T> get(Query query) {
        RealmQuery<DynamicRealmObject> realmQuery = mRealm.where(mCollection);
        QueryHelper.prepareRealmQuery(realmQuery, query);

        RealmResults<DynamicRealmObject> objects = realmQuery.findAll();

        List<T> ret = new ArrayList<T>();

        for (Iterator<DynamicRealmObject> iterator = objects.iterator(); iterator.hasNext(); ){
            DynamicRealmObject obj = iterator.next();
            ret.add(realmToObject(obj));
        }

        return ret;
    }

    @Override
    public List<T> get(List<String> ids) {
        RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection)
                .beginGroup();
        if (ids.size() > 0){
            query.equalTo("_id", ids.get(0));
            for (int i = 0 ; i < ids.size(); i++){
                query.or().equalTo("_id", ids.get(i));
            }

        }
        query.endGroup();
        RealmResults<DynamicRealmObject> objects = query.findAll();

        List<T> ret = new ArrayList<T>();

        for (Iterator<DynamicRealmObject> iterator = objects.iterator(); iterator.hasNext(); ){
            DynamicRealmObject obj = iterator.next();
            ret.add(realmToObject(obj));
        }
        return ret;
    }

    @Override
    public T get(String id) {
        DynamicRealmObject obj = mRealm.where(mCollection)
                .equalTo("_id", id)
                .findFirst();

        return obj == null ? null : realmToObject(obj);
    }


    @Override
    public List<T> get() {
        RealmQuery<DynamicRealmObject> query = mRealm.where(mCollection);

        RealmResults<DynamicRealmObject> objects = query.findAll();

        List<T> ret = new ArrayList<T>();

        for (Iterator<DynamicRealmObject> iterator = objects.iterator(); iterator.hasNext(); ){
            DynamicRealmObject obj = iterator.next();
            ret.add(realmToObject(obj));
        }
        return ret;
    }



    @Override
    public List<String> save(Iterable<T> items) {
        mRealm.beginTransaction();
        List<String> ids = new ArrayList<String>();
        try{
            for (T item : items){
                ids.add(insertOrUpdate(item));
            }
        } finally {
            mRealm.commitTransaction();
        }
        return ids;
    }



    @Override
    public String save(T item) {
        String ret = null;
        mRealm.beginTransaction();
        try{
            ret = insertOrUpdate(item);
        } finally {
            mRealm.commitTransaction();

        }

        return ret;
    }

    @Override
    public void delete(Query query) {

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
        return obj.get("_id").toString();
    }

    private T realmToObject(DynamicRealmObject dynamic){
        T ret = null;
        try {
            ret = mCollectionItemClass.newInstance();
            Map<String, Class> fields = ClassHash.getAllowedFields(mCollectionItemClass);
            for (Map.Entry<String, Class> entry : fields.entrySet()){
                ret.put(entry.getKey(), dynamic.get(entry.getKey()));
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
