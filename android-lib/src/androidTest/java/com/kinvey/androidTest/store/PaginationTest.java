package com.kinvey.androidTest.store;

import android.content.Context;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.model.User;
import com.kinvey.android.store.DataStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.callback.CustomKinveyPullCallback;
import com.kinvey.androidTest.callback.CustomKinveySyncCallback;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.Constants;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyPullResponse;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.TEST_USERNAME;
import static com.kinvey.androidTest.TestManager.TEST_USERNAME_2;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PaginationTest {

    private Client client;
    private TestManager<Person> testManager;

    @Before
    public void setUp() throws InterruptedException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
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


    @Test
    public void testPagedPull() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        client.getSyncManager().clear(Person.COLLECTION);

        testManager.cleanBackendDataStore(store);

        // Arrange
        Person curly = new Person("Curly");
        Person larry = new Person("Larry");
        Person moe = new Person("Moe");
        testManager.save(store,curly);
        testManager.save(store,larry);
        testManager.save(store,moe);
        long cacheSizeBefore = testManager.getCacheSize(StoreType.CACHE, client);
        assertTrue(cacheSizeBefore == 3);
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.CACHE.ttl).clear();
        long cacheSizeBetween = testManager.getCacheSize(StoreType.CACHE, client);
        assertTrue(cacheSizeBetween == 0);

        // Act
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, null, 2);

        // Assert
        assertNull(pullCallback.getError());
        assertNotNull(pullCallback.getResult());
        assertTrue(pullCallback.getResult().getCount() == 3);
        assertTrue(pullCallback.getResult().getCount() == testManager.getCacheSize(StoreType.CACHE,client));
    }

    @Test
    public void testPagedPullBlocking() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        client.getSyncManager().clear(Person.COLLECTION);

        testManager.cleanBackendDataStore(store);

        // Arrange
        ArrayList<Person> persons = new ArrayList<>();
        Person alvin = new Person("Alvin");
        Person simon = new Person("Simon");
        Person theodore = new Person("Theodore");
        testManager.save(store,alvin);
        testManager.save(store,simon);
        testManager.save(store,theodore);
        long cacheSizeBefore = testManager.getCacheSize(StoreType.CACHE, client);
        assertTrue(cacheSizeBefore == 3);
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.CACHE.ttl).clear();
        long cacheSizeBetween = testManager.getCacheSize(StoreType.CACHE, client);
        assertTrue(cacheSizeBetween == 0);

        // Act
        KinveyPullResponse pullResponse = store.pullBlocking(null, 2);
        assertEquals(3, pullResponse.getCount());
        assertTrue(pullResponse.getCount() == testManager.getCacheSize(StoreType.CACHE, client));
    }


    @Test
    public void testSyncBlockingPaged() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        testManager.cleanBackend(store, StoreType.SYNC);
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = new Person(TEST_USERNAME);
        testManager.save(store,person);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 1);
        store.pushBlocking();
        person = new Person(TEST_USERNAME_2);
        testManager.save(store,person);
        store.syncBlocking(client.query(), 1);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
        assertTrue(testManager.find(store, client.query()).getResult().getResult().size() == 2);
    }

    @Test
    public void testSyncPaged() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        testManager.cleanBackend(store, StoreType.SYNC);
        client.getSyncManager().clear(Person.COLLECTION);
        testManager.createPersons(store, 10);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 10);
        CustomKinveySyncCallback syncCallback = testManager.sync(store, null, 1);
        assertNull(syncCallback.getError());
        assertNotNull(syncCallback.getKinveyPushResponse().getSuccessCount());
        assertEquals(10, syncCallback.getKinveyPushResponse().getSuccessCount());
        assertNotNull(syncCallback.getResult());
        assertEquals(0, syncCallback.getResult().getListOfExceptions().size());
        assertEquals(10, syncCallback.getResult().getCount());
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
        assertEquals(10, store.find().getResult().size());
    }

    @Test
    public void testSyncPagedWithQuery() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        testManager.cleanBackend(store, StoreType.SYNC);
        client.getSyncManager().clear(Person.COLLECTION);
        testManager.createPersons(store, 10);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 10);
        CustomKinveySyncCallback syncCallback = testManager.sync(store, client.query().equals("username", TEST_USERNAME + 0).or(client.query().equals("username", TEST_USERNAME + 1)), 1);
        assertNull(syncCallback.getError());
        assertNotNull(syncCallback.getKinveyPushResponse().getSuccessCount());
        assertEquals(10, syncCallback.getKinveyPushResponse().getSuccessCount());
        assertNotNull(syncCallback.getResult());
        assertEquals(0, syncCallback.getResult().getListOfExceptions().size());
        assertEquals(2, syncCallback.getResult().getCount());
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
        assertEquals(10, store.find().getResult().size());
    }


    @Test
    public void testPagedPullNotCorrectItem() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION_WITH_EXCEPTION, Person.class, StoreType.SYNC, client);
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, null, 2);
        assertTrue(pullCallback.getResult().getListOfExceptions().size() == 1);
        assertTrue(pullCallback.getResult().getCount() == 4);
        testManager.cleanBackend(store, StoreType.SYNC);
    }


    @Test
    public void testPullBlockingWithAutoPagination() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        testManager.cleanBackend(store, StoreType.SYNC);

        testManager.createPersons(store, 5);
        testManager.push(store);
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).clear();

        assertEquals(5, store.pullBlocking(client.query(), true).getCount());
        assertEquals(5, testManager.getCacheSize(StoreType.SYNC, client));
    }

    @Test
    public void testPullBlockingWithoutAutoPagination() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);

        testManager.cleanBackend(store, StoreType.SYNC);
        testManager.createPersons(store, 5);
        testManager.push(store);
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).clear();

        assertEquals(5, store.pullBlocking(client.query(), false).getCount());
        assertEquals(5, testManager.getCacheSize(StoreType.SYNC, client));
    }


}
