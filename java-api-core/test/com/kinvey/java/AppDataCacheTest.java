/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import com.kinvey.java.cache.CachePolicy;
import com.kinvey.java.cache.InMemoryLRUCache;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.KinveyMockUnitTest;
import com.kinvey.java.testing.HttpTesting;

import junit.framework.Assert;

import java.io.IOException;

/**
 * @author edwardf
 * @since 2.0
 */
public class AppDataCacheTest extends KinveyMockUnitTest{

    TestCache<String, CachedEntity> cache;

    private CachedEntity ret;


    private static String kinveyUrl(String entityId) {
        return HttpTesting.SIMPLE_URL + "/appdata//myCollection/"+entityId;
    }

    public void testInMemoryCacheMaxSize(){
        AppData<CachedEntity> appData = getGenericAppData(CachedEntity.class);

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

        AppData<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();
        CachedEntity ent = new CachedEntity();
        ent.setTitle("Title");
        ent.setName("Name");
        //put entity in the cache, then get it from the cache
        cache.put(kinveyUrl("Title"), ent);

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

        AppData<CachedEntity> appData = getGenericAppData(CachedEntity.class);

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

        AppData<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();
        CachedEntity ent = new CachedEntity();
        ent.setTitle("Title");
        ent.setName("Name");
        //put entity in the cache, then get it from the cache
        cache.put(kinveyUrl("Title"), ent);

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

        AppData<CachedEntity> appData = getGenericAppData(CachedEntity.class);

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

        AppData<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();
        CachedEntity ent = new CachedEntity();
        ent.setTitle("Title");
        ent.setName("Name");

        cache.put(kinveyUrl("Title"), ent);

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

        AppData<CachedEntity> appData = getGenericAppData(CachedEntity.class);

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

        AppData<CachedEntity> appData = getGenericAppData(CachedEntity.class);

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

        AppData<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();
        CachedEntity ent = new CachedEntity();
        ent.setTitle("Title");
        ent.setName("Name");

        cache.put(kinveyUrl("Title"), ent);

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

        AppData<CachedEntity> appData = getGenericAppData(CachedEntity.class);

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

        AppData<CachedEntity> appData = getGenericAppData(CachedEntity.class);

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

        AppData<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();
        CachedEntity ent = new CachedEntity();
        ent.setTitle("Title");
        ent.setName("Name");

        cache.put(kinveyUrl("Title"), ent);

        appData.setCache(cache, CachePolicy.BOTH);

        CachedEntity ret = null;
        try {

            AppData.GetEntity get = appData.getEntityBlocking(ent.getTitle());
            get.setCallback(new KinveyClientCallback() {

                boolean fired = false;
                @Override
                public void onSuccess(Object result) {
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

        AppData<CachedEntity> appData = getGenericAppData(CachedEntity.class);

        cache = new TestCache<String, CachedEntity>();

        appData.setCache(cache, CachePolicy.BOTH);

        CachedEntity ret = null;
        try {

            AppData.GetEntity get = appData.getEntityBlocking("Title");
            get.setCallback(new KinveyClientCallback() {

                boolean fired = false;
                @Override
                public void onSuccess(Object result) {
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


    private <T> AppData<T> getGenericAppData(Class<? extends Object> myClass) {
        AppData appData = new AppData("myCollection", myClass, mockClient);
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
