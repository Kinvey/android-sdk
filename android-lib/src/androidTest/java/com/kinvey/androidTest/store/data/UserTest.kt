package com.kinvey.androidTest.store.data

import android.content.Context
import android.os.Message
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.callback.KinveyUserCallback
import com.kinvey.android.model.User
import com.kinvey.android.store.UserStore
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.TestManager
import com.kinvey.androidTest.store.user.UserStoreTest
import com.kinvey.java.AbstractClient
import com.kinvey.java.auth.Credential
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.store.UserStoreRequestManager
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.RuntimeException
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
    fun loginWithEx() {
        var isExceptionThrown = false
        val latch = CountDownLatch(1)
        val looperThread: LooperThread
        looperThread = LooperThread(Runnable {
            try {
                UserStore.Login("token", UserStoreRequestManager.LoginType.THIRDPARTY,
                        client as AbstractClient<User>, object : KinveyClientCallback<User> {
                    override fun onSuccess(result: User?) {
                        latch.countDown()
                    }

                    override fun onFailure(error: Throwable?) {
                        isExceptionThrown = true
                        latch.countDown()
                    }
                }).execute()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        Assert.assertTrue(isExceptionThrown)
    }

    @Test
    @Throws(RuntimeException::class)
    fun loginWithException() {
        var isExceptionThrown = false
        val latch = CountDownLatch(1)
        val looperThread: LooperThread
        looperThread = LooperThread(Runnable {
            try {
                UserStore.Login("token", UserStoreRequestManager.LoginType.THIRDPARTY,
                        client as AbstractClient<User>, object : KinveyClientCallback<User> {
                    override fun onSuccess(result: User?) {
                        latch.countDown()
                    }

                    override fun onFailure(error: Throwable?) {
                        isExceptionThrown = true
                        latch.countDown()
                    }
                }).execute()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        Assert.assertTrue(isExceptionThrown)
    }

    @Test
    @Throws(RuntimeException::class)
    fun loginAuthLink() {
        var isExceptionThrown = false
        val latch = CountDownLatch(1)
        val looperThread: LooperThread
        looperThread = LooperThread(Runnable {
            try {
                UserStore.Login("token", UserStoreRequestManager.LoginType.AUTH_LINK,
                        client as AbstractClient<User>, object : KinveyClientCallback<User> {
                    override fun onSuccess(result: User?) {
                        latch.countDown()
                    }

                    override fun onFailure(error: Throwable?) {
                        isExceptionThrown = true
                        latch.countDown()
                    }
                }).execute()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        Assert.assertTrue(isExceptionThrown)
    }

    @Test
    @Throws(RuntimeException::class)
    fun loginAuthLinkWrapp() {
        var isExceptionThrown = false
        val latch = CountDownLatch(1)
        val looperThread: LooperThread
        looperThread = LooperThread(Runnable {
            try {
                UserStore.loginAuthLink("accessToken", "refreshToken",
                        client as AbstractClient<User>, object : KinveyClientCallback<User> {
                    override fun onSuccess(result: User?) {
                        latch.countDown()
                    }

                    override fun onFailure(error: Throwable?) {
                        isExceptionThrown = true
                        latch.countDown()
                    }
                })
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        Assert.assertTrue(isExceptionThrown)
    }

    @Test
    @Throws(RuntimeException::class)
    fun loginSettersTest() {
        val ACCESS_TOKEN = "access_token"
        val REFRESH_TOKEN = "refresh_token"

        val username = "user"
        val password = "passwrd"
        val accessToken = "accessToken"
        val refreshToken = "refreshToken"
        val accessSecret = "accessSecret"
        val consumerKey = "consumerKey"
        val consumerSecret = "consumerSecret"

        val refresh = "refresh"
        val id = "id"
        val clientId = "clientId"
        val authToken = "authToken"

        val credential = Credential(id, authToken, refresh)
        val type = UserStoreRequestManager.LoginType.AUTH_LINK

        var login: UserStore.Login<*>? = null
        val latch = CountDownLatch(1)
        val looperThread: LooperThread
        looperThread = LooperThread(Runnable {
            try {
             login =  UserStore.Login("token", UserStoreRequestManager.LoginType.AUTH_LINK,
                     client as AbstractClient<User>, null)
                login?.username = username
                login?.password = password
                login?.accessToken = accessToken
                login?.refreshToken = refreshToken
                login?.accessSecret = accessSecret
                login?.consumerKey = consumerKey
                login?.consumerSecret = consumerSecret
                login?.id = id
                login?.clientId = clientId
                login?.credential = credential
                login?.type = type
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
            latch.countDown()
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        Assert.assertEquals(username, login?.username)
        Assert.assertEquals(password, login?.password)
        Assert.assertEquals(accessToken, login?.accessToken)
        Assert.assertEquals(refreshToken, login?.refreshToken)
        Assert.assertEquals(accessSecret, login?.accessSecret)
        Assert.assertEquals(consumerKey, login?.consumerKey)
        Assert.assertEquals(consumerSecret, login?.consumerSecret)
        Assert.assertEquals(id, login?.id)
        Assert.assertEquals(clientId, login?.clientId)
        Assert.assertEquals(credential, login?.credential)
        Assert.assertEquals(type, login?.type)
        Assert.assertEquals(ACCESS_TOKEN, UserStore.ACCESS_TOKEN)
        Assert.assertEquals(REFRESH_TOKEN, UserStore.REFRESH_TOKEN)
    }

    @Test
    fun testPostForOAuthTokenIncorrectData() {
        val clientId = "clientId"
        val username = "username"
        val password = "password"
        var isError = false
        val latch = CountDownLatch(1)
        val callback = object: KinveyUserCallback<User> {
            override fun onSuccess(result: User?) {
                latch.countDown()
            }
            override fun onFailure(error: Throwable?) {
                isError = true
                latch.countDown()
            }
        }
        val looperThread = LooperThread(Runnable {
            UserStore.loginWithMIC(client as Client<User>, username, password, clientId, callback)
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        Assert.assertTrue(isError)
    }

    @Test
    fun testPostForAccessTokenIncorrectData() {
        val redirectURI = "redirectURI"
        val clientId = "clientId"
        val token = "token"
        var isError = false
        val latch = CountDownLatch(1)
        val callback = object: KinveyClientCallback<User> {
            override fun onSuccess(result: User?) {
                latch.countDown()
            }
            override fun onFailure(error: Throwable?) {
                isError = true
                latch.countDown()
            }
        }
        val looperThread = LooperThread(Runnable {
            UserStore.PostForAccessToken(client as Client<User>, redirectURI, token, clientId, callback).execute()
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        Assert.assertTrue(isError)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginWithAuthorizationCodeAPIError() {
        val callback = loginWithAuthorizationCodeAPI(TestManager.USERNAME, TestManager.PASSWORD, "someClientId")
        junit.framework.Assert.assertNotNull(callback.error)
        Assert.assertNull(callback.result)
    }

    @Throws(InterruptedException::class)
    private fun loginWithAuthorizationCodeAPI(username: String?, password: String?,
                                              clientId: String): UserStoreTest.DefaultKinveyUserCallback {
        val latch = CountDownLatch(1)
        val callback = UserStoreTest.DefaultKinveyUserCallback(latch)
        val looperThread = LooperThread(Runnable { UserStore.loginWithAuthorizationCodeAPI(client as AbstractClient<User>, username!!, password!!, clientId, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
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