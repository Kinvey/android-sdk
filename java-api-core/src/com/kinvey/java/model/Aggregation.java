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

package com.kinvey.java.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.util.ArrayList;

/**
 * This class wraps the response of an Aggregation Request.
 *
 * Access the results through the array called `results`
 *
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

    /**
     * Return a list of result values from the aggregation
     *
     * @param key the key to search for
     * @param value the value of the key to search for
     * @return a list of numbers containing the results for the provided key/value
     */
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


    /**
     * This class represents an individual result of an Aggregation request.
     *
     */
    public static class Result extends GenericJson{
        @Key("_result")
        public Number result;

        public Result(){}
    }


}
//[{"_acl.creator":"5230dea6c5cf276b1200000f","_result":3},{"_acl.creator":"520a7096519e1429230000f5","_result":1},{"_acl.creator":"5238c3a29bb2741a3800015d","_result":3}]
