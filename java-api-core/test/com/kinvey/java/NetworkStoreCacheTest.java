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
package com.kinvey.java;

import java.io.IOException;

import junit.framework.Assert;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.gson.Gson;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.KinveyMockUnitTest;
import com.kinvey.java.network.NetworkStore;
import com.kinvey.java.testing.HttpTesting;

/**
 * @author edwardf
 * @since 2.0
 */
public class NetworkStoreCacheTest extends KinveyMockUnitTest{

    TestCache<String, CachedEntity> cache;

    private CachedEntity ret;


    private static String kinveyUrl(String entityId) {
        return HttpTesting.SIMPLE_URL + "/appdata//myCollection/"+entityId;
    }

    public void testInMemoryCacheMaxSize(){
        NetworkStore<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        int cachesize = 5;
        //create cache of size 5
        cache = new TestCache<String, CachedEntity>(cachesize);
        // but try to add ten elements
        for (int i = 0; i < cachesize * 2; i++){
            CachedEntity ent = new CachedEntity();
            String id = "Title"  + i;
            ent.setTitle(id);
            ent.setName("Name" + i);
            cache.put("/appdata//myCollection/" + id, ent);
        }
        //make sure cache still only has 5 elements (not 10)
        Assert.assertEquals(cachesize, cache.getSize());
    }


    public void testCacheOnlyWithEntity(){

        NetworkStore<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();
        CachedEntity ent = new CachedEntity();
        ent.setTitle("Title");
        ent.setName("Name");
        //put entity in the cache, then get it from the cache
        GenericJson req =  new GenericJson();
        req.put("URL", kinveyUrl("Title"));
        cache.put(new Gson().toJson(req), ent);
        
        appData.setCache(cache, CachePolicy.CACHEONLY);

        CachedEntity ret = null;
        try{
            ret = appData.getEntityBlocking("Title").execute();
        }catch(IOException e){
            fail("IOException -> " + e);
        }

        Assert.assertNotNull( "Couldn't get object from cache!", ret);
        Assert.assertEquals("Name", ret.getName());
        Assert.assertEquals("Title", ret.getTitle());
    }

    public void testCacheOnlyWithoutEntity(){

        NetworkStore<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();

        appData.setCache(cache, CachePolicy.CACHEONLY);

        CachedEntity ret = null;
        try{
            ret = appData.getEntityBlocking("Title").execute();
        }catch(IOException e){
            fail("IOException -> " + e);
        }

        Assert.assertNull("The cache should be empty...!", ret);
    }


    public void testCacheFirstNoRefreshWithEntity(){

        NetworkStore<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();
        CachedEntity ent = new CachedEntity();
        ent.setTitle("Title");
        ent.setName("Name");
        //put entity in the cache, then get it from the cache
        GenericJson req =  new GenericJson();
        req.put("URL", kinveyUrl("Title"));
        cache.put(new Gson().toJson(req), ent);

        appData.setCache(cache, CachePolicy.CACHEFIRST_NOREFRESH);

        CachedEntity ret = null;
        try{
            ret = appData.getEntityBlocking("Title").execute();
        }catch(IOException e){
            fail("IOException -> " + e);
        }

        Assert.assertNotNull( "Couldn't get object from cache!", ret);
        Assert.assertEquals("Name", ret.getName());
        Assert.assertEquals("Title", ret.getTitle());
    }

    public void testCacheFirstNoRefreshNoEntity(){

        NetworkStore<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();

        appData.setCache(cache, CachePolicy.CACHEFIRST_NOREFRESH);

        CachedEntity ret = null;
        try{
            ret = appData.getEntityBlocking("Title").execute();
        }catch(IOException e){
            fail("IOException -> " + e);
        }

        Assert.assertNull("the cache should be empty!", ret);

    }


    public void testCacheFirstWithEntity(){

        NetworkStore<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();
        CachedEntity ent = new CachedEntity();
        ent.setTitle("Title");
        ent.setName("Name");

        GenericJson req =  new GenericJson();
        req.put("URL", kinveyUrl("Title"));
        cache.put(new Gson().toJson(req), ent);
        
        appData.setCache(cache, CachePolicy.CACHEFIRST);

        CachedEntity ret = null;
        try{
            ret = appData.getEntityBlocking("Title").execute();
        }catch(IOException e){
            fail("IOException -> " + e);
        }

        Assert.assertNotNull( "Couldn't get object from cache!", ret);
        Assert.assertEquals("Name", ret.getName());
        Assert.assertEquals("Title", ret.getTitle());
    }

    public void testCacheFirstNoEntity(){

        NetworkStore<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();

        appData.setCache(cache, CachePolicy.CACHEFIRST);

        CachedEntity ret = null;
        try{
            ret = appData.getEntityBlocking("Title").execute();
        }catch(IOException e){
            fail("IOException -> " + e);
        }

        Assert.assertNull("cache should be empty!", ret);

    }

