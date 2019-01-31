package com.kinvey.androidTest.store.data.cache;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;


import com.kinvey.android.Client;
import com.kinvey.android.cache.RealmCacheManager;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * Created by Prots on 1/27/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ClientCacheTest {
    Client client;
    @Before
    public void setUp(){
        Client.Builder builder = new Client.Builder(InstrumentationRegistry.getContext());
        client = builder.build();
    }
    @Test
    public void testGetCacheManagerShouldNotFail(){
        client.getCacheManager();
    }

    @Test
    public void testGetCacheManagerShouldReturnNotNull(){
        assertNotNull(client.getCacheManager());
    }

    @Test
    public void testgetCacheManagerShouldRetuntRealmCacheManager(){
        assertTrue("cahe is not realm instance", client.getCacheManager() instanceof RealmCacheManager);
    }

    @After
    public void tearDown() {
        if (client.getKinveyHandlerThread() != null) {
            try {
                client.stopKinveyHandlerThread();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
}
