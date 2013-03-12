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
package com.kinvey.java.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.kinvey.java.Query;
import com.kinvey.java.core.AbstractKinveyJsonClient;

/**
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
                reduce = "function(doc,out){ out._result = Math.minBlocking(out._result, doc."+aggregateField+");}";
                break;
            case MAX:
                initial.put("_result","-Infinity");
                reduce = "function(doc,out){ out._result = Math.maxBlocking(out._result, doc."+aggregateField+");}";
                break;
            case AVERAGE:
                initial.put("_result",0);
                reduce = "function(doc,out){ var countBlocking = (out._kcs_count == undefined) ? 0 : out._kcs_count; " +
                        "out._result =(out._result * countBlocking + doc."+aggregateField+") " +
                        "/ (countBlocking + 1); out._kcs_count = countBlocking+1;}";
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
