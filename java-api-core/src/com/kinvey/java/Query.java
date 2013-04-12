/*
 * Copyright (c) 2013 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.java;

import com.google.common.base.Preconditions;

import com.kinvey.java.query.AbstractQuery;
import com.kinvey.java.query.MongoQueryFilter;
import com.kinvey.java.query.QueryFilter.QueryFilterBuilder;

import java.io.Serializable;

/**
 * Query API for creating query requests to AppData store.
 *
 * @author mjsalinger
 * @since 2.0
 */
public class Query extends AbstractQuery implements Serializable {

    private static final long serialVersionUID = 5635939847038496849L;

    private int limit;
    private int skip;

    /**
     * Constructor for Query API.  Used to instantiate a query request.
     *
     * @param builder that implements QueryFilter.builder
     */
    public Query(QueryFilterBuilder builder) {
        super(builder);
    }


    /**
     * Constructor for Query API.  Used to instantiate a query request.
     *
     * defaults to using a Mongo DB Query Filter.
     */
    public Query(){
        this(new MongoQueryFilter.MongoQueryFilterBuilder());
    }

    // Comparison Operators

    /**
     * Add a filter condition for a specific field being equal to a value
     *
     * @param key Field to filter on
     * @param value Value condition for filter
     * @return Query object
     */
    public Query equals(String key, Object value) {
        Preconditions.checkNotNull(key);
        builder.equals(key, value);
        return this;
    }

