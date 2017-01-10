package com.kinvey.androidTest.store;


import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyPurgeCallback;
import com.kinvey.android.store.DataStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.android.sync.KinveyPullCallback;
import com.kinvey.android.sync.KinveyPullResponse;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.android.sync.KinveyPushResponse;
import com.kinvey.android.sync.KinveySyncCallback;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.cache.ICacheManager;
import com.kinvey.java.cache.KinveyCachedAggregateCallback;
import com.kinvey.java.cache.KinveyCachedClientCallback;
import com.kinvey.java.core.KinveyAggregateCallback;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.dto.User;
import com.kinvey.java.model.AggregateEntity;
import com.kinvey.java.model.Aggregation;
import com.kinvey.java.store.StoreType;

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

    private Client client;


    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        final CountDownLatch latch = new CountDownLatch(1);
        if (!client.isUserLoggedIn()) {
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
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
                    Looper.loop();
                }
            }).start();
        } else {
            latch.countDown();
        }
        latch.await();
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
        person.setMoney(200D);
        person.setAge("22");
        person.setTestInt(100);
        person.setTime(1000000L);
        return person;
    }


    private DefaultKinveyClientCallback save(final DataStore<Person> store, final Person person) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.save(person, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }


    @Test
    public void testSave() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSycManager().clear(Person.COLLECTION);
        String personName = "TestName";
        DefaultKinveyClientCallback callback = save(store, createPerson(personName));
        assertNotNull(callback.result);
        assertNotNull(callback.result.getUsername());
        assertNull(callback.error);
        assertTrue(callback.result.getUsername().equals(personName));
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
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.purge(callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    @Test
    public void testPurge() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPurge"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveyPurgeCallback purgeCallback = purge(store);
        assertNull(purgeCallback.error);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }


    @Test
    public void testPurgeInvalidDataStoreType() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPurge"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
        DefaultKinveyPurgeCallback purgeCallback = purge(store);
        assertNotNull(purgeCallback.error);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
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

        Person person = createPerson("PersonForTestPurge");
        save(store, person);
        person.setAge("25");
        save(store, person);

        DefaultKinveyPurgeCallback purgeCallback = purge(store);
        assertNotNull(purgeCallback.error);
        assertNotNull(purgeCallback.error.getCause());
        assertTrue(purgeCallback.error.getCause().getClass() == SocketTimeoutException.class);
    }


    private DefaultKinveySyncCallback sync(final DataStore<Person> store, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveySyncCallback callback = new DefaultKinveySyncCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.sync(callback);
                Looper.loop();
            }
        }).start();
        latch.await(seconds, TimeUnit.SECONDS);
        return callback;
    }

    @Test
    public void testSync() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPurge"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveySyncCallback syncCallback = sync(store, 120);
        assertNull(syncCallback.error);
        assertNotNull(syncCallback.kinveyPushResponse);
        assertNotNull(syncCallback.kinveyPullResponse);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }

    @Test
    public void testSyncInvalidDataStoreType() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPurge"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
        DefaultKinveySyncCallback syncCallback = sync(store, 120);
        assertNotNull(syncCallback.error);
        assertNull(syncCallback.kinveyPushResponse);
        assertNull(syncCallback.kinveyPullResponse);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
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
        client.getSycManager().clear(Person.COLLECTION);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        Person person = createPerson("PersonForTestSyncTimeOut");
        save(store, person);
        DefaultKinveySyncCallback syncCallback = sync(store, 120);
        assertNotNull(syncCallback.error);
        assertTrue(syncCallback.error.getClass() == SocketTimeoutException.class);
    }


    @Test
    public void testSyncNoCompletionHandler() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForSyncNoCompletionHandler"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveySyncCallback syncCallback = sync(store, 60);
        assertFalse(syncCallback.error == null && syncCallback.kinveyPullResponse == null && syncCallback.kinveyPushResponse == null);
        assertNotNull(syncCallback.kinveyPushResponse);
        assertNotNull(syncCallback.kinveyPullResponse);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }

    private DefaultKinveyPushCallback push(final DataStore<Person> store, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyPushCallback callback = new DefaultKinveyPushCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.push(callback);
                Looper.loop();
            }
        }).start();
        latch.await(seconds, TimeUnit.SECONDS);
        return callback;
    }

    @Test
    public void testPush() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPush"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveyPushCallback pushCallback = push(store, 120);
        assertNull(pushCallback.error);
        assertTrue(pushCallback.result.getListOfExceptions().size() == 0);
        assertNotNull(pushCallback.result);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }


    @Test
    public void testPushInvalidDataStoreType() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPush"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
        DefaultKinveyPushCallback pushCallback = push(store, 120);
        assertTrue(pushCallback.error != null || pushCallback.result.getListOfExceptions() != null);
        assertNull(pushCallback.result);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }

    @Test
    public void testPushNoCompletionHandler() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForPushNoCompletionHandler"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveyPushCallback pushCallback = push(store, 60);
        assertFalse(pushCallback.error == null && pushCallback.result == null);
        assertNull(pushCallback.error);
        assertTrue(pushCallback.result.getListOfExceptions().size() == 0);
        assertNotNull(pushCallback.result);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }


    private DefaultKinveyPullCallback pull(final DataStore<Person> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyPullCallback callback = new DefaultKinveyPullCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                if (query != null) {
                    store.pull(query, callback);
                } else {
                    store.pull(callback);
                }
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    private DefaultKinveyDeleteCallback delete(final DataStore<Person> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyDeleteCallback callback = new DefaultKinveyDeleteCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.delete(query, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
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
        for (int i = 0 ; i < 100; i++){
            person = createPerson("Test" + i);
            person.setId(String.valueOf(i));
            items.add(person);
            ids.add(String.valueOf(i));
        }


        for (Person p:items) {
            save(store, p);
        }

        List<Person> cachedObjects = client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).get(ids);
        assertEquals(100, cachedObjects.size());

        for (int i = 0 ; i < cachedObjects.size() ; i ++){
            Person res = cachedObjects.get(i);
            assertEquals(res.getId(), String.valueOf(i));
        }

        assertTrue(true);
        client.getSycManager().clear(Person.COLLECTION);

    }

    @Test
    public void testPull() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        client.getSycManager().clear(Person.COLLECTION);

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
        query = query.equals("username", victor.getUsername());
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
        query = query.equals("username", victor.getUsername());
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
        query = query.equals("username", victor.getUsername());
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
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("TestPullPendingSyncItems"));
        DefaultKinveyPullCallback pullCallback = pull(store, null);
        assertNull(pullCallback.result);
        assertNotNull(pullCallback.error);
    }


    @Test
    public void testPullInvalidDataStoreType() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        client.getSycManager().clear(Person.COLLECTION);

        DefaultKinveyPullCallback pullCallback = pull(store, null);
        assertNull(pullCallback.result);
        assertNotNull(pullCallback.error);
    }


    private DefaultKinveyClientCallback find(final DataStore<Person> store, final String id, int seconds, final KinveyCachedClientCallback<Person> cachedClientCallback) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.find(id, callback, cachedClientCallback);
                Looper.loop();
            }
        }).start();
        latch.await(seconds, TimeUnit.SECONDS);
        return callback;
    }

    @Test
    public void testFindById() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).clear();
        Log.d("testPull: ", "cache size = " + getCacheSize(StoreType.SYNC)  + " should be n");
        Person person = createPerson("TestFindByIdPerson");
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        Log.d("testPull: ", "cache size = " + getCacheSize(StoreType.SYNC)  + " should be n+1");

        String personId = saveCallback.result.getId();

        DefaultKinveyClientCallback findCallback = find(store, personId, 60, null);
        assertNotNull(findCallback.result);
        assertNull(saveCallback.error);
        assertEquals(findCallback.result.getId(), personId);
        Log.d("testPull: ", "cache size = " + getCacheSize(StoreType.SYNC)  + " should be n+1");
    }

    @Test
    public void testFindByIdWithCacheCallback() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.CACHE.ttl).clear();
        Person person = createPerson("testFindByIdWithCacheCallback");
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());

        String personId = saveCallback.result.getId();
        DefaultKinveyClientCallback findCallback = find(store, personId, 360, new KinveyCachedClientCallback<Person>() {
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
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.find(query, callback, null);
                Looper.loop();
            }
        }).start();
        latch.await(seconds, TimeUnit.SECONDS);
        return callback;
    }

    @Test
    public void testFindByQuery() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSycManager().clear(Person.COLLECTION);

        Person person = createPerson("TestFindByIdPerson");
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());

        String userId = saveCallback.result.getId();
        Query query = client.query();
        query = query.equals("_id", userId);

        DefaultKinveyListCallback kinveyListCallback = find(store, query, 60);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertNotNull(kinveyListCallback.result.get(0));
        assertEquals(kinveyListCallback.result.get(0).getId(), userId);
    }


    private DefaultKinveyDeleteCallback delete(final DataStore<Person> store, final String id, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyDeleteCallback callback = new DefaultKinveyDeleteCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.delete(id, callback);
                Looper.loop();
            }
        }).start();
        latch.await(seconds, TimeUnit.SECONDS);
        return callback;
    }

    @Test
    public void testRemovePersistable() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSycManager().clear(Person.COLLECTION);

        Person person = createPerson("TestFindByIdPerson");
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());

        String userId = saveCallback.result.getId();

        DefaultKinveyDeleteCallback deleteCallback = delete(store, userId, 60);
        assertNull(deleteCallback.error);
        assertNotNull(deleteCallback.result);
        assertTrue(deleteCallback.result == 1);
    }


    @Test
    public void testRemovePersistableIdMissing() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSycManager().clear(Person.COLLECTION);

        Person person = createPerson("TestFindByIdPerson");
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());

        DefaultKinveyDeleteCallback deleteCallback = delete(store, null, 60);
        assertNotNull(deleteCallback.error);
        assertNull(deleteCallback.result);
    }

    private DefaultKinveyDeleteCallback delete(final DataStore<Person> store, final Iterable<String> entityIDs) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyDeleteCallback callback = new DefaultKinveyDeleteCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.delete(entityIDs, callback);
                Looper.loop();
            }
        }).start();
        latch.await(120, TimeUnit.SECONDS);
        return callback;
    }

    @Test
    public void testRemovePersistableArray() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSycManager().clear(Person.COLLECTION);

        DefaultKinveyClientCallback saveCallback = save(store, createPerson("Test1FindByIdPerson"));
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        String user1Id = saveCallback.result.getId();

        saveCallback = save(store, createPerson("Test2FindByIdPerson"));
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        String user2Id = saveCallback.result.getId();

        assertNotEquals(user1Id, user2Id);

        DefaultKinveyDeleteCallback deleteCallback = delete(store, Arrays.asList(user1Id, user2Id));
        assertNull(deleteCallback.error);
        assertNotNull(deleteCallback.result);
        assertTrue(deleteCallback.result == 2);
    }


    @Test
    public void testExpiredTTL() throws InterruptedException {
        StoreType.SYNC.ttl = 1;
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSycManager().clear(Person.COLLECTION);

        DefaultKinveyClientCallback saveCallback = save(store, createPerson("Test1FindByIdPerson"));
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        String userId = saveCallback.result.getId();
        assertNotNull(userId);

        Thread.sleep(1000);

        Query query = client.query();
        query = query.equals("_id", userId);

        DefaultKinveyListCallback findCallback = find(store, query, 120);
        assertNull(findCallback.error);
        assertNotNull(findCallback.result);
        assertTrue(findCallback.result.size() == 0);
        StoreType.SYNC.ttl = Long.MAX_VALUE;
    }


    @Test
    public void testSaveAndFind10SkipLimit() throws IOException, InterruptedException {
        assertNotNull(client.activeUser());
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        cleanBackendDataStore(store);
        sync(store, 120);

        User user = client.activeUser();

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
            kinveyListCallback = find(store, query, 60);
            assertNull(kinveyListCallback.error);
            assertNotNull(kinveyListCallback.result);
            assertEquals(kinveyListCallback.result.get(0).getUsername(), "Person_" + skip);
            assertEquals(kinveyListCallback.result.get(1).getUsername(), "Person_" + (skip+1));
            skip += limit;
        }

        query = client.query();
        query.setLimit(5);
        kinveyListCallback = find(store, query, 60);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.size() == 5);
        assertEquals(kinveyListCallback.result.get(0).getUsername(), "Person_0");
        assertEquals(kinveyListCallback.result.get(kinveyListCallback.result.size()-1).getUsername(), "Person_4");


        query = client.query();
        query.setSkip(5);
        kinveyListCallback = find(store, query, 60);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.size() == 5);
        assertEquals(kinveyListCallback.result.get(0).getUsername(), "Person_5");
        assertEquals(kinveyListCallback.result.get(kinveyListCallback.result.size()-1).getUsername(), "Person_9");

        query = client.query();
        query.setLimit(6);
        query.setSkip(6);
        kinveyListCallback = find(store, query, 60);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.size() == 4);
        assertEquals(kinveyListCallback.result.get(0).getUsername(), "Person_6");
        assertEquals(kinveyListCallback.result.get(kinveyListCallback.result.size()-1).getUsername(), "Person_9");


        query = client.query();
        query.setSkip(10);
        kinveyListCallback = find(store, query, 60);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.size() == 0);

        query = client.query();
        query.setSkip(11);
        kinveyListCallback = find(store, query, 60);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.size() == 0);


        DefaultKinveyPushCallback pushCallback = push(store, 60);
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
    public void testSum() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        client.getSycManager().clear(Person.COLLECTION);
        DefaultKinveyClientCallback clientCallback = save(store, createPerson("PersonForSUM23"));
        assertNotNull(clientCallback.result);

        Query query = client.query();
        query = query.notEqual("age", "100200300");

        DefaultKinveyNumberCallback callback = aggregation(AggregateEntity.AggregateType.SUM, store, query, new KinveyCachedAggregateCallback() {
            @Override
            public void onSuccess(Aggregation result) {
                Log.d("TestSum", String.valueOf(result.results));
            }

            @Override
            public void onFailure(Throwable error) {
                Log.d("TestSum", error.getMessage());
            }
        });

        assertNotNull(callback.result);
        Log.d("TestSum", String.valueOf(callback.result.results));
    }


    private DefaultKinveyNumberCallback aggregation(final AggregateEntity.AggregateType type, final DataStore<Person> store, final Query query, final KinveyCachedAggregateCallback cachedClientCallback) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyNumberCallback callback = new DefaultKinveyNumberCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                ArrayList<String> list = new ArrayList<String>();
                list.add("username");
                list.add("testInt");
                String field = "money";