    public void testCacheNoCache(){

        NetworkStore<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();
        CachedEntity ent = new CachedEntity();
        ent.setTitle("Title");
        ent.setName("Name");

        cache.put(kinveyUrl("Title"), ent);

        appData.setCache(cache, CachePolicy.NOCACHE);

        CachedEntity ret = null;
        try{
            ret = appData.getEntityBlocking("Title").execute();
        }catch(IOException e){
            fail("IOException -> " + e);
        }

        Assert.assertNull("Shouldn't be able to get cached objact with policy NOCACHE", ret);
    }

    public void testNetworkFirstWithEntity(){

        NetworkStore<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();
        CachedEntity ent = new CachedEntity();
        ent.setTitle("Title");
        ent.setName("Name");

        //cache.put(kinveyUrl("Title"), ent);
        
        GenericJson req =  new GenericJson();
        req.put("URL", kinveyUrl("Title"));
        cache.put(new Gson().toJson(req), ent);

        appData.setCache(cache, CachePolicy.NETWORKFIRST);

        CachedEntity ret = null;
        try{
            ret = appData.getEntityBlocking("Title").execute();
        }catch(IOException e){
            fail("IOException -> " + e);
        }

        Assert.assertNotNull( "Couldn't get object from cache!", ret);
        Assert.assertEquals("Name", ret.getName());
        Assert.assertEquals("Title", ret.getTitle());
    }

    public void testNetworkFirstNoEntity(){

        NetworkStore<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();

        appData.setCache(cache, CachePolicy.NETWORKFIRST);

        CachedEntity ret = null;
        try{
            ret = appData.getEntityBlocking("Title").execute();
        }catch(IOException e){
            fail("IOException -> " + e);
        }

        Assert.assertNull("cache should be empty!", ret);

    }

    public void testBothWithEntityNoCallback(){

        NetworkStore<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();
        CachedEntity ent = new CachedEntity();
        ent.setTitle("Title");
        ent.setName("Name");

        cache.put(kinveyUrl("Title"), ent);

        appData.setCache(cache, CachePolicy.BOTH);

        CachedEntity ret = null;
        try{
            ret = appData.getEntityBlocking("Title").execute();
        }catch(IOException e){
            fail("IOException -> " + e);
        }

        Assert.assertNull("Without callback should be from service, and fail", ret);
    }

    public void testBothWithEntity() {

        NetworkStore<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();
        CachedEntity ent = new CachedEntity();
        ent.setTitle("Title");
        ent.setName("Name");

        cache.put(kinveyUrl("Title"), ent);

        appData.setCache(cache, CachePolicy.BOTH);

        CachedEntity ret = null;
        try {

            NetworkStore<CachedEntity>.GetEntity get = appData.getEntityBlocking(ent.getTitle());
            get.setCallback(new KinveyClientCallback<CachedEntity>() {

                boolean fired = false;
                @Override
                public void onSuccess(CachedEntity result) {
                    if (!fired){
                        Assert.assertNotNull("Couldn't get object from cache!", result);
                    }else{
                        Assert.assertNull("Second callback, from online, should return null!", result);


                    }
                    fired = true;
                }

                @Override
                public void onFailure(Throwable error) {
                    Assert.fail("onFailure?!");
                }
            });

            ret = (CachedEntity) get.execute();
        } catch (IOException e) {
            fail("IOException -> " + e);
        }
    }

    public void testBothNoEntity() {

        NetworkStore<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();

        appData.setCache(cache, CachePolicy.BOTH);

        CachedEntity ret = null;
        try {

            NetworkStore<CachedEntity>.GetEntity get = appData.getEntityBlocking("Title");
            get.setCallback(new KinveyClientCallback<CachedEntity>() {

                boolean fired = false;
                @Override
                public void onSuccess(CachedEntity result) {
                    if (!fired){
                        Assert.assertNull("Cache should be empty!", result);
                    }else{
                        Assert.assertNull("Second callback, from online, should return null!", result);
                    }
                    fired = true;
                }

                @Override
                public void onFailure(Throwable error) {
                    Assert.fail("onFailure?!");
                }
            });

            ret = (CachedEntity) get.execute();
        } catch (IOException e) {
            fail("IOException -> " + e);
        }
    }
    
    public void testCacheWithClientAppVersion(){
    	
    	
    }
    
    public void testCacheWithCustomRequestHeaders(){
    	
    
    }


    public class CachedEntity extends GenericJson {

        @Key("_id")
        private String title;

        @Key("Name")
        private String name;

        public CachedEntity() {}

        public CachedEntity(String title) {
            super();
            this.title = title;
        }

        public CachedEntity(String title, String name) {
            super();
            this.title = title;
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    private <T> NetworkStore<T> getGenericAppData(Class<? extends Object> myClass) {
        NetworkStore appData = new NetworkStore("myCollection", myClass, getClient());
        return appData;
    }

    private class TestCache<String, T> extends InMemoryLRUCache<String, T> {


        public TestCache(){
            super();
        }

        public TestCache(int size){
            super(size);
        }

        public int getSize(){
            return mCache.size();
        }


    }
}
