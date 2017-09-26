package com.kinvey.androidTest.cache;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.Client;
import com.kinvey.android.cache.RealmCacheManager;
import com.kinvey.android.model.User;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.cache.ICacheManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.List;

/**
 * Created by Prots on 1/27/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class CacheTest {
    ICacheManager cacheManager;
    @Before
    public void setup(){
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        cacheManager = new Client.Builder(mMockContext).build().getCacheManager();
        User user = new User();
        user.setId("testId");
        Client.sharedInstance().setActiveUser(user);
    }

    @Test
    public void testSaveSingleInsert(){
        ICache<SampleGsonObject1> cache = cacheManager.getCache("test", SampleGsonObject1.class, Long.MAX_VALUE);

        SampleGsonObject1 obj = new SampleGsonObject1();
        String id = UUID.randomUUID().toString();
        obj._id = id;
        obj.title = "test";
        assertEquals(cache.save(obj)._id, id);

        SampleGsonObject1 ret = cache.get(id);
        //test the same
        assertNotNull(ret);
        assertEquals(ret._id, obj._id);
        assertEquals(ret.title, obj.title);
    }

    @Test
    public void testSaveSingleUpdate(){
        ICache<SampleGsonObject1> cache = cacheManager.getCache("test", SampleGsonObject1.class, Long.MAX_VALUE);
        String id = "test";

        SampleGsonObject1 obj = new SampleGsonObject1();
        obj.put("_id", id);
        obj.put("title", "testTile");

        assertEquals(cache.save(obj)._id, id);

        obj.put("title", "testTitle2");
        assertEquals(cache.save(obj)._id, id);

        SampleGsonObject1 ret = cache.get(id);

        assertNotNull(ret);
        assertEquals(ret.title, "testTitle2");

    }

    @Test
    public void testMultipleInsert(){
        ICache<SampleGsonObject1> cache = cacheManager.getCache("test", SampleGsonObject1.class, Long.MAX_VALUE);
        String id = "test";

        List<SampleGsonObject1> items = new ArrayList<SampleGsonObject1>();

        for (int i = 0 ; i < 100; i++){
            items.add(new SampleGsonObject1(String.valueOf(i), "multipleInsert"+i));
        }

        List<SampleGsonObject1> saved = cache.save(items);

        assertNotNull(saved);

        assertEquals(100, saved.size());
        boolean idsOk = true;
        for (int i = 0 ; i < 100; i++){
            idsOk &= String.valueOf(i).equalsIgnoreCase(saved.get(i).get("_id").toString());
        }
        assertTrue("all ids are right and in right order", idsOk);

        List<String> ids = new ArrayList<String>();
        for (SampleGsonObject1 obj : saved){
            ids.add(obj._id.toString());
        }

        List<SampleGsonObject1> cachedObjects = cache.get(ids);

        assertEquals(100, cachedObjects.size());

    }


    @Test
    public void testDelete(){
        ICache<SampleGsonObject1> cache = cacheManager.getCache("test", SampleGsonObject1.class, Long.MAX_VALUE);

        List<SampleGsonObject1> items = new ArrayList<SampleGsonObject1>();

        List<String> ids = new ArrayList<String>();

        for (int i = 0 ; i < 100; i++){
            items.add(new SampleGsonObject1(String.valueOf(i), "deleteTest"+i));
            ids.add(String.valueOf(i));
        }

        List<SampleGsonObject1> saved = cache.save(items);

        assertNotNull(saved);

        cache.delete(ids);

        List<SampleGsonObject1> cachedObjects = cache.get(ids);

        assertEquals(0, cachedObjects.size());
    }

    @Test
    public void testSaveWithInner(){
        ICache<SampleGsonWithInner> cache = cacheManager.getCache("testInner2", SampleGsonWithInner.class, Long.MAX_VALUE);

        List<SampleGsonWithInner> items = new ArrayList<SampleGsonWithInner>();

        List<String> ids = new ArrayList<String>();

        for (int i = 0 ; i < 100; i++){
            items.add(new SampleGsonWithInner(String.valueOf(i), new SampleGsonObject1(String.valueOf(i), "test"+i)));
            ids.add(String.valueOf(i));
        }

        List<SampleGsonWithInner> saved = cache.save(items);

        assertNotNull(saved);


        List<SampleGsonWithInner> cachedObjects = cache.get(ids);

        assertEquals(100, cachedObjects.size());

        for (int i = 0 ; i < cachedObjects.size() ; i ++){
            SampleGsonWithInner res = cachedObjects.get(i);
            assertEquals(res.getDetails()._id, String.valueOf(i));
        }

    }

    @Test
    public void testSaveWithInnerList(){
        ICache<SampleGsonWithInnerList> cache = cacheManager.getCache("testInnerWithList3", SampleGsonWithInnerList.class, Long.MAX_VALUE);

        List<SampleGsonWithInnerList> items = new ArrayList<SampleGsonWithInnerList>();

        List<String> ids = new ArrayList<String>();

        for (int i = 0 ; i < 100; i++){
            List<SampleGsonObject1> details = new ArrayList<SampleGsonObject1>();
            details.add(new SampleGsonObject1(String.valueOf(i), "test"+i));
            items.add(new SampleGsonWithInnerList(String.valueOf(i), details));
            ids.add(String.valueOf(i));
        }

            List<SampleGsonWithInnerList> saved = cache.save(items);

        assertNotNull(saved);


        List<SampleGsonWithInnerList> cachedObjects = cache.get(ids);

        assertEquals(100, cachedObjects.size());

        for (int i = 0 ; i < cachedObjects.size() ; i ++){
            SampleGsonWithInnerList res = cachedObjects.get(i);
            assertEquals(res.getDetails().get(0)._id, String.valueOf(i));
        }

    }
}
