package com.kinvey.androidTest.store.data.cache;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.cache.RealmCacheManager;
import com.kinvey.android.model.User;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.cache.ICacheManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Prots on 1/27/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class CacheTTLTest {
    ICacheManager cacheManager;

    private static final long TTL_MINUTE = 60 * 1000;
    private static final long TTL_ZERO = 0;


    @Before
    public void setup(){
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        cacheManager = new Client.Builder(mMockContext).build().getCacheManager();
        User user = new User();
        user.setId("testId");
        Client.sharedInstance().setActiveUser(user);
    }

    @Test
    public void testSaveSingleInsertNoTTL(){
        ICache<SampleGsonObject1> cache = cacheManager.getCache("test", SampleGsonObject1.class, TTL_ZERO);

        SampleGsonObject1 obj = new SampleGsonObject1();
        String id = UUID.randomUUID().toString();
        obj._id = id;
        obj.title = "test";
        assertEquals(cache.save(obj)._id, id);

        SampleGsonObject1 ret = cache.get(id);
        //test the same
        assertNull(ret);
    }

    @Test
    public void testSaveSingleInsertTTLOneMinute(){
        ICache<SampleGsonObject1> cache = cacheManager.getCache("test", SampleGsonObject1.class, TTL_MINUTE);

        SampleGsonObject1 obj = new SampleGsonObject1();
        String id = UUID.randomUUID().toString();
        obj._id = id;
        obj.title = "test";
        assertEquals(cache.save(obj).get("_id"), id);

        SampleGsonObject1 ret = cache.get(id);
        //test the same
        assertNotNull(ret);
        assertEquals(ret._id, obj._id);
        assertEquals(ret.title, obj.title);
    }

    @Test
    public void testSaveSingleInsertTTLOneMinuteExpired() throws InterruptedException {
        ICache<SampleGsonObject1> cache = cacheManager.getCache("test", SampleGsonObject1.class, TTL_MINUTE);

        SampleGsonObject1 obj = new SampleGsonObject1();
        String id = UUID.randomUUID().toString();
        obj._id = id;
        obj.title = "test";
        assertEquals(cache.save(obj)._id, id);
        Thread.sleep(TTL_MINUTE);
        SampleGsonObject1 ret = cache.get(id);
        //test the same
        assertNull(ret);
    }
}
