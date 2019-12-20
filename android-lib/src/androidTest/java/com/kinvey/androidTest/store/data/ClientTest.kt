package com.kinvey.androidTest.store.data

import android.content.Context
import android.os.Message
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.callback.KinveyPingCallback
import com.kinvey.android.model.User
import com.kinvey.androidTest.LooperThread
import junit.framework.Assert.assertNull
import junit.framework.Assert.assertTrue
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch


@RunWith(AndroidJUnit4::class)
@SmallTest
class ClientTest {

    private val defSyncRate = (1000 * 60 * 10).toLong() //10 minutes
    private val defBatchRate = 1000L * 30L //30 seconds
    private val defBatchSize = 5

    private var client: Client<User>? = null
    private var mockContext: Context?  = null

    @Before
    @Throws(InterruptedException::class)
    fun setUp() {
        mockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Client.Builder<User>(mockContext).build()
    }

    private class KinveyPingCallbackAdapter(private val latch: CountDownLatch) : KinveyPingCallback {
        var result: Boolean? = null
        var error: Throwable? = null
        override fun onFailure(error: Throwable?) {
            this.error = error
            latch.countDown()
        }
        override fun onSuccess(result: Boolean?) {
            this.result = result
            latch.countDown()
        }
    }

    private fun ping(): KinveyPingCallbackAdapter {
        val latch = CountDownLatch(1)
        val pingCallback = KinveyPingCallbackAdapter(latch)
        val looperThread = LooperThread(Runnable { client?.ping(pingCallback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return pingCallback
    }

    @Test
    fun testClientConstructor() {

        assertNotNull(client?.context)

        client?.pushServiceClass = this.javaClass
        assertNotNull(client?.pushServiceClass)

        assertNotNull(client?.fileCacheFolder)
        assertNotNull(Client.kinveyHandlerThread)

        assertEquals(defSyncRate, client?.syncRate)
        assertEquals(defBatchRate, client?.batchRate)
        assertEquals(defBatchSize, client?.batchSize)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPingRequest() {
        val pingCallback = ping()
        assertTrue(pingCallback.result == true)
        assertNull(pingCallback.error)
    }
}