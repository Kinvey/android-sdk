package com.kinvey.androidTest.store.data

import android.content.Context
import android.os.Message
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.model.User
import com.kinvey.androidTest.LooperThread
import com.kinvey.java.core.KinveyClientCallback
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@SmallTest
class UserTest {

    private var client: Client<User>? = null
    private var mockContext: Context?  = null

    @Before
    @Throws(InterruptedException::class)
    fun setUp() {
        mockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Client.Builder<User>(mockContext).build()
    }

    @Test
    fun testUserUpdateConstructor() {
        val callback = object: KinveyClientCallback<User> {
            override fun onSuccess(result: User?) {}
            override fun onFailure(error: Throwable?) {}
        }

        var update: User.Update<User>? = null
        val latch = CountDownLatch(1)
        val looperThread = LooperThread(Runnable {
            update = User.Update(callback)
            latch.countDown()
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        Assert.assertEquals(update?.callback, callback)
    }

    @Test
    fun testUserRegisterLiveServiceConstructor() {
        val callback = object: KinveyClientCallback<Void> {
            override fun onSuccess(result: Void?) {}
            override fun onFailure(error: Throwable?) {}
        }

        var registerLiveService: User.RegisterLiveService? = null
        val latch = CountDownLatch(1)
        val looperThread = LooperThread(Runnable {
            registerLiveService = User.RegisterLiveService(callback)
            latch.countDown()
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        Assert.assertEquals(registerLiveService?.callback, callback)
    }

    @Test
    fun testUserUnregisterLiveServiceConstructor() {
        val callback = object: KinveyClientCallback<Void> {
            override fun onSuccess(result: Void?) {}
            override fun onFailure(error: Throwable?) {}
        }

        var unregisterLiveService: User.UnregisterLiveService? = null
        val latch = CountDownLatch(1)
        val looperThread = LooperThread(Runnable {
            unregisterLiveService = User.UnregisterLiveService(callback)
            latch.countDown()
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        Assert.assertEquals(unregisterLiveService?.callback, callback)
    }
}