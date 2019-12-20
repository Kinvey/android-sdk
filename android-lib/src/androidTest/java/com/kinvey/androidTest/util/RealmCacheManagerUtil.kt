package com.kinvey.androidTest.util

import com.kinvey.android.Client
import com.kinvey.android.cache.RealmCacheManager
import io.realm.DynamicRealm
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Created by yuliya on 11/17/17.
 */

object RealmCacheManagerUtil {

    fun getRealm(client: Client<*>?): DynamicRealm? {
        var method: Method? = null
        try {
            method = RealmCacheManager::class.java.getDeclaredMethod("getDynamicRealm", null)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
        method?.isAccessible = true
        var realm: DynamicRealm? = null
        try {
            realm = method?.invoke(RealmCacheManager(client!!), null) as DynamicRealm
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return realm
    }

    fun setTableHash(client: Client<*>?, collection: String?, hash: String?, realm: DynamicRealm?) {
        var method: Method? = null
        try {
            method = RealmCacheManager::class.java.getDeclaredMethod("setTableHash", String::class.java, String::class.java, DynamicRealm::class.java)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
        method?.isAccessible = true
        try {
            method?.invoke(RealmCacheManager(client!!), collection, hash, realm)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
}