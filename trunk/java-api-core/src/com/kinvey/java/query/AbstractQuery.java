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
package com.kinvey.java.query;

//import com.google.gson.Gson;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.common.base.Preconditions;

import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.LinkedHashMap;

import com.kinvey.java.query.QueryFilter.QueryFilterBuilder;

/**
 * Abstract class for Query objects.
 *
 * @author mjsalinger
 * @since 2.0
 */
public abstract class AbstractQuery {

    /**
     * Possible sort orders (Ascending and Descending
     */
    public enum SortOrder {
        ASC,
        DESC
    }

    protected QueryFilterBuilder builder;
    protected LinkedHashMap<String,SortOrder> sort;

    /**
     * Constructor, sets the appropriate Filter builder and creates a Sort object
     *
     * @param builder
     */
    public AbstractQuery(QueryFilterBuilder builder) {
        this.builder = builder;
        this.sort = new LinkedHashMap<String, SortOrder>();
    }

    /**
     *
     * @return Query filter as AbstractMap<String, Object>
     */
    public AbstractMap<String,Object> getQueryFilterMap() {
        return builder.getFilterMap();
    }

    /**
     *
     * @return Query filter as JSon
     */
    public String getQueryFilterJson(JsonFactory factory) {
        Preconditions.checkNotNull(factory);
        StringWriter writer = new StringWriter();
        String jsonResult = "";
        try {
            JsonGenerator generator = factory.createJsonGenerator(writer);
            generator.serialize(getQueryFilterMap());
            generator.flush();
            jsonResult = writer.toString();
        } catch (Exception ex) {}

        // TODO:  Put exception here?

        return jsonResult;
    }

    /**
     * Adds a sort  sort condition to the Query
     *
     * @param field Field to sort on
     * @param order Order to sort values (Ascending/Descending)
     * @return  Query object
     */
    public AbstractQuery addSort(String field, SortOrder order) {
        Preconditions.checkNotNull(field);
        if (order == null) {
            order = SortOrder.ASC;
        }
        sort.put(field,order);
        return this;
    }

    // Abstract Methods
    public abstract AbstractQuery equals(String key, Object value);
    public abstract AbstractQuery greaterThan(String key, Object value);
    public abstract AbstractQuery lessThan(String key, Object value);
    public abstract AbstractQuery greaterThanEqualTo(String key, Object value);
    public abstract AbstractQuery lessThanEqualTo(String key, Object value);
    public abstract AbstractQuery notEqual(String key, Object value);
    public abstract AbstractQuery in(String key, Object[] value);
    public abstract AbstractQuery notIn(String key, Object[] value);
    public abstract AbstractQuery regEx(String key, Object value);
    public abstract AbstractQuery startsWith(String key, Object value);
    public abstract AbstractQuery endsWith(String key, Object value);
    public abstract AbstractQuery all(String key, Object[] value);
    public abstract AbstractQuery size(String key, int value);
    public abstract AbstractQuery and(AbstractQuery query);
    public abstract AbstractQuery or(AbstractQuery query);
    public abstract AbstractQuery not();
    public abstract String getSortString();
    public abstract AbstractQuery nearSphere(String field, double lat, double lon);
    public abstract AbstractQuery nearSphere(String field, double lat, double lon, double maxDistance);
    public abstract AbstractQuery withinBox(String field, double pointOneLat, double pointOneLon,
                                            double pointTwoLat, double pointTwoLon);
    public abstract AbstractQuery withinPolygon(String field, double pointOneLat, double pointOneLon,
                                                double pointTwoLat, double pointTwoLon,
                                                double pointThreeLat, double pointThreeLon,
                                                double pointFourLat, double pointFourLon);

}
