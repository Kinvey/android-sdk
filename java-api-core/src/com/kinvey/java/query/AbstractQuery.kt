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

package com.kinvey.java.query


//import com.google.gson.Gson;
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.JsonGenerator
import com.google.common.base.Preconditions
import com.kinvey.java.KinveyException
import com.kinvey.java.query.QueryFilter.QueryFilterBuilder
import java.io.IOException
import java.io.Serializable
import java.io.StringWriter
import java.util.*

/**
 * Abstract class for Query objects.
 *
 * @author mjsalinger
 * @since 2.0
 */
abstract class AbstractQuery(protected var builder: QueryFilterBuilder?) : Serializable {
    /**
     * Possible sort orders (Ascending and Descending)
     */
    enum class SortOrder { ASC, DESC }

    protected var sort: LinkedHashMap<String, SortOrder>
    private var queryString: String? = null
    /**
     * Sets the raw query string
     *
     * @param queryString
     * @return this
     */
    open fun setQueryString(queryString: String?): AbstractQuery {
        this.queryString = queryString
        return this
    }

    /**
     *
     * @return Query filter as AbstractMap<String></String>, Object>
     */
    open val queryFilterMap: AbstractMap<String, Any>?
        get() = builder?.filterMap
                ?.filterNot { it == null } as AbstractMap<String, Any>?

    /**
     *
     * @return Query filter as JSon
     */
    open fun getQueryFilterJson(factory: JsonFactory?): String? {
        Preconditions.checkNotNull(factory)
        if (queryString != null) {
            return queryString
        }
        val writer = StringWriter()
        var jsonResult = ""
        try {
            val generator = factory?.createJsonGenerator(writer)
            val filterMap = queryFilterMap
            buildQueryString(generator, filterMap)
            generator?.flush()
            jsonResult = writer.toString()
        } catch (ex: Exception) {
            throw KinveyException("Exception thrown during building of query.", "Please inspect the QueryMap for errors: $queryFilterMap", ex.message)
        }
        return if (jsonResult == "{}" || jsonResult == "") {
            null
        } else jsonResult
    }

    @Throws(IOException::class)
    private fun buildQueryString(generator: JsonGenerator?, filterMap: Map<String, Any>?) {
        try {
            val filterMapSize = filterMap?.entries?.size ?: 0
            var hasMultipleFilters = false
            var i = 0
            filterMap?.let { map ->
                for ((key, value) in map) {
                    if (!hasMultipleFilters) {
                        generator?.writeStartObject()
                    }
                    if (value == null) {
                        generator?.writeFieldName(key)
                        generator?.writeNull()
                    } else {
                        generateJsonDataFromValue(generator, key, value)
                    }
                    i++
                    if (filterMapSize > 1 && i < filterMapSize) {
                        // there are multiple filters and we have not reached the last one
                        hasMultipleFilters = true
                    } else {
                        generator?.writeEndObject()
                    }
                }
            }
        } catch (e: Exception) {
            e.message
        }
    }

