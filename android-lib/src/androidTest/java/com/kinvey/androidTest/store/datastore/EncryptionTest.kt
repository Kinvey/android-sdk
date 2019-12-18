package com.kinvey.androidTest.store.datastore

import android.content.Context
import android.os.Message
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.callback.AsyncDownloaderProgressListener
import com.kinvey.android.callback.AsyncUploaderProgressListener
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore
import com.kinvey.android.store.DataStore.Companion.collection
import com.kinvey.android.store.FileStore
import com.kinvey.android.store.UserStore.Companion.login
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.model.Person
import com.kinvey.java.AbstractClient
import com.kinvey.java.KinveyException
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.core.MediaHttpDownloader
import com.kinvey.java.core.MediaHttpUploader
import com.kinvey.java.model.FileMetaData
import com.kinvey.java.store.StoreType
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.SecureRandom
import java.util.concurrent.CountDownLatch

/**
 * Created by yuliya on 08/25/17.
 */

@RunWith(AndroidJUnit4::class)
@SmallTest
class EncryptionTest {

    private var client: Client<*>? = null
    private var mContext: Context? = null

    private class UserKinveyClientCallback(private val latch: CountDownLatch) : KinveyClientCallback<User> {
        var result: User? = null
        var error: Throwable? = null
        override fun onSuccess(user: User?) {
            result = user
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

    private class PersonKinveyClientCallback(private val latch: CountDownLatch) : KinveyClientCallback<Person?> {
        var result: Person? = null
        var error: Throwable? = null
        override fun onSuccess(result: Person?) {
            this.result = result
            finish()
        }
        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }
        fun finish() {
            latch.countDown()
        }
    }

