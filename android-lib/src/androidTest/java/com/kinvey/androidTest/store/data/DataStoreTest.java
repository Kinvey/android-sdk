package com.kinvey.androidTest.store.data;


import android.content.Context;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.GenericJson;
import com.kinvey.android.Client;
import com.kinvey.android.async.AsyncPullRequest;
import com.kinvey.android.callback.KinveyCountCallback;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyReadCallback;
import com.kinvey.android.callback.KinveyPurgeCallback;
import com.kinvey.android.model.User;
import com.kinvey.android.store.DataStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.android.sync.KinveyPullCallback;
import com.kinvey.androidTest.model.DateExample;
import com.kinvey.androidTest.model.PersonArray;
import com.kinvey.java.model.KinveyPullResponse;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.android.sync.KinveyPushResponse;
import com.kinvey.android.sync.KinveySyncCallback;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.callback.CustomKinveyClientCallback;
import com.kinvey.androidTest.callback.CustomKinveyListCallback;
import com.kinvey.androidTest.callback.CustomKinveyReadCallback;
import com.kinvey.androidTest.callback.CustomKinveyPullCallback;
import com.kinvey.androidTest.callback.CustomKinveySyncCallback;
import com.kinvey.androidTest.model.Address;
import com.kinvey.androidTest.model.Author;
import com.kinvey.androidTest.model.LongClassNameLongClassNameLongClassNameLongClassNameLongClassName;
import com.kinvey.androidTest.model.Person;
import com.kinvey.androidTest.model.Person56;
import com.kinvey.androidTest.model.PersonWithPersonAndList;
import com.kinvey.androidTest.model.PersonLongListName;
import com.kinvey.androidTest.model.PersonOver63CharsInFieldName;
import com.kinvey.androidTest.util.RealmCacheManagerUtil;
import com.kinvey.androidTest.model.PersonList;
import com.kinvey.androidTest.model.PersonRoomAddressPerson;
import com.kinvey.androidTest.model.PersonRoomPerson;
import com.kinvey.androidTest.model.PersonWithAddress;
import com.kinvey.androidTest.model.Room;
import com.kinvey.androidTest.model.RoomAddress;
import com.kinvey.androidTest.model.RoomPerson;
import com.kinvey.androidTest.model.SelfReferencePerson;
import com.kinvey.androidTest.util.TableNameManagerUtil;
import com.kinvey.java.Constants;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.cache.ICacheManager;
import com.kinvey.java.cache.KinveyCachedClientCallback;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyReadResponse;
import com.kinvey.java.query.AbstractQuery;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.realm.DynamicRealm;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DataStoreTest {

    private static final String TEST_USERNAME = "Test_UserName";
    private static final String TEST_USERNAME_2 = "Test_UserName_2";
    private static final String TEST_TEMP_USERNAME = "Temp_UserName";
    private static final String USERNAME = "username";
    private static final String ID = "_id";
    private static final String KMD = "_kmd";
    private static final String SORT_FIELD = "_kmd.ect";
    private static final String LMT = "lmt";
    private static final String FIELD = "field";
    private static final int DEFAULT_TIMEOUT = 60;
    private static final int LONG_TIMEOUT = 6*DEFAULT_TIMEOUT;

    private Client client;

    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
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

    private static class DefaultKinveyPurgeCallback implements KinveyPurgeCallback {

        private CountDownLatch latch;
        Throwable error;

        DefaultKinveyPurgeCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Void result) {
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

    private static class DefaultKinveyClientArrayCallback implements KinveyClientCallback<PersonArray> {

        private CountDownLatch latch;
        PersonArray result;
        Throwable error;

        DefaultKinveyClientArrayCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(PersonArray result) {
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

    private static class DefaultKinveyPullCallback implements KinveyPullCallback {

        private CountDownLatch latch;
        KinveyPullResponse result;
        Throwable error;

        DefaultKinveyPullCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(KinveyPullResponse result) {
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

    private static class DefaultKinveyCountCallback implements KinveyCountCallback {
        private CountDownLatch latch;
        Integer result;
        Throwable error;

        DefaultKinveyCountCallback(CountDownLatch latch) { this.latch = latch; }

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

        void finish() { latch.countDown(); }
    }

    private static class DefaultKinveyCachedCallback<T> implements KinveyCachedClientCallback<Integer> {
        private CountDownLatch latch;
        Integer result;
        Throwable error;

        public DefaultKinveyCachedCallback() {
        }

        DefaultKinveyCachedCallback(CountDownLatch latch) { this.latch = latch; }

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

        void finish() { latch.countDown(); }

        public CountDownLatch getLatch() {
            return latch;
        }

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }
    }

    private static class CustomKinveyCachedCallback<T> implements KinveyCachedClientCallback<T> {
        private CountDownLatch latch;
        T result;
        Throwable error;

        public CustomKinveyCachedCallback() {
        }

        CustomKinveyCachedCallback(CountDownLatch latch) { this.latch = latch; }

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

        void finish() { latch.countDown(); }

        public CountDownLatch getLatch() {
            return latch;
        }

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
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

    private static class DefaultKinveyReadDateCallback implements KinveyReadCallback<DateExample> {

        private CountDownLatch latch;
        KinveyReadResponse<DateExample> result;
        Throwable error;

        DefaultKinveyReadDateCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(KinveyReadResponse<DateExample> result) {
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

    private static class DefaultKinveyDateCallback implements KinveyClientCallback<DateExample> {

        private CountDownLatch latch;
        DateExample result;
        Throwable error;

        DefaultKinveyDateCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(DateExample result) {
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


    @Test
    public void testSaveSync() throws InterruptedException {
        testSave(StoreType.SYNC);
    }

    @Test
    public void testSaveCache() throws InterruptedException {
        testSave(StoreType.CACHE);
    }

    @Test
    public void testSaveNetwork() throws InterruptedException {
        testSave(StoreType.NETWORK);
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

    @Test
    public void testUpdateSync() throws InterruptedException {
        testUpdate(StoreType.SYNC);
    }

    @Test
    public void testUpdateCache() throws InterruptedException {
        testUpdate(StoreType.CACHE);
    }

    @Test
    public void testUpdateNetwork() throws InterruptedException {
        testUpdate(StoreType.NETWORK);
    }

    @Test
    public void testUpdateSyncPush() throws InterruptedException {
        // Setup
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        clearBackend(store);
        client.getSyncManager().clear(Person.COLLECTION);

        // Save an entity locally
        Person person = createPerson(TEST_USERNAME);
        DefaultKinveyClientCallback callback = save(store, person);
        assertNotNull(callback);
        assertNotNull(callback.result);

        // Record the temporary Realm-generated ID
        Person updatedPerson = callback.result;
        String tempID = updatedPerson.getId();

        // Push the local entity to the backend
        DefaultKinveyPushCallback pushCallback = push(store, 60);
        assertNotNull(pushCallback);

        // Find the item locally and verify that the permanent ID is in place
        DefaultKinveyReadCallback findCallback = find(store,60);
        assertNotNull(findCallback);
        assertNotNull(findCallback.result);
        KinveyReadResponse<Person> readResponse = findCallback.result;
        assertNotNull(readResponse);
        List<Person> people = readResponse.getResult();
        assertNotNull(people);
        assertEquals(1, people.size());
        String permID = people.get(0).getId();
        assertNotNull(permID);
        assertNotEquals(tempID, permID);
    }

    private void testUpdate(StoreType storeType) throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = createPerson(TEST_USERNAME);
        DefaultKinveyClientCallback callback = save(store, person);
        assertNotNull(callback.result);
        assertNotNull(callback.result.getUsername());
        assertNull(callback.error);
        assertTrue(callback.result.getUsername().equals(TEST_USERNAME));
        person.setUsername(TEST_USERNAME_2);
        callback = save(store, person);
        assertNotNull(callback.result);
        assertNotNull(callback.result.getUsername());
        assertNull(callback.error);
        assertFalse(callback.result.getUsername().equals(TEST_USERNAME));
    }

    @Test
    public void testSaveItemLongCollectionNameNetwork() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.LONG_NAME, Person.class, StoreType.NETWORK, client);
        client.getSyncManager().clear(Person.LONG_NAME);
        DefaultKinveyClientCallback callback = save(store, createPerson(TEST_USERNAME));
        assertNotNull(callback.result);
        assertNotNull(callback.result.getUsername());
        assertNull(callback.error);
        assertTrue(callback.result.getUsername().equals(TEST_USERNAME));
    }

    @Test
    public void testDateObject() throws InterruptedException {
        DataStore<DateExample> store = DataStore.collection(DateExample.COLLECTION, DateExample.class, StoreType.SYNC, client);
        client.getSyncManager().clear(DateExample.COLLECTION);
        Date date = new Date();
        DateExample object = new DateExample("first", date);
        long startTime = date.getTime();
        long timeFromEntity = object.getDate().getTime();
        DefaultKinveyDateCallback callback = saveDate(store, object);
        assertNotNull(callback.result);
        Query query = client.query();
        query = query.equals(FIELD, "first");
        DefaultKinveyReadDateCallback kinveyListCallback = findDate(store, query, DEFAULT_TIMEOUT);
        deleteDate(store, query);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(startTime == timeFromEntity);
        assertTrue(kinveyListCallback.result.getResult().get(0).getDate().getTime() == startTime);
    }

    private DefaultKinveyDateCallback saveDate(final DataStore<DateExample> store, final DateExample object) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyDateCallback callback = new DefaultKinveyDateCallback(latch);
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

    private DefaultKinveyReadDateCallback findDate(final DataStore<DateExample> store, final Query query, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyReadDateCallback callback = new DefaultKinveyReadDateCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.find(query, callback, null);
            }
        });
        looperThread.start();
        latch.await(seconds, TimeUnit.SECONDS);
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private DefaultKinveyDeleteCallback deleteDate(final DataStore<DateExample> store, final Query query) throws InterruptedException {
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

    @Test
    public void testUpdatePersonArray() throws InterruptedException, IOException {
        DataStore<PersonArray> store = DataStore.collection(PersonArray.COLLECTION, PersonArray.class, StoreType.SYNC, client);
        client.getSyncManager().clear(PersonArray.COLLECTION);
        PersonArray person = new PersonArray();
        DefaultKinveyClientArrayCallback callback = savePersonArray(store, person);
        assertNotNull(callback.result);
        KinveyReadResponse<PersonArray> callbackFind = store.find();
        assertTrue(callbackFind.getResult().size() > 0);
    }

    private DefaultKinveyClientArrayCallback savePersonArray(final DataStore<PersonArray> store, final PersonArray person) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientArrayCallback callback = new DefaultKinveyClientArrayCallback(latch);
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

    @Test
    public void testSaveItemLongCollectionNameLocally() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.LONG_NAME, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.LONG_NAME);
        DefaultKinveyClientCallback callback = save(store, createPerson(TEST_USERNAME));
        assertNotNull(callback.result);
        assertNotNull(callback.result.getUsername());
        assertNull(callback.error);
        assertTrue(callback.result.getUsername().equals(TEST_USERNAME));
    }

    @Test
    public void test56SymbolsInTableName() throws InterruptedException, IOException {
        DataStore<Person56> store = DataStore.collection(Person.LONG_NAME, Person56.class, StoreType.SYNC, client);
        assertNotNull(store);
        client.getSyncManager().clear(Person.LONG_NAME);
        Person56 result = store.save(new Person56());
        assertNotNull(result);
    }

    @Test
    public void testALotSymbolsInListName() throws InterruptedException, IOException {
        DataStore<PersonLongListName> store = DataStore.collection(PersonLongListName.LONG_NAME, PersonLongListName.class, StoreType.SYNC, client);
        assertNotNull(store);
        client.getSyncManager().clear(PersonLongListName.LONG_NAME);
        PersonLongListName person = new PersonLongListName();
        List<String> list = new ArrayList<>();
        list.add("Test1");
        list.add("Test2");
        person.setList(list);
        PersonLongListName result = store.save(person);
        assertNotNull(result);
        result = store.find(client.query().equals(ID, result.getId())).getResult().get(0);
        assertNotNull(result);
        assertEquals(1, store.delete(result.getId()).longValue());
    }

    @Test
    public void testOver63SymbolsInListName() {
        try {
            DataStore.collection(PersonOver63CharsInFieldName.LONG_NAME, PersonOver63CharsInFieldName.class, StoreType.SYNC, client);
            assertFalse(true);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Column names are currently limited to max 63 characters."));
        }
    }

    @Test
    public void testCollectionWithLongClassName() throws InterruptedException, IOException {
        DataStore<LongClassNameLongClassNameLongClassNameLongClassNameLongClassName> store = DataStore.collection(
                "LongClassNameLongClassNameLongClassNameLongClassNameLongClassName",
                LongClassNameLongClassNameLongClassNameLongClassNameLongClassName.class, StoreType.SYNC, client);
        assertNotNull(store);
        client.getSyncManager().clear(Person.LONG_NAME);
        LongClassNameLongClassNameLongClassNameLongClassNameLongClassName result =
                store.save(new LongClassNameLongClassNameLongClassNameLongClassNameLongClassName());
        assertNotNull(result);
    }

    private DefaultKinveyClientCallback find(final DataStore<Person> store, final String id, int seconds, final KinveyCachedClientCallback<Person> cachedClientCallback) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.find(id, callback, cachedClientCallback);
            }
        });
        looperThread.start();
        latch.await(seconds, TimeUnit.SECONDS);
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testFindByIdSync() throws InterruptedException {
        testFindById(StoreType.SYNC);
    }

    @Test
    public void testFindByIdCache() throws InterruptedException {
        testFindById(StoreType.CACHE);
    }

    @Test
    public void testFindByIdNetwork() throws InterruptedException {
        testFindById(StoreType.NETWORK);
    }

    private void testFindById(StoreType storeType) throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        if (storeType != StoreType.NETWORK) {
            cleanBackendDataStore(store);
        }
        Person person = createPerson(TEST_USERNAME);
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        String personId = saveCallback.result.getId();
        DefaultKinveyClientCallback findCallback = find(store, personId, DEFAULT_TIMEOUT, null);
        assertNotNull(findCallback.result);
        assertNull(saveCallback.error);
        assertEquals(findCallback.result.getId(), personId);
    }

    @Test
    public void testFindByIdWithCacheCallback() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.CACHE.ttl).clear();
        Person person = createPerson(TEST_USERNAME);
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());

        String personId = saveCallback.result.getId();
        DefaultKinveyClientCallback findCallback = find(store, personId, LONG_TIMEOUT, new KinveyCachedClientCallback<Person>() {
            @Override
            public void onSuccess(Person result) {
                Log.d("testFindById: username ", result.getUsername());
            }

            @Override
            public void onFailure(Throwable error) {
                Log.d("testFindById: ", error.getMessage());
            }
        });
        assertNotNull(findCallback.result);
        assertNull(saveCallback.error);
        assertEquals(findCallback.result.getId(), personId);
    }

    private DefaultKinveyReadCallback find(final DataStore<Person> store, final Query query, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyReadCallback callback = new DefaultKinveyReadCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.find(query, callback, null);
            }
        });
        looperThread.start();
        latch.await(seconds, TimeUnit.SECONDS);
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

    @Test
    public void testFindByQuerySync() throws InterruptedException {
        testFindByQuery(StoreType.SYNC);
    }

    @Test
    public void testFindByQueryCache() throws InterruptedException {
        testFindByQuery(StoreType.CACHE);
    }

    @Test
    public void testFindByQueryNetwork() throws InterruptedException {
        testFindByQuery(StoreType.NETWORK);
    }

    private void testFindByQuery(StoreType storeType) throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        clearBackend(store);
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = createPerson(TEST_USERNAME);
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        String userId = saveCallback.result.getId();
        Query query = client.query();
        query = query.equals(ID, userId);
        DefaultKinveyReadCallback kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.getResult().size() > 0);
        delete(store, query);
    }

    @Test
    public void testMongoQueryStringBuilder() {
        // Arrange
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        Query myQuery = client.query();
        String expectedMongoQuery;
        String mongoQuery;

        // Act
        // Assert

        // Test field string value
        myQuery = client.query();
        myQuery.equals("testString", "a test");
        expectedMongoQuery = "{\"testString\":\"a test\"}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // Test field boolean value
        myQuery = client.query();
        myQuery.equals("testbool", true);
        expectedMongoQuery = "{\"testbool\":true}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // Test field int value
        myQuery = client.query();
        myQuery.equals("testint", 33);
        expectedMongoQuery = "{\"testint\":33}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        int ttl = 120;
        myQuery = client.query();
        myQuery.equals("ttl_in_seconds", ttl);
        expectedMongoQuery = "{\"ttl_in_seconds\":120}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // Test field long value
        myQuery = client.query();
        myQuery.equals("testlong", 34L);
        expectedMongoQuery = "{\"testlong\":34}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // Test field double value
        myQuery = client.query();
        myQuery.equals("testdouble", 34.0);
        expectedMongoQuery = "{\"testdouble\":34.0}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // Test field float value
        myQuery = client.query();
        myQuery.equals("testfloat", 34.0f);
        expectedMongoQuery = "{\"testfloat\":34.0}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // Test field null value
        myQuery = client.query();
        myQuery.equals("testnull", null);
        expectedMongoQuery = "{\"testnull\":null}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // Test $ne operator
        myQuery = client.query();
        myQuery.notEqual("age", "100500");
        expectedMongoQuery = "{\"age\":{\"$ne\":\"100500\"}}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // Test $in operator - string
        myQuery = client.query();
        myQuery.in("testIn", new String[]{"1","2","3"});
        expectedMongoQuery = "{\"testIn\":{\"$in\":[\"1\",\"2\",\"3\"]}}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // Test $in operator - bool
        myQuery = client.query();
        myQuery.in("testIn", new Boolean[]{true,false,true});
        expectedMongoQuery = "{\"testIn\":{\"$in\":[true,false,true]}}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // Test $in operator - int
        myQuery = client.query();
        myQuery.in("testIn", new Integer[]{1,2,3});
        expectedMongoQuery = "{\"testIn\":{\"$in\":[1,2,3]}}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // Test $in operator - long
        myQuery = client.query();
        myQuery.in("testIn", new Long[]{1L,2L,3L});
        expectedMongoQuery = "{\"testIn\":{\"$in\":[1,2,3]}}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // Test $in operator - float
        myQuery = client.query();
        myQuery.in("testIn", new Float[]{1.0f,2.0f,3.0f});
        expectedMongoQuery = "{\"testIn\":{\"$in\":[1.0,2.0,3.0]}}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // Test $in operator - double
        myQuery = client.query();
        myQuery.in("testIn", new Double[]{1.1,2.2,3.3});
        expectedMongoQuery = "{\"testIn\":{\"$in\":[1.1,2.2,3.3]}}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // $and query with 2 string values
        myQuery = client.query();
        myQuery.equals("testStr1", "test 1").and(client.query().equals("testStr2", "test 2"));
        expectedMongoQuery = "{\"$and\":[{\"testStr1\":\"test 1\"},{\"testStr2\":\"test 2\"}]}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // $and query with 2 boolean values
        myQuery = client.query();
        myQuery.equals("testBool1", true).and(client.query().equals("testBool2", false));
        expectedMongoQuery = "{\"$and\":[{\"testBool1\":true},{\"testBool2\":false}]}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // $and query with 2 int values
        myQuery = client.query();
        myQuery.equals("testInt1", 33).and(client.query().equals("testInt2", 23));
        expectedMongoQuery = "{\"$and\":[{\"testInt1\":33},{\"testInt2\":23}]}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // $and query with null value
        myQuery = client.query();
        String hospitalCode = "H1";
        myQuery.equals("hospitalCode", hospitalCode).and(client.query().equals("archived", null));
        expectedMongoQuery = "{\"$and\":[{\"hospitalCode\":\"H1\"},{\"archived\":null}]}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // $and query with null value and boolean
        myQuery = client.query();
        Boolean isHospital = false;
        myQuery.equals("isHospital", isHospital).and(client.query().equals("archived", null));
        expectedMongoQuery = "{\"$and\":[{\"isHospital\":false},{\"archived\":null}]}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);

        // implicit $and equals query
        myQuery = client.query();
        myQuery.equals("city", "Boston");
        myQuery.equals("age", "21");
        expectedMongoQuery = "{\"city\":\"Boston\",\"age\":\"21\"}";
        mongoQuery = myQuery.getQueryFilterJson(client.getJsonFactory());
        assertEquals(expectedMongoQuery, mongoQuery);
    }

    @Test
    public void testFindCountSync() throws InterruptedException, IOException {
        testFindCount(StoreType.SYNC, false);
    }

    @Test
    public void testFindCountCache() throws InterruptedException, IOException {
        testFindCount(StoreType.CACHE, false);
    }

    @Test
    public void testFindCountCachedCallbackCache() throws InterruptedException, IOException {
        testFindCount(StoreType.CACHE, true);
    }

    @Test
    public void testFindCountNetwork() throws InterruptedException, IOException {
        testFindCount(StoreType.NETWORK, false);
    }

    private void testFindCount(StoreType storeType, boolean isCachedCallbackUsed) throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        if (storeType != StoreType.NETWORK) {
            client.getSyncManager().clear(Person.COLLECTION);
        }

        clearBackend(store);
        Person person = createPerson(TEST_USERNAME);
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());

        DefaultKinveyCachedCallback<Integer> cachedCallback = null;
        if (storeType == StoreType.CACHE && isCachedCallbackUsed) {
            cachedCallback = new DefaultKinveyCachedCallback<>();
        }
        DefaultKinveyCountCallback countCallback = findCount(store, DEFAULT_TIMEOUT, cachedCallback);
        assertNull(countCallback.error);
        assertNotNull(countCallback.result);
        if (storeType == StoreType.CACHE && isCachedCallbackUsed) {
            assertNotNull(cachedCallback.result);
            assertNotNull(cachedCallback.result == 1);
            assertNull(cachedCallback.error);
        }
        assertTrue(countCallback.result == 1);
    }

    private DefaultKinveyCountCallback findCount(final DataStore<Person> store, int seconds, final DefaultKinveyCachedCallback<Integer> cachedClientCallback) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(cachedClientCallback != null ? 2 : 1);
        if (cachedClientCallback != null) {
           cachedClientCallback.setLatch(latch);
        }
        final DefaultKinveyCountCallback callback = new DefaultKinveyCountCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                if (cachedClientCallback != null) {
                    store.count(callback, cachedClientCallback);
                } else {
                    store.count(callback);
                }
            }
        });
        looperThread.start();
        latch.await(seconds, TimeUnit.SECONDS);
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private DefaultKinveyDeleteCallback delete(final DataStore<Person> store, final String id, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyDeleteCallback callback = new DefaultKinveyDeleteCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.delete(id, callback);
            }
        });
        looperThread.start();
        latch.await(seconds, TimeUnit.SECONDS);
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testDeleteSync() throws InterruptedException {
        testDelete(StoreType.SYNC);
    }

    @Test
    public void testDeleteCache() throws InterruptedException {
        testDelete(StoreType.CACHE);
    }

    @Test
    public void testDeleteNetwork() throws InterruptedException {
        testDelete(StoreType.NETWORK);
    }

    private void testDelete(StoreType storeType) throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = createPerson(TEST_USERNAME);
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        String userId = saveCallback.result.getId();
        DefaultKinveyDeleteCallback deleteCallback = delete(store, userId, DEFAULT_TIMEOUT);
        assertNull(deleteCallback.error);
        assertNotNull(deleteCallback.result);
        assertTrue(deleteCallback.result == 1);
    }

    @Test
    public void testDeleteNullIdSync() throws InterruptedException {
        testDeleteNullId(StoreType.SYNC);
    }

    @Test
    public void testDeleteNullIdCache() throws InterruptedException {
        testDeleteNullId(StoreType.CACHE);
    }

    @Test
    public void testDeleteNullIdNetwork() throws InterruptedException {
        testDeleteNullId(StoreType.NETWORK);
    }
    
    private void testDeleteNullId(StoreType storeType) throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = createPerson(TEST_USERNAME);
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        DefaultKinveyDeleteCallback deleteCallback = delete(store, null, DEFAULT_TIMEOUT);
        assertNotNull(deleteCallback.error);
        assertNull(deleteCallback.result);
    }

    private DefaultKinveyDeleteCallback delete(final DataStore<Person> store, final Iterable<String> entityIDs) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyDeleteCallback callback = new DefaultKinveyDeleteCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.delete(entityIDs, callback);
            }
        });
        looperThread.start();
        latch.await(120, TimeUnit.SECONDS);
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testDeleteArraySync() throws InterruptedException {
        testDeleteArray(StoreType.SYNC);
    }

    @Test
    public void testDeleteArrayCache() throws InterruptedException {
        testDeleteArray(StoreType.CACHE);
    }

    @Test
    public void testDeleteArrayNetwork() throws InterruptedException {
        testDeleteArray(StoreType.NETWORK);
    }

    private void testDeleteArray(StoreType storeType) throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientCallback saveCallback = save(store, createPerson(TEST_USERNAME));
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        String user1Id = saveCallback.result.getId();

        saveCallback = save(store, createPerson(TEST_USERNAME_2));
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        String user2Id = saveCallback.result.getId();

        assertNotEquals(user1Id, user2Id);

        List<String> list = Arrays.asList(user1Id, user2Id);
        DefaultKinveyDeleteCallback deleteCallback = delete(store, list);
        assertNull(deleteCallback.error);
        assertNotNull(deleteCallback.result);
        assertTrue(deleteCallback.result == list.size());
    }

