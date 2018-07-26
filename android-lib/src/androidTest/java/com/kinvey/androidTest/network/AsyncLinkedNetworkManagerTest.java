package com.kinvey.androidTest.network;

import android.content.Context;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.network.AsyncLinkedNetworkManager;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.TestManager;
import com.kinvey.java.Constants;
import com.kinvey.java.Query;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.network.LinkedNetworkManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class AsyncLinkedNetworkManagerTest {

    private Client client;
    private TestManager testManager;

    @Before
    public void setUp() throws InterruptedException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        client.enableDebugLogging();
        testManager = new TestManager();
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
    public void testConstructor() {
        AsyncLinkedNetworkManager<LinkedPerson> linkedNetworkManager =
                new AsyncLinkedNetworkManager<>(LinkedPerson.COLLECTION, LinkedPerson.class, client);
        assertNotNull(linkedNetworkManager);
    }

    @Test
    public void testGetEntity() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                CountDownLatch downLatch = new CountDownLatch(1);
                AsyncLinkedNetworkManager<LinkedPerson> linkedNetworkManager =
                        spy(new AsyncLinkedNetworkManager<>(LinkedPerson.COLLECTION, LinkedPerson.class, client));
                assertNotNull(linkedNetworkManager);
                LinkedPerson result = new LinkedPerson();
                result.setId("TestID");
                LinkedNetworkManager<LinkedPerson>.GetEntity getEntityRequest = mock(LinkedNetworkManager.GetEntity.class);
                try {
                    when(getEntityRequest.execute()).thenReturn(result);
                    when(linkedNetworkManager.getEntityBlocking(
                            any(String.class),
                            any(DownloaderProgressListener.class),
                            (String[]) isNull())).thenReturn(getEntityRequest);
                    //method for the test - getEntity
                    linkedNetworkManager.getEntity("", createKinveyClientCallback(downLatch), new DownloaderProgressListener() {
                        @Override
                        public void progressChanged(MediaHttpDownloader downloader) throws IOException {

                        }
                    });
                    downLatch.await(15, TimeUnit.SECONDS);
                    verify(linkedNetworkManager, times(1)).getEntityBlocking(any(String.class),
                            any(DownloaderProgressListener.class),
                            (String[]) isNull());
                    verify(getEntityRequest, times(1)).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                latch.countDown();

            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
    }

    private KinveyClientCallback<LinkedPerson> createKinveyClientCallback(final CountDownLatch latch) {
        return new KinveyClientCallback<LinkedPerson>() {

            @Override
            public void onSuccess(LinkedPerson result) {
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                latch.countDown();
            }
        };
    }



}
