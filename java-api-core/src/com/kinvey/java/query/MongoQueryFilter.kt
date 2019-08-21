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

import java.io.Serializable
import java.util.HashMap
import java.util.LinkedHashMap
import java.util.AbstractMap

/**
 * Implementation of QueryFilter and QueryFilter.builder for Mongo
 *
 * @author mjsalinger
 * @since 2.0
 */
open class MongoQueryFilter : QueryFilter, Serializable {
    @Key
    override var filter = linkedMapOf<String, Any>()

    override fun put(key: String, value: Any) {
        filter[key] = value
    }

    override fun put(operation: String, key: String, value: Any) {
        val nestedMap = LinkedHashMap<String, Any>()
        nestedMap.put(operation, value)

        if (filter.containsKey(key)) {
            val currentKey = filter[key]
            if (currentKey?.javaClass == String::class.java) {
                filter[key] = nestedMap
            } else {
                val currentMap = currentKey as LinkedHashMap<String, Any>
                currentMap[operation] = value
            }
        } else {
            filter[key] = nestedMap
        }
    }

    override fun put(operation: String, key: String, value: Array<Any>) {
        val nestedMap = LinkedHashMap<String, Any>()
        nestedMap.put(operation, value)

        if (filter.containsKey(key)) {
            val currentKey = filter[key]
            if (currentKey?.javaClass == String::class.java) {
                filter[key] = nestedMap
            } else {
                val currentMap = currentKey as LinkedHashMap<String, Any>
                currentMap[operation] = value
            }
        } else {
            filter[key] = nestedMap
        }
    }

    override fun joinFilters(operation: String, filterToJoin: LinkedHashMap<String, Any>) {
        val newFilter = LinkedHashMap<String, Any>()
        val combinedQueries = arrayOf(this.filter, filterToJoin)
        newFilter.put(operation, combinedQueries)
        this.filter = newFilter
    }

    override fun get(key: String): Any {
        return filter[key] as Any
    }

    override fun reset() {
        filter = LinkedHashMap()
    }

    open class MongoQueryFilterBuilder : QueryFilter.QueryFilterBuilder, Serializable {

        private val query: MongoQueryFilter

        private var operatorMap: HashMap<QueryFilter.QueryFilterBuilder.Operators, String>? = null

        override val filterMap: AbstractMap<String, Any>
            get() = query.filter ?: linkedMapOf()

        init {
            query = MongoQueryFilter()
            populateOperators()
        }

        override fun addFilter(operator: String, key: String, value: Any) {
            query.put(operator, key, value)
        }

        override fun addFilter(operator: String, key: String, value: Array<Any>) {
            query.put(operator, key, value)
        }

        override fun equals(key: String, value: Any) {
            query.put(key, value)
        }

        override fun resetFilter() {

        }

        override fun joinFilter(operator: String, newQuery: AbstractQuery) {
            query.joinFilters(operator, newQuery.queryFilterMap as LinkedHashMap<String, Any>)
        }

        override fun addLocationFilter(field: String, operator: String, point: DoubleArray, distance: Double) {
            val locationFilter = LinkedHashMap<String, Any>()
            val mongoPoint = arrayOf(point[1], point[0])
            locationFilter[operator] = mongoPoint
            if (distance > 0) {
                locationFilter[getOperator(QueryFilter.QueryFilterBuilder.Operators.MAXDISTANCE)] = distance
            }
            query.put(field, locationFilter)
        }

        override fun addLocationWhereFilter(field: String, operator: String, points: Array<DoubleArray>) {
            val locationFilter = LinkedHashMap<String, Any>()
            val withinFilter = LinkedHashMap<String, Any>()
            val mongoPoints = Array(points.size) { arrayOf(0.0, 0.0) }
            for (i in mongoPoints.indices) {
                mongoPoints[i][0] = points[i][1]
                mongoPoints[i][1] = points[i][0]
            }
            locationFilter[operator] = mongoPoints
            withinFilter[getOperator(QueryFilter.QueryFilterBuilder.Operators.WITHIN)] = locationFilter
            query.put(field, withinFilter)
        }

        override fun negateQuery() {
            val currentMap = query.filter ?: return
            negate(currentMap)
            query.filter = currentMap
        }

