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

package com.kinvey.java.cache;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.Query;
import com.kinvey.java.model.Aggregation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prots on 1/26/16.
 */
public interface ICache<T extends GenericJson> {
    /**
     * Query cache for objects mathing query
     * @param query
     * @return
     */
    List<T> get(Query query);   //run query

    /**
     * Query cache for objects with given ids
     * @param ids
     * @return
     */
    List<T> get (Iterable<String> ids);    //get

    /**
     * Get Single object from cache with given id
     * @return Object
     */
    T get(String id);    //get all

    /**
     * Get all objects from cache
     * @return
     */
    List<T> get();    //get all

    /**
     * Saving all given item
     * if item does not have an _id field application will assign it to the object
     * and return all the ids of saved items
     * @param items
     * @return ids of saved items
     */
    List<T> save (Iterable<T> items);
    /**
     * Saving given item
     * if item does not have an _id field application will assign it to the object
     * and return all the id of saved items
     * @param item
     * @return id of saved items
     */
    T save (T item);   //store objects in cache
    /**
     * Delete all object that matches query
     * @param query
     */
    int delete(Query query);

    /**
     * Delete all object with given ids
     * @param ids
     */
    int delete(Iterable<String> ids);

    /**
     * Delete all object with given ids
     * @param id
     */
    int delete(String id);

    /**
     * Delete all object from cache
     */
    void clear();


    /**
     * gets the first item from the cache
     * @return first item
     */
    T getFirst();

    /**
     * gets the first item from the cache matched query
     * @return first item
     */
    T getFirst(Query q);

    /**
     * gets the count of matching elements from the cache
     * @return first the count of matching elements from the cache
     */
    long count(Query q);

    /**
     * get current ttl value
     * @return current ttl value
     */
    long getTtl();

    /**
     * sets current ttl value
     * @param ttl ttl value to be set in milliseconds
     */
    void setTtl(long ttl);

    /**
     * Get the sum of elements from the sumField from the cache
     * @param fields  fields for group by
     * @param sumField field for sum
     * @param q query to filter results
     * @return the sum of elements from the sumField from the cache
     */
    List<Aggregation.Result> sum(ArrayList<String> fields, String sumField, Query q);

    /**
     * Get the min of elements from the minField from the cache
     * @param fields  fields for group by
     * @param minField field for searching min value
     * @param q query to filter results
     * @return the min of elements from the minField from the cache
     */
    List<Aggregation.Result> min(ArrayList<String> fields, String minField, Query q);

    /**
     * Get the max of elements from the maxField from the cache
     * @param fields  fields for group by
     * @param maxField field for searching max value
     * @param q query to filter results
     * @return the max of elements from the maxField from the cache
     */
    List<Aggregation.Result> max(ArrayList<String> fields, String maxField, Query q);

    /**
     * Get the average of elements from the averageField from the cache
     * @param fields fields for group by
     * @param averageField field for calculating average value
     * @param q query to filter results
     * @return the average of elements from the averageField from the cache
     */
    List<Aggregation.Result> average(ArrayList<String> fields, String averageField, Query q);

    /**
     * Get the count of elements from the countField from the cache
     * @param fields fields for group by
     * @param q query to filter results
     * @return the count of elements from the countField from the cache
     */
    List<Aggregation.Result> count(ArrayList<String> fields, Query q);
}
