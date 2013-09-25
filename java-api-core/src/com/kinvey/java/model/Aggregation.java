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

/**
 * @author edwardf
 */
public class Aggregation {

    public Result[] results;

    public Aggregation(Result[] res){
        if (res == null){
            res = new Result[0];
        }
        this.results = res;
    }

    public ArrayList<Number> getResultsFor(String key, String value){
        ArrayList<Number> ret = new ArrayList<Number>();
        for (Result a : results){
            if (a.containsKey(key)){
                if (a.get(key).equals(value)){
                    ret.add(a.result);
                }
            }
        }
        return ret;
    }









    public static class Result extends GenericJson{
        @Key("_result")
        public Number result;

        public Result(){}
    }


}
//[{"_acl.creator":"5230dea6c5cf276b1200000f","_result":3},{"_acl.creator":"520a7096519e1429230000f5","_result":1},{"_acl.creator":"5238c3a29bb2741a3800015d","_result":3}]
