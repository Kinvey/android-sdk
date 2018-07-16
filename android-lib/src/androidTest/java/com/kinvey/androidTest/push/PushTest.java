package com.kinvey.androidTest.push;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockApplication;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.push.GCMPush;
import com.kinvey.android.push.KinveyGCMService;
import com.kinvey.android.store.DataStore;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.model.LiveModel;
import com.kinvey.java.KinveyException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.io.IOException;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

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

    @Test
    public void testKinveyGCMService() {
        new GCMService();
    }

    //check not initialized state
    @Test
    public void testGCMPushConstructor() {
        String[] senderIds = {"id1", "id2"};
        GCMPush push = new GCMPush(client, false, senderIds);
        assertFalse(push.isInProduction());
        assertFalse(push.isPushEnabled());
        assertEquals(senderIds, push.getSenderIDs());
        assertEquals("", push.getPushId());

    }

    @Test
    public void testGCMPush() throws InterruptedException {
        if (client.isUserLoggedIn()) {
            testManager.logout(client);
        }
        try {
            GCMPush push = (GCMPush) client.push(GCMService.class).initialize(new MockApplication());
            assertTrue(false);
        } catch (KinveyException ex) {
            assertNotNull(ex);
            assertEquals("No user is currently logged in", ex.getReason());
        }

    }

}
