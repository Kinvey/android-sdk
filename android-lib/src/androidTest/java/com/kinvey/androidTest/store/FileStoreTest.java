package com.kinvey.androidTest.store;

import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.kinvey.android.Client;
import com.kinvey.android.callback.AsyncDownloaderProgressListener;
import com.kinvey.android.callback.AsyncUploaderProgressListener;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.store.FileStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.java.Query;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.dto.User;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.query.MongoQueryFilter;
import com.kinvey.java.store.StoreType;
import com.kinvey.java.store.BaseUserStore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import static org.junit.Assert.*;


import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class FileStoreTest {

    private Client client = null;
    private boolean success;
    private StoreType storeTypeResult;

    private static class DefaultUploadProgressListener implements AsyncUploaderProgressListener<FileMetaData> {
        private CountDownLatch latch;
        FileMetaData fileMetaDataResult;
        Throwable error;


        DefaultUploadProgressListener(CountDownLatch latch){
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

    private static class DefaultDownloadProgressListener implements AsyncDownloaderProgressListener<FileMetaData> {

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
        if (!client.isUserLoggedIn()) {
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    try {
                        UserStore.login("test", "test", client, new KinveyClientCallback() {
                            @Override
                            public void onSuccess(Object result) {
                                latch.countDown();
                            }

                            @Override
                            public void onFailure(Throwable error) {
                                latch.countDown();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
        assertEquals(listener.error.getCause().getMessage(), "file must not be null");
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

    public void uploadFile(final StoreType storeType, final AsyncUploaderProgressListener<FileMetaData> listener,
                           final File f, final FileMetaData metaData) throws InterruptedException, IOException {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    client.getFileStore(storeType).upload(f, metaData, listener);
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
        assertEquals(listener.error.getCause().getMessage(), "metadata must not be null");
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

    public void downloadFile(final StoreType storeType, final FileMetaData metaFile, final AsyncDownloaderProgressListener<FileMetaData> listener) throws InterruptedException, IOException {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    File file = new File(client.getContext().getFilesDir(), "testDownload.xml");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    final FileOutputStream fos = new FileOutputStream(file);
                    client.getFileStore(storeType).download(metaFile, fos, listener);

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

                    client.getFileStore(storeType).remove(fileMetaData, callback);

                } catch (IOException e) {
                    callback.onFailure(e);
                }
                Looper.loop();
            }
        }).start();
    }

    @Test
    public void testDownloadFileByQueryNetwork() throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);

        uploadFile(StoreType.NETWORK, listener, testFile(), testMetadata());
        latch.await();

        assertNotNull(listener.fileMetaDataResult);
        Query query = new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).equals("_id", listener.fileMetaDataResult.getId());
        downloadFileQuery(StoreType.NETWORK, query);
    }

    @Test
    public void testDownloadFileByQuerySync() throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);

        uploadFile(StoreType.SYNC, listener, testFile(), testMetadata());
        latch.await();

        assertNotNull(listener.fileMetaDataResult);

        Query query = new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).equals("_id", listener.fileMetaDataResult.getId());
        downloadFileQuery(StoreType.SYNC, query);
    }

    @Test
    public void testDownloadFileByQueryCache() throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);

        uploadFile(StoreType.CACHE, listener, testFile(), testMetadata());
        latch.await();
        assertNotNull(listener.fileMetaDataResult);

        Query query = new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).equals("_id", listener.fileMetaDataResult.getId());
        downloadFileQuery(StoreType.CACHE, query);
    }

    public void downloadFileQuery(final StoreType storeType, final Query query) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    String dst = client.getContext().getFilesDir() + "test.xml";
                    client.getFileStore(storeType).download(query, dst, new AsyncDownloaderProgressListener<FileMetaData[]>() {
                        @Override
                        public void progressChanged(MediaHttpDownloader downloader) throws IOException {

                        }

                        @Override
                        public void onSuccess(FileMetaData[] result) {
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
    public void testDownloadFileByFileNameNetwork() throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);

        uploadFile(StoreType.NETWORK, listener, testFile(), testMetadata());
        latch.await();
        assertNotNull(listener.fileMetaDataResult);
        downloadFileByFileName(StoreType.NETWORK, listener.fileMetaDataResult.getFileName());
    }

    @Test
    public void testDownloadFileByFileNameCache() throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);

        uploadFile(StoreType.CACHE, listener, testFile(), testMetadata());
        latch.await();
        assertNotNull(listener.fileMetaDataResult);
        downloadFileByFileName(StoreType.CACHE, listener.fileMetaDataResult.getFileName());
    }

    @Test
    public void testDownloadFileByFileNameSync() throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);

        uploadFile(StoreType.SYNC, listener, testFile(), testMetadata());
        latch.await();
        assertNotNull(listener.fileMetaDataResult);
        downloadFileByFileName(StoreType.SYNC, listener.fileMetaDataResult.getFileName());
    }

    public void downloadFileByFileName(final StoreType storeType, final String fileName) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    String dst = client.getContext().getFilesDir() + fileName;
                    client.getFileStore(storeType).download(fileName, dst, new AsyncDownloaderProgressListener<FileMetaData[]>() {
                        @Override
                        public void progressChanged(MediaHttpDownloader downloader) throws IOException {
                            Log.d("downloadFileByFileName", String.valueOf(downloader.getProgress()));
                        }

                        @Override
                        public void onSuccess(FileMetaData[] result) {
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
    public void testRefreshFileNetwork() throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);

        uploadFile(StoreType.NETWORK, listener, testFile(), testMetadata());
        latch.await();
        assertNotNull(listener.fileMetaDataResult);
        refresh(StoreType.NETWORK, listener.fileMetaDataResult);
    }

    @Test
    public void testRefreshFileCache() throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);

        uploadFile(StoreType.CACHE, listener, testFile(), testMetadata());
        latch.await();
        assertNotNull(listener.fileMetaDataResult);
        listener.fileMetaDataResult.setPublic(true);
        refresh(StoreType.CACHE, listener.fileMetaDataResult);
    }

    @Test
    public void testRefreshFileSync() throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);

        uploadFile(StoreType.SYNC, listener, testFile(), testMetadata());
        latch.await();
        assertNotNull(listener.fileMetaDataResult);
        refresh(StoreType.SYNC, listener.fileMetaDataResult);
    }

    public void refresh(final StoreType storeType, final FileMetaData fileMetaData) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    client.getFileStore(storeType).refresh(fileMetaData, new KinveyClientCallback<FileMetaData>() {
                        @Override
                        public void onSuccess(FileMetaData result) {
                            finish(result != null);
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
    public void testFindFileNetwork() throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);

        uploadFile(StoreType.NETWORK, listener, testFile(), testMetadata());
        latch.await();
        assertNotNull(listener.fileMetaDataResult);
        find(StoreType.NETWORK, listener.fileMetaDataResult, false);
    }

    @Test
    public void testFindFileCache() throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);

        uploadFile(StoreType.CACHE, listener, testFile(), testMetadata());
        latch.await();
        assertNotNull(listener.fileMetaDataResult);
        find(StoreType.CACHE, listener.fileMetaDataResult, false);
    }

    @Test
    public void testFindFileSync() throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);

        uploadFile(StoreType.SYNC, listener, testFile(), testMetadata());
        latch.await();
        assertNotNull(listener.fileMetaDataResult);
        find(StoreType.SYNC, listener.fileMetaDataResult, false);
    }

    public void find(final StoreType storeType, final FileMetaData fileMetaData, final boolean isCacheCleaning) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                Query query = new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).equals("_id", fileMetaData.getId());
                client.getFileStore(storeType).find(query, new KinveyClientCallback<FileMetaData[]>() {
                    @Override
                    public void onSuccess(FileMetaData[] result) {
                        if (isCacheCleaning) {
                            finish(result != null && result.length == 0);
                        } else {
                            finish(result != null && result.length > 0);
                        }
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        if (isCacheCleaning) {
                            finish(true);
                        } else {
                            finish(false);
                        }
                    }

                    public void finish(boolean result) {
                        success = result;
                        latch.countDown();
                    }
                });

                Looper.loop();
            }
        }).start();
        latch.await();
        assertTrue(success);
        if (!isCacheCleaning) {
            storeTypeResult = storeType;
        } else {
            storeTypeResult = null;
        }
    }

    @Test
    public void testClearCacheCache() throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);

        uploadFile(StoreType.CACHE, listener, testFile(), testMetadata());
        latch.await();
        assertNotNull(listener.fileMetaDataResult);
        client.getFileStore(StoreType.CACHE).clearCache();
        find(StoreType.CACHE, listener.fileMetaDataResult, true);
    }

    @Test
    public void testClearCacheSync() throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);

        uploadFile(StoreType.CACHE, listener, testFile(), testMetadata());
        latch.await();
        assertNotNull(listener.fileMetaDataResult);
        client.getFileStore(StoreType.SYNC).clearCache();
        find(StoreType.SYNC, listener.fileMetaDataResult, true);
    }
}
