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
        updateItemLocally();
        testManager.push(store);
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(1, pullCallback.getResult().getCount());
        testManager.delete(store, emptyQuery);
        testManager.push(store);
        pullCallback = testManager.pullCustom(store, emptyQuery);
        assertEquals(0, pullCallback.getResult().getCount());
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
        deleteItemLocally(usernameQuery);
        testManager.push(store);
        pullCallback = testManager.pullCustom(store, usernameQuery);
        assertEquals(0, pullCallback.getResult().getCount());
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
        updateItemLocally(usernameQuery);
        testManager.push(store);
        pullCallback = testManager.pullCustom(store, usernameQuery);
        assertEquals(1, pullCallback.getResult().getCount());
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
        testManager.save(store, new Person(TEST_USERNAME_2));
        syncCallback  = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
    }

    @Test
    public void testSyncAfterUpdating() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        CustomKinveySyncCallback syncCallback  = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        updateItemLocally();
        syncCallback  = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
    }

    @Test
    public void testSyncAfterUpdatingAndDeleting() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        CustomKinveySyncCallback syncCallback  = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        updateItemLocally();
        syncCallback  = testManager.sync(store, emptyQuery);
        assertEquals(1, syncCallback.getResult().getCount());
        deleteItemLocally();
        syncCallback  = testManager.sync(store, emptyQuery);
        assertEquals(0, syncCallback.getResult().getCount());
    }

    @Test
    public void testSyncWithQueryAfterDeleting() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME_2));
        CustomKinveySyncCallback syncCallback  = testManager.sync(store, usernameQuery);
        assertEquals(2, syncCallback.getResult().getCount());
        deleteItemLocally(usernameQuery);
        syncCallback  = testManager.sync(store, usernameQuery);
        assertEquals(0, syncCallback.getResult().getCount());
    }

    @Test
    public void testSyncWithQueryAfterUpdating() throws InterruptedException {
        initDeltaSetCachedCollection(StoreType.SYNC);
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME));
        testManager.save(store, new Person(TEST_USERNAME_2));
        CustomKinveySyncCallback syncCallback  = testManager.sync(store, usernameQuery);
        assertEquals(2, syncCallback.getResult().getCount());
        updateItemLocally(usernameQuery);
        syncCallback  = testManager.sync(store, usernameQuery);
        assertEquals(1, syncCallback.getResult().getCount());
    }



    /* support methods */

    private void initDeltaSetCachedCollection(StoreType storeType) throws InterruptedException {
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, storeType, client);
        testManager.cleanBackend(store, StoreType.SYNC);
        store.setDeltaSetCachingEnabled(true);
    }

    /* change one random person's user name to UPDATED_USERNAME */
    private void updateItemLocally() throws InterruptedException {
        updateItemLocally(emptyQuery);
    }


    /* change one random person's user name to UPDATED_USERNAME */
    private void updateItemLocally(Query query) throws InterruptedException {
        List<Person> personsInCache = testManager.find(store, query).getResult().getResult();
        Person person = personsInCache.get(0);
        person.setAge("20");
        testManager.save(store, person);
    }

    /* delete one random item from the cache */
    private void deleteItemLocally() throws InterruptedException {
        deleteItemLocally(emptyQuery);
    }

    /* delete one random item from the cache */
    private void deleteItemLocally(Query query) throws InterruptedException {
        List<Person> personsInCache = testManager.find(store, query).getResult().getResult();
        Person person = personsInCache.get(0);
        testManager.delete(store, person.getId());
    }

}
