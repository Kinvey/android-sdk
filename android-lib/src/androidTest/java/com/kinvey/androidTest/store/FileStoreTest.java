package com.kinvey.androidTest.store;

import android.content.Context;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.kinvey.android.Client;
import com.kinvey.android.callback.AsyncDownloaderProgressListener;
import com.kinvey.android.callback.AsyncUploaderProgressListener;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.model.User;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.java.Query;
import com.kinvey.java.cache.KinveyCachedClientCallback;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.query.MongoQueryFilter;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class FileStoreTest {

    private Client client = null;
    private boolean success;
    private StoreType storeTypeResult;
    private static final String ID = "_id";
    private static final String TEST_FILENAME = "test.xml";
    private static final String USER = "test";
    private static final String PASSWORD = "test";
    private static final int MB = 1024 * 1024;
    private static final int DEFAULT_FILE_SIZE_MB = 1;
    private static final int CUSTOM_FILE_SIZE_MB = 22;
    private static final int UPLOAD_CHUNK_SIZE_MB = 10;
    private static final int DOWNLOAD_CHUNK_SIZE_MB = 5;

    private static class DefaultUploadProgressListener implements AsyncUploaderProgressListener<FileMetaData> {
        private CountDownLatch latch;
        private FileMetaData fileMetaDataResult;
        private Throwable error;
        private boolean isCancelled = false;
        private boolean onCancelled = false;
        private int progressChangedCounter = 0;

        private DefaultUploadProgressListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void progressChanged(MediaHttpUploader uploader) throws IOException {
            progressChangedCounter++;
//            Log.d("UPLOAD TAG: ", String.valueOf(uploader.getProgress()));
        }

        @Override
        public void onCancelled() {
            onCancelled = true;
            finish();
        }

        @Override
        public boolean isCancelled() {
            return isCancelled;
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
        private boolean isCancelled = false;
        private boolean onCancelled = false;
        private int progressChangedCounter = 0;

        private DefaultDownloadProgressListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void progressChanged(MediaHttpDownloader downloader) throws IOException {
            progressChangedCounter++;
//            Log.d("DOWNLOAD TAG: ", String.valueOf(downloader.getProgress()));
        }

        @Override
        public void onCancelled() {
            onCancelled = true;
            finish();
        }

        @Override
        public boolean isCancelled() {
            return isCancelled;
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

        private DefaultDeleteListener(CountDownLatch latch) {
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
        LooperThread looperThread = null;
        if (!client.isUserLoggedIn()) {
            looperThread = new LooperThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        UserStore.login(client, new KinveyClientCallback<User>() {
                            @Override
                            public void onSuccess(User result) {
                                assertNotNull(result);
                                latch.countDown();
                            }

                            @Override
                            public void onFailure(Throwable error) {
                                assertNull(error);
                                latch.countDown();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            looperThread.start();
        } else {
            latch.countDown();
        }
        latch.await();
        if (looperThread != null) {
            looperThread.mHandler.sendMessage(new Message());
        }
    }


    private FileMetaData testMetadata() {
        final FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setFileName(TEST_FILENAME);
        return fileMetaData;
    }

    private void nullUpload(StoreType storeType) throws IOException, InterruptedException {
        DefaultUploadProgressListener listener = uploadFileWithMetadata(storeType, null, null);
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

    public DefaultUploadProgressListener uploadFileWithMetadata(final StoreType storeType, final File f, final FileMetaData metaData) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.getFileStore(storeType).upload(f, metaData, listener);
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onFailure(e);
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return listener;
    }

    private void nullDownload(StoreType storeType) throws IOException, InterruptedException {
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

    private void downloadFile(StoreType type) throws InterruptedException, IOException {
        File file = createFile(DEFAULT_FILE_SIZE_MB);
        DefaultUploadProgressListener listener = uploadFileWithMetadata(type, file, testMetadata());
        assertNotNull(listener.fileMetaDataResult);
        DefaultDownloadProgressListener downloadListener = downloadFile(type, listener.fileMetaDataResult);
        assertNotNull(downloadListener.fileMetaDataResult);
        file.delete();
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

    private DefaultDownloadProgressListener downloadFile(final StoreType storeType, final FileMetaData metaFile) throws InterruptedException, IOException {
        final CountDownLatch downloadLatch = new CountDownLatch(1);
        final DefaultDownloadProgressListener listener = new DefaultDownloadProgressListener(downloadLatch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final FileOutputStream fos = new FileOutputStream(createFile());
                    client.getFileStore(storeType).download(metaFile, fos, listener,
                            storeType == StoreType.CACHE ? createCachedClientCallback() : null);
                } catch (IOException e) {
                    listener.onFailure(e);
                }
            }
        });
        looperThread.start();
        downloadLatch.await();
        looperThread.mHandler.sendMessage(new Message());
        return listener;
    }

    private void executeRemoveFile(StoreType type) throws IOException, InterruptedException {
        File file = createFile(DEFAULT_FILE_SIZE_MB);
        DefaultUploadProgressListener listener = uploadFileWithMetadata(type, file, testMetadata());
        file.delete();
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

    private DefaultDeleteListener removeFile(final StoreType storeType, final FileMetaData fileMetaData) throws InterruptedException, IOException {
        final CountDownLatch deleteLatch = new CountDownLatch(1);
        final DefaultDeleteListener deleteListener = new DefaultDeleteListener(deleteLatch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.getFileStore(storeType).remove(fileMetaData, deleteListener);
                } catch (IOException e) {
                    deleteListener.onFailure(e);
                }
            }
        });
        looperThread.start();
        deleteLatch.await();
        looperThread.mHandler.sendMessage(new Message());
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
        File file = createFile(DEFAULT_FILE_SIZE_MB);
        DefaultUploadProgressListener listener = uploadFileWithMetadata(storeType, file, testMetadata());
        file.delete();
        assertNotNull(listener.fileMetaDataResult);
        refresh(storeType, listener.fileMetaDataResult);
        removeFile(storeType, listener.fileMetaDataResult);
    }

    private void refresh(final StoreType storeType, final FileMetaData fileMetaData) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
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
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
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
        File file = createFile(DEFAULT_FILE_SIZE_MB);
        DefaultUploadProgressListener listener = uploadFileWithMetadata(storeType, file, testMetadata());
        file.delete();
        assertNotNull(listener.fileMetaDataResult);
        find(storeType, listener.fileMetaDataResult, false);
        removeFile(storeType, listener.fileMetaDataResult);
    }

    private void find(final StoreType storeType, final FileMetaData fileMetaData, final boolean isCacheCleaning) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
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
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        assertTrue(success);
        if (!isCacheCleaning) {
            storeTypeResult = storeType;
        } else {
            storeTypeResult = null;
        }
    }

    @Test
    public void testClearCacheStoreCache() throws InterruptedException, IOException {
        File file = createFile(DEFAULT_FILE_SIZE_MB);
        DefaultUploadProgressListener listener = uploadFileWithMetadata(StoreType.CACHE, file, testMetadata());
        file.delete();
        assertNotNull(listener.fileMetaDataResult);
        client.getFileStore(StoreType.CACHE).clearCache();
        find(StoreType.CACHE, listener.fileMetaDataResult, false);
        removeFile(StoreType.CACHE, listener.fileMetaDataResult);
    }

    @Test
    public void testClearCacheStoreSync() throws InterruptedException, IOException {
        File file = createFile(DEFAULT_FILE_SIZE_MB);
        DefaultUploadProgressListener listener = uploadFileWithMetadata(StoreType.SYNC, file, testMetadata());
        file.delete();
        assertNotNull(listener.fileMetaDataResult);
        client.getFileStore(StoreType.SYNC).clearCache();
        find(StoreType.SYNC, listener.fileMetaDataResult, true);
        removeFile(StoreType.SYNC, listener.fileMetaDataResult);
    }

    @Test
    public void tesUploadFileNetwork() throws InterruptedException, IOException {
        testUploadFileWithMetadata(StoreType.NETWORK);
    }

    @Test
    public void tesUploadFileCache() throws InterruptedException, IOException {
        testUploadFileWithMetadata(StoreType.CACHE);
    }

    @Test
    public void tesUploadFileSync() throws InterruptedException, IOException {
        testUploadFileWithMetadata(StoreType.SYNC);
    }

    private void testUploadFileWithMetadata(StoreType type) throws IOException, InterruptedException {
        File file = createFile(DEFAULT_FILE_SIZE_MB);
        DefaultUploadProgressListener listener = uploadFileWithMetadata(type, file, testMetadata());
        file.delete();
        assertNotNull(listener.fileMetaDataResult);
        DefaultDeleteListener deleteListener = removeFile(type, listener.fileMetaDataResult);
        assertNotNull(deleteListener.result);
    }

    @Test
    public void testUploadInputStreamWithMetadataNetwork() throws InterruptedException, IOException {
        testUploadInputStreamWithMetadata(StoreType.NETWORK);
    }

    @Test
    public void testUploadInputStreamWithMetadataCache() throws InterruptedException, IOException {
        testUploadInputStreamWithMetadata(StoreType.CACHE);
    }

    @Test
    public void testUploadInputStreamWithMetadataSync() throws InterruptedException, IOException {
        testUploadInputStreamWithMetadata(StoreType.SYNC);
    }

    private void testUploadInputStreamWithMetadata(StoreType type) throws IOException, InterruptedException {
        File file = createFile(DEFAULT_FILE_SIZE_MB);
        DefaultUploadProgressListener listener = uploadInputStreamWithMetadata(type, new FileInputStream(file), testMetadata());
        file.delete();
        assertNotNull(listener.fileMetaDataResult);
        DefaultDeleteListener deleteListener = removeFile(type, listener.fileMetaDataResult);
        assertNotNull(deleteListener.result);
    }

    private DefaultUploadProgressListener uploadInputStreamWithMetadata(final StoreType storeType, final InputStream is, final FileMetaData metaData) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.getFileStore(storeType).upload(is, metaData, listener);
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onFailure(e);
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return listener;
    }


    @Test
    public void tesUploadFileWithOutMetadataNetwork() throws InterruptedException, IOException {
        testUploadFileWithOutMetadata(StoreType.NETWORK);
    }

    @Test
    public void tesUploadFileWithOutMetadataCache() throws InterruptedException, IOException {
        testUploadFileWithOutMetadata(StoreType.CACHE);
    }

    @Test
    public void tesUploadFileWithOutMetadataSync() throws InterruptedException, IOException {
        testUploadFileWithOutMetadata(StoreType.SYNC);
    }

    private void testUploadFileWithOutMetadata(StoreType type) throws IOException, InterruptedException {
        File file = createFile(DEFAULT_FILE_SIZE_MB);
        DefaultUploadProgressListener listener = uploadFileWithOutMetadata(type, file);
        file.delete();
        assertNotNull(listener.fileMetaDataResult);
        DefaultDeleteListener deleteListener = removeFile(type, listener.fileMetaDataResult);
        assertNotNull(deleteListener.result);
    }

    private DefaultUploadProgressListener uploadFileWithOutMetadata(final StoreType storeType, final File f) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.getFileStore(storeType).upload(f, listener);
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onFailure(e);
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return listener;
    }

    @Test
    public void testUploadInputStreamWithFileNameNetwork() throws InterruptedException, IOException {
        testUploadInputStreamWithFileName(StoreType.NETWORK);
    }

    @Test
    public void testUploadInputStreamWithFileNameCache() throws InterruptedException, IOException {
        testUploadInputStreamWithFileName(StoreType.CACHE);
    }

    @Test
    public void testUploadInputStreamWithFileNameSync() throws InterruptedException, IOException {
        testUploadInputStreamWithFileName(StoreType.SYNC);
    }

    private void testUploadInputStreamWithFileName(StoreType type) throws IOException, InterruptedException {
        File file = createFile(DEFAULT_FILE_SIZE_MB);
        DefaultUploadProgressListener listener = uploadInputStreamWithFilename(type, new FileInputStream(file), TEST_FILENAME);
        file.delete();
        assertNotNull(listener.fileMetaDataResult);
        DefaultDeleteListener deleteListener = removeFile(type, listener.fileMetaDataResult);
        assertNotNull(deleteListener.result);
    }

    private DefaultUploadProgressListener uploadInputStreamWithFilename(final StoreType storeType, final InputStream is, final String fileName) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.getFileStore(storeType).upload(fileName, is, listener);
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onFailure(e);
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return listener;
    }

    @Test
    public void testCancelFileUploadingNetwork() throws InterruptedException, IOException {
        testCancelFileUploading(StoreType.NETWORK);
    }

    @Test
    public void testCancelFileUploadingCache() throws InterruptedException, IOException {
        testCancelFileUploading(StoreType.CACHE);
    }

    @Test
    public void testCancelFileUploadingSync() throws InterruptedException, IOException {
        testCancelFileUploading(StoreType.SYNC);
    }

    private void testCancelFileUploading(StoreType storeType) throws IOException, InterruptedException {
        File file = createFile(CUSTOM_FILE_SIZE_MB);
        DefaultUploadProgressListener listener = cancelFileUploading(storeType, file);
        file.delete();
        assertTrue(listener.onCancelled);
        assertNull(listener.error);
        assertNull(listener.fileMetaDataResult);
    }

    private DefaultUploadProgressListener cancelFileUploading(final StoreType storeType, final File f) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.getFileStore(storeType).upload(f, listener);
                    listener.isCancelled = true;
                    client.getFileStore(storeType).cancelUploading();
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onFailure(e);
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return listener;
    }

    @Test
    public void testCancelFileDownloadingNetwork() throws InterruptedException, IOException {
        testCancelFileDownloading(StoreType.NETWORK);
    }

    @Test
    public void testCancelFileDownloadingCache() throws InterruptedException, IOException {
        testCancelFileDownloading(StoreType.CACHE);
    }

    @Test
    public void testCancelFileDownloadingSync() throws InterruptedException, IOException {
        testCancelFileDownloading(StoreType.SYNC);
    }

    private void testCancelFileDownloading(StoreType storeType) throws IOException, InterruptedException {
        DefaultUploadProgressListener listener = uploadFileWithOutMetadata(storeType, createFile(15));
        assertNotNull(listener.fileMetaDataResult);
        DefaultDownloadProgressListener downloadListener = cancelFileDownloading(storeType, listener.fileMetaDataResult);
        assertTrue(downloadListener.onCancelled);
        assertNull(downloadListener.error);
        assertNull(downloadListener.fileMetaDataResult);
        DefaultDeleteListener deleteListener = removeFile(storeType, listener.fileMetaDataResult);
        assertNotNull(deleteListener.result);
    }

    private DefaultDownloadProgressListener cancelFileDownloading(final StoreType storeType, final FileMetaData metaFile) throws InterruptedException, IOException {
        final CountDownLatch downloadLatch = new CountDownLatch(1);
        final DefaultDownloadProgressListener listener = new DefaultDownloadProgressListener(downloadLatch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final FileOutputStream fos = new FileOutputStream(createFile());
                    client.getFileStore(storeType).download(metaFile, fos, listener,
                            storeType == StoreType.CACHE ? createCachedClientCallback() : null);
                    listener.isCancelled = true;
                    client.getFileStore(storeType).cancelDownloading();
                } catch (IOException e) {
                    listener.onFailure(e);
                }
            }
        });
        looperThread.start();
        downloadLatch.await();
        looperThread.mHandler.sendMessage(new Message());
        return listener;
    }

    private File createFile(int mb) throws IOException {
        File file = createFile();
        RandomAccessFile f = new RandomAccessFile(createFile(), "rw");
        f.setLength(mb * MB);
        return file;
    }

    private File createFile() throws IOException {
        File file = new File(client.getContext().getFilesDir(), TEST_FILENAME);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    @Test
    public void testUploadPubliclyReadableFileNetwork() throws InterruptedException, IOException {
        testUploadPubliclyReadableFile(StoreType.NETWORK);
    }

    @Test
    public void testUploadPubliclyReadableFileCache() throws InterruptedException, IOException {
        testUploadPubliclyReadableFile(StoreType.CACHE);
    }

    @Test
    public void testUploadPubliclyReadableFileSync() throws InterruptedException, IOException {
        testUploadPubliclyReadableFile(StoreType.SYNC);
    }

    private void testUploadPubliclyReadableFile(StoreType storeType) throws IOException, InterruptedException {
        FileMetaData fileMetaData = testMetadata();
        fileMetaData.setPublic(true);
        File file = createFile(DEFAULT_FILE_SIZE_MB);
        DefaultUploadProgressListener listener = uploadFileWithMetadata(storeType, file, fileMetaData);
        file.delete();
        assertNotNull(listener.fileMetaDataResult);
        DefaultDownloadProgressListener downloadListener = downloadFile(storeType, listener.fileMetaDataResult);
        assertNull(downloadListener.error);
        assertNotNull(downloadListener.fileMetaDataResult);
        assertTrue(downloadListener.fileMetaDataResult.isPublic());
        removeFile(storeType, listener.fileMetaDataResult);
    }

    @Test
    public void testUploadPrivatelyReadableFileNetwork() throws InterruptedException, IOException {
        testUploadPrivatelyReadableFile(StoreType.NETWORK);
    }

    @Test
    public void testUploadPrivatelyReadableFileCache() throws InterruptedException, IOException {
        testUploadPrivatelyReadableFile(StoreType.CACHE);
    }

    @Test
    public void testUploadPrivatelyReadableFileSync() throws InterruptedException, IOException {
        testUploadPrivatelyReadableFile(StoreType.SYNC);
    }

    private void testUploadPrivatelyReadableFile(StoreType storeType) throws IOException, InterruptedException {
        FileMetaData fileMetaData = testMetadata();
        fileMetaData.setPublic(false);
        File file = createFile(DEFAULT_FILE_SIZE_MB);
        DefaultUploadProgressListener listener = uploadFileWithMetadata(storeType, file, fileMetaData);
        file.delete();
        assertNotNull(listener.fileMetaDataResult);
        DefaultDownloadProgressListener downloadListener = downloadFile(storeType, listener.fileMetaDataResult);
        assertNull(downloadListener.error);
        assertNotNull(downloadListener.fileMetaDataResult);
        removeFile(storeType, listener.fileMetaDataResult);
        assertFalse(downloadListener.fileMetaDataResult.isPublic());
    }

    @Test
    public void testUploadProgressChangingNetwork() throws InterruptedException, IOException {
        testUploadProgressChanging(StoreType.NETWORK);
    }

    @Test
    public void testUploadProgressChangingCache() throws InterruptedException, IOException {
        testUploadProgressChanging(StoreType.CACHE);
    }

    @Test
    public void testUploadProgressChangingSync() throws InterruptedException, IOException {
        testUploadProgressChanging(StoreType.SYNC);
    }

    private void testUploadProgressChanging(StoreType storeType) throws IOException, InterruptedException {
        File file = createFile(CUSTOM_FILE_SIZE_MB);
        DefaultUploadProgressListener listener = uploadFileWithOutMetadata(storeType, file);
        file.delete();
        assertNotNull(listener.fileMetaDataResult);
        if (storeType == StoreType.SYNC) {
            assertTrue(listener.progressChangedCounter == 0);
        } else {
            assertTrue(listener.progressChangedCounter >= CUSTOM_FILE_SIZE_MB / UPLOAD_CHUNK_SIZE_MB);
        }
        DefaultDeleteListener deleteListener = removeFile(storeType, listener.fileMetaDataResult);
        assertNotNull(deleteListener.result);
    }

    @Test
    public void testDownloadProgressChangingNetwork() throws InterruptedException, IOException {
        testDownloadProgressChanging(StoreType.NETWORK);
    }

    @Test
    public void testDownloadProgressChangingCache() throws InterruptedException, IOException {
        testDownloadProgressChanging(StoreType.CACHE);
    }

    @Test
    public void testDownloadProgressChangingSync() throws InterruptedException, IOException {
        testDownloadProgressChanging(StoreType.SYNC);
    }

    private void testDownloadProgressChanging(StoreType storeType) throws IOException, InterruptedException {
        File file = createFile(CUSTOM_FILE_SIZE_MB);
        DefaultUploadProgressListener listener = uploadFileWithOutMetadata(storeType, file);
        file.delete();
        assertNotNull(listener.fileMetaDataResult);
        DefaultDownloadProgressListener downloadListener = downloadFile(storeType, listener.fileMetaDataResult);
        assertNotNull(downloadListener.fileMetaDataResult);
        if (storeType == StoreType.SYNC) {
            assertTrue(downloadListener.progressChangedCounter == 0);
        } else {
            assertTrue(downloadListener.progressChangedCounter >= CUSTOM_FILE_SIZE_MB / DOWNLOAD_CHUNK_SIZE_MB);
        }
        DefaultDeleteListener deleteListener = removeFile(storeType, listener.fileMetaDataResult);
        assertNotNull(deleteListener.result);
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

}
