package com.kinvey.androidTest.util;

import com.kinvey.android.Client;
import com.kinvey.android.cache.RealmCacheManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.realm.DynamicRealm;

/**
 * Created by yuliya on 11/17/17.
 */

public class RealmCacheManagerUtil {

    public static DynamicRealm getRealm(Client client) {
        Method method = null;
        try {
            method = RealmCacheManager.class.getDeclaredMethod("getDynamicRealm", null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        method.setAccessible(true);
        DynamicRealm realm = null;
        try {
            realm = (DynamicRealm) method.invoke(new RealmCacheManager(client) , null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return realm;
    }

    public static void setTableHash(Client client, String collection, String hash, DynamicRealm realm) {
        Method method = null;
        try {
            method = RealmCacheManager.class.getDeclaredMethod("setTableHash", String.class, String.class, DynamicRealm.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        method.setAccessible(true);
        try {
            method.invoke(new RealmCacheManager(client) , collection, hash, realm);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
