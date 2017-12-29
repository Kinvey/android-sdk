package com.kinvey.androidTest.store;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.store.DataStore;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.callback.CustomKinveyPullCallback;
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
import static com.kinvey.androidTest.TestManager.TEST_USERNAME;
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

    private static final int TEN_ITEMS = 10;

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
        person.setUsername(TEST_USERNAME);
        person.set("CustomField", "CustomValue");
        DefaultKinveyClientCallback callback = testManager.save(store, person);
        assertNotNull(callback);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());
        assertNotNull(callback.getResult().getUsername());
        assertEquals(TEST_USERNAME, callback.getResult().getUsername());

        CustomKinveySyncCallback<Person> syncCallback = testManager.sync(store, client.query());
        assertNotNull(syncCallback);
        assertNotNull(syncCallback.getResult());
        assertNull(syncCallback.getError());
        assertNotNull(syncCallback.getResult().getResult().get(0).getUsername());
        assertEquals(TEST_USERNAME, syncCallback.getResult().getResult().get(0).getUsername());
    }

    @Test
    public void testCreateSync() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        Person person = new Person();
        person.setUsername(TEST_USERNAME);
        person.set("CustomField", "CustomValue");
        Person savedPerson = store.save(person);
        assertNotNull(savedPerson);
        assertEquals(TEST_USERNAME, savedPerson.getUsername());

        store.pushBlocking();
        Person downloadedPerson = store.pullBlocking(client.query()).getResult().get(0);
        assertNotNull(downloadedPerson);
        assertEquals(TEST_USERNAME, downloadedPerson.getUsername());
    }

    @Test
    public void testRead() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        Person person = new Person();
        person.setUsername(TEST_USERNAME);
        person.set("CustomField", "CustomValue");
        Person savedPerson = store.save(person);
        assertNotNull(savedPerson);
        assertEquals(TEST_USERNAME, savedPerson.getUsername());

        store.pushBlocking();
        Person downloadedPerson = store.pullBlocking(client.query()).getResult().get(0);
        assertNotNull(downloadedPerson);
        assertEquals(TEST_USERNAME, downloadedPerson.getUsername());

        List<Person> personList = testManager.find(store, client.query()).getResult();
        Person person1 = personList.get(0);
        assertNotNull(person1);
        assertNotNull(person1.getUsername());
        assertEquals(TEST_USERNAME, person1.getUsername());
    }

    @Test
    public void testReadALotOfItems() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        testManager.createPersons(store, TEN_ITEMS);

        store.pushBlocking();
        List<Person> pulledPersons = store.pullBlocking(client.query()).getResult();
        assertNotNull(pulledPersons);
        assertEquals(TEN_ITEMS, pulledPersons.size());

        Person person;
        for (int i = 0; i < TEN_ITEMS; i++) {
            person = pulledPersons.get(i);
            assertEquals(TEST_USERNAME + i, person.getUsername());
        }

        pulledPersons = store.pullBlocking(client.query()).getResult();
        assertNotNull(pulledPersons);
        assertEquals(TEN_ITEMS, pulledPersons.size());

        for (int i = 0; i < TEN_ITEMS; i++) {
            person = pulledPersons.get(i);
            assertEquals(TEST_USERNAME + i, person.getUsername());
        }

        List<Person> foundPersons = testManager.find(store, client.query()).getResult();
        assertNotNull(foundPersons);
        assertEquals(TEN_ITEMS, foundPersons.size());

        Person person1;
        for (int i = 0; i < TEN_ITEMS; i++) {
            person1 = foundPersons.get(i);
            assertEquals(TEST_USERNAME + i, person1.getUsername());
        }
    }

    @Test
    public void testReadALotOfItemsWithClearCache() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);
        testManager.createPersons(store, TEN_ITEMS);
        store.pushBlocking();

        store.clear();

        List<Person> pulledPersons = store.pullBlocking(client.query()).getResult();
        assertNotNull(pulledPersons);
        assertEquals(TEN_ITEMS, pulledPersons.size());

        Person person;
        for (int i = 0; i < TEN_ITEMS; i++) {
            person = pulledPersons.get(i);
            assertEquals(TEST_USERNAME + i, person.getUsername());
        }

        pulledPersons = store.pullBlocking(client.query()).getResult();
        assertNotNull(pulledPersons);
        assertEquals(TEN_ITEMS, pulledPersons.size());

        for (int i = 0; i < TEN_ITEMS; i++) {
            person = pulledPersons.get(i);
            assertEquals(TEST_USERNAME + i, person.getUsername());
        }

        List<Person> foundPersons = testManager.find(store, client.query()).getResult();
        assertNotNull(foundPersons);
        assertEquals(TEN_ITEMS, foundPersons.size());

        Person person1;
        for (int i = 0; i < TEN_ITEMS; i++) {
            person1 = foundPersons.get(i);
            assertEquals(TEST_USERNAME + i, person1.getUsername());
        }
    }

    @Test
    public void testReadALotOfItemsByQuery() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        testManager.createPersons(store, TEN_ITEMS);

        for (int i = 0; i < TEN_ITEMS; i++) {
            Person person = new Person();
            person.setUsername("DeltaCacheUserNameQuery_" + i);
            person.setAge("30");
            Person savedPerson = store.save(person);
            assertNotNull(savedPerson);
            assertEquals("DeltaCacheUserNameQuery_" + i, savedPerson.getUsername());
        }

        store.pushBlocking();
        List<Person> pulledPersons = store.pullBlocking(client.query().equals("age", "30")).getResult();
        assertNotNull(pulledPersons);
        assertEquals(TEN_ITEMS, pulledPersons.size());

        Person person;
        for (int i = 0; i < TEN_ITEMS; i++) {
            person = pulledPersons.get(i);
            assertEquals("DeltaCacheUserNameQuery_" + i, person.getUsername());
        }

        List<Person> foundPersons = testManager.find(store, client.query().equals("age", "30")).getResult();

        Person person1;
        for (int i = 0; i < TEN_ITEMS; i++) {
            person1 = foundPersons.get(i);
            assertEquals("DeltaCacheUserNameQuery_" + i, person1.getUsername());
        }
    }

    @Test
    public void testReadSync() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        Person person = new Person();
        person.setUsername(TEST_USERNAME);
        person.set("CustomField", "CustomValue");
        Person savedPerson = store.save(person);
        assertNotNull(savedPerson);
        assertEquals(TEST_USERNAME, savedPerson.getUsername());

        store.pushBlocking();
        Person downloadedPerson = store.pullBlocking(client.query()).getResult().get(0);
        assertNotNull(downloadedPerson);
        assertEquals(TEST_USERNAME, downloadedPerson.getUsername());

        List<Person> personList = store.find();
        Person person1 = personList.get(0);
        assertNotNull(person1);
        assertNotNull(person1.getUsername());
        assertEquals(TEST_USERNAME, person1.getUsername());
    }

    @Test
    public void testUpdate() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        Person person = new Person();
        person.setUsername(TEST_USERNAME);
        person.set("CustomField", "CustomValue");
        DefaultKinveyClientCallback callback = testManager.save(store, person);
        assertNotNull(callback);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());
        assertNotNull(callback.getResult().getUsername());
        assertEquals(TEST_USERNAME, callback.getResult().getUsername());

        CustomKinveySyncCallback<Person> syncCallback = testManager.sync(store, client.query());
        assertNotNull(syncCallback);
        assertNotNull(syncCallback.getResult());
        assertNull(syncCallback.getError());
        assertNotNull(syncCallback.getResult().getResult().get(0).getUsername());
        assertEquals(TEST_USERNAME, syncCallback.getResult().getResult().get(0).getUsername());

        List<Person> personList = testManager.find(store, client.query()).getResult();
        Person changedPerson = personList.get(0);
        assertNotNull(changedPerson);
        assertNotNull(changedPerson.getUsername());
        assertEquals(TEST_USERNAME, changedPerson.getUsername());

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
        person.setUsername(TEST_USERNAME);
        person.set("CustomField", "CustomValue");
        Person savedPerson = store.save(person);
        assertNotNull(savedPerson);
        assertEquals(TEST_USERNAME, savedPerson.getUsername());

        store.pushBlocking();
        Person downloadedPerson = store.pullBlocking(client.query()).getResult().get(0);
        assertNotNull(downloadedPerson);
        assertEquals(TEST_USERNAME, downloadedPerson.getUsername());

        List<Person> personList = store.find();
        Person changedPerson = personList.get(0);
        assertNotNull(changedPerson);
        assertNotNull(changedPerson.getUsername());
        assertEquals(TEST_USERNAME, changedPerson.getUsername());

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
        person.setUsername(TEST_USERNAME);
        person.set("CustomField", "CustomValue");
        DefaultKinveyClientCallback callback = testManager.save(store, person);
        assertNotNull(callback);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());
        assertNotNull(callback.getResult().getUsername());
        assertEquals(TEST_USERNAME, callback.getResult().getUsername());

        CustomKinveySyncCallback<Person> syncCallback = testManager.sync(store, client.query());
        assertNotNull(syncCallback);
        assertNotNull(syncCallback.getResult());
        assertNull(syncCallback.getError());
        assertNotNull(syncCallback.getResult().getResult().get(0).getUsername());
        assertEquals(TEST_USERNAME, syncCallback.getResult().getResult().get(0).getUsername());

        List<Person> personList = testManager.find(store, client.query()).getResult();
        Person foundPerson = personList.get(0);
        assertNotNull(foundPerson);
        assertNotNull(foundPerson.getUsername());
        assertEquals(TEST_USERNAME, foundPerson.getUsername());

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
        person.setUsername(TEST_USERNAME);
        person.set("CustomField", "CustomValue");
        Person savedPerson = store.save(person);
        assertNotNull(savedPerson);
        assertEquals(TEST_USERNAME, savedPerson.getUsername());

        store.pushBlocking();
        Person downloadedPerson = store.pullBlocking(client.query()).getResult().get(0);
        assertNotNull(downloadedPerson);
        assertEquals(TEST_USERNAME, downloadedPerson.getUsername());

        List<Person> personList = store.find();
        Person foundPerson = personList.get(0);
        assertNotNull(foundPerson);
        assertNotNull(foundPerson.getUsername());
        assertEquals(TEST_USERNAME, foundPerson.getUsername());

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
    public void testDeleteWithTwoStorageTypes() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);

        DataStore<Person> networkStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        assertTrue(networkStore.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        testManager.createPersons(store, TEN_ITEMS);
        testManager.sync(store, client.query());

        CustomKinveyPullCallback<Person> pullCallback = testManager.pullCustom(store, client.query());
        assertNotNull(pullCallback);
        assertNotNull(pullCallback.getResult());
        assertNull(pullCallback.getError());
        assertEquals(TEN_ITEMS, pullCallback.getResult().getResult().size());

        DefaultKinveyDeleteCallback deleteCallback = testManager.delete(networkStore, client.query().equals("username", TEST_USERNAME + 0));
        assertNotNull(deleteCallback);
        assertNotNull(deleteCallback.getResult());
        assertNull(deleteCallback.getError());
        assertEquals(1, deleteCallback.getResult().intValue());

        pullCallback = testManager.pullCustom(store, client.query());
        assertNotNull(pullCallback);
        assertNotNull(pullCallback.getResult());
        assertNull(pullCallback.getError());
        assertEquals(TEN_ITEMS - 1, pullCallback.getResult().getResult().size());

        List<Person> personList = testManager.find(store, client.query()).getResult();
        assertEquals(TEN_ITEMS - 1, personList.size());
    }

    @Test
    public void testSaveWithTwoStorageTypes() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);

        DataStore<Person> networkStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        assertTrue(networkStore.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        testManager.createPersons(store, TEN_ITEMS);
        testManager.sync(store, client.query());

        CustomKinveyPullCallback<Person> pullCallback = testManager.pullCustom(store, client.query());
        assertNotNull(pullCallback);
        assertNotNull(pullCallback.getResult());
        assertNull(pullCallback.getError());
        assertEquals(TEN_ITEMS, pullCallback.getResult().getResult().size());

        DefaultKinveyClientCallback saveCallback = testManager.save(networkStore, new Person(TEST_USERNAME + TEN_ITEMS));
        assertNotNull(saveCallback);
        assertNotNull(saveCallback.getResult());
        assertNull(saveCallback.getError());
        assertEquals(TEST_USERNAME + TEN_ITEMS, saveCallback.getResult().getUsername());

        pullCallback = testManager.pullCustom(store, client.query());
        assertNotNull(pullCallback);
        assertNotNull(pullCallback.getResult());
        assertNull(pullCallback.getError());
        assertEquals(TEN_ITEMS + 1, pullCallback.getResult().getResult().size());

        List<Person> personList = testManager.find(store, client.query()).getResult();
        assertEquals(TEN_ITEMS + 1, personList.size());
    }

    @Test
    public void testUpdateWithTwoStorageTypes() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);

        DataStore<Person> networkStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        assertTrue(networkStore.isDeltaSetCachingEnabled());
        testManager.cleanBackend(store, StoreType.SYNC);

        testManager.createPersons(store, TEN_ITEMS);
        testManager.sync(store, client.query());

        Person personForUpdate = testManager.find(networkStore, client.query().equals("username", TEST_USERNAME + 0)).getResult().get(0);
        personForUpdate.setUsername(TEST_USERNAME + 100);
        DefaultKinveyClientCallback saveCallback = testManager.save(networkStore, personForUpdate);
        assertNotNull(saveCallback);
        assertNotNull(saveCallback.getResult());
        assertNull(saveCallback.getError());
        assertEquals(TEST_USERNAME + 100, saveCallback.getResult().getUsername());

        CustomKinveyPullCallback<Person> pullCallback = testManager.pullCustom(store, client.query());
        assertNotNull(pullCallback);
        assertNotNull(pullCallback.getResult());
        assertNull(pullCallback.getError());
        assertEquals(TEN_ITEMS, pullCallback.getResult().getResult().size());
        assertEquals(TEST_USERNAME + 100, pullCallback.getResult().getResult().get(0).getUsername());

        List<Person> personList = testManager.find(store, client.query().equals("username", TEST_USERNAME + 100)).getResult();
        assertEquals(1, personList.size());
        assertEquals(TEST_USERNAME + 100, personList.get(0).getUsername());
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
