package com.kinvey.androidTest.store.data.request

import android.content.Context
import android.os.Message
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.kinvey.android.Client
import com.kinvey.android.model.User
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.model.Person
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.model.KinveyCountResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.ReadPolicy
import com.kinvey.java.store.requests.data.read.ReadCountRequest
import com.kinvey.java.sync.SyncManager
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.spyk
import io.mockk.verifySequence
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@SmallTest
class ReadCountRequestTest {

    private var ttlValue = 20L
    private lateinit var client: Client<*>
    private lateinit var spyNetworkManager: NetworkManager<Person>
    private lateinit var syncManager: SyncManager

    private var cache: ICache<Person>? = null
    private var query: Query = Query()

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

    private fun getReadCountRequest(readPolicy: ReadPolicy): ReadCountRequest<Person> {
        return spyk(ReadCountRequest(cache, spyNetworkManager, readPolicy, query, syncManager), recordPrivateCalls = true)
    }

    @Test
    fun testReadCountRequestForceLocal() {

        Assert.assertNotNull(spyNetworkManager)

        val latch = CountDownLatch(1)

        val countResult = 2
        val kinveyCountResponse = KinveyCountResponse(countResult)

        val looperThread = LooperThread(Runnable {

            val readCountRequest = getReadCountRequest(ReadPolicy.FORCE_LOCAL)

            every { readCountRequest["countCached"]() } returns countResult
            every { readCountRequest["countNetwork"]() } returns kinveyCountResponse

            readCountRequest.execute()

            excludeRecords { readCountRequest.execute() }
            verifySequence {
                readCountRequest["countCached"]()
            }

            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    @Test
    fun testReadCountRequestForceNetwork() {

        Assert.assertNotNull(spyNetworkManager)

        val latch = CountDownLatch(1)

        val countResult = 2
        val kinveyCountResponse = KinveyCountResponse(countResult)

        val looperThread = LooperThread(Runnable {

            val readCountRequest = getReadCountRequest(ReadPolicy.FORCE_NETWORK)

            every { readCountRequest["countCached"]() } returns countResult
            every { readCountRequest["countNetwork"]() } returns kinveyCountResponse

            readCountRequest.execute()

            excludeRecords { readCountRequest.execute() }
            verifySequence {
                readCountRequest["countNetwork"]()
            }

            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    @Test
    fun testReadCountRequestBoth() {

        Assert.assertNotNull(spyNetworkManager)

        val latch = CountDownLatch(1)

        val countResult = 2
        val kinveyCountResponse = KinveyCountResponse(countResult)

        val looperThread = LooperThread(Runnable {

            val readCountRequest = getReadCountRequest(ReadPolicy.BOTH)

            every { readCountRequest["countCached"]() } returns countResult
            every { readCountRequest["countNetwork"]() } returns kinveyCountResponse
            every { readCountRequest["runPushAutoRequest"]() } returns Unit

            readCountRequest.execute()

            excludeRecords { readCountRequest.execute() }
            verifySequence {
                readCountRequest["runPushAutoRequest"]()
                readCountRequest["countNetwork"]()
            }

            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    @Test
    fun testReadCountRequestNetworkOtherwiseLocal() {

        Assert.assertNotNull(spyNetworkManager)

        val latch = CountDownLatch(1)

        val countResult = 2
        val kinveyCountResponse = KinveyCountResponse(countResult)

        val looperThread = LooperThread(Runnable {

            val readCountRequest = getReadCountRequest(ReadPolicy.NETWORK_OTHERWISE_LOCAL)

            every { readCountRequest["countCached"]() } returns countResult
            every { readCountRequest["countNetwork"]() } returns kinveyCountResponse
            every { readCountRequest["runPushAutoRequest"]() } returns Unit

            readCountRequest.execute()

            excludeRecords { readCountRequest.execute() }
            verifySequence {
                readCountRequest["runPushAutoRequest"]()
                readCountRequest["countNetwork"]()
            }

            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    @Test
    fun testReadCountRequestNetworkOtherwiseLocalException() {

        Assert.assertNotNull(spyNetworkManager)

        val latch = CountDownLatch(1)

        val countResult = 2
        //val kinveyCountResponse = KinveyCountResponse(countResult)

        val looperThread = LooperThread(Runnable {

            val readCountRequest = getReadCountRequest(ReadPolicy.NETWORK_OTHERWISE_LOCAL)

            every { readCountRequest["countCached"]() } returns countResult
            every { readCountRequest["countNetwork"]() } throws(UnknownHostException("test"))
            every { readCountRequest["runPushAutoRequest"]() } returns Unit

            readCountRequest.execute()

            excludeRecords { readCountRequest.execute() }
            verifySequence {
                readCountRequest["runPushAutoRequest"]()
                readCountRequest["countNetwork"]()
                readCountRequest["countCached"]()
            }

            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

}