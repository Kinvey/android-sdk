package com.kinvey.androidTest;

import android.os.Message;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.Client;
import com.kinvey.android.model.User;
import com.kinvey.android.store.DataStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.callback.CustomKinveyClientCallback;
import com.kinvey.androidTest.callback.CustomKinveyListCallback;
import com.kinvey.androidTest.callback.CustomKinveyReadCallback;
import com.kinvey.androidTest.callback.CustomKinveyLiveServiceCallback;
import com.kinvey.androidTest.callback.CustomKinveyPullCallback;
import com.kinvey.androidTest.callback.CustomKinveySyncCallback;
import com.kinvey.androidTest.callback.DefaultKinveyAggregateCallback;
import com.kinvey.androidTest.callback.DefaultKinveyClientCallback;
import com.kinvey.androidTest.callback.DefaultKinveyDeleteCallback;
import com.kinvey.androidTest.callback.DefaultKinveyReadCallback;
import com.kinvey.androidTest.callback.CustomKinveyPullCallback;
import com.kinvey.androidTest.callback.DefaultKinveyPushCallback;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyCachedAggregateCallback;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.model.AggregateType;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.BaseDataStore;
import com.kinvey.java.store.StoreType;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by yuliya on 09/14/17.
 */

public class TestManager<T extends GenericJson> {

    public static final String TEST_USERNAME = "Test_UserName";
    public static final String TEST_USERNAME_2 = "Test_UserName_2";
    public static final String USERNAME = "test";
    public static final String USERNAME_USER = "test_user";
    public static final String USERNAME_FILE = "test_file";
    public static final String PASSWORD = "test";

    public void login(final String userName, final String password, final Client client) throws InterruptedException {
        if (!client.isUserLoggedIn()) {
            final CountDownLatch latch = new CountDownLatch(1);
            LooperThread looperThread;
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
            latch.await();
            looperThread.mHandler.sendMessage(new Message());
        }
    }

