package com.kinvey.androidTest.store;

import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.google.common.io.Files;
import com.kinvey.android.Client;
import com.kinvey.android.callback.AsyncDownloaderProgressListener;
import com.kinvey.android.callback.AsyncUploaderProgressListener;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.store.UserStore;
import com.kinvey.java.Query;
import com.kinvey.java.cache.KinveyCachedClientCallback;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.query.MongoQueryFilter;
import com.kinvey.java.store.StoreType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class FileStoreTest {

    private Client client = null;
    private boolean success;
    private StoreType storeTypeResult;
    private static final String ID = "_id";
    private static final String ENCODING = "UTF-8";
    private static final String TEST_FILENAME = "test.xml";
    private static final String TEST_DOWNLOAD_FILENAME = "testDownload.xml";
    private static final String TEXT_IN_FILE = "Test String";
    private static final String USER = "test";
    private static final String PASSWORD = "test";

    private static class DefaultUploadProgressListener implements AsyncUploaderProgressListener<FileMetaData> {
        private CountDownLatch latch;
        private FileMetaData fileMetaDataResult;
        private Throwable error;
        private DefaultUploadProgressListener(CountDownLatch latch){
            this.latch = latch;
        }

        @Override
        public void progressChanged(MediaHttpUploader uploader) throws IOException {}

        @Override
        public void onCancelled() {

        }

        @Override
        public boolean isCancelled() {
            return false;
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

        private void finish() {
            latch.countDown();
        }
    }

    private static class DefaultDownloadProgressListener implements AsyncDownloaderProgressListener<FileMetaData> {

        private CountDownLatch latch;
        private FileMetaData fileMetaDataResult;
        private Throwable error;
        private DefaultDownloadProgressListener(CountDownLatch latch){
            this.latch = latch;
        }

        @Override
        public void progressChanged(MediaHttpDownloader downloader) throws IOException {

        }

        @Override
        public void onCancelled() {

        }

        @Override
        public boolean isCancelled() {
            return false;
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

        private void finish() {
            latch.countDown();
        }
    }

    private static class DefaultDeleteListener implements KinveyDeleteCallback {

        private CountDownLatch latch;
        private Integer result;
        private Throwable error;
        private DefaultDeleteListener(CountDownLatch latch){
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

        private void finish() {
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
                        UserStore.login(USER, PASSWORD, client, new KinveyClientCallback() {
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
        final File file = new File(client.getContext().getFilesDir(), TEST_FILENAME);
        if (!file.exists()) {
            file.createNewFile();
        }
        Files.write(TEXT_IN_FILE, file, Charset.forName(ENCODING));
        return file;
    }

    private FileMetaData testMetadata() {
        final FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setFileName(TEST_FILENAME);
        return fileMetaData;
    }

    private void nullUpload(StoreType storeType) throws IOException, InterruptedException {
        DefaultUploadProgressListener listener = uploadFile(storeType, null, null);
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

    public DefaultUploadProgressListener uploadFile(final StoreType storeType, final File f, final FileMetaData metaData) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);
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
        latch.await();
        return listener;
    }

    public void nullDownload(StoreType storeType) throws IOException, InterruptedException {
        DefaultDownloadProgressListener listener = downloadFile(storeType, null);
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
        DefaultUploadProgressListener listener = uploadFile(type, testFile(), testMetadata());
        assertNotNull(listener.fileMetaDataResult);
        DefaultDownloadProgressListener downloadListener = downloadFile(type, listener.fileMetaDataResult);
        assertNotNull(downloadListener.fileMetaDataResult);
        removeFile(type, listener.fileMetaDataResult);
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

    private KinveyCachedClientCallback<FileMetaData> createCachedClientCallback() {
        return new KinveyCachedClientCallback<FileMetaData>() {
            @Override
            public void onSuccess(FileMetaData result) {
                Log.d(Test.class.getName(), " KinveyCachedClientCallback onSuccess");
            }

            @Override
            public void onFailure(Throwable error) {
                Log.d(Test.class.getName(), " KinveyCachedClientCallback onFailure");
            }
        };
    }

    private KinveyCachedClientCallback<FileMetaData[]> createArrayCachedClientCallback() {
        return new KinveyCachedClientCallback<FileMetaData[]>() {
            @Override
            public void onSuccess(FileMetaData[] result) {
                Log.d(Test.class.getName(), " KinveyCachedClientCallback onSuccess");
            }

            @Override
            public void onFailure(Throwable error) {
                Log.d(Test.class.getName(), " KinveyCachedClientCallback onFailure");
            }
        };
    }

    public DefaultDownloadProgressListener downloadFile(final StoreType storeType, final FileMetaData metaFile) throws InterruptedException, IOException {
        final CountDownLatch downloadLatch = new CountDownLatch(1);
        final DefaultDownloadProgressListener listener = new DefaultDownloadProgressListener(downloadLatch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    File file = new File(client.getContext().getFilesDir(), TEST_DOWNLOAD_FILENAME);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    final FileOutputStream fos = new FileOutputStream(file);
                    client.getFileStore(storeType).download(metaFile, fos, listener,
                            storeType == StoreType.CACHE ? createCachedClientCallback() : null);

                } catch (IOException e) {
                    listener.onFailure(e);
                }
                Looper.loop();
            }
        }).start();
        downloadLatch.await();
        return listener;
    }

    public void executeRemoveFile(StoreType type) throws IOException, InterruptedException {
        DefaultUploadProgressListener listener = uploadFile(type, testFile(), testMetadata());
        assertNotNull(listener.fileMetaDataResult);
        DefaultDeleteListener deleteListener = removeFile(type, listener.fileMetaDataResult);
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

    public DefaultDeleteListener removeFile(final StoreType storeType, final FileMetaData fileMetaData) throws InterruptedException, IOException {
        final CountDownLatch deleteLatch = new CountDownLatch(1);
        final DefaultDeleteListener deleteListener = new DefaultDeleteListener(deleteLatch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    client.getFileStore(storeType).remove(fileMetaData, deleteListener);
                } catch (IOException e) {
                    deleteListener.onFailure(e);
                }
                Looper.loop();
            }
        }).start();
        deleteLatch.await();
        return deleteListener;
    }

    @Test
    public void testRefreshFileNetwork() throws InterruptedException, IOException {
        testRefreshFile(StoreType.NETWORK);
    }

    @Test
    public void testRefreshFileCache() throws InterruptedException, IOException {
        testRefreshFile(StoreType.CACHE);
    }

    @Test
    public void testRefreshFileSync() throws InterruptedException, IOException {
        testRefreshFile(StoreType.SYNC);
    }

    private void testRefreshFile(StoreType storeType) throws IOException, InterruptedException {
        DefaultUploadProgressListener listener = uploadFile(storeType, testFile(), testMetadata());
        assertNotNull(listener.fileMetaDataResult);
        refresh(storeType, listener.fileMetaDataResult);
        removeFile(storeType, listener.fileMetaDataResult);
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

                        private void finish(boolean result) {
                            success = result;
                            latch.countDown();
                        }
                    }, storeType == StoreType.CACHE ? createCachedClientCallback() : null);

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
        testFindFile(StoreType.NETWORK);
    }

    @Test
    public void testFindFileCache() throws InterruptedException, IOException {
        testFindFile(StoreType.CACHE);
    }

    @Test
    public void testFindFileSync() throws InterruptedException, IOException {
        testFindFile(StoreType.SYNC);
    }

    private void testFindFile(StoreType storeType) throws IOException, InterruptedException {
        DefaultUploadProgressListener listener = uploadFile(storeType, testFile(), testMetadata());
        assertNotNull(listener.fileMetaDataResult);
        find(storeType, listener.fileMetaDataResult, false);
        removeFile(storeType, listener.fileMetaDataResult);
    }

    public void find(final StoreType storeType, final FileMetaData fileMetaData, final boolean isCacheCleaning) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                Query query = new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).equals(ID, fileMetaData.getId());
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
                }, storeType == StoreType.CACHE ? createArrayCachedClientCallback() : null);

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
        DefaultUploadProgressListener listener = uploadFile(StoreType.CACHE, testFile(), testMetadata());
        assertNotNull(listener.fileMetaDataResult);
        client.getFileStore(StoreType.CACHE).clearCache();
        find(StoreType.CACHE, listener.fileMetaDataResult, false);
        removeFile(StoreType.CACHE, listener.fileMetaDataResult);
    }

    @Test
    public void testClearCacheSync() throws InterruptedException, IOException {
        DefaultUploadProgressListener listener = uploadFile(StoreType.SYNC, testFile(), testMetadata());
        assertNotNull(listener.fileMetaDataResult);
        client.getFileStore(StoreType.SYNC).clearCache();
        find(StoreType.SYNC, listener.fileMetaDataResult, true);
        removeFile(StoreType.SYNC, listener.fileMetaDataResult);
    }

}
