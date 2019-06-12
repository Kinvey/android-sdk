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
import com.kinvey.androidTest.model.EntitySet;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.KinveyJsonResponseException;
import com.kinvey.java.model.KinveyReadResponse;
import com.kinvey.java.store.StoreType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private static final int MAX_PERSONS_COUNT = 5;
    private static final int MAX_ENTITY_COUNT = 5;
    private static final String ERR_GEOLOC = "#!@%^&*())_+?{}";

    private Client client;
    private Client unauthClient;

    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AbstractClient.KINVEY_API_VERSION = "5";
        client = new Client.Builder(mMockContext).build();
        unauthClient = new Client.Builder(mMockContext).build();
        client.enableDebugLogging();
        unauthClient.enableDebugLogging();
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

    private static class DefaultKinveyClientListCallback implements KinveyClientCallback<List<Person>> {

        private CountDownLatch latch;
        List<Person> result;
        Throwable error;

        DefaultKinveyClientListCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(List<Person> result) {
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

    private static class DefaultKinveyEntityListCallback implements KinveyClientCallback<List<EntitySet>> {

        private CountDownLatch latch;
        List<EntitySet> result;
        Throwable error;

        DefaultKinveyEntityListCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(List<EntitySet> result) {
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

    private void print(String msg) {
        Console con = System.console();
        if (con != null) {
            con.printf(msg);
        }
    }

    public void createAndSavePerson(final DataStore<Person> store, String username) throws InterruptedException {
        Person person = createPerson(username);
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
    }

    private DefaultKinveyClientListCallback saveList(final DataStore<Person> store, final List<Person> persons) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientListCallback callback = new DefaultKinveyClientListCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
            try {
                store.save(persons, callback);
            } catch (Exception e) {
                callback.onFailure(e);
            }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
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

    private void testSaveWithoutId(StoreType storeType) throws InterruptedException {
        DataStore<Person> storeAuto = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        clearBackend(storeAuto);
        createAndSavePerson(storeAuto, TEST_USERNAME);
        DefaultKinveyReadCallback findCallback = find(storeAuto, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(findCallback.result.getResult().size() == 1);
        assertTrue(findCallback.result.getResult().get(0).getId() != null);
    }

    private void testSaveWithId(StoreType storeType) throws InterruptedException {
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

    private DefaultKinveyEntityListCallback saveListEntitySet(final DataStore<EntitySet> store, final List<EntitySet> object) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyEntityListCallback callback = new DefaultKinveyEntityListCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.save(object, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    // create an array with a few items that have _id property or have not.
    private List<Person> createPersonsList(boolean withId) {
        List<Person> items = new ArrayList<>();
        for (int i = 0; i < MAX_PERSONS_COUNT; i++) {
            Person person = createPerson(TEST_USERNAME + String.valueOf(i));
            if (withId) {
                String id = "123456" + i;
                person.setId(id);
            }
            items.add(person);
        }
        return items;
    }

    // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no _id, _id]
    private List<Person> createCombineList() {
        List<Person> items = new ArrayList<>();
        Person person1 = new Person(TEST_USERNAME + String.valueOf(1));
        items.add(person1);
        Person person2 = new Person("76575", TEST_USERNAME + String.valueOf(2));
        items.add(person2);
        Person person3 = new Person(TEST_USERNAME + String.valueOf(3));
        items.add(person3);
        Person person4 = new Person("53521", TEST_USERNAME + String.valueOf(4));
        items.add(person4);
        return items;
    }

    // create an array containing two items failing for different reasons
    private List<Person> createErrList() {
        List<Person> items = new ArrayList<>();
        String errStr = ERR_GEOLOC;
        Person person1 = new Person(errStr, TEST_USERNAME + String.valueOf(1));
        items.add(person1);
        Person person2 = new Person("76575", TEST_USERNAME + String.valueOf(2));
        person2.setGeoloc(errStr);
        items.add(person2);
        return items;
    }

    // create an array of items with no _id and the second of them should have invalid _geoloc params
    private List<Person> createErrList1() {
        List<Person> items = new ArrayList<>();
        Person person1 = new Person(TEST_USERNAME + String.valueOf(1));
        items.add(person1);
        Person person2 = new Person("76575", TEST_USERNAME + String.valueOf(2));
        person2.setGeoloc(ERR_GEOLOC);
        items.add(person2);
        return items;
    }

    // create an array  - [{no_id, invalid_geoloc},{_id}, {_id, invalid_geoloc}, {no_id}]
    private List<Person> createErrListGeoloc() {
        List<Person> items = new ArrayList<>();
        String errStr = ERR_GEOLOC;
        Person person1 = new Person(TEST_USERNAME + String.valueOf(1));
        person1.setGeoloc(errStr);
        items.add(person1);
        Person person2 = new Person("76575", TEST_USERNAME + String.valueOf(2));
        items.add(person2);
        Person person3 = new Person("343275", TEST_USERNAME + String.valueOf(3));
        person1.setGeoloc(errStr);
        items.add(person3);
        Person person4 = new Person(TEST_USERNAME + String.valueOf(4));
        items.add(person4);
        return items;
    }

    private List<EntitySet> createEntityList() {
        List<EntitySet> items = new ArrayList<>();
        EntitySet entity = null;
        for (int i = 0; i < MAX_ENTITY_COUNT; i++) {
            entity = new EntitySet();
            items.add(entity);
        }
        return items;
    }

    private void testSaveList(List<Person> personList, StoreType storeType) throws InterruptedException {
        DataStore<Person> personStore = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        clearBackend(personStore);
        DefaultKinveyClientListCallback saveCallback = saveList(personStore, personList);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertEquals(saveCallback.result.size(), personList.size());
    }

    private void testSaveListWithId(StoreType storeType) throws InterruptedException {
        List<Person> personList = createPersonsList(true);
        testSaveList(personList, storeType);
    }

    private void testSaveEmptyList(List<Person> personList, StoreType storeType) throws InterruptedException {
        DataStore<Person> personStore = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        clearBackend(personStore);
        DefaultKinveyClientListCallback saveCallback = saveList(personStore, personList);
        assertNull(saveCallback.result);
        assertNotNull(saveCallback.error);
    }

    private void testSaveListNoAccessErr(List<EntitySet> list, StoreType storeType) throws InterruptedException {
        DataStore<EntitySet> entityStore = DataStore.collection(EntitySet.COLLECTION, EntitySet.class, storeType, client);
        DefaultKinveyEntityListCallback defaultKinveyEntityCallback = saveListEntitySet(entityStore, list);
        assertNotNull(defaultKinveyEntityCallback.error);
        assertEquals(defaultKinveyEntityCallback.error.getClass(), KinveyJsonResponseException.class);
    }

    private void testSaveListWithErr(List<Person> personList, StoreType storeType) throws InterruptedException {
        DataStore<Person> personStore = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        clearBackend(personStore);
        DefaultKinveyClientListCallback saveCallback = saveList(personStore, personList);
        assertNull(saveCallback.result);
        assertNotNull(saveCallback.error);
    }

    private void testSaveListWithoutId(StoreType storeType) throws InterruptedException {
        List<Person> personList = createPersonsList(false);
        testSaveList(personList, storeType);
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
        testSaveListWithoutId(StoreType.NETWORK);
    }

    @Test
    public void testSaveListWithIdNetwork() throws InterruptedException {
        print("should sent PUT requests for an array of items with _id");
        // create an array with a few items that have _id property
        // save() with the array as param
        // find using network store
        testSaveListWithId(StoreType.NETWORK);
    }

    @Test
    public void testSaveListCombineWithIdAndWithoutIdNetwork() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no _id, _id]
        // save() using the array
        // find using network store
        List<Person> list = createCombineList();
        testSaveList(list, StoreType.NETWORK);
    }

    @Test
    public void testSaveListReturnErrorForEmptyListNetwork() throws InterruptedException {
        print("should return an error for an empty array");
        // create an empty array
        // save() using the array
        List<Person> list = new ArrayList<>();
        testSaveEmptyList(list, StoreType.NETWORK);
    }

    @Test
    public void testSaveListReturnErrorForInvalidCredentialsNetwork() throws InterruptedException {
        print("should return an error when all items fail with multi-insert for invalid credentials");
        // create an array with a few items that have no _id property
        // set a collection permission to deny creating items
        // save() using the array from above
        List<EntitySet> entityList = createEntityList();
        testSaveListNoAccessErr(entityList, StoreType.NETWORK);
    }

    @Test
    public void testSaveListReturnErrorArrayForAllItemsFailNetwork() throws InterruptedException {
        print("should return an array of errors for all items failing for different reasons");
        // create an array containing two items failing for different reasons
        // save using the array above
        List<Person> personList = createErrList();
        testSaveListWithErr(personList, StoreType.NETWORK);
    }

    @Test
    public void testSaveListReturnErrorArrayForSomeItemsFailNetwork() throws InterruptedException {
        print("should return an entities and errors when some requests fail and some succeed");
        // create an array of items with no _id and the second of them should have invalid _geoloc params
        // save using the array above
        // find using network store
        List<Person> personList = createErrList1();
        testSaveListWithErr(personList, StoreType.NETWORK);
    }

    @Test
    public void testSaveListReturnPutFailuresAtMatchingIndexNetwork() throws InterruptedException {
        print("should return PUT failures at the matching index");
        // create an array  - [{no_id, invalid_geoloc},{_id}, {_id, invalid_geoloc}, {no_id}]
        // save using the array above
        // find using network store
        List<Person> personList = createErrListGeoloc();
        testSaveListWithErr(personList, StoreType.NETWORK);
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
        testSaveWithoutId(StoreType.SYNC);
    }

    @Test
    public void testSaveListWithIdSync() throws InterruptedException {
        print("should save an array of items with _id");
        // create an array with a few items that have _id property
        // save() with the array as param
        // pendingSyncEntities()
        // find() using syncstore
        testSaveWithId(StoreType.SYNC);
    }

    @Test
    public void testSaveListCombineWithIdAndWithoutIdSync() throws InterruptedException {
        print("should save and array of items with and without _id");
        // create an array that has 2 items with _id and 2 without
        // save() using the array
        // pendingSyncEntities()
        // find() using syncstore
        List<Person> list = createCombineList();
        testSaveList(list, StoreType.SYNC);
    }

    @Test
    public void testSaveListReturnErrorForEmptyListSync() throws InterruptedException {
        print("should return an error for an empty array");
        // create an empty array
        // save() using the array
        List<Person> list = new ArrayList<>();
        testSaveEmptyList(list, StoreType.SYNC);
    }

    @Test
    public void testPushItemsListWithoutIdSync() throws InterruptedException {
        print("should use multi insert for multiple items without _id");
        // create an array of items without _id
        // save using the array above
        // push()
        // pendingSyncEntities()
        // find using syncstore
    }

    @Test
    public void testPushItemsListCombineWithIdAndWithoutIdSync() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array
        // push()
        // pendingSyncEntities()
        // find using syncstore
    }

    @Test
    public void testPushItemsListCombineWithIdAndWithoutIdMockedSync() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id - mocked");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array
        // push()
        // pendingSyncEntities()
        // find using syncstore
    }

    @Test
    public void testPushItemsListReturnsErrorForEachItemSync() throws InterruptedException {
        print("should return the failure reason in the result for each pushed item even if it is the same");
        // create an array of items without _id and set the collection permission for create to never
        // save() using the array
        // push()
        // pendingSyncEntities()
        // find using syncstore
    }

    @Test
    public void testPushItemsListReturnsErrorsForEachItemErrorSync() throws InterruptedException {
        print("should return the failure reason in the result for each pushed item when they are different");
        // create an array with 3 items without _id, two of which should be invalid for different reasons
        // save using the array above
        // push()
        // pendingSyncEntities()
        // find using syncstore
    }

    @Test
    public void testPushShouldUseMultiInsertAfterSaveSync() throws InterruptedException {
        print("should use multi-insert even if the items have not been created in an array");
        // save an item without _id
        // save another item without _id
        // push()
        // pendingSyncEntities()
        // find using syncstore
    }

    @Test
    public void testSyncItemsListSync() throws InterruptedException {
        print("test sync items list");
        // create an array of 3 items, the second of which has invalid _geoloc parameters
        // save()
        // sync()
        // pendingSyncEntities()
        // find() using networkstore
        // find using syncstore
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

    @Test
    public void testSaveListWithoutIdAuto() throws InterruptedException {
        print("should send POST multi-insert request for array of items with no _id");
        // create an array with a few items that have no _id property
        // save() with the array as param
        // find using network store
    }

    @Test
    public void testSaveListWithIdAuto() throws InterruptedException {
        print("should sent PUT requests for an array of items with _id");
        // create an array with a few items that have _id property
        // save() with the array as param
        // find using network store
    }

    @Test
    public void testSaveListCombineWithIdAndWithoutIdAuto() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array
        // find using network store
    }

    @Test
    public void testSaveListReturnErrorForEmptyListAuto() throws InterruptedException {
        print("should return an error for an empty array");
        // create an empty array
        // save() using the array
    }

    @Test
    public void testSaveListReturnErrorForInvalidCredentialsAuto() throws InterruptedException {
        print("should return an error when all items fail with multi-insert for invalid credentials");
        // create an array with a few items that have no _id property
        // set a collection permission to deny creating items
        // save() using the array from above
    }

    @Test
    public void testSaveListReturnErrorArrayForAllItemsFailAuto() throws InterruptedException {
        print("should return an array of errors for all items failing for different reasons");
        // create an array containing two items failing for different reasons
        // save using the array above
    }


    @Test
    public void testSaveListReturnErrorArrayForSomeItemsFailAuto() throws InterruptedException {
        print("should return an entities and errors when some requests fail and some succeed");
        // create an array of items with no _id and the second of them should have invalid _geoloc params
        // save using the array above
        // find using network store
    }

    @Test
    public void testSaveListReturnPutFailuresAtMatchingIndexAuto() throws InterruptedException {
        print("should return PUT failures at the matching index");
        // create an array  - [{no_id, invalid_geoloc},{_id}, {_id, invalid_geoloc}, {no_id}]
        // save using the array above
        // find using network store
    }

    @Test
    public void testPushItemsListWithoutIdAuto() throws InterruptedException {
        print("should use multi insert for multiple items without _id");
        // create an array of items without _id
        // save using the array above
        // push()
        // pendingSyncEntities()
        // find using syncstore
    }

    @Test
    public void testPushItemsListCombineWithIdAndWithoutIdAuto() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array
        // push()
        // pendingSyncEntities()
        // find using syncstore
    }

    @Test
    public void testPushItemsListCombineWithIdAndWithoutIdMockedAuto() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id - mocked");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array
        // push()
        // pendingSyncEntities()
        // find using syncstore
    }

    @Test
    public void testPushItemsListReturnsErrorForEachItemAuto() throws InterruptedException {
        print("should return the failure reason in the result for each pushed item even if it is the same");
        // create an array of items without _id and set the collection permission for create to never
        // save() using the array
        // push()
        // pendingSyncEntities()
        // find using syncstore
    }

    @Test
    public void testPushItemsListReturnsErrorsForEachItemErrorAuto() throws InterruptedException {
        print("should return the failure reason in the result for each pushed item when they are different");
        // create an array with 3 items without _id, two of which should be invalid for different reasons
        // save using the array above
        // push()
        // pendingSyncEntities()
        // find using syncstore
    }

    @Test
    public void testPushShouldUseMultiInsertAfterSaveAuto() throws InterruptedException {
        print("should use multi-insert even if the items have not been created in an array");
        // save an item without _id
        // save another item without _id
        // push()
        // pendingSyncEntities()
        // find using syncstore
    }

    @Test
    public void testSyncItemsListAuto() throws InterruptedException {
        print("test sync items list");
        // create an array of 3 items, the second of which has invalid _geoloc parameters
        // save() mocking connectivity error
        // Sync()
        // pendingSyncEntities()
        // find() using networkstore
        // find using syncstore
    }
}
