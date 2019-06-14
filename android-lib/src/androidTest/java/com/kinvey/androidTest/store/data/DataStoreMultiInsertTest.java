package com.kinvey.androidTest.store.data;

import android.content.Context;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyReadCallback;
import com.kinvey.android.model.User;
import com.kinvey.android.store.DataStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.android.sync.KinveyPushResponse;
import com.kinvey.android.sync.KinveySyncCallback;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.model.EntitySet;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Logger;
import com.kinvey.java.Query;
import com.kinvey.java.core.AbstractKinveyClient;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.KinveyJsonResponseException;
import com.kinvey.java.model.KinveyPullResponse;
import com.kinvey.java.model.KinveyReadResponse;
import com.kinvey.java.store.StoreType;
import com.kinvey.java.sync.dto.SyncItem;
import com.kinvey.java.sync.dto.SyncRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.Console;
import java.io.IOException;
import java.lang.reflect.Field;
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

    private static class DefaultKinveyClientCallback<T extends GenericJson> implements KinveyClientCallback<T> {

        private CountDownLatch latch;
        T result;
        Throwable error;

        DefaultKinveyClientCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(T result) {
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

    private static class DefaultKinveyClientListCallback<T extends GenericJson> implements KinveyClientCallback<List<T>> {

        private CountDownLatch latch;
        List<T> result;
        Throwable error;

        DefaultKinveyClientListCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(List<T> result) {
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

    private static class DefaultKinveyReadCallback<T extends GenericJson> implements KinveyReadCallback<T> {

        private CountDownLatch latch;
        KinveyReadResponse<T> result;
        Throwable error;

        DefaultKinveyReadCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(KinveyReadResponse<T> result) {
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

    private static class DefaultKinveyPushCallback implements KinveyPushCallback {

        private CountDownLatch latch;
        KinveyPushResponse result;
        Throwable error;

        DefaultKinveyPushCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(KinveyPushResponse result) {
            this.result = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        @Override
        public void onProgress(long current, long all) {

        }

        void finish() {
            latch.countDown();
        }
    }

    private static class DefaultKinveySyncCallback implements KinveySyncCallback {

        private CountDownLatch latch;
        KinveyPushResponse kinveyPushResponse;
        KinveyPullResponse kinveyPullResponse;
        Throwable error;

        DefaultKinveySyncCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(KinveyPushResponse kinveyPushResponse, KinveyPullResponse kinveyPullResponse) {
            this.kinveyPushResponse = kinveyPushResponse;
            this.kinveyPullResponse = kinveyPullResponse;
            finish();
        }

        @Override
        public void onPullStarted() {

        }

        @Override
        public void onPushStarted() {

        }

        @Override
        public void onPullSuccess(KinveyPullResponse kinveyPullResponse) {
            this.kinveyPullResponse = kinveyPullResponse;
        }

        @Override
        public void onPushSuccess(KinveyPushResponse kinveyPushResponse) {
            this.kinveyPushResponse = kinveyPushResponse;
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

    private <T extends GenericJson> DefaultKinveyDeleteCallback delete(final DataStore<T> store, final Query query) throws InterruptedException {
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

    private <T extends GenericJson> DefaultKinveyReadCallback find(final DataStore<T> store, int seconds) throws InterruptedException {
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

    private DefaultKinveySyncCallback sync(final DataStore<Person> store, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveySyncCallback callback = new DefaultKinveySyncCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.sync(callback);
            }
        });
        looperThread.start();
        latch.await(seconds, TimeUnit.SECONDS);
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private <T extends GenericJson> void clearBackend(DataStore<T> store) throws InterruptedException {
        Query query = client.query();
        query = query.notEqual("age", "100500");
        DefaultKinveyDeleteCallback deleteCallback = delete(store, query);
    }

    private List<SyncItem> pendingSyncEntities(String collectionName) {
        return client.getSyncManager().popSingleItemQueue(collectionName);
    }

    private void print(String msg) {
        Logger.DEBUG(msg);
        Console con = System.console();
        if (con != null) {
            con.printf(msg);
        }
    }

    private void createAndSavePerson(final DataStore<Person> store, String username) throws InterruptedException {
        Person person = createPerson(username);
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
    }

    private <T extends GenericJson> DefaultKinveyClientListCallback saveList(final DataStore<T> store, final List<T> persons) throws InterruptedException {
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

    private <T extends GenericJson> DefaultKinveyClientCallback save(final DataStore<T> store, final T item) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.save(item, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private <T extends GenericJson> DefaultKinveyPushCallback push(final DataStore<T> store, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyPushCallback callback = new DefaultKinveyPushCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.push(callback);
            }
        });
        looperThread.start();
        latch.await(seconds, TimeUnit.SECONDS);
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private void mockInvalidConnection() {
        Field field = null;
        try {
            field = AbstractKinveyClient.class.getDeclaredField("rootUrl");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        assert field != null;
        field.setAccessible(true);
        try {
            field.set(client, "https://bmock.kinvey.com/");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void cancelMockInvalidConnection() {
        Field field = null;
        try {
            field = AbstractKinveyClient.class.getDeclaredField("rootUrl");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        assert field != null;
        field.setAccessible(true);
        try {
            field.set(client, AbstractClient.DEFAULT_BASE_URL);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    // create an array with a few items that have _id property or have not.
    private List<Person> createPersonsList(int count, boolean withId) {
        List<Person> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Person person = createPerson(TEST_USERNAME + String.valueOf(i));
            if (withId) {
                String id = "123456" + i;
                person.setId(id);
            }
            items.add(person);
        }
        return items;
    }

    private List<Person> createPersonsList(boolean withId) {
        return createPersonsList(MAX_ENTITY_COUNT, withId);
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
    private List<Person> createPushErrList() {
        List<Person> items = new ArrayList<>();
        String errStr = ERR_GEOLOC;
        Person person1 = new Person(TEST_USERNAME + String.valueOf(1));
        person1.setGeoloc(errStr);
        items.add(person1);
        Person person2 = new Person(TEST_USERNAME + String.valueOf(2));
        person2.setGeoloc(errStr);
        items.add(person2);
        Person person3 = new Person(TEST_USERNAME + String.valueOf(3));
        items.add(person3);
        return items;
    }

    // create an array containing two items failing for different reasons
    private List<Person> createErrList() {
        List<Person> items = new ArrayList<>();
        String errStr = ERR_GEOLOC;
        Person person1 = new Person(TEST_USERNAME + String.valueOf(1));
        person1.setGeoloc(errStr);
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

    private void testSaveWithoutId(StoreType storeType) throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        clearBackend(store);
        client.getSyncManager().clear(Person.COLLECTION);

        createAndSavePerson(store, TEST_USERNAME);

        if (storeType == StoreType.SYNC) {
            List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
            assertNotNull(syncItems);
            assertEquals(syncItems.size(), 1);
        }

        DefaultKinveyReadCallback findCallback = find(store, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        List<Person> list = findCallback.result.getResult();
        assertTrue(list.size() == 1);
        assertTrue(list.get(0).getId() != null);
    }

    private void testSaveWithId(StoreType storeType) throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        clearBackend(store);
        client.getSyncManager().clear(Person.COLLECTION);

        String id = "123456";
        Person person = createPerson(TEST_USERNAME);
        person.setId(id);

        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        Person result = (Person) saveCallback.result;
        assertEquals(result.getId(), id);

        if (storeType == StoreType.SYNC) {
            List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
            assertNotNull(syncItems);
            assertEquals(syncItems.size(), 1);
        }

        DefaultKinveyReadCallback findCallback = find(store, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        List<Person> list = findCallback.result.getResult();
        assertTrue(list.size() == 1);
        assertTrue(list.get(0).getId() != null);
    }

    private <T extends GenericJson> void testSaveLocally(T item, Class<T> cls, String collection, StoreType storeType) throws InterruptedException {

        DataStore<T> store = DataStore.collection(collection, cls, storeType, client);
        clearBackend(store);
        client.getSyncManager().clear(Person.COLLECTION);

        mockInvalidConnection();

        DefaultKinveyClientCallback saveCallback = save(store, item);
        //assertNotNull(saveCallback.result);
        //assertNull(saveCallback.error);

        DefaultKinveyReadCallback findCallback = find(store, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        List<Person> list = findCallback.result.getResult();
        assertEquals(1, list.size());
        assertNotNull(list.get(0).getId());

        cancelMockInvalidConnection();
    }

    private SyncItem testSaveLocallyIfNetworkError(Person person, StoreType storeType) throws InterruptedException {

        testSaveLocally(person, Person.class, Person.COLLECTION, storeType);

        List<SyncItem> pendingList = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(pendingList);
        assertEquals(1, pendingList.size());
        assertNotNull(pendingList.get(0));
        return pendingList.get(0);
    }

    private void testSaveList(List<Person> personList, int checkCount, StoreType storeType) throws InterruptedException {
        DataStore<Person> personStore = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        DataStore<Person> syncStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);

        clearBackend(personStore);
        clearBackend(syncStore);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallback = saveList(personStore, personList);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertEquals(saveCallback.result.size(), personList.size());

        if (storeType == StoreType.SYNC || storeType == StoreType.AUTO) {

            if (storeType == StoreType.SYNC) {
                List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
                int errCount = personList.size() - checkCount;
                int count = errCount == 0 ? personList.size() : errCount;
                assertNotNull(syncItems);
                assertEquals(syncItems.size(), count);
            }

            DefaultKinveyReadCallback findCallback = find(syncStore, LONG_TIMEOUT);
            assertNotNull(findCallback.result);
            assertEquals(findCallback.result.getResult().size(), personList.size());
        }

        DefaultKinveyReadCallback findCallback = find(personStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertEquals(findCallback.result.getResult().size(), personList.size());
    }

    private void testSaveListWithId(StoreType storeType) throws InterruptedException {
        List<Person> personList = createPersonsList(true);
        testSaveList(personList, personList.size(), storeType);
    }

    private void testSaveListWithoutId(StoreType storeType) throws InterruptedException {
        List<Person> personList = createPersonsList(false);
        testSaveList(personList, personList.size(), storeType);
    }

    private <T extends GenericJson> void testSaveEmptyList(List<T> list, Class<T> cls, String collection, StoreType storeType) throws InterruptedException {
        DataStore<T> personStore = DataStore.collection(collection, cls, storeType, client);
        clearBackend(personStore);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallback = saveList(personStore, list);
        assertNull(saveCallback.result);
        assertNotNull(saveCallback.error);

        if (storeType == StoreType.AUTO) {
            List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
            assertNotNull(syncItems);
            assertEquals(syncItems.size(), list.size());
        }
    }

    private <T extends GenericJson> void testSaveListNoAccessErr(List<T> list, Class<T> cls, String collection, StoreType storeType) throws InterruptedException {
        DataStore<T> store = DataStore.collection(collection, cls, storeType, client);
        clearBackend(store);

        DefaultKinveyClientListCallback defaultKinveyListCallback = saveList(store, list);
        assertNotNull(defaultKinveyListCallback.error);
        assertEquals(defaultKinveyListCallback.error.getClass(), KinveyJsonResponseException.class);

        DefaultKinveyClientListCallback saveCallback = saveList(store, list);
        assertNull(saveCallback.result);
        assertNotNull(saveCallback.error);
    }

    private <T extends GenericJson> void testSaveListWithErr(List<T> list, int checkCount, Class<T> cls, String collection, StoreType storeType) throws InterruptedException {
        DataStore<T> personStore = DataStore.collection(collection, cls, storeType, client);
        DataStore<T> netStore = DataStore.collection(collection, cls, StoreType.NETWORK, client);
        clearBackend(personStore);

        DefaultKinveyClientListCallback saveCallback = saveList(personStore, list);
        assertNull(saveCallback.result);
        assertNotNull(saveCallback.error);

        DefaultKinveyReadCallback findCallback = find(netStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertEquals(findCallback.result.getResult().size(), checkCount);
    }


    //TODO: Add improvements to support multi-insert
    private <T extends GenericJson> void testPushItemsList(List<T> list, int checkCount, Class<T> cls, String collection, StoreType storeType) throws InterruptedException {
        DataStore<T> personStore = DataStore.collection(collection, cls, storeType, client);
        DataStore<T> storeSync = DataStore.collection(collection, cls, StoreType.SYNC, client);
        DataStore<T> storeNetwork = DataStore.collection(collection, cls, StoreType.NETWORK, client);

        clearBackend(personStore);
        clearBackend(storeSync);
        clearBackend(storeNetwork);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallbackSecond = saveList(storeSync, list);
        assertNotNull(saveCallbackSecond.result);
        assertNull(saveCallbackSecond.error);

        DefaultKinveyPushCallback pushCallback = push(personStore, LONG_TIMEOUT);
        assertNotNull(pushCallback.result);
        //assertEquals(pushCallback.result.getSuccessCount(), checkCount);

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        int errCount = list.size() - checkCount;
        assertEquals(syncItems.size(), errCount);

        DefaultKinveyReadCallback findCallback = find(storeNetwork, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertEquals(findCallback.result.getResult().size(), checkCount);
    }

    //TODO: Add improvements to support multi-insert
    private <T extends GenericJson> void testPushItemsListError(List<T> list, int checkCount, Class<T> cls, String collection, StoreType storeType) throws InterruptedException {
        DataStore<T> personStore = DataStore.collection(collection, cls, storeType, client);
        DataStore<T> storeSync = DataStore.collection(collection, cls, StoreType.SYNC, client);

        clearBackend(personStore);
        clearBackend(storeSync);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallbackSecond = saveList(storeSync, list);
        assertNotNull(saveCallbackSecond.result);
        assertNull(saveCallbackSecond.error);

        DefaultKinveyPushCallback pushCallback = push(personStore, LONG_TIMEOUT);
        assertNotNull(pushCallback.error);

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        int errCount = list.size() - checkCount;
        assertTrue(syncItems.size() == errCount || syncItems.isEmpty());

        DefaultKinveyReadCallback findCallback = find(storeSync, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertEquals(findCallback.result.getResult().size(), checkCount);
    }

    //TODO: Add improvements to support multi-insert
    private <T extends GenericJson> void testPushMultiInsertSupport(List<T> list, int checkCount, Class<T> cls, String collection, StoreType storeType) throws InterruptedException {
        DataStore<T> personStore = DataStore.collection(collection, cls, storeType, client);
        DataStore<T> storeSync = DataStore.collection(collection, cls, StoreType.SYNC, client);
        DataStore<T> storeNetwork = DataStore.collection(collection, cls, StoreType.NETWORK, client);

        clearBackend(storeSync);
        clearBackend(storeNetwork);
        client.getSyncManager().clear(Person.COLLECTION);

        for (T item : list) {
            save(storeSync, item);
        }

        DefaultKinveyPushCallback pushCallback = push(personStore, LONG_TIMEOUT);
        assertNotNull(pushCallback.result);
        assertEquals(pushCallback.result.getSuccessCount(), checkCount);

        DefaultKinveyReadCallback findCallback = find(storeNetwork, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertEquals(findCallback.result.getResult().size(), checkCount);

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertTrue(syncItems == null || syncItems.isEmpty());
    }

    //TODO: Add improvements to support multi-insert
    private void testSyncItemsList(List<Person> list, int checkCount, boolean mockConnectionErr, StoreType storeType) throws InterruptedException {

        DataStore<Person> personStoreCurrent = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        DataStore<Person> personStoreNet = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        DataStore<Person> personStoreSync = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);

        clearBackend(personStoreNet);
        clearBackend(personStoreSync);
        client.getSyncManager().clear(Person.COLLECTION);

        if (mockConnectionErr) { mockInvalidConnection(); }

        DefaultKinveyClientListCallback saveCallback = saveList(personStoreSync, list);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);

        if (mockConnectionErr) { cancelMockInvalidConnection(); }

        sync(personStoreCurrent, LONG_TIMEOUT);

        int errCount = list.size() - checkCount;

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        assertEquals(syncItems.size(), errCount);

        DefaultKinveyReadCallback findCallbackNet = find(personStoreNet, LONG_TIMEOUT);
        DefaultKinveyReadCallback findCallbackSync = find(personStoreSync, LONG_TIMEOUT);

        assertNotNull(findCallbackNet.result);
        assertEquals(findCallbackNet.result.getResult().size(), checkCount);

        assertNotNull(findCallbackSync.result);
        assertEquals(findCallbackSync.result.getResult().size(), list.size());
    }

    // NETWORK STORE

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
        testSaveList(list, 2, StoreType.NETWORK);
    }

    @Test
    public void testSaveListReturnErrorForEmptyListNetwork() throws InterruptedException {
        print("should return an error for an empty array");
        // create an empty array
        // save() using the array
        List<Person> list = new ArrayList<>();
        testSaveEmptyList(list, Person.class, Person.COLLECTION, StoreType.NETWORK);
    }

    @Test
    public void testSaveListReturnErrorForInvalidCredentialsNetwork() throws InterruptedException {
        print("should return an error when all items fail with multi-insert for invalid credentials");
        // create an array with a few items that have no _id property
        // set a collection permission to deny creating items
        // save() using the array from above
        List<EntitySet> entityList = createEntityList();
        testSaveListNoAccessErr(entityList, EntitySet.class, EntitySet.COLLECTION, StoreType.NETWORK);
    }

    @Test
    public void testSaveListReturnErrorArrayForAllItemsFailNetwork() throws InterruptedException {
        print("should return an array of errors for all items failing for different reasons");
        // create an array containing two items failing for different reasons
        // save using the array above
        List<Person> personList = createErrList();
        testSaveListWithErr(personList, 0, Person.class, Person.COLLECTION, StoreType.NETWORK);
    }

    @Test
    public void testSaveListReturnErrorArrayForSomeItemsFailNetwork() throws InterruptedException {
        print("should return an entities and errors when some requests fail and some succeed");
        // create an array of items with no _id and the second of them should have invalid _geoloc params
        // save using the array above
        // find using network store
        List<Person> personList = createErrList1();
        testSaveListWithErr(personList, 1, Person.class, Person.COLLECTION, StoreType.NETWORK);
    }

    @Test
    public void testSaveListReturnPutFailuresAtMatchingIndexNetwork() throws InterruptedException {
        print("should return PUT failures at the matching index");
        // create an array  - [{no_id, invalid_geoloc},{_id}, {_id, invalid_geoloc}, {no_id}]
        // save using the array above
        // find using network store
        List<Person> personList = createErrListGeoloc();
        testSaveListWithErr(personList, 2, Person.class, Person.COLLECTION, StoreType.NETWORK);
    }

    // SYNC STORE

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
        testSaveListWithoutId(StoreType.SYNC);
    }

    @Test
    public void testSaveListWithIdSync() throws InterruptedException {
        print("should save an array of items with _id");
        // create an array with a few items that have _id property
        // save() with the array as param
        // pendingSyncEntities()
        // find() using syncstore
        testSaveListWithId(StoreType.SYNC);
    }

    @Test
    public void testSaveListCombineWithIdAndWithoutIdSync() throws InterruptedException {
        print("should save and array of items with and without _id");
        // create an array that has 2 items with _id and 2 without
        // save() using the array
        // pendingSyncEntities()
        // find() using syncstore
        List<Person> list = createCombineList();
        testSaveList(list, list.size(), StoreType.SYNC);
    }

    @Test
    public void testSaveListReturnErrorForEmptyListSync() throws InterruptedException {
        print("should return an error for an empty array");
        // create an empty array
        // save() using the array
        List<Person> list = new ArrayList<>();
        testSaveEmptyList(list, Person.class, Person.COLLECTION, StoreType.SYNC);
    }

    @Test
    public void testPushItemsListWithoutIdSync() throws InterruptedException {
        print("should use multi insert for multiple items without _id");
        // create an array of items without _id
        // save using the array above
        // push()
        // pendingSyncEntities()
        // find using syncstore
        List<Person> personsList = createPersonsList(false);
        testPushItemsList(personsList, personsList.size(), Person.class, Person.COLLECTION, StoreType.SYNC);
    }

    @Test
    public void testPushItemsListCombineWithIdAndWithoutIdSync() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array
        // push()
        // pendingSyncEntities()
        // find using syncstore
        List<Person> personsList = createCombineList();
        testPushItemsList(personsList, personsList.size(), Person.class, Person.COLLECTION, StoreType.SYNC);
    }

    @Test
    public void testPushItemsListCombineWithIdAndWithoutIdMockedSync() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id - mocked");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array
        // push()
        // pendingSyncEntities()
        // find using syncstore
        List<Person> personsList = createCombineList();
        testPushItemsList(personsList, personsList.size(), Person.class, Person.COLLECTION, StoreType.SYNC);
    }

    @Test
    public void testPushItemsListReturnsErrorForEachItemSync() throws InterruptedException {
        print("should return the failure reason in the result for each pushed item even if it is the same");
        // create an array of items without _id and set the collection permission for create to never
        // save() using the array
        // push()
        // pendingSyncEntities()
        // find using syncstore
        List<EntitySet> entitySetList = createEntityList();
        testPushItemsListError(entitySetList, 0, EntitySet.class, EntitySet.COLLECTION, StoreType.SYNC);
    }

    @Test
    public void testPushItemsListReturnsErrorsForEachItemErrorSync() throws InterruptedException {
        print("should return the failure reason in the result for each pushed item when they are different");
        // create an array with 3 items without _id, two of which should be invalid for different reasons
        // save using the array above
        // push()
        // pendingSyncEntities()
        // find using syncstore
        List<Person> personList = createPushErrList();
        testPushItemsList(personList, 0, Person.class, Person.COLLECTION, StoreType.SYNC);
    }

    @Test
    public void testPushShouldUseMultiInsertAfterSaveSync() throws InterruptedException {
        print("should use multi-insert even if the items have not been created in an array");
        // save an item without _id
        // save another item without _id
        // push()
        // pendingSyncEntities()
        // find using syncstore
        int itemsCount = 2;
        List<Person> list = createPersonsList(itemsCount, false);
        testPushMultiInsertSupport(list, itemsCount, Person.class, Person.COLLECTION, StoreType.SYNC);
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
        List<Person> personList = createErrListGeoloc();
        testSyncItemsList(personList, 3, false, StoreType.SYNC);
    }

    // AUTO STORE

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

        Person person = new Person(TEST_USERNAME);
        person.setGeoloc(ERR_GEOLOC);

        SyncItem item = testSaveLocallyIfNetworkError(person, StoreType.AUTO);
        assertEquals(SyncRequest.HttpVerb.POST, item.getRequestMethod());
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
        String testId = "TEST_ID_123";
        Person person = new Person("testId", TEST_USERNAME);
        person.setGeoloc(ERR_GEOLOC);

        SyncItem item = testSaveLocallyIfNetworkError(person, StoreType.AUTO);
        assertEquals(testId, item.getEntityID().id);
        assertEquals(SyncRequest.HttpVerb.PUT, item.getHttpVerb());
    }

    @Test
    public void testSaveListWithoutIdAuto() throws InterruptedException {
        print("should send POST multi-insert request for array of items with no _id");
        // create an array with a few items that have no _id property
        // save() with the array as param
        // find() with syncstore
        // find() using networkstore
        testSaveListWithoutId(StoreType.AUTO);
    }

    @Test
    public void testSaveListWithIdAuto() throws InterruptedException {
        print("should sent PUT requests for an array of items with _id");
        // create an array with a few items that have _id property
        // save() with the array as param
        // find using network store

        testSaveListWithId(StoreType.AUTO);
    }

    @Test
    public void testSaveListCombineWithIdAndWithoutIdAuto() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array
        // find() with syncstore
        // find() using networkstore

        List<Person> list = createCombineList();

        DataStore<EntitySet> syncStore = DataStore.collection(EntitySet.COLLECTION, EntitySet.class, StoreType.SYNC, client);
        clearBackend(syncStore);
        client.getSyncManager().clear(Person.COLLECTION);

        testSaveList(list, list.size(), StoreType.AUTO);

        DefaultKinveyReadCallback findCallback = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertEquals(findCallback.result.getResult().size(), list.size());
    }

    @Test
    public void testSaveListReturnErrorForEmptyListAuto() throws InterruptedException {
        print("should return an error for an empty array");
        // create an empty array
        // save() using the array
        // pendingSyncEntities()
        List<Person> list = new ArrayList<>();
        testSaveEmptyList(list, Person.class, Person.COLLECTION, StoreType.AUTO);
    }

    @Test
    public void testSaveListReturnErrorForInvalidCredentialsAuto() throws InterruptedException {
        print("should return an error when all items fail with multi-insert for invalid credentials");
        // create an array with a few items that have no _id property
        // set a collection permission to deny creating items
        // save() using the array from above
        // pendingSyncEntities()
        // find() using syncstore

        List<EntitySet> entityList = createEntityList();

        DataStore<EntitySet> syncStore = DataStore.collection(EntitySet.COLLECTION, EntitySet.class, StoreType.SYNC, client);
        clearBackend(syncStore);
        client.getSyncManager().clear(Person.COLLECTION);

        testSaveListNoAccessErr(entityList, EntitySet.class, EntitySet.COLLECTION, StoreType.AUTO);

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        assertEquals(syncItems.size(), entityList.size());

        DefaultKinveyReadCallback findCallback = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertEquals(findCallback.result.getResult().size(), entityList.size());
    }

    @Test
    public void testSaveListReturnErrorArrayForAllItemsFailAuto() throws InterruptedException {
        print("should return an array of errors for all items failing for different reasons");
        // create an array containing two items failing for different reasons
        // save using the array above
        // find using syncstore
        // pendingSyncEntities()
        List<Person> personList = createErrList();

        DataStore<EntitySet> syncStore = DataStore.collection(EntitySet.COLLECTION, EntitySet.class, StoreType.SYNC, client);
        clearBackend(syncStore);
        client.getSyncManager().clear(Person.COLLECTION);

        testSaveListWithErr(personList, 1, Person.class, Person.COLLECTION, StoreType.AUTO);

        DefaultKinveyReadCallback findCallback = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertEquals(findCallback.result.getResult().size(), personList.size());

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        assertEquals(syncItems.size(), personList.size());
    }

    @Test
    public void testSaveListReturnErrorArrayForSomeItemsFailAuto() throws InterruptedException {
        print("should return an entities and errors when some requests fail and some succeed");
        // create an array of items with no _id and the second of them should have invalid _geoloc params
        // save using the array above
        // pendingSyncEntities()
        // find() using syncstore
        // find() using networkstore
        List<Person> personList = createErrList1();

        DataStore<EntitySet> syncStore = DataStore.collection(EntitySet.COLLECTION, EntitySet.class, StoreType.SYNC, client);
        clearBackend(syncStore);
        client.getSyncManager().clear(Person.COLLECTION);

        testSaveListWithErr(personList, 1, Person.class, Person.COLLECTION, StoreType.AUTO);

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        assertEquals(syncItems.size(), personList.size());

        DefaultKinveyReadCallback findCallback = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertEquals(findCallback.result.getResult().size(), personList.size());
    }

    @Test
    public void testSaveListReturnPutFailuresAtMatchingIndexAuto() throws InterruptedException {
        print("should return PUT failures at the matching index");
        // create an array  - [{no_id, invalid_geoloc},{_id}, {_id, invalid_geoloc}, {no_id}]
        // save using the array above
        // pendingSyncEntities()
        // find() using syncstore
        List<Person> personList = createErrListGeoloc();

        DataStore<EntitySet> syncStore = DataStore.collection(EntitySet.COLLECTION, EntitySet.class, StoreType.SYNC, client);
        clearBackend(syncStore);
        client.getSyncManager().clear(Person.COLLECTION);

        testSaveListWithErr(personList, 2, Person.class, Person.COLLECTION, StoreType.AUTO);

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        assertEquals(syncItems.size(), personList.size());

        DefaultKinveyReadCallback findCallback = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertEquals(findCallback.result.getResult().size(), personList.size());
    }

    @Test
    public void testPushItemsListWithoutIdAuto() throws InterruptedException {
        print("should use multi insert for multiple items without _id");
        // create an array of items without _id
        // save using the array above
        // push()
        // pendingSyncEntities()
        // find using syncstore
        List<Person> personsList = createPersonsList(false);
        testPushItemsList(personsList, personsList.size(), Person.class, Person.COLLECTION, StoreType.AUTO);
    }

    @Test
    public void testPushItemsListCombineWithIdAndWithoutIdAuto() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array
        // push()
        // pendingSyncEntities()
        // find using syncstore
        List<Person> personsList = createCombineList();
        testPushItemsList(personsList, personsList.size(), Person.class, Person.COLLECTION, StoreType.AUTO);
    }

    @Test
    public void testPushItemsListCombineWithIdAndWithoutIdMockedAuto() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id - mocked");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array
        // push()
        // pendingSyncEntities()
        // find using syncstore
        List<Person> personsList = createCombineList();
        testPushItemsList(personsList, personsList.size(), Person.class, Person.COLLECTION, StoreType.AUTO);
    }

    @Test
    public void testPushItemsListReturnsErrorForEachItemAuto() throws InterruptedException {
        print("should return the failure reason in the result for each pushed item even if it is the same");
        // create an array of items without _id and set the collection permission for create to never
        // save() using the array
        // push()
        // pendingSyncEntities()
        // find using syncstore
        List<EntitySet> entitySetList = createEntityList();
        testPushItemsListError(entitySetList, 0, EntitySet.class, EntitySet.COLLECTION, StoreType.AUTO);
    }

    @Test
    public void testPushItemsListReturnsErrorsForEachItemErrorAuto() throws InterruptedException {
        print("should return the failure reason in the result for each pushed item when they are different");
        // create an array with 3 items without _id, two of which should be invalid for different reasons
        // save using the array above
        // push()
        // pendingSyncEntities()
        // find using syncstore
        List<Person> personList = createPushErrList();
        testPushItemsList(personList, 0, Person.class, Person.COLLECTION, StoreType.AUTO);
    }

    @Test
    public void testPushShouldUseMultiInsertAfterSaveAuto() throws InterruptedException {
        print("should use multi-insert even if the items have not been created in an array");
        // save an item without _id
        // save another item without _id
        // push()
        // pendingSyncEntities()
        // find using syncstore
        int itemsCount = 2;
        List<Person> list = createPersonsList(itemsCount, false);
        testPushMultiInsertSupport(list, itemsCount, Person.class, Person.COLLECTION, StoreType.AUTO);
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
        List<Person> personList = createErrListGeoloc();
        testSyncItemsList(personList, 3, true, StoreType.AUTO);
    }
}
