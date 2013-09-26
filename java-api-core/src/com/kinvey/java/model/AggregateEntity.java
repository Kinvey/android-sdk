/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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
package com.kinvey.java.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.kinvey.java.Query;
import com.kinvey.java.core.AbstractKinveyJsonClient;

/**
 * This class maintains information about Aggregation Requests
 *
 * @author mjsalinger
 * @since 2.0
 */
public class AggregateEntity extends GenericJson {

    @Key
    private HashMap<String, Boolean> key;
    @Key
    private HashMap<String, Object> initial;
    @Key
    private String reduce;
    @Key
    private LinkedHashMap<String,Object> condition;

    public enum AggregateType {
        COUNT,
        SUM,
        MIN,
        MAX,
        AVERAGE
    }

    public AggregateEntity(ArrayList<String> fields, AggregateType type, String aggregateField, Query query,
                           AbstractKinveyJsonClient client) {


        key = new HashMap<String, Boolean>();
        for (String s : fields) {
            key.put(s, true);
        }
        initial = new HashMap<String, Object>();
        reduce="";
        if (query != null) {
            condition = new LinkedHashMap<String, Object>();
            condition = (LinkedHashMap<String, Object>) query.getQueryFilterMap();
        }

        switch(type) {
            case COUNT:
                initial.put("_result",0);
                reduce = "function(doc,out){ out._result++;}";
                break;
            case SUM:
                initial.put("_result",0);
                reduce = "function(doc,out){ out._result= out._result + doc."+aggregateField+";}";
                break;
            case MIN:
                initial.put("_result","Infinity");
                reduce = "function(doc,out){ out._result = Math.min(out._result, doc."+aggregateField+");}";
                break;
            case MAX:
                initial.put("_result","-Infinity");
                reduce = "function(doc,out){ out._result = Math.max(out._result, doc."+aggregateField+");}";
                break;
            case AVERAGE:
                initial.put("_result",0);
                reduce = "function(doc,out){ var count = (out._kcs_count == undefined) ? 0 : out._kcs_count; " +
                        "out._result =(out._result * count + doc."+aggregateField+") " +
                        "/ (count + 1); out._kcs_count = count+1;}";
                break;
        }

    }

    public HashMap<String, Boolean> getKey() {
        return key;
    }

    public HashMap<String, Object> getInitial() {
        return initial;
    }

    public String getReduce() {
        return reduce;
    }
    public LinkedHashMap<String, Object> getCondition() {
        return condition;
    }

}
