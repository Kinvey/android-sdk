package com.kinvey.androidTest.store.data.request

import android.content.Context
import android.os.Message
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.kinvey.android.Client
import com.kinvey.android.async.AsyncPullRequest
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore
import com.kinvey.android.sync.KinveyPullCallback
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.model.Person
import com.kinvey.java.Query
import com.kinvey.java.model.KinveyPullResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.StoreType
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch


@RunWith(AndroidJUnit4::class)
@SmallTest
class AsyncPullRequestTest {

    private lateinit var spyNetworkManager: NetworkManager<Person>
    private lateinit var client: Client<*>

    private val kinveyPullCallback = object : KinveyPullCallback {
        override fun onSuccess(result: KinveyPullResponse?) {}
        override fun onFailure(error: Throwable?) {}
    }

    @Before
    fun setUp() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Client.Builder<User>(mMockContext).build()
        spyNetworkManager = spyk(NetworkManager(Person.TEST_COLLECTION, Person::class.java, client))
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

    private fun getDataStore(): DataStore<Person> {
        return spyk(DataStore(Person.COLLECTION, Person::class.java, client, StoreType.AUTO, spyNetworkManager))
    }

    private fun getAsyncPullRequest(dataStore: DataStore<*>,
                                    pageSize: Int? = null,
                                    autoPagination: Boolean? = null): AsyncPullRequest {
        return if (pageSize != null) {
            AsyncPullRequest(dataStore, Query(), pageSize, kinveyPullCallback)
        } else {
            AsyncPullRequest(dataStore, Query(), autoPagination == true, kinveyPullCallback)
        }
    }

    @Test
    fun testAsyncPullRequestUseFixedPageSize() {
        val pageSize = 20
        val latch = CountDownLatch(1)

        val looperThread = LooperThread(Runnable {

            val  dataStore = getDataStore()
            val asyncPullRequest = getAsyncPullRequest(dataStore, pageSize = pageSize)

            every { dataStore.pullBlocking(any<Query>(), any<Int>()) } returns KinveyPullResponse(0)

            asyncPullRequest.executeAsync()

            verify { dataStore.pullBlocking(any<Query>(), any<Int>()) }

            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    @Test
    fun testAsyncPullRequestUseAutoPagination() {
        val autoPagination = true
        val latch = CountDownLatch(1)

        val looperThread = LooperThread(Runnable {

            val  dataStore = getDataStore()
            val asyncPullRequest = getAsyncPullRequest(dataStore, autoPagination = autoPagination)

            every { dataStore.pullBlocking(any<Query>(), any<Boolean>()) } returns KinveyPullResponse(0)

            asyncPullRequest.executeAsync()

            verify { dataStore.pullBlocking(any<Query>(), any<Boolean>()) }

            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }
}