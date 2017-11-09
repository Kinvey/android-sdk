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
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.Data;
import com.google.api.client.util.FieldInfo;
import com.kinvey.android.Client;
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
import io.realm.RealmSchema;

/**
 * Created by Prots on 1/27/16.
 */
public abstract class ClassHash {

    public static final String TTL = "__ttl__";
    private static final String ID = "_id";
    private static final String ITEMS = "_items";

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
        RealmObjectSchema schema = createSchemeFromClass(name, realm, clazz);

        String shortName = TableNameManager.getShortName(name, realm);

        if (!schema.hasField(KinveyMetaData.AccessControlList.ACL)
                && !name.endsWith("_" + KinveyMetaData.KMD)
                && !name.endsWith("_" + KinveyMetaData.AccessControlList.ACL)){
            RealmObjectSchema innerScheme = createSchemeFromClass(shortName + "_" + KinveyMetaData.AccessControlList.ACL, realm, KinveyMetaData.AccessControlList.class);
            schema.addRealmObjectField(KinveyMetaData.AccessControlList.ACL, innerScheme);
        }
        return schema;
    }

    /**
     * Migrate from old table name to new table name
     * @param name table name to rename
     * @param realm Realm object
     * @param clazz Class in table
     */
    static void migration(String name, DynamicRealm realm, Class<? extends GenericJson> clazz){
        rename(name, null, realm, clazz);
    }

    /**
     * Rename realm table
     * @param oldName old table name
     * @param newName new table name
     * @param realm Realm object
     * @param clazz Class in table
     */
    private static void rename(String oldName, String newName,  DynamicRealm realm, Class<? extends GenericJson> clazz) {
        String shortName = newName != null ? newName : TableNameManager.createShortName(oldName, realm);
        RealmSchema schema = realm.getSchema();
        schema.rename(oldName, shortName);
        List<Field> fields = getClassFieldsAndParentClassFields(clazz);
        for (Field f : fields){
            FieldInfo fieldInfo = FieldInfo.of(f);
            if (fieldInfo == null){
                continue;
            }
            if (fieldInfo.getType().isArray() || Collection.class.isAssignableFrom(fieldInfo.getType())){
                Class underlying = getUnderlying(f);
                if (underlying != null && GenericJson.class.isAssignableFrom(underlying)){
                    rename(oldName + "_" + fieldInfo.getName(), TableNameManager.createShortName(shortName + "_" + fieldInfo.getName(), realm), realm, (Class<? extends GenericJson>) fieldInfo.getType());
                } else {
                    for (Class c : ALLOWED) {
                        if (underlying.equals(c)) {
                            schema.rename(oldName, shortName);
                            break;
                        }
                    }
                }
            } else if (GenericJson.class.isAssignableFrom(fieldInfo.getType())) {
                rename(oldName + "_" + fieldInfo.getName(), TableNameManager.createShortName(shortName + "_" + fieldInfo.getName(), realm), realm, (Class<? extends GenericJson>) fieldInfo.getType());
            }
        }
        if (schema.contains(oldName + "_" + KinveyMetaData.AccessControlList.ACL)) {
            schema.rename(oldName + "_" + KinveyMetaData.AccessControlList.ACL,
                    TableNameManager.createShortName(TableNameManager.getShortName(oldName, realm) + "_" + KinveyMetaData.AccessControlList.ACL, realm));
        } else {
            RealmObjectSchema objectSchema = schema.get(shortName);
            RealmObjectSchema innerScheme = createSchemeFromClass(shortName + "_" + KinveyMetaData.AccessControlList.ACL, realm, KinveyMetaData.AccessControlList.class);
            objectSchema.addRealmObjectField(KinveyMetaData.AccessControlList.ACL, innerScheme);
        }
        if (schema.contains(oldName + "_" + KinveyMetaData.KMD)) {
            schema.rename(oldName + "_" + KinveyMetaData.KMD,
                    TableNameManager.createShortName(shortName + "_" + KinveyMetaData.KMD, realm));
        }
    }

    private static RealmObjectSchema createSchemeFromClass(String name, DynamicRealm realm, Class<? extends GenericJson> clazz) {

        String shortName = TableNameManager.createShortName(name, realm);

        RealmObjectSchema schema = realm.getSchema().create(shortName);
        List<Field> fields = getClassFieldsAndParentClassFields(clazz);
        for (Field f : fields){
            FieldInfo fieldInfo = FieldInfo.of(f);
            if (fieldInfo == null){
                continue;
            }
            if (fieldInfo.getType().isArray() || Collection.class.isAssignableFrom(fieldInfo.getType())){
                Class underlying = getUnderlying(f);

                if (underlying != null && GenericJson.class.isAssignableFrom(underlying)){
                    RealmObjectSchema innerScheme = createSchemeFromClass(shortName + "_" + fieldInfo.getName(), realm, (Class<? extends GenericJson>) underlying);

                    schema.addRealmListField(fieldInfo.getName(), innerScheme);
                } else {
                    for (Class c : ALLOWED) {
                        if (underlying.equals(c)) {
                            RealmObjectSchema innerScheme = realm.getSchema().create(shortName + "_" + fieldInfo.getName());
                            if (!innerScheme.hasField(ID)){
                                innerScheme.addField(ID, String.class, FieldAttribute.PRIMARY_KEY);
                            }
                            innerScheme.addField(fieldInfo.getName() + ITEMS, underlying);
                            schema.addRealmListField(fieldInfo.getName(), innerScheme);
                            break;
                        }
                    }
                }

            } else if (GenericJson.class.isAssignableFrom(fieldInfo.getType())){
                RealmObjectSchema innerScheme = createSchemeFromClass(shortName + "_" + fieldInfo.getName(), realm, (Class<? extends GenericJson>) fieldInfo.getType());
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

        if (!schema.hasField(TTL)){
            schema.addField(TTL, Long.class);
        }

        if (!schema.hasField(KinveyMetaData.KMD) && !name.endsWith("_" + KinveyMetaData.KMD) && !name.endsWith("_" + KinveyMetaData.AccessControlList.ACL)){
            RealmObjectSchema innerScheme = createSchemeFromClass(shortName + "_" + KinveyMetaData.KMD , realm, KinveyMetaData.class);
            schema.addRealmObjectField(KinveyMetaData.KMD, innerScheme);
        }

        return schema;
    }


    public static DynamicRealmObject saveData(String name, DynamicRealm realm, Class<? extends GenericJson> clazz, GenericJson obj) {
        DynamicRealmObject object = saveClassData(name, realm, clazz, obj);

        String shortName = TableNameManager.getShortName(name, realm);

        if (!obj.containsKey(KinveyMetaData.AccessControlList.ACL)
                && !name.endsWith("_" + KinveyMetaData.AccessControlList.ACL)
                && !name.endsWith("_" + KinveyMetaData.KMD)
                && realm.getSchema().contains(TableNameManager.getShortName(shortName + "_" + KinveyMetaData.AccessControlList.ACL, realm))){
            KinveyMetaData.AccessControlList acl = new KinveyMetaData.AccessControlList();
            acl.set("creator", Client.sharedInstance().getActiveUser().getId());
            DynamicRealmObject innerObject = saveClassData(shortName + "_" + KinveyMetaData.AccessControlList.ACL,
                    realm,
                    KinveyMetaData.AccessControlList.class,
                    acl);
            object.setObject(KinveyMetaData.AccessControlList.ACL, innerObject);
        }
        return object;
    }

    private static DynamicRealmObject saveClassData(String name, DynamicRealm realm, Class<? extends GenericJson> clazz, GenericJson obj) {

        String shortName = TableNameManager.getShortName(name, realm);

        List<Field> fields = getClassFieldsAndParentClassFields(clazz);

        DynamicRealmObject object = null;

        if (obj.containsKey(ID) && obj.get(ID) != null) {
            object = realm.where(shortName)
                    .equalTo(ID, (String) obj.get(ID))
                    .findFirst();
        } else {
            obj.put(ID, UUID.randomUUID().toString());
        }

        String kmdId = null;
        String aclId = null;

        if (object == null){
            object = realm.createObject(shortName, obj.get(ID));
        } else {
            if (object.hasField(KinveyMetaData.KMD)
                    && object.getObject(KinveyMetaData.KMD) != null) {
                kmdId = object.getObject(KinveyMetaData.KMD).getString(ID);
            }
            if (object.hasField(KinveyMetaData.AccessControlList.ACL)
                    && object.getObject(KinveyMetaData.AccessControlList.ACL) != null) {
                aclId = object.getObject(KinveyMetaData.AccessControlList.ACL).getString(ID);
            }
        }

        if (obj.containsKey(KinveyMetaData.KMD)){
            Map kmd = (Map)obj.get(KinveyMetaData.KMD);
            if (kmd != null) {
                KinveyMetaData metadata = KinveyMetaData.fromMap(kmd);
                metadata.set(ID, kmdId);
                DynamicRealmObject innerObject = saveClassData(shortName + "_" + KinveyMetaData.KMD,
                        realm,
                        KinveyMetaData.class,
                        metadata);
                object.setObject(KinveyMetaData.KMD, innerObject);
            }
        }

        if (obj.containsKey(KinveyMetaData.AccessControlList.ACL)
                && realm.getSchema().contains(TableNameManager.getShortName(shortName + "_" + KinveyMetaData.AccessControlList.ACL, realm))){
            Map acl = (Map)obj.get(KinveyMetaData.AccessControlList.ACL);
            if (acl != null) {
                KinveyMetaData.AccessControlList accessControlList = KinveyMetaData.AccessControlList.fromMap(acl);
                accessControlList.set(ID, aclId);
                DynamicRealmObject innerObject = saveClassData(shortName + "_" + KinveyMetaData.AccessControlList.ACL,
                        realm,
                        KinveyMetaData.AccessControlList.class,
                        accessControlList);
                object.setObject(KinveyMetaData.AccessControlList.ACL, innerObject);
            }
        }

        for (Field f : fields){
            FieldInfo fieldInfo = FieldInfo.of(f);
            if (fieldInfo == null){
                continue;
            }


            if (isArrayOrCollection(f.getType()) && fieldInfo.getValue(obj) != null) {
                Class underlying = getUnderlying(f);
                    RealmList list = new RealmList();
                    Object collection = fieldInfo.getValue(obj);
                    if (f.getType().isArray()){
                        for (int i = 0 ; i < Array.getLength(collection); i++){
                            list.add(saveClassData(shortName + "_" + fieldInfo.getName(),
                                    realm,
                                    (Class<? extends GenericJson>)underlying,
                                    (GenericJson) Array.get(collection, i)));
                        }
                    } else {

                        if (GenericJson.class.isAssignableFrom(underlying)) {
                            for (GenericJson genericJson : ((Collection<? extends GenericJson>) collection)) {
                                list.add(saveClassData(shortName + "_" + fieldInfo.getName(),
                                        realm,
                                        (Class<? extends GenericJson>) underlying,
                                        genericJson));
                            }
                        } else {
                            DynamicRealmObject dynamicRealmObject = null;
                            for (Object o : (Collection) collection) {
                                dynamicRealmObject = realm.createObject(shortName + "_" + fieldInfo.getName(), UUID.randomUUID().toString());

                                for (Class c : ALLOWED) {
                                    if (underlying.equals(c)) {

                                        dynamicRealmObject.set(fieldInfo.getName() + ITEMS, o);
                                        break;
                                    }
                                }

                                list.add(dynamicRealmObject);
                            }

                        }
                        object.setList(fieldInfo.getName(), list);

                    }
            } else if (GenericJson.class.isAssignableFrom(fieldInfo.getType()) && fieldInfo.getValue(obj) != null){

                DynamicRealmObject innerObject = saveClassData(shortName + "_" + fieldInfo.getName(),
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


        if (!obj.containsKey(KinveyMetaData.KMD) && !name.endsWith("_" + KinveyMetaData.KMD) && !name.endsWith("_" + KinveyMetaData.AccessControlList.ACL)){
            KinveyMetaData metadata = new KinveyMetaData();
            metadata.set("lmt", String.format("%tFT%<tTZ",
                    Calendar.getInstance(TimeZone.getTimeZone("Z"))));
            metadata.set("ect", String.format("%tFT%<tTZ",
                    Calendar.getInstance(TimeZone.getTimeZone("Z"))));

            DynamicRealmObject innerObject = saveClassData(shortName + "_" + KinveyMetaData.KMD,
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
                    if (underlying != null){
                        RealmList<DynamicRealmObject> list = dynamic.getList(info.getName());
                        if (underlying.isArray() && GenericJson.class.isAssignableFrom(underlying)){
                            GenericJson[] array = (GenericJson[])Array.newInstance(underlying, list.size());
                            for (int i = 0 ; i < list.size(); i++){
                                array[i] = realmToObject(list.get(i), underlying);
                            }
                            ret.put(info.getName(), array);
                        } else {
                            Collection<Object> c = Data.newCollectionInstance(info.getType());
                            if (GenericJson.class.isAssignableFrom(underlying)) {
                                for (int i = 0; i < list.size(); i++) {
                                    c.add(realmToObject(list.get(i), underlying));
                                }
                            } else {
                                for (int i = 0; i < list.size(); i++) {
                                    Object object = list.get(i).get(info.getName() + ITEMS);
                                    c.add(object);
                                }
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

    static boolean isArrayOrCollection(Class clazz){
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
