package com.kinvey.androidTest.store.user

import android.content.Context
import android.os.Message
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.model.User
import com.kinvey.android.store.UserStore.Companion.login
import com.kinvey.android.store.UserStore.Companion.logout
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.TestManager.PASSWORD
import com.kinvey.androidTest.TestManager.USERNAME
import com.kinvey.androidTest.store.user.MockHttpErrorTransport.DESCRIPTION_500
import com.kinvey.androidTest.store.user.MockHttpErrorTransport.ERROR_500
import com.kinvey.java.AbstractClient
import com.kinvey.java.core.KinveyJsonResponseException
import com.kinvey.java.dto.BaseUser
import junit.framework.Assert.assertNotNull
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@SmallTest
class UserStoreMockTest {
    private var client: Client<*>? = null
    private var mMockContext: Context? = null

    @Before
    @Throws(InterruptedException::class)
    fun setup() {
        if (mMockContext == null) {
            mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        }
        if (client == null) {
            client = Builder<User>(mMockContext).build()
        }
        if (client?.isUserLoggedIn == true) {
            logout(client)
        }
    }

    @After
    @Throws(InterruptedException::class)
    fun tearDown() {
        if (client?.isUserLoggedIn == true) {
            logout(client)
        }
        client?.performLockDown()
        if (Client.kinveyHandlerThread != null) {
            try {
                client?.stopKinveyHandlerThread()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLogin() {
        val mockedClient = Builder<User>(mMockContext!!).build()
        val callback = login(mockedClient)
        assertNull(callback.error)
        assertNotNull(callback.result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginWithUsernameAndPassword() {
        val mockedClient = Builder<User>(mMockContext!!).build()
        val callback = login(USERNAME, PASSWORD, mockedClient)
        assertNull(callback.error)
        assertNotNull(callback.result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginError() {
        val mockedClient = MockClient.Builder<User>(mMockContext!!).build(MockHttpErrorTransport())
        val callback = login(USERNAME, PASSWORD, mockedClient)
        assertNotNull(callback.error)
        assertNull(callback.result)
        assertEquals(500, (callback.error as KinveyJsonResponseException?)?.statusCode)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginError500() {
        val mockedClient = MockClient.Builder<User>(mMockContext!!).build(MockHttpErrorTransport())
        val callback = login(USERNAME, PASSWORD, mockedClient)
        assertNotNull(callback.error)
        assertNull(callback.result)
        assertEquals(500, (callback.error as KinveyJsonResponseException?)?.statusCode)
        assertEquals(ERROR_500 + "\n" + DESCRIPTION_500, callback.error?.message)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginWithCredentialsError() {
        login(USERNAME, PASSWORD, client) //to save login and password to Credential
        Assert.assertTrue(client?.isUserLoggedIn == true)
        val mockedClient = MockClient.Builder<User>(mMockContext!!).build(MockHttpErrorTransport())
        Assert.assertFalse(mockedClient.isUserLoggedIn)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginWithCredentials() {
        if (client?.isUserLoggedIn == false) {
            login(USERNAME, PASSWORD, client) //to save login and password to Credential
        }
        Assert.assertTrue(client?.isUserLoggedIn == true)
        val mockedClient = MockClient.Builder<User>(mMockContext!!).build(MockHttpTransport())
        Assert.assertTrue(mockedClient.isUserLoggedIn)
    }

    @Throws(InterruptedException::class)
    private fun login(client: Client<*>): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable {
            try {
                login<User>(client as AbstractClient<User>, callback)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun login(username: String?, password: String?, client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable {
            try {
                login<User>(username!!, password!!, client as AbstractClient<User>, callback)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun logout(client: Client<*>?): DefaultKinveyVoidCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyVoidCallback(latch)
        val looperThread = LooperThread(Runnable { logout(client as AbstractClient<BaseUser>, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        return callback
    }
}