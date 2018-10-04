package com.kinvey.androidTest.store.data.request;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.store.DataStore;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.callback.DefaultKinveyClientCallback;
import com.kinvey.androidTest.callback.DefaultKinveyPushCallback;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.TEST_USERNAME;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by yuliya on 1/5/17.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class RequestTest {

    private static final int DEFAULT_TIMEOUT = 60 * 1000;

    private Client client;
    private TestManager<Person> testManager;
    private DataStore<Person> store;

    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        testManager = new TestManager<>();
        testManager.login(USERNAME, PASSWORD, client);

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


    @Test
    public void testDefaultTimeOutValue() throws InterruptedException, IOException {
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        testManager.cleanBackend(store, StoreType.SYNC);
        client.enableDebugLogging();
        assertEquals(DEFAULT_TIMEOUT, client.getRequestTimeout());

        Person person = new Person();
        person.setUsername(TEST_USERNAME);
        DefaultKinveyClientCallback callback = testManager.save(store, person);
        assertNotNull(callback);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());
        assertNotNull(callback.getResult().getUsername());
        assertEquals(TEST_USERNAME, callback.getResult().getUsername());

        DefaultKinveyPushCallback pushCallback = testManager.push(store);
        assertNotNull(pushCallback);
        assertNotNull(pushCallback.getResult());
        assertNull(pushCallback.getError());
        assertEquals(1, pushCallback.getResult().getSuccessCount());
    }

    @Test
    @Ignore
    public void testSetCustomTimeoutAndCheckException() throws InterruptedException, IOException {
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        testManager.cleanBackend(store, StoreType.SYNC);
        assertEquals(DEFAULT_TIMEOUT, client.getRequestTimeout());

        client.setRequestTimeout(1);
        assertEquals(1, client.getRequestTimeout());

        Person person = new Person();
        person.setUsername(TEST_USERNAME);
        DefaultKinveyClientCallback callback = testManager.save(store, person);
        assertNotNull(callback);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());
        assertNotNull(callback.getResult().getUsername());
        assertEquals(TEST_USERNAME, callback.getResult().getUsername());

        DefaultKinveyPushCallback pushCallback = testManager.push(store);
        assertNotNull(pushCallback);
        assertNull(pushCallback.getResult());
        assertNotNull(pushCallback.getError());
        assertEquals("SocketTimeoutException", pushCallback.getError().getClass().getSimpleName());
    }

    @Test
    public void testSetCustomTimeout() throws InterruptedException, IOException {
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        testManager.cleanBackend(store, StoreType.SYNC);
        assertEquals(DEFAULT_TIMEOUT, client.getRequestTimeout());

        client.setRequestTimeout(DEFAULT_TIMEOUT * 2);
        assertEquals(DEFAULT_TIMEOUT * 2, client.getRequestTimeout());

        Person person = new Person();
        person.setUsername(TEST_USERNAME);
        DefaultKinveyClientCallback callback = testManager.save(store, person);
        assertNotNull(callback);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());
        assertNotNull(callback.getResult().getUsername());
        assertEquals(TEST_USERNAME, callback.getResult().getUsername());

        DefaultKinveyPushCallback pushCallback = testManager.push(store);
        assertNotNull(pushCallback);
        assertNotNull(pushCallback.getResult());
        assertNull(pushCallback.getError());
        assertEquals(1, pushCallback.getResult().getSuccessCount());
    }


}
