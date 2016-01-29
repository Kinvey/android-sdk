/**
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
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
import com.google.gson.Gson;
import com.kinvey.java.Query;
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
    List<T> get (List<String> ids);    //get

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
    List<String> save (Iterable<T> items);
    /**
     * Saving given item
     * if item does not have an _id field application will assign it to the object
     * and return all the id of saved items
     * @param item
     * @return id of saved items
     */
    String save (T item);   //store objects in cache
    /**
     * Delete all object that matches query
     * @param query
     */
    void delete(Query query);

    /**
     * Delete all object with given ids
     * @param ids
     */
    void delete(Iterable<String> ids);


}
