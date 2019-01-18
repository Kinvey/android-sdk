package com.kinvey.androidTest.store.data.cache;


import android.content.Context;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyReadCallback;
import com.kinvey.android.model.User;
import com.kinvey.android.store.DataStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyReadResponse;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class QueryWithRealDataTest {

    private static final String TEST_USERNAME = "Test_UserName";
    private static final String TEST_TEMP_USERNAME = "Temp_UserName";

    private Client client;

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


    @Test
    @Ignore
    public void testACLInNETWORK() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        client.getSyncManager().clear(Person.COLLECTION);
        for (int i = 0; i < 3; i++) {
            store.save(createPerson(TEST_TEMP_USERNAME));
        }
        Query query = new Query();
        query.in("_acl.creator", new String[]{client.getActiveUser().getId()});
        DefaultKinveyReadCallback findCallback = find(store, query);
        delete(store, client.query().notEqual("name", "no_exist_field"));
        assertTrue(findCallback.result.getResult().size() == 3);
    }

    @Test
    public void testACLInSYNC() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        for (int i = 0; i < 3; i++) {
            store.save(createPerson(TEST_TEMP_USERNAME));
        }
        Query query = new Query();
        query.in("_acl.creator", new String[]{client.getActiveUser().getId()});
        DefaultKinveyReadCallback findCallback = find(store, query);
        assertTrue(findCallback.result.getResult().size() == 3);
    }

    @Test
    public void testACLInOneParameterForQuery() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        for (int i = 0; i < 3; i++) {
            store.save(createPerson(TEST_TEMP_USERNAME));
        }
        store.save(createPerson(TEST_USERNAME));
        Query query = new Query();
        query.in("username", new String[]{TEST_TEMP_USERNAME});
        DefaultKinveyReadCallback findCallback = find(store, query);
        assertTrue(findCallback.result.getResult().size() == 3);
    }

    @Test
    public void testACLInTwoParametersForQuery() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        client.getSyncManager().clear(Person.COLLECTION);
        for (int i = 0; i < 3; i++) {
            store.save(createPerson(TEST_TEMP_USERNAME));
        }
        store.save(createPerson(TEST_USERNAME));
        Query query = new Query();
        query.in("username", new String[]{TEST_TEMP_USERNAME, TEST_USERNAME});
        DefaultKinveyReadCallback findCallback = find(store, query);
        assertTrue(findCallback.result.getResult().size() == 4);
    }

    @Test
    @Ignore
    public void testACLInCACHE() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.CACHE, client);
        client.getSyncManager().clear(Person.COLLECTION);
        for (int i = 0; i < 3; i++) {
            store.save(createPerson(TEST_TEMP_USERNAME));
        }
        Query query = new Query();
        query.in("_acl.creator", new String[]{client.getActiveUser().getId()});
        DefaultKinveyReadCallback findCallback = find(store, query);
        delete(store, client.query().notEqual("name", "no_exist_field"));
        assertTrue(findCallback.result.getResult().size() == 3);
    }

    private Person createPerson(String name) {
        Person person = new Person();
        person.setUsername(name);
        return person;
    }

    private DefaultKinveyReadCallback find(final DataStore<Person> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyReadCallback callback = new DefaultKinveyReadCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.find(query, callback, null);
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
