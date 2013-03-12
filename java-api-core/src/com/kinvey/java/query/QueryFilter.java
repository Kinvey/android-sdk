package com.kinvey.java.query;/*
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

import java.util.AbstractMap;
import java.util.LinkedHashMap;

/**
 * A mechanism to filter resultsets within a query
 */
public interface QueryFilter {

    /**
     * Put a value in the QueryFilter
     *
     * @param key field
     * @param value value
     */
    public void put(String key, Object value);

    /**
     * Put a value in the QueryFilter
     *
     * @param operation operator
     * @param key field
     * @param value value
     */
    public void put(String operation, String key, Object value);

    /**
     * Put a value in the QueryFilter
     *
     * @param operation operator
     * @param key field
     * @param value value
     */
    public void put(String operation, String key, Object[] value);

    /**
     * Join two filters together with a logical and/or
     *
     * @param operation and/or operator
     * @param newFilter new filter to join to the current filter
     */
    public void joinFilters(String operation, LinkedHashMap<String, Object> newFilter);

    /**
     * Get a value within the current filter based on a specified key
     *
     * @param key
     * @return value
     */
    public Object get(String key);

    /**
     * Get the current filter
     *
     * @return Current filter
     */
    public LinkedHashMap<String,Object> getFilter();

    /**
     * Resets the current filter
     */
    public void reset();

    /**
     * Builder for Query object
     */
    public interface QueryFilterBuilder {
        /**
         * Possible filter operators
         */
        public static enum Operators {
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
        public void addFilter(String operator, String field, Object value);

        /**
         * Add a filter to the builder's QueryFilter
         *
         * @param operator
         * @param field
         * @param value
         */
        public void addFilter(String operator, String field, Object[] value);

        /**
         * Join the specified Query's filter to the current builder's queryfilter
         *
         * @param operator
         * @param newQuery
         */
        public void joinFilter(String operator, AbstractQuery newQuery);

        /**
         * Add a location filter
         *
         * @param field
         * @param operator
         * @param point an array of type double[] containing the latitude/longitude points
         * @param distance
         */
        public void addLocationFilter(String field, String operator, double[] point, double distance);

        /**
         * Add a location where filter
         *
         * @param field
         * @param operator
         * @param points a 2D array of type double[][] containing points for geolocation
         */
        public void addLocationWhereFilter(String field, String operator, double[][] points);


        /**
         * Negates the current query filter.
         */
        public void negateQuery();

        /**
         * Adds an equality comparison to the QueryFilter
         *
         * @param field
         * @param value
         */
        public void equals(String field, Object value);

        /**
         * Resets the current filter
         */
        public void resetFilter();

        /**
         * Helper method to getBlocking the concrete operator value from the passed in Operator
         *
         * @param operator
         * @return
         */
        public String getOperator(Operators operator);

        /**
         * Gets the current filter map
         *
         * @return
         */
        public AbstractMap<String, Object> getFilterMap();
    }
}