    private class DefaultUploadProgressListener(private val latch: CountDownLatch) : AsyncUploaderProgressListener<FileMetaData> {
        var fileMetaDataResult: FileMetaData? = null
        var error: Throwable? = null
        override val isCancelled = false
        private var onCancelled = false
        private var progressChangedCounter = 0
        @Throws(IOException::class)
        override fun progressChanged(uploader: MediaHttpUploader?) {
            progressChangedCounter++
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

    private class DefaultDownloadProgressListener(private val latch: CountDownLatch) : AsyncDownloaderProgressListener<FileMetaData> {
        var fileMetaDataResult: FileMetaData? = null
        var error: Throwable? = null
        override val isCancelled = false
        private var onCancelled = false
        private var progressChangedCounter = 0
        @Throws(IOException::class)
        override fun progressChanged(downloader: MediaHttpDownloader) {
            progressChangedCounter++
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

    @Before
    @Throws(InterruptedException::class, IOException::class)
    fun setUp() {
        mContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    @Throws(IOException::class)
    fun testSetEncryptionKey() {
        val key = ByteArray(64)
        SecureRandom().nextBytes(key)
        client = Builder<User>(mContext).setEncryptionKey(key).build()
        assertNotNull(client)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDataStoreEncryption() {
        val key = ByteArray(64)
        SecureRandom().nextBytes(key)
        client = Builder<User>(mContext).setEncryptionKey(key).build()
        val callback = login(USERNAME, PASSWORD)
        assertNull(callback.error)
        assertNotNull(callback.result)
        val encryptedStore = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person = createPerson(USERNAME)

        val saveCallback = save(encryptedStore, person)
        Assert.assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)

        val secondClient = Builder<User>(mContext).setEncryptionKey(key).build()
        val notEncryptedStore = collection(COLLECTION, Person::class.java, StoreType.SYNC, secondClient)
        val findCallback = find(notEncryptedStore, saveCallback.result?.id)
        Assert.assertNotNull(findCallback.result)
        assertNull(findCallback.error)
        client?.performLockDown()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDataStoreEncryptionFail() {
        val key = ByteArray(64)
        SecureRandom().nextBytes(key)
        client = Builder<User>(mContext).setEncryptionKey(key).build()
        val callback = login(USERNAME, PASSWORD)
        assertNull(callback.error)
        assertNotNull(callback.result)

        val encryptedStore = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person = createPerson(USERNAME)
        val saveCallback = save(encryptedStore, person)
        Assert.assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val clientWithoutEncryption = Builder<User>(mContext).build()
        var fileException: KinveyException? = null
        try {
            collection(COLLECTION, Person::class.java, StoreType.SYNC, clientWithoutEncryption)
        } catch (exception: KinveyException) {
            fileException = exception
        }
        assertNotNull(fileException)
        assertEquals(fileException?.reason, ACCESS_ERROR)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDataStoresWithAndWithoutEncryptionInOneClient() {
        val key = ByteArray(64)
        SecureRandom().nextBytes(key)
        client = Builder<User>(mContext).setEncryptionKey(key).build()
        val callback = login(USERNAME, PASSWORD)
        assertNull(callback.error)
        assertNotNull(callback.result)
        val encryptedStore = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person = createPerson(USERNAME)
        val saveCallback = save(encryptedStore, person)
        Assert.assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val clientWithoutEncryption = Builder<User>(mContext).build()
        var kinveyException: KinveyException? = null
        var otherStore: DataStore<Person>? = null
        try {
            otherStore = collection(COLLECTION + "_OTHER", Person::class.java, StoreType.SYNC, clientWithoutEncryption)
        } catch (exception: KinveyException) {
            kinveyException = exception
        }
        assertNull(otherStore)
        assertNotNull(kinveyException)
        assertEquals(kinveyException?.reason, ACCESS_ERROR)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDataStoresDecryption() {
        val key = ByteArray(64)
        SecureRandom().nextBytes(key)
        client = Builder<User>(mContext).setEncryptionKey(key).build()
        val callback = login(USERNAME, PASSWORD)
        assertNull(callback.error)
        assertNotNull(callback.result)
        val encryptedStore = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person = createPerson(USERNAME)
        val saveCallback = save(encryptedStore, person)
        Assert.assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val secondClient = Builder<User>(mContext).setEncryptionKey(key).build()
        val otherStore = collection(COLLECTION + "_OTHER", Person::class.java, StoreType.SYNC, secondClient)
        assertNotNull(otherStore)
    }

    @Test
    fun testFileStoreCreatingWithEncryption() {
        val key = ByteArray(64)
        SecureRandom().nextBytes(key)
        client = Builder<User>(mContext).setEncryptionKey(key).build()
        val fileStore = client?.getFileStore(StoreType.SYNC)
        assertNotNull(fileStore)
    }

    @Test
    fun testFileStoresWithAndWithoutEncryptionInOneClient() {
        val key = ByteArray(64)
        SecureRandom().nextBytes(key)
        client = Builder<User>(mContext).setEncryptionKey(key).build()
        val fileStore = client?.getFileStore(StoreType.SYNC)
        assertNotNull(fileStore)
        val clientWithoutEncryption = Builder<User>(mContext).build()
        var kinveyException: KinveyException? = null
        try {
            clientWithoutEncryption.getFileStore(StoreType.SYNC)
        } catch (exception: KinveyException) {
            kinveyException = exception
        }
        assertNotNull(kinveyException)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFileStoresDecryption() {
        val key = ByteArray(64)
        SecureRandom().nextBytes(key)

        client = Builder<User>(mContext).setEncryptionKey(key).build()
        val fileStore = client?.getFileStore(StoreType.SYNC)
        assertNotNull(fileStore)

        val secondClient = Builder<User>(mContext).setEncryptionKey(key).build()
        val secondFileStore = secondClient.getFileStore(StoreType.SYNC)
        assertNotNull(secondFileStore)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFileStoreEncryptionUploading() {
        val key = ByteArray(64)
        SecureRandom().nextBytes(key)

        client = Builder<User>(mContext).setEncryptionKey(key).build()
        val callback = login(USERNAME, PASSWORD)
        assertNull(callback.error)
        assertNotNull(callback.result)

        val fileStore = client?.getFileStore(StoreType.SYNC) as FileStore
        val file = createFile()
        val listener = uploadFileWithMetadata(fileStore, file, testMetadata())
        assertNotNull(listener.fileMetaDataResult)
        assertNull(listener.error)
        file.delete()
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFileStoreEncryptionUploadingDownloading() {
        val key = ByteArray(64)
        SecureRandom().nextBytes(key)
        client = Builder<User>(mContext).setEncryptionKey(key).build()
        val callback = login(USERNAME, PASSWORD)
        assertNull(callback.error)
        assertNotNull(callback.result)

        val fileStore = client?.getFileStore(StoreType.SYNC) as FileStore
        val file = createFile()
        val listener = uploadFileWithMetadata(fileStore, file, testMetadata())
        assertNotNull(listener.fileMetaDataResult)
        assertNull(listener.error)
        file.delete()

        val secondClient = Builder<User>(mContext).setEncryptionKey(key).build()
        val secondFileStore = secondClient.getFileStore(StoreType.SYNC)
        assertNotNull(secondFileStore)

        val downloadListener = downloadFile(secondFileStore, listener.fileMetaDataResult)
        assertNotNull(downloadListener.fileMetaDataResult)
        assertNull(downloadListener.error)
    }

    @Throws(IOException::class)
    private fun createFile(): File {
        val file = File(client?.context?.filesDir, TEST_FILENAME)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    private fun testMetadata(): FileMetaData {
        val fileMetaData = FileMetaData()
        fileMetaData.fileName = TEST_FILENAME
        return fileMetaData
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun downloadFile(fileStore: FileStore, metaFile: FileMetaData?): DefaultDownloadProgressListener {
        val downloadLatch = CountDownLatch(1)
        val listener = DefaultDownloadProgressListener(downloadLatch)
        val looperThread = LooperThread(Runnable {
            try {
                val fos = FileOutputStream(createFile())
                fileStore.download(metaFile!!, fos, listener)
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
    private fun uploadFileWithMetadata(fileStore: FileStore, f: File, metaData: FileMetaData): DefaultUploadProgressListener {
        val latch = CountDownLatch(1)
        val listener = DefaultUploadProgressListener(latch)
        val looperThread = LooperThread(Runnable {
            try {
                fileStore.upload(f, metaData, listener)
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

    @Throws(InterruptedException::class)
    private fun login(userName: String, password: String): UserKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = UserKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable {
            if (client?.isUserLoggedIn == false) {
                try {
                    login<User>(userName, password, client as AbstractClient<User>, callback)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                callback.onSuccess(client?.activeUser as User)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun save(store: DataStore<Person>, person: Person): PersonKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = PersonKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable { store.save(person, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun find(store: DataStore<Person>, id: String?): PersonKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = PersonKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable { store.find(id ?: "", callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        return callback
    }

    private fun createPerson(name: String): Person {
        val person = Person()
        person.username = name
        return person
    }

    @After
    fun tearDown() {
        if (Client.kinveyHandlerThread != null) {
            client?.performLockDown()
            try {
                client?.stopKinveyHandlerThread()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    companion object {
        private const val COLLECTION = "PersonsNew"
        private const val USERNAME = "test"
        private const val PASSWORD = "test"
        private const val ACCESS_ERROR = "Access Error"
        private const val TEST_FILENAME = "test.xml"
    }
}