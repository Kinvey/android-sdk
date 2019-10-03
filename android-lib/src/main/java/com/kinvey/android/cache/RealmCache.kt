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

package com.kinvey.android.cache

import com.google.api.client.json.GenericJson
import com.kinvey.android.cache.ClassHash.checkAclKmdFields
import com.kinvey.android.cache.ClassHash.createScheme
import com.kinvey.android.cache.ClassHash.deleteClassData
import com.kinvey.android.cache.ClassHash.getClassHash
import com.kinvey.android.cache.ClassHash.isArrayOrCollection
import com.kinvey.android.cache.ClassHash.migration
import com.kinvey.android.cache.ClassHash.realmToObject
import com.kinvey.android.cache.ClassHash.saveData
import com.kinvey.java.KinveyException
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.model.AggregateType
import com.kinvey.java.model.Aggregation.Result
import com.kinvey.java.query.AbstractQuery.SortOrder
import io.realm.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Prots on 1/26/16.
 */
class RealmCache<T : GenericJson>(val collection: String, private val mCacheManager: RealmCacheManager,
                                   val collectionItemClass: Class<T>, ttl: Long) : ICache<T> {

    override var ttl: Long = ttl
        get() {
            return field
        }
        set(value) {
            field = if (value > 0) value else 0
        }

    /**
     * Get items from the realm with sorting it it exists
     * @param realmQuery
     * @param query
     * @return
     */
    private operator fun get(realmQuery: RealmQuery<DynamicRealmObject>, query: Query?): RealmResults<DynamicRealmObject> {
        val objects: RealmResults<DynamicRealmObject>
        val sortingOrders = query?.getSort()
        objects = if (!sortingOrders.isNullOrEmpty()) {
            val fields: MutableList<String> = ArrayList()
            val sorts: MutableList<Sort> = ArrayList()
            for ((key, value) in sortingOrders) {
                fields.add(key)
                if (value == SortOrder.ASC) {
                    sorts.add(Sort.ASCENDING)
                } else {
                    sorts.add(Sort.DESCENDING)
                }
            }
            //objects = realmQuery.findAllSorted(fields.toArray(new String[fields.size()]), sorts.toArray(new Sort[sorts.size()]));
            realmQuery.sort(fields.toTypedArray(), sorts.toTypedArray()).findAll()
        } else {
            realmQuery.findAll()
        }
        return objects
    }

    override fun get(query: Query?): List<T> {
        val mRealm: DynamicRealm = mCacheManager.dynamicRealm
        var ret: MutableList<T> = mutableListOf()
        mRealm.use { mRealm ->
            val realmQuery: RealmQuery<DynamicRealmObject> = mRealm.where(TableNameManager.getShortName(collection, mRealm))
                    .greaterThanOrEqualTo(ClassHash.TTL, Calendar.getInstance().timeInMillis)
            val isIgnoreIn = isQueryContainsInOperator(query?.queryFilterMap)
            QueryHelper.prepareRealmQuery(realmQuery, query?.queryFilterMap, isIgnoreIn)
            val objects = get(realmQuery, query)
            val limit = query?.limit ?: 0
            val skip = query?.skip ?: 0
            ret = objects.mapNotNull { obj ->
                realmToObject(obj, collectionItemClass)
            }.toMutableList()
            if (isIgnoreIn) {
                checkCustomInQuery(query?.queryFilterMap, ret)
            }
            //own skipping implementation
            if (skip > 0) {
                for (i in 0 until skip) {
                    if (ret.isNotEmpty()) {
                        ret.removeAt(0)
                    }
                }
            }
            //own limit implementation
            if (limit > 0 && ret.size > limit) {
                ret = ret.subList(0, limit)
            }
        }
        return ret
    }

    override fun get(ids: Iterable<String>): List<T> {
        val mRealm: DynamicRealm = mCacheManager.dynamicRealm
        var ret: List<T> = mutableListOf()
        mRealm.use { mRealm ->
            mRealm.beginTransaction()
            val query: RealmQuery<DynamicRealmObject> = mRealm.where(TableNameManager.getShortName(collection, mRealm))
                    .greaterThanOrEqualTo(ClassHash.TTL, Calendar.getInstance().timeInMillis)
                    .beginGroup()
            val iterator = ids.iterator()
            if (iterator.hasNext()) {
                query.equalTo(ID, iterator.next())
                while (iterator.hasNext()) {
                    val id = iterator.next()
                    query.or().equalTo(ID, id)
                }
            }
            query.endGroup()
            val objects = get(query, null)
            ret = objects.mapNotNull { obj ->
                realmToObject(obj, collectionItemClass)
            }
            mRealm.commitTransaction()
        }
        return ret
    }

    override fun get(id: String): T? {
        val mRealm: DynamicRealm = mCacheManager.dynamicRealm
        var ret: T? = null
        mRealm.use { mRealm ->
            mRealm.beginTransaction()
            val obj = mRealm.where(TableNameManager.getShortName(collection, mRealm))
                    .equalTo(ID, id)
                    .greaterThanOrEqualTo(ClassHash.TTL, Calendar.getInstance().timeInMillis)
                    .findFirst()
            ret = if (obj == null) null else realmToObject(obj, collectionItemClass)
            mRealm.commitTransaction()
        }
        return ret
    }

    override fun get(): List<T> {
        val mRealm: DynamicRealm = mCacheManager.dynamicRealm
        var ret: MutableList<T> = mutableListOf()
        mRealm.use { mRealm ->
            mRealm.beginTransaction()
            val query: RealmQuery<DynamicRealmObject> = mRealm.where(TableNameManager.getShortName(collection, mRealm))
                    .greaterThanOrEqualTo(ClassHash.TTL, Calendar.getInstance().timeInMillis)
            val objects = get(query, null)
            ret = objects.mapNotNull { obj ->
                realmToObject(obj, collectionItemClass)
            }.toMutableList()
            mRealm.commitTransaction()
        }
        return ret
    }

    override fun save(items: Iterable<T>?): List<T> {
        val mRealm: DynamicRealm = mCacheManager.dynamicRealm
        var ret: MutableList<T> = mutableListOf()
        mRealm.use { mRealm ->
            mRealm.beginTransaction()
            ret = items?.mapNotNull {
                it?.put(ID, insertOrUpdate(it, mRealm)) as T
            }?.toMutableList() as MutableList<T>
            mRealm.commitTransaction()
        }
        return ret
    }

    override fun save(item: T?): T? {
        val mRealm: DynamicRealm = mCacheManager.dynamicRealm
        mRealm.use { mRealm ->
            mRealm.beginTransaction()
            item?.put(ID, insertOrUpdate(item, mRealm))
            mRealm.commitTransaction()
        }
        return item
    }

    override fun delete(query: Query?): Int {
        val realm: DynamicRealm = mCacheManager.dynamicRealm
        var i: Int = 0
        realm.use { realm -> i = delete(realm, query, collection) }
        return i
    }

    private fun delete(realm: DynamicRealm, query: Query?, tableName: String): Int {
        var ret: Int
        if (!isQueryContainsInOperator(query?.queryFilterMap)) {
            realm.beginTransaction()
            val realmQuery: RealmQuery<DynamicRealmObject> = realm.where(TableNameManager.getShortName(tableName, realm))
            QueryHelper.prepareRealmQuery(realmQuery, query?.queryFilterMap)
            val result = get(realmQuery, query)
            ret = result.size
            val limit = query?.limit ?: 0
            val skip = query?.skip ?: 0
            if (limit > 0) {
                // limit modifier has been applied, so take a subset of the Realm result set
                if (skip < result.size) {
                    val endIndex = Math.min(ret, skip + limit)
                    val subresult: List<DynamicRealmObject> = result.subList(skip, endIndex)
                    val ids = getRealmListIds(subresult)
                    ret = subresult.size
                    realm.commitTransaction()
                    if (ids.isNotEmpty()) {
                        this.delete(realm, ids, collection)
                    }
                } else {
                    realm.commitTransaction()
                    ret = 0
                }
            } else if (skip > 0) {
                // only skip modifier has been applied, so take a subset of the Realm result set
                if (skip < result.size) {
                    val subresult: List<DynamicRealmObject> = result.subList(skip, result.size)
                    ret = subresult.size
                    val ids = getRealmListIds(subresult)
                    realm.commitTransaction()
                    this.delete(realm, ids, tableName)
                } else {
                    realm.commitTransaction()
                    ret = 0
                }
            } else {
                // no skip or limit applied to query, so delete all results from Realm
                realm.commitTransaction()
                val ids = getRealmListIds(result)
                this.delete(realm, ids, tableName)
            }
        } else {
            val list = get(query)
            ret = list.size
            val ids = list.map { id -> id[ID] as String }.toMutableList()
            delete(realm, ids, tableName)
            return ret
        }
        return ret
    }

    private fun getRealmListIds(list: List<DynamicRealmObject>?): List<String> {
        var ids: MutableList<String>? = mutableListOf()
        ids = list?.mapNotNull { id -> id.get<Any>(ID) as String }?.toMutableList()
        return ids ?: mutableListOf()
    }

    override fun delete(ids: Iterable<String>): Int {
        val realm: DynamicRealm = mCacheManager.dynamicRealm
        var i = 0
        realm.use { realm ->
            realm.beginTransaction()
            for (id in ids) {
                i += deleteClassData(collection, realm, collectionItemClass, id)
            }
            realm.commitTransaction()
        }
        return i
    }

    private fun delete(realm: DynamicRealm, ids: Iterable<String>, tableName: String): Int {
        var ret = 0
        realm.beginTransaction()
        ret = ids.onEach { id -> deleteClassData(tableName, realm, collectionItemClass, id) }
              .count()
        realm.commitTransaction()
        return ret
    }

    override fun delete(id: String): Int {
        val realm: DynamicRealm = mCacheManager.dynamicRealm
        var i: Int = 0
        realm.use { realm ->
            realm.beginTransaction()
            i = deleteClassData(collection, realm, collectionItemClass, id)
            realm.commitTransaction()
        }
        return i
    }

    override fun clear() {
        val mRealm: DynamicRealm = mCacheManager.dynamicRealm
        mRealm.use { mRealm ->
            mRealm.beginTransaction()
            mRealm.where(TableNameManager.getShortName(collection, mRealm))
                    .findAll()
                    .deleteAllFromRealm()
            mRealm.commitTransaction()
        }
    }

    override val first: T?
        get() {
            val mRealm: DynamicRealm = mCacheManager.dynamicRealm
            var ret: T? = null
            mRealm.use { mRealm ->
                mRealm.beginTransaction()
                val obj = mRealm.where(TableNameManager.getShortName(collection, mRealm)).findFirst()
                if (obj != null) {
                    ret = realmToObject(obj, collectionItemClass)
                }
                mRealm.commitTransaction()
            }
            return ret
        }

    override fun getFirst(q: Query): T? {
        val mRealm: DynamicRealm = mCacheManager.dynamicRealm
        var ret: T? = null
        mRealm.use { mRealm ->
            if (!isQueryContainsInOperator(q.queryFilterMap)) {
                mRealm.beginTransaction()
                val query: RealmQuery<DynamicRealmObject> = mRealm.where(TableNameManager.getShortName(collection, mRealm))
                QueryHelper.prepareRealmQuery(query, q.queryFilterMap)
                val obj = query.findFirst()
                if (obj != null) {
                    ret = realmToObject(obj, collectionItemClass)
                }
                mRealm.commitTransaction()
            } else {
                val list = get(q)
                ret = list[0]
            }
        }
        return ret
    }

    override fun count(q: Query?): Long {
        val mRealm: DynamicRealm = mCacheManager.dynamicRealm
        var ret: Long = 0
        mRealm.use { mRealm ->
            if (q != null && !isQueryContainsInOperator(q.queryFilterMap)) {
                mRealm.beginTransaction()
                val query: RealmQuery<DynamicRealmObject> = mRealm.where(TableNameManager.getShortName(collection, mRealm))
                QueryHelper.prepareRealmQuery(query, q.queryFilterMap)
                ret = query.count()
                mRealm.commitTransaction()
            } else {
                val list: List<T> = if (q != null) { get(q) } else { get() }
                ret = list.size.toLong()
            }
        }
        return ret
    }

    val hash: String
        get() = getClassHash(collectionItemClass)

    fun createRealmTable(realm: DynamicRealm) {
        createScheme(collection, realm, collectionItemClass)
    }

    /**
     * Migrate from old table name to new table name
     * @param realm Realm object
     */
    fun migration(realm: DynamicRealm) {
        migration(collection, realm, collectionItemClass)
    }

    /**
     * Fix to _acl_kmd tables
     * @param realm Realm object
     */
    fun checkAclKmdFields(realm: DynamicRealm) {
        checkAclKmdFields(collection, realm, collectionItemClass)
    }

    private fun insertOrUpdate(item: T?, mRealm: DynamicRealm): String {
        item?.put(ClassHash.TTL, itemExpireTime)
        saveData(collection, mRealm, collectionItemClass, item)
        item?.remove(ClassHash.TTL)
        return item?.run { item[ID].toString() } ?: ""
    }

    private val itemExpireTime: Long
        private get() {
            val currentTime = Calendar.getInstance().timeInMillis
            return if (currentTime + ttl < 0) Long.MAX_VALUE else currentTime + ttl
        }

    private fun isQueryContainsInOperator(queryMap: Map<String, Any>?): Boolean {
        for ((field, params) in queryMap!!) {
            if (field.equals("\$or", ignoreCase = true) || field.equals("\$and", ignoreCase = true)) {
                if (params?.javaClass?.isArray) {
                    val components = params as Array<Map<String, Any>>
                    if (components.size > 0) {
                        for (component in components) {
                            if (isQueryContainsInOperator(component)) {
                                return true
                            }
                        }
                    }
                }
            }
            if (field.contains(".")) {
                return false
            }
            if (params is Map<*, *>) {
                for ((key) in params as Map<String, Any>) {
                    if (key.equals("\$in", ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun checkCustomInQuery(queryMap: Map<String, Any>?, ret: MutableList<T>?): MutableList<T>? {
        //helper for ".in()" operator in List of primitives fields, because Realm doesn't support it
        var ret: MutableList<T>? = ret
        for ((field, params) in queryMap ?: mapOf()) {
            if (field.equals("\$or", ignoreCase = true)) {
                val mRealm: DynamicRealm = mCacheManager.dynamicRealm
                var objects = mutableListOf<DynamicRealmObject>()
                //get all objects from realm. It need for make manual search in elements with "in" operator
                mRealm.use { mRealm ->
                    objects = mRealm.where(TableNameManager.getShortName(collection, mRealm))
                            .greaterThanOrEqualTo(ClassHash.TTL, Calendar.getInstance().timeInMillis)
                            .findAll()
                }
                val allItems = objects.mapNotNull { obj ->
                    realmToObject(obj, collectionItemClass)
                }
                if (params.javaClass.isArray) {
                    val components = params as Array<Map<String, Any>>
                    if (components.isNotEmpty()) {
                        var newItems: List<T>? = ArrayList()
                        //get items from both sides of "or"
                        for (component in components) {
                            if (isQueryContainsInOperator(component)) {
                                newItems = checkCustomInQuery(component, allItems as MutableList<T>?)
                            }
                        }
                        //merge items from left and right parts of "or"
                        if (newItems != null && ret != null) {
                            // "ret" - it's items from search with was made exclude "in" operator
                            // "newItems" - it's items from manual search with "in" operator

                            val retCopy = ArrayList(ret)
                            var isItemExist: Boolean
                            for (item in newItems) {
                                isItemExist = false
                                for (oldItem in retCopy) {
                                    if (oldItem[ID] == item[ID]) {
                                        isItemExist = true
                                        break
                                    }
                                }
                                if (!isItemExist) {
                                    ret.add(item)
                                }
                            }
                        }
                    }
                }
            } else if (field.equals("\$and", ignoreCase = true)) {
                if (params.javaClass.isArray) {
                    val components = params as Array<Map<String, Any>>
                    if (components.isNotEmpty()) {
                        for (component in components) {
                            ret = checkCustomInQuery(component, ret)
                        }
                    }
                }
            }
            if (params is Map<*, *>) {
                var clazz: Class<*>
                var types: Types
                for ((operation, value) in params as Map<String, Any>) {
                    //paramMap.getValue() - contains operator's parameters
                    if (!isArrayOrCollection(value.javaClass)) {
                        return ret
                    }
                    val operatorParams = value as Array<Any>
                    clazz = value[0].javaClass
                    types = Types.getType(clazz)
                    if (operation.equals("\$in", ignoreCase = true)) {
                        val retCopy = ArrayList(ret ?: listOf())
                        for (t in retCopy) {
                            val isArray = t[field] is ArrayList<*>
                            var isExist = false
/*                            //check that search field is List (not primitives or object)
                            if (t.get(field) instanceof ArrayList) {*/

                            var arrayList: ArrayList<*>? = null
                            if (isArray) {
                                arrayList = t[field] as ArrayList<*>?
                            }
                            // if search field is LIST
                            else {
                                // if search field is not LIST
                                when (types) {
                                    Types.LONG -> {
                                        for (l in operatorParams as Array<Long>) {
                                            isExist = l == t[field] as Long?
                                            if (isExist) { break }
                                        }
                                        if (!isExist) { ret?.remove(t) }
                                    }
                                    Types.STRING -> {
                                        for (s in operatorParams as Array<String>) {
                                            isExist = s == t[field] as String?
                                            if (isExist) { break }
                                        }
                                        if (!isExist) { ret?.remove(t) }
                                    }
                                    Types.BOOLEAN -> {
                                        for (b in operatorParams as Array<Boolean>) {
                                            isExist = b == t[field] as Boolean?
                                            if (isExist) { break }
                                        }
                                        if (!isExist) { ret?.remove(t) }
                                    }
                                    Types.INTEGER -> {
                                        for (i in operatorParams as Array<Int>) {
                                            isExist = i == t[field] as Int?
                                            if (isExist) { break }
                                        }
                                        if (!isExist) { ret?.remove(t) }
                                    }
                                    Types.FLOAT -> {
                                        for (i in operatorParams as Array<Float>) {
                                            isExist = i == t[field] as Float
                                            if (isExist) { break }
                                        }
                                        if (!isExist) { ret?.remove(t) }
                                    }
                                }
                            }
                            if (isArray && arrayList?.isNotEmpty() == true && operatorParams.isNotEmpty()) {
                                when (types) {
                                    Types.LONG -> {
                                        val listOfLong = ArrayList(arrayList as List<Long>)
                                        for (lValue in listOfLong) {
                                            for (l in operatorParams as Array<Long>) {
                                                isExist = l == lValue
                                                if (isExist) { break }
                                            }
                                            if (isExist) break
                                        }
                                        if (!isExist) { ret?.remove(t) }
                                    }
                                    Types.STRING -> {
                                        val listOfString = ArrayList(arrayList as List<String>)
                                        for (sValue in listOfString) {
                                            for (s in operatorParams as Array<String>) {
                                                isExist = sValue == s
                                                if (isExist) { break }
                                            }
                                            if (isExist) break
                                        }
                                        if (!isExist) { ret?.remove(t) }
                                    }
                                    Types.BOOLEAN -> {
                                        val listOfBoolean = ArrayList(arrayList as List<Boolean>)
                                        for (bValue in listOfBoolean) {
                                            for (b in operatorParams as Array<Boolean>) {
                                                isExist = bValue == b
                                                if (isExist) { break }
                                            }
                                            if (isExist) break
                                        }
                                        if (!isExist) { ret?.remove(t) }
                                    }
                                    Types.INTEGER -> {
                                        val listOfInteger = ArrayList(arrayList as List<Long>)
                                        for (lValue in listOfInteger) {
                                            for (l in operatorParams as Array<Int>) {
                                                isExist = lValue == l.toLong()
                                                if (isExist) { break }
                                            }
                                            if (isExist) break
                                        }
                                        if (!isExist) { ret?.remove(t) }
                                    }
                                    Types.FLOAT -> {
                                        val listOfFloat = ArrayList(arrayList as List<Float>)
                                        for (lValue in listOfFloat) {
                                            for (l in operatorParams as Array<Float>) {
                                                isExist = lValue == l
                                                if (isExist) { break }
                                            }
                                            if (isExist) break
                                        }
                                        if (!isExist) { ret?.remove(t) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret
    }

    private fun calculation(type: AggregateType, fields: ArrayList<String>, reduceField: String?, q: Query): Array<Result> {
        val mRealm: DynamicRealm = mCacheManager.dynamicRealm
        val results: MutableList<Result> = ArrayList()
        mRealm.use { mRealm ->
            mRealm.beginTransaction()
            var query: RealmQuery<DynamicRealmObject> = mRealm.where(TableNameManager.getShortName(collection, mRealm))
            QueryHelper.prepareRealmQuery(query, q.queryFilterMap)
            var fieldType: RealmFieldType
            var ret: Number? = null
            var result: Result
            for (field in fields) {
                //RealmResults<DynamicRealmObject> realmObjects = query.findAllSorted(field);
                val realmObjects: RealmResults<DynamicRealmObject> = query.sort(field).findAll()
                for (dynObj in realmObjects) {
                    result = Result()
                    query = realmObjects.where()
                    for (fieldToQuery in fields) {
                        fieldType = dynObj.getFieldType(fieldToQuery)
                        query = when (fieldType) {
                            RealmFieldType.STRING -> {
                                val fieldObj: Any = dynObj.get(fieldToQuery)
                                val fieldStr = fieldObj.toString()
                                query.equalTo(fieldToQuery, fieldStr)
                            }
                            RealmFieldType.INTEGER -> query.equalTo(fieldToQuery, dynObj.get<Any>(fieldToQuery) as Long)
                            RealmFieldType.BOOLEAN -> query.equalTo(field, dynObj.get<Any>(field) as Boolean)
                            RealmFieldType.DATE -> query.equalTo(field, dynObj.get<Any>(field) as Date)
                            RealmFieldType.FLOAT -> query.equalTo(field, dynObj.get<Any>(field) as Float)
                            RealmFieldType.DOUBLE -> query.equalTo(field, dynObj.get<Any>(field) as Double)
                            else -> throw KinveyException("Current fieldType doesn't support. Supported types: STRING, INTEGER, BOOLEAN, DATE, FLOAT, DOUBLE")
                        }
                        result[fieldToQuery] = dynObj.get(fieldToQuery)
                    }
                    ret = when (type) {
                        AggregateType.SUM -> query.sum(reduceField)
                        AggregateType.MIN -> query.min(reduceField)
                        AggregateType.MAX -> query.max(reduceField)
                        AggregateType.AVERAGE -> query.average(reduceField)
                        AggregateType.COUNT -> query.count()
                    }
                    if (ret != null) {
                        result["_result"] = ret
                        if (results.contains(result)) {
                            continue
                        }
                        results.add(result)
                    }
                }
            }
            mRealm.commitTransaction()
        }
        //val resultsArray = arrayOfNulls<Result?>(results.size)
        return results.toTypedArray()
    }

    override fun group(aggregateType: AggregateType, fields: ArrayList<String>, reduceField: String?, q: Query): Array<Result> {
        return calculation(aggregateType, fields, reduceField, q)
    }

    enum class Types {
        STRING, INTEGER, LONG, BOOLEAN, FLOAT, OBJECT;

        companion object {
            private val ALL_TYPES_STRING: String = Arrays.toString(values())
            fun getType(clazz: Class<*>): Types {
                val className = clazz.simpleName.toUpperCase()
                return if (ALL_TYPES_STRING.contains(className)) {
                    valueOf(className)
                } else {
                    OBJECT
                }
            }
        }
    }

    companion object {
        private const val ID = "_id"
    }

    init {
        this.ttl = if (ttl > 0) ttl else 0
    }
}