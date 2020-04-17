package com.kinvey.androidTest.store.data;

import android.content.Context
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kinvey.android.AsyncClientRequest
import com.kinvey.android.Client
import com.kinvey.android.KinveyCallbackHandler
import com.kinvey.android.model.User
import com.kinvey.java.core.KinveyClientCallback
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CountDownLatch

/**
 * Created by Prots on 8/24/16.
 */
@RunWith(AndroidJUnit4::class)
class AsyncTaskTest {

    var client: Client<*>? = null
    var resultingLooper: Looper? = null
    var currentLooper: Looper? = null

    class AsyncTest(private val result: Int, callback: KinveyClientCallback<Int>) : AsyncClientRequest<Int>(callback) {

        fun initKinveyCallbackHandler() {
            kinveyCallbackHandler = KinveyCallbackHandler()
        }

        @Throws(IOException::class, InvocationTargetException::class, IllegalAccessException::class)
        override fun executeAsync(): Int {
            return result
        }
    }

    @Before
    fun setup() {
        val mMockContext: Context = InstrumentationRegistry.getInstrumentation().getTargetContext()
        client = Client.Builder<User>(mMockContext).build()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRunOnLooper() {
        val latch = CountDownLatch(1);
        Thread(Runnable {
            Looper.prepare()
            currentLooper = Looper.myLooper()
            AsyncTest(1, object: KinveyClientCallback<Int> {
                override fun onSuccess(result: Int?) {
                    finish()
                }
                override fun onFailure(error: Throwable?) {
                    finish()
                }
                fun finish() {
                    resultingLooper = Looper.myLooper()
                    latch.countDown()
                }
            }).execute()
            Looper.loop()
        }).start()
        latch.await()
        assertEquals(currentLooper, resultingLooper)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testNotifyOnLooper() {
        val latch = CountDownLatch(1);
        Thread(Runnable {
            Looper.prepare()
            currentLooper = Looper.myLooper()
            val async = AsyncTest(1, object: KinveyClientCallback<Int> {
                override fun onSuccess(result: Int?) {
                    finish()
                }
                override fun onFailure(error: Throwable?) {
                    finish()
                }
                fun finish() {
                    resultingLooper = Looper.myLooper()
                    latch.countDown()
                }
            })
            async.initKinveyCallbackHandler()
            async.notify(1)
            Looper.loop()
        }).start()
        latch.await()
        assertEquals(currentLooper, resultingLooper)
    }

    @After
    fun tearDown() {
        if (Client.kinveyHandlerThread != null) {
            try {
                client?.stopKinveyHandlerThread()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }
}
