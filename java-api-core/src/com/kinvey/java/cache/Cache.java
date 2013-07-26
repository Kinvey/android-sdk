/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java.cache;

/**
 * Interface defining a Cache, which can be used by {@link com.kinvey.java.AppData} to store responses of GET requests
 * locally in memory.
 * <p/>
 * @author edwardf
 * @since 2.0
 */
public interface Cache<String, V> {

    public void put(String key, V value);

    public V get(String key);

}