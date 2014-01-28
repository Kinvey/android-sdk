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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is a very simple implementation of a Least-Recently-Used In-Memory Cache.
 * <p>
 * It utilizes a LinkedHashMaps removeEldestEntry() override to remove older elements.
 * </p>
 *
 * <p>
 * This implementation is thread-safe. All accessors (get/put) are synchronized on the cache object itself.
 * </p>
 * @author edwardf
 * @since 2.0
 *
 */
public class InMemoryLRUCache<T, V> implements Cache<T, V> {

    protected LinkedHashMap<T, V> mCache;

    //these are defaults declared by Oracle: http://docs.oracle.com/javase/6/docs/api/java/util/LinkedHashMap.html
    int cacheSize = 16;
    float loadFactor = 0.75f;

    /**Use the default cache size and load factor for a new in-memory cache.
     * <p/>
     * the default cacheSize is 16 elements, and the default load factor is 0.75
     */
    public InMemoryLRUCache(){
        initCache();
    }

    /**Use a custom cache size but the default load factor for a new in-memory cache.
     *
     * @param cacheSize
     */
    public InMemoryLRUCache(int cacheSize){
        this.cacheSize = cacheSize;
        initCache();

    }

    /**Use a custom cache size and a custom load factor for a new in-memory cache.
     *
     * @param cacheSize
     * @param loadFactor
     */
    public InMemoryLRUCache(int cacheSize, float loadFactor){
        this.cacheSize = cacheSize;
        this.loadFactor = loadFactor;
        initCache();
    }

    private void initCache(){
        mCache = new LinkedHashMap<T, V>(cacheSize+1, loadFactor) {
            public boolean removeEldestEntry(Map.Entry eldest) {
                return size() > cacheSize;
            }
        };
    }


    /**
     * Put a Value in the LRUCache with the associated Key
     *
     * This method is threadsafe through java's synchronization.
     *
     *
     * @param key - the Key of the Value to store
     * @param value - the Value to store
     */
    @Override
    public void put(T key, V value) {
        synchronized (mCache){
            mCache.put(key, value);
        }
    }

    /**
     * Pull a value from the cache with the associated key.
     *
     * This method is threadsafe through java's synchronization.
     *
     * @param key - the Key of the value to retrieve
     * @return
     */
    @Override
    public V get(T key) {
        synchronized (mCache){
            return mCache.get(key);
        }
    }


}
