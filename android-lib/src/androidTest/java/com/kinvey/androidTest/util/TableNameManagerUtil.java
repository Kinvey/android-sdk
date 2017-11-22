package com.kinvey.androidTest.util;

import com.kinvey.android.cache.TableNameManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.realm.DynamicRealm;

/**
 * Created by yuliya on 11/17/17.
 */

public class TableNameManagerUtil {

    public static String getShortName(String originalName, DynamicRealm realm) {
        Method method = null;
        try {
            method = TableNameManager.class.getDeclaredMethod("getShortName", String.class, DynamicRealm.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        method.setAccessible(true);
        String s = null;
        try {
            s = (String) method.invoke(null, originalName, realm);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return s;
    }

}
