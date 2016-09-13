package com.kinvey.androidTest.async;

import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.Client;
import com.kinvey.android.store.AsyncUserStore;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.dto.User;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.store.StoreType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class FileStoreAsyncTest {

    Client client = null;
    boolean success;
    FileMetaData fileMetaData;
    String fileID = "0706e906-c5d2-41fd-bcf5-282e73563c31";

/*    private static class AsyncTest<T> extends AsyncClientRequest<T> {
        private final T result;

        public AsyncTest(T result, KinveyClientCallback<T> callback) {
            super(callback);
            this.result = result;
        }

        @Override
        protected T executeAsync() throws IOException, InvocationTargetException, IllegalAccessException {
            return result;
        }
    }*/

    @Before
    public void setup() {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
/*        client.userStore().login("test", "test", new KinveyClientCallback<User>() {
            @Override
            public void onSuccess(User result) {
                user = result;
            }

            @Override
            public void onFailure(Throwable error) {

            }
        });*/
    }

    @Test
    public void uploadFileNetwork() throws InterruptedException, IOException {
        uploadFile(StoreType.NETWORK);
    }

    @Test
    public void uploadFileCache() throws InterruptedException, IOException {
        uploadFile(StoreType.CACHE);
    }

    @Test
    public void uploadFileSync() throws InterruptedException, IOException {
        uploadFile(StoreType.SYNC);
    }

    public void uploadFile(final StoreType storeType) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    try {

                        File file = new File(client.getContext().getFilesDir(), "test.xml");
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        FileMetaData fileMetaData = new FileMetaData();
                        fileMetaData.setFileName("test.xml");
                        client.getFileStore(storeType).upload(file, fileMetaData, new KinveyClientCallback<FileMetaData>() {
                            @Override
                            public void onSuccess(FileMetaData result) {

                                finish(true);
                            }

                            @Override
                            public void onFailure(Throwable error) {
                                finish(false);
                            }

                            public void finish(boolean result) {
                                success = result;
                                latch.countDown();
                            }
                        }, new UploaderProgressListener() {
                            @Override
                            public void progressChanged(MediaHttpUploader uploader) throws IOException {

                            }

                            @Override
                            public void onSuccess(FileMetaData result) {
                                finish(true);
                            }

                            @Override
                            public void onFailure(Throwable error) {
                                finish(false);
                            }

                            public void finish(boolean result) {
                                success = result;
                                latch.countDown();
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                        latch.countDown();
                    }
                    Looper.loop();
                }
            }).start();
        latch.await();
        assertTrue(success);
    }

    @Test
    public void downloadFileNetwork() throws InterruptedException, IOException {
        downloadFile(StoreType.NETWORK);
    }

    @Test
    public void downloadFileCache() throws InterruptedException, IOException {
        downloadFile(StoreType.CACHE);
    }

    @Test
    public void downloadFileSync() throws InterruptedException, IOException {
        downloadFile(StoreType.SYNC);
    }

    public void downloadFile(final StoreType storeType) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    FileMetaData fileMetaData = new FileMetaData();
                    fileMetaData.setId(fileID);

                    File file = new File(client.getContext().getFilesDir(), "testDownload.xml");
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    final FileOutputStream fos = new FileOutputStream(file);
                    client.getFileStore(storeType).download(fileMetaData, fos, new KinveyClientCallback<FileMetaData>() {
                        @Override
                        public void onSuccess(FileMetaData result) {
                            finish(true);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            finish(false);
                        }

                        public void finish(boolean result) {
                            success = result;
                            latch.countDown();
                        }
                    }, new DownloaderProgressListener() {
                        @Override
                        public void progressChanged(MediaHttpDownloader downloader) throws IOException {

                        }

                        @Override
                        public void onSuccess(Void result) {
                            finish(true);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            finish(false);
                        }

                        public void finish(boolean result) {
                            success = result;
                            latch.countDown();
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                    latch.countDown();
                }
                Looper.loop();
            }
        }).start();
        latch.await();
        assertTrue(success);
    }

}
