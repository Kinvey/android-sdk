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
import com.kinvey.androidTest.callback.CustomKinveyReadCallback;
import com.kinvey.androidTest.callback.CustomKinveySyncCallback;
import com.kinvey.androidTest.callback.DefaultKinveyReadCallback;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.Constants;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.model.KinveyDeltaSetCountResponse;
import com.kinvey.java.store.BaseDataStore;
import com.kinvey.java.store.QueryCacheItem;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.TEST_USERNAME;
import static com.kinvey.androidTest.TestManager.TEST_USERNAME_2;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Created by yuliya on 05/28/18.
 */

/* MLIBZ-2470 Test Server-side Delta Set use cases */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class DeltaSetNewTest {

    private Client client;
    private TestManager<Person> testManager;
    private DataStore<Person> store;
    private Query emptyQuery;
    private Query usernameQuery;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() throws InterruptedException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        testManager = new TestManager<>();
        testManager.login(USERNAME, PASSWORD, client);
        emptyQuery = client.query();
        usernameQuery = client.query().equals("username", TEST_USERNAME);
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

    /* The goal of the test is to make sure delta set is not used when not enabled */
    @Test
    public void testDeltaSetIsNotUsed() throws InterruptedException {
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.SYNC, client);
        testManager.cleanBackend(store, StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.push(store);
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
        testManager.save(store, new Person(TEST_USERNAME_2));
        testManager.push(store);
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(2, pullCallback.getResult().getCount());
        assertFalse(store.isDeltaSetCachingEnabled());
    }

    /* The goal of the test is to confirm that empty array will be returned
    if the user has no changes and the since is respected */
    @Test
    public void testEmptyArrayIsReturnedIfNoChanges() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.push(store);
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(0, pullCallback.getResult().getCount());
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
    }

    /* The test aims to confirm that since is respected and changes are handled properly */
    @Test
    public void testNewItemHandling() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.push(store);
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
        testManager.save(store, new Person(TEST_USERNAME_2));
        testManager.push(store);
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
        assertEquals(2, testManager.find(store, emptyQuery).getResult().getResult().size());
    }

    /* The test aims to confirm the correct use of the since param and
    its update in the queryCache table */
    /* with enabled deltaset should return correct number of items when deleting and updating */
    @Test
    public void testPullStoreTypeSync_DeletingUpdating() throws InterruptedException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(networkStore, new Person(TEST_USERNAME));
        testManager.save(networkStore, new Person(TEST_USERNAME_2));
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(2, pullCallback.getResult().getCount());
        deleteItem(networkStore);
        updateItem(networkStore);
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
    }

    /* The test aims to confirm the correct use of deltaset in combination with queries */
    // TODO: 28.5.18 It would be good to add tests for complex and nested queries, to assure they are recorded and used properly in the queryCache table
    @Test
    public void testQueryAfterDeleting() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME_2));
        testManager.push(store);
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, usernameQuery);
        assertEquals(2, pullCallback.getResult().getCount());
        deleteItem(usernameQuery);
        testManager.push(store);
        pullCallback = testManager.pullCustom(store, usernameQuery);
        assertEquals(0, pullCallback.getResult().getCount());
        assertEquals(1, testManager.find(store, usernameQuery).getResult().getResult().size());
    }

    /* The test aims to confirm the correct use of deltaset in combination with queries */
    // TODO: 28.5.18 It would be good to add tests for complex and nested queries, to assure they are recorded and used properly in the queryCache table
    @Test
    public void testQueryAfterUpdating() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME_2));
        testManager.push(store);
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, usernameQuery);
        assertEquals(2, pullCallback.getResult().getCount());
        assertEquals(2, testManager.find(store, usernameQuery).getResult().getResult().size());
        updateItem(usernameQuery);
        testManager.push(store);
        pullCallback = testManager.pullCustom(store, usernameQuery);
        assertEquals(1, pullCallback.getResult().getCount());
        assertEquals(2, testManager.find(store, usernameQuery).getResult().getResult().size());
    }

    /* The test aims to confirm the correct behavior for disabling deltaset */
    @Test
    public void testDisablingDeltaSet() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.push(store);
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(0, pullCallback.getResult().getCount());
        store.setDeltaSetCachingEnabled(false);
        CustomKinveySyncCallback syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
    }

    @Test
    public void testSyncAfterCreating() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        CustomKinveySyncCallback syncCallback  = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
        testManager.save(store, new Person(TEST_USERNAME_2));
        syncCallback  = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        assertEquals(2, testManager.find(store, emptyQuery).getResult().getResult().size());
    }

    @Test
    public void testSyncAfterUpdating() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        CustomKinveySyncCallback syncCallback  = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        updateItem();
        syncCallback  = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
    }

    /* with enabled deltaset should return correct number of items when deleting and updating */
    @Test
    public void testSyncStoreTypeSync_DeletingUpdating() throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.SYNC, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        CustomKinveySyncCallback syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(2, syncCallback.getResult().getCount());
        assertEquals(2, store.count().intValue());
        deleteItem(networkStore);
        updateItem(networkStore);
        syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        assertEquals(1, store.count().intValue());
    }

    @Test
    public void testSyncWithQueryAfterDeleting() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME_2));
        CustomKinveySyncCallback syncCallback  = testManager.sync(store, usernameQuery);
        assertEquals(2, syncCallback.getResult().getCount());
        deleteItem(usernameQuery);
        syncCallback  = testManager.sync(store, usernameQuery);
        assertEquals(0, syncCallback.getResult().getCount());
        assertEquals(1, testManager.find(store, usernameQuery).getResult().getResult().size());
    }

    @Test
    public void testSyncWithQueryAfterUpdating() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME_2));
        CustomKinveySyncCallback syncCallback  = testManager.sync(store, usernameQuery);
        assertEquals(2, syncCallback.getResult().getCount());
        updateItem(usernameQuery);
        syncCallback  = testManager.sync(store, usernameQuery);
        assertEquals(1, syncCallback.getResult().getCount());
    }

    @Test
    public void testSyncAfterDisablingDeltaSet() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.push(store);
        CustomKinveySyncCallback syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(0, syncCallback.getResult().getCount());
        store.setDeltaSetCachingEnabled(false);
        syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
    }

    /* The test aims at confirming that find with forceNetwork works as intended */
    @Test
    public void testForceNetwork() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME_2));
        testManager.push(store);
        store.setStoreType(StoreType.NETWORK);
        List<Person> people = testManager.find(store, emptyQuery).getResult().getResult();
        assertEquals(2, people.size());
        Person person = people.get(0);
        person.setAge("20");
        testManager.save(store, person);
        assertEquals(2, testManager.find(store, emptyQuery).getResult().getResult().size());
        testManager.delete(store, person.getId());
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
    }

    @Test
    public void testForceNetworkWithQuery() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME_2));
        testManager.push(store);
        store.setStoreType(StoreType.NETWORK);
        List<Person> people = testManager.find(store, usernameQuery).getResult().getResult();
        assertEquals(2, people.size());
        Person person = people.get(0);
        person.setAge("20");
        testManager.save(store, person);
        assertEquals(2, testManager.find(store, usernameQuery).getResult().getResult().size());
        testManager.delete(store, person.getId());
        assertEquals(1, testManager.find(store, usernameQuery).getResult().getResult().size());
    }

    @Test
    public void testPullStoreTypeCacheDisabledDeltaSet() throws InterruptedException {
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.CACHE, client);
        testManager.cleanBackend(store, StoreType.CACHE);
        testManager.save(store, new Person(TEST_USERNAME));
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
        testManager.save(store, new Person(TEST_USERNAME_2));
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(2, pullCallback.getResult().getCount());
        assertFalse(store.isDeltaSetCachingEnabled());
    }

    @Test
    public void testPullStoreTypeCache() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        testManager.save(store, new Person(TEST_USERNAME));
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        assertEquals(0, testManager.pullCustom(store, emptyQuery).getResult().getCount());
    }

    @Test
    public void testPullStoreTypeCache_CreatingNewItem() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        testManager.save(store, new Person(TEST_USERNAME));
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        testManager.save(store, new Person(TEST_USERNAME));
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
    }

    @Test
    public void testPullStoreTypeCache_UpdatingItem() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        Person person = testManager.save(store, new Person(TEST_USERNAME)).getResult();
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        person.setAge("20");
        testManager.save(store, person);
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        testManager.delete(store, person.getId());
        assertEquals(0, testManager.pullCustom(store, emptyQuery).getResult().getCount());
    }

    /* with enabled deltaset should return correct number of items when deleting and updating */
    @Test
    public void testPullStoreTypeCache_DeletingUpdating() throws InterruptedException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        initDeltaSetCachedCollection(StoreType.CACHE);
        testManager.save(networkStore, new Person(TEST_USERNAME));
        testManager.save(networkStore, new Person(TEST_USERNAME_2));
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(2, pullCallback.getResult().getCount());
        deleteItem(networkStore);
        updateItem(networkStore);
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
    }

    @Test
    public void testPullStoreTypeCache_DeletingOneOfThreeItems() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        Person person = testManager.save(store, new Person(TEST_USERNAME)).getResult();
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME_2));
        assertEquals(2, testManager.pullCustom(store, usernameQuery).getResult().getCount());
        testManager.delete(store, person.getId());
        assertEquals(0, testManager.pullCustom(store, usernameQuery).getResult().getCount());
    }

    @Test
    public void testPullStoreTypeCache_Update() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        Person person = testManager.save(store, new Person(TEST_USERNAME)).getResult();
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME_2));
        assertEquals(2, testManager.pullCustom(store, usernameQuery).getResult().getCount());
        person.setAge("20");
        testManager.save(store, person);
        assertEquals(1, testManager.pullCustom(store, usernameQuery).getResult().getCount());
    }

    @Test
    public void testPullStoreTypeCache_DisablingDeltaSet() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        testManager.save(store, new Person(TEST_USERNAME));
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        assertEquals(0, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        store.setDeltaSetCachingEnabled(false);
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
    }

    @Test
    public void testPullStoreTypeCache_DoublePull() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        testManager.save(store, new Person(TEST_USERNAME));
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        assertEquals(0, testManager.pullCustom(store, emptyQuery).getResult().getCount());
    }

    @Test
    public void testSyncStoreTypeCache_CreateItem() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        Person person = testManager.save(store, new Person(TEST_USERNAME)).getResult();
        assertEquals(1, testManager.sync(store, emptyQuery).getResult().getCount());
        testManager.save(store, person);
        assertEquals(1, testManager.sync(store, emptyQuery).getResult().getCount());
    }

    @Test
    public void testSyncStoreTypeCache_UpdateItem() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        Person person = testManager.save(store, new Person(TEST_USERNAME)).getResult();
        assertEquals(1, testManager.sync(store, emptyQuery).getResult().getCount());
        person.setAge("20");
        testManager.save(store, person);
        assertEquals(1, testManager.sync(store, emptyQuery).getResult().getCount());
    }

    /* with enabled deltaset should return correct number of items when deleting and updating */
    @Test
    public void testSyncStoreTypeCache_DeletingUpdating() throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.CACHE, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        CustomKinveySyncCallback syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(2, syncCallback.getResult().getCount());
        assertEquals(2, store.count().intValue());
        deleteItem(networkStore);
        updateItem(networkStore);
        syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        assertEquals(1, store.count().intValue());
    }

    @Test
    public void testSyncStoreTypeCache_DeleteItemWithQuery() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        Person person = testManager.save(store, new Person(TEST_USERNAME)).getResult();
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME_2));
        assertEquals(2, testManager.sync(store, usernameQuery).getResult().getCount());
        testManager.delete(store, person.getId());
        assertEquals(0, testManager.sync(store, usernameQuery).getResult().getCount());
    }

    @Test
    public void testSyncStoreTypeCache_UpdateItemWithQuery() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        Person person = testManager.save(store, new Person(TEST_USERNAME)).getResult();
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME_2));
        assertEquals(2, testManager.sync(store, usernameQuery).getResult().getCount());
        person.setAge("20");
        testManager.save(store, person);
        assertEquals(1, testManager.sync(store, usernameQuery).getResult().getCount());
    }

    @Test
    public void testSyncStoreTypeCache_DisablingDeltaSet() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        testManager.save(store, new Person(TEST_USERNAME));
        assertEquals(1, testManager.sync(store, emptyQuery).getResult().getCount());
        assertEquals(0, testManager.sync(store, emptyQuery).getResult().getCount());
        store.setDeltaSetCachingEnabled(false);
        assertEquals(1, testManager.sync(store, emptyQuery).getResult().getCount());
    }

    @Test
    public void testFindStoreTypeCache_CreateItem() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        testManager.save(store, new Person(TEST_USERNAME));
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
        testManager.save(store, new Person(TEST_USERNAME_2));
        assertEquals(2, testManager.find(store, emptyQuery).getResult().getResult().size());
    }

    @Test
    public void testFindStoreTypeCache_UpdateItem() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        Person person = testManager.save(store, new Person(TEST_USERNAME)).getResult();
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
        person.setAge("20");
        testManager.save(store, person);
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
    }

    @Test
    public void testFindStoreTypeCache_DeleteOneOfThreeItems() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        Person person = testManager.save(store, new Person(TEST_USERNAME)).getResult();
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME_2));
        assertEquals(2, testManager.find(store, usernameQuery).getResult().getResult().size());
        testManager.delete(store, person.getId());
        List<Person> people = testManager.find(store, usernameQuery).getResult().getResult();
        assertEquals(1, people.size());
    }

    @Test
    public void testFindStoreTypeCache_UpdateOneOfThreeItems() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        Person person = testManager.save(store, new Person(TEST_USERNAME)).getResult();
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME_2));
        assertEquals(2, testManager.find(store, usernameQuery).getResult().getResult().size());
        person.setAge("20");
        testManager.save(store, person);
        assertEquals(2, testManager.find(store, usernameQuery).getResult().getResult().size());
    }

    @Test
    public void testFindStoreTypeCache_DisablingDeltaSet() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        testManager.save(store, new Person(TEST_USERNAME));
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
        store.setDeltaSetCachingEnabled(false);
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
    }

    @Test
    public void testFindStoreTypeCache_FindById() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        Person person = testManager.save(store, new Person(TEST_USERNAME)).getResult();
        assertEquals(1, testManager.find(store, client.query().equals("_id", person.getId())).getResult().getResult().size());
        assertEquals(1, testManager.find(store, client.query().equals("_id", person.getId())).getResult().getResult().size());
        assertEquals(person.getId(), testManager.find(store, person.getId()).getResult().getId());
    }

    @Test
    public void testFindStoreTypeNetwork() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.NETWORK);
        testManager.save(store, new Person(TEST_USERNAME));
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
    }

    @Test
    public void testChangeStoreTypeSyncToCache() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.push(store);
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        testManager.save(store, new Person(TEST_USERNAME_2));
        testManager.push(store);
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.CACHE, client);
        store.setDeltaSetCachingEnabled(true);
        testManager.save(store, new Person(TEST_USERNAME_2));
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
    }

    @Test
    public void testChangeStoreTypeCacheToSync() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        testManager.save(store, new Person(TEST_USERNAME));
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        testManager.save(store, new Person(TEST_USERNAME_2));
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.SYNC, client);
        store.setDeltaSetCachingEnabled(true);
        testManager.save(store, new Person(TEST_USERNAME_2));
        testManager.push(store);
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
    }

    @Test
    public void testChangeStoreTypeNetworkToSync() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.NETWORK);
        Person firstPerson = testManager.save(store, new Person(TEST_USERNAME)).getResult();
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.SYNC, client);
        store.setDeltaSetCachingEnabled(true);
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        testManager.save(store, new Person(TEST_USERNAME_2));
        testManager.delete(store, firstPerson.getId());
        testManager.push(store);
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
    }

    @Test
    public void testChangeStoreTypeNetworkToCache() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.NETWORK);
        Person firstPerson = testManager.save(store, new Person(TEST_USERNAME)).getResult();
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.CACHE, client);
        store.setDeltaSetCachingEnabled(true);
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        testManager.save(store, new Person(TEST_USERNAME_2));
        testManager.delete(store, firstPerson.getId());
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
    }

    /* The test aims to confirm that second request after clearing the cache would use deltaset */
    @Test
    public void testSecondRequestAfterClearingCacheStoreTypeSync() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.push(store);
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.push(store);
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        store.clear();
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.push(store);
        assertEquals(3, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.push(store);
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
    }

    @Test
    public void testSecondRequestAfterClearingCacheStoreTypeCache() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        testManager.save(store, new Person(TEST_USERNAME));
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        testManager.save(store, new Person(TEST_USERNAME));
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        store.clear();
        testManager.save(store, new Person(TEST_USERNAME));
        assertEquals(3, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        testManager.save(store, new Person(TEST_USERNAME));
        assertEquals(1, testManager.pullCustom(store, emptyQuery).getResult().getCount());
    }


    /* The test aims to confirm that the autopagination is envoked only at the regular GET request when deltaset is on */
    /* with enabled deltaset and autopagination should use AP for first request and DS for the next */
    @Test
    public void testPullAutoPaginationStoreTypeSync() throws InterruptedException, IOException {
        testPullAutoPagination(StoreType.SYNC);
    }

    /* with enabled deltaset and autopagination should use AP for first request and DS for the next */
    @Test
    public void testSyncAutoPaginationStoreTypeSync() throws InterruptedException, IOException {
        testSyncAutoPagination(StoreType.SYNC);
    }

    /* with enabled deltaset and autopagination should use AP for first request and DS for the next */
    @Test
    public void testPullAutoPaginationStoreTypeCache() throws InterruptedException, IOException {
        testPullAutoPagination(StoreType.CACHE);
    }

    /* with enabled deltaset and autopagination should use AP for first request and DS for the next */
    @Test
    public void testSyncAutoPaginationStoreTypeCache() throws InterruptedException, IOException {
        testSyncAutoPagination(StoreType.CACHE);
    }

    private void testPullAutoPagination(StoreType storeType) throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, storeType, client);
        store.setDeltaSetCachingEnabled(true);
        testManager.createPersons(networkStore, 4);
        assertEquals(4, testManager.pullCustom(store, client.query(), 2).getResult().getCount());
        List<Person> people = testManager.find(store, emptyQuery).getResult().getResult();
        assertEquals(4, people.size());
        testManager.save(networkStore, new Person(TEST_USERNAME));
        assertEquals(1, testManager.pullCustom(store, client.query(), 2).getResult().getCount());
        people = testManager.find(store, emptyQuery).getResult().getResult();
        assertEquals(5, people.size());
    }

    private void testSyncAutoPagination(StoreType storeType) throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, storeType, client);
        store.setDeltaSetCachingEnabled(true);
        testManager.createPersons(networkStore, 4);
        assertEquals(4, testManager.sync(store, client.query(), 2).getResult().getCount());
        List<Person> people = testManager.find(store, emptyQuery).getResult().getResult();
        assertEquals(4, people.size());
        testManager.save(networkStore, new Person(TEST_USERNAME));
        assertEquals(1, testManager.sync(store, client.query(), 2).getResult().getCount());
        people = testManager.find(store, emptyQuery).getResult().getResult();
        assertEquals(5, people.size());
    }

    /* should use regular GET when deltaset configuration is missing on the backend */
    @Test
    public void testStoreTypeSyncWithMissedConfigurationAtTheBackend() throws IOException, InterruptedException {
        testMissedConfigurationAtTheBackend(StoreType.SYNC);
    }

    /* should use regular GET when deltaset configuration is missing on the backend */
    @Test
    public void testStoreTypeCacheWithMissedConfigurationAtTheBackend() throws IOException, InterruptedException {
        testMissedConfigurationAtTheBackend(StoreType.CACHE);
    }

    private void testMissedConfigurationAtTheBackend(StoreType storeType) throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_OFF_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_OFF_COLLECTION, Person.class, storeType, client);
        store.setDeltaSetCachingEnabled(true);
        testManager.createPersons(networkStore, 1);
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
        testManager.createPersons(networkStore, 1);
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(2, pullCallback.getResult().getCount());
    }

    /* should use regular auto-pagination when deltaset configuration is missing on the backend and AP is on */
    @Test
    public void testStoreTypeSyncWithMissedConfigurationAtTheBackendAPisOn() throws IOException, InterruptedException {
        testMissedConfigurationAtTheBackendAPisOn(StoreType.SYNC);
    }

    /* should use regular auto-pagination when deltaset configuration is missing on the backend and AP is on */
    @Test
    public void testStoreTypeCacheWithMissedConfigurationAtTheBackendAPisOn() throws IOException, InterruptedException {
        testMissedConfigurationAtTheBackendAPisOn(StoreType.CACHE);
    }

    private void testMissedConfigurationAtTheBackendAPisOn(StoreType storeType) throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_OFF_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_OFF_COLLECTION, Person.class, storeType, client);
        store.setDeltaSetCachingEnabled(true);
        testManager.createPersons(networkStore, 3);
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, emptyQuery, 2);
        assertEquals(3, pullCallback.getResult().getCount());
        testManager.createPersons(networkStore, 1);
        pullCallback = testManager.pullCustom(store, emptyQuery, 2);
        assertEquals(4, pullCallback.getResult().getCount());
    }

    /* check that if query contains skip or limit then delta set is ignored */
    /* with enable deltaset and limit and skip should not use deltaset and should not override lastRunAt */
    @Test
    public void testPullSkipLimitStoreTypeSync() throws InterruptedException, IOException {
        testPullSkipLimit(StoreType.SYNC);
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not override lastRunAt */
    @Test
    public void testPullSkipLimitStoreTypeCache() throws InterruptedException, IOException {
        testPullSkipLimit(StoreType.CACHE);
    }

    private void testPullSkipLimit(StoreType storeType) throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, storeType, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME_2));
        assertEquals(4, testManager.pullCustom(store, client.query()).getResult().getCount());
        Query query = new Query().equals("username", TEST_USERNAME);
        assertEquals(2, testManager.pullCustom(store, query.setLimit(2).setSkip(1)).getResult().getCount());
        updateItem(networkStore, new Query().equals("username", TEST_USERNAME_2));
        assertEquals(2, testManager.pullCustom(store, query.setLimit(2).setSkip(1)).getResult().getCount());
        assertEquals(1, testManager.pullCustom(store, client.query()).getResult().getCount());
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not override lastRunAt */
    @Test
    public void testSyncSkipLimitStoreTypeSync() throws InterruptedException, IOException {
        testSyncSkipLimit(StoreType.SYNC);
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not override lastRunAt */
    @Test
    public void testSyncSkipLimitStoreTypeCache() throws InterruptedException, IOException {
        testSyncSkipLimit(StoreType.CACHE);
    }

    private void testSyncSkipLimit(StoreType storeType) throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, storeType, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME_2));
        assertEquals(4, testManager.sync(store, client.query()).getResult().getCount());
        Query query = new Query().equals("username", TEST_USERNAME);
        assertEquals(2, testManager.sync(store, query.setLimit(2).setSkip(1)).getResult().getCount());
        updateItem(networkStore, new Query().equals("username", TEST_USERNAME_2));
        assertEquals(2, testManager.sync(store, query.setLimit(2).setSkip(1)).getResult().getCount());
        assertEquals(1, testManager.sync(store, client.query()).getResult().getCount());
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not override lastRunAt */
    @Test
    public void testFindSkipLimitStoreTypeCache() throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.CACHE, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME_2));
        assertEquals(4, testManager.find(store, client.query()).getResult().getResult().size());
        Query query = new Query().equals("username", TEST_USERNAME);
        assertEquals(2, testManager.find(store, query.setLimit(2).setSkip(1)).getResult().getResult().size());
        updateItem(networkStore, new Query().equals("username", TEST_USERNAME_2));
        assertEquals(2, testManager.find(store, query.setLimit(2).setSkip(1)).getResult().getResult().size());
        assertEquals(4, testManager.find(store, client.query()).getResult().getResult().size());
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not cause inconsistent data */
    @Test
    public void testPullSkipLimitStoreTypeSync2() throws InterruptedException, IOException {
        testPullSkipLimit2(StoreType.SYNC);
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not cause inconsistent data */
    @Test
    public void testPullSkipLimitStoreTypeCache2() throws InterruptedException, IOException {
        testPullSkipLimit2(StoreType.CACHE);
    }

    private void testPullSkipLimit2(StoreType storeType) throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, storeType, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME_2));
        assertEquals(4, testManager.pullCustom(store, client.query()).getResult().getCount());
        Query query = new Query().equals("username", TEST_USERNAME);
        assertEquals(2, testManager.pullCustom(store, query.setLimit(2).setSkip(1)).getResult().getCount());
        updateItem(networkStore, new Query().equals("username", TEST_USERNAME_2));
        assertEquals(2, testManager.pullCustom(store, query.setLimit(2).setSkip(1)).getResult().getCount());
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.SYNC, client);
        assertEquals(4, store.count().intValue());
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not cause inconsistent data */
    @Test
    public void testSyncSkipLimitStoreTypeSync2() throws InterruptedException, IOException {
        testSyncSkipLimit2(StoreType.SYNC);
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not cause inconsistent data */
    @Test
    public void testSyncSkipLimitStoreTypeCache2() throws InterruptedException, IOException {
        testSyncSkipLimit2(StoreType.CACHE);
    }

    private void testSyncSkipLimit2(StoreType storeType) throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, storeType, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME_2));
        assertEquals(4, testManager.sync(store, client.query()).getResult().getCount());
        Query query = new Query().equals("username", TEST_USERNAME);
        assertEquals(2, testManager.sync(store, query.setLimit(2).setSkip(1)).getResult().getCount());
        updateItem(networkStore, new Query().equals("username", TEST_USERNAME_2));
        assertEquals(2, testManager.sync(store, query.setLimit(2).setSkip(1)).getResult().getCount());
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.SYNC, client);
        assertEquals(4, store.count().intValue());
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not cause inconsistent data */
    @Test
    public void testFindSkipLimitStoreTypeCache2() throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.CACHE, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME_2));
        assertEquals(4, testManager.find(store, client.query()).getResult().getResult().size());
        Query query = new Query().equals("username", TEST_USERNAME);
        assertEquals(2, testManager.find(store, query.setLimit(2).setSkip(1)).getResult().getResult().size());
        updateItem(networkStore, new Query().equals("username", TEST_USERNAME_2));
        assertEquals(2, testManager.find(store, query.setLimit(2).setSkip(1)).getResult().getResult().size());
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.SYNC, client);
        assertEquals(4, store.count().intValue());
    }

    /* support methods */

    private void initDeltaSetCachedCollection(StoreType storeType) throws InterruptedException {
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, storeType, client);
        testManager.cleanBackend(store, storeType);
        store.setDeltaSetCachingEnabled(true);
    }

    /* change one random person's user name to UPDATED_USERNAME */
    private void updateItem() throws InterruptedException {
        updateItem(emptyQuery);
    }

    /* change one random person's user name to UPDATED_USERNAME */
    private void updateItem(DataStore<Person> store) throws InterruptedException {
        updateItem(store, emptyQuery);
    }


    /* change one random person's user name to UPDATED_USERNAME */
    private void updateItem(Query query) throws InterruptedException {
        updateItem(store, query);
    }

    /* change one random person's user name to UPDATED_USERNAME */
    private void updateItem(DataStore<Person> store, Query query) throws InterruptedException {
        List<Person> personsInCache = testManager.find(store, query).getResult().getResult();
        Person person = personsInCache.get(0);
        person.setAge("20");
        testManager.save(store, person);
    }

    /* delete one random item from the cache */
    private void deleteItem() throws InterruptedException {
        deleteItem(emptyQuery);
    }

    /* delete one random item */
    private void deleteItem(DataStore<Person> store) throws InterruptedException {
        deleteItem(store, emptyQuery);
    }

    /* delete one random item from the cache */
    private void deleteItem(Query query) throws InterruptedException {
        deleteItem(store, query);
    }

    /* delete one random item from the cache */
    private void deleteItem(DataStore<Person> store, Query query) throws InterruptedException {
        List<Person> personsInCache = testManager.find(store, query).getResult().getResult();
        Person person = personsInCache.get(0);
        testManager.delete(store, person.getId());
    }

    /* end support methods*/


    /* updated tests*/

    /* with enabled deltaset should return correct number of items when creating (SYNC)*/
    @Test
    public void testPullStoreTypeSync_Creating() throws InterruptedException, IOException {
        testPull_Creating(StoreType.SYNC);
    }

    /* with enabled deltaset should return correct number of items when creating (CACHE)*/
    @Test
    public void testPullStoreTypeCache_Creating() throws InterruptedException, IOException {
        testPull_Creating(StoreType.CACHE);
    }

    private void testPull_Creating(StoreType storeType) throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, storeType, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
        networkStore.save(new Person(TEST_USERNAME_2));
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
        networkStore.save(new Person(TEST_USERNAME_2));
        networkStore.save(new Person(TEST_USERNAME_2));
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(2, pullCallback.getResult().getCount());
        assertEquals(4, store.count().intValue());
    }

    /* with enabled deltaset should return correct number of items when updating (SYNC)*/
    @Test
    public void testPullStoreTypeSync_Updating() throws InterruptedException, IOException {
        testPull_Updating(StoreType.SYNC);
    }

    /* with enabled deltaset should return correct number of items when updating (CACHE)*/
    @Test
    public void testPullStoreTypeCache_Updating() throws InterruptedException, IOException {
        testPull_Updating(StoreType.CACHE);
    }

    private void testPull_Updating(StoreType storeType) throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, storeType, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(3, pullCallback.getResult().getCount());
        List<Person> people = store.find().getResult();
        Person personToUpdate1 = people.get(0);
        Person personToUpdate2 = people.get(1);
        personToUpdate1.setAge("40");
        networkStore.save(personToUpdate1);
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
        personToUpdate1.setAge("50");
        personToUpdate2.setAge("50");
        networkStore.save(personToUpdate1);
        networkStore.save(personToUpdate2);
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(2, pullCallback.getResult().getCount());
        assertEquals(3, store.count().intValue());
    }

    /* with enabled deltaset should return correct number of items when deleting */
    @Test
    public void testPullStoreTypeSync_Deleting() throws InterruptedException, IOException {
        testPull_Deleting(StoreType.SYNC);
    }

    /* with enabled deltaset should return correct number of items when deleting */
    @Test
    public void testPullStoreTypeCache_Deleting() throws InterruptedException, IOException {
        testPull_Deleting(StoreType.CACHE);
    }

    public void testPull_Deleting(StoreType storeType) throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, storeType, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(3, pullCallback.getResult().getCount());
        assertEquals(3, store.count().intValue());
        deleteItem(networkStore);
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(0, pullCallback.getResult().getCount());
        assertEquals(2, store.count().intValue());
        deleteItem(networkStore);
        deleteItem(networkStore);
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(0, pullCallback.getResult().getCount());
        assertEquals(0, store.count().intValue());
    }

    /* with enabled deltaset should return correct number of items when creating (SYNC)*/
    @Test
    public void tesSyncStoreTypeSync_Creating() throws InterruptedException, IOException {
        tesSync_Creating(StoreType.SYNC);
    }

    /* with enabled deltaset should return correct number of items when creating (CACHE)*/
    @Test
    public void tesSyncStoreTypeCache_Creating() throws InterruptedException, IOException {
        tesSync_Creating(StoreType.CACHE);
    }

    private void tesSync_Creating(StoreType storeType) throws InterruptedException, IOException{
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, storeType, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        CustomKinveySyncCallback syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        networkStore.save(new Person(TEST_USERNAME_2));
        syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        networkStore.save(new Person(TEST_USERNAME_2));
        networkStore.save(new Person(TEST_USERNAME_2));
        syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(2, syncCallback.getResult().getCount());
        assertEquals(4, store.count().intValue());
    }

    /* with enabled deltaset should return correct number of items when updating (SYNC)*/
    @Test
    public void testSyncStoreTypeSync_Updating() throws InterruptedException, IOException {
        testSync_Updating(StoreType.SYNC);
    }

    /* with enabled deltaset should return correct number of items when updating (CACHE)*/
    @Test
    public void testSyncStoreTypeCache_Updating() throws InterruptedException, IOException {
        testSync_Updating(StoreType.CACHE);
    }

    private void testSync_Updating(StoreType storeType) throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, storeType, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        CustomKinveySyncCallback syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(3, syncCallback.getResult().getCount());
        assertEquals(3, store.count().intValue());
        updateItem(networkStore);
        syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        assertEquals(3, store.count().intValue());
    }

    /* with enabled deltaset should return correct number of items when deleting (SYNC)*/
    @Test
    public void testSyncStoreTypeSync_Deleting() throws InterruptedException, IOException {
        testSync_Deleting(StoreType.SYNC);
    }

    /* with enabled deltaset should return correct number of items when deleting (CACHE)*/
    @Test
    public void testSyncStoreTypeCache_Deleting() throws InterruptedException, IOException {
        testSync_Deleting(StoreType.CACHE);
    }

    private void testSync_Deleting(StoreType storeType) throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, storeType, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        CustomKinveySyncCallback syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(3, syncCallback.getResult().getCount());
        assertEquals(3, store.count().intValue());
        deleteItem(networkStore);
        syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(0, syncCallback.getResult().getCount());
        assertEquals(2, store.count().intValue());
        deleteItem(networkStore);
        deleteItem(networkStore);
        syncCallback = testManager.sync(store, emptyQuery);
        assertEquals(0, syncCallback.getResult().getCount());
        assertEquals(0, store.count().intValue());
    }

    /* with enabled deltaset should return correct number of items when creating */
    @Test
    public void testFindStoreTypeCache_Creating() throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.CACHE, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        DefaultKinveyReadCallback readCallback = testManager.find(store, emptyQuery);
        assertEquals(1, readCallback.getResult().getResult().size());
        networkStore.save(new Person(TEST_USERNAME_2));
        readCallback = testManager.find(store, emptyQuery);
        assertEquals(2, readCallback.getResult().getResult().size());
        assertEquals(2, store.count().intValue());
    }

    /* with enabled deltaset should return correct number of items when updating */
    @Test
    public void testFindStoreTypeCache_Updating() throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.CACHE, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        DefaultKinveyReadCallback readCallback = testManager.find(store, emptyQuery);
        assertEquals(1, readCallback.getResult().getResult().size());
        updateItem(networkStore);
        readCallback = testManager.find(store, emptyQuery);
        assertEquals(1, readCallback.getResult().getResult().size());
    }

    /* with enabled deltaset should return correct number of items when deleting v2 */
    @Test
    public void testFindStoreTypeCache_Deleting() throws InterruptedException, IOException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.CACHE, client);
        store.setDeltaSetCachingEnabled(true);
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        networkStore.save(new Person(TEST_USERNAME));
        DefaultKinveyReadCallback readCallback = testManager.find(store, emptyQuery);
        assertEquals(3, readCallback.getResult().getResult().size());
        deleteItem(networkStore);
        readCallback = testManager.find(store, emptyQuery);
        assertEquals(2, readCallback.getResult().getResult().size());
        deleteItem(networkStore);
        deleteItem(networkStore);
        readCallback = testManager.find(store, emptyQuery);
        assertEquals(0, readCallback.getResult().getResult().size());
    }

    /* with enabled deltaset should return correct number of items when deleting and updating */
    @Test
    public void testFindStoreTypeCache_DeletingUpdating() throws InterruptedException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.CACHE, client);
        store.setDeltaSetCachingEnabled(true);
        testManager.save(networkStore, new Person(TEST_USERNAME));
        testManager.save(networkStore, new Person(TEST_USERNAME_2));
        testManager.save(networkStore, new Person(TEST_USERNAME_2));
        DefaultKinveyReadCallback readCallback = testManager.find(store, emptyQuery);
        assertEquals(3, readCallback.getResult().getResult().size());
        deleteItem(networkStore);
        updateItem(networkStore);
        readCallback = testManager.find(store, emptyQuery);
        assertEquals(2, readCallback.getResult().getResult().size());
    }

    /* should delete old items when lastRunAt is outdated */
    @Test
    public void testParameterValueOutOfRangeErrorHandling_StoreTypeSync() throws IOException, InterruptedException {
        testParameterValueOutOfRangeErrorHandling(StoreType.SYNC);
    }

    /* should delete old items when lastRunAt is outdated */
    @Test
    public void testParameterValueOutOfRangeErrorHandling_StoreTypeCache() throws IOException, InterruptedException {
        testParameterValueOutOfRangeErrorHandling(StoreType.CACHE);
    }

    private void testParameterValueOutOfRangeErrorHandling(StoreType storeType) throws IOException, InterruptedException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, storeType, client);
        store.setDeltaSetCachingEnabled(true);
        testManager.createPersons(networkStore, 10);
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, client.query());
        assertEquals(10, pullCallback.getResult().getCount());
        deleteItem(networkStore);
        deleteItem(networkStore);
        deleteItem(networkStore);
        deleteItem(networkStore);
        deleteItem(networkStore);
        String lastRequestTime = "2018-05-14T09:40:44.470Z";
        ICache<QueryCacheItem> queryCache = client.getSyncManager().getCacheManager().getCache(Constants.QUERY_CACHE_COLLECTION, QueryCacheItem.class, Long.MAX_VALUE);
        QueryCacheItem cacheItem = queryCache.getFirst();
        cacheItem.setLastRequestTime(lastRequestTime);
        queryCache.save(cacheItem);
        pullCallback = testManager.pullCustom(store, client.query());
        assertEquals(5, pullCallback.getResult().getCount());
        assertEquals(5, testManager.find(store, emptyQuery).getResult().getResult().size());
    }

    @Test
    public void testDeltaSetDoRequestAfter2FailedAttempts() throws IOException, InterruptedException {
        DataStore<Person> networkStore = DataStore.collection(Person.DELTA_SET_OFF_COLLECTION, Person.class, StoreType.NETWORK, client);
        testManager.cleanBackend(networkStore, StoreType.NETWORK);
        store = DataStore.collection(Person.DELTA_SET_OFF_COLLECTION, Person.class, StoreType.SYNC, client);
        store.setDeltaSetCachingEnabled(true);
        testManager.createPersons(networkStore, 2);
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, client.query());
        assertEquals(2, pullCallback.getResult().getCount());
        ICache<QueryCacheItem> queryCache = client.getSyncManager().getCacheManager().getCache(Constants.QUERY_CACHE_COLLECTION, QueryCacheItem.class, Long.MAX_VALUE);
        assertEquals(1, queryCache.get().size());
        pullCallback = testManager.pullCustom(store, client.query());
        assertEquals(2, pullCallback.getResult().getCount());
        assertEquals(1, queryCache.get().size());
        pullCallback = testManager.pullCustom(store, client.query());
        assertEquals(2, pullCallback.getResult().getCount());
        assertEquals(1, queryCache.get().size());
    }

    @Test
    public void testCountHaveTimeStamp() {
        Method method = null;
        try {
            method = BaseDataStore.class.getDeclaredMethod("internalCountNetwork");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        assert method != null;
        method.setAccessible(true);
        KinveyDeltaSetCountResponse response = null;
        try {
            response = (KinveyDeltaSetCountResponse) method.invoke(BaseDataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        assertNotNull(response);
        assertNotNull(response.getLastRequestTime());
    }
}
