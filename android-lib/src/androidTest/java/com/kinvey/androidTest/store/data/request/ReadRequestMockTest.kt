package com.kinvey.androidTest.store.data.request

import android.content.Context
import android.os.Message
import androidx.test.platform.app.InstrumentationRegistry
import com.kinvey.android.Client
import com.kinvey.android.model.User
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.model.Person
import com.kinvey.java.Query
import com.kinvey.java.model.KinveyReadResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.ReadPolicy
import com.kinvey.java.store.requests.data.ReadRequest
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class ReadRequestMockTest {

    private var maxValue = 20L
    private var ttlValue = 20L

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

    private fun getReadRequest(readPolicy: ReadPolicy): ReadRequest<Person> {
        return spyk(ReadRequest(client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, ttlValue),
                query, readPolicy, maxValue, spyNetworkManager), recordPrivateCalls = true)
    }

    private fun testReadRequest(policy: ReadPolicy, testMethod: String, params: Array<Any> = arrayOf()) {

        assertNotNull(spyNetworkManager)

        val latch = CountDownLatch(1)

        val kinveyReadResponse = KinveyReadResponse("", arrayListOf(Person(), Person()))

        val looperThread = LooperThread(Runnable {

            val readRequest = getReadRequest(policy)

            every { readRequest[testMethod](*params) } returns kinveyReadResponse

            readRequest.execute()

            excludeRecords { readRequest.execute() }
            verify {
                readRequest[testMethod](*params)
            }

            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    @Test
    fun testLocalReadRequest() {
        testReadRequest(ReadPolicy.FORCE_LOCAL, "runLocal")
    }

    @Test
    fun testNetworkReadRequest() {
        testReadRequest(ReadPolicy.FORCE_NETWORK, "readItem", arrayOf(query as Query))
    }

    @Test
    fun testNetworkElseLocalReadRequest() {
        testReadRequest(ReadPolicy.NETWORK_OTHERWISE_LOCAL, "runOverNetwork")
    }
}