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

package com.kinvey.java.cache

import com.google.api.client.json.GenericJson
import com.kinvey.java.Query
import com.kinvey.java.model.AggregateType
import com.kinvey.java.model.Aggregation

import java.util.ArrayList

/**
 * Created by Prots on 1/26/16.
 */
interface ICache<T : GenericJson?> {


    /**
     * gets the first item from the cache
     * @return first item
     */
    val first: T?

    /**
     * get current ttl value
     * @return current ttl value
     */
    /**
     * sets current ttl value
     * @param ttl ttl value to be set in milliseconds
     */
    open var ttl: Long

    /**
     * Query cache for objects matching query
     * @param query
     * @return
     */
    operator fun get(query: Query?): List<T>    //run query

    /**
     * Query cache for objects with given ids
     * @param ids
     * @return
     */
    operator fun get(ids: Iterable<String>): List<T>     //get

    /**
     * Get Single object from cache with given id
     * @return Object
     */
    operator fun get(id: String): T?     //get all

    /**
     * Get all objects from cache
     * @return
     */
    fun get(): List<T>     //get all

    /**
     * Saving all given item
     * if item does not have an _id field application will assign it to the object
     * and return all the ids of saved items
     * @param items
     * @return ids of saved items
     */
    fun save(items: Iterable<T>?): List<T>

    /**
     * Saving given item
     * if item does not have an _id field application will assign it to the object
     * and return all the id of saved items
     * @param item
     * @return id of saved items
     */
    fun save(item: T?): T?    //store objects in cache

    /**
     * Delete all object that matches query
     * @param query
     */
    fun delete(query: Query?): Int

    /**
     * Delete all object with given ids
     * @param ids
     */
    fun delete(ids: Iterable<String>): Int

    /**
     * Delete all object with given ids
     * @param id
     */
    fun delete(id: String): Int

    /**
     * Delete all object from cache
     */
    fun clear()

    /**
     * gets the first item from the cache matched query
     * @return first item
     */
    fun getFirst(q: Query): T?

    /**
     * gets the count of matching elements from the cache
     * @return first the count of matching elements from the cache
     */
    fun count(q: Query?): Long

    /**
     * Grouping allows you to collect all entities with the same value for fields,
     * and then apply a reduce function (such as count or average) on all those items.
     * @param aggregateType [AggregateType]
     * @param fields fields for group by
     * @param reduceField field for apply reduce function
     * @param q query to filter results
     * @return the array of groups containing the result of the reduce function
     */
    fun group(aggregateType: AggregateType, fields: ArrayList<String>, reduceField: String?, q: Query): Array<Aggregation.Result>
}
