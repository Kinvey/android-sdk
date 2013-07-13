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
 * This interface is thread-safe. All accessors (get/put) are synchronized on the cache object itself.
 * </p>
 * @author edwardf
 * @since 2.0
 *
 */
public class InMemoryLRUCache<T, V> implements Cache<T, V> {

    protected LinkedHashMap<T, V> mCache;

    //these are defaults declared by Oracle: http://docs.oracle.com/javase/1.4.2/docs/api/java/util/LinkedHashMap.html
    int cacheSize = 16;
    float loadFactor = 0.75f;

    /**Use the default cache size and load factor for a new in-memory cache
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