//                list.add("time");
                switch (type) {
                    case COUNT:
                        store.count(list, query, callback, cachedClientCallback);
                        break;
                    case SUM:
                        store.sum(list, field, query, callback, cachedClientCallback);
                        break;
                    case MIN:
                        store.min(list, field, query, callback, cachedClientCallback);
                        break;
                    case MAX:
                        store.max(list, field, query, callback, cachedClientCallback);
                        break;
                    case AVERAGE:
                        store.average(list, field, query, callback, cachedClientCallback);
                        break;
                }

                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    private static class DefaultKinveyNumberCallback extends KinveyAggregateCallback {

        private CountDownLatch latch;
        Aggregation result;
        Throwable error;

        DefaultKinveyNumberCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        @Override
        public void onSuccess(Aggregation response) {
            this.result = response;
            finish();
        }

        void finish() {
            latch.countDown();
        }
    }


    @Test
    public void testMin() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        client.getSycManager().clear(Person.COLLECTION);
        DefaultKinveyClientCallback clientCallback = save(store, createPerson("PersonForSUM23"));
        assertNotNull(clientCallback.result);

        Query query = client.query();
        query = query.notEqual("age", "100200300");

        DefaultKinveyNumberCallback callback = aggregation(AggregateEntity.AggregateType.MIN, store, query, new KinveyCachedAggregateCallback() {
            @Override
            public void onSuccess(Aggregation result) {
                Log.d("TestAggregationMIN", String.valueOf(result.results));
            }

            @Override
            public void onFailure(Throwable error) {
                Log.d("TestAggregationMIN", error.getMessage());
            }
        });

        assertNotNull(callback.result);
        Log.d("TestAggregationMIN", String.valueOf(callback.result.results));
    }

    @Test
    public void testMax() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        client.getSycManager().clear(Person.COLLECTION);
        DefaultKinveyClientCallback clientCallback = save(store, createPerson("PersonForSUM23"));
        assertNotNull(clientCallback.result);

        Query query = client.query();
        query = query.notEqual("age", "100200300");

        DefaultKinveyNumberCallback callback = aggregation(AggregateEntity.AggregateType.MAX, store, query, new KinveyCachedAggregateCallback() {
            @Override
            public void onSuccess(Aggregation result) {
                Log.d("TestAggregationMAX", String.valueOf(result.results));
            }

            @Override
            public void onFailure(Throwable error) {
                Log.d("TestAggregationMAX", error.getMessage());
            }
        });

        assertNotNull(callback.result);
        Log.d("TestAggregationMAX", String.valueOf(callback.result.results));
    }

    @Test
    public void testCount() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        client.getSycManager().clear(Person.COLLECTION);
        DefaultKinveyClientCallback clientCallback = save(store, createPerson("PersonForSUM23"));
        assertNotNull(clientCallback.result);

        Query query = client.query();
        query = query.notEqual("age", "100200300");

        DefaultKinveyNumberCallback callback = aggregation(AggregateEntity.AggregateType.COUNT, store, query, new KinveyCachedAggregateCallback() {
            @Override
            public void onSuccess(Aggregation result) {
                Log.d("TestAggregationCOUNT", String.valueOf(result.results));
            }

            @Override
            public void onFailure(Throwable error) {
                Log.d("TestAggregationCOUNT", error.getMessage());
            }
        });

        assertNotNull(callback.result);
        Log.d("TestAggregationCOUNT", String.valueOf(callback.result.results));
    }

    @Test
    public void testAverage() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        client.getSycManager().clear(Person.COLLECTION);
        DefaultKinveyClientCallback clientCallback = save(store, createPerson("PersonForSUM23"));
        assertNotNull(clientCallback.result);

        Query query = client.query();
        query = query.notEqual("age", "100200300");

        DefaultKinveyNumberCallback callback = aggregation(AggregateEntity.AggregateType.AVERAGE, store, query, new KinveyCachedAggregateCallback() {
            @Override
            public void onSuccess(Aggregation result) {
                Log.d("TestAggregationAverage", String.valueOf(result.results));
            }

            @Override
            public void onFailure(Throwable error) {
                Log.d("TestAggregationAverage", error.getMessage());
            }
        });

        assertNotNull(callback.result);
        Log.d("TestAggregationAverage", String.valueOf(callback.result.results));
    }
}

