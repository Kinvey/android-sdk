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

import com.google.api.client.util.Key
import com.kinvey.java.query.QueryFilter.QueryFilterBuilder
import com.kinvey.java.query.QueryFilter.QueryFilterBuilder.Operators
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

/**
 * Implementation of QueryFilter and QueryFilter.builder for Mongo
 *
 * @author mjsalinger
 * @since 2.0
 */
open class MongoQueryFilter : QueryFilter, Serializable {

    @Key
    override var filter: LinkedHashMap<String, Any?> = LinkedHashMap()

    override fun put(key: String, value: Any?) {
        filter[key] = value
    }

    override fun put(operation: String, key: String, value: Any?) {
        val nestedMap: LinkedHashMap<String, Any?> = LinkedHashMap()
        nestedMap[operation] = value
        if (filter.containsKey(key)) {
            val currentKey = filter[key]
            if (currentKey?.javaClass == String::class.java) {
                filter[key] = nestedMap
            } else {
                val currentMap = currentKey as LinkedHashMap<String, Any?>
                currentMap[operation] = value
            }
        } else {
            filter[key] = nestedMap
        }
    }

    override fun put(operation: String, key: String, value: Array<Any?>) {
        val nestedMap: LinkedHashMap<String, Any?> = LinkedHashMap()
        nestedMap[operation] = value
        if (filter.containsKey(key)) {
            val currentKey = filter[key]
            if (currentKey?.javaClass == String::class.java) {
                filter[key] = nestedMap
            } else {
                val currentMap = currentKey as LinkedHashMap<String, Any?>
                currentMap[operation] = value
            }
        } else {
            filter[key] = nestedMap
        }
    }

    override fun joinFilters(operation: String, filterToJoin: LinkedHashMap<String, Any?>) {
        val newFilter: LinkedHashMap<String, Any?> = LinkedHashMap()
        val combinedQueries: Array<LinkedHashMap<String, Any?>> = arrayOf(this.filter, filterToJoin)
        newFilter[operation] = combinedQueries
        this.filter = newFilter
    }

    override fun get(key: String): Any? {
        return filter[key]
    }

    override fun reset() {
        this.filter = LinkedHashMap()
    }

    open class MongoQueryFilterBuilder : QueryFilterBuilder, Serializable {

        private val query: MongoQueryFilter
        private var operatorMap: HashMap<Operators, String> = HashMap()

        override fun addFilter(operator: String, key: String, value: Any?) {
            query.put(operator, key, value)
        }

        override fun addFilter(operator: String, key: String, value: Array<Any?>?) {
            query.put(operator, key, value)
        }

        override fun equals(key: String, value: Any?) {
            query.put(key, value)
        }

        override fun resetFilter() {}
        override val filterMap: AbstractMap<String, Any?>
            get() = query.filter

        override fun joinFilter(operator: String, newQuery: AbstractQuery?) {
            query.joinFilters(operator, newQuery?.queryFilterMap as LinkedHashMap<String, Any?>)
        }

        override fun addLocationFilter(field: String, operator: String, point: DoubleArray, distance: Double) {
            val locationFilter = LinkedHashMap<String, Any>()
            val mongoPoint = arrayOf(point[1], point[0])
            locationFilter[operator] = mongoPoint
            if (distance > 0) {
                locationFilter[getOperator(Operators.MAXDISTANCE)] = distance
            }
            query.put(field, locationFilter)
        }

        override fun addLocationWhereFilter(field: String, operator: String, points: Array<DoubleArray>) {
            val locationFilter = LinkedHashMap<String, Any>()
            val withinFilter = LinkedHashMap<String, Any>()
            val mongoPoints = Array(points.size) { arrayOfNulls<Double?>(2) }
            for (i in mongoPoints.indices) {
                mongoPoints[i][0] = points[i][1]
                mongoPoints[i][1] = points[i][0]
            }
            locationFilter[operator] = mongoPoints
            withinFilter[getOperator(Operators.WITHIN)] = locationFilter
            query.put(field, withinFilter)
        }

        override fun negateQuery() {
            val currentMap = query.filter
            negate(currentMap)
            query.filter = currentMap
        }

