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
import com.kinvey.java.dto.User;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.store.StoreType;
import com.kinvey.java.store.UserStore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class FileStoreAsyncTest {

    Client client = null;


    private static class DefaultUploadProgressListener implements UploaderProgressListener {
        private CountDownLatch latch;
        FileMetaData fileMetaDataResult;
        Throwable error;


        public DefaultUploadProgressListener(CountDownLatch latch){
            this.latch = latch;
        }

        @Override
        public void progressChanged(MediaHttpUploader uploader) throws IOException {}

        @Override
        public void onSuccess(FileMetaData result) {
            this.fileMetaDataResult = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        public void finish() {
            latch.countDown();
        }
    }

    private static class DefaultDownloadProgressListener implements DownloaderProgressListener {

        private CountDownLatch latch;
        FileMetaData fileMetaDataResult;
        Throwable error;

        public DefaultDownloadProgressListener(CountDownLatch latch){
            this.latch = latch;
        }

        @Override
        public void progressChanged(MediaHttpDownloader downloader) throws IOException {

        }

        @Override
        public void onSuccess(FileMetaData result) {
            this.fileMetaDataResult = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        public void finish() {
            latch.countDown();
        }
    }

    private static class DefaultDeleteListener implements KinveyDeleteCallback {

        private CountDownLatch latch;
        Integer result;
        Throwable error;

        public DefaultDeleteListener(CountDownLatch latch){
            this.latch = latch;
        }

        @Override
        public void onSuccess(Integer result) {
            this.result = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        public void finish() {
            latch.countDown();
        }
    }


    @Before
    public void setup() throws InterruptedException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        final CountDownLatch latch = new CountDownLatch(1);
        if (!client.userStore().isUserLoggedIn()) {
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    client.userStore().login(new KinveyClientCallback<User>() {
                        @Override
                        public void onSuccess(User result) {
                            latch.countDown();
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            latch.countDown();
                        }
                    });
                    Looper.loop();
                }
            }).start();
        } else {
            latch.countDown();
        }
        latch.await();
    }

    private File testFile() throws IOException {
        final File file = new File(client.getContext().getFilesDir(), "test.xml");
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    private FileMetaData testMetadata() {
        final FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setFileName("test.xml");
        return fileMetaData;
    }

    private void nullUpload(StoreType storeType) throws IOException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);
        uploadFile(storeType, listener, null, null);
        latch.await();

        assertNotNull(listener.error);
        assertEquals(listener.error.getMessage(), "file must not be null");
    }

    @Test
    public void testUploadFileNetworkNullCheck() throws InterruptedException, IOException {
        nullUpload(StoreType.NETWORK);
    }

    @Test
    public void testUploadFileCacheNullCheck() throws InterruptedException, IOException {
        nullUpload(StoreType.CACHE);
    }

    @Test
    public void testUploadFileSyncNullCheck() throws InterruptedException, IOException {
        nullUpload(StoreType.SYNC);
    }

    public void uploadFile(final StoreType storeType, final UploaderProgressListener listener,
                           final File f, final FileMetaData metaData) throws InterruptedException, IOException {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {




                    client.getFileStore(storeType).uploadAsync(f, metaData, listener);

                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onFailure(e);
                }
                Looper.loop();
            }
        }).start();
    }

    public void nullDownload(StoreType storeType) throws IOException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        DefaultDownloadProgressListener listener = new DefaultDownloadProgressListener(latch);
        downloadFile(storeType, null, listener);
        latch.await();

        assertNotNull(listener.error);
        assertEquals(listener.error.getMessage(), "metadata must not be null");
    }

    @Test
    public void testDownloadFileNetworkNullCheck() throws InterruptedException, IOException {
        nullDownload(StoreType.NETWORK);
    }

    @Test
    public void testDownloadFileCacheNullCheck() throws InterruptedException, IOException {
        nullDownload(StoreType.CACHE);
    }

    @Test
    public void testDownloadFileSyncNullCheck() throws InterruptedException, IOException {
        nullDownload(StoreType.SYNC);
    }


    public void downloadFile(StoreType type) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);

        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);

        uploadFile(type, listener, testFile(), testMetadata());
        latch.await();

        assertNotNull(listener.fileMetaDataResult);

        final CountDownLatch downloadLatch = new CountDownLatch(1);

        DefaultDownloadProgressListener downloadListener = new DefaultDownloadProgressListener(downloadLatch);
        downloadFile(type, listener.fileMetaDataResult, downloadListener);
        downloadLatch.await();

        assertNotNull(downloadListener.fileMetaDataResult);
    }

    @Test
    public void testDownloadFileNetwork() throws InterruptedException, IOException {
        downloadFile(StoreType.NETWORK);
    }

    @Test
    public void testDownloadFileCache() throws InterruptedException, IOException {
        downloadFile(StoreType.CACHE);
    }



    @Test
    public void testDownloadFileSync() throws InterruptedException, IOException {
        downloadFile(StoreType.SYNC);
    }

    public void downloadFile(final StoreType storeType, final FileMetaData metaFile, final DownloaderProgressListener listener) throws InterruptedException, IOException {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    File file = new File(client.getContext().getFilesDir(), "testDownload.xml");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    final FileOutputStream fos = new FileOutputStream(file);
                    client.getFileStore(storeType).downloadAsync(metaFile, fos, listener);

                } catch (IOException e) {
                    listener.onFailure(e);
                }
                Looper.loop();
            }
        }).start();
    }

    public void executeRemoveFile(StoreType type) throws IOException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);

        uploadFile(type, listener, testFile(), testMetadata());
        latch.await();

        assertNotNull(listener.fileMetaDataResult);

        final CountDownLatch deleteLatch = new CountDownLatch(1);

        DefaultDeleteListener deleteListener = new DefaultDeleteListener(deleteLatch);
        removeFile(type, listener.fileMetaDataResult, deleteListener);
        deleteLatch.await();

        assertNotNull(deleteListener.result);
        assertTrue(deleteListener.result > 0);
    }

    @Test
    public void testRemoveFileNetwork() throws InterruptedException, IOException {
        executeRemoveFile(StoreType.NETWORK);
    }

    @Test
    public void testRemoveFileSync() throws InterruptedException, IOException {
        executeRemoveFile(StoreType.SYNC);
    }

    @Test
    public void testRemoveFileCache() throws InterruptedException, IOException {
        executeRemoveFile(StoreType.CACHE);
    }

    public void removeFile(final StoreType storeType, final FileMetaData fileMetaData, final KinveyDeleteCallback callback) throws InterruptedException, IOException {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    File file = new File(client.getContext().getFilesDir(), "testDownload.xml");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    client.getFileStore(storeType).remove(fileMetaData, callback);

                } catch (IOException e) {
                    callback.onFailure(e);
                }
                Looper.loop();
            }
        }).start();
    }
}
