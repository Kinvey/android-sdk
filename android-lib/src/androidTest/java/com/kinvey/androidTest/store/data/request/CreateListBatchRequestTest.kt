package com.kinvey.androidTest.store.data.request

import android.content.Context
import android.os.Message
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.api.client.json.GenericJson
import com.kinvey.android.Client
import com.kinvey.android.model.User
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.model.Person
import com.kinvey.java.cache.ICache
import com.kinvey.java.model.KinveyBatchInsertError
import com.kinvey.java.model.KinveySaveBatchResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.WritePolicy
import com.kinvey.java.store.requests.data.save.CreateListBatchRequest
import com.kinvey.java.sync.SyncManager
import io.mockk.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@SmallTest
class CreateListBatchRequestTest {

    private var ttlValue = 20L
    private val saveListFieldName = "saveList"
    private lateinit var client: Client<*>
    private lateinit var spyNetworkManager: NetworkManager<Person>
    private lateinit var syncManager: SyncManager
    private var cache: ICache<Person>? = null

    private var objects: Iterable<Person> = arrayListOf(Person(), Person())

    @Before
    fun setUp() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Client.Builder<User>(mMockContext).build()
        spyNetworkManager = spyk(NetworkManager(Person.COLLECTION, Person::class.java, client))
        syncManager = spyk(SyncManager(client?.cacheManager), recordPrivateCalls = true)
        cache = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, ttlValue)
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

    private fun getCreateListBatchRequest(writePolicy: WritePolicy): CreateListBatchRequest<Person> {
        return spyk(CreateListBatchRequest(cache, spyNetworkManager, writePolicy, objects, syncManager),
                recordPrivateCalls = true)
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

    @Test
    fun testCreateListBatchRequestExecuteNetwork() {

        Assert.assertNotNull(spyNetworkManager)

        val latch = CountDownLatch(1)

        val kinveySaveBatchResponse = KinveySaveBatchResponse(arrayListOf(Person(), Person()), arrayListOf())

        val looperThread = LooperThread(Runnable {

            val createListBatchRequest = getCreateListBatchRequest(WritePolicy.FORCE_NETWORK)

            every { createListBatchRequest["filterObjects"](any<List<Person>>()) } returns Unit
            every { createListBatchRequest["runSaveBatchBlocking"](any<List<Person>>()) } returns kinveySaveBatchResponse
            every { createListBatchRequest["removeSuccessBatchItemsFromCache"](any<List<Person>>(), any<List<KinveyBatchInsertError>>()) } returns Unit
            setPrivateField(createListBatchRequest, CreateListBatchRequest::class.java, saveListFieldName, arrayListOf(Person(), Person()))

            createListBatchRequest.execute()

            excludeRecords { createListBatchRequest.execute() }
            verifySequence {
                createListBatchRequest["runSaveItemsRequest"](any<List<Person>>(), any<Boolean>())
                createListBatchRequest["filterObjects"](any<List<Person>>())
                createListBatchRequest["postBatchItems"](any<List<Person>>(), any<KinveySaveBatchResponse<Person>>(), any<Boolean>())
                createListBatchRequest["postSaveBatchRequest"](any<List<Person>>(), any<KinveySaveBatchResponse<Person>>(), any<Boolean>())
                createListBatchRequest["runSaveBatchBlocking"](any<List<Person>>())
                createListBatchRequest["removeSuccessBatchItemsFromCache"](any<List<Person>>(), any<List<KinveyBatchInsertError>>())
            }

            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    @Test
    fun testCreateListBatchRequestExecuteLocal() {

        Assert.assertNotNull(spyNetworkManager)

        val latch = CountDownLatch(1)

        val looperThread = LooperThread(Runnable {

            val createListBatchRequest = getCreateListBatchRequest(WritePolicy.FORCE_LOCAL)

            every { syncManager?.enqueueSaveRequests(Person.COLLECTION, spyNetworkManager, any<List<Person>>()) } returns Unit
            setPrivateField(createListBatchRequest, CreateListBatchRequest::class.java, saveListFieldName, arrayListOf(Person(), Person()))

            createListBatchRequest.execute()

            excludeRecords { createListBatchRequest.execute() }
            verifySequence {
                syncManager?.enqueueSaveRequests(Person.COLLECTION, spyNetworkManager, any<List<Person>>())
            }

            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    @Test
    fun testCreateListBatchRequestExecuteLocalThenNetwork() {

        Assert.assertNotNull(spyNetworkManager)

        val latch = CountDownLatch(1)

        val kinveySaveBatchResponse = KinveySaveBatchResponse(arrayListOf(Person(), Person()), arrayListOf())

        val looperThread = LooperThread(Runnable {

            val createListBatchRequest = getCreateListBatchRequest(WritePolicy.LOCAL_THEN_NETWORK)

            every { createListBatchRequest["doPushRequest"]() } returns Unit
            every { createListBatchRequest["runSaveBatchBlocking"](any<List<Person>>()) } returns kinveySaveBatchResponse
            every { createListBatchRequest["removeSuccessBatchItemsFromCache"](any<List<Person>>(), any<List<KinveyBatchInsertError>>()) } returns Unit
            setPrivateField(createListBatchRequest, CreateListBatchRequest::class.java, saveListFieldName, arrayListOf(Person(), Person()))

            createListBatchRequest.execute()

            excludeRecords { createListBatchRequest.execute() }
            verifySequence {
                createListBatchRequest["doPushRequest"]()
                createListBatchRequest["runSaveItemsRequest"](any<List<Person>>(), any<Boolean>())
                createListBatchRequest["filterObjects"](any<List<Person>>())
                createListBatchRequest["postBatchItems"](any<List<Person>>(), any<KinveySaveBatchResponse<Person>>(), any<Boolean>())
                createListBatchRequest["postSaveBatchRequest"](any<List<Person>>(), any<KinveySaveBatchResponse<Person>>(), any<Boolean>())
                createListBatchRequest["runSaveBatchBlocking"](any<List<Person>>())
                createListBatchRequest["removeSuccessBatchItemsFromCache"](any<List<Person>>(), any<List<KinveyBatchInsertError>>())
            }

            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    @Test
    fun testCreateListBatchRequestExecuteLocalThenNetworkMultipleItems() {

        Assert.assertNotNull(spyNetworkManager)

        val itemsCount = 200
        val items = getItemsList(itemsCount)
        val latch = CountDownLatch(1)
        val kinveySaveBatchResponse = KinveySaveBatchResponse(items, arrayListOf())

        val looperThread = LooperThread(Runnable {

            val createListBatchRequest = getCreateListBatchRequest(WritePolicy.LOCAL_THEN_NETWORK)

            every { createListBatchRequest["doPushRequest"]() } returns Unit
            every { createListBatchRequest["runSaveBatchBlocking"](any<List<Person>>()) } returns kinveySaveBatchResponse
            every { createListBatchRequest["removeSuccessBatchItemsFromCache"](any<List<Person>>(), any<List<KinveyBatchInsertError>>()) } returns Unit
            setPrivateField(createListBatchRequest, CreateListBatchRequest::class.java, saveListFieldName, items)

            createListBatchRequest.execute()

            excludeRecords { createListBatchRequest.execute() }
            verifySequence {
                createListBatchRequest["doPushRequest"]()
                createListBatchRequest["runSaveItemsRequest"](any<List<Person>>(), any<Boolean>())
                createListBatchRequest["filterObjects"](any<List<Person>>())
                createListBatchRequest["postBatchItems"](any<List<Person>>(), any<KinveySaveBatchResponse<Person>>(), any<Boolean>())
                createListBatchRequest["postSaveBatchRequest"](any<List<Person>>(), any<KinveySaveBatchResponse<Person>>(), any<Boolean>())
                createListBatchRequest["runSaveBatchBlocking"](any<List<Person>>())
                createListBatchRequest["removeSuccessBatchItemsFromCache"](any<List<Person>>(), any<List<KinveyBatchInsertError>>())
            }

            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    private fun getItemsList(count: Int): MutableList<Person> {
        return (1..count)
                .map { Person("#$it", "Person_$it") }
                .toMutableList()
    }
}