package com.kinvey.androidTest.cache;


import android.content.Context;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.model.User;
import com.kinvey.android.store.DataStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.android.sync.KinveyPullResponse;
import com.kinvey.android.sync.KinveyPushResponse;
import com.kinvey.android.sync.KinveySyncCallback;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
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
    private static final String USERNAME = "username";

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

    @Test
    public void testACLIn() throws InterruptedException, IOException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = createPerson(TEST_TEMP_USERNAME);
        for (int i = 0; i < 3; i++) {
            store.save(person);
        }
        Query query = new Query();
        query.in("_acl.creator", new String[]{client.getActiveUser().getId()});

        DefaultKinveyListCallback findCallback = find(store, query);
        assertTrue(findCallback.result.size() == 3);
    }

    private Person createPerson(String name) {
        Person person = new Person();
        person.setUsername(name);
        return person;
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

    private DefaultKinveyListCallback find(final DataStore<Person> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyListCallback callback = new DefaultKinveyListCallback(latch);
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
