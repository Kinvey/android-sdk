package com.kinvey.androidTest;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.store.DataStore;
import com.kinvey.androidTest.model.LiveModel;
import com.kinvey.java.store.BaseUserStore;
import com.kinvey.java.store.LiveServiceRouter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


/**
 * Created by yuliya on 2/19/17.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class LiveServiceTest {

    private static final int DEFAULT_TIMEOUT = 60 * 1000;

    private Client client;
    private TestManager<LiveModel> testManager;
    private DataStore<LiveModel> store;

    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        client.enableDebugLogging();
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
    public void testRegisterUnregisterSync() throws InterruptedException, IOException {
        assertTrue(client.isUserLoggedIn());
        BaseUserStore.registerRealtime();
        assertTrue(LiveServiceRouter.getInstance().isInitialized());
        BaseUserStore.unRegisterRealtime();
        assertFalse(LiveServiceRouter.getInstance().isInitialized());
    }

    @Test
    public void testDeviceUuid() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> aClass = Class.forName("com.kinvey.android.UuidFactory");
        Constructor<?> constructor = aClass.getDeclaredConstructor(Context.class);
        constructor.setAccessible(true);
        Object uuidFactory = constructor.newInstance(client.getContext());
        String theFirstUiId = uuidFactory.getClass().getDeclaredMethod("getDeviceUuid").invoke(uuidFactory).toString();
        Object uuidFactorySecond = constructor.newInstance(client.getContext());
        String theSecondUiId = uuidFactorySecond.getClass().getDeclaredMethod("getDeviceUuid").invoke(uuidFactorySecond).toString();
        assertEquals(theFirstUiId, theSecondUiId);
    }

}
