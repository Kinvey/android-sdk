/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java.query;

import com.google.api.client.util.Key;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of QueryFilter and QueryFilter.builder for Mongo
 *
 * @author mjsalinger
 * @since 2.0
 */
public class MongoQueryFilter implements QueryFilter, Serializable {

    private static final long serialVersionUID = 5635939847038496849L;
    @Key
    private LinkedHashMap<String,Object> queryFilter;

    public MongoQueryFilter() {
        queryFilter = new LinkedHashMap();
    }

    @Override
    public void put(String key, Object value) {
        queryFilter.put(key, value);
    }

    @Override
    public void put(String operation, String key, Object value) {
        LinkedHashMap<String,Object> nestedMap = new LinkedHashMap();
        nestedMap.put(operation,value);

        if (queryFilter.containsKey(key)) {
            Object currentKey = queryFilter.get(key);
            if (currentKey.getClass().equals(String.class)) {
                queryFilter.put(key, nestedMap);
            } else {
                LinkedHashMap<String,Object> currentMap = (LinkedHashMap<String,Object>) currentKey;
                currentMap.put(operation,value);
            }
        } else {
            queryFilter.put(key,nestedMap);
        }
    }

    public void put(String operation, String key, Object[] value) {
        LinkedHashMap<String,Object> nestedMap = new LinkedHashMap();
        nestedMap.put(operation,value);

        if (queryFilter.containsKey(key)) {
            Object currentKey = queryFilter.get(key);
            if (currentKey.getClass().equals(String.class)) {
                queryFilter.put(key, nestedMap);
            } else {
                LinkedHashMap<String,Object> currentMap = (LinkedHashMap<String,Object>) currentKey;
                currentMap.put(operation,value);
            }
        } else {
            queryFilter.put(key,nestedMap);
        }
    }

    @Override
    public void joinFilters(String operation, LinkedHashMap<String,Object> filterToJoin) {
        LinkedHashMap<String,Object> newFilter = new LinkedHashMap();
        LinkedHashMap<String,Object>[] combinedQueries = new LinkedHashMap[]
                {this.queryFilter, filterToJoin};
        newFilter.put(operation, combinedQueries);
        this.queryFilter = newFilter;
    }

    @Override
    public Object get(String key) {
        return queryFilter.get(key);
    }

    @Override
    public void reset() {
        queryFilter = new LinkedHashMap<String, Object>();
    }
    @Override
    public LinkedHashMap<String, Object> getFilter() {
        return this.queryFilter;
    }

    public void setFilter(LinkedHashMap<String, Object> map) {
        this.queryFilter = map;
    }

    public static class MongoQueryFilterBuilder implements QueryFilterBuilder, Serializable {

        private static final long serialVersionUID = 5635939847038496849L;

        private MongoQueryFilter query;
        private HashMap<Operators, String> operatorMap;

        public MongoQueryFilterBuilder() {
            query = new MongoQueryFilter();
            populateOperators();
        }

        public void addFilter(String operator, String key, Object value) {
            query.put(operator,key,value);
        }

        public void addFilter(String operator, String key, Object[] value) {
            query.put(operator, key, value);
        }

        @Override
        public void equals(String key, Object value) {
            query.put(key,value);
        }

        @Override
        public void resetFilter() {

        }

        public AbstractMap<String, Object> getFilterMap() {
            return (AbstractMap<String, Object>) query.queryFilter;
        }

        public void joinFilter(String operator, AbstractQuery newQuery) {
            query.joinFilters(operator, (LinkedHashMap<String, Object>) newQuery.getQueryFilterMap());
        }

        public void addLocationFilter(String field, String operator, double[] point, double distance) {
            LinkedHashMap<String,Object> locationFilter = new LinkedHashMap<String, Object>();
            Double[] mongoPoint = new Double[] {point[1],point[0]};
            locationFilter.put(operator,mongoPoint);
            if(distance > 0) {
                locationFilter.put(getOperator(Operators.MAXDISTANCE),distance);
            }

            query.put(field,locationFilter);
        }

        public void addLocationWhereFilter(String field, String operator, double[][] points) {
            LinkedHashMap<String,Object> locationFilter = new LinkedHashMap<String, Object>();
            LinkedHashMap<String, Object> withinFilter = new LinkedHashMap<String, Object>();
            Double[][] mongoPoints = new Double[points.length][2];
            for (int i=0; i < mongoPoints.length; i++) {
                mongoPoints[i][0] = points[i][1];
                mongoPoints[i][1] = points[i][0];
            }

            locationFilter.put(operator, mongoPoints);
            withinFilter.put(getOperator(Operators.WITHIN), locationFilter);
            query.put(field, withinFilter);
        }

