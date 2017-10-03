package com.kinvey.androidTest;

import android.os.Message;

import com.kinvey.android.Client;
import com.kinvey.android.model.User;
import com.kinvey.android.store.DataStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.callback.CustomKinveyClientCallback;
import com.kinvey.androidTest.callback.CustomKinveyListCallback;
import com.kinvey.androidTest.callback.DefaultKinveyClientCallback;
import com.kinvey.androidTest.callback.DefaultKinveyListCallback;
import com.kinvey.androidTest.callback.CustomKinveyPullCallback;
import com.kinvey.androidTest.callback.DefaultKinveyPushCallback;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by yuliya on 09/14/17.
 */

public class TestManager<T extends Person> {

    public static final String TEST_USERNAME = "Test_UserName";
    public static final String USERNAME = "test";
    public static final String PASSWORD = "test";

    public void login(final String userName, final String password, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        LooperThread looperThread = null;
        if (!client.isUserLoggedIn()) {
            looperThread = new LooperThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        UserStore.login(userName, password, client, new KinveyClientCallback<User>() {
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

    public DefaultKinveyClientCallback save(final DataStore<Person> store, final T person) throws InterruptedException {
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

    public CustomKinveyClientCallback<T> saveCustom(final DataStore<T> store, final T person) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CustomKinveyClientCallback<T> callback = new CustomKinveyClientCallback<T>(latch);
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

    public CustomKinveyListCallback<T> saveCustomList(final DataStore<T> store, final List<T> persons) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CustomKinveyListCallback<T> callback = new CustomKinveyListCallback<T>(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.save(persons, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    public DefaultKinveyListCallback find(final DataStore<Person> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyListCallback callback = new DefaultKinveyListCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.find(query, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    public CustomKinveyListCallback<T> findCustom(final DataStore<T> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CustomKinveyListCallback<T> callback = new CustomKinveyListCallback<T>(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.find(query, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    public CustomKinveyPullCallback<T> pullCustom(final DataStore<T> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CustomKinveyPullCallback<T> callback = new CustomKinveyPullCallback<T>(latch);
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

    public DefaultKinveyPushCallback push(final DataStore<Person> store) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyPushCallback callback = new DefaultKinveyPushCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.push(callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }
}