    /**
     * Add a filter condition for a specific field being greater than a value
     *
     * @param key Field to filter on
     * @param value Value condition for filter
     * @return Query object
     */
    public Query greaterThan(String key, Object value) {
        Preconditions.checkNotNull(key);
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.GREATERTHAN), key, value);
        return this;
    }

    /**
     * Add a filter condition for a specific field being less than than a value
     *
     * @param key Field to filter on
     * @param value Value condition for filter
     * @return Query object
     */
    public Query lessThan(String key, Object value) {
        Preconditions.checkNotNull(key);
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.LESSTHAN), key, value);
        return this;
    }

    /**
     * Add a filter condition for a specific field being greater than or equal to a value
     *
     * @param key Field to filter on
     * @param value Value condition for filter
     * @return Query object
     */
    public Query greaterThanEqualTo(String key, Object value) {
        Preconditions.checkNotNull(key);
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.GREATERTHANEQUAL), key, value);
        return this;
    }

    /**
     * Add a filter condition for a specific field being less than or equal to a value
     *
     * @param key Field to filter on
     * @param value Value condition for filter
     * @return Query object
     */
    public Query lessThanEqualTo(String key, Object value) {
        Preconditions.checkNotNull(key);
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.LESSTHANEQUAL), key, value);
        return this;
    }

    /**
     * Add a filter condition for a specific field being not equal to a value
     *
     * @param key Field to filter on
     * @param value Value condition for filter
     * @return Query object
     */
    public Query notEqual(String key, Object value) {
        Preconditions.checkNotNull(key);
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.NOTEQUAL), key, value);
        return this;
    }

    /**
     * Add a filter condition for a specific field being in an array of values
     *
     * @param key Field to filter on
     * @param value An array of values
     * @return Query object
     */
    public Query in(String key, Object[] value) {
        Preconditions.checkNotNull(key);
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.IN), key, value);
        return this;
    }

    /**
     * Add a filter condition for a specific field being not in an array of values
     *
     * @param key Field to filter on
     * @param value An array of values
     * @return Query object
     */
    public Query notIn(String key, Object[] value) {
        Preconditions.checkNotNull(key);
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.NOTIN), key, value);
        return this;
    }

    /**
     * Add a filter condition for a specific field compared to a regular expression
     *
     * @param key Field to filter on
     * @param value Value condition for filter
     * @return Query object
     */
    public Query regEx(String key, Object value) {
        Preconditions.checkNotNull(key);
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.REGEX), key, value);
        return this;
    }

    /**
     * Add a filter condition for a specific field for strings that start with the given value.
     *
     * @param key Field to filter on
     * @param value  Value condition for filter
     * @return Query object
     */
    public Query startsWith(String key, Object value) {
        Preconditions.checkNotNull(key);
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.REGEX), key, "^" + value);
        return this;
    }

    /**
     * Add a filter condition for a specific field for strings that ends with the given value.
     *
     * @param key Field to filter on
     * @param value  Value condition for filter
     * @return Query object
     */
    public Query endsWith(String key, Object value) {
        Preconditions.checkNotNull(key);
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.REGEX), key, value + "$");
        return this;
    }
    /**
     * Add a filter condition for a specific field holds an array and containsa ll the values
     *
     * @param key Field to filter on
     * @param value An array of values Values
     * @return Query object
     */
    public Query all(String key, Object[] value) {
        Preconditions.checkNotNull(key);
        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.ALL), key, value);
        return this;
    }

    /**
     * Add a filter for any array that is of the given size
     *
     * @param key Field to filter on
     * @param value The expected size of the array
     * @return Query object
     */
    public Query size(String key, int value) {
        Preconditions.checkNotNull(key);

        builder.addFilter(builder.getOperator(QueryFilterBuilder.Operators.SIZE), key, value);
        return this;
    }

    // Logical Operators

    /**
     * Joins a second Query filter to the current query object and connects them with a logical AND
     *
     * @param query The query which contains the QueryFilter to be joined
     * @return Query object
     */
    public Query and(AbstractQuery query) {
        Preconditions.checkNotNull(query);
        builder.joinFilter(builder.getOperator(QueryFilterBuilder.Operators.AND), query);
        return this;
    }

    /**
     * Joins a second Query filter to the current query object and connects them with a logical OR
     *
     * @param query The query which contains the QueryFilter to be joined
     * @return Query object
     */
    public Query or(AbstractQuery query) {
        Preconditions.checkNotNull(query);
        builder.joinFilter(builder.getOperator(QueryFilterBuilder.Operators.OR), query);
        return this;
    }
    /**
     * Negates the current query's comparison operators
     *
     * @return Query object
     */
    public Query not() {
        builder.negateQuery();
        return this;
    }

    /**
     * Sets the maximum number of records to return
     *
     * @param limit The maximum number of records to return
     * @return Query
     */
    public Query setLimit(int limit) {

        this.limit=limit;
        return this;
    }

    /**
     *
     * @return Current limit
     */
    public int getLimit() {
        return this.limit;
    }

    /**
     * @return current sort string
     */
    public String getSortString() {
        StringBuilder sortStringBuilder = new StringBuilder();
        if (sort.size() > 0) {
            sortStringBuilder.append("{");
            for (String field : sort.keySet()) {
                sortStringBuilder.append("\"");
                sortStringBuilder.append(field);
                sortStringBuilder.append("\" : ");
                sortStringBuilder.append(sort.get(field) == SortOrder.ASC ? 1 : -1);
                sortStringBuilder.append(",");
            }
            sortStringBuilder.deleteCharAt(sortStringBuilder.length()-1);
            sortStringBuilder.append("}");
        }
        return sortStringBuilder.toString();
    }

    /**
     * Sets the number of records to skip before returning the results (useful for pagination).
     *
     * @return  Query object
     */
    public Query setSkip(int skip) {

        this.skip=skip;
        return this;
    }

    /**
     * @return Current skip setting
     */
    public int getSkip() {
        return this.skip;
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

    public Query nearSphere(String field, double lat, double lon) {
        Preconditions.checkNotNull(field);
        Preconditions.checkArgument(lat >= -90 && lat <=90, "Lat must be between -90 and 90");
        Preconditions.checkArgument(lon >= -180 && lon <=180, "Lon must be between -180 and 180");
        return nearSphere(field, lat, lon, -1);
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
    public Query nearSphere(String field, double lat, double lon, double maxDistance) {
        Preconditions.checkNotNull(field);
        Preconditions.checkArgument(lat >= -90 && lat <=90, "Lat must be between -90 and 90");
        Preconditions.checkArgument(lon >= -180 && lon <=180, "Lon must be between -180 and 180");
        double[] arrayPoint = new double[2];
        arrayPoint[0] = lat;
        arrayPoint[1] = lon;
        builder.addLocationFilter(field, builder.getOperator(QueryFilterBuilder.Operators.NEARSPHERE),
                arrayPoint, maxDistance);
        return this;
    }

    @Override
    public AbstractQuery withinBox(String field, double pointOneLat, double pointOneLon, double pointTwoLat,
                                   double pointTwoLon) {
        Preconditions.checkNotNull(field);
        Preconditions.checkArgument(pointOneLat >= -90 && pointOneLat <=90, "Lat must be between -90 and 90");
        Preconditions.checkArgument(pointOneLon >= -180 && pointOneLon <=180, "Lon must be between -180 and 180");
        Preconditions.checkArgument(pointTwoLat >= -90 && pointTwoLat <=90, "Lat must be between -90 and 90");
        Preconditions.checkArgument(pointTwoLon >= -180 && pointTwoLon <=180, "Lon must be between -180 and 180");

        double[][] arrayPoints = new double[2][2];
        arrayPoints[0][0] = pointOneLat;
        arrayPoints[0][1] = pointOneLon;
        arrayPoints[1][0] = pointTwoLat;
        arrayPoints[1][1] = pointTwoLon;
        builder.addLocationWhereFilter(field, builder.getOperator(QueryFilterBuilder.Operators.WITHINBOX),arrayPoints);
        return this;
    }

    @Override
    public AbstractQuery withinPolygon(String field, double pointOneLat, double pointOneLon, double pointTwoLat,
                                       double pointTwoLon, double pointThreeLat, double pointThreeLon,
                                       double pointFourLat, double pointFourLon) {
        Preconditions.checkNotNull(field);
        Preconditions.checkArgument(pointOneLat >= -90 && pointOneLat <=90, "Lat must be between -90 and 90");
        Preconditions.checkArgument(pointOneLon >= -180 && pointOneLon <=180, "Lon must be between -180 and 180");
        Preconditions.checkArgument(pointTwoLat >= -90 && pointTwoLat <=90, "Lat must be between -90 and 90");
        Preconditions.checkArgument(pointTwoLon >= -180 && pointTwoLon <=180, "Lon must be between -180 and 180");
        Preconditions.checkArgument(pointThreeLat >= -90 && pointThreeLat <=90, "Lat must be between -90 and 90");
        Preconditions.checkArgument(pointThreeLon >= -180 && pointThreeLon <=180, "Lon must be between -180 and 180");
        Preconditions.checkArgument(pointFourLat >= -90 && pointFourLat <=90, "Lat must be between -90 and 90");
        Preconditions.checkArgument(pointFourLon >= -180 && pointFourLon <=180, "Lon must be between -180 and 180");

        double[][] arrayPoints = new double[4][2];
        arrayPoints[0][0] = pointOneLat;
        arrayPoints[0][1] = pointOneLon;
        arrayPoints[1][0] = pointTwoLat;
        arrayPoints[1][1] = pointTwoLon;
        arrayPoints[2][0] = pointThreeLat;
        arrayPoints[2][1] = pointThreeLon;
        arrayPoints[3][0] = pointFourLat;
        arrayPoints[3][1] = pointFourLon;

        builder.addLocationWhereFilter(field, builder.getOperator(QueryFilterBuilder.Operators.WITHINPOLYGON),
                arrayPoints);
        return this;
    }
}
