package com.kinvey.android.cache

import io.realm.DynamicRealm
import io.realm.DynamicRealmObject
import io.realm.FieldAttribute
import io.realm.RealmObjectSchema
import java.util.*

/**
 * Created by yuliya on 10/12/17.
 */
object TableNameManager {

    private const val COLLECTION_NAME = "_tableManager"
    private const val ORIGINAL_NAME_FIELD = "originalName"
    private const val SHORT_NAME_FIELD = "optimizedName"

    private fun initTable(realm: DynamicRealm) {
        if (realm.schema.get(COLLECTION_NAME) == null) {
            val realmObjectSchema: RealmObjectSchema = realm.schema.create(COLLECTION_NAME)
            realmObjectSchema.addField(SHORT_NAME_FIELD, String::class.java, FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED)
            realmObjectSchema.addField(ORIGINAL_NAME_FIELD, String::class.java, FieldAttribute.REQUIRED)
        }
    }

    @JvmStatic
    fun createShortName(originalName: String?, realm: DynamicRealm): String {
        initTable(realm)
        val shortName = UUID.randomUUID().toString()
        val obj: DynamicRealmObject = realm.createObject(COLLECTION_NAME, shortName)
        obj.set(ORIGINAL_NAME_FIELD, originalName)
        return shortName
    }

    @JvmStatic
    fun removeShortName(originalName: String?, realm: DynamicRealm) {
        initTable(realm)
        realm.where(COLLECTION_NAME).equalTo(SHORT_NAME_FIELD, originalName).findFirst()?.deleteFromRealm()
    }

    @JvmStatic
    fun getShortName(originalName: String?, realm: DynamicRealm): String? {
        initTable(realm)
        val realmObject = realm.where(COLLECTION_NAME).equalTo(ORIGINAL_NAME_FIELD, originalName).findFirst()
        return realmObject?.getString(SHORT_NAME_FIELD)
    }

    @JvmStatic
    fun getOriginalName(shortName: String?, realm: DynamicRealm): String? {
        initTable(realm)
        val realmObject = realm.where(COLLECTION_NAME).equalTo(SHORT_NAME_FIELD, shortName).findFirst()
        return realmObject?.getString(ORIGINAL_NAME_FIELD)
    }
}