package com.kinvey.androidTest.store.data.cache;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.kinvey.android.Client;
import com.kinvey.android.store.DataStore;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.callback.DefaultKinveyClientCallback;
import com.kinvey.androidTest.callback.DefaultKinveyReadCallback;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.Constants;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.core.KinveyJsonError;
import com.kinvey.java.core.KinveyJsonResponseException;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.BaseDataStore;
import com.kinvey.java.store.QueryCacheItem;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.TEST_USERNAME;
import static com.kinvey.androidTest.TestManager.TEST_USERNAME_2;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TempIdTest {

    private static final String TEMP_ID = "temp_";
    private Client client;
    private TestManager<Person> testManager;
    private DataStore<Person> store;

    @Before
    public void setUp() throws InterruptedException {
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        client = new Client.Builder(mMockContext).build();
        testManager = new TestManager<>();
        testManager.login(USERNAME, PASSWORD, client);
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

    /* check that temp item is created with 'temp_' prefix for local keeping and after pushing, item id is updated */
    @Test
    public void testCreatingPrefixForTempId() throws  InterruptedException {
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        testManager.cleanBackend(store, StoreType.SYNC);
        Person person = new Person(TEST_USERNAME);
        DefaultKinveyClientCallback saveCallback = testManager.save(store, person);
        assertNotNull(saveCallback);
        assertNull(saveCallback.getError());
        assertNotNull(saveCallback.getResult());
        Person savedPerson = saveCallback.getResult();
        assertTrue(savedPerson.getId().startsWith(TEMP_ID));
        DefaultKinveyReadCallback readCallback = testManager.find(store, client.query());
        assertNotNull(readCallback);
        assertNull(readCallback.getError());
        assertNotNull(readCallback.getResult().getResult());
        assertEquals(1, readCallback.getResult().getResult().size());
        Person fromCache = readCallback.getResult().getResult().get(0);
        assertTrue(fromCache.getId().startsWith(TEMP_ID));
        assertEquals(savedPerson.getId(), fromCache.getId());
        testManager.push(store);
        readCallback = testManager.find(store, client.query());
        assertNotNull(readCallback);
        assertNull(readCallback.getError());
        assertNotNull(readCallback.getResult().getResult());
        assertEquals(1, readCallback.getResult().getResult().size());
        fromCache = readCallback.getResult().getResult().get(0);
        assertFalse(fromCache.getId().startsWith(TEMP_ID));
        assertNotEquals(savedPerson.getId(), fromCache.getId());
        assertEquals(0, client.getSyncManager().getCount(Person.COLLECTION));
    }

    /* check that old temp item which was created without 'temp_' prefix for local keeping will be pushed as expect */
    @Test
    public void testBackwardCompatibility() throws  InterruptedException {
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        testManager.cleanBackend(store, StoreType.SYNC);
        Person person = new Person(TEST_USERNAME);
        person.setId(UUID.randomUUID().toString());
        DefaultKinveyClientCallback saveCallback = testManager.save(store, person);
        assertNotNull(saveCallback);
        assertNull(saveCallback.getError());
        assertNotNull(saveCallback.getResult());
        Person savedPerson = saveCallback.getResult();
        assertFalse(savedPerson.getId().startsWith(TEMP_ID));
        assertEquals(person.getId(), savedPerson.getId());
        DefaultKinveyReadCallback readCallback = testManager.find(store, client.query());
        assertNotNull(readCallback);
        assertNull(readCallback.getError());
        assertNotNull(readCallback.getResult().getResult());
        assertEquals(1, readCallback.getResult().getResult().size());
        Person fromCache = readCallback.getResult().getResult().get(0);
        assertFalse(fromCache.getId().startsWith(TEMP_ID));
        assertEquals(person.getId(), fromCache.getId());
        testManager.push(store);
        readCallback = testManager.find(store, client.query());
        assertNotNull(readCallback);
        assertNull(readCallback.getError());
        assertNotNull(readCallback.getResult().getResult());
        assertEquals(1, readCallback.getResult().getResult().size());
        fromCache = readCallback.getResult().getResult().get(0);
        assertFalse(fromCache.getId().startsWith(TEMP_ID));
        assertEquals(person.getId(), fromCache.getId());
        assertEquals(0, client.getSyncManager().getCount(Person.COLLECTION));
    }

    /* check that old temp item which was created without 'temp_' prefix
    and item with 'temp_' prefix  will be pushed booth as expect */
    @Test
    public void testMixItemsWithOldAndNewTempIdPatterns() throws  InterruptedException {
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        testManager.cleanBackend(store, StoreType.SYNC);
        Person person = new Person(TEST_USERNAME);
        person.setId(UUID.randomUUID().toString());
        Person person2 = new Person(TEST_USERNAME_2);
        DefaultKinveyClientCallback saveCallback = testManager.save(store, person);
        assertNotNull(saveCallback);
        assertNull(saveCallback.getError());
        assertNotNull(saveCallback.getResult());
        Person savedPerson = saveCallback.getResult();
        saveCallback = testManager.save(store, person2);
        assertNotNull(saveCallback);
        assertNull(saveCallback.getError());
        assertNotNull(saveCallback.getResult());
        Person savedPerson2 = saveCallback.getResult();
        assertFalse(savedPerson.getId().startsWith(TEMP_ID));
        assertEquals(person.getId(), savedPerson.getId());
        assertTrue(savedPerson2.getId().startsWith(TEMP_ID));
        assertEquals(person2.getId(), savedPerson2.getId());
        DefaultKinveyReadCallback readCallback = testManager.find(store, client.query());
        assertNotNull(readCallback);
        assertNull(readCallback.getError());
        assertNotNull(readCallback.getResult().getResult());
        assertEquals(2, readCallback.getResult().getResult().size());
        assertEquals(2, client.getSyncManager().getCount(Person.COLLECTION));
        readCallback = testManager.find(store, client.query().equals("username", TEST_USERNAME));
        assertNotNull(readCallback);
        assertNull(readCallback.getError());
        assertNotNull(readCallback.getResult().getResult());
        assertEquals(1, readCallback.getResult().getResult().size());
        Person fromCache = readCallback.getResult().getResult().get(0);
        assertFalse(fromCache.getId().startsWith(TEMP_ID));
        assertEquals(person.getId(), fromCache.getId());
        readCallback = testManager.find(store, client.query().equals("username", TEST_USERNAME_2));
        assertNotNull(readCallback);
        assertNull(readCallback.getError());
        assertNotNull(readCallback.getResult().getResult());
        assertEquals(1, readCallback.getResult().getResult().size());
        Person fromCache2 = readCallback.getResult().getResult().get(0);
        assertTrue(fromCache2.getId().startsWith(TEMP_ID));
        assertEquals(person2.getId(), fromCache2.getId());
        testManager.push(store);
        readCallback = testManager.find(store, client.query());
        assertNotNull(readCallback);
        assertNull(readCallback.getError());
        assertNotNull(readCallback.getResult().getResult());
        assertEquals(2, readCallback.getResult().getResult().size());
        assertFalse(readCallback.getResult().getResult().get(0).getId().startsWith(TEMP_ID));
        assertFalse(readCallback.getResult().getResult().get(1).getId().startsWith(TEMP_ID));
        assertEquals(0, client.getSyncManager().getCount(Person.COLLECTION));
    }


}
