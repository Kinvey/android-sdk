package com.kinvey.androidTest.store.data;

import android.content.Context;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.google.api.client.http.HttpTransport;
import com.kinvey.android.Client;
import com.kinvey.android.model.User;
import com.kinvey.android.store.DataStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.store.StoreType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DataStoreBatchSaveTest {

    private static final String TEST_USERNAME = "Test_UserName";

    private Client client;

    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        client = new Client.Builder(mMockContext).build();
        Logger.getLogger(HttpTransport.class.getName()).setLevel(Level.ALL);
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

    private static class DefaultKinveyClientListCallback implements KinveyClientCallback<List<Person>> {

        private CountDownLatch latch;
        List<Person> result;
        Throwable error;

        DefaultKinveyClientListCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(List<Person> result) {
            this.result = result;
            Log.d("DefaultKinveyClientListCallback", "Success, result: " + result);
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            Log.d("DefaultKinveyClientListCallback", "Error, err: " + error);
            finish();
        }

        void finish() {
            Log.d("DefaultKinveyClientListCallback", "Finish");
            latch.countDown();
        }
    }

    private Person createPerson(String name) {
        return new Person(name);
    }

    private DefaultKinveyClientListCallback saveBatch(final DataStore<Person> store, final List<Person> personList) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientListCallback callback = new DefaultKinveyClientListCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.save(personList, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testSaveBatchSync() throws InterruptedException {
        testSaveBatch(StoreType.SYNC);
    }

    @Test
    public void testSaveBatchCache() throws InterruptedException {
        testSaveBatch(StoreType.CACHE);
    }

    @Test
    public void testSaveBatchAuto() throws InterruptedException {
        testSaveBatch(StoreType.AUTO);
    }

    @Test
    public void testSaveBatchNetwork() throws InterruptedException {
        testSaveBatch(StoreType.NETWORK);
    }

    private void testSaveBatch(StoreType storeType) throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, storeType, client);
        client.getSyncManager().clear(Person.COLLECTION);
        List<Person> personList = getPersonList(5);
        DefaultKinveyClientListCallback callback = saveBatch(store, personList);

        assertNotNull(callback.result);
        assertNull(callback.error);
        assertTrue(!callback.result.isEmpty());
    }

    private List<Person> getPersonList(int count) {

        List<Person> personList = new ArrayList<Person>();
        Person person;
        for (int i = 0; i < count; i++) {
            person = createPerson(TEST_USERNAME + String.valueOf(i));
            personList.add(person);
        }
        return personList;
    }

}