    private fun generateJsonDataFromValue(generator: JsonGenerator?, key: String, value: Any) {
        val valueClass = value.javaClass
        when {
            valueClass == String::class.java -> {
                generator?.writeFieldName(key)
                generator?.writeString(value as String)
            }
            valueClass == Array<String>::class.java -> {
                generator?.writeFieldName(key)
                generator?.writeStartArray()
                val valueStr = value as Array<String>
                for (str in valueStr) {
                    generator?.writeString(str)
                }
                generator?.writeEndArray()
            }
            valueClass == Boolean::class.java || valueClass == java.lang.Boolean::class.java -> {
                generator?.writeFieldName(key)
                generator?.writeBoolean(value as Boolean)
            }
            valueClass == Array<Boolean>::class.java -> {
                generator?.writeFieldName(key)
                generator?.writeStartArray()
                val valueBool = value as Array<Boolean>
                for (bool in valueBool) {
                    generator?.writeBoolean(bool)
                }
                generator?.writeEndArray()
            }
            valueClass == Int::class.java || valueClass == java.lang.Integer::class.java -> {
                generator?.writeFieldName(key)
                generator?.writeNumber(value as Int)
            }
            valueClass == Array<Int>::class.java -> {
                generator?.writeFieldName(key)
                generator?.writeStartArray()
                val valueInt = value as Array<Int>
                for (integer in valueInt) {
                    generator?.writeNumber(integer)
                }
                generator?.writeEndArray()
            }
            valueClass == Long::class.java || valueClass == java.lang.Long::class.java -> {
                generator?.writeFieldName(key)
                generator?.writeNumber(value as Long)
            }
            valueClass == Array<Long>::class.java -> {
                generator?.writeFieldName(key)
                generator?.writeStartArray()
                val valueLong = value as Array<Long>
                for (longVal in valueLong) {
                    generator?.writeNumber(longVal)
                }
                generator?.writeEndArray()
            }
            valueClass == Double::class.java || valueClass == java.lang.Double::class.java -> {
                generator?.writeFieldName(key)
                generator?.writeNumber(value as Double)
            }
            valueClass == Array<Double>::class.java -> {
                generator?.writeFieldName(key)
                generator?.writeStartArray()
                val valueDouble = value as Array<Double>
                for (doubleVal in valueDouble) {
                    generator?.writeNumber(doubleVal)
                }
                generator?.writeEndArray()
            }
            valueClass == Array<DoubleArray>::class.java || valueClass == Array<Array<Double>>::class.java-> {
                generator?.writeFieldName(key)
                generator?.writeStartArray()
                val values = value as Array<Array<Double>>
                for (doubleArr in values) {
                     generator?.serialize(doubleArr)
                }
                generator?.writeEndArray()
            }
            valueClass == Float::class.java || valueClass == java.lang.Float::class.java -> {
                generator?.writeFieldName(key)
                generator?.writeNumber(value as Float)
            }
            valueClass == Array<Float>::class.java -> {
                generator?.writeFieldName(key)
                generator?.writeStartArray()
                val valueFloat = value as Array<Float>
                for (floatVal in valueFloat) {
                    generator?.writeNumber(floatVal)
                }
                generator?.writeEndArray()
            }
            valueClass == LinkedHashMap::class.java -> {
                // Value is an added filter
                generator?.writeFieldName(key)
                buildQueryString(generator, value as LinkedHashMap<String, Any>?)
            }
            valueClass.componentType != null && valueClass.componentType == LinkedHashMap::class.java -> if (valueClass.isArray) {
                // Value is a map, so this is a nested query. Recursively call into it.
                generator?.writeFieldName(key)
                generator?.writeStartArray()
                val valueMap: Array<LinkedHashMap<String, Any>?> = value as Array<LinkedHashMap<String, Any>?>
                for (map in valueMap) {
                    buildQueryString(generator, map)
                }
                generator?.writeEndArray()
            } else {
                // Value is an added filter
                buildQueryString(generator, value as LinkedHashMap<String, Any>?)
            }
        }
    }

    /**
     * Adds a sort  sort condition to the Query
     *
     * @param field Field to sort on
     * @param order Order to sort values (Ascending/Descending)
     * @return  Query object
     */
    open fun addSort(field: String?, order: SortOrder?): AbstractQuery {
        var order = order
        Preconditions.checkNotNull(field)
        if (order == null) {
            order = SortOrder.ASC
        }
        if (field != null) {
            sort[field] = order
        }
        return this
    }

    fun getSort(): Map<String, SortOrder?> {
        return sort
    }

    // Abstract Methods
    abstract fun equals(key: String?, value: Any?): AbstractQuery?

    abstract fun greaterThan(key: String?, value: Any?): AbstractQuery?
    abstract fun lessThan(key: String?, value: Any?): AbstractQuery?
    abstract fun greaterThanEqualTo(key: String?, value: Any?): AbstractQuery?
    abstract fun lessThanEqualTo(key: String?, value: Any?): AbstractQuery?
    abstract fun notEqual(key: String?, value: Any?): AbstractQuery?
    abstract fun `in`(key: String?, value: Array<out Any>?): AbstractQuery?
    abstract fun notIn(key: String?, value: Array<out Any?>?): AbstractQuery?
    abstract fun regEx(key: String?, value: Any?): AbstractQuery?
    abstract fun startsWith(key: String?, value: Any?): AbstractQuery?
    //public abstract AbstractQuery endsWith(String key, Object value);
    abstract fun all(key: String?, value: Array<out Any?>?): AbstractQuery?

    abstract fun size(key: String?, value: Int): AbstractQuery?
    abstract fun and(query: AbstractQuery?): AbstractQuery?
    abstract fun or(query: AbstractQuery?): AbstractQuery?
    abstract operator fun not(): AbstractQuery?
    abstract val sortString: String?
    abstract fun nearSphere(field: String?, lat: Double, lon: Double): AbstractQuery?
    abstract fun nearSphere(field: String?, lat: Double, lon: Double, maxDistance: Double): AbstractQuery?
    abstract fun withinBox(field: String?, pointOneLat: Double, pointOneLon: Double,
                           pointTwoLat: Double, pointTwoLon: Double): AbstractQuery?

    abstract fun withinPolygon(field: String?, pointOneLat: Double, pointOneLon: Double,
                               pointTwoLat: Double, pointTwoLon: Double,
                               pointThreeLat: Double, pointThreeLon: Double,
                               pointFourLat: Double, pointFourLon: Double): AbstractQuery?

    companion object {
        private const val serialVersionUID = 5635939847038496849L
    }

    /**
     * Constructor, sets the appropriate Filter builder and creates a Sort object
     *
     * @param builder
     */

    init {
        this.sort = LinkedHashMap()
    }
}