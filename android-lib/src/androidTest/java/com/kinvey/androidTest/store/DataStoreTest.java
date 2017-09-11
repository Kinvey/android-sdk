package com.kinvey.androidTest.store;


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
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyPurgeCallback;
import com.kinvey.android.model.User;
import com.kinvey.android.store.DataStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.android.sync.KinveyPullCallback;
import com.kinvey.android.sync.KinveyPullResponse;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.android.sync.KinveyPushResponse;
import com.kinvey.android.sync.KinveySyncCallback;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.cache.ICacheManager;
import com.kinvey.java.cache.KinveyCachedClientCallback;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    private static final String LMT = "lmt";
    private static final int DEFAULT_TIMEOUT = 60;
    private static final int LONG_TIMEOUT = 6*DEFAULT_TIMEOUT;

    private Client client;

    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        final CountDownLatch latch = new CountDownLatch(1);
        LooperThread looperThread = null;
        if (!client.isUserLoggedIn()) {
            looperThread = new LooperThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        UserStore.login(client, new KinveyClientCallback<User>() {
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

    private static class DefaultKinveySyncCallback implements KinveySyncCallback<Person> {

        private CountDownLatch latch;
        KinveyPushResponse kinveyPushResponse;
        KinveyPullResponse<Person> kinveyPullResponse;
        Throwable error;

        DefaultKinveySyncCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(KinveyPushResponse kinveyPushResponse, KinveyPullResponse<Person> kinveyPullResponse) {
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
        public void onPullSuccess(KinveyPullResponse<Person> kinveyPullResponse) {
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

    private static class DefaultKinveyPullCallback implements KinveyPullCallback<Person> {

        private CountDownLatch latch;
        KinveyPullResponse<Person> result;
        Throwable error;

        DefaultKinveyPullCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(KinveyPullResponse<Person> result) {
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

    private static class DefaultKinveyListCallback implements KinveyListCallback<Person> {

        private CountDownLatch latch;
        List<Person> result;
        Throwable error;

        DefaultKinveyListCallback(CountDownLatch latch) {
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

    private Person createPerson(String name) {
        Person person = new Person();
        person.setUsername(name);
        return person;
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

    private DefaultKinveyListCallback find(final DataStore<Person> store, final Query query, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyListCallback callback = new DefaultKinveyListCallback(latch);
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
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = createPerson(TEST_USERNAME);
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        String userId = saveCallback.result.getId();
        Query query = client.query();
        query = query.equals(ID, userId);
        DefaultKinveyListCallback kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.size() > 0);
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

    private DefaultKinveyPurgeCallback purge(final DataStore<Person> store) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyPurgeCallback callback = new DefaultKinveyPurgeCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.purge(callback);
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
        DefaultKinveyPurgeCallback purgeCallback = purge(store);
        assertNull(purgeCallback.error);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
    }

    @Test
    public void testPurgeInvalidDataStoreType() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        client.getSyncManager().clear(Person.COLLECTION);
        save(store, createPerson(TEST_USERNAME));
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
        DefaultKinveyPurgeCallback purgeCallback = purge(store);
        assertNotNull(purgeCallback.error);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
    }

    @Test
    public void testPurgeTimeoutError() throws InterruptedException, IOException {
        final ChangeTimeout changeTimeout = new ChangeTimeout();
        HttpRequestInitializer initializer = new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws SocketTimeoutException {
                changeTimeout.initialize(request);
            }
        };

        client = new Client.Builder(client.getContext())
                .setHttpRequestInitializer(initializer)
                .build();

        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);

        Person person = createPerson(TEST_USERNAME);
        save(store, person);
        save(store, person);

        DefaultKinveyPurgeCallback purgeCallback = purge(store);
        assertNotNull(purgeCallback.error);
        assertTrue(purgeCallback.error.getClass() == SocketTimeoutException.class);
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
        save(store, createPerson(TEST_USERNAME));
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveyPushCallback pushCallback = push(store, 120);
        assertNull(pushCallback.error);
        assertTrue(pushCallback.result.getListOfExceptions().size() == 0);
        assertNotNull(pushCallback.result);
        assertTrue(client.getSyncManager().getCount(Person.COLLECTION) == 0);
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
//        Query query = client.query();
//        query = query.notEqual("age", "100500");
        DefaultKinveyDeleteCallback deleteCallback = delete(store, new Query().notEqual("age", "100500"));
        assertNull(deleteCallback.error);
        DefaultKinveyPushCallback pushCallback = push(store, 120);
        assertNull(pushCallback.error);
        assertTrue(pushCallback.result.getListOfExceptions().size() == 0);
        Log.d("testPull", " : clearing backend store successful");
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
        assertTrue(pullCallback.result.getResult().size() == 3);
        assertTrue(pullCallback.result.getResult().size() == getCacheSize(StoreType.CACHE));

        //cleaning cache store
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.CACHE.ttl).clear();

        //test pull only 1 item by query
        Query query = client.query();
        query = query.equals(USERNAME, victor.getUsername());
        pullCallback = pull(store, query);
        assertNull(pullCallback.error);
        assertNotNull(pullCallback.result);
        assertTrue(pullCallback.result.getResult().size() == 1);
        assertTrue(pullCallback.result.getResult().get(0).getUsername().equals(victor.getUsername()));
        assertTrue(pullCallback.result.getResult().size() == getCacheSize(StoreType.SYNC));

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
        assertTrue(pullCallback.result.getResult().size() == 0);
        assertTrue(pullCallback.result.getResult().size() == getCacheSize(StoreType.SYNC));

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
        assertTrue(pullCallback.result.getResult().size() == 1);
        assertTrue(pullCallback.result.getResult().get(0).getUsername().equals(victor.getUsername()));
        assertTrue(pullCallback.result.getResult().size() == getCacheSize(StoreType.CACHE));
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

        DefaultKinveyListCallback findCallback = find(store, query, 120);
        assertNull(findCallback.error);
        assertNotNull(findCallback.result);
        assertTrue(findCallback.result.size() == 0);
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

        DefaultKinveyListCallback kinveyListCallback;
        Query query = client.query();
        for (int i = 0; i < 5; i++) {
            query.setSkip(skip);
            query.setLimit(limit);
            kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
            assertNull(kinveyListCallback.error);
            assertNotNull(kinveyListCallback.result);
            assertEquals(kinveyListCallback.result.get(0).getUsername(), "Person_" + skip);
            assertEquals(kinveyListCallback.result.get(1).getUsername(), "Person_" + (skip+1));
            skip += limit;
        }

        query = client.query();
        query.setLimit(5);
        kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.size() == 5);
        assertEquals(kinveyListCallback.result.get(0).getUsername(), "Person_0");
        assertEquals(kinveyListCallback.result.get(kinveyListCallback.result.size()-1).getUsername(), "Person_4");


        query = client.query();
        query.setSkip(5);
        kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.size() == 5);
        assertEquals(kinveyListCallback.result.get(0).getUsername(), "Person_5");
        assertEquals(kinveyListCallback.result.get(kinveyListCallback.result.size()-1).getUsername(), "Person_9");

        query = client.query();
        query.setLimit(6);
        query.setSkip(6);
        kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.size() == 4);
        assertEquals(kinveyListCallback.result.get(0).getUsername(), "Person_6");
        assertEquals(kinveyListCallback.result.get(kinveyListCallback.result.size()-1).getUsername(), "Person_9");


        query = client.query();
        query.setSkip(10);
        kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.size() == 0);

        query = client.query();
        query.setSkip(11);
        kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.size() == 0);


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

            DefaultKinveyPullCallback pullCallback = pull(store, query);
            assertNull(pullCallback.error);
            assertNotNull(pullCallback.result);
            assertTrue(pullCallback.result.getResult().size() == limit);
            assertNotNull(pullCallback.result.getResult().get(0));
            assertEquals(pullCallback.result.getResult().get(0).getUsername(), "Person_" + skip);
            assertEquals(pullCallback.result.getResult().get(pullCallback.result.getResult().size()-1).getUsername(), "Person_" + (skip+1));
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
        Person person = createPerson(TEST_TEMP_USERNAME);
        Person savedPerson = store.save(person);
        sync(store, DEFAULT_TIMEOUT);
        DefaultKinveyClientCallback findCallback = find(store, savedPerson.getId(), DEFAULT_TIMEOUT, null);
        assertNotNull(findCallback.result.get(KMD));
        assertNotNull(((GenericJson)findCallback.result.get(KMD)).get(LMT));
        delete(store, findCallback.result.getId(), DEFAULT_TIMEOUT);
        push(store, DEFAULT_TIMEOUT);
        client.getSyncManager().clear(Person.COLLECTION);
    }

    @Test
    public void testUpdateLmt() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = createPerson(TEST_TEMP_USERNAME);
        Person savedPerson = store.save(person);
        sync(store, DEFAULT_TIMEOUT);
        DefaultKinveyClientCallback findCallback = find(store, savedPerson.getId(), DEFAULT_TIMEOUT, null);
        assertNotNull(findCallback.result.get(KMD));
        String savedLmd = (String)((GenericJson)findCallback.result.get(KMD)).get(LMT);
        assertNotNull(savedLmd);
        savedPerson.setUsername(TEST_TEMP_USERNAME + "_Change");
        savedPerson = store.save(savedPerson);
        sync(store, DEFAULT_TIMEOUT);
        findCallback = find(store, savedPerson.getId(), DEFAULT_TIMEOUT, null);
        assertNotNull(findCallback.result.get(KMD));
        String updatedLmd = (String)((GenericJson)findCallback.result.get(KMD)).get(LMT);
        assertNotNull(updatedLmd);
        assertNotEquals(savedLmd, updatedLmd);
        delete(store, findCallback.result.getId(), DEFAULT_TIMEOUT);
        push(store, DEFAULT_TIMEOUT);
        client.getSyncManager().clear(Person.COLLECTION);
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


}

