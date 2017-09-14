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

import android.util.Log;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.Data;
import com.google.api.client.util.FieldInfo;
import com.kinvey.java.model.KinveyMetaData;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmList;
import io.realm.RealmObjectSchema;

/**
 * Created by Prots on 1/27/16.
 */
public abstract class ClassHash {

    public static String TTL = "__ttl__";
    private static String ID = "_id";

    private static final HashSet<String> PRIVATE_FIELDS = new HashSet<String>(){
        {
            add(TTL);
        }
    };

    private static final Class[] ALLOWED = new Class[]{
            boolean.class,
            byte.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class,
            String.class,
            Date.class,
            byte[].class,

            Boolean.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            String.class,
            Date.class,
            Byte[].class
    };

    //supported fields
    // boolean, byte, short, ìnt, long, float, double, String, Date and byte[]

    public static String getClassHash(Class<? extends GenericJson> clazz) {

        StringBuilder sb = new StringBuilder();

        List<Field> fields = getClassFieldsAndParentClassFields(clazz);

        for (Field f : fields){
            FieldInfo fieldInfo = FieldInfo.of(f);
            if (fieldInfo == null){
                continue;
            }

            if (isArrayOrCollection(fieldInfo.getType())){
                Class underlying = getUnderlying(f);
                if (underlying != null && GenericJson.class.isAssignableFrom(underlying)){
                    String innerHash = getClassHash((Class<? extends GenericJson>) underlying);

                    sb.append("[").append(fieldInfo.getName()).append("]:")
                            .append(innerHash)
                            .append(";");
                }
            }else if (GenericJson.class.isAssignableFrom(fieldInfo.getType())){
                String innerHash = getClassHash((Class<? extends GenericJson>) fieldInfo.getType());
                sb.append(fieldInfo.getName()).append(":").append(innerHash).append(";");
            }  else {
                for (Class c : ALLOWED) {
                    if (fieldInfo.getType().equals(c)) {
                        if (!fieldInfo.getName().equals(ID) && !fieldInfo.getName().equals(TTL)){
                            sb.append(fieldInfo.getName()).append(":").append(c.getName()).append(";");
                        }

                        break;
                    }
                }
            }
        }
        sb.append(ID).append(":").append(String.class.getName()).append(";");
        sb.append(TTL).append(":").append(Long.class.getName()).append(";");


        String hashtext = null;

        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(sb.toString().getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            hashtext = bigInt.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
        } catch (NoSuchAlgorithmException e){
           e.printStackTrace();
        } finally {
            hashtext = sb.toString();
        }

        return hashtext;
    }


    public static RealmObjectSchema createScheme(String name, DynamicRealm realm, Class<? extends GenericJson> clazz){
        RealmObjectSchema schema = realm.getSchema().create(name);

        List<Field> fields = getClassFieldsAndParentClassFields(clazz);

        for (Field f : fields){
            FieldInfo fieldInfo = FieldInfo.of(f);
            if (fieldInfo == null){
                continue;
            }
            if (fieldInfo.getType().isArray() || Collection.class.isAssignableFrom(fieldInfo.getType())){

                Class underlying = getUnderlying(f);

                Log.d("ClassHash", "underlying " + underlying.toString());
                Log.d("ClassHash", "underlying != null: " + (underlying != null));
                Log.d("ClassHash", "GenericJson.class.isAssignableFrom(underlying): " + GenericJson.class.isAssignableFrom(underlying));
                if (underlying != null && GenericJson.class.isAssignableFrom(underlying)){
                    RealmObjectSchema innerScheme = createScheme(name + "_" + fieldInfo.getName(), realm, (Class<? extends GenericJson>) underlying);
                    schema.addRealmListField(fieldInfo.getName(), innerScheme);
                } else {
                    for (Class c : ALLOWED) {
                        if (underlying.equals(c)) {
                            RealmObjectSchema innerScheme = createScheme(name + "_" + fieldInfo.getName(), realm, (Class<? extends GenericJson>) underlying);
                            schema.addRealmListField(fieldInfo.getName(), innerScheme);
                            break;
                        }
                    }
                }

            } else if (GenericJson.class.isAssignableFrom(fieldInfo.getType())){
                RealmObjectSchema innerScheme = createScheme(name + "_" + fieldInfo.getName(), realm, (Class<? extends GenericJson>) fieldInfo.getType());
                schema.addRealmObjectField(fieldInfo.getName(), innerScheme);
            } else {
                for (Class c : ALLOWED) {
                    if (fieldInfo.getType().equals(c)) {
                        if (!fieldInfo.getName().equals(ID)){
                            schema.addField(fieldInfo.getName(), fieldInfo.getType());
                        }

                        break;
                    }
                }
            }
        }
        if (!schema.hasField(ID)){
            schema.addField(ID, String.class, FieldAttribute.PRIMARY_KEY);
        }
        if (!schema.hasField(KinveyMetaData.KMD) && !name.endsWith("_" + KinveyMetaData.KMD)){
            RealmObjectSchema innerScheme = createScheme(name + "_" + KinveyMetaData.KMD , realm, KinveyMetaData.class);
            schema.addRealmObjectField(KinveyMetaData.KMD, innerScheme);
        }

        if (!schema.hasField(TTL)){
            schema.addField(TTL, Long.class);
        }

        return schema;

    }

