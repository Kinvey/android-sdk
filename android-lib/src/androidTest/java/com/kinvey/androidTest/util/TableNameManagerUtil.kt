package com.kinvey.androidTest.util

import com.kinvey.android.cache.TableNameManager
import io.realm.DynamicRealm
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Created by yuliya on 11/17/17.
 */

object TableNameManagerUtil {

    fun getShortName(originalName: String?, realm: DynamicRealm?): String? {
        var method: Method? = null
        try {
            method = TableNameManager::class.java.getDeclaredMethod("getShortName", String::class.java, DynamicRealm::class.java)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
        method?.isAccessible = true
        var s: String? = null
        try {
            s = method?.invoke(null, originalName, realm) as String?
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return s
    }

    fun getOriginalName(shortName: String?, realm: DynamicRealm?): String? {
        var method: Method? = null
        try {
            method = TableNameManager::class.java.getDeclaredMethod("getOriginalName", String::class.java, DynamicRealm::class.java)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
        method?.isAccessible = true
        var s: String? = null
        try {
            s = method?.invoke(null, shortName, realm) as String?
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return s
    }
}