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
import com.kinvey.java.Constants;
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
import java.util.Locale;
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

    private static final String KMD = "_kmd";
    private static final String ACL = "_acl";
    public static final String TTL = "__ttl__";
    private static final String ID = "_id";
    private static final String ITEMS = "_items";

    private static final HashSet<String> PRIVATE_FIELDS = new HashSet<String>(){
        {
            add(TTL);
        }
    };

    private static final HashSet<String> EMBEDDED_OBJECT_PRIVATE_FIELDS = new HashSet<String>(){
        {
            add(TTL);
            add(ACL);
            add(KMD);
            add(ID);
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

    private enum SelfReferenceState {
        DEFAULT,
        LIST,
        CLASS,
        SUBCLASS,
        SUBLIST
    }

    //supported fields
    // boolean, byte, short, ìnt, long, float, double, String, Date and byte[]

    public static String getClassHash(Class<? extends GenericJson> clazz) {
        return getClassHash(clazz, SelfReferenceState.DEFAULT, new ArrayList<String>());
    }

    private static String getClassHash(Class<? extends GenericJson> clazz, SelfReferenceState selfReferenceState, List<String> classes) {

        StringBuilder sb = new StringBuilder();

        List<Field> fields = getClassFieldsAndParentClassFields(clazz);

        if (classes.contains(clazz.getSimpleName())) {
            return clazz.getName();
        } else {
            classes.add(clazz.getSimpleName());
        }

        for (Field f : fields){

            List<String> classesList = new ArrayList<>(classes);

            FieldInfo fieldInfo = FieldInfo.of(f);
            if (fieldInfo == null){
                continue;
            }

            if (isArrayOrCollection(fieldInfo.getType())){
                Class underlying = getUnderlying(f);
                if (underlying != null && GenericJson.class.isAssignableFrom(underlying)){
                    if (!underlying.getSimpleName().equalsIgnoreCase(clazz.getSimpleName())) {
                        if (selfReferenceState == SelfReferenceState.DEFAULT) {
                            String innerHash = getClassHash((Class<? extends GenericJson>) underlying, selfReferenceState, classesList);
                            sb.append("[").append(fieldInfo.getName()).append("]:").append(innerHash).append(";");
                        }
                    } else if (selfReferenceState == SelfReferenceState.DEFAULT) {
                        classes.add(clazz.getSimpleName());
                        String innerHash = getClassHash((Class<? extends GenericJson>) underlying, SelfReferenceState.SUBCLASS, classesList);
                        sb.append("[").append(fieldInfo.getName()).append("]:").append(innerHash).append(";");
                    }
                }
            } else if (GenericJson.class.isAssignableFrom(fieldInfo.getType())){
                if (!f.getType().getSimpleName().equalsIgnoreCase(clazz.getSimpleName())) {
                    if (selfReferenceState == SelfReferenceState.DEFAULT) {
                        String innerHash = getClassHash((Class<? extends GenericJson>) fieldInfo.getType(), selfReferenceState, classesList);
                        sb.append(fieldInfo.getName()).append(":").append(innerHash).append(";");
                    }
                } else if (selfReferenceState == SelfReferenceState.DEFAULT) {
                    String innerHash = getClassHash((Class<? extends GenericJson>) fieldInfo.getType(), SelfReferenceState.SUBCLASS, classesList);
                    sb.append(fieldInfo.getName()).append(":").append(innerHash).append(";");
                }
            } else {
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


        String hashtext;

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
        RealmObjectSchema schema = createSchemeFromClass(name, realm, clazz, SelfReferenceState.DEFAULT, new ArrayList<String>());
        String shortName = TableNameManager.getShortName(name, realm);
        if (!schema.hasField(KinveyMetaData.KMD) && !name.endsWith(Constants.UNDERSCORE + KinveyMetaData.KMD) && !name.endsWith(Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL)){
            RealmObjectSchema innerScheme = createSchemeFromClass(shortName + Constants.UNDERSCORE + KinveyMetaData.KMD , realm, KinveyMetaData.class, SelfReferenceState.DEFAULT, new ArrayList<String>());
            schema.addRealmObjectField(KinveyMetaData.KMD, innerScheme);
        }
        if (!schema.hasField(KinveyMetaData.AccessControlList.ACL)
                && !name.endsWith(Constants.UNDERSCORE + KinveyMetaData.KMD)
                && !name.endsWith(Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL)){
            RealmObjectSchema innerScheme = createSchemeFromClass(shortName + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL, realm, KinveyMetaData.AccessControlList.class, SelfReferenceState.DEFAULT, new ArrayList<String>());
            schema.addRealmObjectField(KinveyMetaData.AccessControlList.ACL, innerScheme);
        }
        if (!schema.hasField(TTL)){
            schema.addField(TTL, Long.class);
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
        for (Field f : fields) {
            FieldInfo fieldInfo = FieldInfo.of(f);
            if (fieldInfo == null){
                continue;
            }
            if (fieldInfo.getType().isArray() || Collection.class.isAssignableFrom(fieldInfo.getType())){
                Class underlying = getUnderlying(f);
                if (underlying != null && GenericJson.class.isAssignableFrom(underlying)){
                    rename(oldName + Constants.UNDERSCORE + fieldInfo.getName(), TableNameManager.createShortName(shortName + Constants.UNDERSCORE + fieldInfo.getName(), realm), realm, (Class<? extends GenericJson>) fieldInfo.getType());
                } else {
                    for (Class c : ALLOWED) {
                        if (underlying.equals(c)) {
                            schema.rename(oldName, shortName);
                            break;
                        }
                    }
                }
            } else if (GenericJson.class.isAssignableFrom(fieldInfo.getType())) {
                rename(oldName + Constants.UNDERSCORE + fieldInfo.getName(), TableNameManager.createShortName(shortName + Constants.UNDERSCORE + fieldInfo.getName(), realm), realm, (Class<? extends GenericJson>) fieldInfo.getType());
            }
        }

        // remove unnecessary tables ..._acl_kmd
        if (schema.contains(oldName + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL + Constants.UNDERSCORE + KinveyMetaData.KMD)) {
            RealmObjectSchema objectSchema = schema.get(oldName + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL);
            objectSchema.removeField(KinveyMetaData.KMD);
            schema.remove(oldName + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL + Constants.UNDERSCORE + KinveyMetaData.KMD);
        }

        // rename _kmd table
        if (schema.contains(oldName + Constants.UNDERSCORE + KinveyMetaData.KMD)) {
            schema.rename(oldName + Constants.UNDERSCORE + KinveyMetaData.KMD,
                    TableNameManager.createShortName(shortName + Constants.UNDERSCORE + KinveyMetaData.KMD, realm));
        }

        // rename or create _acl table
        if (schema.contains(oldName + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL)) {
            schema.rename(oldName + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL,
                    TableNameManager.createShortName(TableNameManager.getShortName(oldName, realm) + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL, realm));
        } else {
            RealmObjectSchema objectSchema = schema.get(shortName);
            RealmObjectSchema innerScheme = createSchemeFromClass(shortName + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL, realm, KinveyMetaData.AccessControlList.class, SelfReferenceState.DEFAULT, new ArrayList<String>()); // TODO: 24.11.2017 check
            objectSchema.addRealmObjectField(KinveyMetaData.AccessControlList.ACL, innerScheme);
            //get table
            List<DynamicRealmObject> results = realm.where(shortName).findAll();
            //add "_acl" field to each item
            for (DynamicRealmObject realmObject : results) {
                KinveyMetaData.AccessControlList acl = new KinveyMetaData.AccessControlList();
                acl.set("creator", Client.sharedInstance().getActiveUser().getId());
                DynamicRealmObject innerObject = saveClassData(shortName + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL,
                        realm,
                        KinveyMetaData.AccessControlList.class,
                        acl, SelfReferenceState.DEFAULT,
                        new ArrayList<String>());
                realmObject.setObject(KinveyMetaData.AccessControlList.ACL, innerObject);
            }
        }

    }

    /**
     * Fix to _acl_kmd tables
     * @param name Collection name
     * @param realm Realm object
     * @param clazz Class in table
     */
    static void checkAclKmdFields(String name, DynamicRealm realm, Class<? extends GenericJson> clazz) {
        RealmSchema schema = realm.getSchema();
        if (schema.contains(name + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL + Constants.UNDERSCORE + KinveyMetaData.KMD)) {
            RealmObjectSchema objectSchema = schema.get(TableNameManager.getShortName(TableNameManager.getShortName(name, realm) + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL, realm));
            objectSchema.removeField(KinveyMetaData.KMD);
            schema.remove(name + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL + Constants.UNDERSCORE + KinveyMetaData.KMD);
        }
        fillNewAclFields(name, false, realm, clazz);
    }

    /**
     * Fix to _acl_kmd tables
     * Fill acl fields, which were created in migration process in versions before 3.0.11
     * @param name Collection name
     * @param isFill False if don't need to fill acl field (false is used for acl field of main item)
     * @param realm Realm object
     * @param clazz Class in table
     */
    private static void fillNewAclFields(String name, boolean isFill, DynamicRealm realm, Class<? extends GenericJson> clazz) {
        String shortName = TableNameManager.getShortName(name, realm);
        RealmSchema schema = realm.getSchema();
        List<Field> fields = getClassFieldsAndParentClassFields(clazz);
        for (Field f : fields) {
            FieldInfo fieldInfo = FieldInfo.of(f);
            if (fieldInfo == null){
                continue;
            }
            if (fieldInfo.getType().isArray() || Collection.class.isAssignableFrom(fieldInfo.getType())) {
                Class underlying = getUnderlying(f);
                if (underlying != null && GenericJson.class.isAssignableFrom(underlying)){
                    fillNewAclFields(shortName + Constants.UNDERSCORE + fieldInfo.getName(), true,  realm, (Class<? extends GenericJson>) fieldInfo.getType());
                }
            } else if (GenericJson.class.isAssignableFrom(fieldInfo.getType())) {
                fillNewAclFields(shortName + Constants.UNDERSCORE + fieldInfo.getName(), true, realm, (Class<? extends GenericJson>) fieldInfo.getType());
            }
        }
        if (isFill) {
            // fill _acl table
            if (schema.contains(TableNameManager.getShortName(shortName + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL, realm))) {
                List<DynamicRealmObject> results = realm.where(shortName).findAll();
                //add "_acl" field to each item
                for (DynamicRealmObject realmObject : results) {
                    if (realmObject.get("_acl") == null) {
                        KinveyMetaData.AccessControlList acl = new KinveyMetaData.AccessControlList();
                        acl.set("creator", Client.sharedInstance().getActiveUser().getId());
                        DynamicRealmObject innerObject = saveClassData(shortName + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL,
                                realm,
                                KinveyMetaData.AccessControlList.class,
                                acl, SelfReferenceState.DEFAULT,
                                new ArrayList<String>());
                        realmObject.setObject(KinveyMetaData.AccessControlList.ACL, innerObject);
                    }
                }
            }
        }
    }

    private static RealmObjectSchema createSchemeFromClass(String name, DynamicRealm realm, Class<? extends GenericJson> clazz,
                                                           SelfReferenceState selfReferenceState, List<String> classes) {

        String shortName = TableNameManager.createShortName(name, realm);

        RealmObjectSchema schema = realm.getSchema().create(shortName);
        List<Field> fields = getClassFieldsAndParentClassFields(clazz);
        SelfReferenceState state;

        classes.add(clazz.getSimpleName());

        for (Field f : fields){
            state = null;
            List<String> classesList = new ArrayList<>(classes);
            FieldInfo fieldInfo = FieldInfo.of(f);
            if (fieldInfo == null){
                continue;
            }
            if (fieldInfo.getType().isArray() || Collection.class.isAssignableFrom(fieldInfo.getType())){
                Class underlying = getUnderlying(f);
                if (underlying != null && GenericJson.class.isAssignableFrom(underlying)){
                    if (!underlying.getSimpleName().equalsIgnoreCase(clazz.getSimpleName())) {
                        state = selfReferenceState;
                    } else if (selfReferenceState  == SelfReferenceState.DEFAULT || selfReferenceState  == SelfReferenceState.SUBCLASS) {
                        state = SelfReferenceState.LIST;
                    } else if (selfReferenceState  == SelfReferenceState.LIST) {
                        state = SelfReferenceState.CLASS;
                    } else if (selfReferenceState  == SelfReferenceState.CLASS) {
                        state = SelfReferenceState.SUBLIST;
                    } else if (selfReferenceState  == SelfReferenceState.SUBLIST) {
                        state = SelfReferenceState.SUBLIST;
                    }
                    if (state != null) {
                        if (selfReferenceState  == SelfReferenceState.SUBLIST) {
                            schema.addRealmObjectField(fieldInfo.getName(),
                                    realm.getSchema().get(TableNameManager.getShortName(TableNameManager.getOriginalName(TableNameManager.getShortName(name, realm), realm), realm)));
                        } else {
                            RealmObjectSchema innerScheme = createSchemeFromClass(shortName + Constants.UNDERSCORE + fieldInfo.getName(), realm,
                                    (Class<? extends GenericJson>) underlying, state, classesList);
                             schema.addRealmListField(fieldInfo.getName(), innerScheme);
                        }
                    }

                } else {
                    for (Class c : ALLOWED) {
                        if (underlying.equals(c)) {
                            RealmObjectSchema innerScheme = realm.getSchema().create(TableNameManager.createShortName(shortName + Constants.UNDERSCORE + fieldInfo.getName(), realm));
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
                if (!f.getType().getSimpleName().equalsIgnoreCase(clazz.getSimpleName())) {
                    if (classes.contains((f.getType().getSimpleName()))) {
                        if (selfReferenceState == SelfReferenceState.DEFAULT) {
                            RealmObjectSchema innerScheme = createSchemeFromClass(shortName + Constants.UNDERSCORE + fieldInfo.getName(), realm,
                                    (Class<? extends GenericJson>) fieldInfo.getType(), SelfReferenceState.SUBCLASS, classesList);
                            schema.addRealmObjectField(fieldInfo.getName(), innerScheme);
                        } else if (selfReferenceState == SelfReferenceState.SUBCLASS) {
                            schema.addRealmObjectField(fieldInfo.getName(), schema);
                        }
                    } else {
                        RealmObjectSchema innerScheme = createSchemeFromClass(shortName + Constants.UNDERSCORE + fieldInfo.getName(), realm,
                                (Class<? extends GenericJson>) fieldInfo.getType(), selfReferenceState, classesList);
                        schema.addRealmObjectField(fieldInfo.getName(), innerScheme);
                    }
                } else if (selfReferenceState == SelfReferenceState.DEFAULT) {
                    RealmObjectSchema innerScheme = createSchemeFromClass(shortName + Constants.UNDERSCORE + fieldInfo.getName(), realm,
                            (Class<? extends GenericJson>) fieldInfo.getType(), SelfReferenceState.SUBCLASS, classesList);
                    schema.addRealmObjectField(fieldInfo.getName(), innerScheme);
                } else if (selfReferenceState == SelfReferenceState.SUBCLASS) {
                    schema.addRealmObjectField(fieldInfo.getName(), schema);
                }
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

        return schema;
    }


    public static DynamicRealmObject saveData(String name, DynamicRealm realm, Class<? extends GenericJson> clazz, GenericJson obj) {

        DynamicRealmObject realmObject = saveClassData(name, realm, clazz, obj, SelfReferenceState.DEFAULT, new ArrayList<String>());
        String shortName = TableNameManager.getShortName(name, realm);

        if (!obj.containsKey(KinveyMetaData.KMD) && !name.endsWith(Constants.UNDERSCORE + KinveyMetaData.KMD) && !name.endsWith(Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL)){
            KinveyMetaData metadata = new KinveyMetaData();
            metadata.set(KinveyMetaData.LMT, String.format(Locale.US, Constants.TIME_FORMAT,
                    Calendar.getInstance(TimeZone.getTimeZone(Constants.Z))));
            metadata.set(KinveyMetaData.ECT, String.format(Locale.US, Constants.TIME_FORMAT,
                    Calendar.getInstance(TimeZone.getTimeZone(Constants.Z))));

            DynamicRealmObject innerObject = saveClassData(shortName + Constants.UNDERSCORE + KinveyMetaData.KMD,
                    realm,
                    KinveyMetaData.class,
                    metadata,
                    SelfReferenceState.DEFAULT,
                    new ArrayList<String>());
            realmObject.setObject(KinveyMetaData.KMD, innerObject);
        }

        if (!obj.containsKey(KinveyMetaData.AccessControlList.ACL)
                && !name.endsWith(Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL)
                && !name.endsWith(Constants.UNDERSCORE + KinveyMetaData.KMD)
                && realm.getSchema().contains(TableNameManager.getShortName(shortName + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL, realm))){
            KinveyMetaData.AccessControlList acl = new KinveyMetaData.AccessControlList();
            acl.set("creator", Client.sharedInstance().getActiveUser().getId());
            DynamicRealmObject innerObject = saveClassData(shortName + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL,
                    realm,
                    KinveyMetaData.AccessControlList.class,
                    acl, SelfReferenceState.DEFAULT,
                    new ArrayList<String>());
            realmObject.setObject(KinveyMetaData.AccessControlList.ACL, innerObject);
        }
        //set dynamic fields
        if (realmObject.get(TTL) != obj.get(TTL)){
            realmObject.set(TTL, obj.get(TTL));
        }
        return realmObject;
    }

    private static DynamicRealmObject saveClassData(String name, DynamicRealm realm, Class<? extends GenericJson> clazz,
                                                    GenericJson obj, SelfReferenceState selfReferenceState, List<String> classes) {

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

        if (obj.containsKey(KinveyMetaData.KMD)
                && realm.getSchema().contains(TableNameManager.getShortName(shortName + Constants.UNDERSCORE + KinveyMetaData.KMD, realm))){
            Map kmd = (Map)obj.get(KinveyMetaData.KMD);
            if (kmd != null) {
                KinveyMetaData metadata = KinveyMetaData.fromMap(kmd);
                metadata.set(ID, kmdId);
                DynamicRealmObject innerObject = saveClassData(shortName + Constants.UNDERSCORE + KinveyMetaData.KMD,
                        realm,
                        KinveyMetaData.class,
                        metadata,
                        selfReferenceState,
                        new ArrayList<String>());
                object.setObject(KinveyMetaData.KMD, innerObject);
            }
        }

        if (obj.containsKey(KinveyMetaData.AccessControlList.ACL)
                && realm.getSchema().contains(TableNameManager.getShortName(shortName + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL, realm))){
            Map acl = (Map)obj.get(KinveyMetaData.AccessControlList.ACL);
            if (acl != null) {
                KinveyMetaData.AccessControlList accessControlList = KinveyMetaData.AccessControlList.fromMap(acl);
                accessControlList.set(ID, aclId);
                DynamicRealmObject innerObject = saveClassData(shortName + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL,
                        realm,
                        KinveyMetaData.AccessControlList.class,
                        accessControlList,
                        selfReferenceState, new ArrayList<String>());
                object.setObject(KinveyMetaData.AccessControlList.ACL, innerObject);
            }
        }
        SelfReferenceState state;

        classes.add(clazz.getSimpleName());

        for (Field f : fields){
            List<String> classesList = new ArrayList<>(classes);

            FieldInfo fieldInfo = FieldInfo.of(f);
            if (fieldInfo == null){
                continue;
            }

            state = null;
            if (isArrayOrCollection(f.getType()) && fieldInfo.getValue(obj) != null) {
                Class underlying = getUnderlying(f);
                RealmList list = new RealmList();
                Object collection = fieldInfo.getValue(obj);

                if (f.getType().isArray()) {
                    if (!underlying.getSimpleName().equalsIgnoreCase(clazz.getSimpleName())) {
                       state = selfReferenceState;
                    } else if (selfReferenceState == SelfReferenceState.DEFAULT || selfReferenceState  == SelfReferenceState.SUBCLASS) {
                        state = SelfReferenceState.LIST;
                    } else if (selfReferenceState == SelfReferenceState.LIST) {
                        state = SelfReferenceState.CLASS;
                    } else if (selfReferenceState == SelfReferenceState.CLASS) {
                        state = SelfReferenceState.SUBLIST;
                    } else if (selfReferenceState == SelfReferenceState.SUBLIST) {
                        state = SelfReferenceState.SUBLIST;
                    }
                    if (state != null) {
                        for (int i = 0; i < Array.getLength(collection); i++) {
                            list.add(saveClassData(selfReferenceState == SelfReferenceState.SUBLIST ? name : shortName + Constants.UNDERSCORE + fieldInfo.getName(),
                                    realm,
                                    (Class<? extends GenericJson>) underlying,
                                    (GenericJson) Array.get(collection, i),
                                    state, classesList));
                        }
                    }
                } else {
                    if (GenericJson.class.isAssignableFrom(underlying)) {
                        if (!underlying.getSimpleName().equalsIgnoreCase(clazz.getSimpleName())) {
                            state = selfReferenceState;
                        } else if (selfReferenceState == SelfReferenceState.DEFAULT || selfReferenceState  == SelfReferenceState.SUBCLASS) {
                            state = SelfReferenceState.LIST;
                        } else if (selfReferenceState == SelfReferenceState.LIST) {
                            state = SelfReferenceState.CLASS;
                        } else if (selfReferenceState == SelfReferenceState.CLASS) {
                            state = SelfReferenceState.SUBLIST;
                        } else if (selfReferenceState == SelfReferenceState.SUBLIST) {
                            state = SelfReferenceState.SUBLIST;
                        }
                        if (state != null) {
                            for (GenericJson genericJson : ((Collection<? extends GenericJson>) collection)) {
                                list.add(saveClassData(selfReferenceState == SelfReferenceState.SUBLIST ? name : shortName + Constants.UNDERSCORE + fieldInfo.getName(),
                                        realm,
                                        (Class<? extends GenericJson>) underlying,
                                        genericJson,
                                        state, classesList));
                            }
                        }
                    } else {
                        DynamicRealmObject dynamicRealmObject = null;
                        for (Object o : (Collection) collection) {
                            dynamicRealmObject = realm.createObject(TableNameManager.getShortName(shortName + Constants.UNDERSCORE + fieldInfo.getName(), realm), UUID.randomUUID().toString());

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
            } else if (GenericJson.class.isAssignableFrom(fieldInfo.getType())){
                if (!f.getType().getSimpleName().equalsIgnoreCase(clazz.getSimpleName())) {
                    state = selfReferenceState;
                } else if (selfReferenceState == SelfReferenceState.DEFAULT) {
                    state = SelfReferenceState.SUBCLASS;

                } else if (selfReferenceState == SelfReferenceState.SUBCLASS) {
                        state = SelfReferenceState.SUBCLASS;
//                        selfRefClass = clazz;
                }
                if (state != null) {
                    if (fieldInfo.getValue(obj) != null) {
                        DynamicRealmObject innerObject = saveClassData(
                                selfReferenceState == SelfReferenceState.SUBCLASS &&
                                        classes.contains(f.getType().getSimpleName()) ? name : shortName + Constants.UNDERSCORE + fieldInfo.getName(),
                                realm,
                                (Class<? extends GenericJson>) fieldInfo.getType(),
                                (GenericJson) obj.get(fieldInfo.getName()),
                                state, classesList);
                        object.setObject(fieldInfo.getName(), innerObject);
                    } else {
                        if (object.hasField(fieldInfo.getName())) {
                            object.setNull(fieldInfo.getName());
                        }
                    }
                }
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
        return object;
    }

    /**
     * Cascade delete items by id
     * @param collection collection name
     * @param realm Realm object
     * @param clazz Class
     * @param id item id to delete
     * @return count of deleted items (it should be "1" in correct case)
     */
    static int deleteClassData(String collection, DynamicRealm realm, Class<? extends GenericJson> clazz, String id) {
        String shortName = TableNameManager.getShortName(collection, realm);
        DynamicRealmObject realmObject = realm.where(shortName).equalTo(ID, id).findFirst();
        int size = realmObject != null ? 1 : 0;
        List<Field> fields = getClassFieldsAndParentClassFields(clazz);
        for (Field f : fields) {
            FieldInfo fieldInfo = FieldInfo.of(f);
            if (fieldInfo == null) {
                continue;
            }
            if (fieldInfo.getType().isArray() || Collection.class.isAssignableFrom(fieldInfo.getType())) {
                Class underlying = getUnderlying(f);
                if (underlying != null) {
                    RealmList<DynamicRealmObject> list = realmObject.getList(fieldInfo.getName());

                    List<String> ids = new ArrayList<>();
                    for (DynamicRealmObject object : list) {
                        if (object.hasField(ID) && object.getString(ID) != null) {
                            ids.add(object.getString(ID));
                        }
                    }

                    for (String _id : ids) {
                        deleteClassData(shortName + Constants.UNDERSCORE + fieldInfo.getName(), realm,
                                (Class<? extends GenericJson>) underlying, _id);
                    }

                }
            } else if (GenericJson.class.isAssignableFrom(fieldInfo.getType())) {
                DynamicRealmObject object = realmObject.getObject(fieldInfo.getName());
                if (object != null && object.hasField(ID) && object.getString(ID) != null) {
                    deleteClassData(shortName + Constants.UNDERSCORE + fieldInfo.getName(), realm,
                            (Class<? extends GenericJson>) fieldInfo.getType(), object.getString(ID));
                }
            }
        }
        if (realmObject.hasField(KinveyMetaData.AccessControlList.ACL)
                && !collection.endsWith(Constants.UNDERSCORE + KinveyMetaData.KMD)
                && !collection.endsWith(Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL )
                && realmObject.getObject(KinveyMetaData.AccessControlList.ACL) != null
                && realmObject.getObject(KinveyMetaData.AccessControlList.ACL).hasField(ID)) {
            deleteClassData(shortName + Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL, realm,
                    KinveyMetaData.AccessControlList.class, realmObject.getObject(KinveyMetaData.AccessControlList.ACL).getString(ID));
        }
        if (realmObject.hasField(KinveyMetaData.KMD)
                && !collection.endsWith(Constants.UNDERSCORE + KinveyMetaData.KMD)
                && !collection.endsWith(Constants.UNDERSCORE + KinveyMetaData.AccessControlList.ACL)
                && realmObject.getObject(KinveyMetaData.KMD) != null
                && realmObject.getObject(KinveyMetaData.KMD).hasField(ID)) {
            deleteClassData(shortName + Constants.UNDERSCORE + KinveyMetaData.KMD, realm,
                    KinveyMetaData.class, realmObject.getObject(KinveyMetaData.KMD).getString(ID));
        }
        realmObject.deleteFromRealm();
        return size;
    }

    public static <T extends GenericJson> T realmToObject(DynamicRealmObject dynamic, Class<T> objectClass) {
        return realmToObject(dynamic, objectClass, false);
    }

    private static <T extends GenericJson> T realmToObject(DynamicRealmObject dynamic, Class<T> objectClass, boolean isEmbedded) {
        if (dynamic == null){
            return null;
        }
        T ret = null;
        try {
            ret = objectClass.newInstance();
            ClassInfo classInfo = ClassInfo.of(objectClass);
            FieldInfo info;
            Object o;
            for (String field : dynamic.getFieldNames()){

                info = classInfo.getFieldInfo(field);

                o = dynamic.get(field);
                if (info == null){
                    //prevent private fields like "__ttl__" to be published
                    if (isEmbedded) {
                        if (!EMBEDDED_OBJECT_PRIVATE_FIELDS.contains(field)){
                            if (o instanceof DynamicRealmObject){
                                ret.put(field, realmToObject((DynamicRealmObject) o, GenericJson.class, true));
                            } else {
                                ret.put(field, o);
                            }
                        }
                    } else {
                        if (!PRIVATE_FIELDS.contains(field)){
                            if (o instanceof DynamicRealmObject){
                                ret.put(field, realmToObject((DynamicRealmObject) o, GenericJson.class, true));
                            } else {
                                ret.put(field, o);
                            }
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
                            (Class<? extends GenericJson>)info.getType(), true));
                } else if (isArrayOrCollection(info.getType())){
                    Class underlying = getUnderlying(info.getField());
                    if (underlying != null){
                        RealmList<DynamicRealmObject> list = dynamic.getList(info.getName());
                        if (underlying.isArray() && GenericJson.class.isAssignableFrom(underlying)){
                            GenericJson[] array = (GenericJson[])Array.newInstance(underlying, list.size());
                            for (int i = 0 ; i < list.size(); i++){
                                array[i] = realmToObject(list.get(i), underlying, true);
                            }
                            ret.put(info.getName(), array);
                        } else {
                            Collection<Object> c = Data.newCollectionInstance(info.getType());
                            if (GenericJson.class.isAssignableFrom(underlying)) {
                                for (int i = 0; i < list.size(); i++) {
                                    c.add(realmToObject(list.get(i), underlying, true));
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
            if (!isEmbedded && !ret.containsKey(KinveyMetaData.KMD) && dynamic.hasField(KinveyMetaData.KMD)){
                ret.put(KinveyMetaData.KMD, realmToObject(dynamic.getObject(KinveyMetaData.KMD), KinveyMetaData.class, true));
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