    public static DynamicRealmObject saveData(String name, DynamicRealm realm, Class<? extends GenericJson> clazz, GenericJson obj) {

        List<Field> fields = getClassFieldsAndParentClassFields(clazz);

        DynamicRealmObject object = null;

        if (obj.containsKey(ID) && obj.get(ID) != null) {
            object = realm.where(name)
                    .equalTo(ID, (String) obj.get(ID))
                    .findFirst();
        } else {
            obj.put(ID, UUID.randomUUID().toString());
        }

        if (object == null){
            object = realm.createObject(name, obj.get(ID));
        }

        if (obj.containsKey(KinveyMetaData.KMD)){
            Map kmd = (Map)obj.get(KinveyMetaData.KMD);
            if (kmd != null) {
                KinveyMetaData metadata = KinveyMetaData.fromMap(kmd);
                DynamicRealmObject innerObject = saveData(name + "_" + KinveyMetaData.KMD,
                        realm,
                        KinveyMetaData.class,
                        metadata);
                object.setObject(KinveyMetaData.KMD, innerObject);
            }
        }


        for (Field f : fields){
            FieldInfo fieldInfo = FieldInfo.of(f);
            if (fieldInfo == null){
                continue;
            }

            Log.d(ClassHash.class.getName(), "isArrayOrCollection: " + isArrayOrCollection(f.getType()));

            Log.d(ClassHash.class.getName(), "fieldInfo.getValue(obj) != null: " + (fieldInfo.getValue(obj) != null)); //it was
            Log.d(ClassHash.class.getName(), "obj.get(fieldInfo.getName()) != null: " + (obj.get(fieldInfo.getName()) != null)); // 1th variant


            if (isArrayOrCollection(f.getType()) && fieldInfo.getValue(obj) != null) {
                Class underlying = getUnderlying(f);
                Log.d(ClassHash.class.getName(), "underlying: " + underlying);
                Log.d(ClassHash.class.getName(), "f: " + f);
                if (GenericJson.class.isAssignableFrom(underlying)) {
                    RealmList list = new RealmList();
                    Object collection = fieldInfo.getValue(obj);
                    if (f.getType().isArray()){
                        for (int i = 0 ; i < Array.getLength(collection); i++){
                            list.add(saveData(name + "_" + fieldInfo.getName(),
                                    realm,
                                    (Class<? extends GenericJson>)underlying,
                                    (GenericJson) Array.get(collection, i)));
                        }
                    } else {
                        for (GenericJson genericJson : ((Collection<? extends GenericJson>) collection)) {
                            list.add(saveData(name + "_" + fieldInfo.getName(),
                                    realm,
                                    (Class<? extends GenericJson>) underlying,
                                    genericJson));
                        }
                    }
                    object.setList(fieldInfo.getName(), list);

                }
            } else if (GenericJson.class.isAssignableFrom(fieldInfo.getType()) && fieldInfo.getValue(obj) != null){

                DynamicRealmObject innerObject = saveData(name + "_" + fieldInfo.getName(),
                        realm,
                        (Class<? extends GenericJson>) fieldInfo.getType(),
                        (GenericJson) obj.get(fieldInfo.getName()));
                object.setObject(fieldInfo.getName(), innerObject);
            } else {
                if (!fieldInfo.getName().equals(ID)) {
                    for (Class c : ALLOWED) {
                        if (fieldInfo.getType().equals(c)) {
                            object.set(fieldInfo.getName(), fieldInfo.getValue(obj));
                            break;
                        }
                    }
                }
            }
        }
        //set dynamic fields
        if (object.get(TTL) != obj.get(TTL)){
            object.set(TTL, obj.get(TTL));
        }


        if (!obj.containsKey(KinveyMetaData.KMD) && !name.endsWith("_" + KinveyMetaData.KMD)){
            KinveyMetaData metadata = new KinveyMetaData();
            metadata.set("lmt", String.format("%tFT%<tTZ",
                    Calendar.getInstance(TimeZone.getTimeZone("Z"))));
            metadata.set("ect", String.format("%tFT%<tTZ",
                    Calendar.getInstance(TimeZone.getTimeZone("Z"))));

            DynamicRealmObject innerObject = saveData(name + "_" + KinveyMetaData.KMD,
                    realm,
                    KinveyMetaData.class,
                    metadata);
            object.setObject(KinveyMetaData.KMD, innerObject);
        }

        return object;
    }


