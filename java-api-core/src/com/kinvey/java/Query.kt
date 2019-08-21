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

package com.kinvey.java

import com.google.common.base.Preconditions
import com.kinvey.java.query.AbstractQuery
import com.kinvey.java.query.MongoQueryFilter
import com.kinvey.java.query.QueryFilter.QueryFilterBuilder

import java.io.Serializable

/**
 * Query API for creating query requests to NetworkManager store.
 *
 * @author mjsalinger
 * @since 2.0
 */
open class Query
/**
 * Constructor for Query API.  Used to instantiate a query request.
 *
 * @param builder that implements QueryFilter.builder
 */
@JvmOverloads constructor(builder: QueryFilterBuilder = MongoQueryFilter.MongoQueryFilterBuilder()) : AbstractQuery(builder), Serializable {

    private var limit: Int = 0
    private var skip: Int = 0

    val isQueryEmpty: Boolean
        get() = queryFilterMap.size == 0

    // Comparison Operators

    /**
     * Add a filter condition for a specific field being equal to a value
     *
     * @param key Field to filter on
     * @param value Value condition for filter
     * @return Query object
     */
    override fun equals(key: String, value: Any): Query {
        Preconditions.checkNotNull(key)
        builder.equals(key, value)
        return this
    }

    /**
     * Add a filter condition for a specific field being greater than a value
     *
     * @param key Field to filter on
     * @param value Value condition for filter
     * @return Query object
     */
    override fun greaterThan(key: String, value: Any): Query {
        Preconditions.checkNotNull(key)
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.GREATERTHAN), key, value)
        return this
    }

    /**
     * Add a filter condition for a specific field being less than than a value
     *
     * @param key Field to filter on
     * @param value Value condition for filter
     * @return Query object
     */
    override fun lessThan(key: String, value: Any): Query {
        Preconditions.checkNotNull(key)
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.LESSTHAN), key, value)
        return this
    }

    /**
     * Add a filter condition for a specific field being greater than or equal to a value
     *
     * @param key Field to filter on
     * @param value Value condition for filter
     * @return Query object
     */
    override fun greaterThanEqualTo(key: String, value: Any): Query {
        Preconditions.checkNotNull(key)
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.GREATERTHANEQUAL), key, value)
        return this
    }

    /**
     * Add a filter condition for a specific field being less than or equal to a value
     *
     * @param key Field to filter on
     * @param value Value condition for filter
     * @return Query object
     */
    override fun lessThanEqualTo(key: String, value: Any): Query {
        Preconditions.checkNotNull(key)
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.LESSTHANEQUAL), key, value)
        return this
    }

    /**
     * Adds a sort  sort condition to the Query
     *
     * @param field Field to sort on
     * @param order Order to sort values (Ascending/Descending)
     * @return  Query object
     */
    override fun addSort(field: String, order: SortOrder?): Query {
        super.addSort(field, order)
        return this
    }

    /**
     * Sets the raw query string
     *
     * @param queryString
     * @return this
     */
    override fun setQueryString(queryString: String): Query {
        super.setQueryString(queryString)
        return this
    }

    /**
     * Add a filter condition for a specific field being not equal to a value
     *
     * @param key Field to filter on
     * @param value Value condition for filter
     * @return Query object
     */
    override fun notEqual(key: String, value: Any): Query {
        Preconditions.checkNotNull(key)
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.NOTEQUAL), key, value)
        return this
    }

    /**
     * Add a filter condition for a specific field being in an array of values
     *
     * @param key Field to filter on
     * @param value An array of values
     * @return Query object
     */
    override fun `in`(key: String, value: Array<out Any>): Query {
        Preconditions.checkNotNull(key)
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.IN), key, value)
        return this
    }

    /**
     * Add a filter condition for a specific field being not in an array of values
     *
     * @param key Field to filter on
     * @param value An array of values
     * @return Query object
     */
    override fun notIn(key: String, value: Array<out Any>): Query {
        Preconditions.checkNotNull(key)
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.NOTIN), key, value)
        return this
    }

    /**
     * Add a filter condition for a specific field compared to a regular expression
     *
     *
     * A regex must begin with a carat `^`, and case-insensitive queries `/i` are not supported.
     *
     *
     *
     * @param key Field to filter on
     * @param value Value condition for filter
     * @return Query object
     */
    override fun regEx(key: String, value: Any): Query {
        if (value.toString().contains("/i")) {
            throw UnsupportedOperationException("Cannot perform regex which contains an `/i`")
        }
        if (!value.toString().startsWith("^")) {
            throw UnsupportedOperationException("All regex must be anchored, it must begin with a carat `^`")
        }
        Preconditions.checkNotNull(key)
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.REGEX), key, value)
        return this
    }

    /**
     * Add a filter condition for a specific field for strings that start with the given value.
     *
     * @param key Field to filter on
     * @param value  Value condition for filter
     * @return Query object
     */
    override fun startsWith(key: String, value: Any): Query {
        Preconditions.checkNotNull(key)
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.REGEX), key, "^$value")
        return this
    }

    //    /**
    //     * Add a filter condition for a specific field for strings that ends with the given value.
    //     *
    //     * @param key Field to filter on
    //     * @param value  Value condition for filter
    //     * @return Query object
    //     */
    //    @Deprecated
    //    public Query endsWith(String key, Object value) {
    //        Preconditions.checkNotNull(key);
    //        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.REGEX), key, value + "$");
    //        return this;
    //    }
    /**
     * Add a filter condition for a specific field holds an array and contains all the values
     *
     * @param key Field to filter on
     * @param value An array of values Values
     * @return Query object
     */
    override fun all(key: String, value: Array<out Any>): Query {
        Preconditions.checkNotNull(key)
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.ALL), key, value)
        return this
    }

    /**
     * Add a filter for any array that is of the given size
     *
     * @param key Field to filter on
     * @param value The expected size of the array
     * @return Query object
     */
    override fun size(key: String, value: Int): Query {
        Preconditions.checkNotNull(key)
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.SIZE), key, value)
        return this
    }

    // Logical Operators

    /**
     * Joins a second Query filter to the current query object and connects them with a logical AND
     *
     * @param query The query which contains the QueryFilter to be joined
     * @return Query object
     */
    override fun and(query: AbstractQuery): Query {
        Preconditions.checkNotNull(query)
        builder.joinFilter(builder.getOperator(QueryFilterBuilder.Operators.AND), query)
        return this
    }

    /**
     * Joins a second Query filter to the current query object and connects them with a logical OR
     *
     * @param query The query which contains the QueryFilter to be joined
     * @return Query object
     */
    override fun or(query: AbstractQuery): Query {
        Preconditions.checkNotNull(query)
        builder.joinFilter(builder.getOperator(QueryFilterBuilder.Operators.OR), query)
        return this
    }

    /**
     * Negates the current query's comparison operators
     *
     * @return Query object
     */
    override fun not(): Query {
        builder.negateQuery()
        return this
    }

    /**
     * Sets the maximum number of records to return
     *
     * @param limit The maximum number of records to return
     * @return Query
     */
    fun setLimit(limit: Int): Query {

        this.limit = limit
        return this
    }

    /**
     *
     * @return Current limit
     */
    fun getLimit(): Int {
        return this.limit
    }

    /**
     * @return current sort string
     */
    override val sortString: String
        get() {
            val sortStringBuilder = StringBuilder()
            if (sort.size > 0) {
                sortStringBuilder.append("{")
                for (field in sort.keys) {
                    sortStringBuilder.append("\"")
                    sortStringBuilder.append(field)
                    sortStringBuilder.append("\" : ")
                    sortStringBuilder.append(if (sort[field] == SortOrder.ASC) 1 else -1)
                    sortStringBuilder.append(",")
                }
                sortStringBuilder.deleteCharAt(sortStringBuilder.length - 1)
                sortStringBuilder.append("}")
            }
            return sortStringBuilder.toString()
        }
    
    /**
     * Sets the number of records to skip before returning the results (useful for pagination).
     *
     * @return  Query object
     */
    fun setSkip(skip: Int): Query {

        this.skip = skip
        return this
    }

    /**
     * @return Current skip setting
     */
    fun getSkip(): Int {
        return this.skip
    }

    // Geolocation Queries

    /**
     * Used on Geospatial fields to return all points near a given point.
     *
     * @param field The geolocation field to filter on
     * @param lat latitude
     * @param lon longitude
     * @return  Query object
     */

    override fun nearSphere(field: String, lat: Double, lon: Double): Query {
        Preconditions.checkNotNull(field)
        Preconditions.checkArgument(lat >= -90 && lat <= 90, "Lat must be between -90 and 90")
        Preconditions.checkArgument(lon >= -180 && lon <= 180, "Lon must be between -180 and 180")
        return nearSphere(field, lat, lon, -1.0)
    }

    /**
     * Used on Geospatial fields to return all points near a given point.
     *
     * @param field The geolocation field to filter on
     * @param lat latitude
     * @param lon longitude
     * @param maxDistance The maximum distance a geolocation point can be from the given point
     * @return
     */
    override fun nearSphere(field: String, lat: Double, lon: Double, maxDistance: Double): Query {
        Preconditions.checkNotNull(field)
        Preconditions.checkArgument(lat >= -90 && lat <= 90, "Lat must be between -90 and 90")
        Preconditions.checkArgument(lon >= -180 && lon <= 180, "Lon must be between -180 and 180")
        val arrayPoint = DoubleArray(2)
        arrayPoint[0] = lat
        arrayPoint[1] = lon
        builder.addLocationFilter(field, builder.getOperator(QueryFilterBuilder.Operators.NEARSPHERE),
                arrayPoint, maxDistance)
        return this
    }

    override fun withinBox(field: String, pointOneLat: Double, pointOneLon: Double, pointTwoLat: Double,
                           pointTwoLon: Double): AbstractQuery {
        Preconditions.checkNotNull(field)
        Preconditions.checkArgument(pointOneLat >= -90 && pointOneLat <= 90, "Lat must be between -90 and 90")
        Preconditions.checkArgument(pointOneLon >= -180 && pointOneLon <= 180, "Lon must be between -180 and 180")
        Preconditions.checkArgument(pointTwoLat >= -90 && pointTwoLat <= 90, "Lat must be between -90 and 90")
        Preconditions.checkArgument(pointTwoLon >= -180 && pointTwoLon <= 180, "Lon must be between -180 and 180")

        val arrayPoints = Array(2) { DoubleArray(2) }
        arrayPoints[0][0] = pointOneLat
        arrayPoints[0][1] = pointOneLon
        arrayPoints[1][0] = pointTwoLat
        arrayPoints[1][1] = pointTwoLon
        builder.addLocationWhereFilter(field, builder.getOperator(QueryFilterBuilder.Operators.WITHINBOX), arrayPoints)
        return this
    }

    override fun withinPolygon(field: String, pointOneLat: Double, pointOneLon: Double, pointTwoLat: Double,
                               pointTwoLon: Double, pointThreeLat: Double, pointThreeLon: Double,
                               pointFourLat: Double, pointFourLon: Double): AbstractQuery {
        Preconditions.checkNotNull(field)
        Preconditions.checkArgument(pointOneLat >= -90 && pointOneLat <= 90, "Lat must be between -90 and 90")
        Preconditions.checkArgument(pointOneLon >= -180 && pointOneLon <= 180, "Lon must be between -180 and 180")
        Preconditions.checkArgument(pointTwoLat >= -90 && pointTwoLat <= 90, "Lat must be between -90 and 90")
        Preconditions.checkArgument(pointTwoLon >= -180 && pointTwoLon <= 180, "Lon must be between -180 and 180")
        Preconditions.checkArgument(pointThreeLat >= -90 && pointThreeLat <= 90, "Lat must be between -90 and 90")
        Preconditions.checkArgument(pointThreeLon >= -180 && pointThreeLon <= 180, "Lon must be between -180 and 180")
        Preconditions.checkArgument(pointFourLat >= -90 && pointFourLat <= 90, "Lat must be between -90 and 90")
        Preconditions.checkArgument(pointFourLon >= -180 && pointFourLon <= 180, "Lon must be between -180 and 180")

        val arrayPoints = Array(4) { DoubleArray(2) }
        arrayPoints[0][0] = pointOneLat
        arrayPoints[0][1] = pointOneLon
        arrayPoints[1][0] = pointTwoLat
        arrayPoints[1][1] = pointTwoLon
        arrayPoints[2][0] = pointThreeLat
        arrayPoints[2][1] = pointThreeLon
        arrayPoints[3][0] = pointFourLat
        arrayPoints[3][1] = pointFourLon

        builder.addLocationWhereFilter(field, builder.getOperator(QueryFilterBuilder.Operators.WITHINPOLYGON),
                arrayPoints)
        return this
    }

    companion object {
        private const val serialVersionUID = 5635939847038496849L
    }
}
/**
 * Constructor for Query API.  Used to instantiate a query request.
 *
 * defaults to using a Mongo DB Query Filter.
 */