/*    @Test
    public void testCustomTag() {
        String path = client.getContext().getFilesDir().getAbsolutePath();
        String customPath = path + "/_baas.kinvey.com_-1";
        removeFiles(customPath);
        File file = new File(customPath);
        assertFalse(file.exists());
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(file.exists());
    }*/

    private void removeFiles(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        String lockPath = path.concat(".lock");
        file = new File(lockPath);
        if (file.exists()) {
            file.delete();
        }
        String logPath = path.concat(".management");
        file = new File(logPath);
        if (file.exists()) {
            file.delete();
        }
    }

    private DefaultKinveyPurgeCallback purge(final Query query, final DataStore<Person> store) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyPurgeCallback callback = new DefaultKinveyPurgeCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                if (query != null) {
                    store.purge(query, callback);
                } else {
                    store.purge(callback);
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testPurge() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        save(store, createPerson(TEST_USERNAME));
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveyPurgeCallback purgeCallback = purge(null, store);
        assertNull(purgeCallback.error);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);

        store.clear();
        save(store, createPerson(TEST_USERNAME));
        save(store, createPerson(TEST_USERNAME_2));
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 2);
        purgeCallback = purge(new Query().equals("username", TEST_USERNAME), store);
        assertNull(purgeCallback.error);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 1);

        purgeCallback = purge(new Query(), store);
        assertNull(purgeCallback.error);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
    }

    @Test
    public void testPurgeInvalidDataStoreType() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        client.getSyncManager().clear(Person.COLLECTION);
        save(store, createPerson(TEST_USERNAME));
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
        DefaultKinveyPurgeCallback purgeCallback = purge(null, store);
        assertNotNull(purgeCallback.error);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
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

    private DefaultKinveySyncCallback sync(final DataStore<Person> store, final Query query, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveySyncCallback callback = new DefaultKinveySyncCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.sync(query, callback);
            }
        });
        looperThread.start();
        latch.await(seconds, TimeUnit.SECONDS);
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testSync() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        save(store, createPerson(TEST_USERNAME));
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveySyncCallback syncCallback = sync(store, 120);
        assertNull(syncCallback.error);
        assertNotNull(syncCallback.kinveyPushResponse);
        assertNotNull(syncCallback.kinveyPullResponse);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
    }

    @Test
    public void testSyncInvalidDataStoreType() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        client.getSyncManager().clear(Person.COLLECTION);
        save(store, createPerson(TEST_USERNAME));
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
        DefaultKinveySyncCallback syncCallback = sync(store, 120);
        assertNotNull(syncCallback.error);
        assertNull(syncCallback.kinveyPushResponse);
        assertNull(syncCallback.kinveyPullResponse);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
    }

    @Test
    public void testSyncTimeoutError() throws InterruptedException {
        final ChangeTimeout changeTimeout = new ChangeTimeout();
        HttpRequestInitializer initializer = new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws SocketTimeoutException {
                changeTimeout.initialize(request);
            }
        };
        client = new Client.Builder(client.getContext())
                .setHttpRequestInitializer(initializer)
                .build();
        client.getSyncManager().clear(Person.COLLECTION);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        Person person = createPerson(TEST_USERNAME);
        save(store, person);
        DefaultKinveySyncCallback syncCallback = sync(store, 120);
        assertNotNull(syncCallback.error);
        assertTrue(syncCallback.error.getClass() == SocketTimeoutException.class);
    }

    @Test
    public void testSyncNoCompletionHandler() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        save(store, createPerson(TEST_USERNAME));
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveySyncCallback syncCallback = sync(store, DEFAULT_TIMEOUT);
        assertFalse(syncCallback.error == null && syncCallback.kinveyPullResponse == null && syncCallback.kinveyPushResponse == null);
        assertNotNull(syncCallback.kinveyPushResponse);
        assertNotNull(syncCallback.kinveyPullResponse);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
    }

    private DefaultKinveyPushCallback push(final DataStore<Person> store, int seconds) throws InterruptedException {
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

    @Test
    public void testPush() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = createPerson(TEST_USERNAME);
        save(store, person);
        Log.d("DataStoreTest", "id: " + person.getId());
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveyPushCallback pushCallback = push(store, 120);
        assertNull(pushCallback.error);
        assertTrue(pushCallback.result.getListOfExceptions().size() == 0);
        assertNotNull(pushCallback.result);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
    }

    @Test
    public void testPushBatching() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);

        final int LIMIT = 25;
        for (int i = 0; i < LIMIT; i++) {
            Person person = createPerson(TEST_USERNAME);
            save(store, person);
            Log.d("DataStoreTest", "id: " + person.getId());
            assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == i+1);
        }

        DefaultKinveyPushCallback pushCallback = push(store, 120);
        assertNull(pushCallback.error);
        assertTrue(pushCallback.result.getListOfExceptions().size() == 0);
        assertNotNull(pushCallback.result);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
    }

    @Test
    public void testPushBlocking() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = createPerson(TEST_USERNAME);
        save(store, person);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 1);
        store.pushBlocking();
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
    }

    @Test
    public void testSyncBlocking() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        cleanBackendDataStore(store);
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = createPerson(TEST_USERNAME);
        save(store, person);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 1);
        store.pushBlocking();
        person = createPerson(TEST_USERNAME_2);
        save(store, person);
        store.syncBlocking(new Query());
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
        DefaultKinveyCountCallback countCallback = findCount(store, DEFAULT_TIMEOUT, null);
        assertTrue(countCallback.result == 2);
    }

    @Test
    public void testSyncBlocking2() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        cleanBackendDataStore(store);
        client.getSyncManager().clear(Person.COLLECTION);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
        Person person = createPerson(TEST_USERNAME);
        save(store, person);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 1);
        person.setAge("237 y.o.");
        save(store, person);
        long countAfterSave = client.getSyncManager().getCount(Person.COLLECTION);
        assertTrue(countAfterSave == 1);
        person = createPerson(TEST_USERNAME_2);
        save(store, person);
        long countAfter2ndSave = client.getSyncManager().getCount(Person.COLLECTION);
        assertTrue(countAfter2ndSave == 2);
        store.syncBlocking(new Query());
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
        DefaultKinveyCountCallback countCallback = findCount(store, DEFAULT_TIMEOUT, null);
        assertTrue(countCallback.result == 2);
    }

    @Test
    public void testPushInvalidDataStoreType() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        client.getSyncManager().clear(Person.COLLECTION);
        save(store, createPerson(TEST_USERNAME));
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
        DefaultKinveyPushCallback pushCallback = push(store, 120);
        assertTrue(pushCallback.error != null || pushCallback.result.getListOfExceptions() != null);
        assertNull(pushCallback.result);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
    }

    @Test
    public void testPushNoCompletionHandler() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        save(store, createPerson(TEST_USERNAME));
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveyPushCallback pushCallback = push(store, DEFAULT_TIMEOUT);
        assertFalse(pushCallback.error == null && pushCallback.result == null);
        assertNull(pushCallback.error);
        assertTrue(pushCallback.result.getListOfExceptions().size() == 0);
        assertNotNull(pushCallback.result);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
    }

    private DefaultKinveyPullCallback pull(final DataStore<Person> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyPullCallback callback = new DefaultKinveyPullCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                if (query != null) {
                    store.pull(query, callback);
                } else {
                    store.pull(callback);
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private DefaultKinveyPullCallback pull(final DataStore<Person> store, final Query query, final int pageSize) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyPullCallback callback = new DefaultKinveyPullCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                if (query != null) {
                    store.pull(query, pageSize, callback);
                } else {
                    store.pull(pageSize, callback);
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
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

    //cleaning backend store
    private void cleanBackendDataStore(DataStore<Person> store) throws InterruptedException {
        DefaultKinveySyncCallback syncCallback = sync(store, 120);
        assertNull(syncCallback.error);
        Query query = client.query();
        query = query.notEqual("age", "100500");
        DefaultKinveyDeleteCallback deleteCallback = delete(store, query);
        assertNull(deleteCallback.error);
        DefaultKinveyPushCallback pushCallback = push(store, 120);
        assertNull(pushCallback.error);
        assertTrue(pushCallback.result.getListOfExceptions().size() == 0);
        Log.d("testPull", " : clearing backend store successful");
    }

    private void clearBackend(DataStore<Person> store) throws InterruptedException {
        Query query = client.query();
        query = query.notEqual("age", "100500");
        DefaultKinveyDeleteCallback deleteCallback = delete(store, query);
    }

    //use for Person.COLLECTION and for Person.class
    private long getCacheSize(StoreType storeType) {
        return client.getCacheManager().getCache(Person.COLLECTION, Person.class, storeType.ttl).get().size();
    }

    @Test
    public void testGettingItemsByIds() throws InterruptedException {

        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);

        ICacheManager cacheManager = client.getCacheManager();
        ICache<Person> cache = cacheManager.getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl);
        cache.clear();

        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).clear();

        List<Person> items = new ArrayList<>();

        List<String> ids = new ArrayList<>();

        Person person;
        for (int i = 0; i < 100; i++) {
            person = createPerson("Test" + i);
            person.setId(String.valueOf(i));
            items.add(person);
            ids.add(String.valueOf(i));
        }

        for (Person p : items) {
            save(store, p);
        }

        List<Person> cachedObjects = client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).get(ids);
        assertEquals(100, cachedObjects.size());

        for (int i = 0; i < cachedObjects.size(); i++) {
            Person res = cachedObjects.get(i);
            assertEquals(res.getId(), String.valueOf(i));
        }

        assertTrue(true);
        client.getSyncManager().clear(Person.COLLECTION);
    }

    /**
     * Test checks that if you have some not correct value type in item's field at server,
     * you will have exception in KinveyPullResponse#getListOfExceptions after #pull.
     */
    @Test
    public void testPullNotCorrectItem() throws InterruptedException {
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION_WITH_EXCEPTION, Person.class, StoreType.SYNC, client);
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, null);
        assertTrue(pullCallback.getResult().getListOfExceptions().size() == 1);
        assertTrue(pullCallback.getResult().getCount() == 4);
        testManager.cleanBackendDataStore(store);
    }

        /**
         * Check that your collection has public permission console.kinvey.com
         * Collections / Collection Name / Settings / Permissions - Public
         */
    @Test
    public void testPull() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        client.getSyncManager().clear(Person.COLLECTION);

        cleanBackendDataStore(store);

        // uploading 3 person to backend
        ArrayList<Person> persons = new ArrayList<>();
        Person victor = createPerson("Victor_" + UUID.randomUUID().toString());
        Person hugo = createPerson("Hugo_" + UUID.randomUUID().toString());
        Person barros = createPerson("Barros_" + UUID.randomUUID().toString());
        save(store, victor);
        save(store, hugo);
        save(store, barros);
        DefaultKinveyPushCallback pushCallback = push(store, 120);
        assertNull(pushCallback.error);
        assertNotNull(pushCallback.result);

        //cleaning cache store
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.CACHE.ttl).clear();

        //test pulling all data from backend
        DefaultKinveyPullCallback pullCallback = pull(store, null);
        assertNull(pullCallback.error);
        assertNotNull(pullCallback.result);
        assertTrue(pullCallback.result.getCount() == 3);
        assertTrue(pullCallback.result.getCount() == getCacheSize(StoreType.CACHE));

        //cleaning cache store
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.CACHE.ttl).clear();

        //test pull only 1 item by query
        Query query = client.query();
        query = query.equals(USERNAME, victor.getUsername());
        pullCallback = pull(store, query);
        assertNull(pullCallback.error);
        assertNotNull(pullCallback.result);
        assertTrue(pullCallback.result.getCount() == 1);
        assertTrue(pullCallback.result.getCount() == getCacheSize(StoreType.SYNC));

        cleanBackendDataStore(store);

        //creating 1 entity and uploading to backend
        save(store, hugo);
        pushCallback = push(store, 120);
        assertNull(pushCallback.error);
        assertNotNull(pushCallback.result);

        //cleaning cache store
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).clear();
        //test pulling not existing data from backend
        query = client.query();
        query = query.equals(USERNAME, victor.getUsername());
        pullCallback = pull(store, query);
        assertNull(pullCallback.error);
        assertNotNull(pullCallback.result);
        assertTrue(pullCallback.result.getCount() == 0);
        assertTrue(pullCallback.result.getCount() == getCacheSize(StoreType.SYNC));

        cleanBackendDataStore(store);

        //creating 1 entity and uploading to backend
        save(store, victor);
        pushCallback = push(store, 120);
        assertNull(pushCallback.error);
        assertNotNull(pushCallback.result);

        //cleaning cache store
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).clear();

        //test pulling 1 entity if only 1 entity exist at backend
        query = client.query();
        query = query.equals(USERNAME, victor.getUsername());
        pullCallback = pull(store, query);
        assertNull(pullCallback.error);
        assertNotNull(pullCallback.result);
        assertTrue(pullCallback.result.getCount() == 1);
        assertTrue(pullCallback.result.getCount() == getCacheSize(StoreType.CACHE));
    }

    @Test
    public void testPullPendingSyncItems() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        save(store, createPerson("TestPullPendingSyncItems"));
        DefaultKinveyPullCallback pullCallback = pull(store, null);
        assertNull(pullCallback.result);
        assertNotNull(pullCallback.error);
    }

    @Test
    public void testSkipLimitInPullBlocking() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        client.getSyncManager().clear(Person.COLLECTION);

        cleanBackendDataStore(store);

        // Arrange
        ArrayList<Person> persons = new ArrayList<>();
        Person alvin = createPerson("Alvin");
        Person simon = createPerson("Simon");
        Person theodore = createPerson("Theodore");
        Person anna = createPerson("Anna");
        Person kate = createPerson("Kate");
        save(store, alvin);
        save(store, simon);
        save(store, theodore);
        save(store, anna);
        save(store, kate);
        long cacheSizeBefore = getCacheSize(StoreType.CACHE);
        assertTrue(cacheSizeBefore == 5);
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.CACHE.ttl).clear();
        long cacheSizeBetween = getCacheSize(StoreType.CACHE);
        assertTrue(cacheSizeBetween == 0);

        Query query = client.query().addSort(SORT_FIELD, AbstractQuery.SortOrder.ASC);
        for (int i = 0; i < 5; i++) {
            query.setLimit(1);
            query.setSkip(i);
            assertEquals(1, store.pullBlocking(query).getCount());
            assertEquals(i+1, getCacheSize(StoreType.CACHE));
        }
        assertEquals(5, getCacheSize(StoreType.CACHE));
        for (int i = 0; i < 5; i++) {
            query.setLimit(1);
            query.setSkip(i);
            assertEquals(1, store.pullBlocking(query).getCount());
            assertEquals(5, getCacheSize(StoreType.CACHE));
        }
        assertEquals(5, getCacheSize(StoreType.CACHE));
    }

    @Test
    public void testSkipLimitInPullAsync() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        client.getSyncManager().clear(Person.COLLECTION);

        cleanBackendDataStore(store);

        // Arrange
        ArrayList<Person> persons = new ArrayList<>();
        Person alvin = createPerson("Alvin");
        Person simon = createPerson("Simon");
        Person theodore = createPerson("Theodore");
        Person anna = createPerson("Anna");
        Person kate = createPerson("Kate");
        save(store, alvin);
        save(store, simon);
        save(store, theodore);
        save(store, anna);
        save(store, kate);
        long cacheSizeBefore = getCacheSize(StoreType.CACHE);
        assertTrue(cacheSizeBefore == 5);
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.CACHE.ttl).clear();
        long cacheSizeBetween = getCacheSize(StoreType.CACHE);
        assertTrue(cacheSizeBetween == 0);

        int resultCount = 0;
        Query query = client.query().addSort(ID, AbstractQuery.SortOrder.ASC);
        for (int i = 0; i < 5; i++) {
            query.setLimit(1);
            query.setSkip(i);
            resultCount = pull(store, query).result.getCount();

            assertEquals(1, resultCount);
            assertEquals(i+1, getCacheSize(StoreType.CACHE));
        }
        assertEquals(5, getCacheSize(StoreType.CACHE));
        for (int i = 0; i < 5; i++) {
            query.setLimit(1);
            query.setSkip(i);
            resultCount = pull(store, query).result.getCount();

            assertEquals(1, resultCount);
            assertEquals(5, getCacheSize(StoreType.CACHE));
        }
        assertEquals(5, getCacheSize(StoreType.CACHE));
    }

    /**
     * Check that SDK pulls the items in correct order from the server
     * if skip limit is used in pull/sync query.
     */
    @Test
    public void testPullOrderWithSkipLimitQuery() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);

        cleanBackendDataStore(store);

        for (int i = 0; i < 5; i++) {
            save(store, createPerson(TEST_USERNAME + Constants.UNDERSCORE + i));
        }
        sync(store, DEFAULT_TIMEOUT);
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).clear();

        Query query = client.query().addSort(ID, AbstractQuery.SortOrder.ASC);
        query.setLimit(1);
        for (int i = 0; i < 5; i++) {
            query.setSkip(i);
            assertEquals(1, pull(store, query).result.getCount());
            assertEquals(i+1, getCacheSize(StoreType.SYNC));
        }
        assertEquals(5, getCacheSize(StoreType.SYNC));
    }

    @Test
    public void testPullOrderWithSkipLimitQueryWithCachedItemsBeforeTestSortById() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        DefaultKinveyPullCallback pullResponse;
        for (int j = 0; j < 10; j++) {
            cleanBackendDataStore(store);
            for (int i = 0; i < 5; i++) {
                save(store, createPerson(TEST_USERNAME + Constants.UNDERSCORE + i));
            }
            sync(store, DEFAULT_TIMEOUT);
            Query query = client.query();
            query.setLimit(1).addSort(ID, AbstractQuery.SortOrder.ASC);
            for (int i = 0; i < 5; i++) {
                query.setSkip(i);
                pullResponse = pull(store, query);
                assertNotNull(pullResponse);
                assertTrue(pullResponse.result.getCount() == 1);
                assertEquals(5, getCacheSize(StoreType.SYNC));
            }
            assertEquals(5, getCacheSize(StoreType.SYNC));
        }
    }


    @Test
    public void testPullOrderWithSkipLimitQueryWithCachedItemsBeforeTest() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        KinveyPullResponse pullResponse;
        for (int j = 0; j < 10; j++) {
            cleanBackendDataStore(store);
            for (int i = 0; i < 5; i++) {
                save(store, createPerson(TEST_USERNAME + Constants.UNDERSCORE + i));
            }
            sync(store, DEFAULT_TIMEOUT);
            Query query = client.query();
            query.setLimit(1).addSort(ID, AbstractQuery.SortOrder.ASC);
            for (int i = 0; i < 5; i++) {
                query.setSkip(i);
                pullResponse = pull(store, query).result;
                assertNotNull(pullResponse);
                assertTrue(pullResponse.getCount() == 1);
                assertEquals(5, getCacheSize(StoreType.SYNC));
            }
            assertEquals(5, getCacheSize(StoreType.SYNC));
        }
    }

    @Test
    public void testPullOrderWithSkipLimitQueryWithCachedItemsBeforeTestWithAutoPagination() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        KinveyPullResponse pullResponse;
        for (int j = 0; j < 10; j++) {
            cleanBackendDataStore(store);
            for (int i = 0; i < 5; i++) {
                save(store, createPerson(TEST_USERNAME + Constants.UNDERSCORE + i));
            }
            sync(store, DEFAULT_TIMEOUT);
            Query query = client.query();
            pullResponse = pull(store, query, 1).result;
            assertNotNull(pullResponse);
            assertEquals(5, getCacheSize(StoreType.SYNC));
        }
    }

    /**
     * Check that SDK finds the items in the cache in correct order
     * if skip limit is used in find method.
     */
    @Test
    public void testFindInCacheOrderWithSkipLimitQuery() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);

        for (int i = 0; i < 5; i++) {
            save(store, createPerson(TEST_USERNAME + Constants.UNDERSCORE + i));
        }

        Query query = client.query();
        DefaultKinveyReadCallback findCallback;
        for (int i = 0; i < 5; i++) {
            query.setLimit(1);
            query.setSkip(i);
            findCallback = find(store, query, DEFAULT_TIMEOUT);
            assertTrue(findCallback.result.getResult().size() == 1);
            assertEquals(TEST_USERNAME + Constants.UNDERSCORE + i, findCallback.result.getResult().get(0).getUsername());
        }
        assertEquals(5, getCacheSize(StoreType.SYNC));
    }

    /**
     * Check that SDK removes the correct items from cache in pull/sync process
     * if skip limit is used in pull/sync query.
     */
    @Test
    public void testSyncUpdateCacheInCorrectWay() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);

        cleanBackendDataStore(store);

        for (int i = 0; i < 5; i++) {
            save(store, createPerson(TEST_USERNAME + Constants.UNDERSCORE + i));
        }
        sync(store, DEFAULT_TIMEOUT);
        List<Person> findResult = find(store, client.query(), DEFAULT_TIMEOUT).result.getResult();
        assertEquals(5, findResult.size());
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).clear();

        List<Person> pullResults;
        int resultCount;
        Query query = client.query().addSort(ID, AbstractQuery.SortOrder.ASC);
        query.setLimit(1);
        for (int i = 0; i < 5; i++) {
            query.setSkip(i);
            resultCount = pull(store, query).result.getCount();
            assertNotNull(resultCount);
        }
        assertEquals(5, getCacheSize(StoreType.SYNC));
        findResult = find(store, client.query(), DEFAULT_TIMEOUT).result.getResult();
        assertEquals(5, findResult.size());
        assertEquals(5, getCacheSize(StoreType.SYNC));

        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).clear();

        query = client.query().addSort(ID, AbstractQuery.SortOrder.ASC);
        int limit = 2;
        int skip = 0;
        query.setLimit(limit);
        for (int i = 0; i < 5; i++) {
            query.setSkip(skip);
            skip += limit;
            resultCount = pull(store, query).result.getCount();
            assertNotNull(resultCount);
        }
        assertEquals(5, getCacheSize(StoreType.SYNC));
        findResult = find(store, client.query(), DEFAULT_TIMEOUT).result.getResult();
        assertEquals(5, findResult.size());
        assertEquals(5, getCacheSize(StoreType.SYNC));
    }

    @Test
    public void testSkipLimitInSyncBlocking() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        client.getSyncManager().clear(Person.COLLECTION);

        cleanBackendDataStore(store);

        // Arrange
        ArrayList<Person> persons = new ArrayList<>();
        Person alvin = createPerson("Alvin");
        Person simon = createPerson("Simon");
        Person theodore = createPerson("Theodore");
        Person anna = createPerson("Anna");
        Person kate = createPerson("Kate");
        save(store, alvin);
        save(store, simon);
        save(store, theodore);
        save(store, anna);
        save(store, kate);
        long cacheSizeBefore = getCacheSize(StoreType.CACHE);
        assertTrue(cacheSizeBefore == 5);
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.CACHE.ttl).clear();
        long cacheSizeBetween = getCacheSize(StoreType.CACHE);
        assertTrue(cacheSizeBetween == 0);

        Query query = client.query().addSort(SORT_FIELD, AbstractQuery.SortOrder.ASC);
        for (int i = 0; i < 5; i++) {
            query.setLimit(1);
            query.setSkip(i);
            store.syncBlocking(query);

            assertEquals(i+1, getCacheSize(StoreType.CACHE));
        }
        assertEquals(5, getCacheSize(StoreType.CACHE));
        for (int i = 0; i < 5; i++) {
            query.setLimit(1);
            query.setSkip(i);
            store.syncBlocking(query);

            assertEquals(5, getCacheSize(StoreType.CACHE));
        }
        assertEquals(5, getCacheSize(StoreType.CACHE));
    }

    @Test
    public void testSkipLimitInSyncAsync() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        client.getSyncManager().clear(Person.COLLECTION);

        cleanBackendDataStore(store);

        // Arrange
        ArrayList<Person> persons = new ArrayList<>();
        Person alvin = createPerson("Alvin");
        Person simon = createPerson("Simon");
        Person theodore = createPerson("Theodore");
        Person anna = createPerson("Anna");
        Person kate = createPerson("Kate");
        save(store, alvin);
        save(store, simon);
        save(store, theodore);
        save(store, anna);
        save(store, kate);
        long cacheSizeBefore = getCacheSize(StoreType.CACHE);
        assertTrue(cacheSizeBefore == 5);
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.CACHE.ttl).clear();
        long cacheSizeBetween = getCacheSize(StoreType.CACHE);
        assertTrue(cacheSizeBetween == 0);

        Query query = client.query().addSort(SORT_FIELD, AbstractQuery.SortOrder.ASC);
        for (int i = 0; i < 5; i++) {
            query.setLimit(1);
            query.setSkip(i);
            sync(store, query, 120);
            assertEquals(i+1, getCacheSize(StoreType.CACHE));
        }
        assertEquals(5, getCacheSize(StoreType.CACHE));
        for (int i = 0; i < 5; i++) {
            query.setLimit(1);
            query.setSkip(i);
            sync(store, query, 120);
            assertEquals(5, getCacheSize(StoreType.CACHE));
        }
        assertEquals(5, getCacheSize(StoreType.CACHE));
    }

    @Test
    public void testPullInvalidDataStoreType() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyPullCallback pullCallback = pull(store, null);
        assertNull(pullCallback.result);
        assertNotNull(pullCallback.error);
    }

    @Test
    public void testExpiredTTL() throws InterruptedException {
        StoreType.SYNC.ttl = 1;
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);

        DefaultKinveyClientCallback saveCallback = save(store, createPerson(TEST_USERNAME));
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        String userId = saveCallback.result.getId();
        assertNotNull(userId);

        Thread.sleep(1000);

        Query query = client.query();
        query = query.equals(ID, userId);

        DefaultKinveyReadCallback findCallback = find(store, query, 120);
        assertNull(findCallback.error);
        assertNotNull(findCallback.result);
        assertTrue(findCallback.result.getResult().size() == 0);
        StoreType.SYNC.ttl = Long.MAX_VALUE;
    }

    /**
    * Check that your collection has public permission console.kinvey.com
    * Collections / Collection Name / Settings / Permissions - Public
    */
    @Test
    public void testSaveAndFind10SkipLimit() throws IOException, InterruptedException {
        assertNotNull(client.getActiveUser());
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        cleanBackendDataStore(store);
        sync(store, 120);

        User user = client.getActiveUser();

        for (int i = 0; i < 10; i++) {
            Person person = createPerson("Person_" + i);
            DefaultKinveyClientCallback saveCallback = save(store, person);
            assertNull(saveCallback.error);
            assertNotNull(saveCallback.result);
        }

        int skip = 0;
        int limit = 2;

        DefaultKinveyReadCallback kinveyListCallback;
        Query query = client.query().addSort(SORT_FIELD, AbstractQuery.SortOrder.ASC);
        for (int i = 0; i < 5; i++) {
            query.setSkip(skip);
            query.setLimit(limit);
            kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
            assertNull(kinveyListCallback.error);
            assertNotNull(kinveyListCallback.result);
            assertEquals(kinveyListCallback.result.getResult().get(0).getUsername(), "Person_" + skip);
            assertEquals(kinveyListCallback.result.getResult().get(1).getUsername(), "Person_" + (skip+1));
            skip += limit;
        }

        query = client.query();
        query.setLimit(5);
        kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.getResult().size() == 5);
        assertEquals(kinveyListCallback.result.getResult().get(0).getUsername(), "Person_0");
        assertEquals(kinveyListCallback.result.getResult().get(kinveyListCallback.result.getResult().size()-1).getUsername(), "Person_4");


        query = client.query();
        query.setSkip(5);
        kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.getResult().size() == 5);
        assertEquals(kinveyListCallback.result.getResult().get(0).getUsername(), "Person_5");
        assertEquals(kinveyListCallback.result.getResult().get(kinveyListCallback.result.getResult().size()-1).getUsername(), "Person_9");

        query = client.query();
        query.setLimit(6);
        query.setSkip(6);
        kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.getResult().size() == 4);
        assertEquals(kinveyListCallback.result.getResult().get(0).getUsername(), "Person_6");
        assertEquals(kinveyListCallback.result.getResult().get(kinveyListCallback.result.getResult().size()-1).getUsername(), "Person_9");


        query = client.query();
        query.setSkip(10);
        kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.getResult().size() == 0);

        query = client.query();
        query.setSkip(11);
        kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.getResult().size() == 0);


        DefaultKinveyPushCallback pushCallback = push(store, DEFAULT_TIMEOUT);
        assertNull(pushCallback.error);
        assertNotNull(pushCallback.result);
        assertTrue(pushCallback.result.getSuccessCount() == 10);

        skip = 0;
        for (int i = 0; i < 5; i++) {
            query = client.query();
            query.equals("_acl.creator", user.getId());
            query.setSkip(skip);
            query.setLimit(limit);
            query.addSort("username", AbstractQuery.SortOrder.ASC);

            DefaultKinveyPullCallback pullCallback = pull(store, query);
            assertNull(pullCallback.error);
            assertNotNull(pullCallback.result);
            assertTrue(pullCallback.result.getCount() == limit);
            skip += limit;
        }

    }

    class ChangeTimeout implements HttpRequestInitializer {
        public void initialize(HttpRequest request) throws SocketTimeoutException {
            throw new SocketTimeoutException();
        }
    }

    @Test
    public void testSyncCount() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = createPerson(TEST_USERNAME);
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        assertTrue(store.syncCount() == 1);
        sync(store, DEFAULT_TIMEOUT);
        assertTrue(store.syncCount() == 0);
        delete(store, saveCallback.result.getId(), DEFAULT_TIMEOUT);
        assertTrue(store.syncCount() == 1);
        sync(store, DEFAULT_TIMEOUT);
        assertTrue(store.syncCount() == 0);
    }

    @Test
    public void testSaveKmd() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, 60L).clear();
        clearBackend(DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client));
        store.clear();
        Person person = createPerson(TEST_TEMP_USERNAME);
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback);
        sync(store, DEFAULT_TIMEOUT);
        DefaultKinveyReadCallback findCallback = find(store, DEFAULT_TIMEOUT);
        assertNotNull(findCallback);
        assertNotNull(findCallback.result);
        List<Person> people = findCallback.result.getResult();
        assertNotNull(people);
        assertEquals(1, people.size());
        Person savedPerson = people.get(0);
        assertNotNull(savedPerson.get(KMD));
        assertNotNull(((GenericJson)savedPerson.get(KMD)).get(LMT));
        delete(store, savedPerson.getId(), DEFAULT_TIMEOUT);
        push(store, DEFAULT_TIMEOUT);
        client.getSyncManager().clear(Person.COLLECTION);
    }

    @Test
    public void testUpdateLmt() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        store.clear();
        clearBackend(DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client));
        Person person = createPerson(TEST_TEMP_USERNAME);
        Person savedPerson = store.save(person);
        sync(store, DEFAULT_TIMEOUT);
        DefaultKinveyReadCallback findCallback = find(store, DEFAULT_TIMEOUT);
        assertNotNull(findCallback);
        assertNotNull(findCallback.result);
        List<Person> people = findCallback.result.getResult();
        assertEquals(1, people.size());
        Person syncedPerson = people.get(0);
        assertNotNull(syncedPerson.get(KMD));
        String savedLmd = (String)((GenericJson)syncedPerson.get(KMD)).get(LMT);
        assertNotNull(savedLmd);
        syncedPerson.setUsername(TEST_TEMP_USERNAME + "_Change");
        syncedPerson = store.save(syncedPerson);
        sync(store, DEFAULT_TIMEOUT);
        findCallback = find(store, DEFAULT_TIMEOUT);
        assertNotNull(findCallback.result.getResult());
        Person updatedSyncedPerson = findCallback.result.getResult().get(0);
        assertNotNull(updatedSyncedPerson.get(KMD));
        String updatedLmd = (String)((GenericJson)updatedSyncedPerson.get(KMD)).get(LMT);
        assertNotNull(updatedLmd);
        assertNotEquals(savedLmd, updatedLmd);
        delete(store, updatedSyncedPerson.getId(), DEFAULT_TIMEOUT);
        push(store, DEFAULT_TIMEOUT);
        client.getSyncManager().clear(Person.COLLECTION);
    }

    @Test
    public void testSaveList() throws InterruptedException {
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        ArrayList<Person> persons = new ArrayList<>();
        persons.add(new Person(TEST_USERNAME));

        CustomKinveyListCallback<Person> saveCallback = testManager.saveCustomList(store, persons);
        assertNotNull(saveCallback.getResult());
        com.kinvey.androidTest.callback.DefaultKinveyPushCallback pushCallback = testManager.push(store);
        assertNotNull(pushCallback.getResult());
    }

    @Test
    public void testSaveListWithStoreTypeCache() throws InterruptedException {
        client.enableDebugLogging();
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        ArrayList<Person> persons = new ArrayList<>();
        persons.add(new Person(TEST_USERNAME));
        persons.add(new Person(TEST_USERNAME_2));
        persons.add(new Person(TEST_TEMP_USERNAME));

        CustomKinveyListCallback<Person> saveCallback = testManager.saveCustomList(store, persons);
        assertNotNull(saveCallback.getResult());
        assertEquals(3, saveCallback.getResult().size());
        List<Person> cachedItems = client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.CACHE.ttl).get();
        assertEquals(3, cachedItems.size());
    }

    @Test
    public void testClear() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);

        assertTrue(store.syncCount() == 0);
        assertTrue(store.count() == 0);

        com.kinvey.androidTest.callback.DefaultKinveyClientCallback saveCallback = testManager.save(store, new Person(TEST_USERNAME));
        assertNotNull(saveCallback.getResult());
        assertNull(saveCallback.getError());
        assertTrue(store.syncCount() == 1);
        assertTrue(store.count() == 1);

        store.clear();

        assertTrue(store.syncCount() == 0);
        assertTrue(store.count() == 0);

        saveCallback = testManager.save(store, new Person(TEST_USERNAME));
        assertNotNull(saveCallback.getResult());
        assertNull(saveCallback.getError());

        assertTrue(store.syncCount() == 1);
        assertTrue(store.count() == 1);
    }

    @Test
    public void testQueryClear() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);

        assertTrue(store.syncCount() == 0);
        assertTrue(store.count() == 0);

        com.kinvey.androidTest.callback.DefaultKinveyClientCallback saveCallback = testManager.save(store, new Person(TEST_USERNAME));
        assertNotNull(saveCallback.getResult());
        assertNull(saveCallback.getError());
        assertTrue(store.syncCount() == 1);
        assertTrue(store.count() == 1);

        saveCallback = testManager.save(store, new Person(TEST_USERNAME_2));
        assertNotNull(saveCallback.getResult());
        assertNull(saveCallback.getError());
        assertTrue(store.syncCount() == 2);
        assertTrue(store.count() == 2);

        store.clear(client.query().equals("username", TEST_USERNAME));

        assertTrue(store.syncCount() == 1);
        assertTrue(store.count() == 1);

        Person deletePerson = new Person(TEST_USERNAME);
        saveCallback = testManager.save(store, deletePerson);
        assertNotNull(saveCallback.getResult());
        assertNull(saveCallback.getError());

        assertTrue(store.syncCount() == 2);
        assertTrue(store.count() == 2);
    }

    @Test
    public void testQueryPurge() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);

        assertTrue(store.syncCount() == 0);
        assertTrue(store.count() == 0);

        com.kinvey.androidTest.callback.DefaultKinveyClientCallback saveCallback = testManager.save(store, new Person(TEST_USERNAME));
        assertNotNull(saveCallback.getResult());
        assertNull(saveCallback.getError());
        assertTrue(store.syncCount() == 1);
        assertTrue(store.count() == 1);

        saveCallback = testManager.save(store, new Person(TEST_USERNAME_2));
        assertNotNull(saveCallback.getResult());
        assertNull(saveCallback.getError());
        assertTrue(store.syncCount() == 2);
        assertTrue(store.count() == 2);

        store.purge(client.query().equals("username", TEST_USERNAME));

        assertTrue(store.syncCount() == 1);
        assertTrue(store.count() == 2);

        store.purge(client.query());
        assertTrue(store.syncCount() == 0);

        Person deletePerson = new Person(TEST_USERNAME);
        saveCallback = testManager.save(store, deletePerson);
        assertNotNull(saveCallback.getResult());
        assertNull(saveCallback.getError());

        assertTrue(store.syncCount() == 1);
        assertTrue(store.count() == 3);
    }

    @Test
    public void testSelfReferenceClass() throws InterruptedException {
        TestManager<SelfReferencePerson> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<SelfReferencePerson> store = DataStore.collection(Person.COLLECTION, SelfReferencePerson.class, StoreType.SYNC, client);
        assertNotNull(store);
    }

    @Test
    public void testSelfReferenceClassWithData() throws InterruptedException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        client.enableDebugLogging();
        TestManager<SelfReferencePerson> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<SelfReferencePerson> store = DataStore.collection(Person.COLLECTION, SelfReferencePerson.class, StoreType.SYNC, client);
        SelfReferencePerson person1 = new SelfReferencePerson("person1");
        SelfReferencePerson person2 = new SelfReferencePerson("person2");
        SelfReferencePerson person3 = new SelfReferencePerson("person3");
        SelfReferencePerson person4 = new SelfReferencePerson("person4");
        SelfReferencePerson person5 = new SelfReferencePerson("person5");

        person4.setPerson(person5);
        person3.setPerson(person4);
        person2.setPerson(person3);
        person1.setPerson(person2);

        CustomKinveyClientCallback<SelfReferencePerson> callback = testManager.saveCustom(store, person1);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());

        CustomKinveyReadCallback<SelfReferencePerson> listCallback = testManager.findCustom(store, client.query());
        assertNotNull(listCallback.getResult());
        assertNull(listCallback.getError());
        SelfReferencePerson person = listCallback.getResult().getResult().get(0);
        assertTrue(person.getUsername().equals("person1"));
        assertTrue(person.getPerson().getUsername().equals("person2"));
        assertTrue(person.getPerson().getPerson().getUsername().equals("person3"));
        assertTrue(person.getPerson().getPerson().getPerson().getUsername().equals("person4"));
        assertTrue(person.getPerson().getPerson().getPerson().getPerson().getUsername().equals("person5"));
    }

    @Test
    public void testQueryInSelfReferenceClass() throws InterruptedException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        client.enableDebugLogging();
        TestManager<SelfReferencePerson> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<SelfReferencePerson> store = DataStore.collection(Person.COLLECTION, SelfReferencePerson.class, StoreType.SYNC, client);
        for (int i = 0; i < 10; i++) {
            SelfReferencePerson person1 = new SelfReferencePerson("person1");
            SelfReferencePerson person2 = new SelfReferencePerson("person2");
            SelfReferencePerson person3 = new SelfReferencePerson("person3");
            SelfReferencePerson person4 = new SelfReferencePerson("person4");
            SelfReferencePerson person5 = new SelfReferencePerson("person5");

            person4.setPerson(person5);
            person3.setPerson(person4);
            person2.setPerson(person3);
            person1.setPerson(person2);

            CustomKinveyClientCallback<SelfReferencePerson> callback = testManager.saveCustom(store, person1);
            assertNotNull(callback.getResult());
            assertNull(callback.getError());
        }

        Query query = client.query().in("selfReferencePerson.selfReferencePerson.username", new String[] {"person3"});
        CustomKinveyReadCallback<SelfReferencePerson> listCallback = testManager.findCustom(store, query);

        assertNotNull(listCallback.getResult());
        assertNull(listCallback.getError());
        assertTrue(listCallback.getResult().getResult().size() == 10);
    }


    @Test
    public void testSelfReferenceClassInList() throws InterruptedException {
        TestManager<PersonList> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<PersonList> store = DataStore.collection(Person.COLLECTION, PersonList.class, StoreType.SYNC, client);
        assertNotNull(store);
    }

    @Test
    public void testSelfReferenceClassInListWithData() throws InterruptedException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        client.enableDebugLogging();
        TestManager<PersonList> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<PersonList> store = DataStore.collection(Person.COLLECTION, PersonList.class, StoreType.SYNC, client);
        assertNotNull(store);

        PersonList person1 = new PersonList("person1");
        PersonList person2 = new PersonList("person2");
        PersonList person3 = new PersonList("person3");
        PersonList person4 = new PersonList("person4");
        PersonList person5 = new PersonList("person5");

        PersonList person6 = new PersonList("person6");
        PersonList person7 = new PersonList("person7");
        PersonList person8 = new PersonList("person8");
        PersonList person9 = new PersonList("person9");

        List<PersonList> list = new ArrayList<>();
        list.add(person6);
        list.add(person7);
        list.add(person8);
        list.add(person9);
        person2.setList(list);

        List<PersonList> list2 = new ArrayList<>();
        list2.add(person2);
        list2.add(person3);
        list2.add(person4);
        list2.add(person5);
        person1.setList(list2);

        CustomKinveyClientCallback<PersonList> callback = testManager.saveCustom(store, person1);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());

        CustomKinveyReadCallback<PersonList> listCallback = testManager.findCustom(store, client.query());
        assertNotNull(listCallback.getResult());
        assertNull(listCallback.getError());

        PersonList person = listCallback.getResult().getResult().get(0);
        assertEquals(person.getUsername(), "person1");
        assertEquals(person.getList().get(1).getUsername(), "person3");
        assertEquals(person.getList().get(0).getUsername(), "person2");
        assertEquals(person.getList().get(0).getList().get(0).getUsername(), "person6");

        com.kinvey.androidTest.callback.DefaultKinveyDeleteCallback deleteCallback = testManager.deleteCustom(store, client.query());
        assertNotNull(deleteCallback.getResult());
        assertNull(deleteCallback.getError());
    }

    @Test
    public void testQueryInSelfReferenceClassInList() throws InterruptedException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        client.enableDebugLogging();
        TestManager<PersonList> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<PersonList> store = DataStore.collection(Person.COLLECTION, PersonList.class, StoreType.SYNC, client);
        assertNotNull(store);
        for (int i = 0; i < 5; i++) {
            PersonList person1 = new PersonList("person1_" + i);
            PersonList person2 = new PersonList("person2_" + i);
            PersonList person3 = new PersonList("person3_" + i);
            PersonList person4 = new PersonList("person4_" + i);
            PersonList person5 = new PersonList("person5_" + i);

            PersonList person6 = new PersonList("person6_" + i);
            PersonList person7 = new PersonList("person7_" + i);
            PersonList person8 = new PersonList("person8_" + i);
            PersonList person9 = new PersonList("person9_" + i);

            List<PersonList> list = new ArrayList<>();
            list.add(person6);
            list.add(person7);
            list.add(person8);
            list.add(person9);
            person2.setList(list);

            List<PersonList> list2 = new ArrayList<>();
            list2.add(person2);
            list2.add(person3);
            list2.add(person4);
            list2.add(person5);
            person1.setList(list2);

            CustomKinveyClientCallback<PersonList> callback = testManager.saveCustom(store, person1);
            assertNotNull(callback.getResult());
            assertNull(callback.getError());
        }

        Query query = client.query().in("username", new String[] {"person1_0"});
        CustomKinveyReadCallback<PersonList> listCallback = testManager.findCustom(store, query);
        assertNotNull(listCallback.getResult());
        assertTrue(listCallback.getResult().getResult().size() == 1);
        assertNull(listCallback.getError());
        PersonList person = listCallback.getResult().getResult().get(0);
        assertEquals(person.getUsername(), "person1_0");

        query = client.query().in("list.username", new String[] {"person2_0"});
        listCallback = testManager.findCustom(store, query);
        assertNotNull(listCallback.getResult());
        assertTrue(listCallback.getResult().getResult().size() == 1);
        assertEquals(listCallback.getResult().getResult().get(0).getUsername(), "person1_0");
        assertNull(listCallback.getError());

        query = client.query().in("list.list.username", new String[] {"person6_1"});
        listCallback = testManager.findCustom(store, query);
        assertNotNull(listCallback.getResult());
        assertTrue(listCallback.getResult().getResult().size() == 1);
        assertEquals(listCallback.getResult().getResult().get(0).getUsername(), "person1_1");
        assertNull(listCallback.getError());

        query = client.query().in("list.list.username", new String[] {"person6_1", "person6_2"});
        listCallback = testManager.findCustom(store, query);
        assertNotNull(listCallback.getResult());
        assertTrue(listCallback.getResult().getResult().size() == 2);
        assertEquals(listCallback.getResult().getResult().get(0).getUsername(), "person1_1");
        assertEquals(listCallback.getResult().getResult().get(1).getUsername(), "person1_2");
        assertNull(listCallback.getError());

        query = client.query().equals("list.list.username", "person6_1");
        listCallback = testManager.findCustom(store, query);
        assertNotNull(listCallback.getResult());
        assertTrue(listCallback.getResult().getResult().size() == 1);
        assertEquals(listCallback.getResult().getResult().get(0).getUsername(), "person1_1");
        assertNull(listCallback.getError());
    }

    @Test
    public void testSelfReferenceClassInClassWithList() throws InterruptedException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        client.enableDebugLogging();
        TestManager<PersonList> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<PersonList> store = DataStore.collection(Person.COLLECTION, PersonList.class, StoreType.SYNC, client);
        assertNotNull(store);

        PersonList person1 = new PersonList("person1");

        PersonList person2 = new PersonList("person2");

        PersonList person3 = new PersonList("person3");
        PersonList person4 = new PersonList("person4");
        PersonList person5 = new PersonList("person5");
        PersonList person6 = new PersonList("person6");

        List<PersonList> list = new ArrayList<>();
        list.add(person3);
        list.add(person4);
        list.add(person5);
        list.add(person6);
        person2.setList(list);

        person1.setPersonList(person2);

        CustomKinveyClientCallback<PersonList> callback = testManager.saveCustom(store, person1);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());

        CustomKinveyReadCallback<PersonList> listCallback = testManager.findCustom(store, client.query());
        assertNotNull(listCallback.getResult());
        assertNull(listCallback.getError());

        PersonList person = listCallback.getResult().getResult().get(0);
        assertEquals(person.getUsername(), "person1");
        assertEquals(person.getPersonList().getUsername(), "person2");
        assertEquals(person.getPersonList().getList().get(0).getUsername(), "person3");

        com.kinvey.androidTest.callback.DefaultKinveyDeleteCallback deleteCallback = testManager.deleteCustom(store, client.query());
        assertNotNull(deleteCallback.getResult());
        assertNull(deleteCallback.getError());
    }

