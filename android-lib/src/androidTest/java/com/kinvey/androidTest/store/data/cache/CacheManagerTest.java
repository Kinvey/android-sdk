package com.kinvey.androidTest.store.data.cache;


import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.kinvey.android.Client;
import com.kinvey.android.cache.RealmCache;
import com.kinvey.android.store.DataStore;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.KinveyException;
import com.kinvey.java.cache.ICacheManager;
import com.kinvey.java.store.StoreType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
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
    public void clearCollectionShouldNotFail() throws IOException, InterruptedException {
        Client client = new Client.Builder(mMockContext).build();
        DataStore dataStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        dataStore.clear();
        client.performLockDown();
    }

}