        private fun negate(map: LinkedHashMap<String, Any>): Boolean? {

            var processParent = false
            val newMap = LinkedHashMap<String, LinkedHashMap<String, Any>>()
            val tempMap = map.clone() as LinkedHashMap<String, Any>
            val element = LinkedHashMap<String, Any>()

            for ((key, obj) in tempMap) {
                val op = getOperatorKey(key)
                if (obj.javaClass == LinkedHashMap::class.java) {
                    processParent = negate(obj as LinkedHashMap<String, Any>) ?: false
                    if (processParent) {
                        map[key] = obj["\$ne"] as Any
                    }
                }

                if (op == null && obj !is Map<*, *> && !processParent) {
                    val newNotEqualMap = LinkedHashMap<String, Any>()
                    newNotEqualMap.put(getOperator(QueryFilter.QueryFilterBuilder.Operators.NOTEQUAL), obj)
                    map[key] = newNotEqualMap
                } else if (op != null) {
                    when (op) {
                        QueryFilter.QueryFilterBuilder.Operators.GREATERTHAN -> {
                            element["\$gt"] = obj
                            newMap["\$not"] = element
                        }
                        QueryFilter.QueryFilterBuilder.Operators.LESSTHAN -> {
                            element["\$lt"] = obj
                            newMap["\$not"] = element
                        }
                        QueryFilter.QueryFilterBuilder.Operators.GREATERTHANEQUAL -> {
                            element["\$gte"] = obj
                            newMap["\$not"] = element
                        }
                        QueryFilter.QueryFilterBuilder.Operators.LESSTHANEQUAL -> {
                            element["\$lte"] = obj
                            newMap["\$not"] = element
                        }
                        QueryFilter.QueryFilterBuilder.Operators.IN -> {
                            element["\$in"] = obj
                            newMap["\$not"] = element
                        }
                        QueryFilter.QueryFilterBuilder.Operators.NOTIN -> {
                            element["\$nin"] = obj
                            newMap["\$not"] = element
                        }
                        QueryFilter.QueryFilterBuilder.Operators.NOTEQUAL -> processParent = true
                        else -> {
                        }
                    }// If not an operation that is negatable, skip.
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
            val map = operatorMap ?: return
            map[QueryFilter.QueryFilterBuilder.Operators.GREATERTHAN] = "\$gt"
            map[QueryFilter.QueryFilterBuilder.Operators.LESSTHAN] = "\$lt"
            map[QueryFilter.QueryFilterBuilder.Operators.GREATERTHANEQUAL] = "\$gte"
            map[QueryFilter.QueryFilterBuilder.Operators.LESSTHANEQUAL] = "\$lte"
            map[QueryFilter.QueryFilterBuilder.Operators.NOTEQUAL] = "\$ne"
            map[QueryFilter.QueryFilterBuilder.Operators.IN] = "\$in"
            map[QueryFilter.QueryFilterBuilder.Operators.ALL] = "\$all"
            map[QueryFilter.QueryFilterBuilder.Operators.NOTIN] = "\$nin"
            map[QueryFilter.QueryFilterBuilder.Operators.SIZE] = "\$size"
            map[QueryFilter.QueryFilterBuilder.Operators.REGEX] = "\$regex"
            map[QueryFilter.QueryFilterBuilder.Operators.OPTIONS] = "\$options"
            map[QueryFilter.QueryFilterBuilder.Operators.AND] = "\$and"
            map[QueryFilter.QueryFilterBuilder.Operators.OR] = "\$or"
            map[QueryFilter.QueryFilterBuilder.Operators.NOT] = "\$not"
            map[QueryFilter.QueryFilterBuilder.Operators.NEARSPHERE] = "\$nearSphere"
            map[QueryFilter.QueryFilterBuilder.Operators.MAXDISTANCE] = "\$maxDistance"
            map[QueryFilter.QueryFilterBuilder.Operators.WITHIN] = "\$within"
            map[QueryFilter.QueryFilterBuilder.Operators.WITHINBOX] = "\$box"
            map[QueryFilter.QueryFilterBuilder.Operators.WITHINPOLYGON] = "\$polygon"
            map[QueryFilter.QueryFilterBuilder.Operators.LOCATION] = "\$loc"
        }

        override fun getOperator(operator: QueryFilter.QueryFilterBuilder.Operators): String {
            operatorMap?.let { map -> return map[operator] ?: ""}
            return ""
        }

        fun getOperatorKey(value: String): QueryFilter.QueryFilterBuilder.Operators? {
            operatorMap?.let { map ->
                for (key in map.keys) {
                    if (map[key] == value) {
                        return key
                    }
                }
            }
            return null
        }

        companion object {
            private const val serialVersionUID = 5635939847038496849L
        }
    }

    companion object {
        private const val serialVersionUID = 5635939847038496849L
    }
}
