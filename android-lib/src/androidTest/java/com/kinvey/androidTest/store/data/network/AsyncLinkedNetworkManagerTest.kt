package com.kinvey.androidTest.store.data.network

import android.content.Context
import android.os.Message
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.AndroidMimeTypeFinder
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.callback.KinveyDeleteCallback
import com.kinvey.android.callback.KinveyListCallback
import com.kinvey.android.model.User
import com.kinvey.android.network.AsyncLinkedNetworkManager
import com.kinvey.android.store.UserStore.Companion.login
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.TestManager.Companion.PASSWORD
import com.kinvey.androidTest.TestManager.Companion.USERNAME
import com.kinvey.androidTest.store.datastore.BaseDataStoreTest
import com.kinvey.java.Query
import com.kinvey.java.core.*
import com.kinvey.java.linkedResources.LinkedFile
import com.kinvey.java.linkedResources.LinkedGenericJson
import com.kinvey.java.model.FileMetaData
import com.kinvey.java.store.StoreType
import junit.framework.Assert.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@SmallTest
class AsyncLinkedNetworkManagerTest {

    private var client: Client<User>? = null

    @Before
    @Throws(InterruptedException::class)
    fun setup() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        val latch = CountDownLatch(1)
        var looperThread: LooperThread? = null
        if (client?.isUserLoggedIn == false) {
            looperThread = LooperThread(Runnable {
                try {
                    login(USERNAME, PASSWORD, client as Client<User>, object : KinveyClientCallback<User> {
                        override fun onSuccess(result: User?) {
                            Assert.assertNotNull(result)
                            latch.countDown()
                        }

                        override fun onFailure(error: Throwable?) {
                            Assert.assertNull(error)
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

    private class DefaultKinveyClientCallback<T>(private val latch: CountDownLatch) : KinveyClientCallback<T> {
        var result: T? = null
        var error: Throwable? = null
        override fun onSuccess(result: T?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        internal fun finish() {
            latch.countDown()
        }

    }

    private class DefaultKinveyListCallback<T>(private val latch: CountDownLatch) : KinveyListCallback<T> {
        var result: List<T>? = null
        var error: Throwable? = null

        override fun onSuccess(result: List<T>?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        internal fun finish() {
            latch.countDown()
        }
    }

    @Test
    fun testGetConstructor() {
        val latch = CountDownLatch(1)
        var get: AsyncLinkedNetworkManager.Get<LinkedPerson>? = null

        val linkedNetworkManager = AsyncLinkedNetworkManager(LinkedPerson.COLLECTION, LinkedPerson::class.java, client)
        val query = Query()
        val attachments = arrayOf("path1", "path2")
        val resolves = arrayOf<String?>("res1", "res2")
        val resolveDepth = 1
        val retain = true
        val storeType = StoreType.AUTO

        val looperThread = LooperThread(Runnable {
            get = AsyncLinkedNetworkManager.Get(linkedNetworkManager, query, null, null,
                    attachments, resolves, resolveDepth, retain, storeType)
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(linkedNetworkManager, get?.networkManager)
        assertEquals(query, get?.query)
        assertEquals(attachments, get?.attachments)
        assertEquals(resolves, get?.resolves)
        assertEquals(resolveDepth, get?.resolve_depth)
        assertEquals(retain, get?.retain)
        assertEquals(storeType, get?.storeType)
    }

    @Test
    fun testGetEntityConstructor() {
        val latch = CountDownLatch(1)
        var getEntity: AsyncLinkedNetworkManager.GetEntity<LinkedPerson>? = null

        val linkedNetworkManager = AsyncLinkedNetworkManager(LinkedPerson.COLLECTION, LinkedPerson::class.java, client)
        val entityId = "entityId1"
        val attachments = arrayOf("path1", "path2")
        val storeType = StoreType.AUTO

        val looperThread = LooperThread(Runnable {
            getEntity = AsyncLinkedNetworkManager.GetEntity(linkedNetworkManager, entityId, null, null, attachments, storeType)
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(linkedNetworkManager, getEntity?.networkManager)
        assertEquals(entityId, getEntity?.entityID)
        assertEquals(attachments, getEntity?.attachments)
        assertEquals(storeType, getEntity?.storeType)
    }

    @Test
    fun testSaveConstructor() {
        val latch = CountDownLatch(1)
        var save: AsyncLinkedNetworkManager.Save<LinkedPerson>? = null

        val linkedNetworkManager = AsyncLinkedNetworkManager(LinkedPerson.COLLECTION, LinkedPerson::class.java, client)
        val person = LinkedPerson()
        val attachments = arrayOf<String?>("path1", "path2")
        val storeType = StoreType.AUTO

        val looperThread = LooperThread(Runnable {
            save = AsyncLinkedNetworkManager.Save(linkedNetworkManager, person, null, null, attachments, storeType)
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(linkedNetworkManager, save?.networkManager)
        assertEquals(person, save?.entity)
        assertEquals(attachments, save?.attachments)
        assertEquals(storeType, save?.storeType)
    }

    @Test
    fun testConstructor() {
        val linkedNetworkManager = AsyncLinkedNetworkManager(LinkedPerson.COLLECTION, LinkedPerson::class.java, client)
        assertNotNull(linkedNetworkManager)
    }

    @Test
    fun testMimeTypeFinder() {
        val androidMimeTypeFinder: AndroidMimeTypeFinder = mock(AndroidMimeTypeFinder::class.java)
        androidMimeTypeFinder.getMimeType(FileMetaData(), object : InputStream() {
            @Throws(IOException::class)
            override fun read(): Int {
                return -1
            }
        })
        verify(androidMimeTypeFinder, times(1)).getMimeType(any<Any>() as FileMetaData?, any<Any>() as InputStream?)
        verify(androidMimeTypeFinder, never()).getMimeType(any<Any>() as FileMetaData?)
    }

    @Test
    fun testMimeTypeFinderInputStream() {
        val androidMimeTypeFinder = AndroidMimeTypeFinder()
        val fileMetaData = FileMetaData()
        androidMimeTypeFinder.getMimeType(fileMetaData, object : InputStream() {
            @Throws(IOException::class)
            override fun read(): Int {
                return -1
            }
        })
        assertEquals(fileMetaData.mimetype, "application/octet-stream")
    }

    @Test
    fun testMimeTypeFinderNullFile() {
        val androidMimeTypeFinder = AndroidMimeTypeFinder()
        val fileMetaData = FileMetaData()
        androidMimeTypeFinder.getMimeType(fileMetaData, null as File?)
        assertEquals(fileMetaData.mimetype, "application/octet-stream")
    }

    @Test
    fun testMimeTypeFinderNullMimetype() {
        val androidMimeTypeFinder = AndroidMimeTypeFinder()
        val fileMetaData = FileMetaData()
        androidMimeTypeFinder.getMimeType(fileMetaData)
        assertEquals(fileMetaData.mimetype, "application/octet-stream")
    }

    private class DefaultKinveyDeleteCallback internal constructor(private val latch: CountDownLatch) : KinveyDeleteCallback {
        internal var result: Int? = null
        internal var error: Throwable? = null
        override fun onSuccess(result: Int?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        internal fun finish() {
            latch.countDown()
        }

    }

    @Throws(InterruptedException::class)
    private fun <T : LinkedGenericJson> clearBackend(netMan: AsyncLinkedNetworkManager<T>) {
        var query = client?.query()
        query = query?.notEqual("username", "100500")
        val result = delete(netMan, query)
        //assertTrue(result > 0);

    }

    @Throws(InterruptedException::class)
    private fun <T : LinkedGenericJson> delete(netMan: AsyncLinkedNetworkManager<T>, query: Query?): Int {
        var result = 0
        try {
            val response = netMan.deleteBlocking(query)!!.execute()
            assertNotNull(response)
            result = response!!.count
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    @Throws(InterruptedException::class)
    private fun <T : LinkedGenericJson> getEntity(entityId: String?,
                                                   netMan: AsyncLinkedNetworkManager<T>, storeType: StoreType): DefaultKinveyClientCallback<*> {
        val latch = CountDownLatch(1)
        val callback: DefaultKinveyClientCallback<T> = DefaultKinveyClientCallback<T>(latch)
        val looperThread = LooperThread(Runnable {
            netMan.getEntity(entityId, callback, object : DownloaderProgressListener {
                @Throws(IOException::class)
                override fun progressChanged(downloader: MediaHttpDownloader?) {
                }
            }, storeType)
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun <T : LinkedGenericJson> get(query: Query?,
                                                  netMan: AsyncLinkedNetworkManager<T>, storeType: StoreType): DefaultKinveyListCallback<*> {
        val latch = CountDownLatch(1)
        val callback: DefaultKinveyListCallback<T> = DefaultKinveyListCallback<T>(latch)
        val looperThread = LooperThread(Runnable {
            netMan.get(query, callback, object : DownloaderProgressListener {
                @Throws(IOException::class)
                override fun progressChanged(downloader: MediaHttpDownloader?) {
                }
            }, storeType)
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun <T : LinkedGenericJson> getMoreParam(query: Query?,
                                            netMan: AsyncLinkedNetworkManager<T>, resolves: Array<String?>?,
                                                     resolve_depth: Int, retain: Boolean, storeType: StoreType): DefaultKinveyListCallback<*> {
        val latch = CountDownLatch(1)
        val callback: DefaultKinveyListCallback<T> = DefaultKinveyListCallback<T>(latch)
        val looperThread = LooperThread(Runnable {
            netMan.get(query, callback, object : DownloaderProgressListener {
                @Throws(IOException::class)
                override fun progressChanged(downloader: MediaHttpDownloader?) {
                }
            }, resolves, resolve_depth, retain, storeType)
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun <T : LinkedGenericJson> getAll(netMan: AsyncLinkedNetworkManager<T>,
                                               storeType: StoreType): DefaultKinveyListCallback<*> {
        val latch = CountDownLatch(1)
        val callback: DefaultKinveyListCallback<T> = DefaultKinveyListCallback<T>(latch)
        val looperThread = LooperThread(Runnable {
            netMan.get(callback, object : DownloaderProgressListener {
                @Throws(IOException::class)
                override fun progressChanged(downloader: MediaHttpDownloader?) {
                }
            }, storeType)
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun <T : LinkedGenericJson> save(entity: T, netMan: AsyncLinkedNetworkManager<T>, storeType: StoreType): DefaultKinveyClientCallback<*> {
        val latch = CountDownLatch(1)
        val callback: DefaultKinveyClientCallback<T> = DefaultKinveyClientCallback<T>(latch)
        val looperThread = LooperThread(Runnable {
            netMan.save(entity, callback, object : UploaderProgressListener {
                @Throws(IOException::class)
                override fun progressChanged(uploader: MediaHttpUploader?) {
                }
            }, storeType)
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveEntity() {
        val linkedNetworkManager = AsyncLinkedNetworkManager(LinkedPerson.COLLECTION, LinkedPerson::class.java, client)
        assertNotNull(linkedNetworkManager)
        clearBackend(linkedNetworkManager)
        val entityId = "testId"
        val person = LinkedPerson()
        person.id = entityId
        val file = LinkedFile()
        file.id = "ba14f983-9391-43b7-b7bij6-337b7e41cc37"
        file.fileName = "test.xml"
        file.input = ByteArrayInputStream("123456789".toByteArray())
        person.putFile("attachment", file)
        val saveCallback = save(person, linkedNetworkManager, StoreType.NETWORK)
        assertNotNull(saveCallback)
        assertNull(saveCallback.error)
        val result = saveCallback.result as LinkedPerson
        assertNotNull(result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testGetEntity() {
        val linkedNetworkManager = AsyncLinkedNetworkManager(LinkedPerson.COLLECTION, LinkedPerson::class.java, client)
        assertNotNull(linkedNetworkManager)
        clearBackend(linkedNetworkManager)
        val entityId = "testId"
        val person = LinkedPerson()
        person.id = entityId
        val saveCallback = save(person, linkedNetworkManager, StoreType.NETWORK)
        assertNotNull(saveCallback)
        assertNull(saveCallback.error)
        val result = saveCallback.result as LinkedPerson
        assertNotNull(result)
        val getCallback = getEntity(result.id, linkedNetworkManager, StoreType.NETWORK)
        assertNotNull(getCallback)
        assertNull(getCallback.error)
        assertNotNull(getCallback.result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testGet() {
        val linkedNetworkManager = AsyncLinkedNetworkManager(LinkedPerson.COLLECTION, LinkedPerson::class.java, client)
        assertNotNull(linkedNetworkManager)
        clearBackend(linkedNetworkManager)
        val entityId = "testId"
        val userName = "user"
        val person = LinkedPerson()
        person.id = entityId
        person.username = userName
        val saveCallback = save(person, linkedNetworkManager, StoreType.NETWORK)
        assertNotNull(saveCallback)
        assertNull(saveCallback.error)
        val result = saveCallback.result as LinkedPerson
        assertNotNull(result)
        val entitySecId = "testSecId"
        val personSec = LinkedPerson()
        personSec.id = entitySecId
        personSec.username = userName
        val saveCallbackSec = save(personSec, linkedNetworkManager, StoreType.NETWORK)
        assertNotNull(saveCallbackSec)
        assertNull(saveCallbackSec.error)
        val resultSec = saveCallbackSec.result as LinkedPerson
        assertNotNull(resultSec)
        var query = client?.query()
        query = query?.equals("username", userName)
        val getCallback = get(query, linkedNetworkManager, StoreType.NETWORK)
        assertNotNull(getCallback)
        assertNull(getCallback.error)
        assertNotNull(getCallback.result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testGetAll() {
        val linkedNetworkManager = AsyncLinkedNetworkManager(LinkedPerson.COLLECTION, LinkedPerson::class.java, client)
        assertNotNull(linkedNetworkManager)
        clearBackend(linkedNetworkManager)
        val entityId = "testId"
        val userName = "user"
        val person = LinkedPerson()
        person.id = entityId
        person.username = userName
        val saveCallback = save(person, linkedNetworkManager, StoreType.NETWORK)
        assertNotNull(saveCallback)
        assertNull(saveCallback.error)
        val result = saveCallback.result as LinkedPerson
        assertNotNull(result)
        val entitySecId = "testSecId"
        val personSec = LinkedPerson()
        personSec.id = entitySecId
        val saveCallbackSec = save(personSec, linkedNetworkManager, StoreType.NETWORK)
        assertNotNull(saveCallbackSec)
        assertNull(saveCallbackSec.error)
        val resultSec = saveCallbackSec.result as LinkedPerson
        assertNotNull(resultSec)
        var query = client?.query()
        query = query?.equals("username", userName)
        val getCallbackMoreParam = getMoreParam(query, linkedNetworkManager, null,
                0, false, StoreType.NETWORK)
        assertNotNull(getCallbackMoreParam)
        assertNull(getCallbackMoreParam.error)
        assertNotNull(getCallbackMoreParam.result)
        assertEquals(getCallbackMoreParam.result?.size, 1)
        val getCallbackAll = getAll(linkedNetworkManager, StoreType.NETWORK)
        assertNotNull(getCallbackAll)
        assertNull(getCallbackAll.error)
        assertNotNull(getCallbackAll.result)
        assertEquals(getCallbackAll.result?.size, 2)
    }
}