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
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.Query;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
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
    @Test
    public void testUpdateHandling() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.push(store);
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
        updateItem();
        testManager.push(store);
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
        testManager.delete(store, emptyQuery);
        testManager.push(store);
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(0, pullCallback.getResult().getCount());
        assertEquals(0, testManager.find(store, emptyQuery).getResult().getResult().size());
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

    @Test
    public void testSyncAfterUpdatingAndDeleting() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        CustomKinveySyncCallback syncCallback  = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        updateItem();
        syncCallback  = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        deleteItem();
        syncCallback  = testManager.sync(store, emptyQuery);
        assertEquals(0, syncCallback.getResult().getCount());
        assertEquals(0, testManager.find(store, emptyQuery).getResult().getResult().size());
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

    @Test
    public void testSyncStoreTypeCache_UpdateAndDeleteItemWithQuery() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        Person person = testManager.save(store, new Person(TEST_USERNAME)).getResult();
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME_2));
        assertEquals(3, testManager.sync(store, emptyQuery).getResult().getCount());
        person.setAge("20");
        testManager.save(store, person);
        assertEquals(1, testManager.sync(store, emptyQuery).getResult().getCount());
        testManager.delete(store, person.getId());
        assertEquals(0, testManager.sync(store, emptyQuery).getResult().getCount());
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
    public void testFindStoreTypeCache_UpdateAndDeleteItem() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        Person person = testManager.save(store, new Person(TEST_USERNAME)).getResult();
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
        person.setAge("20");
        testManager.save(store, person);
        assertEquals(1, testManager.find(store, emptyQuery).getResult().getResult().size());
        testManager.delete(store, person.getId());
        assertEquals(0, testManager.find(store, emptyQuery).getResult().getResult().size());
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
    @Test
    public void testAutoPaginationStoreTypeSync() throws InterruptedException, IOException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.createPersons(store, 5);
        testManager.push(store);
        assertEquals(5, testManager.pullCustom(store, client.query(), 2).getResult().getCount());
        List<Person> people = testManager.find(store, emptyQuery).getResult().getResult();
        Person person1 = people.get(0);
        Person person2 = people.get(1);
        Person person3 = people.get(2);
        person1.setAge("20");
        person2.setAge("21");
        person3.setAge("22");
        testManager.save(store, person1);
        testManager.save(store, person2);
        testManager.save(store, person3);
        testManager.push(store);
        assertEquals(3, testManager.pullCustom(store, emptyQuery, 2).getResult().getCount());
        assertEquals(0, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        person1.setAge("23");
        person2.setAge("24");
        person3.setAge("25");
        testManager.save(store, person1);
        testManager.save(store, person2);
        testManager.save(store, person3);
        testManager.push(store);
        assertEquals(3, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        testManager.delete(store, person1.getId());
        testManager.push(store);
        assertEquals(0, testManager.pullCustom(store, emptyQuery).getResult().getCount());
    }

    @Test
    public void testAutoPaginationStoreTypeCache() throws InterruptedException, IOException {
        initDeltaSetCachedCollection(StoreType.CACHE);
        testManager.createPersons(store, 5);
        assertEquals(5, testManager.pullCustom(store, client.query(), 2).getResult().getCount());
        List<Person> people = testManager.find(store, emptyQuery).getResult().getResult();
        Person person1 = people.get(0);
        Person person2 = people.get(1);
        Person person3 = people.get(2);
        person1.setAge("20");
        person2.setAge("21");
        person3.setAge("22");
        testManager.save(store, person1);
        testManager.save(store, person2);
        testManager.save(store, person3);
        assertEquals(3, testManager.pullCustom(store, client.query(), 2).getResult().getCount());
        assertEquals(0, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        //delta set is used in this pull because find in cache store type makes call to the backend and caches result (to query cache collection as well)
        person1.setAge("23");
        person2.setAge("24");
        person3.setAge("25");
        testManager.save(store, person1);
        testManager.save(store, person2);
        testManager.save(store, person3);
        assertEquals(3, testManager.pullCustom(store, emptyQuery).getResult().getCount());
        testManager.delete(store, person1.getId());
        assertEquals(0, testManager.pullCustom(store, emptyQuery).getResult().getCount());
    }


    /* error handling*/
    @Test
    public void testUnconfiguredCollectionAtTheBackend() throws InterruptedException {
        store = DataStore.collection(Person.DELTA_SET_OFF_COLLECTION, Person.class, StoreType.SYNC, client);
        testManager.cleanBackend(store, StoreType.SYNC);
        store.setDeltaSetCachingEnabled(true);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.push(store);
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
        updateItem();
        testManager.push(store);
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
    }

    /* check that skip and limit is ignored in delta set*/
    @Test
    public void testSkipLimit() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.cleanBackend(store, StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.push(store);
        assertEquals(3, testManager.pullCustom(store, client.query().setLimit(2).setSkip(0)).getResult().getCount());
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.push(store);
        assertEquals(1, testManager.pullCustom(store, client.query().setLimit(2).setSkip(0)).getResult().getCount());
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
    private void updateItem(Query query) throws InterruptedException {
        List<Person> personsInCache = testManager.find(store, query).getResult().getResult();
        Person person = personsInCache.get(0);
        person.setAge("20");
        testManager.save(store, person);
    }

    /* delete one random item from the cache */
    private void deleteItem() throws InterruptedException {
        deleteItem(emptyQuery);
    }

    /* delete one random item from the cache */
    private void deleteItem(Query query) throws InterruptedException {
        List<Person> personsInCache = testManager.find(store, query).getResult().getResult();
        Person person = personsInCache.get(0);
        testManager.delete(store, person.getId());
    }

}
