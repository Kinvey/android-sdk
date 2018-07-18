package com.kinvey.androidTest.push;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.push.GCMPush;
import com.kinvey.android.push.KinveyGCMService;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.TestManager;
import com.kinvey.java.KinveyException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PushTest {


    private Client client;
    private TestManager testManager;

    @Before
    public void setUp() {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        client.enableDebugLogging();
        testManager = new TestManager<>();

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

    //check not initialized state
    @Test
    public void testGCMPushDefaultValues() {
        String[] senderIds = {"id1", "id2"};
        GCMPush push = new GCMPush(client, false, senderIds);
        assertFalse(push.isInProduction());
        assertFalse(push.isPushEnabled());
        assertEquals(senderIds, push.getSenderIDs());
        assertEquals("", push.getPushId());
        assertEquals("com.kinvey.android.push.AbstractPush", GCMPush.TAG);
    }

    @Test
    public void testGCMPushUserIsNotLoggedIn() throws InterruptedException {
        if (client.isUserLoggedIn()) {
            testManager.logout(client);
        }
        try {
            client.push(GCMService.class).initialize((Application) InstrumentationRegistry.getContext().getApplicationContext());
            assertTrue(false);
        } catch (KinveyException ex) {
            assertNotNull(ex);
            assertEquals("No user is currently logged in", ex.getReason());
        }
    }

    @Test
    @Ignore
    public void testGCMPush() throws InterruptedException {
        if (!client.isUserLoggedIn()) {
            testManager.login(USERNAME, PASSWORD, client);
        }
        final CountDownLatch latch = new CountDownLatch(1);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                GCMPush push = (GCMPush) client.push(GCMService.class).initialize((Application) InstrumentationRegistry.getContext().getApplicationContext());
                assertEquals(GCMService.class.getName(), push.getPushServiceClass().getName());

                assertNotNull(push);
                sleep();
                assertTrue(push.isPushEnabled());
                assertNotNull(push.getPushId());

                push.disablePush(); //when we call disablePush, all token's are deleted from device, but not from the back end,
                //but we don't have possibility get this tokens from the backend.
                //so push.registerWithKinvey(regid, true); isn't work after it
                sleep();
                assertFalse(push.isPushEnabled());

                push.initialize((Application) InstrumentationRegistry.getContext().getApplicationContext());
                sleep();
                assertTrue(push.isPushEnabled());

                push.disablePush();
                sleep();
                assertFalse(push.isPushEnabled());

                latch.countDown();
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
    }

    private void sleep() {
        try {
            Thread.sleep(20_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
