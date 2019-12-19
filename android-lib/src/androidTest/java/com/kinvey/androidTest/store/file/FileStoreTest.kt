package com.kinvey.androidTest.store.file

import android.content.Context
import android.os.Message
import android.util.Log
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.callback.AsyncDownloaderProgressListener
import com.kinvey.android.callback.AsyncUploaderProgressListener
import com.kinvey.android.callback.KinveyDeleteCallback
import com.kinvey.android.model.User
import com.kinvey.android.store.UserStore.Companion.login
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.TestManager.PASSWORD
import com.kinvey.androidTest.TestManager.USERNAME
import com.kinvey.java.AbstractClient
import com.kinvey.java.Query
import com.kinvey.java.cache.KinveyCachedClientCallback
import com.kinvey.java.core.*
import com.kinvey.java.model.FileMetaData
import com.kinvey.java.model.KinveyMetaData.AccessControlList
import com.kinvey.java.query.MongoQueryFilter.MongoQueryFilterBuilder
import com.kinvey.java.store.StoreType
import junit.framework.Assert.assertFalse
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.spy
import java.io.*
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@SmallTest
class FileStoreTest {

    private var client: Client<*>? = null
    private var success = false
    private var storeTypeResult: StoreType? = null

    class DefaultUploadProgressListener(private val latch: CountDownLatch) : AsyncUploaderProgressListener<FileMetaData> {

        var fileMetaDataResult: FileMetaData? = null
        var error: Throwable? = null
        override var isCancelled: Boolean = false
        var onCancelled = false
        var progressChangedCounter = 0

        @Throws(IOException::class)
        override fun progressChanged(uploader: MediaHttpUploader?) {
            progressChangedCounter++
//            Log.d("UPLOAD TAG: ", String.valueOf(uploader.getProgress()));
        }

        override fun onCancelled() {
            onCancelled = true
            finish()
        }

        override fun onSuccess(result: FileMetaData?) {
            fileMetaDataResult = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        private fun finish() {
            latch.countDown()
        }
    }

    protected class ResumeUploadProgressListener(private val latch: CountDownLatch) : MetaUploadProgressListener(), UploaderProgressListener {

        private var progressChangedCounter = 0
        var uploadMetadata: FileMetaData? = null

        override fun metaDataRetrieved(meta: FileMetaData?) {
            uploadMetadata = meta
        }

        @Throws(IOException::class)
        override fun progressChanged(uploader: MediaHttpUploader?) {
            progressChangedCounter++
            if (progressChangedCounter == 1) {
                finish()
            }
        }

        private fun finish() {
            latch.countDown()
        }
    }

    protected class DefaultDownloadProgressListener(private val latch: CountDownLatch) : AsyncDownloaderProgressListener<FileMetaData> {

        var fileMetaDataResult: FileMetaData? = null
        var error: Throwable? = null
        override var isCancelled: Boolean = false
        var onCancelled = false
        var progressChangedCounter = 0

        @Throws(IOException::class)
        override fun progressChanged(downloader: MediaHttpDownloader) {
            progressChangedCounter++
//          Log.d("DOWNLOAD TAG: ", String.valueOf(downloader.getProgress()));
        }

        override fun onCancelled() {
            onCancelled = true
            finish()
        }

        override fun onSuccess(result: FileMetaData?) {
            fileMetaDataResult = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        private fun finish() {
            latch.countDown()
        }
    }

    private class DefaultDeleteListener(private val latch: CountDownLatch) : KinveyDeleteCallback {

        var result: Int? = null
        private var error: Throwable? = null

        override fun onSuccess(result: Int?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        private fun finish() {
            latch.countDown()
        }
    }

    private class DefaultDownloadCachedListener() : KinveyCachedClientCallback<FileMetaData> {

        var latch: CountDownLatch? = null
        var result: FileMetaData? = null
        var error: Throwable? = null

        private constructor(latch: CountDownLatch?): this() {
            this.latch = latch
        }

        override fun onSuccess(result: FileMetaData?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        private fun finish() {
            latch?.countDown()
        }
    }

    private class DefaultDownloadCallback() : KinveyClientCallback<FileMetaData> {

        var latch: CountDownLatch? = null
        var result: FileMetaData? = null
        private var error: Throwable? = null

        constructor(latch: CountDownLatch): this() {
            this.latch = latch
        }

        override fun onSuccess(result: FileMetaData?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        private fun finish() {
            latch?.countDown()
        }
    }

