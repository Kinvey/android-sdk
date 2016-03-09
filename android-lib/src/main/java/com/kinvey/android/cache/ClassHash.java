package com.kinvey.android.cache;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.Data;
import com.google.api.client.util.FieldInfo;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

    public static String TTL_FIELD = "__ttl__";

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


        Field[] fields = clazz.getDeclaredFields();
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
            } else {
                for (Class c : ALLOWED) {
                    if (fieldInfo.getType().equals(c)) {
                        if (!fieldInfo.getName().equals("_id") && !fieldInfo.getName().equals(TTL_FIELD)){
                            sb.append(fieldInfo.getName()).append(":").append(c.getName()).append(";");
                        }

                        break;
                    }
                }
            }
        }
        sb.append("_id").append(":").append(String.class.getName()).append(";");
        sb.append(TTL_FIELD).append(":").append(Long.class.getName()).append(";");


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

        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields){
            FieldInfo fieldInfo = FieldInfo.of(f);
            if (fieldInfo == null){
                continue;
            }
            if (fieldInfo.getType().isArray() || Collection.class.isAssignableFrom(fieldInfo.getType())){

                Class underlying = getUnderlying(f);

                if (underlying != null && GenericJson.class.isAssignableFrom(underlying)){
                    RealmObjectSchema innerScheme = createScheme(name + "_" + fieldInfo.getName(), realm, (Class<? extends GenericJson>) underlying);
                    schema.addRealmListField(fieldInfo.getName(), innerScheme);
                }
            } else if (GenericJson.class.isAssignableFrom(fieldInfo.getType())){
                RealmObjectSchema innerScheme = createScheme(name + "_" + fieldInfo.getName(), realm, (Class<? extends GenericJson>) fieldInfo.getType());
                schema.addRealmObjectField(fieldInfo.getName(), innerScheme);
            } else {
                for (Class c : ALLOWED) {
                    if (fieldInfo.getType().equals(c)) {
                        if (!fieldInfo.getName().equals("_id")){
                            schema.addField(fieldInfo.getName(), fieldInfo.getType());
                        }

                        break;
                    }
                }
            }
        }
        if (!schema.hasField("_id")){
            schema.addField("_id", String.class, FieldAttribute.PRIMARY_KEY);
        }
        if (!schema.hasField(TTL_FIELD)){
            schema.addField(TTL_FIELD, Long.class);
        }

        return schema;

    }

    public static DynamicRealmObject saveData(String name, DynamicRealm realm, Class<? extends GenericJson> clazz, GenericJson obj) {

        Field[] fields = clazz.getDeclaredFields();

        DynamicRealmObject object = null;

        if (obj.containsKey("_id") && obj.get("_id") != null) {
            object = realm.where(name)
                    .equalTo("_id", (String) obj.get("_id"))
                    .findFirst();
        } else {
            obj.put("_id", UUID.randomUUID().toString());
        }

        if (object == null){
            object = realm.createObject(name, obj.get("_id"));
        }

        for (Field f : fields){
            FieldInfo fieldInfo = FieldInfo.of(f);
            if (fieldInfo == null){
                continue;
            }
            if (isArrayOrCollection(f.getType()) && fieldInfo.getValue(obj) != null){
                Class underlying = getUnderlying(f);
                if (GenericJson.class.isAssignableFrom(underlying)){
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
                if (!fieldInfo.getName().equals("_id")) {
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
        if (object.get(TTL_FIELD) != obj.get(TTL_FIELD)){
            object.set(TTL_FIELD, obj.get(TTL_FIELD));
        }

        if (object.get("_id") != obj.get("_id")){
            object.set("_id", obj.get("_id"));
        }

        return object;
    }


    public static <T extends GenericJson> T realmToObject(DynamicRealmObject dynamic, Class<T> objectClass){
        T ret = null;
        try {
            ret = objectClass.newInstance();

            ClassInfo classInfo = ClassInfo.of(objectClass);

            for (FieldInfo info : classInfo.getFieldInfos()){
                Object o = dynamic.get(info.getName());

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
                        } else {
                            Collection<Object> c = Data.newCollectionInstance(info.getType());
                            for (int i = 0 ; i < list.size(); i++){
                                c.add(realmToObject(list.get(i), underlying));
                            }
                        }

                    }
                } else {
                    ret.put(info.getName(), o);
                }

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


}
