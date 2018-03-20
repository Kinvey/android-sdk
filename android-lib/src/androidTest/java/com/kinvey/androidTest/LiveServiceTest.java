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
import com.kinvey.java.store.KinveyDataStoreLiveServiceCallback;
import com.kinvey.java.store.KinveyLiveServiceStatus;
import com.kinvey.java.store.LiveServiceRouter;
import com.kinvey.java.store.StoreType;

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
        BaseUserStore.registerLiveService();
        assertTrue(LiveServiceRouter.getInstance().isInitialized());
        BaseUserStore.unRegisterLiveService();
        assertFalse(LiveServiceRouter.getInstance().isInitialized());
    }

    @Test
    public void testSubscribeUnsubscribeSync() throws InterruptedException, IOException {
        assertTrue(client.isUserLoggedIn());
        BaseUserStore.registerLiveService();
        assertTrue(LiveServiceRouter.getInstance().isInitialized());
        store = DataStore.collection(LiveModel.COLLECTION, LiveModel.class, StoreType.SYNC, client);
        LiveModel model = new LiveModel();
        model.setUsername("Live model name");
        store.save(model);
        store.pushBlocking();
        store.subscribe(new KinveyDataStoreLiveServiceCallback<LiveModel>() {
            @Override
            public void onNext(LiveModel next) {
                System.out.println("TEST_TEST: " + next.toString());
            }

            @Override
            public void onError(Exception e) {
                System.out.println("TEST_TEST: " + e.toString());
            }

            @Override
            public void onStatus(KinveyLiveServiceStatus status) {
                System.out.println("TEST_TEST: " + status.toString());
            }
        });
        store.unsubscribe();
        BaseUserStore.unRegisterLiveService();
        assertFalse(LiveServiceRouter.getInstance().isInitialized());
    }

/*
    @Test
    public void testSubscribe() throws InterruptedException, IOException {
        assertTrue(client.isUserLoggedIn());
        BaseUserStore.registerLiveService();
        assertTrue(LiveServiceRouter.getInstance().isInitialized());
        store = DataStore.collection(LiveModel.COLLECTION, LiveModel.class, StoreType.SYNC, client);
        DataStore<LiveModel> networkStore = DataStore.collection(LiveModel.COLLECTION, LiveModel.class, StoreType.NETWORK, client);
        LiveModel model = new LiveModel();
        model.setUsername("Live model name");
        LiveModel savedModel = store.save(model);
        store.pushBlocking();
        testManager = new TestManager<>();


        final CountDownLatch latch = new CountDownLatch(1);
        CustomKinveyLiveServiceCallback<LiveModel> liveServiceCallback = new CustomKinveyLiveServiceCallback<>(latch);
        CustomKinveyClientCallback<Boolean> subscribeCallback = testManager.subscribe(store, liveServiceCallback);
        assertTrue(subscribeCallback.getResult());
        assertNull(subscribeCallback.getError());
        savedModel.setUsername("Live model name - changed");
        testManager.saveCustomInBackground(networkStore, savedModel);
        System.out.println("stopped");
        latch.await(20, TimeUnit.SECONDS);

        assertNotNull(liveServiceCallback.getResult());
        assertNull(liveServiceCallback.getError());
        assertEquals("Live model name - changed", liveServiceCallback.getResult().getUsername());
        store.unsubscribe();
        BaseUserStore.unRegisterLiveService();
        assertFalse(LiveServiceRouter.getInstance().isInitialized());
        networkStore.delete(liveServiceCallback.getResult().getId());
    }
*/

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

    @Test
    public void testLogout() throws InterruptedException, IOException {
        assertTrue(client.isUserLoggedIn());
        BaseUserStore.registerLiveService();
        assertTrue(LiveServiceRouter.getInstance().isInitialized());
        store = DataStore.collection(LiveModel.COLLECTION, LiveModel.class, StoreType.SYNC, client);
        store.subscribe(new KinveyDataStoreLiveServiceCallback<LiveModel>() {
            @Override
            public void onNext(LiveModel next) {
                System.out.println("TEST_TEST: " + next.toString());
            }

            @Override
            public void onError(Exception e) {
                System.out.println("TEST_TEST: " + e.toString());
            }

            @Override
            public void onStatus(KinveyLiveServiceStatus status) {
                System.out.println("TEST_TEST: " + status.toString());
            }
        });

        testManager = new TestManager<>();
        BaseUserStore.logout(client);
        assertFalse(LiveServiceRouter.getInstance().isInitialized());
    }

}
