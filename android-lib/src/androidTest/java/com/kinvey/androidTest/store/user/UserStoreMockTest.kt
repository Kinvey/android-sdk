package com.kinvey.androidTest.store.user

import android.content.Context
import android.os.Message
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.AndroidJson
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.KinveySocketFactory
import com.kinvey.android.callback.KinveyUserCallback
import com.kinvey.android.model.User
import com.kinvey.android.store.UserStore.Companion.login
import com.kinvey.android.store.UserStore.Companion.logout
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.TestManager.Companion.PASSWORD
import com.kinvey.androidTest.TestManager.Companion.USERNAME
import com.kinvey.androidTest.store.user.MockHttpErrorTransport.Companion.DESCRIPTION_500
import com.kinvey.androidTest.store.user.MockHttpErrorTransport.Companion.ERROR_500
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
        var isExceptionThrown = false
        val mockedBuilder = MockClient.Builder<User>(mMockContext!!)
        val latch = CountDownLatch(1)
        val looperThread: LooperThread
        looperThread = LooperThread(Runnable {
            mockedBuilder.setRetrieveUserCallback(object : KinveyUserCallback<User> {
                override fun onSuccess(result: User?) {

                }

                override fun onFailure(t: Throwable?) {
                    isExceptionThrown = true
                }
            })
            latch.countDown()
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        val mockedClient = mockedBuilder.build(MockHttpErrorTransport())
        val callback = login(USERNAME, PASSWORD, mockedClient)
        assertNotNull(callback.error)
        assertNull(callback.result)
        assertEquals(500, (callback.error as KinveyJsonResponseException?)?.statusCode)
        Assert.assertTrue(isExceptionThrown)
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
        looperThread.mHandler?.sendMessage(Message())
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
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    fun testMimeTypeFinderInputStream() {
        val shared = Client.sharedInstance()
        val ex = AndroidJson.JSONPARSER.GSON.name + ", " +
                AndroidJson.JSONPARSER.JACKSON.name + ", " + AndroidJson.JSONPARSER.RAW.name
        Assert.assertEquals(AndroidJson.JSONPARSER.options, ex)
        val ad = AndroidJson()
        val jacksonFactory = AndroidJson.newCompatibleJsonFactory(AndroidJson.JSONPARSER.JACKSON)
        val rawFactory = AndroidJson.newCompatibleJsonFactory(AndroidJson.JSONPARSER.RAW)
        Assert.assertNotNull(shared)
        Assert.assertNotNull(ad)
        Assert.assertNotNull(jacksonFactory)
        Assert.assertNotNull(rawFactory)
        Assert.assertEquals(AndroidJson.JSONPARSER.valueOf("GSON"), AndroidJson.JSONPARSER.GSON)
    }

    @Test
    @Throws(Exception::class)
    fun socketFactoryConstructorTest() {
        val factory = KinveySocketFactory()
        Assert.assertNotNull(factory)
        Assert.assertNotNull(factory.defaultCipherSuites)
        Assert.assertEquals(factory.defaultCipherSuites.size, 15)
        Assert.assertNotNull(factory.supportedCipherSuites)
        Assert.assertEquals(factory.supportedCipherSuites.size, 28)
        val socket = factory.createSocket()
        Assert.assertNotNull(socket)
        socket?.close()
    }

    @Throws(InterruptedException::class)
    private fun logout(client: Client<*>?): DefaultKinveyVoidCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyVoidCallback(latch)
        val looperThread = LooperThread(Runnable { logout(client as AbstractClient<BaseUser>, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }
}