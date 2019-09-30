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

import java.util.AbstractMap
import java.util.LinkedHashMap

/**
 * A mechanism to filter resultsets within a query
 */
interface QueryFilter {

    /**
     * Get the current filter
     *
     * @return Current filter
     */
    val filter: LinkedHashMap<String, Any?>

    /**
     * Put a value in the QueryFilter
     *
     * @param key field
     * @param value value
     */
    fun put(key: String, value: Any?)

    /**
     * Put a value in the QueryFilter
     *
     * @param operation operator
     * @param key field
     * @param value value
     */
    fun put(operation: String, key: String, value: Any?)

    /**
     * Put a value in the QueryFilter
     *
     * @param operation operator
     * @param key field
     * @param value value
     */
    fun put(operation: String, key: String, value: Array<Any?>)

    /**
     * Join two filters together with a logical and/or
     *
     * @param operation and/or operator
     * @param newFilter new filter to join to the current filter
     */
    fun joinFilters(operation: String, newFilter: LinkedHashMap<String, Any?>)

    /**
     * Get a value within the current filter based on a specified key
     *
     * @param key
     * @return value
     */
    operator fun get(key: String): Any?

    /**
     * Resets the current filter
     */
    fun reset()

    /**
     * Builder for Query object
     */
    interface QueryFilterBuilder {

        /**
         * Gets the current filter map
         *
         * @return
         */
        val filterMap: AbstractMap<String, Any?>

        /**
         * Possible filter operators
         */
        enum class Operators {
            GREATERTHAN,
            LESSTHAN,
            GREATERTHANEQUAL,
            LESSTHANEQUAL,
            NOTEQUAL,
            IN,
            NOTIN,
            ALL,
            SIZE,
            REGEX,
            OPTIONS,
            AND,
            OR,
            NOT,
            LOCATION,
            NEARSPHERE,
            MAXDISTANCE,
            WITHIN,
            WITHINBOX,
            WITHINPOLYGON
        }

        /**
         * Add a filter to the builder's QueryFilter
         *
         * @param operator
         * @param field
         * @param value
         */
        fun addFilter(operator: String, field: String, value: Any?)

        /**
         * Add a filter to the builder's QueryFilter
         *
         * @param operator
         * @param field
         * @param value
         */
        fun addFilter(operator: String, field: String, value: Array<Any?>?)

        /**
         * Join the specified Query's filter to the current builder's queryfilter
         *
         * @param operator
         * @param newQuery
         */
        fun joinFilter(operator: String, newQuery: AbstractQuery?)

        /**
         * Add a location filter
         *
         * @param field
         * @param operator
         * @param point an array of type double[] containing the latitude/longitude points
         * @param distance
         */
        fun addLocationFilter(field: String, operator: String, point: DoubleArray, distance: Double)

        /**
         * Add a location where filter
         *
         * @param field
         * @param operator
         * @param points a 2D array of type double[][] containing points for geolocation
         */
        fun addLocationWhereFilter(field: String, operator: String, points: Array<DoubleArray>)


        /**
         * Negates the current query filter.
         */
        fun negateQuery()

        /**
         * Adds an equality comparison to the QueryFilter
         *
         * @param field
         * @param value
         */
        fun equals(field: String, value: Any?)

        /**
         * Resets the current filter
         */
        fun resetFilter()

        /**
         * Helper method to get the concrete operator value from the passed in Operator
         *
         * @param operator
         * @return
         */
        fun getOperator(operator: Operators): String
    }
}
