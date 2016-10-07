package com.kinvey.androidTest.store;

/**
 * Created by Prots on 30/09/2016.
 */

import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyPurgeCallback;
import com.kinvey.android.store.AsyncDataStore;
import com.kinvey.android.store.AsyncUserStore;
import com.kinvey.android.sync.KinveyPullResponse;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.android.sync.KinveyPushResponse;
import com.kinvey.android.sync.KinveySyncCallback;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.dto.User;
import com.kinvey.java.store.StoreType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DataStoreTest {

    private Client client;
    private AsyncDataStore<Person> personAsyncDataStore;


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
                        AsyncUserStore.login("test", "test", client, new KinveyClientCallback<User>() {
                            @Override
                            public void onSuccess(User result) {
                                latch.countDown();
                            }

                            @Override
                            public void onFailure(Throwable error) {
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

        }

        @Override
        public void onPushSuccess(KinveyPushResponse kinveyPushResponse) {

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

    private Person createPerson(String name) {
        Person person = new Person();
        person.setName(name);
        return person;
    }


    private DefaultKinveyClientCallback save(final AsyncDataStore<Person> store, final Person person) throws InterruptedException {
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
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);
        String personName = "TestName";
        DefaultKinveyClientCallback callback = save(store, createPerson(personName));
        assertNotNull(callback.result);
        assertNotNull(callback.result.getName());
        assertNull(callback.error);
        assertTrue(callback.result.getName().equals(personName));
    }


    @Test
    public void testCustomTag() {
        String path = client.getContext().getFilesDir().getAbsolutePath();
        String customPath = path + "/_baas.kinvey.com_-1";
        removeFiles(customPath);
        File file = new File(customPath);
        assertFalse(file.exists());
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        assertTrue(file.exists());
    }

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


    private DefaultKinveyPurgeCallback purge(final AsyncDataStore<Person> store) throws InterruptedException {
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
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPurge"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveyPurgeCallback purgeCallback = purge(store);
        assertNull(purgeCallback.error);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }


    @Test
    public void testPurgeInvalidDataStoreType() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.NETWORK);
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

        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);

        Person person = createPerson("PersonForTestPurge");
        save(store, person);
        person.setAge("25");
        save(store, person);

        DefaultKinveyPurgeCallback purgeCallback = purge(store);
        assertNotNull(purgeCallback.error);
        assertNotNull(purgeCallback.error.getCause());
        assertTrue(purgeCallback.error.getCause().getClass() == SocketTimeoutException.class);
    }


    private DefaultKinveySyncCallback sync(final AsyncDataStore<Person> store) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveySyncCallback callback = new DefaultKinveySyncCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.sync(callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    @Test
    public void testSync() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPurge"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveySyncCallback syncCallback = sync(store);
        assertNull(syncCallback.error);
        assertNotNull(syncCallback.kinveyPushResponse);
        assertNotNull(syncCallback.kinveyPullResponse);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }

    @Test
    public void testSyncInvalidDataStoreType() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.NETWORK);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPurge"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
        DefaultKinveySyncCallback syncCallback = sync(store);
        assertNotNull(syncCallback.error);
        assertNull(syncCallback.kinveyPushResponse);
        assertNull(syncCallback.kinveyPullResponse);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }


/*    @Test
    public void testSyncTimeoutError() {

    }*/

    //TODO uncomment
/*    @Test
    public void testSyncNoCompletionHandler() {

    }*/

    private DefaultKinveyPushCallback push(final AsyncDataStore<Person> store) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyPushCallback callback = new DefaultKinveyPushCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.push(callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    @Test
    public void testPush() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPush"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveyPushCallback syncCallback = push(store);
        assertNull(syncCallback.error);
        assertNotNull(syncCallback.result);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }


    @Test
    public void testPushInvalidDataStoreType() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.NETWORK);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPush"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
        DefaultKinveyPushCallback syncCallback = push(store);
        assertNotNull(syncCallback.error);
        assertNull(syncCallback.result);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }



    class ChangeTimeout implements HttpRequestInitializer {
        public void initialize(HttpRequest request) throws SocketTimeoutException {
            throw new SocketTimeoutException();
        }
    }


}

