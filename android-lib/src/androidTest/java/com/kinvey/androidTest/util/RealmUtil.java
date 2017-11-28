package com.kinvey.androidTest.util;

import com.kinvey.android.Client;
import com.kinvey.android.cache.RealmCacheManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.realm.DynamicRealm;

/**
 * Created by yuliya on 11/17/17.
 */

public class RealmUtil {

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
}
