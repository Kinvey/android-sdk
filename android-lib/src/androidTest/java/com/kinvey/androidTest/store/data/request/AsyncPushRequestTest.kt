package com.kinvey.androidTest.store.data.request

import android.content.Context
import android.os.Message
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.google.api.client.json.GenericJson
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.async.AsyncPushRequest
import com.kinvey.android.model.User
import com.kinvey.android.sync.KinveyPushCallback
import com.kinvey.android.sync.KinveyPushResponse
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.model.Person
import com.kinvey.java.KinveyException
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.StoreType
import com.kinvey.java.sync.SyncManager
import com.kinvey.java.sync.dto.SyncRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import java.io.IOException
import java.security.AccessControlException
import java.util.*
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@SmallTest
class AsyncPushRequestTest {
    private var client: Client<*>? = null
    @Before
    fun setUp() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
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

    @Test
    @Throws(InterruptedException::class)
    fun testPushSyncRequestOnProgress() {
        val latch = CountDownLatch(1)
        val mockPushCallback: KinveyPushCallback = spy(KinveyPushCallbackAdapter(latch))
        val looperThread = LooperThread(Runnable {
            val spyNetworkManager: NetworkManager<Person> = spy(NetworkManager(Person.TEST_COLLECTION, Person::class.java, client))
            val syncManager: SyncManager = mock(SyncManager::class.java)
            try {
                `when`(syncManager.executeRequest(any(Client::class.java), any(SyncRequest::class.java))).thenReturn(GenericJson())
            } catch (e: IOException) {
                e.printStackTrace()
                assertTrue(false)
            }
            val syncRequests: MutableList<SyncRequest> = ArrayList()
            syncRequests.add(SyncRequest())
            `when`(syncManager.popSingleQueue(any(String::class.java))).thenReturn(syncRequests)
            AsyncPushRequest(Person.COLLECTION, syncManager, client, StoreType.SYNC, spyNetworkManager, Person::class.java, mockPushCallback).execute()
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        verify(mockPushCallback, times(1)).onProgress(any(Long::class.javaPrimitiveType)!!, any(Long::class.javaPrimitiveType)!!)
    }

    protected open class KinveyPushCallbackAdapter(val latch: CountDownLatch) : KinveyPushCallback {
        override fun onSuccess(result: KinveyPushResponse?) {
            latch.countDown()
        }

        override fun onFailure(error: Throwable?) {
            latch.countDown()
        }

        override fun onProgress(current: Long, all: Long) {}

    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushSyncRequestOnFailure() {
        val latch = CountDownLatch(1)
        val mockPushCallback: KinveyPushCallback = spy(KinveyPushCallbackAdapter(latch))
        val looperThread = LooperThread(Runnable {
            val spyNetworkManager: NetworkManager<Person> = spy(NetworkManager(Person.TEST_COLLECTION, Person::class.java, client))
            val syncManager: SyncManager = mock(SyncManager::class.java)
            val syncRequests: MutableList<SyncRequest> = ArrayList()
            syncRequests.add(SyncRequest())
            `when`(syncManager.popSingleQueue(any(String::class.java))).thenReturn(syncRequests)
            try {
                doThrow(IOException()).`when`(syncManager).executeRequest(any(Client::class.java), any(SyncRequest::class.java))
            } catch (e: IOException) {
                e.printStackTrace()
                assertTrue(false)
            }
            AsyncPushRequest(Person.COLLECTION, syncManager, client, StoreType.SYNC, spyNetworkManager, Person::class.java, mockPushCallback).execute()
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        verify(mockPushCallback, times(1)).onFailure(any(Throwable::class.java))
    }

    protected open class DefaultKinveyPushCallback(val latch: CountDownLatch) : KinveyPushCallback {
        var result: KinveyPushResponse? = null
        var error: Throwable? = null
        override fun onSuccess(result: KinveyPushResponse?) {
            this.result = result
            finish()
        }
        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }
        override fun onProgress(current: Long, all: Long) {}
        fun finish() {
            latch.countDown()
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushSyncRequestKinveyException() {
        val errorMessage = "TestException"
        val latch = CountDownLatch(1)
        val callback: DefaultKinveyPushCallback = spy(DefaultKinveyPushCallback(latch))
        val looperThread = LooperThread(Runnable {
            val spyNetworkManager: NetworkManager<Person> = spy(NetworkManager(Person.TEST_COLLECTION, Person::class.java, client))
            val syncManager: SyncManager = mock(SyncManager::class.java)
            val syncRequests: MutableList<SyncRequest> = ArrayList()
            syncRequests.add(SyncRequest())
            `when`(syncManager.popSingleQueue(any(String::class.java))).thenReturn(syncRequests)
            try {
                doThrow(KinveyException(errorMessage)).`when`(syncManager).executeRequest(any(Client::class.java), any(SyncRequest::class.java))
            } catch (e: IOException) {
                e.printStackTrace()
                assertTrue(false)
            }
            AsyncPushRequest(Person.COLLECTION, syncManager, client, StoreType.SYNC, spyNetworkManager, Person::class.java, callback).execute()
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        verify(callback, times(0)).onFailure(any(Throwable::class.java))
        verify(callback, times(1)).onProgress(any(Long::class.javaPrimitiveType), any(Long::class.javaPrimitiveType))
        verify(callback, times(1)).onSuccess(any(KinveyPushResponse::class.java))
        assertEquals(errorMessage, (callback.result?.listOfExceptions?.get(0) as KinveyException).reason)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushSyncRequestAccessControlException() {
        val errorMessage = "TestException"
        val latch = CountDownLatch(1)
        val callback: DefaultKinveyPushCallback = spy(DefaultKinveyPushCallback(latch))
        val looperThread = LooperThread(Runnable {
            val spyNetworkManager: NetworkManager<Person> = spy(NetworkManager(Person.TEST_COLLECTION, Person::class.java, client))
            val syncManager: SyncManager = mock(SyncManager::class.java)
            val syncRequests: MutableList<SyncRequest> = ArrayList()
            syncRequests.add(SyncRequest())
            `when`(syncManager.popSingleQueue(any(String::class.java))).thenReturn(syncRequests)
            try {
                doThrow(AccessControlException(errorMessage)).`when`(syncManager).executeRequest(any(Client::class.java), any(SyncRequest::class.java))
            } catch (e: IOException) {
                e.printStackTrace()
                assertTrue(false)
            }
            AsyncPushRequest(Person.COLLECTION, syncManager, client, StoreType.SYNC, spyNetworkManager, Person::class.java, callback).execute()
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        verify(callback, times(0)).onFailure(any(Throwable::class.java))
        verify(callback, times(1)).onProgress(any(Long::class.javaPrimitiveType), any(Long::class.javaPrimitiveType))
        verify(callback, times(1)).onSuccess(any(KinveyPushResponse::class.java))
        assertEquals(errorMessage, callback.result?.listOfExceptions?.get(0)?.message)
    }
}