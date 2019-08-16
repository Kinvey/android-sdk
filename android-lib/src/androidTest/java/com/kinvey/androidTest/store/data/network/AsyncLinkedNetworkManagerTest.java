package com.kinvey.androidTest.store.data.network;

import android.content.Context;
import android.os.Message;

import androidx.test.runner.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.kinvey.android.AndroidMimeTypeFinder;
import com.kinvey.android.Client;
import com.kinvey.android.network.AsyncLinkedNetworkManager;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.TestManager;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.network.LinkedNetworkManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
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
    public void testMimeTypeFinder() {
        AndroidMimeTypeFinder androidMimeTypeFinder = mock(AndroidMimeTypeFinder.class);
        androidMimeTypeFinder.getMimeType(new FileMetaData(), new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        });
        verify(androidMimeTypeFinder, times(1)).getMimeType((FileMetaData) any(), (InputStream) any());
        verify(androidMimeTypeFinder, never()).getMimeType((FileMetaData) any());
    }

    @Test
    public void testMimeTypeFinderInputStream() {
        AndroidMimeTypeFinder androidMimeTypeFinder = new AndroidMimeTypeFinder();
        FileMetaData fileMetaData = new FileMetaData();
        androidMimeTypeFinder.getMimeType(fileMetaData, new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        });
        assertEquals(fileMetaData.getMimetype(), "application/octet-stream");
    }

    @Test
    public void testMimeTypeFinderNullFile() {
        AndroidMimeTypeFinder androidMimeTypeFinder = new AndroidMimeTypeFinder();
        FileMetaData fileMetaData = new FileMetaData();
        androidMimeTypeFinder.getMimeType(fileMetaData, (File) null);
        assertEquals(fileMetaData.getMimetype(), "application/octet-stream");
    }

    @Test
    public void testMimeTypeFinderNullMimetype() {
        AndroidMimeTypeFinder androidMimeTypeFinder = new AndroidMimeTypeFinder();
        FileMetaData fileMetaData = new FileMetaData();
        androidMimeTypeFinder.getMimeType(fileMetaData);
        assertEquals(fileMetaData.getMimetype(), "application/octet-stream");
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