        public void negateQuery() {
            LinkedHashMap<String,Object> currentMap = query.getFilter();
            negate(currentMap);
            query.setFilter(currentMap);
        }

        private Boolean negate(LinkedHashMap<String, Object> map) {

            boolean processParent = false;
            LinkedHashMap<String,Object> newMap = new LinkedHashMap<String, Object>();
            LinkedHashMap<String,Object> tempMap = (LinkedHashMap<String,Object>) map.clone();

            for (Map.Entry<String, Object> entry : tempMap.entrySet()) {
                String key = entry.getKey();
                Object obj = entry.getValue();
                Operators op = getOperatorKey(key);
                if (obj.getClass().equals(LinkedHashMap.class)) {
                    processParent = negate((LinkedHashMap<String,Object>) obj);
                    if (processParent) {
                        LinkedHashMap<String,Object> subMap = (LinkedHashMap<String,Object>) obj;
                        map.put(key, subMap.get("$ne"));
                    }
                }

                if (op == null && obj.getClass().equals(String.class) && !processParent) {
                    LinkedHashMap<String,Object> newNotEqualMap = new LinkedHashMap();
                    newNotEqualMap.put(getOperator(Operators.NOTEQUAL), obj);
                    map.put(key,newNotEqualMap);
                } else if (op != null) {
                    switch(op) {
                        case GREATERTHAN:
                            newMap.put("$lt", obj);
                            break;
                        case LESSTHAN :
                            newMap.put("$gt", obj);
                            break;
                        case GREATERTHANEQUAL:
                            newMap.put("$lte", obj);
                            break;
                        case LESSTHANEQUAL:
                            newMap.put("$gte", obj);
                            break;
                        case IN :
                            newMap.put("$nin", obj);
                            break;
                        case NOTIN :
                            newMap.put("$in", obj);
                            break;
                        case NOTEQUAL :
                            processParent = true;
                            break;
                        default:
                            // If not an operation that is negatable, skip.
                            break;
                    }
                }
            }
            if (!processParent) {
                map.remove("$lt");
                map.remove("$gt");
                map.remove("$gte");
                map.remove("$lte");
                map.remove("$in");
                map.remove("$nin");
                for (Map.Entry<String, Object> entry : newMap.entrySet()) {
                    map.put(entry.getKey(),entry.getValue());
                }
            }
            return processParent;
        }

        // Helper method to populate the Operators Map with the mongo operators
        private void populateOperators() {
            operatorMap = new HashMap();
            operatorMap.put(Operators.GREATERTHAN, "$gt");
            operatorMap.put(Operators.LESSTHAN, "$lt");
            operatorMap.put(Operators.GREATERTHANEQUAL, "$gte");
            operatorMap.put(Operators.LESSTHANEQUAL, "$lte");
            operatorMap.put(Operators.NOTEQUAL, "$ne");
            operatorMap.put(Operators.IN, "$in");
            operatorMap.put(Operators.ALL, "$all");
            operatorMap.put(Operators.NOTIN, "$nin");
            operatorMap.put(Operators.SIZE, "$size");
            operatorMap.put(Operators.REGEX, "$regex");
            operatorMap.put(Operators.OPTIONS, "$options");
            operatorMap.put(Operators.AND, "$and");
            operatorMap.put(Operators.OR, "$or");
            operatorMap.put(Operators.NOT, "$not");
            operatorMap.put(Operators.NEARSPHERE, "$nearSphere");
            operatorMap.put(Operators.MAXDISTANCE, "$maxDistance");
            operatorMap.put(Operators.WITHIN, "$within");
            operatorMap.put(Operators.WITHINBOX, "$box");
            operatorMap.put(Operators.WITHINPOLYGON, "$polygon");
            operatorMap.put(Operators.LOCATION, "$loc");
        }

        public String getOperator(Operators operator) {
            return operatorMap.get(operator);
        }

        public Operators getOperatorKey(String value) {
            for (Operators key : operatorMap.keySet()) {
                if (operatorMap.get(key).equals(value)) {
                    return key;
                }
            }
            return null;
        }
    }
}
