package com.kinvey.androidTest.store.user.push;

import android.app.Application;
import android.content.Context;
import android.os.Message;

import androidx.test.runner.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.kinvey.android.Client;
import com.kinvey.android.push.AbstractPush;
import com.kinvey.android.push.FCMPush;
import com.kinvey.android.push.KinveyFCMService;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.TestManager;
import com.kinvey.java.KinveyException;
import com.kinvey.java.core.KinveyClientCallback;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PushTest {


    private Client client;
    private TestManager testManager;
    private static final String ID_1 = "id1";
    private static final String ID_2 = "id2";
    private static final String DEVICE_ID = "DeviceID";

    @Before
    public void setUp() {
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
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

    @Test
    public void testFCMPushDefaultValues() {
        String[] senderIds = {ID_1, ID_2};
        FCMPush push = new FCMPush(client, false, senderIds);
        assertFalse(push.isInProduction());
        assertFalse(push.isPushEnabled());
        assertEquals(senderIds, push.getSenderIDs());
        assertEquals("", push.getPushId());
        assertEquals("com.kinvey.android.push.AbstractPush", FCMPush.TAG);
        push.setPushServiceClass(FCMService.class);
        assertEquals(FCMService.class.getName(), push.getPushServiceClass().getName());
        Method method = null;
        try {
            method = AbstractPush.class.getDeclaredMethod("getClient");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        assert method != null;
        method.setAccessible(true);
        Client client = null;
        try {
            client = (Client) method.invoke(push);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        assertNotNull(client);
    }

    @Test
    public void testPushRegistrationConstructors() {
        AbstractPush.PushRegistration pushRegistration = new AbstractPush.PushRegistration();
        assertNotNull(pushRegistration);
        AbstractPush.PushRegistration pushRegistration2 = new AbstractPush.PushRegistration(DEVICE_ID);
        assertNotNull(pushRegistration2);
    }

    @Test
    public void testPushRegistrationServiceParameter() {
        AbstractPush.PushRegistration pushRegistration = new AbstractPush.PushRegistration();
        assertNotNull(pushRegistration);
        assertTrue(pushRegistration.containsKey("service"));
        assertTrue(pushRegistration.containsValue("firebase"));
    }

    @Test
    public void testRegisterPushRequestConstructor() {
        FCMPush push = createFCMPush();
        Method method = null;
        try {
            method = AbstractPush.class.getDeclaredMethod("createRegisterPushRequest", AbstractPush.PushRegistration.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        assert method != null;
        method.setAccessible(true);
        AbstractPush.RegisterPush registerPush = null;
        try {
            registerPush = (AbstractPush.RegisterPush) method.invoke(push, new AbstractPush.PushRegistration(DEVICE_ID));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        assertNotNull(registerPush);
    }

    @Test
    public void testUnRegisterPushRequestConstructor() {
        FCMPush push = new FCMPush(client, false, ID_1, ID_2);
        Method method = null;
        try {
            method = AbstractPush.class.getDeclaredMethod("createUnregisterPushRequest", AbstractPush.PushRegistration.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        assert method != null;
        method.setAccessible(true);
        AbstractPush.UnregisterPush unregisterPush = null;
        try {
            unregisterPush = (AbstractPush.UnregisterPush) method.invoke(push, new AbstractPush.PushRegistration(DEVICE_ID));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        assertNotNull(unregisterPush);
    }

    @Test
    public void testKinveyFCMServiceConstructor() {
        KinveyFCMService fcmService = new FCMService();
        assertNotNull(fcmService);
    }

    @Test
    public void testPushConfigField() {
        FCMPush.PushConfigField configField = new FCMPush.PushConfigField();
        assertNotNull(configField);
        String[] senderIds = {ID_1, ID_2};
        configField.setIds(senderIds);
        assertEquals(senderIds, configField.getIds());
        configField.setNotificationKey("NotificationKey");
        assertEquals("NotificationKey", configField.getNotificationKey());
    }

    @Test
    public void testPushConfig() {
        FCMPush.PushConfig pushConfig = new FCMPush.PushConfig();
        assertNotNull(pushConfig);
        String[] senderIds = {ID_1, ID_2};
        FCMPush.PushConfigField configField = new FCMPush.PushConfigField();
        pushConfig.setGcm(configField);
        assertEquals(configField, pushConfig.getGcm());
        FCMPush.PushConfigField configField2 = new FCMPush.PushConfigField();
        pushConfig.setGcmDev(configField2);
        assertEquals(configField2, pushConfig.getGcmDev());
    }

    @Test
    public void testFCMPushUserIsNotLoggedIn() throws InterruptedException {
        if (client.isUserLoggedIn()) {
            testManager.logout(client);
        }
        try {
            client.push(FCMService.class).initialize((Application) InstrumentationRegistry.getInstrumentation().getContext().getApplicationContext());
            assertTrue(false);
        } catch (KinveyException ex) {
            assertNotNull(ex);
            assertEquals("No user is currently logged in", ex.getReason());
        }
    }

    private FCMPush createFCMPush() {
        return new FCMPush(client, false, ID_1, ID_2);
    }

    @Test
    public void testAsyncEnablePushRequestConstructor() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                FCMPush push = new FCMPush(client, false, ID_1, ID_2);
                Method method = null;
                try {
                    method = FCMPush.class.getDeclaredMethod("createAsyncEnablePushRequest", KinveyClientCallback.class, String.class);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                assert method != null;
                method.setAccessible(true);
                Object enablePushRequest = null;
                try {
                    enablePushRequest = method.invoke(push, null, DEVICE_ID);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                assertNotNull(enablePushRequest);
                latch.countDown();
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
    }

    @Test
    public void testAsyncDisablePushRequestConstructor() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                FCMPush push = new FCMPush(client, false, ID_1, ID_2);
                Method method = null;
                try {
                    method = FCMPush.class.getDeclaredMethod("createAsyncDisablePushRequest", KinveyClientCallback.class, String.class);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                assert method != null;
                method.setAccessible(true);
                Object enablePushRequest = null;
                try {
                    enablePushRequest = method.invoke(push, null, DEVICE_ID);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                assertNotNull(enablePushRequest);
                latch.countDown();
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
    }


// Google Services is needed for this test
/*    @Test
    @Ignore
    public void testGCMPush() throws InterruptedException {
        if (!client.isUserLoggedIn()) {
            testManager.login(USERNAME, PASSWORD, client);
        }
        final CountDownLatch latch = new CountDownLatch(1);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                FCMPush push = (FCMPush) client.push(FCMService.class).initialize((Application) InstrumentationRegistry.getContext().getApplicationContext());
                assertEquals(FCMService.class.getName(), push.getPushServiceClass().getName());

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
    }*/
}
