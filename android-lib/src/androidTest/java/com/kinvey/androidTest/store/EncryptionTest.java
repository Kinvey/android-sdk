package com.kinvey.androidTest.store;

import android.content.Context;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.model.User;
import com.kinvey.android.store.DataStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.KinveyException;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by yuliya on 08/25/17.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class EncryptionTest {

    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";
    private static final String ACCESS_ERROR = "Access Error";

    private Client client;
    private Context mContext;

    private static class UserKinveyClientCallback implements KinveyClientCallback<User> {

        private CountDownLatch latch;
        private User result;
        private Throwable error;

        private UserKinveyClientCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(User user) {
            this.result = user;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        private void finish() {
            latch.countDown();
        }
    }

    private static class PersonKinveyClientCallback implements KinveyClientCallback<Person> {

        private CountDownLatch latch;
        private Person result;
        private Throwable error;

        PersonKinveyClientCallback(CountDownLatch latch) {
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

    
    @Before
    public void setUp() throws InterruptedException, IOException {
        mContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
    }

    @Test
    public void testSetEncryptionKey() throws IOException {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        client = new Client.Builder(mContext).setEncryptionKey(key).build();
        assertNotNull(client);
    }

    @Test
    public void testDataStoreEncryption() throws InterruptedException {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        client = new Client.Builder(mContext).setEncryptionKey(key).build();
        UserKinveyClientCallback callback = login(USERNAME, PASSWORD);
        assertNull(callback.error);
        assertNotNull(callback.result);
        DataStore<Person> encryptedStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        Person person = createPerson(USERNAME);
        PersonKinveyClientCallback saveCallback = save(encryptedStore, person);
        Assert.assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);

        Client secondClient = new Client.Builder(mContext).setEncryptionKey(key).build();
        DataStore<Person> notEncryptedStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, secondClient);
        PersonKinveyClientCallback findCallback = find(notEncryptedStore, saveCallback.result.getId());
        Assert.assertNotNull(findCallback.result);
        assertNull(findCallback.error);
        client.performLockDown();
    }


    @Test
    public void testDataStoreEncryptionFail() throws InterruptedException {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        client = new Client.Builder(mContext).setEncryptionKey(key).build();
        UserKinveyClientCallback callback = login(USERNAME, PASSWORD);
        assertNull(callback.error);
        assertNotNull(callback.result);
        DataStore<Person> encryptedStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        Person person = createPerson(USERNAME);
        PersonKinveyClientCallback saveCallback = save(encryptedStore, person);
        Assert.assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);

        Client clientWithoutEncryption = new Client.Builder(mContext).build();
        KinveyException fileException = null;
        try {
            DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, clientWithoutEncryption);
        } catch (KinveyException exception) {
            fileException = exception;
        }
        assertNotNull(fileException);
        assertEquals(fileException.getReason(), ACCESS_ERROR);
    }

    @Test
    public void testClientsWithAndWithoutEncryptionSimultaneously() throws InterruptedException {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        client = new Client.Builder(mContext).setEncryptionKey(key).build();
        UserKinveyClientCallback callback = login(USERNAME, PASSWORD);
        assertNull(callback.error);
        assertNotNull(callback.result);
        DataStore<Person> encryptedStore = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        Person person = createPerson(USERNAME);
        PersonKinveyClientCallback saveCallback = save(encryptedStore, person);
        Assert.assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);

        Client clientWithoutEncryption = new Client.Builder(mContext).build();
        KinveyException fileException = null;
        DataStore<Person> otherStore = null;
        try {
            otherStore = DataStore.collection(Person.COLLECTION + "_OTHER", Person.class, StoreType.SYNC, clientWithoutEncryption);
        } catch (KinveyException exception) {
            fileException = exception;
        }
        assertNull(otherStore);
        assertNotNull(fileException);
        assertEquals(fileException.getReason(), ACCESS_ERROR);
    }

    private UserKinveyClientCallback login(final String userName, final String password) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final UserKinveyClientCallback callback = new UserKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            public void run() {
                if (!client.isUserLoggedIn()) {
                    try {
                        UserStore.login(userName, password, client, callback);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    callback.onSuccess(client.getActiveUser());
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private PersonKinveyClientCallback save(final DataStore<Person> store, final Person person) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final PersonKinveyClientCallback callback = new PersonKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            public void run() {
                store.save(person, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private PersonKinveyClientCallback find(final DataStore<Person> store, final String id) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final PersonKinveyClientCallback callback = new PersonKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            public void run() {
                store.find(id, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private Person createPerson(String name) {
        Person person = new Person();
        person.setUsername(name);
        return person;
    }

    @After
    public void tearDown() {
        if (client != null && client.getKinveyHandlerThread() != null) {
            client.performLockDown();
            try {
                client.stopKinveyHandlerThread();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }


}