    public void logout(final Client client) throws InterruptedException {
        if (client.isUserLoggedIn()) {
            final CountDownLatch latch = new CountDownLatch(1);
            LooperThread looperThread;
            looperThread = new LooperThread(new Runnable() {
                @Override
                public void run() {
                    UserStore.logout(client, new KinveyClientCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            latch.countDown();
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            assertNull(error);
                            latch.countDown();
                        }
                    });
                }
            });
            looperThread.start();
            latch.await();
            looperThread.mHandler.sendMessage(new Message());
        }
    }

    public KinveyAuthRequest.Builder createBuilder(AbstractClient client) {
        String appKey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String appSecret = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppSecret();
        return new KinveyAuthRequest.Builder(client.getRequestFactory().getTransport(),
                client.getJsonFactory(), client.getBaseUrl(), appKey, appSecret, null);
    }

    public DefaultKinveyClientCallback save(final DataStore<Person> store, final Person person) throws InterruptedException {
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

    public DefaultKinveyDeleteCallback delete(final DataStore<Person> store, final String id) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyDeleteCallback callback = new DefaultKinveyDeleteCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.delete(id, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    public DefaultKinveyDeleteCallback delete(final DataStore<Person> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyDeleteCallback callback = new DefaultKinveyDeleteCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.delete(query, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    public DefaultKinveyDeleteCallback deleteCustom(final DataStore<T> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyDeleteCallback callback = new DefaultKinveyDeleteCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.delete(query, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    public DefaultKinveyDeleteCallback deleteCustom(final DataStore<T> store, final String id) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyDeleteCallback callback = new DefaultKinveyDeleteCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.delete(id, callback);
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

    public void saveCustomInBackground(final DataStore<T> store, final T person) throws InterruptedException {
        final LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    LooperThread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                store.save(person, new KinveyClientCallback<T>() {
                    @Override
                    public void onSuccess(T result) {
                        System.out.println("saved " +result);
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        System.out.println(error.getMessage());
                    }
                });
            }
        });
        looperThread.start();
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

    public DefaultKinveyReadCallback find(final DataStore<Person> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyReadCallback callback = new DefaultKinveyReadCallback(latch);
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

    public DefaultKinveyClientCallback find(final DataStore<Person> store, final String id) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.find(id, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    public CustomKinveyReadCallback<T> findCustom(final DataStore<T> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CustomKinveyReadCallback<T> callback = new CustomKinveyReadCallback<T>(latch);
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

    public CustomKinveyPullCallback pullCustom(final DataStore<T> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CustomKinveyPullCallback callback = new CustomKinveyPullCallback(latch);
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

    public CustomKinveyPullCallback pullCustom(final DataStore<T> store, final Query query, final int pageSize) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CustomKinveyPullCallback callback = new CustomKinveyPullCallback(latch);
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

    public CustomKinveyPullCallback pullCustom(final DataStore<T> store, final Query query, final boolean isAutoPagination) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CustomKinveyPullCallback callback = new CustomKinveyPullCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                if (query != null) {
                    store.pull(query, isAutoPagination, callback);
                } else {
                    store.pull(isAutoPagination, callback);
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

    public DefaultKinveyAggregateCallback calculation(final DataStore<T> store, final AggregateType aggregateType,
                                                      final ArrayList<String> fields, final String sumField, final Query query,
                                                      final KinveyCachedAggregateCallback cachedCallback) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyAggregateCallback callback = new DefaultKinveyAggregateCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.group(aggregateType, fields, sumField, query, callback, cachedCallback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    public CustomKinveySyncCallback sync(final DataStore<T> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CustomKinveySyncCallback callback = new CustomKinveySyncCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.sync(query, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    public CustomKinveySyncCallback sync(final DataStore<T> store, final Query query, final boolean isAutoPagination) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CustomKinveySyncCallback callback = new CustomKinveySyncCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.sync(query, isAutoPagination, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    public CustomKinveySyncCallback sync(final DataStore<T> store, final Query query, final int pageSize) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CustomKinveySyncCallback callback = new CustomKinveySyncCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                if (query != null) {
                    store.sync(query, pageSize, callback);
                } else {
                    store.sync(pageSize, callback);
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    //cleaning backend store (can be improved)
    @Deprecated
    public void cleanBackendDataStore(DataStore<Person> store) throws InterruptedException {
        DefaultKinveyDeleteCallback deleteCallback = delete(store, new Query().notEqual("age", "100500"));
        assertNull(deleteCallback.getError());
    }

    public void cleanBackend(DataStore<T> store, StoreType storeType) throws InterruptedException {
        if (storeType != StoreType.NETWORK) {
            sync(store, store.getClient().query());
        }
        DefaultKinveyDeleteCallback deleteCallback = deleteCustom(store, new Query().notEqual("age", "100500"));
        assertNull(deleteCallback.getError());
        if (storeType == StoreType.SYNC) {
            sync(store, store.getClient().query());
        }
    }

    public void createPersons(DataStore<Person> store, int n) throws IOException {
        for (int i = 0; i < n; i++) {
            Person person = new Person();
            person.setUsername(TEST_USERNAME + i);
            Person savedPerson = store.save(person);
            assertNotNull(savedPerson);
        }
    }

    public CustomKinveyClientCallback<Boolean> subscribe(final DataStore<T> store, final CustomKinveyLiveServiceCallback<T> liveServiceCallback) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CustomKinveyClientCallback<Boolean> callback = new CustomKinveyClientCallback<>(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.subscribe(liveServiceCallback, callback);
            }
        });
        looperThread.start();
        latch.await(60, TimeUnit.SECONDS);
//        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    public CustomKinveyLiveServiceCallback subscribeSync(final DataStore<T> store) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CustomKinveyLiveServiceCallback<T> callback = new CustomKinveyLiveServiceCallback<T>(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    store.subscribe(callback);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    public CustomKinveyLiveServiceCallback subscribeAsync(final DataStore<T> store) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CustomKinveyLiveServiceCallback<T> callback = new CustomKinveyLiveServiceCallback<T>(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    store.subscribe(callback);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    public BaseDataStore<T> mockBaseDataStore(AbstractClient client, String collectionName, Class className, StoreType storeType, NetworkManager networkManager) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
        Class<?> aClass = Class.forName("com.kinvey.java.store.BaseDataStore");
        Constructor<BaseDataStore> constructor = (Constructor<BaseDataStore>) aClass.getDeclaredConstructor(AbstractClient.class, String.class, Class.class, StoreType.class, NetworkManager.class);
        constructor.setAccessible(true);
        BaseDataStore baseDataStore = constructor.newInstance(client, collectionName, className, storeType, networkManager);
        return baseDataStore;
    }

    //use for Person.COLLECTION and for Person.class
    public long getCacheSize(StoreType storeType, Client client) {
        return client.getCacheManager().getCache(Person.COLLECTION, Person.class, storeType.ttl).get().size();
    }
}
