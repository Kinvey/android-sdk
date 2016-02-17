package com.kinvey.androidTest.cache;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.cache.RealmCache;
import com.kinvey.android.cache.RealmCacheManager;
import com.kinvey.java.KinveyException;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.cache.ICacheManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * Created by Prots on 1/27/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class CacheManagerTest {

    ICacheManager manager;
    Context mMockContext;

    @Before
    public void setUp() {
        mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        Client.Builder builder = new Client.Builder(mMockContext);
        manager = builder.build().getCacheManager();
    }

    @Test
    public void getCacheShouldNotFail(){
        manager.getCache("test", SampleGsonObject1.class, Long.MAX_VALUE);
    }

    @Test
    public void getCacheShouldFailOnRuntimeChanges(){
        manager.getCache("test", SampleGsonObject1.class, Long.MAX_VALUE);
        try{
            manager.getCache("test",SampleGsonObject2.class, Long.MAX_VALUE);
        }catch (KinveyException e){
            return;
        }
        assertTrue("expecting exception to be thrown", false);
    }

    @Test
    public void getCacheShouldNotBeNull(){
        assertNotNull(manager.getCache("test", SampleGsonObject1.class, Long.MAX_VALUE));
    }

    @Test
    public void getCacheShouldBeInstanceOfRealmCache(){
        assertTrue(manager.getCache("test", SampleGsonObject1.class, Long.MAX_VALUE) instanceof RealmCache);
    }

    @Test
     public void getCacheShouldReturnSameInstance(){
        assertEquals(manager.getCache("test", SampleGsonObject1.class, Long.MAX_VALUE),
                manager.getCache("test", SampleGsonObject1.class, Long.MAX_VALUE));
    }
}
