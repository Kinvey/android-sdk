package com.kinvey.androidTest.store;

/**
 *
 * Created by Prots on 30/09/2016.
 *
 */

import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.android.Client;
import com.kinvey.android.store.AsyncDataStore;
import com.kinvey.android.store.AsyncUserStore;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.dto.User;
import com.kinvey.java.store.StoreType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DataStoreTest {

    private Client client;
    private AsyncDataStore<Person> personAsyncDataStore;


    @Before
    public void setUp() throws InterruptedException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        personAsyncDataStore = client.dataStore(Person.COLLECTION, Person.class, StoreType.CACHE);
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

        DefaultKinveyClientCallback(CountDownLatch latch){
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


    private Person createPerson(String name) {
        Person person = new Person();
        person.setName(name);
        return person;
    }


    private DefaultKinveyClientCallback save(final Person person) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                personAsyncDataStore.save(person, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }


    @Test
    public void testSave() throws InterruptedException {
        String personName = "TestName";
        DefaultKinveyClientCallback callback = save(createPerson(personName));
        assertNotNull(callback.result);
        assertNotNull(callback.result.getName());
        assertTrue(callback.result.getName().equals(personName));
    }


}

