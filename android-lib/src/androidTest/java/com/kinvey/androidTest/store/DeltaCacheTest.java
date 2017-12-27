package com.kinvey.androidTest.store;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.store.DataStore;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.callback.CustomKinveySyncCallback;
import com.kinvey.androidTest.callback.DefaultKinveyClientCallback;
import com.kinvey.androidTest.callback.DefaultKinveyDeleteCallback;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by yuliya on 12/27/17.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DeltaCacheTest {

    private Client client;
    private TestManager<Person> testManager;
    private DataStore<Person> store;

    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        testManager = new TestManager<Person>();
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
    public void testCreate() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        Person person = new Person();
        person.setUsername("DeltaCacheUserName");
        person.set("CustomField", "CustomValue");
        DefaultKinveyClientCallback callback = testManager.save(store, person);
        assertNotNull(callback);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());
        assertNotNull(callback.getResult().getUsername());
        assertEquals("DeltaCacheUserName", callback.getResult().getUsername());

        CustomKinveySyncCallback<Person> syncCallback = testManager.sync(store, client.query());
        assertNotNull(syncCallback);
        assertNotNull(syncCallback.getResult());
        assertNull(syncCallback.getError());
        assertNotNull(syncCallback.getResult().getResult().get(0).getUsername());
        assertEquals("DeltaCacheUserName", syncCallback.getResult().getResult().get(0).getUsername());
    }

    @Test
    public void testCreateSync() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        Person person = new Person();
        person.setUsername("DeltaCacheUserName");
        person.set("CustomField", "CustomValue");
        Person savedPerson = store.save(person);
        assertNotNull(savedPerson);
        assertEquals("DeltaCacheUserName", savedPerson.getUsername());

        store.pushBlocking();
        Person downloadedPerson = store.pullBlocking(client.query()).getResult().get(0);
        assertNotNull(downloadedPerson);
        assertEquals("DeltaCacheUserName", downloadedPerson.getUsername());
    }

    @Test
    public void testRead() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        Person person = new Person();
        person.setUsername("DeltaCacheUserName");
        person.set("CustomField", "CustomValue");
        Person savedPerson = store.save(person);
        assertNotNull(savedPerson);
        assertEquals("DeltaCacheUserName", savedPerson.getUsername());

        store.pushBlocking();
        Person downloadedPerson = store.pullBlocking(client.query()).getResult().get(0);
        assertNotNull(downloadedPerson);
        assertEquals("DeltaCacheUserName", downloadedPerson.getUsername());

        List<Person> personList = testManager.find(store, client.query()).getResult();
        Person person1 = personList.get(0);
        assertNotNull(person1);
        assertNotNull(person1.getUsername());
        assertEquals("DeltaCacheUserName", person1.getUsername());
    }

    @Test
    public void testReadSync() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        Person person = new Person();
        person.setUsername("DeltaCacheUserName");
        person.set("CustomField", "CustomValue");
        Person savedPerson = store.save(person);
        assertNotNull(savedPerson);
        assertEquals("DeltaCacheUserName", savedPerson.getUsername());

        store.pushBlocking();
        Person downloadedPerson = store.pullBlocking(client.query()).getResult().get(0);
        assertNotNull(downloadedPerson);
        assertEquals("DeltaCacheUserName", downloadedPerson.getUsername());

        List<Person> personList = store.find();
        Person person1 = personList.get(0);
        assertNotNull(person1);
        assertNotNull(person1.getUsername());
        assertEquals("DeltaCacheUserName", person1.getUsername());
    }

    @Test
    public void testUpdate() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        Person person = new Person();
        person.setUsername("DeltaCacheUserName");
        person.set("CustomField", "CustomValue");
        DefaultKinveyClientCallback callback = testManager.save(store, person);
        assertNotNull(callback);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());
        assertNotNull(callback.getResult().getUsername());
        assertEquals("DeltaCacheUserName", callback.getResult().getUsername());

        CustomKinveySyncCallback<Person> syncCallback = testManager.sync(store, client.query());
        assertNotNull(syncCallback);
        assertNotNull(syncCallback.getResult());
        assertNull(syncCallback.getError());
        assertNotNull(syncCallback.getResult().getResult().get(0).getUsername());
        assertEquals("DeltaCacheUserName", syncCallback.getResult().getResult().get(0).getUsername());

        List<Person> personList = testManager.find(store, client.query()).getResult();
        Person changedPerson = personList.get(0);
        assertNotNull(changedPerson);
        assertNotNull(changedPerson.getUsername());
        assertEquals("DeltaCacheUserName", changedPerson.getUsername());

        changedPerson.setUsername("DeltaCacheUserName_changed");
        testManager.save(store, changedPerson);

        personList = testManager.find(store, client.query()).getResult();
        Person changedPerson1 = personList.get(0);
        assertNotNull(changedPerson1);
        assertNotNull(changedPerson1.getUsername());
        assertEquals("DeltaCacheUserName_changed", changedPerson1.getUsername());

        syncCallback = testManager.sync(store, client.query());
        assertNotNull(syncCallback);
        assertNotNull(syncCallback.getResult());
        assertNull(syncCallback.getError());
        assertNotNull(syncCallback.getResult().getResult().get(0).getUsername());
        assertEquals("DeltaCacheUserName_changed", syncCallback.getResult().getResult().get(0).getUsername());

        personList = testManager.find(store, client.query()).getResult();
        Person person1 = personList.get(0);
        assertNotNull(person1);
        assertNotNull(person1.getUsername());
        assertEquals("DeltaCacheUserName_changed", person1.getUsername());
    }

    @Test
    public void testUpdateSync() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        Person person = new Person();
        person.setUsername("DeltaCacheUserName");
        person.set("CustomField", "CustomValue");
        Person savedPerson = store.save(person);
        assertNotNull(savedPerson);
        assertEquals("DeltaCacheUserName", savedPerson.getUsername());

        store.pushBlocking();
        Person downloadedPerson = store.pullBlocking(client.query()).getResult().get(0);
        assertNotNull(downloadedPerson);
        assertEquals("DeltaCacheUserName", downloadedPerson.getUsername());

        List<Person> personList = store.find();
        Person changedPerson = personList.get(0);
        assertNotNull(changedPerson);
        assertNotNull(changedPerson.getUsername());
        assertEquals("DeltaCacheUserName", changedPerson.getUsername());

        changedPerson.setUsername("DeltaCacheUserName_changed");
        store.save(changedPerson);

        personList = store.find();
        Person changedPerson1 = personList.get(0);
        assertNotNull(changedPerson1);
        assertNotNull(changedPerson1.getUsername());
        assertEquals("DeltaCacheUserName_changed", changedPerson1.getUsername());

        store.pushBlocking();
        downloadedPerson = store.pullBlocking(client.query()).getResult().get(0);
        assertNotNull(downloadedPerson);
        assertEquals("DeltaCacheUserName_changed", downloadedPerson.getUsername());

        personList = store.find();
        changedPerson1 = personList.get(0);
        assertNotNull(changedPerson1);
        assertNotNull(changedPerson1.getUsername());
        assertEquals("DeltaCacheUserName_changed", changedPerson1.getUsername());
    }

    @Test
    public void testDelete() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        Person person = new Person();
        person.setUsername("DeltaCacheUserName");
        person.set("CustomField", "CustomValue");
        DefaultKinveyClientCallback callback = testManager.save(store, person);
        assertNotNull(callback);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());
        assertNotNull(callback.getResult().getUsername());
        assertEquals("DeltaCacheUserName", callback.getResult().getUsername());

        CustomKinveySyncCallback<Person> syncCallback = testManager.sync(store, client.query());
        assertNotNull(syncCallback);
        assertNotNull(syncCallback.getResult());
        assertNull(syncCallback.getError());
        assertNotNull(syncCallback.getResult().getResult().get(0).getUsername());
        assertEquals("DeltaCacheUserName", syncCallback.getResult().getResult().get(0).getUsername());

        List<Person> personList = testManager.find(store, client.query()).getResult();
        Person foundPerson = personList.get(0);
        assertNotNull(foundPerson);
        assertNotNull(foundPerson.getUsername());
        assertEquals("DeltaCacheUserName", foundPerson.getUsername());

        DefaultKinveyDeleteCallback deleteCallback = testManager.delete(store, foundPerson.getId());
        assertNotNull(deleteCallback);
        assertNotNull(deleteCallback.getResult());
        assertNull(deleteCallback.getError());

        personList = testManager.find(store, client.query()).getResult();
        assertEquals(0, personList.size());
        assertEquals(1, store.syncCount());

        syncCallback = testManager.sync(store, client.query());
        assertNotNull(syncCallback);
        assertNotNull(syncCallback.getResult());
        assertNull(syncCallback.getError());
        assertEquals(0, syncCallback.getResult().getResult().size());

        personList = testManager.find(store, client.query()).getResult();
        assertEquals(0, personList.size());
        assertEquals(0, store.count().intValue());
        assertEquals(0, store.syncCount());
    }

    @Test
    public void testDeleteSync() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        Person person = new Person();
        person.setUsername("DeltaCacheUserName");
        person.set("CustomField", "CustomValue");
        Person savedPerson = store.save(person);
        assertNotNull(savedPerson);
        assertEquals("DeltaCacheUserName", savedPerson.getUsername());

        store.pushBlocking();
        Person downloadedPerson = store.pullBlocking(client.query()).getResult().get(0);
        assertNotNull(downloadedPerson);
        assertEquals("DeltaCacheUserName", downloadedPerson.getUsername());

        List<Person> personList = store.find();
        Person foundPerson = personList.get(0);
        assertNotNull(foundPerson);
        assertNotNull(foundPerson.getUsername());
        assertEquals("DeltaCacheUserName", foundPerson.getUsername());

        int deletedItemsCount = store.delete(foundPerson.getId());
        assertEquals(1, deletedItemsCount);

        personList = store.find();
        assertEquals(0, personList.size());
        assertEquals(1, store.syncCount());

        store.pushBlocking();
        List<Person> downloadedPersons = store.pullBlocking(client.query()).getResult();
        assertNotNull(downloadedPersons);
        assertEquals(0, downloadedPersons.size());

        personList = store.find();
        assertEquals(0, personList.size());
        assertEquals(0, store.count().intValue());
        assertEquals(0, store.syncCount());
    }

    @Test
    public void testDefaultDeltaCacheValue() throws InterruptedException, IOException {
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertFalse(store.isDeltaSetCachingEnabled());
    }

    @Test
    public void testChangeDeltaCache() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.setUseDeltaCache(false);
        assertTrue(store.isDeltaSetCachingEnabled());
    }

}
