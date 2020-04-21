package com.kinvey.androidTest.store.data.request

import android.content.Context
import android.os.Message
import androidx.test.platform.app.InstrumentationRegistry
import com.kinvey.android.Client
import com.kinvey.android.model.User
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.model.Person
import com.kinvey.java.Query
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.WritePolicy
import com.kinvey.java.store.requests.data.DeleteRequest
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class DeleteRequestMockTest {

    private var ttlValue = 20L
    private var id = "ididid"

    private var client: Client<*>? = null
    private lateinit var spyNetworkManager: NetworkManager<Person>
    private var query: Query? = null

    @Before
    fun setUp() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Client.Builder<User>(mMockContext).build()
        spyNetworkManager = spyk(NetworkManager(Person.TEST_COLLECTION, Person::class.java, client))
        query = Query()
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

    private fun getDeleteRequest(writePolicy: WritePolicy): DeleteRequest<Person> {
        return spyk(DeleteRequest(client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, ttlValue),
                id, writePolicy, spyNetworkManager, query), recordPrivateCalls = true)
    }

    private fun testDeleteRequest(policy: WritePolicy, testMethod: String, params: Array<Any> = arrayOf()) {

        Assert.assertNotNull(spyNetworkManager)

        val latch = CountDownLatch(1)

        val looperThread = LooperThread(Runnable {

            val deleteRequest = getDeleteRequest(policy)

            every { deleteRequest[testMethod](*params) } returns Unit

            deleteRequest.execute()

            excludeRecords { deleteRequest.execute() }
            verify {
                deleteRequest[testMethod](*params)
            }

            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    @Test
    fun testLocalDeleteRequest() {
        testDeleteRequest(WritePolicy.FORCE_LOCAL, "deleteForceLocal")
    }

    @Test
    fun testNetworkDeleteRequest() {
        testDeleteRequest(WritePolicy.FORCE_NETWORK, "deleteForceNetwork")
    }

    @Test
    fun testNetworkElseLocalDeleteRequest() {
        testDeleteRequest(WritePolicy.LOCAL_THEN_NETWORK, "deleteLocalThenNetwork")
    }
}