    public static <T extends GenericJson> T realmToObject(DynamicRealmObject dynamic, Class<T> objectClass){
        if (dynamic == null){
            return null;
        }
        T ret = null;
        try {
            ret = objectClass.newInstance();

            ClassInfo classInfo = ClassInfo.of(objectClass);

            for (String field : dynamic.getFieldNames()){

                FieldInfo info = classInfo.getFieldInfo(field);

                Object o = dynamic.get(field);

                if (info == null){
                    //prevent private fields like "__ttl__" to be published
                    if (!PRIVATE_FIELDS.contains(field)){
                        if (o instanceof DynamicRealmObject){
                            ret.put(field, realmToObject((DynamicRealmObject) o, GenericJson.class));
                        } else {
                            ret.put(field, o);
                        }
                    }
                    continue;
                }

                if (Number.class.isAssignableFrom(info.getType())){
                    Number n = (Number)dynamic.get(info.getName());
                    if (Long.class.isAssignableFrom(info.getType())){
                        ret.put(info.getName(), n.longValue());
                    } else if (Byte.class.isAssignableFrom(info.getType())){
                        ret.put(info.getName(), n.byteValue());
                    } else if (Integer.class.isAssignableFrom(info.getType())){
                        ret.put(info.getName(), n.intValue());
                    } else if (Short.class.isAssignableFrom(info.getType())){
                        ret.put(info.getName(), n.shortValue());
                    } else if (Float.class.isAssignableFrom(info.getType())){
                        ret.put(info.getName(), n.floatValue());
                    } else if (Double.class.isAssignableFrom(info.getType())){
                        ret.put(info.getName(), n.doubleValue());
                    }

                } else if (GenericJson.class.isAssignableFrom(info.getType())) {
                    ret.put(info.getName(), realmToObject(dynamic.getObject(info.getName()),
                            (Class<? extends GenericJson>)info.getType()));
                } else if (isArrayOrCollection(info.getType())){
                    Class underlying = getUnderlying(info.getField());
                    if (underlying != null && GenericJson.class.isAssignableFrom(underlying)){
                        RealmList<DynamicRealmObject> list = dynamic.getList(info.getName());
                        if (info.getType().isArray()){
                            GenericJson[] array = (GenericJson[])Array.newInstance(underlying, list.size());
                            for (int i = 0 ; i < list.size(); i++){
                                array[i] = realmToObject(list.get(i), underlying);
                            }
                            ret.put(info.getName(), array);
                        } else {
                            Collection<Object> c = Data.newCollectionInstance(info.getType());
                            for (int i = 0 ; i < list.size(); i++){
                                c.add(realmToObject(list.get(i), underlying));
                            }
                            ret.put(info.getName(), c);

                        }

                    }
                } else {
                    ret.put(info.getName(), o);
                }

            }
            if (!ret.containsKey(KinveyMetaData.KMD) && dynamic.hasField(KinveyMetaData.KMD)){
                ret.put(KinveyMetaData.KMD, realmToObject(dynamic.getObject(KinveyMetaData.KMD), KinveyMetaData.class));
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return ret;
    }


    public boolean isAllowed(FieldInfo f){
        boolean allowed = false;
        for (Class c : ALLOWED){
            if (f.getType().equals(c)){
                allowed = true;
                break;
            }
        }
        if (GenericJson.class.isAssignableFrom(f.getType())){
            allowed = true;
        } else if (isArrayOrCollection(f.getType())){
            Class underlying = getUnderlying(f.getField());
            if (underlying != null && GenericJson.class.isAssignableFrom(underlying)){
                allowed = true;
            }
        }
        return allowed;
    }

    private static boolean isArrayOrCollection(Class clazz){
        return clazz.isArray() || Collection.class.isAssignableFrom(clazz);
    }


    private static Class getUnderlying(Field f){
        Class type = f.getType();
        Class underlying;
        if (type.isArray()){
            underlying = type.getComponentType();
        } else {
            ParameterizedType genericSuperclass = (ParameterizedType)f.getGenericType();
            underlying = (Class)genericSuperclass.getActualTypeArguments()[0];
        }
        return underlying;
    }

    private static List<Field> getClassFieldsAndParentClassFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        if (clazz.getSuperclass() != null) {
            fields.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
        }
        return fields;
    }

}