    @Before
    @Throws(InterruptedException::class)
    fun setup() {

        val mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        val latch = CountDownLatch(1)
        var looperThread: LooperThread? = null

        if (client?.isUserLoggedIn == false) {
            looperThread = LooperThread(Runnable {
                try {
                    login(USERNAME, PASSWORD, client as AbstractClient<User>, object : KinveyClientCallback<User> {
                        override fun onSuccess(result: User?) {
                            assertNotNull(result)
                            latch.countDown()
                        }

                        override fun onFailure(error: Throwable?) {
                            assertNull(error)
                            latch.countDown()
                        }
                    })
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            })
            looperThread.start()
        } else {
            latch.countDown()
        }
        latch.await()
        looperThread?.mHandler?.sendMessage(Message())
    }

    private fun testMetadata(): FileMetaData {
        val fileMetaData = FileMetaData()
        fileMetaData.fileName = TEST_FILENAME
        return fileMetaData
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun nullFileUpload(storeType: StoreType) {
        val listener = uploadFileWithMetadata(storeType, null, null)
        assertNotNull(listener.error)
        assertEquals(listener.error?.message, "Parameter specified as non-null is null: method kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull, parameter file")
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun nullMetadataUpload(storeType: StoreType) {
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadFileWithMetadata(storeType, file, null)
        assertNotNull(listener.error)
        assertEquals(listener.error?.message, "Parameter specified as non-null is null: method kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull, parameter metadata")
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun nullUpload(storeType: StoreType) {
        nullFileUpload(storeType)
        nullMetadataUpload(storeType)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadFileNetworkNullCheck() {
        nullUpload(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadFileCacheNullCheck() {
        nullUpload(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadFileAUTONullCheck() {
        nullUpload(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadFileSyncNullCheck() {
        nullUpload(StoreType.SYNC)
    }

    @Throws(InterruptedException::class, IOException::class)
    fun uploadFileWithMetadata(storeType: StoreType, f: File?, metaData: FileMetaData?): DefaultUploadProgressListener {
        val latch = CountDownLatch(1)
        val listener = DefaultUploadProgressListener(latch)
        val looperThread = LooperThread(Runnable {
            try {
                client?.getFileStore(storeType)?.upload(f, metaData, listener)
            } catch (e: Throwable) {
                e.printStackTrace()
                listener.onFailure(e)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        return listener
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun nullDownload(storeType: StoreType) {
        val listener = downloadFile(storeType, null)
        assertNotNull(listener.error)
        assertEquals(listener.error!!.message, "Parameter specified as non-null is null: method kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull, parameter metadata")
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDownloadFileNetworkNullCheck() {
        nullDownload(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDownloadFileCacheNullCheck() {
        nullDownload(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDownloadFileAutoNullCheck() {
        nullDownload(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDownloadFileSyncNullCheck() {
        nullDownload(StoreType.SYNC)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun downloadFile(type: StoreType) {
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadFileWithMetadata(type, file, testMetadata())
        assertNotNull(listener.fileMetaDataResult)
        val cachedListener = DefaultDownloadCachedListener()
        val downloadListener = downloadFile(type, cachedListener, listener.fileMetaDataResult)
        assertNotNull(downloadListener.fileMetaDataResult)
        if (type === StoreType.CACHE) {
            assertNotNull(cachedListener.result)
            assertNotNull(cachedListener.result!!.path)
            assertNull(cachedListener.error)
        }
        assertNull(downloadListener.error)
        file.delete()
        removeFile(type, listener.fileMetaDataResult)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun downloadCachedOutputStreamFile(type: StoreType) {
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadFileWithMetadata(type, file, testMetadata())
        assertNotNull(listener.fileMetaDataResult)
        val cachedListener = DefaultDownloadCachedListener()
        val downloadListener = downloadCachedOutputStreamFile(type, cachedListener, listener.fileMetaDataResult)
        assertNotNull(downloadListener.fileMetaDataResult)
        if (type === StoreType.CACHE) {
            assertNotNull(cachedListener.result)
            assertNull(cachedListener.error)
        }
        assertNull(downloadListener.error)
        file.delete()
        removeFile(type, listener.fileMetaDataResult)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testDownloadCachedFile_TypeSync() {
        testDownloadFileNoCachedCallback(StoreType.SYNC)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testDownloadCachedFile_TypeCache() {
        testDownloadFileNoCachedCallback(StoreType.CACHE)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testDownloadCachedFile_TypeAuto() {
        testDownloadFileNoCachedCallback(StoreType.AUTO)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testDownloadCachedFile_TypeNetwork() {
        testDownloadFileNoCachedCallback(StoreType.NETWORK)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testDownloadFileNoCachedCallback(storeType: StoreType) {
        val file = createFile()
        val listener = uploadFileWithMetadata(storeType, file, testMetadata())
        assertNotNull(listener.fileMetaDataResult)
        val downloadListener = downloadCachedOutputStreamFile(storeType, null, listener.fileMetaDataResult)
        assertNotNull(downloadListener.fileMetaDataResult)
        assertNull(downloadListener.error)
        file.delete()
        removeFile(storeType, listener.fileMetaDataResult)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDownloadFileNetwork() {
        downloadFile(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDownloadFileCache() {
        downloadFile(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDownloadFileAuto() {
        downloadFile(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDownloadFileSync() {
        downloadCachedOutputStreamFile(StoreType.SYNC)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDownloadFileWithCachedOutputStreamNetwork() {
        downloadCachedOutputStreamFile(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDownloadFileWithCachedOutputStreamCache() {
        downloadCachedOutputStreamFile(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDownloadFileWithCachedOutputStreamAuto() {
        downloadCachedOutputStreamFile(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDownloadFileWithCachedOutputStreamSync() {
        downloadFile(StoreType.SYNC)
    }

    private fun createOutputStream(): FileOutputStream? {
        return try {
            FileOutputStream(createFile("new_file.txt"))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun createCachedClientCallback(): KinveyCachedClientCallback<FileMetaData>? {
        return object : KinveyCachedClientCallback<FileMetaData> {
            override fun onSuccess(result: FileMetaData?) {
                Log.d(Test::class.java.name, " KinveyCachedClientCallback onSuccess")
            }

            override fun onFailure(error: Throwable?) {
                Log.d(Test::class.java.name, " KinveyCachedClientCallback onFailure")
            }
        }
    }

    private fun createArrayCachedClientCallback(): KinveyCachedClientCallback<Array<FileMetaData>> {
        return object : KinveyCachedClientCallback<Array<FileMetaData>> {
            override fun onSuccess(result: Array<FileMetaData>?) {
                Log.d(Test::class.java.name, " KinveyCachedClientCallback onSuccess")
            }

            override fun onFailure(error: Throwable?) {
                Log.d(Test::class.java.name, " KinveyCachedClientCallback onFailure")
            }
        }
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun downloadFile(storeType: StoreType, metaFile: FileMetaData?): DefaultDownloadProgressListener {
        val downloadLatch = CountDownLatch(1)
        val listener = DefaultDownloadProgressListener(downloadLatch)
        val looperThread = LooperThread(Runnable {
            try {
                val fos = FileOutputStream(createFile())
                client?.getFileStore(storeType)?.download(metaFile, fos, listener,
                        if (storeType === StoreType.CACHE) createCachedClientCallback() else null)
            } catch (e: Throwable) {
                listener.onFailure(e)
            }
        })
        looperThread.start()
        downloadLatch.await()
        looperThread.mHandler.sendMessage(Message())
        return listener
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun downloadFile(storeType: StoreType,
                             cachedListener: DefaultDownloadCachedListener, metaFile: FileMetaData?)
            : DefaultDownloadProgressListener {
        val downloadLatch = CountDownLatch(if (storeType === StoreType.CACHE) 2 else 1)
        val listener = DefaultDownloadProgressListener(downloadLatch)
        cachedListener.latch = downloadLatch
        val looperThread = LooperThread(Runnable {
            try {
                val fos = FileOutputStream(createFile())
                client!!.getFileStore(storeType).download(metaFile, fos, listener,
                        if (storeType === StoreType.CACHE) cachedListener else null)
            } catch (e: IOException) {
                listener.onFailure(e)
            }
        })
        looperThread.start()
        downloadLatch.await()
        looperThread.mHandler.sendMessage(Message())
        return listener
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun downloadCachedOutputStreamFile(storeType: StoreType,
                                               cachedListener: DefaultDownloadCachedListener?, metaFile: FileMetaData?)
            : DefaultDownloadProgressListener {
        val downloadLatch = CountDownLatch(if (storeType === StoreType.CACHE) 2 else 1)
        val listener = DefaultDownloadProgressListener(downloadLatch)
        if (cachedListener != null) {
            cachedListener.latch = downloadLatch
        } else if (storeType === StoreType.CACHE) {
            downloadLatch.countDown()
        }
        val looperThread = LooperThread(Runnable {
            try {
                val fos = FileOutputStream(createFile())
                if (cachedListener != null) {
                    client?.getFileStore(storeType)?.download(metaFile, fos, listener,
                            if (storeType === StoreType.CACHE) createOutputStream() else null,
                            if (storeType === StoreType.CACHE) cachedListener else null)
                } else {
                    client?.getFileStore(storeType)?.download(metaFile, fos, listener)
                }
            } catch (e: IOException) {
                listener.onFailure(e)
            }
        })
        looperThread.start()
        downloadLatch.await()
        looperThread.mHandler.sendMessage(Message())
        return listener
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun executeRemoveFile(type: StoreType) {
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadFileWithMetadata(type, file, testMetadata())
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        val deleteListener = removeFile(type, listener.fileMetaDataResult)
        assertNotNull(deleteListener.result)
        assertTrue(deleteListener.result ?: 0 > 0)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testRemoveFileNetwork() {
        executeRemoveFile(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testRemoveFileSync() {
        executeRemoveFile(StoreType.SYNC)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testRemoveFileCache() {
        executeRemoveFile(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testRemoveFileAuto() {
        executeRemoveFile(StoreType.AUTO)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun removeFile(storeType: StoreType, fileMetaData: FileMetaData?): DefaultDeleteListener {
        val deleteLatch = CountDownLatch(1)
        val deleteListener = DefaultDeleteListener(deleteLatch)
        val looperThread = LooperThread(Runnable {
            try {
                client?.getFileStore(storeType)?.remove(fileMetaData, deleteListener)
            } catch (e: IOException) {
                deleteListener.onFailure(e)
            }
        })
        looperThread.start()
        deleteLatch.await()
        looperThread.mHandler.sendMessage(Message())
        return deleteListener
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testRefreshFileNetwork() {
        testRefreshFile(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testRefreshFileCache() {
        testRefreshFile(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testRefreshFileAuto() {
        testRefreshFile(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testRefreshFileSync() {
        testRefreshFile(StoreType.SYNC)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testRefreshFile(storeType: StoreType) {
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadFileWithMetadata(storeType, file, testMetadata())
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        refresh(storeType, listener.fileMetaDataResult, true)
        removeFile(storeType, listener.fileMetaDataResult)
    }

    @Throws(InterruptedException::class)
    private fun refresh(storeType: StoreType, fileMetaData: FileMetaData?, isCreateCachedCallback: Boolean) {
        val latch = CountDownLatch(1)
        val callback = DefaultDownloadCallback(latch)
        val looperThread = LooperThread(Runnable {
            try {
                if (isCreateCachedCallback) {
                    client?.getFileStore(storeType)?.refresh(fileMetaData, callback,
                            if (storeType === StoreType.CACHE) createCachedClientCallback() else null)
                } else {
                    client?.getFileStore(storeType)?.refresh(fileMetaData, callback)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                latch.countDown()
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        assertNotNull(callback.result)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testRefreshFile_TypeSync() {
        testRefreshFileNoCachedCallback(StoreType.SYNC)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testRefreshFile_TypeCache() {
        testRefreshFileNoCachedCallback(StoreType.CACHE)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testRefreshFile_TypeAuto() {
        testRefreshFileNoCachedCallback(StoreType.AUTO)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testRefreshFile_TypeNetwork() {
        testRefreshFileNoCachedCallback(StoreType.NETWORK)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testRefreshFileNoCachedCallback(storeType: StoreType) {
        val file = createFile()
        val listener = uploadFileWithMetadata(storeType, file, testMetadata())
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        refresh(storeType, listener.fileMetaDataResult, false)
        removeFile(storeType, listener.fileMetaDataResult)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFindFileNetwork() {
        testFindFile(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFindFileCache() {
        testFindFile(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFindFileAuto() {
        testFindFile(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFindFileSync() {
        testFindFile(StoreType.SYNC)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testFindFile(storeType: StoreType) {
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadFileWithMetadata(storeType, file, testMetadata())
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        find(storeType, listener.fileMetaDataResult, false)
        removeFile(storeType, listener.fileMetaDataResult)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun find(storeType: StoreType, fileMetaData: FileMetaData?, isCacheCleaning: Boolean) {
        val latch = CountDownLatch(1)
        val looperThread = LooperThread(Runnable {
            val query = Query(MongoQueryFilterBuilder()).equals(ID, fileMetaData?.id)
            client?.getFileStore(storeType)?.find(query, object : KinveyClientCallback<Array<FileMetaData>> {
                override fun onSuccess(result: Array<FileMetaData>?) {
                    if (isCacheCleaning) {
                        finish(result != null && result.size == 0)
                    } else {
                        finish(result != null && result.size > 0)
                    }
                }
                override fun onFailure(error: Throwable?) {
                    if (isCacheCleaning) {
                        finish(true)
                    } else {
                        finish(false)
                    }
                }

                fun finish(result: Boolean) {
                    success = result
                    latch.countDown()
                }
            }, if (storeType === StoreType.CACHE) createArrayCachedClientCallback() else null)
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        assertTrue(success)
        storeTypeResult = if (!isCacheCleaning) {
            storeType
        } else {
            null
        }
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testFindFile_TypeSync() {
        testFindFileNoCachedCallback(StoreType.SYNC)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testFindFile_TypeCache() {
        testFindFileNoCachedCallback(StoreType.CACHE)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testFindFile_TypeAuto() {
        testFindFileNoCachedCallback(StoreType.AUTO)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testFindFile_TypeNetwork() {
        testFindFileNoCachedCallback(StoreType.NETWORK)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testFindFileNoCachedCallback(storeType: StoreType) {
        val file = createFile()
        val listener = uploadFileWithMetadata(storeType, file, testMetadata())
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        find(storeType, listener.fileMetaDataResult)
        removeFile(storeType, listener.fileMetaDataResult)
    }

    @Throws(InterruptedException::class)
    private fun find(storeType: StoreType, fileMetaData: FileMetaData?) {
        val latch = CountDownLatch(1)
        val looperThread = LooperThread(Runnable {
            val query = Query().equals(ID, fileMetaData?.id)
            client?.getFileStore(storeType)?.find(query, object : KinveyClientCallback<Array<FileMetaData>> {
                override fun onSuccess(result: Array<FileMetaData>?) {
                    finish(result != null && result.size > 0)
                }

                override fun onFailure(error: Throwable?) {
                    finish(false)
                }

                fun finish(result: Boolean) {
                    success = result
                    latch.countDown()
                }
            })
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        assertTrue(success)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testGetCacheFile_TypeSync() {
        testGetCacheFileById(StoreType.SYNC)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testGetCacheFile_TypeNetwork() {
        testGetCacheFileById(StoreType.NETWORK)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testGetCacheFile_TypeCache() {
        testGetCacheFileById(StoreType.CACHE)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testGetCacheFile_TypeAuto() {
        testGetCacheFileById(StoreType.AUTO)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testGetCacheFileById(storeType: StoreType?) {
        val file = createFile()
        val listener = uploadFileWithMetadata(StoreType.SYNC, file, testMetadata())
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        val fileMetaData = client?.getFileStore(storeType)?.cachedFile(listener.fileMetaDataResult?.id ?: "")
        assertNotNull(fileMetaData)
        removeFile(StoreType.SYNC, listener.fileMetaDataResult)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testGetCacheFileByFileMetadata_TypeSync() {
        testGetCacheFileByFileMetadata(StoreType.SYNC)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testGetCacheFileByFileMetadata_TypeNetwork() {
        testGetCacheFileByFileMetadata(StoreType.NETWORK)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testGetCacheFileByFileMetadata_TypeCache() {
        testGetCacheFileByFileMetadata(StoreType.CACHE)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testGetCacheFileByFileMetadata_TypeAuto() {
        testGetCacheFileByFileMetadata(StoreType.AUTO)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testGetCacheFileByFileMetadata(storeType: StoreType?) {
        val file = createFile()
        val listener = uploadFileWithMetadata(StoreType.SYNC, file, testMetadata())
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        val fileMetaData = client?.getFileStore(storeType)?.cachedFile(listener.fileMetaDataResult)
        assertNotNull(fileMetaData)
        removeFile(StoreType.SYNC, listener.fileMetaDataResult)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testClearCacheStoreCache() {
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadFileWithMetadata(StoreType.CACHE, file, testMetadata())
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        client?.getFileStore(StoreType.CACHE)?.clearCache()
        find(StoreType.CACHE, listener.fileMetaDataResult, false)
        removeFile(StoreType.CACHE, listener.fileMetaDataResult)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testClearCacheStoreSync() {
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadFileWithMetadata(StoreType.SYNC, file, testMetadata())
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        client?.getFileStore(StoreType.SYNC)?.clearCache()
        find(StoreType.SYNC, listener.fileMetaDataResult, true)
        removeFile(StoreType.SYNC, listener.fileMetaDataResult)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testClearCacheStoreAuto() {
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadFileWithMetadata(StoreType.AUTO, file, testMetadata())
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        client?.getFileStore(StoreType.AUTO)?.clearCache()
        find(StoreType.AUTO, listener.fileMetaDataResult, false)
        removeFile(StoreType.AUTO, listener.fileMetaDataResult)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun tesUploadFileNetwork() {
        testUploadFileWithMetadata(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun tesUploadFileCache() {
        testUploadFileWithMetadata(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun tesUploadFileAuto() {
        testUploadFileWithMetadata(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun tesUploadFileSync() {
        testUploadFileWithMetadata(StoreType.SYNC)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testUploadFileWithMetadata(type: StoreType) {
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadFileWithMetadata(type, file, testMetadata())
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        val deleteListener = removeFile(type, listener.fileMetaDataResult)
        assertNotNull(deleteListener.result)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadInputStreamWithMetadataNetwork() {
        testUploadInputStreamWithMetadata(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadInputStreamWithMetadataCache() {
        testUploadInputStreamWithMetadata(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadInputStreamWithMetadataAuto() {
        testUploadInputStreamWithMetadata(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadInputStreamWithMetadataSync() {
        testUploadInputStreamWithMetadata(StoreType.SYNC)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testUploadInputStreamWithMetadata(type: StoreType) {
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadInputStreamWithMetadata(type, FileInputStream(file), testMetadata())
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        val deleteListener = removeFile(type, listener.fileMetaDataResult)
        assertNotNull(deleteListener.result)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun uploadInputStreamWithMetadata(storeType: StoreType, `is`: InputStream, metaData: FileMetaData): DefaultUploadProgressListener {
        val latch = CountDownLatch(1)
        val listener = DefaultUploadProgressListener(latch)
        val looperThread = LooperThread(Runnable {
            try {
                client?.getFileStore(storeType)?.upload(`is`, metaData, listener)
            } catch (e: IOException) {
                e.printStackTrace()
                listener.onFailure(e)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        return listener
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun tesUploadFileWithOutMetadataNetwork() {
        testUploadFileWithOutMetadata(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun tesUploadFileWithOutMetadataCache() {
        testUploadFileWithOutMetadata(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun tesUploadFileWithOutMetadataAuto() {
        testUploadFileWithOutMetadata(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun tesUploadFileWithOutMetadataSync() {
        testUploadFileWithOutMetadata(StoreType.SYNC)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testUploadFileWithOutMetadata(type: StoreType) {
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadFileWithOutMetadata(type, file)
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        val deleteListener = removeFile(type, listener.fileMetaDataResult)
        assertNotNull(deleteListener.result)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun uploadFileWithOutMetadata(storeType: StoreType, f: File): DefaultUploadProgressListener {
        val latch = CountDownLatch(1)
        val listener = DefaultUploadProgressListener(latch)
        val looperThread = LooperThread(Runnable {
            try {
                client?.getFileStore(storeType)?.upload(f, listener)
            } catch (e: IOException) {
                e.printStackTrace()
                listener.onFailure(e)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        return listener
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadInputStreamWithFileNameNetwork() {
        testUploadInputStreamWithFileName(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadInputStreamWithFileNameCache() {
        testUploadInputStreamWithFileName(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadInputStreamWithFileNameAuto() {
        testUploadInputStreamWithFileName(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadInputStreamWithFileNameSync() {
        testUploadInputStreamWithFileName(StoreType.SYNC)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testUploadInputStreamWithFileName(type: StoreType) {
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadInputStreamWithFilename(type, FileInputStream(file), TEST_FILENAME)
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        val deleteListener = removeFile(type, listener.fileMetaDataResult)
        assertNotNull(deleteListener.result)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun uploadInputStreamWithFilename(storeType: StoreType, `is`: InputStream, fileName: String): DefaultUploadProgressListener {
        val latch = CountDownLatch(1)
        val listener = DefaultUploadProgressListener(latch)
        val looperThread = LooperThread(Runnable {
            try {
                client?.getFileStore(storeType)?.upload(fileName, `is`, listener)
            } catch (e: IOException) {
                e.printStackTrace()
                listener.onFailure(e)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        return listener
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCancelFileUploadingNetwork() {
        testCancelFileUploading(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCancelFileUploadingCache() {
        testCancelFileUploading(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCancelFileUploadingAuto() {
        testCancelFileUploading(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCancelFileUploadingSync() {
        testCancelFileUploading(StoreType.SYNC)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testCancelFileUploading(storeType: StoreType) {
        val file = createFile(CUSTOM_FILE_SIZE_MB)
        val listener = cancelFileUploading(storeType, file)
        file.delete()
        assertTrue(listener.onCancelled)
        assertNull(listener.error)
        assertNull(listener.fileMetaDataResult)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCancelAndResumeFileUploadingNetwork() {
        testCancelAndResumeFileUploading(StoreType.NETWORK)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testCancelAndResumeFileUploading(storeType: StoreType) {
        val file = createFile(CUSTOM_FILE_SIZE_MB)
        val metadata = testMetadata()
        val listener = cancelAndResumeFileUploading(storeType, metadata, file)
        file.delete()
        assertNull(listener.error)
        assertNotNull(listener.fileMetaDataResult)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun cancelFileUploading(storeType: StoreType, f: File): DefaultUploadProgressListener {
        val latch = CountDownLatch(1)
        val listener = DefaultUploadProgressListener(latch)
        val looperThread = LooperThread(Runnable {
            try {
                client?.getFileStore(storeType)?.upload(f, listener)
                listener?.isCancelled = true
                client?.getFileStore(storeType)?.cancelUploading()
            } catch (e: IOException) {
                e.printStackTrace()
                listener.onFailure(e)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        return listener
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun cancelAndResumeFileUploading(storeType: StoreType, metadata: FileMetaData?, f: File?): DefaultUploadProgressListener {
        val latchCancelListener = CountDownLatch(1)
        val latchUploadListener = CountDownLatch(1)
        val cancelListener = ResumeUploadProgressListener(latchCancelListener)
        val uploadListener = DefaultUploadProgressListener(latchUploadListener)
        val looperThread = LooperThread(Runnable {
            try {
                client?.getFileStore(storeType)?.upload(f, metadata, cancelListener)
                var fileMetaDataWithUploadUrl = cancelListener.uploadMetadata
                if (fileMetaDataWithUploadUrl == null) {
                    fileMetaDataWithUploadUrl = metadata
                }
                client?.getFileStore(storeType)?.upload(f as File, fileMetaDataWithUploadUrl as FileMetaData, uploadListener)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        })
        looperThread.start()
        latchCancelListener.await()
        latchUploadListener.await()
        looperThread.mHandler.sendMessage(Message())
        return uploadListener
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCancelFileDownloadingNetwork() {
        testCancelFileDownloading(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCancelFileDownloadingCache() {
        testCancelFileDownloading(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCancelFileDownloadingAuto() {
        testCancelFileDownloading(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCancelFileDownloadingSync() {
        testCancelFileDownloading(StoreType.SYNC)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testCancelFileDownloading(storeType: StoreType) {
        val listener = uploadFileWithOutMetadata(storeType, createFile(15))
        assertNotNull(listener.fileMetaDataResult)
        val downloadListener = cancelFileDownloading(storeType, listener.fileMetaDataResult)
        assertTrue(downloadListener.onCancelled)
        assertNull(downloadListener.error)
        assertNull(downloadListener.fileMetaDataResult)
        val deleteListener = removeFile(storeType, listener.fileMetaDataResult)
        assertNotNull(deleteListener.result)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun cancelFileDownloading(storeType: StoreType, metaFile: FileMetaData?): DefaultDownloadProgressListener {
        val downloadLatch = CountDownLatch(1)
        val listener = DefaultDownloadProgressListener(downloadLatch)
        val looperThread = LooperThread(Runnable {
            try {
                val fos = FileOutputStream(createFile())
                client?.getFileStore(storeType)?.download(metaFile, fos, listener,
                        if (storeType === StoreType.CACHE) createOutputStream() else null,
                        if (storeType === StoreType.CACHE) createCachedClientCallback() else null)
                listener.isCancelled = true
                client?.getFileStore(storeType)?.cancelDownloading()
            } catch (e: IOException) {
                listener.onFailure(e)
            }
        })
        looperThread.start()
        downloadLatch.await()
        looperThread.mHandler.sendMessage(Message())
        return listener
    }

    @Throws(IOException::class)
    private fun createFile(mb: Int): File {
        val file = createFile()
        val f = RandomAccessFile(createFile(), "rw")
        f.setLength(mb * MB.toLong())
        return file
    }

    @Throws(IOException::class)
    private fun createSmallFile(): File {
        val file = createFile()
        val f = RandomAccessFile(createFile(), "rw")
        f.setLength(1)
        return file
    }

    @Throws(IOException::class)
    private fun createFile(fileName: String): File {
        val file = File(client?.context?.filesDir, fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    @Throws(IOException::class)
    private fun createFile(): File {
        val file = File(client?.context?.filesDir, TEST_FILENAME)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadPubliclyReadableFileNetwork() {
        testUploadPubliclyReadableFile(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadPubliclyReadableFileCache() {
        testUploadPubliclyReadableFile(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadPubliclyReadableFileAuto() {
        testUploadPubliclyReadableFile(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadPubliclyReadableFileSync() {
        testUploadPubliclyReadableFile(StoreType.SYNC)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testUploadPubliclyReadableFile(storeType: StoreType) {
        val fileMetaData = testMetadata()
        fileMetaData.isPublic = true
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadFileWithMetadata(storeType, file, fileMetaData)
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        val downloadListener = downloadFile(storeType, listener.fileMetaDataResult)
        assertNull(downloadListener.error)
        assertNotNull(downloadListener.fileMetaDataResult)
        assertTrue(downloadListener.fileMetaDataResult?.isPublic == true)
        removeFile(storeType, listener.fileMetaDataResult)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadPrivatelyReadableFileNetwork() {
        testUploadPrivatelyReadableFile(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadPrivatelyReadableFileCache() {
        testUploadPrivatelyReadableFile(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadPrivatelyReadableFileAuto() {
        testUploadPrivatelyReadableFile(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadPrivatelyReadableFileSync() {
        testUploadPrivatelyReadableFile(StoreType.SYNC)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testUploadPrivatelyReadableFile(storeType: StoreType) {
        val fileMetaData = testMetadata()
        fileMetaData.isPublic = false
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadFileWithMetadata(storeType, file, fileMetaData)
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        val downloadListener = downloadFile(storeType, listener.fileMetaDataResult)
        assertNull(downloadListener.error)
        assertNotNull(downloadListener.fileMetaDataResult)
        removeFile(storeType, listener.fileMetaDataResult)
        assertFalse(downloadListener.fileMetaDataResult?.isPublic == true)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadProgressChangingNetwork() {
        testUploadProgressChanging(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadProgressChangingCache() {
        testUploadProgressChanging(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadProgressChangingAuto() {
        testUploadProgressChanging(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadProgressChangingSync() {
        testUploadProgressChanging(StoreType.SYNC)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testUploadProgressChanging(storeType: StoreType) {
        val file = createFile(CUSTOM_FILE_SIZE_MB)
        val listener = uploadFileWithOutMetadata(storeType, file)
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        if (storeType === StoreType.SYNC) {
            assertTrue(listener.progressChangedCounter == 0)
        } else {
            assertTrue(listener.progressChangedCounter >= CUSTOM_FILE_SIZE_MB / UPLOAD_CHUNK_SIZE_MB)
        }
        val deleteListener = removeFile(storeType, listener.fileMetaDataResult)
        assertNotNull(deleteListener.result)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDownloadProgressChangingNetwork() {
        testDownloadProgressChanging(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDownloadProgressChangingCache() {
        testDownloadProgressChanging(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDownloadProgressChangingAuto() {
        testDownloadProgressChanging(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDownloadProgressChangingSync() {
        testDownloadProgressChanging(StoreType.SYNC)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testDownloadProgressChanging(storeType: StoreType) {
        val file = createFile(CUSTOM_FILE_SIZE_MB)
        val listener = uploadFileWithOutMetadata(storeType, file)
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        val downloadListener = downloadFile(storeType, listener.fileMetaDataResult)
        assertNotNull(downloadListener.fileMetaDataResult)
        if (storeType === StoreType.SYNC) {
            assertTrue(downloadListener.progressChangedCounter == 0)
        } else {
            assertTrue(downloadListener.progressChangedCounter >= CUSTOM_FILE_SIZE_MB / DOWNLOAD_CHUNK_SIZE_MB)
        }
        val deleteListener = removeFile(storeType, listener.fileMetaDataResult)
        assertNotNull(deleteListener.result)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testDownloadProgressChangingError() {
        val file = createSmallFile()
        val listener = uploadFileWithOutMetadata(StoreType.NETWORK, file)
        assertTrue(file.delete())
        assertNotNull(listener.fileMetaDataResult)
        val downloadLatch = CountDownLatch(1)
        val downloadProgressListener: DefaultDownloadProgressListener = spy(DefaultDownloadProgressListener(downloadLatch))
        doThrow(IOException()).`when`(downloadProgressListener).progressChanged(any(MediaHttpDownloader::class.java))
        val looperThread = LooperThread(Runnable {
            try {
                val fos = FileOutputStream(createFile())
                client?.getFileStore(StoreType.NETWORK)?.download(listener.fileMetaDataResult,
                        fos, downloadProgressListener)
            } catch (e: IOException) {
                downloadProgressListener.onFailure(e)
            }
        })
        looperThread.start()
        downloadLatch.await()
        looperThread.mHandler.sendMessage(Message())
        assertNotNull(downloadProgressListener.error)
        val deleteListener = removeFile(StoreType.NETWORK, listener.fileMetaDataResult)
        assertNotNull(deleteListener.result)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testUploadProgressChangingError() {
        val file = createSmallFile()
        val latch = CountDownLatch(1)
        val listener: DefaultUploadProgressListener = spy(DefaultUploadProgressListener(latch))
        doThrow(IOException()).`when`(listener).progressChanged(any(MediaHttpUploader::class.java))
        val looperThread = LooperThread(Runnable {
            try {
                client?.getFileStore(StoreType.NETWORK)?.upload(file, listener)
            } catch (e: IOException) {
                e.printStackTrace()
                listener.onFailure(e)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        assertTrue(file.delete())
        assertNotNull(listener.error)
    }

    @Throws(IOException::class, InterruptedException::class)
    fun testUploadGloballyReadableFile(storeType: StoreType) {
        val fileMetaData = testMetadata()
        fileMetaData.isPublic = true
        fileMetaData.acl = AccessControlList().setGloballyReadable(true)
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadFileWithMetadata(storeType, file, fileMetaData)
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        val downloadListener = downloadFile(storeType, listener.fileMetaDataResult)
        assertNull(downloadListener.error)
        assertNotNull(downloadListener.fileMetaDataResult)
        assertTrue(downloadListener.fileMetaDataResult?.isPublic == true)
        assertNotNull(downloadListener.fileMetaDataResult?.acl)
        assertTrue(downloadListener.fileMetaDataResult?.acl?.isGloballyReadable == true)
        removeFile(storeType, listener.fileMetaDataResult)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testUploadGloballyReadableFileNetwork() {
        testUploadGloballyReadableFile(StoreType.NETWORK)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testUploadGloballyReadableFileSync() {
        testUploadGloballyReadableFile(StoreType.SYNC)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testUploadGloballyReadableFileAuto() {
        testUploadGloballyReadableFile(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadFileCheckUploadUrlCache() {
        testUploadPubliclyReadableFile(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadFileCheckUploadUrlAuto() {
        testUploadFileCheckUploadUrl(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUploadFileCheckUploadUrlSync() {
        testUploadFileCheckUploadUrl(StoreType.SYNC)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testUploadFileCheckUploadUrl(storeType: StoreType) {
        val fileMetaData = testMetadata()
        fileMetaData.isPublic = true
        val file = createFile(DEFAULT_FILE_SIZE_MB)
        val listener = uploadFileWithMetadata(storeType, file, fileMetaData)
        file.delete()
        assertNotNull(listener.fileMetaDataResult)
        assertNull(listener.fileMetaDataResult?.uploadUrl)
        val downloadListener = downloadFile(storeType, listener.fileMetaDataResult)
        assertNull(downloadListener.error)
        assertNotNull(downloadListener.fileMetaDataResult)
        assertTrue(downloadListener.fileMetaDataResult?.isPublic == true)
        assertNull(listener.fileMetaDataResult?.uploadUrl)
        removeFile(storeType, listener.fileMetaDataResult)
    }

    @After
    fun tearDown() {
        client?.performLockDown()
        if (Client.kinveyHandlerThread != null) {
            try {
                client?.stopKinveyHandlerThread()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    companion object {
        private const val ID = "_id"
        private const val TEST_FILENAME = "test.xml"
        private const val MB = 1024 * 1024
        private const val DEFAULT_FILE_SIZE_MB = 1
        private const val CUSTOM_FILE_SIZE_MB = 5
        private const val UPLOAD_CHUNK_SIZE_MB = 4
        private const val DOWNLOAD_CHUNK_SIZE_MB = 2
    }
}