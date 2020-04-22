package com.kinvey.androidTest.store.data.request

import android.content.Context
import android.os.Message
import androidx.test.platform.app.InstrumentationRegistry
import com.kinvey.android.Client
import com.kinvey.android.model.User
import com.kinvey.androidTest.LooperThread
import com.kinvey.java.Query
import com.kinvey.java.model.AggregateType
import com.kinvey.java.model.Aggregation
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.ReadPolicy
import com.kinvey.java.store.requests.data.AggregationRequest
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class AggregationRequestTest {

    private val aggregationCollection = "Aggregation.Result"
    private var ttlValue = 20L
    private var fields: ArrayList<String> = arrayListOf()
    private val reduceField: String = ""

    private lateinit var query: Query
    private lateinit var spyNetworkManager: NetworkManager<Aggregation.Result>
    private lateinit var client: Client<*>

    @Before
    fun setUp() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Client.Builder<User>(mMockContext).build()
        spyNetworkManager = spyk(NetworkManager(aggregationCollection, Aggregation.Result::class.java, client), recordPrivateCalls = true)
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

    private fun getAggregationRequest(type: AggregateType, readPolicy: ReadPolicy): AggregationRequest {
        return spyk(AggregationRequest(type, client?.cacheManager?.getCache(aggregationCollection, Aggregation.Result::class.java, ttlValue),
               readPolicy, spyNetworkManager, fields, reduceField, query), recordPrivateCalls = true)
    }

    private fun testAggregationRequest(type: AggregateType, readPolicy: ReadPolicy, testMethod: String, params: Array<Any> = arrayOf()) {

        Assert.assertNotNull(spyNetworkManager)

        val latch = CountDownLatch(1)

        val looperThread = LooperThread(Runnable {

            val aggregationRequest =  getAggregationRequest(type, readPolicy)

            every { aggregationRequest[testMethod](*params) } returns null

            aggregationRequest.execute()

            excludeRecords { aggregationRequest.execute() }
            verify { aggregationRequest[testMethod](*params) }

            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    @Test
    fun testCountAggregationRequestNetwork() {
        testAggregationRequest(AggregateType.COUNT, ReadPolicy.FORCE_NETWORK, "countBlocking", arrayOf())
    }

    @Test
    fun testSumAggregationRequestNetwork() {
        testAggregationRequest(AggregateType.SUM, ReadPolicy.FORCE_NETWORK, "sumBlocking", arrayOf())
    }

    @Test
    fun testMinAggregationRequestNetwork() {
        testAggregationRequest(AggregateType.MIN, ReadPolicy.FORCE_NETWORK, "minBlocking", arrayOf())
    }

    @Test
    fun testMaxAggregationRequestNetwork() {
        testAggregationRequest(AggregateType.MAX, ReadPolicy.FORCE_NETWORK, "maxBlocking", arrayOf())
    }

    @Test
    fun testAverageAggregationRequestNetwork() {
        testAggregationRequest(AggregateType.AVERAGE, ReadPolicy.FORCE_NETWORK, "averageBlocking", arrayOf())
    }

    @Test
    fun testCountAggregationRequestLocal() {
        testAggregationRequest(AggregateType.COUNT, ReadPolicy.FORCE_LOCAL, "groupCache", arrayOf())
    }

    @Test
    fun testSumAggregationRequestLocal() {
        testAggregationRequest(AggregateType.SUM, ReadPolicy.FORCE_LOCAL, "groupCache", arrayOf())
    }

    @Test
    fun testMinAggregationRequestLocal() {
        testAggregationRequest(AggregateType.MIN, ReadPolicy.FORCE_LOCAL, "groupCache", arrayOf())
    }

    @Test
    fun testMaxAggregationRequestLocal() {
        testAggregationRequest(AggregateType.MAX, ReadPolicy.FORCE_LOCAL, "groupCache", arrayOf())
    }

    @Test
    fun testAverageAggregationRequestLocal() {
        testAggregationRequest(AggregateType.AVERAGE, ReadPolicy.FORCE_LOCAL, "groupCache", arrayOf())
    }

    @Test
    fun testCountAggregationRequestNetworkOrLocal() {
        testAggregationRequest(AggregateType.COUNT, ReadPolicy.NETWORK_OTHERWISE_LOCAL, "runNetworkOrLocal", arrayOf())
    }

    @Test
    fun testSumAggregationRequestNetworkOrLocal() {
        testAggregationRequest(AggregateType.SUM, ReadPolicy.NETWORK_OTHERWISE_LOCAL, "runNetworkOrLocal", arrayOf())
    }

    @Test
    fun testMinAggregationRequestNetworkOrLocal() {
        testAggregationRequest(AggregateType.MIN, ReadPolicy.NETWORK_OTHERWISE_LOCAL, "runNetworkOrLocal", arrayOf())
    }

    @Test
    fun testMaxAggregationRequestNetworkOrLocal() {
        testAggregationRequest(AggregateType.MAX, ReadPolicy.NETWORK_OTHERWISE_LOCAL, "runNetworkOrLocal", arrayOf())
    }

    @Test
    fun testAverageAggregationRequestNetworkOrLocal() {
        testAggregationRequest(AggregateType.AVERAGE, ReadPolicy.NETWORK_OTHERWISE_LOCAL, "runNetworkOrLocal", arrayOf())
    }

}