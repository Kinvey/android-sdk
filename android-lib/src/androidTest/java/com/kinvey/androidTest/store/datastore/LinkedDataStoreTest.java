package com.kinvey.androidTest.store.datastore;


import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.kinvey.android.Client;
import com.kinvey.android.store.LinkedDataStore;

import com.kinvey.androidTest.store.data.network.LinkedPerson;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class LinkedDataStoreTest {

    private Client client;

    @Before
    public void setUp() throws InterruptedException {
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        client = new Client.Builder(mMockContext).build();
    }

    @After
    public void tearDown() {
        client.performLockDown();
        if (client.getKinveyHandlerThread() != null) {
            try {
                client.stopKinveyHandlerThread();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    @Test
    public void testConstructor() {
        LinkedDataStore<LinkedPerson> linkedDataStore = new LinkedDataStore<>(client, LinkedPerson.COLLECTION, LinkedPerson.class, StoreType.NETWORK);
        assertNotNull(linkedDataStore);
    }

}
