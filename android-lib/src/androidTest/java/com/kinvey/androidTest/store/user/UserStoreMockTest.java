package com.kinvey.androidTest.store.user;

import android.content.Context;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.kinvey.android.Client;
import com.kinvey.android.model.User;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.java.core.KinveyJsonResponseException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class UserStoreMockTest {

    private Client client = null;
    private Context mMockContext = null;

    @Before
    public void setup() {
        if (mMockContext == null) {
            mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        }
        if (client == null) {
            client = new Client.Builder(mMockContext).build();
        }
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
    public void testLogin() throws InterruptedException {
        MockClient<User> mockedClient = new MockClient.Builder<>(mMockContext).build();
        DefaultKinveyClientCallback callback = login(mockedClient);
        assertNull(callback.error);
        assertNotNull(callback.result);
    }

    @Test
    public void testLoginWithUsernameAndPassword() throws InterruptedException {
        MockClient<User> mockedClient = new MockClient.Builder<>(mMockContext).build();
        DefaultKinveyClientCallback callback = login(USERNAME, PASSWORD, mockedClient);
        assertNull(callback.error);
        assertNotNull(callback.result);
    }

    @Test
    public void testLoginError() throws InterruptedException {
        MockClient<User> mockedClient = new MockClient.Builder<>(mMockContext).build(new MockHttpErrorTransport());
        DefaultKinveyClientCallback callback = login(USERNAME, PASSWORD, mockedClient);
        assertNotNull(callback.error);
        assertNull(callback.result);
        assertEquals(500, ((KinveyJsonResponseException) callback.error).getStatusCode());
    }

    private DefaultKinveyClientCallback login(final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(() -> {
            try {
                UserStore.login(client, callback);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private DefaultKinveyClientCallback login(final String username, final String password, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(() -> {
            try {
                UserStore.login(username, password, client, callback);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

}
