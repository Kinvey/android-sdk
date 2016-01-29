package com.kinvey.android.cache;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.FieldInfo;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Prots on 1/27/16.
 */
public abstract class ClassHash {

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

        Map<String, Class> allowedFields = getAllowedFields(clazz);

        for (Map.Entry<String, Class> f : allowedFields.entrySet()){
            sb.append(f.getKey()).append(":").append(f.getValue().getName()).append("\n");
        }

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

    public static Map<String, Class> getAllowedFields(Class<? extends GenericJson> clazz){
        Map<String, Class> ret = new HashMap<String, Class>();

        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields){
            FieldInfo fieldInfo = FieldInfo.of(f);
            if (fieldInfo == null){
                continue;
            }
            for (Class c : ALLOWED){
                if (fieldInfo.getType().equals(c)){
                    ret.put(fieldInfo.getName(), c);
                }
            }
        }
        if (!ret.containsKey("_id")){
            ret.put("_id", String.class);
        }
        return ret;
    }

    public static Map<String, Object> getData(Class<? extends GenericJson> clazz, GenericJson obj){
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Class> allowedFields = getAllowedFields(clazz);

        for (Map.Entry<String, Class> entity : allowedFields.entrySet()){
                data.put(entity.getKey(), obj.get(entity.getKey()));
        }
        if (!data.containsKey("_id")){
            data.put("_id", UUID.randomUUID().toString());
        }
        return data;
    }


}
