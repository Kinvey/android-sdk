package com.kinvey.androidTest.store.data.request;

import android.content.Context;
import android.os.Message;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.Client;
import com.kinvey.android.async.AsyncPushRequest;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.android.sync.KinveyPushResponse;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.KinveyException;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.StoreType;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class AsyncPushRequestTest {

    private Client client;

    @Before
    public void setUp() {
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        client = new Client.Builder(mMockContext).build();
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
    public void testPushSyncRequestOnProgress() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final KinveyPushCallback mockPushCallback = spy(new KinveyPushCallbackAdapter(latch));
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                NetworkManager<Person> spyNetworkManager = spy(new NetworkManager<>(Person.TEST_COLLECTION, Person.class, client));
                SyncManager syncManager = mock(SyncManager.class);
                try {
                    when(syncManager.executeRequest(any(Client.class), any(SyncRequest.class))).thenReturn(new GenericJson());
                } catch (IOException e) {
                    e.printStackTrace();
                    assertTrue(false);
                }
                List<SyncRequest> syncRequests = new ArrayList<>();
                syncRequests.add(new SyncRequest());
                when(syncManager.popSingleQueue(any(String.class))).thenReturn(syncRequests);

                new AsyncPushRequest<>(Person.COLLECTION, syncManager, client, StoreType.SYNC, spyNetworkManager, Person.class, mockPushCallback).execute();

            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        verify(mockPushCallback, times(1)).onProgress(any(long.class), any(long.class));
    }

    protected static class KinveyPushCallbackAdapter implements KinveyPushCallback {

        private CountDownLatch latch;

        protected KinveyPushCallbackAdapter(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(KinveyPushResponse result) {
            latch.countDown();
        }

        @Override
        public void onFailure(Throwable error) {
            latch.countDown();
        }

        @Override
        public void onProgress(long current, long all) {
        }

    }

    @Test
    public void testPushSyncRequestOnFailure() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final KinveyPushCallback mockPushCallback = spy(new KinveyPushCallbackAdapter(latch));
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                NetworkManager<Person> spyNetworkManager = spy(new NetworkManager<>(Person.TEST_COLLECTION, Person.class, client));
                SyncManager syncManager = mock(SyncManager.class);
                List<SyncRequest> syncRequests = new ArrayList<>();
                syncRequests.add(new SyncRequest());
                when(syncManager.popSingleQueue(any(String.class))).thenReturn(syncRequests);

                try {
                    doThrow(new IOException()).when(syncManager).executeRequest(any(Client.class), any(SyncRequest.class));
                } catch (IOException e) {
                    e.printStackTrace();
                    assertTrue(false);
                }
                new AsyncPushRequest<>(Person.COLLECTION, syncManager, client, StoreType.SYNC, spyNetworkManager, Person.class, mockPushCallback).execute();

            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        verify(mockPushCallback, times(1)).onFailure(any(Throwable.class));
    }

    protected static class DefaultKinveyPushCallback implements KinveyPushCallback {

        private CountDownLatch latch;
        KinveyPushResponse result;
        Throwable error;

        DefaultKinveyPushCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(KinveyPushResponse result) {
            this.result = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        @Override
        public void onProgress(long current, long all) {

        }

        void finish() {
            latch.countDown();
        }
    }

    @Test
    public void testPushSyncRequestKinveyException() throws InterruptedException {
        final String errorMessage = "TestException";
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyPushCallback callback = spy(new DefaultKinveyPushCallback(latch));
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                NetworkManager<Person> spyNetworkManager = spy(new NetworkManager<>(Person.TEST_COLLECTION, Person.class, client));
                SyncManager syncManager = mock(SyncManager.class);
                List<SyncRequest> syncRequests = new ArrayList<>();
                syncRequests.add(new SyncRequest());
                when(syncManager.popSingleQueue(any(String.class))).thenReturn(syncRequests);

                try {
                    doThrow(new KinveyException(errorMessage)).when(syncManager).executeRequest(any(Client.class), any(SyncRequest.class));
                } catch (IOException e) {
                    e.printStackTrace();
                    assertTrue(false);
                }
                new AsyncPushRequest<>(Person.COLLECTION, syncManager, client, StoreType.SYNC, spyNetworkManager, Person.class, callback).execute();

            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        verify(callback, times(0)).onFailure(any(Throwable.class));
        verify(callback, times(1)).onProgress(any(long.class), any(long.class));
        verify(callback, times(1)).onSuccess(any(KinveyPushResponse.class));
        assertEquals(errorMessage, ((KinveyException)callback.result.getListOfExceptions().get(0)).getReason());
    }

    @Test
    public void testPushSyncRequestAccessControlException() throws InterruptedException {
        final String errorMessage = "TestException";
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyPushCallback callback = spy(new DefaultKinveyPushCallback(latch));
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                NetworkManager<Person> spyNetworkManager = spy(new NetworkManager<>(Person.TEST_COLLECTION, Person.class, client));
                SyncManager syncManager = mock(SyncManager.class);
                List<SyncRequest> syncRequests = new ArrayList<>();
                syncRequests.add(new SyncRequest());
                when(syncManager.popSingleQueue(any(String.class))).thenReturn(syncRequests);

                try {
                    doThrow(new AccessControlException(errorMessage)).when(syncManager).executeRequest(any(Client.class), any(SyncRequest.class));
                } catch (IOException e) {
                    e.printStackTrace();
                    assertTrue(false);
                }
                new AsyncPushRequest<>(Person.COLLECTION, syncManager, client, StoreType.SYNC, spyNetworkManager, Person.class, callback).execute();

            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        verify(callback, times(0)).onFailure(any(Throwable.class));
        verify(callback, times(1)).onProgress(any(long.class), any(long.class));
        verify(callback, times(1)).onSuccess(any(KinveyPushResponse.class));
        assertEquals(errorMessage, callback.result.getListOfExceptions().get(0).getMessage());
    }



}
