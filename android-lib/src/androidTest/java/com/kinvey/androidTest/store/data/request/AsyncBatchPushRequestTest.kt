package com.kinvey.androidTest.store.data.request

import android.content.Context
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.async.AsyncBatchPushRequest
import com.kinvey.android.model.User
import com.kinvey.android.sync.KinveyPushCallback
import com.kinvey.android.sync.KinveyPushResponse
import com.kinvey.androidTest.model.Person
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.StoreType
import com.kinvey.java.sync.SyncManager
import io.mockk.spyk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class AsyncBatchPushRequestTest {

    private var client: Client<*>? = null
    private lateinit var spyNetworkManager: NetworkManager<Person>
    private var syncManager: SyncManager? = null

    val kinveyPushCallback = object: KinveyPushCallback {
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
            StoreType.NETWORK,
            spyNetworkManager,
            Person::class.java,
            kinveyPushCallback))
    }

    @Test
    fun testAsyncBatchPushRequestExecution() {

        val asyncBatchPushRequest = getAsyncBatchPushRequest()

        asyncBatchPushRequest?.executeAsync()

        //processQueuedSyncRequests(requests, pushResponse)
        //processSingleSyncItems(pushResponse, syncItems)

        //getSaveItems(batchSyncItems)
        //processBatchSyncRequest(batchSyncItems, saveItems)
    }

}