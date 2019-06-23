package com.kinvey.androidTest.store.data;

import android.content.Context;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

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
import com.kinvey.androidTest.network.MockMultiInsertNetworkManager;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Constants;
import com.kinvey.java.KinveySaveBunchException;
import com.kinvey.java.Logger;
import com.kinvey.java.Query;
import com.kinvey.java.core.AbstractKinveyClient;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.KinveyJsonResponseException;
import com.kinvey.java.model.KinveyBatchInsertError;
import com.kinvey.java.model.KinveyPullResponse;
import com.kinvey.java.model.KinveyReadResponse;
import com.kinvey.java.store.StoreType;
import com.kinvey.java.sync.dto.SyncItem;
import com.kinvey.java.sync.dto.SyncRequest;

import org.junit.Before;
import org.junit.Ignore;
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
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DataStoreMultiInsertTest {

    private static final String TEST_USERNAME = "Test_UserName";
    private static final int DEFAULT_TIMEOUT = 60;
    private static final int LONG_TIMEOUT = 6 * DEFAULT_TIMEOUT;
    private static final int MAX_PERSONS_COUNT = 5;
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
        Logger.INFO(msg);
        Console con = System.console();
        if (con != null) {
            con.printf(msg);
        }
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

    private boolean isBackendItem(GenericJson item) {
        return item.containsKey(Constants._KMD) && item.containsKey(Constants._ACL);
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
        return createPersonsList(MAX_PERSONS_COUNT, withId);
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
        Person person2 = new Person(TEST_USERNAME + String.valueOf(2));
        person2.setGeoloc(errStr);
        items.add(person2);
        return items;
    }

    // create an array of items with no _id and the second of them should have invalid _geoloc params
    private List<Person> createErrList1() {
        List<Person> items = new ArrayList<>();
        Person person1 = new Person(TEST_USERNAME + String.valueOf(1));
        items.add(person1);
        Person person2 = new Person(TEST_USERNAME + String.valueOf(2));
        person2.setGeoloc(ERR_GEOLOC);
        items.add(person2);
        return items;
    }

    // create an array of items with no _id and the second of them should have invalid _geoloc params
    private List<Person> createErrList2() {
        List<Person> items = new ArrayList<>();
        Person person1 = new Person(TEST_USERNAME + String.valueOf(1));
        items.add(person1);
        Person person2 = new Person(TEST_USERNAME + String.valueOf(2));
        person2.setGeoloc(ERR_GEOLOC);
        items.add(person2);
        Person person3 = new Person(TEST_USERNAME + String.valueOf(3));
        items.add(person3);
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

    private List<EntitySet> createEntityList(int itemsCount) {
        List<EntitySet> items = new ArrayList<>();
        EntitySet entity = null;
        for (int i = 0; i < itemsCount; i++) {
            entity = new EntitySet();
            entity.setDescription("entity #" + i);
            items.add(entity);
        }
        return items;
    }

    private <T extends GenericJson> List<String> getResultCheckFields(List<T> resultList, String checkField) {
        List<String> result = new ArrayList<String>();
        for (GenericJson item : resultList) {
            result.add(item.get(checkField).toString());
        }
        return result;
    }

    private <T extends GenericJson> boolean checkIfItemsAtRightIndex(List<T> srcList, List<T> resultList,
                                             int[] checkIndexes, String checkField, boolean checkErr, boolean checkIsBackendItem) throws AssertionError {

        assertNotNull(srcList);
        assertNotNull(resultList);

        boolean result = true;
        GenericJson srcItem;
        GenericJson item;
        int curIdx = 0;
        List<String> resultFields = getResultCheckFields(resultList, checkField);
        for (int idx : checkIndexes) {
            item = resultList.get(curIdx);
            if (checkErr) {
                result &= item == null;
            } else {
                srcItem = srcList.get(idx);
                result &= item != null;
                if (checkIsBackendItem) {
                    result &= isBackendItem(item);
                }
                String srcField = srcItem.get(checkField).toString();
                result &= resultFields.contains(srcField);
            }
            curIdx++;
        }
        return result;
    }

    private <T extends GenericJson> boolean checkIfSameObjects(List<T> srcList, List<T> resultList, String checkField, boolean checkIsBackendItem) throws AssertionError {

        assertNotNull(srcList);
        assertNotNull(resultList);

        boolean result = true;
        GenericJson srcItem;
        GenericJson resultItem;
        List<String> resultFields = getResultCheckFields(resultList, checkField);
        for (int i = 0; i < srcList.size(); i++) {
            srcItem = srcList.get(i);
            resultItem = resultList.get(i);
            if (checkIsBackendItem) {
                result &= isBackendItem(resultItem);
            }
            String srcField = srcItem.get(checkField).toString();
            result &= resultFields.contains(srcField);
        }
        return result;
    }

    private boolean checkErrorsIndexes(List<KinveyBatchInsertError> errors, int[] checkIndexes) throws AssertionError {

        assertNotNull(errors);
        assertNotNull(checkIndexes);
        assertEquals(errors.size(), checkIndexes.length);

        boolean result = true;
        int curIdx = 0;
        KinveyBatchInsertError err;
        for (int idx : checkIndexes) {
            err = errors.get(curIdx);
            result &= idx == err.getIndex();
            curIdx++;
        }
        return result;
    }

    private <T extends GenericJson> boolean checkSyncItems(List<SyncItem> list, int checkCount, SyncItem.HttpVerb requestMethod) throws AssertionError {
        assertEquals(list.size(), checkCount);
        boolean result = true;
        for (SyncItem item : list) {
            result &= requestMethod.equals(item.getRequestMethod());
        }
        return result;
    }

    private boolean checkPersonIfSameObjects(List<Person> srcList, List<Person> resultList, boolean checkIsBackendItem) throws AssertionError {
        return checkIfSameObjects(srcList, resultList, Person.USERNAME_KEY, checkIsBackendItem);
    }

    private boolean checkPersonIfSameObjects(List<Person> srcList, List<Person> resultList) throws AssertionError {
        return checkIfSameObjects(srcList, resultList, Person.USERNAME_KEY, true);
    }

    private boolean checkIfPersonItemsAtRightIndex(List<Person> srcList, List<Person> resultList, int[] checkIndexes, boolean checkErr) {
        return checkIfItemsAtRightIndex(srcList, resultList, checkIndexes, Person.USERNAME_KEY, checkErr, true);
    }

    private <T extends GenericJson> void testSaveEmptyList(List<T> list, Class<T> cls, String collection, StoreType storeType) throws InterruptedException {
        DataStore<T> personStore = DataStore.collection(collection, cls, storeType, client);
        clearBackend(personStore);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallback = saveList(personStore, list);
        assertNull(saveCallback.result);
        Throwable error = saveCallback.error;
        assertNotNull(error);
        assertEquals(error.getClass(), IllegalStateException.class);
        assertTrue(error.getMessage().contains("Entity list cannot be empty"));
    }

    // save an item without _id
    // save another item without _id
    // push(), should use a multi-insert POST request and return the items with POST operations
    // pendingSyncEntities(), should return an empty array
    // find using syncstore, should return all items from step 1
    private void testPushMultiInsertSupport(StoreType storeType) throws InterruptedException {

        List<Person> personsList = createPersonsList(2, false);

        MockMultiInsertNetworkManager<Person> netManager = new MockMultiInsertNetworkManager<Person>(Person.COLLECTION, Person.class, client);
        DataStore<Person> personStore = new DataStore<Person>(Person.COLLECTION, Person.class, client, storeType, netManager);
        DataStore<Person> storeSync = new DataStore<Person>(Person.COLLECTION, Person.class, client, StoreType.SYNC, netManager);

        clearBackend(personStore);
        clearBackend(storeSync);
        client.getSyncManager().clear(Person.COLLECTION);

        for (Person item : personsList) {
            save(storeSync, item);
        }

        netManager.clear();
        DefaultKinveyPushCallback pushCallback = push(personStore, LONG_TIMEOUT);
        assertNotNull(pushCallback.result);
        assertEquals(pushCallback.result.getSuccessCount(), personsList.size());
        assertTrue(netManager.useMultiInsertSave());

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertTrue(syncItems == null || syncItems.isEmpty());

        DefaultKinveyReadCallback findCallback = find(storeSync, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(checkPersonIfSameObjects(personsList, findCallback.result.getResult()));
    }

    // create an array of 3 items, the second of which has invalid _geoloc parameters
    // save()
    // Sync(), Should return error for not pushed item
    // pendingSyncEntities(), should return the item with invalid params
    // find() using networkstore, should return the valid items
    // find using syncstore, should return all items including the invalid one
    private void testSyncItemsList(boolean mockConnectionErr, StoreType storeType) throws InterruptedException {

        List<Person> personList = createErrList2();
        int[] checkIndexesSuccess = new int[] { 0, 2 };
        int[] checkIndexesErr = new int[] { 1 };
        DataStore<Person> personStoreCurrent = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        DataStore<Person> personStoreNet = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        DataStore<Person> personStoreSync = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);

        clearBackend(personStoreCurrent);
        clearBackend(personStoreNet);
        clearBackend(personStoreSync);
        client.getSyncManager().clear(Person.COLLECTION);

        if (mockConnectionErr) { mockInvalidConnection(); }
        saveList(personStoreSync, personList);
        if (mockConnectionErr) { cancelMockInvalidConnection(); }

        DefaultKinveySyncCallback syncCallback = sync(personStoreCurrent, LONG_TIMEOUT);
        assertNotNull(syncCallback.error);
        if (syncCallback.error instanceof KinveySaveBunchException) {
            assertTrue(checkIfPersonItemsAtRightIndex(personList, ((KinveySaveBunchException) syncCallback.error).getEntities(), checkIndexesSuccess, false));
            assertTrue(checkErrorsIndexes(((KinveySaveBunchException) syncCallback.error).getErrors(), checkIndexesErr));
        }

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        assertEquals(syncItems.size(), 1);

        DefaultKinveyReadCallback findCallbackNet = find(personStoreNet, LONG_TIMEOUT);
        DefaultKinveyReadCallback findCallbackSync = find(personStoreSync, LONG_TIMEOUT);

        assertNotNull(findCallbackNet.result);
        assertTrue(checkIfPersonItemsAtRightIndex(personList, findCallbackNet.result.getResult(), checkIndexesSuccess, false));

        assertNotNull(findCallbackSync.result);
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result.getResult()));
    }

    // NETWORK STORE

    @Test
    public void testSaveWithoutIdNetwork() throws InterruptedException {
        print("should send POST with a single item with no _id");
        // create an item that has no _id property
        // call save() with it
        // find using network store

        DataStore<Person> netStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(netStore);
        client.getSyncManager().clear(Person.COLLECTION);

        Person person = createPerson(TEST_USERNAME);
        DefaultKinveyClientCallback saveCallback = save(netStore, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        Person resultPerson = (Person) saveCallback.result;
        assertNotNull(resultPerson.getId());
        assertEquals(person.getUsername(), resultPerson.getUsername());

        DefaultKinveyReadCallback findCallback = find(netStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        List<Person> list = findCallback.result.getResult();
        assertEquals(1, list.size());
        Person findPerson = list.get(0);
        assertNotNull(findPerson);
        assertNotNull(findPerson.getId());
        assertEquals(person.getUsername(), findPerson.getUsername());
    }

    @Test
    public void testSaveWithIdNetwork() throws InterruptedException {
        print("should send PUT with a sngle item with _id");
        // create an item with _id property
        // call save() with it
        // find using network store

        DataStore<Person> netStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(netStore);
        client.getSyncManager().clear(Person.COLLECTION);

        String id = "123456";
        Person person = createPerson(TEST_USERNAME);
        person.setId(id);

        DefaultKinveyClientCallback saveCallback = save(netStore, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        Person resultPerson = (Person) saveCallback.result;
        assertNotNull(resultPerson);
        assertEquals(id, resultPerson.getId());

        DefaultKinveyReadCallback findCallback = find(netStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        List<Person> list = findCallback.result.getResult();
        assertEquals(1, list.size());
        Person findPerson = list.get(0);
        assertNotNull(findPerson);
        assertEquals(id, findPerson.getId());
    }

    @Test
    public void testSaveListWithoutIdNetwork() throws InterruptedException {
        print("should send POST multi-insert request for array of items with no _id");
        // create an array with a few items that have no _id property
        // save() with the array as param
        // find using network store

        List<Person> personList = createPersonsList(false);

        DataStore<Person> personStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(personStore);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallback = saveList(personStore, personList);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result);
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result));

        DefaultKinveyReadCallback findCallback = find(personStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(checkPersonIfSameObjects(personList, findCallback.result.getResult()));
    }

    @Test
    public void testSaveListWithIdNetwork() throws InterruptedException {
        print("should sent PUT requests for an array of items with _id");
        // create an array with a few items that have _id property
        // save() with the array as param
        // find using network store

        List<Person> personList = createPersonsList(true);

        DataStore<Person> personStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(personStore);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallback = saveList(personStore, personList);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result);
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result));

        DefaultKinveyReadCallback findCallback = find(personStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(checkPersonIfSameObjects(personList, findCallback.result.getResult()));
    }

    @Test
    public void testSaveListCombineWithIdAndWithoutIdNetwork() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no _id, _id]
        // save() using the array
        // find using network store

        List<Person> personList = createCombineList();

        DataStore<Person> personStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(personStore);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallback = saveList(personStore, personList);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result);
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result));

        DefaultKinveyReadCallback findCallback = find(personStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(checkPersonIfSameObjects(personList, findCallback.result.getResult()));
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
        List<EntitySet> entityList = createEntityList(2);

        DataStore<EntitySet> store = DataStore.collection(EntitySet.COLLECTION, EntitySet.class, StoreType.NETWORK, client);
        clearBackend(store);

        DefaultKinveyClientListCallback defaultKinveyListCallback = saveList(store, entityList);
        assertNotNull(defaultKinveyListCallback.error);
        assertEquals(defaultKinveyListCallback.error.getClass(), KinveyJsonResponseException.class);
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    public void testSaveListReturnErrorArrayForAllItemsFailNetwork() throws InterruptedException {
        print("should return an array of errors for all items failing for different reasons");
        // create an array containing two items failing for different reasons
        // save using the array above
        List<Person> personList = createErrList();
        int[] checkIndexes = new int[] {0, 1};

        DataStore<Person> personStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(personStore);

        DefaultKinveyClientListCallback saveCallback = saveList(personStore, personList);
        assertNotNull(saveCallback.error);
        if (saveCallback.error instanceof KinveySaveBunchException) {
            List<Person> resultEntities = ((KinveySaveBunchException) saveCallback.error).getEntities();
            List<KinveyBatchInsertError> errorsList = ((KinveySaveBunchException) saveCallback.error).getErrors();
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexes, true));
            assertTrue(checkErrorsIndexes(errorsList, checkIndexes));
        }
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    public void testSaveListReturnErrorArrayForSomeItemsFailNetwork() throws InterruptedException {
        print("should return an entities and errors when some requests fail and some succeed");
        // create an array of items with no _id and the second of them should have invalid _geoloc params
        // save using the array above
        // find using network store

        List<Person> personList = createErrList1();
        int[] checkIndexesSuccess = new int[] {0};
        int[] checkIndexesErr = new int[] {1};

        DataStore<Person> personStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(personStore);

        DefaultKinveyClientListCallback saveCallback = saveList(personStore, personList);
        assertNotNull(saveCallback.error);
        if (saveCallback.error instanceof KinveySaveBunchException) {
            List<Person> resultEntities = ((KinveySaveBunchException) saveCallback.error).getEntities();
            List<KinveyBatchInsertError> errorsList = ((KinveySaveBunchException) saveCallback.error).getErrors();
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexesSuccess, true));
            assertTrue(checkErrorsIndexes(errorsList, checkIndexesErr));
        }

        DefaultKinveyReadCallback findCallback = find(personStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(checkIfPersonItemsAtRightIndex(personList, findCallback.result.getResult(), checkIndexesSuccess, false));
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    public void testSaveListReturnPutFailuresAtMatchingIndexNetwork() throws InterruptedException {
        print("should return PUT failures at the matching index");
        // create an array  - [{no_id, invalid_geoloc},{_id}, {_id, invalid_geoloc}, {no_id}]
        // save using the array above
        // find using network store
        List<Person> personList = createErrListGeoloc();
        int[] checkIndexesSuccess = new int[] {1, 3};
        int[] checkIndexesErr = new int[] {0, 2};

        DataStore<Person> personStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(personStore);

        DefaultKinveyClientListCallback saveCallback = saveList(personStore, personList);
        assertNotNull(saveCallback.error);
        if (saveCallback.error instanceof KinveySaveBunchException) {
            List<Person> resultEntities = ((KinveySaveBunchException) saveCallback.error).getEntities();
            List<KinveyBatchInsertError> errorsList = ((KinveySaveBunchException) saveCallback.error).getErrors();
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexesSuccess, false));
            assertTrue(checkErrorsIndexes(errorsList, checkIndexesErr));
        }

        DefaultKinveyReadCallback findCallback = find(personStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(checkIfPersonItemsAtRightIndex(personList, findCallback.result.getResult(), checkIndexesSuccess, false));
    }

    // SYNC STORE

    @Test
    public void testSaveWithoutIdSync() throws InterruptedException {
        print("should send POST with a single item with no _id");
        // create an item that has no _id property
        // call save() with it
        // pendingSyncEntities()
        // find() using sync store

        DataStore<Person> syncStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        clearBackend(syncStore);
        client.getSyncManager().clear(Person.COLLECTION);

        Person person = createPerson(TEST_USERNAME);

        DefaultKinveyClientCallback saveCallback = save(syncStore, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        Person resultPerson = (Person) saveCallback.result;
        assertNotNull(resultPerson.getId());
        assertEquals(person.getUsername(), resultPerson.getUsername());

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        assertEquals(syncItems.size(), 1);
        assertEquals(syncItems.get(0).getRequestMethod(), SyncRequest.HttpVerb.POST);

        DefaultKinveyReadCallback findCallback = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        List<Person> list = findCallback.result.getResult();
        assertEquals(1, list.size());
        Person findPerson = list.get(0);
        assertNotNull(findPerson);
        assertNotNull(findPerson.getId());
        assertEquals(person.getUsername(), findPerson.getUsername());
    }

    @Test
    public void testSaveWithIdSync() throws InterruptedException {
        print("should send PUT with a single item with _id");
        // create an item with _id property
        // call save() with it
        // pendingSyncEntities()
        // find() using syncstore

        DataStore<Person> syncStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        clearBackend(syncStore);
        client.getSyncManager().clear(Person.COLLECTION);

        String id = "123456";
        Person person = createPerson(TEST_USERNAME);
        person.setId(id);

        DefaultKinveyClientCallback saveCallback = save(syncStore, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        Person resultPerson = (Person) saveCallback.result;
        assertNotNull(resultPerson);
        assertEquals(id, resultPerson.getId());

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        assertEquals(syncItems.size(), 1);
        assertEquals(syncItems.get(0).getRequestMethod(), SyncRequest.HttpVerb.PUT);

        DefaultKinveyReadCallback findCallback = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        List<Person> list = findCallback.result.getResult();
        assertEquals(1, list.size());
        Person findPerson = list.get(0);
        assertNotNull(findPerson);
        assertEquals(id, findPerson.getId());
    }

    @Test
    public void testSaveListWithoutIdSync() throws InterruptedException {
        print("should send save an array of items with no _id");
        // create an array with a few items that have no _id property
        // save() with the array as param
        // pendingSyncEntities()
        // find() using syncstore

        List<Person> personList = createPersonsList(false);

        DataStore<Person> syncStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);

        clearBackend(syncStore);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallback = saveList(syncStore, personList);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result);
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result, false));

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        assertTrue(checkSyncItems(syncItems, personList.size(), SyncRequest.HttpVerb.POST));

        DefaultKinveyReadCallback findCallbackSync = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallbackSync.result);
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result.getResult()));
    }

    @Test
    public void testSaveListWithIdSync() throws InterruptedException {
        print("should save an array of items with _id");
        // create an array with a few items that have _id property
        // save() with the array as param
        // pendingSyncEntities()
        // find() using syncstore

        List<Person> personList = createPersonsList(true);

        DataStore<Person> syncStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);

        clearBackend(syncStore);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallback = saveList(syncStore, personList);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result);
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result, false));

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        assertTrue(checkSyncItems(syncItems, personList.size(), SyncRequest.HttpVerb.PUT));

        DefaultKinveyReadCallback findCallbackSync = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallbackSync.result);
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result.getResult()));
    }

    @Test
    public void testSaveListCombineWithIdAndWithoutIdSync() throws InterruptedException {
        print("should save and array of items with and without _id");
        // create an array that has 2 items with _id and 2 without
        // save() using the array, should return the items from step 1
        // pendingSyncEntities(), should return the items with PUT and POST operations respectively
        // find() using syncstore, should return the items from step 1 with _ids of the items that were assigned
        List<Person> personList = createCombineList();

        DataStore<Person> syncStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);

        clearBackend(syncStore);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallback = saveList(syncStore, personList);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result);
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result, false));

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        assertEquals(syncItems.size(), personList.size());
        assertEquals(syncItems.get(0).getRequestMethod(), SyncRequest.HttpVerb.POST);
        assertEquals(syncItems.get(1).getRequestMethod(), SyncRequest.HttpVerb.PUT);
        assertEquals(syncItems.get(2).getRequestMethod(), SyncRequest.HttpVerb.POST);
        assertEquals(syncItems.get(3).getRequestMethod(), SyncRequest.HttpVerb.PUT);

        DefaultKinveyReadCallback findCallbackSync = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallbackSync.result);
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result.getResult()));
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
        // push(), should return an array of the objects pushed with the operation performed
        // pendingSyncEntities(), should return an empty array
        // find using syncstore, should return the items with ect and lmt
        List<Person> personsList = createPersonsList(false);

        DataStore<Person> storeSync = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        clearBackend(storeSync);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallbackSecond = saveList(storeSync, personsList);
        assertNull(saveCallbackSecond.error);
        assertNotNull(saveCallbackSecond.result);
        assertTrue(checkPersonIfSameObjects(personsList, saveCallbackSecond.result, false));

        DefaultKinveyPushCallback pushCallback = push(storeSync, LONG_TIMEOUT);
        assertNotNull(pushCallback.result);
        assertEquals(personsList.size(), pushCallback.result.getSuccessCount());

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertTrue(syncItems == null || syncItems.isEmpty());

        DefaultKinveyReadCallback findCallback = find(storeSync, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(checkPersonIfSameObjects(personsList, findCallback.result.getResult()));
    }

    @Test
    public void testPushItemsListCombineWithIdAndWithoutIdSync() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array
        // push(), should return and array of the items pushed with the respective operation - POST for no _id and PUT for _id
        // pendingSyncEntities(), should return an empty array
        // find using syncstore, should return the items with ect and lmt
        List<Person> personsList = createCombineList();

        DataStore<Person> storeSync = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        clearBackend(storeSync);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallbackSecond = saveList(storeSync, personsList);
        assertNull(saveCallbackSecond.error);
        assertNotNull(saveCallbackSecond.result);
        assertTrue(checkPersonIfSameObjects(personsList, saveCallbackSecond.result, false));

        DefaultKinveyPushCallback pushCallback = push(storeSync, LONG_TIMEOUT);
        assertNotNull(pushCallback.result);
        assertEquals(personsList.size(), pushCallback.result.getSuccessCount());

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertTrue(syncItems == null || syncItems.isEmpty());

        DefaultKinveyReadCallback findCallback = find(storeSync, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(checkPersonIfSameObjects(personsList, findCallback.result.getResult()));
    }

    @Test
    @Ignore("Fails for now, need improvements for push logic: https://kinvey.atlassian.net/browse/MLIBZ-3058")
    public void testPushItemsListCombineWithIdAndWithoutIdMockedSync() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id - mocked");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array
        // push(), should use a multi-insert POST request and separate PUT requests for the items with _id. Should return and array of the items pushed with the respective operation - POST for no _id and PUT for _id
        // pendingSyncEntities(), should return an empty array
        // find using syncstore, should return the items with ect and lmt
        List<Person> personsList = createCombineList();

        MockMultiInsertNetworkManager<Person> mockNetManager = new MockMultiInsertNetworkManager<Person>(Person.COLLECTION, Person.class, client);
        DataStore<Person> storeSync = new DataStore<Person>(Person.COLLECTION, Person.class, client, StoreType.SYNC, mockNetManager);
        clearBackend(storeSync);
        client.getSyncManager().clear(Person.COLLECTION);

        saveList(storeSync, personsList);

        DefaultKinveyPushCallback pushCallback = push(storeSync, LONG_TIMEOUT);
        assertNotNull(pushCallback.result);
        assertEquals(personsList.size(), pushCallback.result.getSuccessCount());
        assertTrue(mockNetManager.useMultiInsertSave());

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertTrue(syncItems == null || syncItems.isEmpty());

        DefaultKinveyReadCallback findCallback = find(storeSync, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(checkPersonIfSameObjects(personsList, findCallback.result.getResult(), true));
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    public void testPushItemsListReturnsErrorForEachItemSync() throws InterruptedException {
        print("should return the failure reason in the result for each pushed item even if it is the same");
        // create an array of items without _id and set the collection permission for create to never
        // save() using the array
        // push(), should return the items from step 1 each with a POST operation and an error property with value: the invalid credentials error
        // pendingSyncEntities(), should return all items from step 1
        // find using syncstore, should return the items from step 1

        List<EntitySet> entitySetList = createEntityList(2);
        int[] itemsErrorIndexes = new int[] { 0, 1 };

        DataStore<EntitySet> storeSync = DataStore.collection(EntitySet.COLLECTION, EntitySet.class, StoreType.SYNC, client);
        clearBackend(storeSync);
        client.getSyncManager().clear(EntitySet.COLLECTION);

        DefaultKinveyClientListCallback saveListCallback = saveList(storeSync, entitySetList);
        assertNotNull(saveListCallback.result);
        assertTrue(checkIfItemsAtRightIndex(entitySetList, saveListCallback.result,
                itemsErrorIndexes, EntitySet.DESCRIPTION_KEY, false, false));

        DefaultKinveyPushCallback pushCallback = push(storeSync, LONG_TIMEOUT);
        assertNotNull(pushCallback.error);
        if (pushCallback.error instanceof KinveySaveBunchException) {
            assertTrue(checkIfItemsAtRightIndex(entitySetList, ((KinveySaveBunchException) pushCallback.error).getEntities(),
                    itemsErrorIndexes, EntitySet.DESCRIPTION_KEY, true, false));
            assertTrue(checkErrorsIndexes(((KinveySaveBunchException) pushCallback.error).getErrors(), itemsErrorIndexes));
        }

        List<SyncItem> syncItems = pendingSyncEntities(EntitySet.COLLECTION);
        assertNotNull(syncItems);
        assertEquals(syncItems.size(), entitySetList.size());

        DefaultKinveyReadCallback findCallback = find(storeSync, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        checkIfSameObjects(entitySetList, findCallback.result.getResult(), EntitySet.DESCRIPTION_KEY, false);
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    public void testPushItemsListReturnsErrorsForEachItemErrorSync() throws InterruptedException {
        print("should return the failure reason in the result for each pushed item when they are different");
        // create an array with 3 items without _id, two of which should be invalid for different reasons
        // save using the array above
        // push(), should return the items with respective POST and PUT operations and items which are set to fail should have their distinct errors
        // pendingSyncEntities(), should return the failing items from step 1
        // find using syncstore, should return the items from step 1 - the successfull one with lmt and ect
        List<Person> personsList = createPushErrList();
        int[] successItemsIdx = new int[] { 2 };

        DataStore<Person> storeSync = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        clearBackend(storeSync);
        client.getSyncManager().clear(Person.COLLECTION);

        saveList(storeSync, personsList);

        DefaultKinveyPushCallback pushCallback = push(storeSync, LONG_TIMEOUT);
        assertNotNull(pushCallback.result);
        assertEquals(personsList.size(), pushCallback.result.getSuccessCount());

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        assertTrue(checkSyncItems(syncItems, 2, SyncRequest.HttpVerb.POST));

        DefaultKinveyReadCallback findCallback = find(storeSync, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(checkIfPersonItemsAtRightIndex(personsList, findCallback.result.getResult(), successItemsIdx, false));
    }

    @Test
    @Ignore("Fails for now, need improvements for push logic: https://kinvey.atlassian.net/browse/MLIBZ-3058")
    public void testPushShouldUseMultiInsertAfterSaveSync() throws InterruptedException {
        print("should use multi-insert even if the items have not been created in an array");
        // save an item without _id
        // save another item without _id
        // push(), should use a multi-insert POST request and return the items with POST operations
        // pendingSyncEntities(), should return an empty array
        // find using syncstore, should return all items from step 1
        testPushMultiInsertSupport(StoreType.SYNC);
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    public void testSyncItemsListSync() throws InterruptedException {
        print("test sync items list");
        // create an array of 3 items, the second of which has invalid _geoloc parameters
        // save()
        // Sync(), Should return error for not pushed item
        // pendingSyncEntities(), should return the item with invalid params
        // find() using networkstore, should return the valid items
        // find using syncstore, should return all items including the invalid one
        testSyncItemsList(false, StoreType.SYNC);
    }

    // AUTO STORE

    @Test
    public void testSaveWithoutIdAuto() throws InterruptedException {
        print("should send POST with a single item with no _id");
        // create an item that has no _id property
        // call save() with it
        // call find() with sync store
        // find using network store

        DataStore<Person> autoStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.AUTO, client);
        DataStore<Person> syncStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        DataStore<Person> netStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(autoStore);
        clearBackend(syncStore);
        clearBackend(netStore);
        client.getSyncManager().clear(Person.COLLECTION);

        Person person = createPerson(TEST_USERNAME);

        DefaultKinveyClientCallback saveCallback = save(autoStore, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        Person resultPerson = (Person) saveCallback.result;
        assertNotNull(resultPerson.getId());
        assertEquals(person.getUsername(), resultPerson.getUsername());

        DefaultKinveyReadCallback findCallbackSync = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallbackSync.result);
        List<Person> listSync = findCallbackSync.result.getResult();
        assertEquals(1, listSync.size());
        Person findPersonSync = listSync.get(0);
        assertNotNull(findPersonSync);
        assertNotNull(findPersonSync.getId());
        assertEquals(person.getUsername(), findPersonSync.getUsername());

        DefaultKinveyReadCallback findCallbackNet = find(netStore, LONG_TIMEOUT);
        assertNotNull(findCallbackNet.result);
        List<Person> listNet = findCallbackNet.result.getResult();
        assertEquals(1, listNet.size());
        Person findPersonNet = listNet.get(0);
        assertNotNull(findPersonNet);
        assertNotNull(findPersonNet.getId());
        assertEquals(person.getUsername(), findPersonNet.getUsername());
    }

    @Test
    public void testSaveWithIdAuto() throws InterruptedException {
        print("should send PUT with a single item with _id");
        // create an item with _id property
        // call save() with it
        // call find() with syncstore
        // find using networkstore

        DataStore<Person> autoStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.AUTO, client);
        DataStore<Person> syncStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        DataStore<Person> netStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(autoStore);
        clearBackend(syncStore);
        clearBackend(netStore);
        client.getSyncManager().clear(Person.COLLECTION);

        String id = "123456";
        Person person = createPerson(TEST_USERNAME);
        person.setId(id);

        DefaultKinveyClientCallback saveCallback = save(autoStore, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        Person resultPerson = (Person) saveCallback.result;
        assertNotNull(resultPerson);
        assertEquals(id, resultPerson.getId());

        DefaultKinveyReadCallback findCallbackSync = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallbackSync.result);
        List<Person> listSync = findCallbackSync.result.getResult();
        assertEquals(1, listSync.size());
        Person findPersonSync = listSync.get(0);
        assertNotNull(findPersonSync);
        assertEquals(id, findPersonSync.getId());

        DefaultKinveyReadCallback findCallbackNet = find(netStore, LONG_TIMEOUT);
        assertNotNull(findCallbackNet.result);
        List<Person> listNet = findCallbackNet.result.getResult();
        assertEquals(1, listNet.size());
        Person findPersonNet = listNet.get(0);
        assertNotNull(findPersonNet);
        assertEquals(id, findPersonNet.getId());
    }

    @Test
    public void testSaveWithConnectivityErrorAuto() throws InterruptedException {
        print("should send with connectivity error");
        // create an item that has no _id property with invalid _geoloc params
        // call save() with it -mock it for connectivity error
        // call find() with syncstore, should return the item from step 1
        // call pendingSyncEntities(), should return the item from step 1 with POST operation

        Person person = new Person(TEST_USERNAME);
        person.setGeoloc(ERR_GEOLOC);

        DataStore<Person> autoStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.AUTO, client);
        DataStore<Person> syncStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        clearBackend(autoStore);
        clearBackend(syncStore);
        client.getSyncManager().clear(Person.COLLECTION);

        mockInvalidConnection();
        save(autoStore, person);
        cancelMockInvalidConnection();

        DefaultKinveyReadCallback findCallback = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        List<Person> list = findCallback.result.getResult();
        assertEquals(1, list.size());
        assertNotNull(list.get(0).getId());

        List<SyncItem> pendingList = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(pendingList);
        assertEquals(1, pendingList.size());
        SyncItem item = pendingList.get(0);
        assertNotNull(item);
        assertEquals(SyncRequest.HttpVerb.POST, item.getRequestMethod());
    }

    public void testSaveLocallyIfNetworkErrorAuto() throws InterruptedException {
        print("should save the item with _id locally if network connectivity issue");
        //create an item with _id
        //call save with it - mock the request to return connectivity error, should return connectivity error
        //find using syncstore, should return the item from step 1
        //pendingSyncEntities(), should return the item from step 1 with PUT operation with the specified _id

        String testId = "TEST_ID_123";
        Person person = new Person(testId, TEST_USERNAME);

        DataStore<Person> autoStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.AUTO, client);
        DataStore<Person> syncStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        clearBackend(autoStore);
        clearBackend(syncStore);
        client.getSyncManager().clear(Person.COLLECTION);

        mockInvalidConnection();
        save(autoStore, person);
        cancelMockInvalidConnection();

        DefaultKinveyReadCallback findCallback = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        List<Person> list = findCallback.result.getResult();
        assertEquals(1, list.size());
        assertEquals(testId, list.get(0).getId());

        List<SyncItem> pendingList = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(pendingList);
        assertEquals(1, pendingList.size());
        SyncItem item = pendingList.get(0);
        assertNotNull(item);
        assertEquals(testId, item.getEntityID().id);
        assertEquals(SyncRequest.HttpVerb.PUT, item.getRequestMethod());
    }

    @Test
    public void testSaveListWithoutIdAuto() throws InterruptedException {
        print("should send save an array of items with no _id");
        // create an array with a few items that have no _id property
        // save() with the array as param
        // find() with syncstore
        // find() using networkstore

        List<Person> personList = createPersonsList(false);

        DataStore<Person> autoStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.AUTO, client);
        DataStore<Person> syncStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        DataStore<Person> netStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(autoStore);
        clearBackend(syncStore);
        clearBackend(netStore);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallback = saveList(autoStore, personList);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result);
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result));

        DefaultKinveyReadCallback findCallbackSync = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallbackSync.result);
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result.getResult()));

        DefaultKinveyReadCallback findCallbackNet = find(netStore, LONG_TIMEOUT);
        assertNotNull(findCallbackNet.result);
        assertTrue(checkPersonIfSameObjects(personList, findCallbackNet.result.getResult()));
    }

    @Test
    public void testSaveListWithIdAuto() throws InterruptedException {
        print("should save an array of items with _id");
        // create an array with a few items that have _id property
        // save() with the array as param
        // find() using networkstore

        List<Person> personList = createPersonsList(true);

        DataStore<Person> autoStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.AUTO, client);
        DataStore<Person> netStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(autoStore);
        clearBackend(netStore);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallback = saveList(autoStore, personList);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result);
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result));

        DefaultKinveyReadCallback findCallbackNet = find(netStore, LONG_TIMEOUT);
        assertNotNull(findCallbackNet.result);
        assertTrue(checkPersonIfSameObjects(personList, findCallbackNet.result.getResult()));
    }

    @Test
    public void testSaveListCombineWithIdAndWithoutIdAuto() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array, should send POST multi-insert and PUT and return the items in their order from the original array, this one should be mocked to trace the requests.
        // find() with syncstore, should return the items along with metadata from the backend - ect, lmt
        // find() using networkstore, should return the items form step 1

        List<Person> personList = createCombineList();

        MockMultiInsertNetworkManager<Person> mockNetManager = new MockMultiInsertNetworkManager<Person>(Person.COLLECTION, Person.class, client);
        DataStore<Person> autoStore = new DataStore<Person>(Person.COLLECTION, Person.class, client, StoreType.AUTO, mockNetManager);
        DataStore<Person> syncStore = new DataStore<Person>(Person.COLLECTION, Person.class, client, StoreType.SYNC, mockNetManager);
        DataStore<Person> netStore = new DataStore<Person>(Person.COLLECTION, Person.class, client, StoreType.NETWORK, mockNetManager);

        clearBackend(autoStore);
        clearBackend(syncStore);
        clearBackend(netStore);
        client.getSyncManager().clear(Person.COLLECTION);

        mockNetManager.clear();
        DefaultKinveyClientListCallback saveCallback = saveList(autoStore, personList);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result);
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result));
        assertTrue(mockNetManager.useMultiInsertSave());

        DefaultKinveyReadCallback findCallbackSync = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallbackSync.result);
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result.getResult()));

        DefaultKinveyReadCallback findCallbackNet = find(netStore, LONG_TIMEOUT);
        assertNotNull(findCallbackNet.result);
        assertTrue(checkPersonIfSameObjects(personList, findCallbackNet.result.getResult()));
    }

    @Test
    public void testSaveListReturnErrorForEmptyListAuto() throws InterruptedException {
        print("should return an error for an empty array");
        // create an empty array
        // save() using the array
        // pendingSyncEntities()
        List<Person> list = new ArrayList<>();
        testSaveEmptyList(list, Person.class, Person.COLLECTION, StoreType.AUTO);

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        int pendingCount = syncItems == null ? 0 : syncItems.size();
        //assertNotNull(syncItems);
        assertEquals(pendingCount, list.size());
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    public void testSaveListReturnErrorForInvalidCredentialsAuto() throws InterruptedException {
        print("should return an error when all items fail with multi-insert for invalid credentials");
        // create an array with a few items that have no _id property
        // set a collection permission to deny creating items
        // save() using the array from above
        // pendingSyncEntities()
        // find() using syncstore

        List<EntitySet> entityList = createEntityList(2);

        DataStore<EntitySet> autoStore = DataStore.collection(EntitySet.COLLECTION, EntitySet.class, StoreType.AUTO, client);
        DataStore<EntitySet> syncStore = DataStore.collection(EntitySet.COLLECTION, EntitySet.class, StoreType.SYNC, client);
        clearBackend(autoStore);
        clearBackend(syncStore);
        client.getSyncManager().clear(EntitySet.COLLECTION);

        DefaultKinveyClientListCallback defaultKinveyListCallback = saveList(autoStore, entityList);
        assertNotNull(defaultKinveyListCallback.error);
        assertEquals(defaultKinveyListCallback.error.getClass(), KinveyJsonResponseException.class);

        List<SyncItem> syncItems = pendingSyncEntities(EntitySet.COLLECTION);
        assertNotNull(syncItems);
        assertTrue(checkSyncItems(syncItems, entityList.size(), SyncRequest.HttpVerb.POST));

        DefaultKinveyReadCallback findCallback = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(checkIfSameObjects(entityList, findCallback.result.getResult(), EntitySet.DESCRIPTION_KEY, false));
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    public void testSaveListReturnErrorArrayForAllItemsFailAuto() throws InterruptedException {
        print("should return an array of errors for all items failing for different reasons");
        // create an array containing two items failing for different reasons
        // save using the array above
        // find using syncstore
        // pendingSyncEntities()
        List<Person> personList = createErrList();
        int[] checkIndexes = new int[] {0, 1};

        DataStore<Person> autoStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.AUTO, client);
        DataStore<Person> syncStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        clearBackend(autoStore);
        clearBackend(syncStore);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallback = saveList(autoStore, personList);
        assertNotNull(saveCallback.error);
        if (saveCallback.error instanceof KinveySaveBunchException) {
            List<Person> resultEntities = ((KinveySaveBunchException) saveCallback.error).getEntities();
            List<KinveyBatchInsertError> errorsList = ((KinveySaveBunchException) saveCallback.error).getErrors();
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexes, true));
            assertTrue(checkErrorsIndexes(errorsList, checkIndexes));
        }

        DefaultKinveyReadCallback findCallback = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(checkPersonIfSameObjects(personList, findCallback.result.getResult()));

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        assertTrue(checkSyncItems(syncItems, personList.size(), SyncRequest.HttpVerb.POST));
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    public void testSaveListReturnErrorArrayForSomeItemsFailAuto() throws InterruptedException {
        print("should return an entities and errors when some requests fail and some succeed");
        // create an array of items with no _id and the second of them should have invalid _geoloc params
        // save using the array above
        // pendingSyncEntities()
        // find() using syncstore
        // find() using networkstore
        List<Person> personList = createErrList1();
        int[] checkIndexesSuccess = new int[] {0};
        int[] checkIndexesErr = new int[] {1};

        DataStore<Person> autoStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.AUTO, client);
        DataStore<Person> syncStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        DataStore<Person> netStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(autoStore);
        clearBackend(syncStore);
        clearBackend(netStore);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallback = saveList(autoStore, personList);
        assertNotNull(saveCallback.error);
        if (saveCallback.error instanceof KinveySaveBunchException) {
            List<Person> resultEntities = ((KinveySaveBunchException) saveCallback.error).getEntities();
            List<KinveyBatchInsertError> errorsList = ((KinveySaveBunchException) saveCallback.error).getErrors();
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexesSuccess, true));
            assertTrue(checkErrorsIndexes(errorsList, checkIndexesErr));
        }

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        assertTrue(checkSyncItems(syncItems, 1, SyncRequest.HttpVerb.POST));

        DefaultKinveyReadCallback findCallbackSync = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallbackSync.result);
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result.getResult()));

        DefaultKinveyReadCallback findCallbackNet = find(netStore, LONG_TIMEOUT);
        assertNotNull(findCallbackNet.result);
        assertTrue(checkIfPersonItemsAtRightIndex(personList, findCallbackNet.result.getResult(), checkIndexesSuccess, false));
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    public void testSaveListReturnPutFailuresAtMatchingIndexAuto() throws InterruptedException {
        print("should return PUT failures at the matching index");
        // create an array  - [{no_id, invalid_geoloc},{_id}, {_id, invalid_geoloc}, {no_id}]
        // save using the array above
        // pendingSyncEntities()
        // find() using syncstore

        List<Person> personList = createErrListGeoloc();
        int[] checkIndexesSuccess = new int[] {1, 3};
        int[] checkIndexesErr = new int[] {0, 2};

        DataStore<Person> autoStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.AUTO, client);
        DataStore<Person> syncStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        clearBackend(autoStore);
        clearBackend(syncStore);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientListCallback saveCallback = saveList(autoStore, personList);
        assertNotNull(saveCallback.error);
        if (saveCallback.error instanceof KinveySaveBunchException) {
            List<Person> resultEntities = ((KinveySaveBunchException) saveCallback.error).getEntities();
            List<KinveyBatchInsertError> errorsList = ((KinveySaveBunchException) saveCallback.error).getErrors();
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexesSuccess, false));
            assertTrue(checkErrorsIndexes(errorsList, checkIndexesErr));
        }

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        assertEquals(syncItems.get(0).getRequestMethod(), SyncRequest.HttpVerb.POST);
        assertEquals(syncItems.get(1).getRequestMethod(), SyncRequest.HttpVerb.PUT);

        DefaultKinveyReadCallback findCallbackSync = find(syncStore, LONG_TIMEOUT);
        assertNotNull(findCallbackSync.result);
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result.getResult()));
    }

    @Test
    @Ignore("Fails for now, need improvements for push logic: https://kinvey.atlassian.net/browse/MLIBZ-3058")
    public void testPushItemsListWithoutIdAuto() throws InterruptedException {
        print("should use multi insert for multiple items without _id");
        // create an array of items without _id
        // save using the array above : mock to return connection error
        // push() : should return an array of the objects pushed with the operation performed
        // pendingSyncEntities() : should return empty array
        // find() using networkstore : should return the items from step 1
        List<Person> personsList = createPersonsList(false);

        DataStore<Person> storeNet = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        DataStore<Person> storeAuto = DataStore.collection(Person.COLLECTION, Person.class, StoreType.AUTO, client);
        clearBackend(storeNet);
        clearBackend(storeAuto);
        client.getSyncManager().clear(Person.COLLECTION);

        mockInvalidConnection();
        saveList(storeAuto, personsList);
        cancelMockInvalidConnection();

        DefaultKinveyPushCallback pushCallback = push(storeAuto, LONG_TIMEOUT);
        assertNotNull(pushCallback.result);
        assertEquals(pushCallback.result.getSuccessCount(), personsList.size());

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertTrue(syncItems == null || syncItems.isEmpty());

        DefaultKinveyReadCallback findCallback = find(storeNet, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(checkPersonIfSameObjects(personsList, findCallback.result.getResult()));
    }

    @Test
    @Ignore("Fails for now, need improvements for push logic: https://kinvey.atlassian.net/browse/MLIBZ-3058")
    public void testPushItemsListCombineWithIdAndWithoutIdAuto() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array, mock to return connection error
        // push(), should use a multi-insert POST request and separate PUT requests for the items with _id. Should return and array of the items pushed with the respective operation - POST for no _id and PUT for _id
        // pendingSyncEntities(), should return empty array
        // find() using networkstore, should return the items from step 1
        List<Person> personsList = createCombineList();

        DataStore<Person> autoSync = DataStore.collection(Person.COLLECTION, Person.class, StoreType.AUTO, client);
        DataStore<Person> netSync = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(autoSync);
        clearBackend(netSync);
        client.getSyncManager().clear(Person.COLLECTION);

        mockInvalidConnection();
        saveList(autoSync, personsList);
        cancelMockInvalidConnection();

        DefaultKinveyPushCallback pushCallback = push(autoSync, LONG_TIMEOUT);
        assertNotNull(pushCallback.result);
        assertEquals(personsList.size(), pushCallback.result.getSuccessCount());

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertTrue(syncItems == null || syncItems.isEmpty());

        DefaultKinveyReadCallback findCallback = find(netSync, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(checkPersonIfSameObjects(personsList, findCallback.result.getResult()));
    }

    @Test
    @Ignore("Fails for now, need improvements for push logic: https://kinvey.atlassian.net/browse/MLIBZ-3058")
    public void testPushItemsListCombineWithIdAndWithoutIdMockedAuto() throws InterruptedException {
        print("should combine POST and PUT requests for items with and without _id - mocked");
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array, mock to return connection error
        // push(), should use a multi-insert POST request and separate PUT requests for the items with _id. Should return and array of the items pushed with the respective operation - POST for no _id and PUT for _id
        // pendingSyncEntities(), should return empty array
        // find() using networkstore, should return the items from step 1
        List<Person> personsList = createCombineList();

        MockMultiInsertNetworkManager<Person> mockNetManager = new MockMultiInsertNetworkManager<Person>(Person.COLLECTION, Person.class, client);
        DataStore<Person> autoSync = new DataStore<Person>(Person.COLLECTION, Person.class, client, StoreType.AUTO, mockNetManager);
        DataStore<Person> netSync = new DataStore<Person>(Person.COLLECTION, Person.class, client, StoreType.NETWORK, mockNetManager);

        clearBackend(autoSync);
        clearBackend(netSync);
        client.getSyncManager().clear(Person.COLLECTION);

        mockInvalidConnection();
        saveList(autoSync, personsList);
        cancelMockInvalidConnection();

        DefaultKinveyPushCallback pushCallback = push(autoSync, LONG_TIMEOUT);
        assertNotNull(pushCallback.result);
        assertEquals(personsList.size(), pushCallback.result.getSuccessCount());
        assertTrue(mockNetManager.useMultiInsertSave());

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertTrue(syncItems == null || syncItems.isEmpty());

        DefaultKinveyReadCallback findCallback = find(netSync, LONG_TIMEOUT);
        assertNotNull(findCallback.result);
        assertTrue(checkPersonIfSameObjects(personsList, findCallback.result.getResult(), true));
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    public void testPushItemsListReturnsErrorForEachItemAuto() throws InterruptedException {
        print("should return the failure reason in the result for each pushed item even if it is the same");
        // create an array of items without _id and set the collection permission for create to never
        // save() using the array
        // push(), should return the items from step 1 each with a POST operation and an error property with value: the invalid credentials error
        // pendingSyncEntities(), should return all items from step 1

        List<EntitySet> entitySetList = createEntityList(2);
        int[] itemsErrorIndexes = new int[] { 0, 1 };

        DataStore<EntitySet> autoSync = DataStore.collection(EntitySet.COLLECTION, EntitySet.class, StoreType.AUTO, client);
        clearBackend(autoSync);
        client.getSyncManager().clear(EntitySet.COLLECTION);

        saveList(autoSync, entitySetList);

        DefaultKinveyPushCallback pushCallback = push(autoSync, LONG_TIMEOUT);
        assertNotNull(pushCallback.error);
        if (pushCallback.error instanceof KinveySaveBunchException) {
            assertTrue(checkIfItemsAtRightIndex(entitySetList, ((KinveySaveBunchException) pushCallback.error).getEntities(),
                    itemsErrorIndexes, EntitySet.DESCRIPTION_KEY, true, false));
            assertTrue(checkErrorsIndexes(((KinveySaveBunchException) pushCallback.error).getErrors(), itemsErrorIndexes));
        }

        List<SyncItem> syncItems = pendingSyncEntities(EntitySet.COLLECTION);
        assertNotNull(syncItems);
        assertEquals(syncItems.size(), entitySetList.size());
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    public void testPushItemsListReturnsErrorsForEachItemErrorAuto() throws InterruptedException {
        print("should return the failure reason in the result for each pushed item when they are different");
        // create an array with 3 items without _id, two of which should be invalid for different reasons
        // save using the array above, mock to return connection error
        // push(), should return the items with respective POST and PUT operations and items which are set to fail should have their distinct errors
        // pendingSyncEntities(), should return the failing items

        List<Person> personsList = createPushErrList();

        DataStore<Person> autoSync = DataStore.collection(Person.COLLECTION, Person.class, StoreType.AUTO, client);
        clearBackend(autoSync);
        client.getSyncManager().clear(Person.COLLECTION);

        mockInvalidConnection();
        saveList(autoSync, personsList);
        cancelMockInvalidConnection();

        DefaultKinveyPushCallback pushCallback = push(autoSync, LONG_TIMEOUT);
        assertNotNull(pushCallback.result);
        assertEquals(personsList.size(), pushCallback.result.getSuccessCount());

        List<SyncItem> syncItems = pendingSyncEntities(Person.COLLECTION);
        assertNotNull(syncItems);
        assertTrue(checkSyncItems(syncItems, 2, SyncRequest.HttpVerb.PUT));
    }

    @Test
    @Ignore("Fails for now, need improvements for push logic: https://kinvey.atlassian.net/browse/MLIBZ-3058")
    public void testPushShouldUseMultiInsertAfterSaveAuto() throws InterruptedException {
        print("should use multi-insert even if the items have not been created in an array");
        // save an item without _id
        // save another item without _id, mock to return connection error
        // push(), should use a multi-insert POST request and return the items with POST operations
        // pendingSyncEntities(), should return empty array
        // find() using networkstore, should return all items from step 1
        testPushMultiInsertSupport(StoreType.AUTO);
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    public void testSyncItemsListAuto() throws InterruptedException {
        print("test sync items list");
        // create an array of 3 items, the second of which has invalid _geoloc parameters
        // save() mocking connectivity error
        // Sync(), Should return error for not pushed item
        // pendingSyncEntities(), should return the item with invalid params
        // find() using networkstore, should return the valid items
        // find using syncstore, should return all items including the invalid one
        testSyncItemsList(true, StoreType.AUTO);
    }
}
