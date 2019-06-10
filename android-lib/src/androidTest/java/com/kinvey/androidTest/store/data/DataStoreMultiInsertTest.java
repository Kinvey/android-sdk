package com.kinvey.androidTest.store.data;

import android.content.Context;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyReadCallback;
import com.kinvey.android.model.User;
import com.kinvey.android.store.DataStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyReadResponse;
import com.kinvey.java.store.StoreType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.Console;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DataStoreMultiInsertTest {

    private static final String TEST_USERNAME = "Test_UserName";
    private static final int DEFAULT_TIMEOUT = 60;
    private static final int LONG_TIMEOUT = 6 * DEFAULT_TIMEOUT;

    private Client client;

    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AbstractClient.KINVEY_API_VERSION = "5";
        client = new Client.Builder(mMockContext).build();
        client.enableDebugLogging();
        final CountDownLatch latch = new CountDownLatch(1);
        LooperThread looperThread = null;
        if (!client.isUserLoggedIn()) {
            looperThread = new LooperThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        UserStore.login(TestManager.USERNAME, TestManager.PASSWORD, client, new KinveyClientCallback<User>() {
                            @Override
                            public void onSuccess(User result) {
                                assertNotNull(result);
                                latch.countDown();
                            }

                            @Override
                            public void onFailure(Throwable error) {
                                assertNull(error);
                                latch.countDown();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            looperThread.start();
        } else {
            latch.countDown();
        }
        latch.await();
        if (looperThread != null) {
            looperThread.mHandler.sendMessage(new Message());
        }
    }

    private static class DefaultKinveyClientCallback implements KinveyClientCallback<Person> {

        private CountDownLatch latch;
        Person result;
        Throwable error;

        DefaultKinveyClientCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Person result) {
            this.result = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        void finish() {
            latch.countDown();
        }
    }

    private static class DefaultKinveyDeleteCallback implements KinveyDeleteCallback {

        private CountDownLatch latch;
        Integer result;
        Throwable error;

        DefaultKinveyDeleteCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Integer result) {
            this.result = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        void finish() {
            latch.countDown();
        }
    }

    private static class DefaultKinveyReadCallback implements KinveyReadCallback<Person> {

        private CountDownLatch latch;
        KinveyReadResponse<Person> result;
        Throwable error;

        DefaultKinveyReadCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(KinveyReadResponse<Person> result) {
            this.result = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        void finish() {
            latch.countDown();
        }
    }

    private Person createPerson(String name) {
        return new Person(name);
    }

    private DefaultKinveyDeleteCallback delete(final DataStore<Person> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyDeleteCallback callback = new DefaultKinveyDeleteCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.delete(query, callback);
            }
        });
        looperThread.start();
        latch.await(120, TimeUnit.SECONDS);
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private DefaultKinveyReadCallback find(final DataStore<Person> store, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyReadCallback callback = new DefaultKinveyReadCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.find(callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private void clearBackend(DataStore<Person> store) throws InterruptedException {
        Query query = client.query();
        query = query.notEqual("age", "100500");
        DefaultKinveyDeleteCallback deleteCallback = delete(store, query);
    }

    public void createAndSavePerson(final DataStore<Person> store, String username) throws InterruptedException {
        Person person = createPerson(username);
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
    }

    private DefaultKinveyClientCallback save(final DataStore<Person> store, final Person person) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.save(person, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private void testSave(StoreType storeType) throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        client.getSyncManager().clear(Person.COLLECTION);
        DefaultKinveyClientCallback callback = save(store, createPerson(TEST_USERNAME));
        assertNotNull(callback.result);
        assertNotNull(callback.result.getUsername());
        assertNull(callback.error);
        assertTrue(callback.result.getUsername().equals(TEST_USERNAME));
    }

    public void testSaveWithoutId(StoreType storeType) throws InterruptedException {
        DataStore<Person> storeAuto = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        clearBackend(storeAuto);
        createAndSavePerson(storeAuto, TEST_USERNAME);
        DefaultKinveyReadCallback findCallback = find(storeAuto, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(findCallback.result.getResult().size() == 1);
        assertTrue(findCallback.result.getResult().get(0).getId() != null);
    }

    public void testSaveWithId(StoreType storeType) throws InterruptedException {
        DataStore<Person> storeAuto = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        clearBackend(storeAuto);
        String id = "123456";
        Person person = createPerson(TEST_USERNAME);
        person.setId(id);
        DefaultKinveyClientCallback saveCallback = save(storeAuto, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertEquals(saveCallback.result.getId(), id);
    }

    private void print(String msg) {
        Console con = System.console();
        if (con != null) {
            con.printf(msg);
        }
    }

    // NETWORK STORE

    @Test
    public void testSaveNetwork() throws InterruptedException {
        print("should send POST with a single item with no _id");
        // create an item that has no _id property
        // call save() with it
        // find using network store
        testSave(StoreType.NETWORK);
    }

    @Test
    public void testSaveWithoutIdNetwork() throws InterruptedException {
        print("should send POST with a single item with no _id");
        // create an item that has no _id property
        // call save() with it
        // find using network store
        testSaveWithoutId(StoreType.NETWORK);
    }

    @Test
    public void testSaveWithIdNetwork() throws InterruptedException {
        print("should send PUT with a sngle item with _id");
        // create an item with _id property
        // call save() with it
        // find using network store
        testSaveWithId(StoreType.NETWORK);
    }

    @Test
    public void testSaveListWithoutIdNetwork() throws InterruptedException {
        print("should send POST multi-insert request for array of items with no _id");
        // create an array with a few items that have no _id property
        // save() with the array as param
        // find using network store
    }

    @Test
    public void testSaveListWithIdNetwork() throws InterruptedException {
        print("should sent PUT requests for an array of items with _id");
        // create an array with a few items that have _id property
        // save() with the array as param
        // find using network store
    }

    @Test
    public void testSaveListCombineWithIdAndWithoutIdNetwork() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array
        // find using network store
    }

    @Test
    public void testSaveListReturnErrorForEmptyListNetwork() throws InterruptedException {
        print("should return an error for an empty array");
        // create an empty array
        // save() using the array
    }

    @Test
    public void testSaveListReturnErrorForInvalidCredentialsNetwork() throws InterruptedException {
        print("should return an error when all items fail with multi-insert for invalid credentials");
        // create an array with a few items that have no _id property
        // set a collection permission to deny creating items
        // save() using the array from above
    }

    @Test
    public void testSaveListReturnErrorArrayForAllItemsFailNetwork() throws InterruptedException {
        print("should return an array of errors for all items failing for different reasons");
        // create an array containing two items failing for different reasons
        // save using the array above
    }


    @Test
    public void testSaveListReturnErrorArrayForSomeItemsFailNetwork() throws InterruptedException {
        print("should return an entities and errors when some requests fail and some succeed");
        // create an array of items with no _id and the second of them should have invalid _geoloc params
        // save using the array above
        // find using network store
    }

    @Test
    public void testSaveListReturnPutFailuresAtMatchingIndexNetwork() throws InterruptedException {
        print("should return PUT failures at the matching index");
        // create an array  - [{no_id, invalid_geoloc},{_id}, {_id, invalid_geoloc}, {no_id}]
        // save using the array above
        // find using network store
    }

    // SYNC STORE

    @Test
    public void testSaveSync() throws InterruptedException {
        print("should send POST with a single item with no _id");
        // create an item that has no _id property
        // call save() with it
        // pendingSyncEntities()
        // find() using sync store
        testSave(StoreType.SYNC);
    }

    @Test
    public void testSaveWithoutIdSync() throws InterruptedException {
        print("should send POST with a single item with no _id");
        // create an item that has no _id property
        // call save() with it
        // pendingSyncEntities()
        // find() using sync store
        testSaveWithoutId(StoreType.SYNC);
    }

    @Test
    public void testSaveWithIdSync() throws InterruptedException {
        print("should send PUT with a single item with _id");
        // create an item with _id property
        // call save() with it
        // pendingSyncEntities()
        // find() using syncstore
        testSaveWithId(StoreType.SYNC);
    }

    @Test
    public void testSaveListWithoutIdSync() throws InterruptedException {
        print("should send save an array of items with no _id");
        // create an array with a few items that have no _id property
        // save() with the array as param
        // pendingSyncEntities()
        // find() using syncstore
    }

    @Test
    public void testSaveListWithIdSync() throws InterruptedException {
        print("should save an array of items with _id");
        // create an array with a few items that have _id property
        // save() with the array as param
        // pendingSyncEntities()
        // find() using syncstore
    }

    @Test
    public void testSaveListCombineWithIdAndWithoutIdSync() throws InterruptedException {
        print("should save and array of items with and without _id");
        // create an array that has 2 items with _id and 2 without
        // save() using the array
        // pendingSyncEntities()
        // find() using syncstore
    }

    @Test
    public void testSaveListReturnErrorForEmptyListSync() throws InterruptedException {
        print("should return an error for an empty array");
        // create an empty array
        // save() using the array
    }

    // AUTO STORE

    @Test
    public void testSaveAuto() throws InterruptedException {
        print("should send POST with a single item with no _id");
        // create an item that has no _id property
        // call save() with it
        // call find() with sync store
        // find using network store
        testSave(StoreType.AUTO);
    }

    @Test
    public void testSaveWithoutIdAuto() throws InterruptedException {
        print("should send POST with a single item with no _id");
        // create an item that has no _id property
        // call save() with it
        // call find() with sync store
        // find using network store
        testSaveWithoutId(StoreType.AUTO);
    }

    @Test
    public void testSaveWithConnectivityErrorAuto() throws InterruptedException {
        print("should send with connectivity error");
        // create an item that has no _id property with invalid _geoloc params
        // call save() with it -mock it for connectivity error
        // call find() with syncstore
        // call pendingSyncEntities()
    }

    @Test
    public void testSaveWithIdAuto() throws InterruptedException {
        print("should send PUT with a single item with _id");
        // create an item with _id property
        // call save() with it
        // call find() with syncstore
        // find using networkstore
        testSaveWithId(StoreType.AUTO);
    }

    public void testSaveLocallyIfNetworkErrorAuto() throws InterruptedException {
        print("should save the item locally if network connectivity issue");
        // create an item with no _id
        // call save with it - mock the request to return connectivity error
        // find using syncstore
        // pendingSyncEntities()
    }
}