// filed as MLIBZ-2647
/*
    @Test
    public void testSelfReferenceClassInClassWithArray() throws InterruptedException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        client.enableDebugLogging();
        TestManager<PersonArray> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<PersonArray> store = DataStore.collection(PersonArray.COLLECTION, PersonArray.class, StoreType.SYNC, client);
        assertNotNull(store);

        PersonArray person1 = new PersonArray("person1");
        PersonArray person2 = new PersonArray("person2");
        PersonArray person3 = new PersonArray("person3");
        PersonArray person4 = new PersonArray("person4");
        PersonArray person5 = new PersonArray("person5");
        PersonArray person6 = new PersonArray("person6");

        PersonArray[] array = new PersonArray[4];
        array[0] = person3;
        array[1] = person4;
        array[2] = person5;
        array[3] = person6;
        person2.setArray(array);

        person1.setPersonArray(person2);

        CustomKinveyClientCallback<PersonArray> callback = testManager.saveCustom(store, person1);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());

        CustomKinveyReadCallback<PersonArray> listCallback = testManager.findCustom(store, client.query());
        assertNotNull(listCallback.getResult());
        assertNull(listCallback.getError());

        PersonArray person = listCallback.getResult().getResult().get(0);
        assertEquals(person.getUsername(), "person1");
        assertEquals(person.getPersonArray().getUsername(), "person2");
        assertEquals(person.getPersonArray().getArray()[0].getUsername(), "person3");

        com.kinvey.androidTest.callback.DefaultKinveyDeleteCallback deleteCallback = testManager.deleteCustom(store, client.query());
        assertNotNull(deleteCallback.getResult());
        assertNull(deleteCallback.getError());
    }
*/

    @Test
    public void testSelfReferenceBookAuthorBook() throws InterruptedException {
        TestManager<PersonWithAddress> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<PersonWithAddress> store = DataStore.collection(Person.COLLECTION, PersonWithAddress.class, StoreType.SYNC, client);
        assertNotNull(store);

        PersonWithAddress person = new PersonWithAddress("person");
        Address address = new Address("test_address");
        PersonWithAddress person2 = new PersonWithAddress("person_2");
        address.setPerson(person2);
        person.setAddress(address);

        CustomKinveyClientCallback<PersonWithAddress> callback = testManager.saveCustom(store, person);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());

        com.kinvey.androidTest.callback.DefaultKinveyDeleteCallback deleteCallback = testManager.deleteCustom(store, client.query());
        assertNotNull(deleteCallback.getResult());
        assertNull(deleteCallback.getError());
    }

    //Check person_person_list and person_list_person initialization
    @Test
    public void testInitializationPersonWithPersonAndList() throws InterruptedException {
        TestManager<PersonWithPersonAndList> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<PersonWithPersonAndList> store = DataStore.collection(Person.COLLECTION, PersonWithPersonAndList.class, StoreType.SYNC, client);
        assertNotNull(store);

        PersonWithPersonAndList person = new PersonWithPersonAndList("person");
        person.setPerson(new PersonWithPersonAndList("person_1"));
        List<PersonWithPersonAndList> list = new ArrayList<>();
        list.add(new PersonWithPersonAndList("person_2"));
        person.setList(list);

        CustomKinveyClientCallback<PersonWithPersonAndList> callback = testManager.saveCustom(store, person);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());

        DynamicRealm realm = RealmCacheManagerUtil.getRealm(client);
        try {
            realm.beginTransaction();
            assertEquals(realm.where(TableNameManagerUtil.getShortName(Person.COLLECTION, realm)).count(), 1);
            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "__kmd", realm)).count(), 1);
            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "__acl", realm)).count(), 1);

            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "_person", realm)).count(), 1);
            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "_list", realm)).count(), 1);
            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "_list", realm) + "_person", realm)).count(), 0);
            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "_person", realm) + "_list", realm)).count(), 0);

            realm.commitTransaction();
        } finally {
            realm.close();
        }

    }

    //Check possibility delete self-reference object
    @Test
    public void testDeletePersonWithPersonAndList() throws InterruptedException {
        TestManager<PersonWithPersonAndList> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<PersonWithPersonAndList> store = DataStore.collection(Person.COLLECTION, PersonWithPersonAndList.class, StoreType.SYNC, client);
        assertNotNull(store);

        PersonWithPersonAndList person = new PersonWithPersonAndList("person");
        person.setPerson(new PersonWithPersonAndList("person_1"));
        List<PersonWithPersonAndList> list = new ArrayList<>();
        list.add(new PersonWithPersonAndList("person_2"));
        person.setList(list);

        CustomKinveyClientCallback<PersonWithPersonAndList> callback = testManager.saveCustom(store, person);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());

        com.kinvey.androidTest.callback.DefaultKinveyDeleteCallback deleteCallback = testManager.deleteCustom(store, client.query());
        assertNotNull(deleteCallback.getResult());
        assertNull(deleteCallback.getError());
    }

    @Test
    public void testDeleteSelfReferenceManyObject() throws InterruptedException {
        TestManager<PersonWithPersonAndList> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<PersonWithPersonAndList> store = DataStore.collection(Person.COLLECTION, PersonWithPersonAndList.class, StoreType.SYNC, client);
        assertNotNull(store);

        PersonWithPersonAndList person = new PersonWithPersonAndList("person");
        PersonWithPersonAndList person1 = new PersonWithPersonAndList("person1");
        PersonWithPersonAndList person2 = new PersonWithPersonAndList("person2");
        PersonWithPersonAndList person3 = new PersonWithPersonAndList("person3");
        PersonWithPersonAndList person4 = new PersonWithPersonAndList("person4");
        PersonWithPersonAndList person5 = new PersonWithPersonAndList("person5");
        PersonWithPersonAndList person6 = new PersonWithPersonAndList("person6");
        PersonWithPersonAndList person7 = new PersonWithPersonAndList("person7");
        PersonWithPersonAndList person8 = new PersonWithPersonAndList("person8");
        person7.setPerson(person8);
        person6.setPerson(person7);
        person5.setPerson(person6);
        person4.setPerson(person5);
        person3.setPerson(person4);
        person2.setPerson(person3);
        person1.setPerson(person2);
        person.setPerson(person1);
        List<PersonWithPersonAndList> list = new ArrayList<>();
        list.add(new PersonWithPersonAndList("person_2"));
        person.setList(list);

        CustomKinveyClientCallback<PersonWithPersonAndList> callback = testManager.saveCustom(store, person);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());

        com.kinvey.androidTest.callback.DefaultKinveyDeleteCallback deleteCallback = testManager.deleteCustom(store, client.query());
        assertNotNull(deleteCallback.getResult());
        assertNull(deleteCallback.getError());
    }

    @Test //Model: PersonRoomAddressPerson - Room - Address - PersonRoomAddressPerson
    public void testSelfReferencePersonRoomAddressPerson() throws InterruptedException {
        TestManager<PersonRoomAddressPerson> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<PersonRoomAddressPerson> store = DataStore.collection(Person.COLLECTION, PersonRoomAddressPerson.class, StoreType.SYNC, client);
        assertNotNull(store);

        PersonRoomAddressPerson person = new PersonRoomAddressPerson();
        person.setName("person_1");
        Room room = new Room();
        room.setName("room_name");
        RoomAddress address = new RoomAddress();
        address.setName("address_name");
        PersonRoomAddressPerson person2 = new PersonRoomAddressPerson();
        person2.setName("person_2");
        address.setPerson(person2);
        room.setRoomAddress(address);
        person.setRoom(room);

        CustomKinveyClientCallback<PersonRoomAddressPerson> callback = testManager.saveCustom(store, person);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());

        PersonRoomAddressPerson result = callback.getResult();
        assertEquals("person_1", result.getName());
        assertNotNull(result.getRoom());
        assertEquals("room_name", result.getRoom().getName());
        assertNotNull(result.getRoom().getRoomAddress());
        assertEquals("address_name", result.getRoom().getRoomAddress().getName());
        assertNotNull(result.getRoom().getRoomAddress().getPerson());
        assertEquals("person_2", result.getRoom().getRoomAddress().getPerson().getName());
    }

    @Test //Model: PersonRoomPerson - RoomPerson - PersonRoomPerson
    public void testSelfReferencePersonRoomPerson() throws InterruptedException {
        TestManager<PersonRoomPerson> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<PersonRoomPerson> store = DataStore.collection(Person.COLLECTION, PersonRoomPerson.class, StoreType.SYNC, client);
        assertNotNull(store);

        PersonRoomPerson person = new PersonRoomPerson();
        PersonRoomPerson personForList = new PersonRoomPerson();
        PersonRoomPerson personForList2 = new PersonRoomPerson();
        person.setName("person_1");
        personForList.setName("personForList_1");
        personForList2.setName("personForList_2");
        RoomPerson room = new RoomPerson();
        room.setName("room_name");
        PersonRoomPerson person2 = new PersonRoomPerson();
        person2.setName("person_2");

        List<PersonRoomPerson> personList = new ArrayList<>();
        personList.add(personForList);
        personList.add(personForList2);
        person.setPersonList(personList);

        person2.setPersonList(personList);
        room.setPerson(person2);
        person.setRoom(room);

        CustomKinveyClientCallback<PersonRoomPerson> callback = testManager.saveCustom(store, person);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());

        PersonRoomPerson result = callback.getResult();
        assertEquals("person_1", result.getName());
        assertEquals("personForList_1", result.getPersonList().get(0).getName());
        assertEquals("personForList_2", result.getPersonList().get(1).getName());
        assertNotNull(result.getRoom());
        assertEquals("room_name", result.getRoom().getName());
        assertNotNull(result.getRoom().getPerson());
        assertEquals("person_2", result.getRoom().getPerson().getName());
        assertEquals("personForList_1", result.getRoom().getPerson().getPersonList().get(0).getName());
        assertEquals("personForList_2", result.getRoom().getPerson().getPersonList().get(1).getName());
    }

    @Test
    public void testSelfReferenceClassComplex() throws InterruptedException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        client.enableDebugLogging();
        TestManager<PersonList> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<PersonList> store = DataStore.collection(Person.COLLECTION, PersonList.class, StoreType.SYNC, client);
        assertNotNull(store);

        PersonList person1 = new PersonList("person1");
        PersonList person2 = new PersonList("person2");
        PersonList person3 = new PersonList("person3");
        PersonList person4 = new PersonList("person4");
        PersonList person5 = new PersonList("person5");
        PersonList person6 = new PersonList("person6");
        PersonList person7 = new PersonList("person7");
        PersonList person8 = new PersonList("person8");
        PersonList person9 = new PersonList("person9");
        PersonList person10 = new PersonList("person10");
        List<PersonList> list = new ArrayList<>();
        list.add(person3);
        list.add(person4);
        list.add(person5);
        list.add(person6);
        person2.setList(list);
        person8.setPersonList(person7);
        person9.setPersonList(person8);
        person10.setPersonList(person9);
        person2.setPersonList(person10);
        person1.setPersonList(person2);

        CustomKinveyClientCallback<PersonList> callback = testManager.saveCustom(store, person1);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());

        CustomKinveyReadCallback<PersonList> listCallback = testManager.findCustom(store, client.query());
        assertNotNull(listCallback.getResult());
        assertNull(listCallback.getError());

        PersonList person = listCallback.getResult().getResult().get(0);
        assertEquals(person.getUsername(), "person1");
        assertEquals(person.getPersonList().getUsername(), "person2");
        assertEquals(person.getPersonList().getList().get(0).getUsername(), "person3");
        assertEquals(person.getPersonList().getList().get(1).getUsername(), "person4");
        assertEquals(person.getPersonList().getList().get(2).getUsername(), "person5");
        assertEquals(person.getPersonList().getList().get(3).getUsername(), "person6");
        assertEquals(person.getPersonList().getPersonList().getUsername(), "person10");
        assertEquals(person.getPersonList().getPersonList().getPersonList().getUsername(), "person9");
        assertEquals(person.getPersonList().getPersonList().getPersonList().getPersonList().getUsername(), "person8");
        assertEquals(person.getPersonList().getPersonList().getPersonList().getPersonList().getPersonList().getUsername(), "person7");
    }


    @Test
    public void testDeleteInternalTables() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.syncCount() == 0);
        assertTrue(store.count() == 0);

        Person person = new Person(TEST_USERNAME);
        person.setAuthor(new Author("author_name"));
        com.kinvey.androidTest.callback.DefaultKinveyClientCallback saveCallback = testManager.save(store, person);
        assertNotNull(saveCallback.getResult());
        assertNull(saveCallback.getError());
        assertTrue(store.syncCount() == 1);
        assertTrue(store.count() == 1);

        DynamicRealm realm = RealmCacheManagerUtil.getRealm(client);
        int resSize;
        try {
            realm.beginTransaction();
            resSize = realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "_author", realm)).findAll().size();
            realm.commitTransaction();
        } finally {
            realm.close();
        }
        assertEquals(1, resSize); // check that item in sub table was created

        testManager.delete(store, saveCallback.getResult().getId());

        realm = RealmCacheManagerUtil.getRealm(client);
        try {
            realm.beginTransaction();
            resSize = realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "_author", realm)).findAll().size();
            realm.commitTransaction();
        } finally {
            realm.close();
        }
        assertEquals(0, resSize); // check that item in sub table was deleted after call 'clear'
        assertTrue(store.count() == 0);
    }

    @Test
    public void testClearInternalTables() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.syncCount() == 0);
        assertTrue(store.count() == 0);

        Person person = new Person(TEST_USERNAME);
        person.setAuthor(new Author("author_name"));
        com.kinvey.androidTest.callback.DefaultKinveyClientCallback saveCallback = testManager.save(store, person);
        assertNotNull(saveCallback.getResult());
        assertNull(saveCallback.getError());
        assertTrue(store.syncCount() == 1);
        assertTrue(store.count() == 1);

        DynamicRealm realm = RealmCacheManagerUtil.getRealm(client);
        int resSize;
        try {
            realm.beginTransaction();
            resSize = realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "_author", realm)).findAll().size();
            realm.commitTransaction();
        } finally {
            realm.close();
        }
        assertEquals(1, resSize); // check that item in sub table was created

        store.clear();

        realm = RealmCacheManagerUtil.getRealm(client);
        try {
            realm.beginTransaction();
            resSize = realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "_author", realm)).findAll().size();
            realm.commitTransaction();
        } finally {
            realm.close();
        }
        assertEquals(0, resSize); // check that item in sub table was deleted after call 'clear'
        assertTrue(store.count() == 0);
    }

    @Test
    public void testClearCollectionIfModelClassChanged() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.syncCount() == 0);
        assertTrue(store.count() == 0);

        Person person = new Person(TEST_USERNAME);
        com.kinvey.androidTest.callback.DefaultKinveyClientCallback saveCallback = testManager.save(store, person);
        assertNotNull(saveCallback.getResult());
        assertNull(saveCallback.getError());
        assertTrue(store.syncCount() == 1);
        assertTrue(store.count() == 1);

        Context mockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_1");
        client = new Client.Builder(mockContext).build();

        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.syncCount() == 1);
        assertTrue(store.count() == 1);

        DynamicRealm realm = RealmCacheManagerUtil.getRealm(client);
        try {
            realm.beginTransaction();
            RealmCacheManagerUtil.setTableHash(client, Person.COLLECTION, "hashTest", realm);
            realm.commitTransaction();
        } finally {
            realm.close();
        }

        mockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_2");
        client = new Client.Builder(mockContext).build();

        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.syncCount() == 0);
        assertTrue(store.count() == 0);

        saveCallback = null;
        person = new Person(TEST_USERNAME);
        saveCallback = testManager.save(store, person);
        assertNotNull(saveCallback.getResult());
        assertNull(saveCallback.getError());
        assertTrue(store.syncCount() == 1);
        assertTrue(store.count() == 1);
        boolean isOneTable = false;
        realm = RealmCacheManagerUtil.getRealm(client);
        try {
            realm.beginTransaction();
            isOneTable = isCollectionHasOneTable(Person.COLLECTION, realm);
            realm.commitTransaction();
        } finally {
            realm.close();
        }
        assertTrue(isOneTable);
    }

    private boolean isCollectionHasOneTable(String collection, DynamicRealm realm) {
        RealmSchema currentSchema = realm.getSchema();
        String className;
        int schemaCounter = 0;
        Set<RealmObjectSchema> schemas = currentSchema.getAll();
        for (RealmObjectSchema schema : schemas) {
            className = schema.getClassName();
            if (className.equals(TableNameManagerUtil.getShortName(collection, realm))) {
                schemaCounter++;
            }
        }
        return schemaCounter == 1;
    }

    @Test
    public void testGrowCollectionExponentially() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        testManager.pullCustom(store, client.query());
        testManager.cleanBackendDataStore(store);
        testManager.push(store);
        assertTrue(store.syncCount() == 0);
        assertTrue(store.count() == 0);

        for (int i = 0; i < 3; i++) {
            Person person = new Person(TEST_USERNAME + i);
            person.setAuthor(new Author("Author_" + i));
            com.kinvey.androidTest.callback.DefaultKinveyClientCallback saveCallback = testManager.save(store, person);
            assertNotNull(saveCallback.getResult());
            assertNull(saveCallback.getError());
            assertTrue(store.syncCount() == i + 1);
            assertTrue(store.count() == i + 1);
        }
        testManager.push(store);

        DynamicRealm realm = RealmCacheManagerUtil.getRealm(client);
        try {
            realm.beginTransaction();
            checkInternalTablesHasItems(3, Person.COLLECTION, realm);
            realm.commitTransaction();
        } finally {
            realm.close();
        }

        testManager.pullCustom(store, client.query());
        testManager.pullCustom(store, client.query());
        testManager.pullCustom(store, client.query());

        realm = RealmCacheManagerUtil.getRealm(client);
        try {
            realm.beginTransaction();
            checkInternalTablesHasItems(3, Person.COLLECTION, realm);
            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }

    /**
     * Check that main and each internal tables have correct items count
     * @param expectedItemsCount expected items count
     */
    private void checkInternalTablesHasItems(int expectedItemsCount, String collection, DynamicRealm realm) {
        RealmSchema currentSchema = realm.getSchema();
        String originalName;
        String className;
        Set<RealmObjectSchema> schemas = currentSchema.getAll();
        for (RealmObjectSchema schema : schemas) {
            className = schema.getClassName();
            //search class
            if (className.equals(TableNameManagerUtil.getShortName(collection, realm))) {
                assertTrue(realm.where(TableNameManagerUtil.getShortName(collection, realm)).count() == expectedItemsCount);
                //search sub-classes
                for (RealmObjectSchema subClassSchema : schemas) {
                    originalName = TableNameManagerUtil.getOriginalName(subClassSchema.getClassName(), realm);
                    if (originalName != null && originalName.startsWith(className + Constants.UNDERSCORE)) {
                        checkInternalTablesHasItems(expectedItemsCount, originalName, realm);
                    }
                }
            }
        }
    }

    @Test
    public void testSaveItemToInternalTable() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.syncCount() == 0);
        assertTrue(store.count() == 0);

        Person person = new Person(TEST_USERNAME);
        person.setAuthor(new Author("Author_"));
        com.kinvey.androidTest.callback.DefaultKinveyClientCallback saveCallback = testManager.save(store, person);
        assertNotNull(saveCallback.getResult());
        assertNull(saveCallback.getError());
        assertTrue(store.syncCount() == 1);
        assertTrue(store.count() == 1);

        DynamicRealm realm = RealmCacheManagerUtil.getRealm(client);
        try {
            realm.beginTransaction();
            assertEquals(realm.where(TableNameManagerUtil.getShortName(Person.COLLECTION, realm)).count(), 1);
            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "__kmd", realm)).count(), 1);
            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "__acl", realm)).count(), 1);

            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "_author", realm)).count(), 1);

            assertNull(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "_author", realm) + "__kmd", realm));
            assertNull(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "_author", realm) + "__acl", realm));

            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }

    @Test
    public void testInitializationInternalTable() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.syncCount() == 0);
        assertTrue(store.count() == 0);

        DynamicRealm realm = RealmCacheManagerUtil.getRealm(client);
        try {
            realm.beginTransaction();
            assertEquals(realm.where(TableNameManagerUtil.getShortName(Person.COLLECTION, realm)).count(), 0);
            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "__kmd", realm)).count(), 0);
            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "__acl", realm)).count(), 0);

            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "_author", realm)).count(), 0);

            assertNull(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "_author", realm) + "__kmd", realm));
            assertNull(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(Person.COLLECTION, realm) + "_author", realm) + "__acl", realm));

            assertEquals(realm.where(TableNameManagerUtil.getShortName("sync", realm)).count(), 0);
            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("sync", realm) + "_meta", realm)).count(), 0);
            assertNull(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("sync", realm) + "_meta", realm) + "__kmd", realm));
            assertNull(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("sync", realm) + "_meta", realm) + "__acl", realm));
            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("sync", realm) + "__kmd", realm)).count(), 0);
            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("sync", realm) + "__acl", realm)).count(), 0);
            assertEquals(realm.where(TableNameManagerUtil.getShortName("syncitems", realm)).count(), 0);
            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("syncitems", realm) + "_meta", realm)).count(), 0);

            assertNull(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("syncitems", realm) + "_meta", realm) + "__kmd", realm));
            assertNull(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("syncitems", realm) + "_meta", realm) + "__acl", realm));
            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("syncitems", realm) + "__kmd", realm)).count(), 0);
            assertEquals(realm.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("syncitems", realm) + "__acl", realm)).count(), 0);
            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }

    @Test
    public void testHashCode() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        cleanBackendDataStore(store);
        store.syncBlocking(null);
        Person person = createPerson(TEST_USERNAME);
        person.setAuthor(new Author("author"));
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        store.syncBlocking(null);
        List<Person> personList = store.find().getResult();
        Person person1 = personList.get(0);
        int hashcode = person1.hashCode();
        assertNotNull(hashcode);

        Person theSamePerson = store.find(client.query().equals("username", TEST_USERNAME)).getResult().get(0);
        assertEquals(hashcode, theSamePerson.hashCode());

        save(store, createPerson(TEST_USERNAME + 2));
        Person theSamePerson2 = store.find(client.query().equals("author.name", "author")).getResult().get(0);
        assertEquals(hashcode, theSamePerson2.hashCode());

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
    @Ignore //ignore while test app won't support delta cache
    public void testDeltaCache() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
        client.getSyncManager().clear(Person.COLLECTION);
        clearBackend(store);

        Person person = new Person();
        person.setUsername("name");
        person.set("CustomField", "CustomValue");
        store.save(person);

        store.syncBlocking(null);
        Person person2 = new Person();
        person2.setUsername("changed name 1");
        person2.set("CustomField", "CustomValueChanged ");
        store.save(person2);

        Person person3 = new Person();
        person3.setUsername("changed name 2");
        person3.set("CustomField", "CustomValueChanged 2");
        store.save(person3);

        DefaultKinveySyncCallback callback = sync(store, DEFAULT_TIMEOUT);
        assertNull(callback.error);
        assertNotNull(callback.kinveyPushResponse);
        assertNotNull(callback.kinveyPullResponse);

        List<Person> personList = store.find().getResult();
        Person person1 = personList.get(0);
        assertNotNull(person1);
        assertNotNull(person1.getUsername());
    }

    @Test
    public void testDeltaCacheAfterDataStoreInitialization() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        store.setDeltaSetCachingEnabled(true);
        assertTrue(store.isDeltaSetCachingEnabled());
    }

    @Test
    public void testUpdateInternalObject() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);

        Person person = new Person();
        person.setUsername("person_name");
        Author author = new Author("author_name");
        person.setAuthor(author);
        store.save(person);

        Person newPerson = store.find(client.query().equals("username", "person_name")).getResult().get(0);
        Author updatedAuthor = new Author("updated_author_name");
        newPerson.setAuthor(updatedAuthor);
        newPerson.setUsername("updated_person_name");
        store.save(newPerson);

        Person updatedPerson = store.find(client.query().equals("username", "updated_person_name")).getResult().get(0);
        assertNotNull(updatedPerson);
        assertEquals("updated_person_name", updatedPerson.getUsername());
        assertEquals("updated_author_name", updatedPerson.getAuthor().getName());

        updatedPerson.setAuthor(null);
        store.save(updatedPerson);

        Person updatedPersonWithoutAuthor = store.find(client.query().equals("username", "updated_person_name")).getResult().get(0);
        assertNotNull(updatedPersonWithoutAuthor);
        assertEquals("updated_person_name", updatedPersonWithoutAuthor.getUsername());
        assertNull(updatedPersonWithoutAuthor.getAuthor());
    }

    @Test
    public void testUpdateInternalList() throws InterruptedException, IOException {
        DataStore<PersonList> store = DataStore.collection(PersonList.COLLECTION, PersonList.class, StoreType.SYNC, client);

        PersonList person = new PersonList();
        person.setUsername("person_name");
        List<PersonList> list = new ArrayList<>();
        list.add(new PersonList("person_name_in_list_1"));
        person.setList(list);
        store.save(person);

        PersonList newPerson = store.find(client.query().equals("username", "person_name")).getResult().get(0);
        list = new ArrayList<>();
        list.add(new PersonList("person_name_in_list_2"));
        newPerson.setList(list);
        newPerson.setUsername("updated_person_name");
        store.save(newPerson);

        PersonList updatedPerson = store.find(client.query().equals("username", "updated_person_name")).getResult().get(0);
        assertNotNull(updatedPerson);
        assertEquals("updated_person_name", updatedPerson.getUsername());
        assertEquals("person_name_in_list_2", updatedPerson.getList().get(0).getUsername());
        updatedPerson.getList().clear();
        updatedPerson.setList(updatedPerson.getList());
        store.save(updatedPerson);

        PersonList updatedPersonWithoutList = store.find(client.query().equals("username", "updated_person_name")).getResult().get(0);
        assertNotNull(updatedPersonWithoutList);
        assertEquals("updated_person_name", updatedPersonWithoutList.getUsername());
        assertEquals(0, updatedPersonWithoutList.getList().size());
    }

    @Test
    public void testCreateUpdateDeleteSync() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        clearBackend(DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client));
        store.clear();

        Person person = createPerson(TEST_USERNAME);
        DefaultKinveyClientCallback callback = save(store, person);
        assertNotNull(callback.result);
        assertNotNull(callback.result.getUsername());
        sync(store, DEFAULT_TIMEOUT);

        person = find(store, DEFAULT_TIMEOUT).result.getResult().get(0);
        person.setUsername(TEST_USERNAME_2);
        callback = save(store, person);
        assertNotNull(callback.result);
        assertNotNull(callback.result.getUsername());
        assertEquals(TEST_USERNAME_2, callback.result.getUsername());

        DefaultKinveyDeleteCallback deleteCallback = delete(store, callback.result.getId(), DEFAULT_TIMEOUT);
        assertNotNull(deleteCallback.result);

        sync(store, DEFAULT_TIMEOUT);

        assertEquals(0, find(store, client.query().equals(Constants._ID, callback.result.getId()), DEFAULT_TIMEOUT).result.getResult().size());
        assertEquals(0, client.getSyncManager().getCount(Person.COLLECTION));
    }

    @Test
    public void testCreateDeleteSync() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = createPerson(TEST_USERNAME);
        DefaultKinveyClientCallback callback = save(store, person);
        assertNotNull(callback.result);
        assertNotNull(callback.result.getUsername());

        DefaultKinveyDeleteCallback deleteCallback = delete(store, callback.result.getId(), DEFAULT_TIMEOUT);
        assertNotNull(deleteCallback.result);

        sync(store, DEFAULT_TIMEOUT);

        assertEquals(0, find(store, client.query().equals(Constants._ID, callback.result.getId()), DEFAULT_TIMEOUT).result.getResult().size());
        assertEquals(0, client.getSyncManager().getCount(Person.COLLECTION));
    }

    @Test
    public void testAsyncPullRequestConstructors() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
                AsyncPullRequest pullRequest = new AsyncPullRequest(store, new Query(), null);
                assertNotNull(pullRequest);
                latch.countDown();
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
    }

    @Test
    public void testFindByIds() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        store.clear();
        client.getSyncManager().clear(Person.COLLECTION);
        List<String> ids = new ArrayList<>();
        DefaultKinveyClientCallback saveCallback = save(store, createPerson(TEST_USERNAME));
        assertNotNull(saveCallback.result.getId());
        ids.add(saveCallback.result.getId());
        saveCallback = save(store, createPerson(TEST_USERNAME_2));
        assertNotNull(saveCallback.result.getId());
        ids.add(saveCallback.result.getId());
        DefaultKinveyReadCallback kinveyListCallback = find(store, ids, DEFAULT_TIMEOUT, null);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertEquals(2, kinveyListCallback.result.getResult().size());
        store.clear();
    }

    @Test
    public void testFindByIdsCached() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        TestManager<Person> testManager = new TestManager<>();
        testManager.cleanBackend(store, StoreType.CACHE);
        List<String> ids = new ArrayList<>();
        DefaultKinveyClientCallback saveCallback = save(store, createPerson(TEST_USERNAME));
        assertNotNull(saveCallback.result.getId());
        ids.add(saveCallback.result.getId());
        saveCallback = save(store, createPerson(TEST_USERNAME_2));
        assertNotNull(saveCallback.result.getId());
        ids.add(saveCallback.result.getId());
        CustomKinveyCachedCallback<KinveyReadResponse<Person>> cachedCallback = new CustomKinveyCachedCallback<>();
        DefaultKinveyReadCallback kinveyListCallback = find(store, ids, DEFAULT_TIMEOUT, cachedCallback);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertEquals(2, kinveyListCallback.result.getResult().size());
        assertNotNull(cachedCallback.result);
        assertNull(cachedCallback.error);
        assertNotNull(cachedCallback.result);
        assertNotNull(cachedCallback.result.getResult());
        assertEquals(2, cachedCallback.result.getResult().size());
        testManager.cleanBackend(store, StoreType.CACHE);
    }

    private DefaultKinveyReadCallback find(final DataStore<Person> store, final Iterable<String> ids, int seconds, final CustomKinveyCachedCallback<KinveyReadResponse<Person>> cachedClientCallback) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(cachedClientCallback != null ? 2 : 1);
        if (cachedClientCallback != null) {
            cachedClientCallback.setLatch(latch);
        }
        final DefaultKinveyReadCallback callback = new DefaultKinveyReadCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                if (cachedClientCallback != null) {
                    store.find(ids, callback, cachedClientCallback);
                } else {
                    store.find(ids, callback);
                }
            }
        });
        looperThread.start();
        latch.await(seconds, TimeUnit.SECONDS);
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testCount() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        store.clear();
        client.getSyncManager().clear(Person.COLLECTION);
        DefaultKinveyClientCallback saveCallback = save(store, createPerson(TEST_USERNAME));
        assertNotNull(saveCallback.result.getId());
        saveCallback = save(store, createPerson(TEST_USERNAME_2));
        assertNotNull(saveCallback.result.getId());
        int count = findCount(store, DEFAULT_TIMEOUT, null).result;
        assertEquals(2, count);
        store.clear();
    }

    @Test
    public void testQuery() {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertNotNull(store.query());
    }

}
