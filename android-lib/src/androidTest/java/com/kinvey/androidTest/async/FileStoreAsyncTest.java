package com.kinvey.androidTest.async;

import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.store.StoreType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class FileStoreAsyncTest {

    Client client = null;
    boolean success;
    FileMetaData fileMetaDataResult;

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
    public void testUploadFileNetworkNullCheck() throws InterruptedException, IOException {
        uploadFile(StoreType.NETWORK, false);
    }

    @Test
    public void testUploadFileCacheNullCheck() throws InterruptedException, IOException {
        uploadFile(StoreType.CACHE, false);
    }

    @Test
    public void testUploadFileSyncNullCheck() throws InterruptedException, IOException {
        uploadFile(StoreType.SYNC, false);
    }

    public FileMetaData uploadFile(final StoreType storeType, final boolean isPreload) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {

                    final File file = new File(client.getContext().getFilesDir(), "test.xml");
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    final FileMetaData fileMetaData = new FileMetaData();
                    fileMetaData.setFileName("test.xml");
                    client.getFileStore(storeType).upload(file, fileMetaData, new KinveyClientCallback<FileMetaData>() {
                        @Override
                        public void onSuccess(FileMetaData result) {
                            fileMetaDataResult = result;
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
                            fileMetaDataResult = result;
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
        if (isPreload) {
            return fileMetaDataResult;
        } else {
            assertTrue(success);
            return null;
        }
    }

    @Test
    public void testDownloadFileNetworkNullCheck() throws InterruptedException, IOException {
        downloadFile(StoreType.NETWORK, null);
    }

    @Test
    public void testDownloadFileNetwork() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFile(StoreType.NETWORK, true);
        downloadFile(StoreType.NETWORK, fileMetaData);
    }

    @Test
    public void testDownloadFileCacheNullCheck() throws InterruptedException, IOException {
        downloadFile(StoreType.CACHE, null);
    }

    @Test
    public void testDownloadFileCache() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFile(StoreType.CACHE, true);
        downloadFile(StoreType.CACHE, fileMetaData);
    }

    @Test
    public void testDownloadFileSyncNullCheck() throws InterruptedException, IOException {
        downloadFile(StoreType.SYNC, null);
    }

    @Test
    public void testDownloadFileSync() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFile(StoreType.SYNC, true);
        downloadFile(StoreType.SYNC, fileMetaData);
    }

    public void downloadFile(final StoreType storeType, final FileMetaData metaFile) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    File file = new File(client.getContext().getFilesDir(), "testDownload.xml");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    final FileOutputStream fos = new FileOutputStream(file);
                    client.getFileStore(storeType).download(metaFile, fos, new KinveyClientCallback<FileMetaData>() {
                        @Override
                        public void onSuccess(FileMetaData result) {
                            finish(true);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            if (metaFile == null) {
                                if (error.getCause().getMessage().contains("metadata must not be null")) {
                                    finish(true);
                                }
                            } else {
                                finish(false);
                            }
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

                            if (metaFile == null) {
                                if (error.getMessage().contains("Missing FileMetaData in cache")) {
                                    finish(true);
                                }
                            } else {
                                finish(false);
                            }
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
    public void testRemoveFileNetwork() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFile(StoreType.NETWORK, true);
        removeFile(StoreType.NETWORK, fileMetaData);
    }

    @Test
    public void testRemoveFileSync() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFile(StoreType.SYNC, true);
        removeFile(StoreType.SYNC, fileMetaData);
    }

    @Test
    public void testRemoveFileCache() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFile(StoreType.CACHE, true);
        removeFile(StoreType.CACHE, fileMetaData);
    }

    public void removeFile(final StoreType storeType, final FileMetaData fileMetaData) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    File file = new File(client.getContext().getFilesDir(), "testDownload.xml");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    client.getFileStore(storeType).remove(fileMetaData, new KinveyDeleteCallback() {
                        @Override
                        public void onSuccess(Integer result) {
                            finish(true);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            if (fileMetaData == null) {
                                if (error.getCause().getMessage().contains("metadata must not be null")) {
                                    finish(true);
                                }
                            } else {
                                finish(false);
                            }
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
