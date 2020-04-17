package com.kinvey.androidTest.store.data.request

import android.content.Context
import android.content.SyncRequest
import android.os.Message
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.api.client.json.GenericJson
import com.kinvey.android.Client
import com.kinvey.android.async.AsyncBatchPushRequest
import com.kinvey.android.model.User
import com.kinvey.android.sync.KinveyPushBatchResponse
import com.kinvey.android.sync.KinveyPushCallback
import com.kinvey.android.sync.KinveyPushResponse
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.model.Person
import com.kinvey.java.model.KinveyBatchInsertError
import com.kinvey.java.model.KinveySyncSaveBatchResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.StoreType
import com.kinvey.java.sync.SyncManager
import com.kinvey.java.sync.dto.SyncItem
import io.mockk.every
import io.mockk.spyk
import io.mockk.verifySequence
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@SmallTest
class AsyncBatchPushRequestTest {

    private var client: Client<*>? = null
    private lateinit var spyNetworkManager: NetworkManager<Person>
    private var syncManager: SyncManager? = null

    private val kinveyPushCallback = object: KinveyPushCallback {
        override fun onSuccess(result: KinveyPushResponse?) {}
        override fun onFailure(error: Throwable?) {}
        override fun onProgress(current: Long, all: Long) {}
    }

    @Before
    fun setUp() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Client.Builder<User>(mMockContext).build()
        spyNetworkManager = spyk(NetworkManager(Person.TEST_COLLECTION, Person::class.java, client))
        syncManager = spyk(SyncManager(client?.cacheManager))
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

    private fun getAsyncBatchPushRequest(): AsyncBatchPushRequest<Person> {
        return spyk(AsyncBatchPushRequest<Person>(Person.TEST_COLLECTION,
            syncManager,
            client,
            StoreType.SYNC,
            spyNetworkManager,
            Person::class.java,
            kinveyPushCallback),
            recordPrivateCalls = true
        )
    }

    @Test
    fun testAsyncBatchPushRequestSequenceExecutionNoSyncItems() {

        assertNotNull(spyNetworkManager)

        val latch = CountDownLatch(1)

        val looperThread = LooperThread(Runnable {

            val asyncBatchPushRequest = getAsyncBatchPushRequest()

            every { asyncBatchPushRequest["processQueuedSyncRequests"](any<List<SyncRequest>>(), any<KinveyPushBatchResponse>()) } returns Unit
            every { asyncBatchPushRequest["processSingleSyncItems"](any<KinveyPushBatchResponse>(), any<List<SyncItem>>()) } returns arrayListOf<GenericJson>()
            every { asyncBatchPushRequest["getSaveItems"](any<List<SyncItem>>()) } returns arrayListOf<GenericJson>()
            every { asyncBatchPushRequest["processBatchSyncRequest"](any<List<SyncItem>>(), arrayListOf<GenericJson>()) } returns KinveySyncSaveBatchResponse(arrayListOf<GenericJson>(), arrayListOf<KinveyBatchInsertError>())

            asyncBatchPushRequest.executeAsync()

            verifySequence {
                asyncBatchPushRequest.executeAsync()
                asyncBatchPushRequest["processQueuedSyncRequests"](any<List<SyncRequest>>(), any<KinveyPushBatchResponse>())
                asyncBatchPushRequest["processSingleSyncItems"](any<KinveyPushBatchResponse>(), any<List<SyncItem>>())
            }

            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    @Test
    fun testAsyncBatchPushRequestSequenceExecutionExistSyncItems() {

        assertNotNull(spyNetworkManager)

        val latch = CountDownLatch(1)

        val looperThread = LooperThread(Runnable {

            val asyncBatchPushRequest = getAsyncBatchPushRequest()

            setPrivateField(asyncBatchPushRequest, AsyncBatchPushRequest::class.java,
            "batchSyncItems", arrayListOf(SyncItem(), SyncItem()))

            every { asyncBatchPushRequest["processQueuedSyncRequests"](any<List<SyncRequest>>(), any<KinveyPushBatchResponse>()) } returns Unit
            every { asyncBatchPushRequest["processSingleSyncItems"](any<KinveyPushBatchResponse>(), any<List<SyncItem>>()) } returns arrayListOf<GenericJson>()
            every { asyncBatchPushRequest["getSaveItems"](any<List<SyncItem>>()) } returns arrayListOf<GenericJson>()
            every { asyncBatchPushRequest["processBatchSyncRequest"](any<List<SyncItem>>(), arrayListOf<GenericJson>()) } returns KinveySyncSaveBatchResponse(arrayListOf<GenericJson>(), arrayListOf<KinveyBatchInsertError>())

            asyncBatchPushRequest.executeAsync()

            verifySequence {
                asyncBatchPushRequest.executeAsync()
                asyncBatchPushRequest["processQueuedSyncRequests"](any<List<SyncRequest>>(), any<KinveyPushBatchResponse>())
                asyncBatchPushRequest["processSingleSyncItems"](any<KinveyPushBatchResponse>(), any<List<SyncItem>>())
                asyncBatchPushRequest["getSaveItems"](any<List<SyncItem>>())
                asyncBatchPushRequest["processBatchSyncRequest"](any<List<SyncItem>>(), arrayListOf<GenericJson>())
            }

            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    private fun setPrivateField(mainClassObj: Any, mainClsType: Class<*>, fieldNameStr: String, fieldValue: Any) {
        try {
            val fieldName = mainClsType.getDeclaredField(fieldNameStr)
            fieldName.isAccessible = true
            fieldName.set(mainClassObj, fieldValue)
        } catch (e: Exception) {
            Log.e("setPrivateField", "", e)
        }
    }
}