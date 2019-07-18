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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertNotNull;
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
        client = new Client.Builder(mMockContext).build();
//        client.enableDebugLogging();
        if (client.isUserLoggedIn()) {
//            logout(client);
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
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        MockClient<User> mockedClient = new MockClient.Builder<>(mMockContext).build();
        DefaultKinveyClientCallback callback = login(mockedClient);
        assertNull(callback.error);
        assertNotNull(callback.result);
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

}