        private fun negate(map: LinkedHashMap<String, Any?>): Boolean {
            var processParent = false
            val newMap = LinkedHashMap<String, LinkedHashMap<String, Any>>()
            val tempMap = map.clone() as LinkedHashMap<String, Any>
            val element = LinkedHashMap<String, Any>()
            for ((key, obj) in tempMap) {
                val op = getOperatorKey(key)
                if (obj.javaClass == LinkedHashMap::class.java) {
                    processParent = negate(obj as LinkedHashMap<String, Any?>)
                    if (processParent) {
                        val subMap = obj
                        map[key] = subMap["\$ne"] as Any
                    }
                }
                if (op == null && obj !is Map<*, *> && !processParent) {
                    val newNotEqualMap: LinkedHashMap<String, Any?> = LinkedHashMap()
                    newNotEqualMap[getOperator(Operators.NOTEQUAL)] = obj
                    map[key] = newNotEqualMap
                } else if (op != null) {
                    when (op) {
                        Operators.GREATERTHAN -> {
                            element["\$gt"] = obj
                            newMap["\$not"] = element
                        }
                        Operators.LESSTHAN -> {
                            element["\$lt"] = obj
                            newMap["\$not"] = element
                        }
                        Operators.GREATERTHANEQUAL -> {
                            element["\$gte"] = obj
                            newMap["\$not"] = element
                        }
                        Operators.LESSTHANEQUAL -> {
                            element["\$lte"] = obj
                            newMap["\$not"] = element
                        }
                        Operators.IN -> {
                            element["\$in"] = obj
                            newMap["\$not"] = element
                        }
                        Operators.NOTIN -> {
                            element["\$nin"] = obj
                            newMap["\$not"] = element
                        }
                        Operators.NOTEQUAL -> processParent = true
                        else -> {
                        }
                    }
                }
            }
            if (!processParent) {
                map.remove("\$lt")
                map.remove("\$gt")
                map.remove("\$gte")
                map.remove("\$lte")
                map.remove("\$in")
                map.remove("\$nin")
                for ((key, value) in newMap) {
                    map[key] = value
                }
            }
            return processParent
        }

        // Helper method to populate the Operators Map with the mongo operators
        private fun populateOperators() {
            operatorMap = HashMap()
            operatorMap[Operators.GREATERTHAN] = "\$gt"
            operatorMap[Operators.LESSTHAN] = "\$lt"
            operatorMap[Operators.GREATERTHANEQUAL] = "\$gte"
            operatorMap[Operators.LESSTHANEQUAL] = "\$lte"
            operatorMap[Operators.NOTEQUAL] = "\$ne"
            operatorMap[Operators.IN] = "\$in"
            operatorMap[Operators.ALL] = "\$all"
            operatorMap[Operators.NOTIN] = "\$nin"
            operatorMap[Operators.SIZE] = "\$size"
            operatorMap[Operators.REGEX] = "\$regex"
            operatorMap[Operators.OPTIONS] = "\$options"
            operatorMap[Operators.AND] = "\$and"
            operatorMap[Operators.OR] = "\$or"
            operatorMap[Operators.NOT] = "\$not"
            operatorMap[Operators.NEARSPHERE] = "\$nearSphere"
            operatorMap[Operators.MAXDISTANCE] = "\$maxDistance"
            operatorMap[Operators.WITHIN] = "\$within"
            operatorMap[Operators.WITHINBOX] = "\$box"
            operatorMap[Operators.WITHINPOLYGON] = "\$polygon"
            operatorMap[Operators.LOCATION] = "\$loc"
        }

        override fun getOperator(operator: Operators): String {
            return operatorMap[operator] ?: ""
        }

        fun getOperatorKey(value: String): Operators? {
            for (key in operatorMap.keys) {
                if (operatorMap[key] == value) {
                    return key
                }
            }
            return null
        }

        companion object {
            private const val serialVersionUID = 5635939847038496849L
        }

        init {
            query = MongoQueryFilter()
            populateOperators()
        }
    }

    companion object {
        private const val serialVersionUID = 5635939847038496849L
    }

    init {
        this.filter = LinkedHashMap()
    }
}