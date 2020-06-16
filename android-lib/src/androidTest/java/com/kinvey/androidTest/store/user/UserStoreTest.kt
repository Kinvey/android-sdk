package com.kinvey.androidTest.store.user

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.google.api.client.http.HttpResponseException
import com.kinvey.android.*
import com.kinvey.android.Client.Builder
import com.kinvey.android.Client.Companion.sharedInstance
import com.kinvey.android.callback.*
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore
import com.kinvey.android.store.DataStore.Companion.collection
import com.kinvey.android.store.UserStore
import com.kinvey.android.store.UserStore.Companion.changePassword
import com.kinvey.android.store.UserStore.Companion.convenience
import com.kinvey.android.store.UserStore.Companion.destroy
import com.kinvey.android.store.UserStore.Companion.exists
import com.kinvey.android.store.UserStore.Companion.forgotUsername
import com.kinvey.android.store.UserStore.Companion.get
import com.kinvey.android.store.UserStore.Companion.login
import com.kinvey.android.store.UserStore.Companion.loginFacebook
import com.kinvey.android.store.UserStore.Companion.loginGoogle
import com.kinvey.android.store.UserStore.Companion.loginLinkedIn
import com.kinvey.android.store.UserStore.Companion.loginMobileIdentity
import com.kinvey.android.store.UserStore.Companion.loginSalesForce
import com.kinvey.android.store.UserStore.Companion.loginTwitter
import com.kinvey.android.store.UserStore.Companion.loginWithAuthorizationCodeAPI
import com.kinvey.android.store.UserStore.Companion.loginWithAuthorizationCodeLoginPage
import com.kinvey.android.store.UserStore.Companion.logout
import com.kinvey.android.store.UserStore.Companion.onOAuthCallbackReceived
import com.kinvey.android.store.UserStore.Companion.resetPassword
import com.kinvey.android.store.UserStore.Companion.retrieve
import com.kinvey.android.store.UserStore.Companion.sendEmailConfirmation
import com.kinvey.android.store.UserStore.Companion.signUp
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.TestManager
import com.kinvey.androidTest.TestManager.Companion.PASSWORD
import com.kinvey.androidTest.TestManager.Companion.USERNAME
import com.kinvey.androidTest.model.EntitySet
import com.kinvey.androidTest.model.InternalUserEntity
import com.kinvey.androidTest.model.Person
import com.kinvey.androidTest.model.TestUser
import com.kinvey.java.*
import com.kinvey.java.Constants.AUTH_TOKEN
import com.kinvey.java.UserGroup.UserGroupResponse
import com.kinvey.java.auth.Credential
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.core.KinveyJsonResponseException
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.model.KinveyMetaData.Companion.KMD
import com.kinvey.java.store.*
import junit.framework.Assert.*
import org.junit.*
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.runner.RunWith
import java.io.IOException
import java.lang.RuntimeException
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@SmallTest
class UserStoreTest {

    private var client: Client<User>? = null

    private class DefaultKinveyUserListCallback(private val latch: CountDownLatch) : KinveyUserListCallback {

        var result: Array<User>? = null
        var error: Throwable? = null

        override fun onSuccess(result: Array<User>?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            error?.printStackTrace()
            this.error = error
            finish()
        }

        private fun finish() {
            latch.countDown()
        }
    }

    private class DefaultKinveyListCallback(private val latch: CountDownLatch) : KinveyListCallback<User> {
        var result: List<*>? = null
        var error: Throwable? = null

        override fun onFailure(error: Throwable?) {
            error?.printStackTrace()
            this.error = error
            finish()
        }

        private fun finish() {
            latch.countDown()
        }

        override fun onSuccess(result: List<User>?) {
            this.result = result
            finish()
        }
    }

    private class CustomKinveyClientCallback(private val latch: CountDownLatch) : KinveyClientCallback<TestUser> {
        var result: TestUser? = null
        var error: Throwable? = null

        override fun onSuccess(user: TestUser?) {
            result = user
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        private fun finish() {
            latch.countDown()
        }
    }

    private class DefaultKinveyUserDeleteCallback(private val latch: CountDownLatch) : KinveyUserDeleteCallback {
        var error: Throwable? = null
        override fun onSuccess(result: Void?) {
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        private fun finish() {
            latch.countDown()
        }
    }

    class UserGroupResponseCallback(private val latch: CountDownLatch) : KinveyClientCallback<UserGroupResponse> {
        var result: UserGroupResponse? = null
        var error: Throwable? = null

        override fun onSuccess(user: UserGroupResponse?) {
            result = user
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        private fun finish() {
            latch.countDown()
        }
    }

    private class DefaultKinveyMICCallback(private val latch: CountDownLatch) : KinveyMICCallback<User> {
        private var result: User? = null
        private var error: Throwable? = null
        var myURLToRender: String? = null

        override fun onSuccess(result: User?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        private fun finish() {
            latch.countDown()
        }

        override fun onReadyToRender(myURLToRender: String) {
            this.myURLToRender = myURLToRender
            finish()
        }
    }

    class DefaultKinveyUserCallback(private val latch: CountDownLatch) : KinveyUserCallback<User> {
        var result: User? = null
        var error: Throwable? = null

        override fun onSuccess(result: User?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        private fun finish() {
            latch.countDown()
        }
    }

    private class DefaultKinveyBooleanCallback(private val latch: CountDownLatch) : KinveyClientCallback<Boolean> {
        var result = false
        private var error: Throwable? = null

        override fun onSuccess(result: Boolean?) {
            this.result = result ?: false
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        private fun finish() {
            latch.countDown()
        }
    }

    private class DefaultPersonKinveyClientCallback(private val latch: CountDownLatch) : KinveyClientCallback<Person> {
        private var result: Person? = null
        private var error: Throwable? = null

        override fun onSuccess(result: Person?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        private fun finish() {
            latch.countDown()
        }
    }

    private class DefaultKinveyUserManagementCallback(private val latch: CountDownLatch) : KinveyUserManagementCallback {
        var result = false
        var error: Throwable? = null
        override fun onSuccess(v: Void?) {
            result = true
            finish()
        }

        override fun onFailure(error: Throwable?) {
            result = false
            this.error = error
            finish()
        }

        private fun finish() {
            latch.countDown()
        }
    }

    @Throws(InterruptedException::class)
    private fun findIdEntitySet(store: DataStore<EntitySet>, id: String, seconds: Int): DefaultKinveyClientEntitySetCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientEntitySetCallback(latch)
        val looperThread = LooperThread(Runnable { store.find(id, callback, null) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    private class DefaultKinveyClientEntitySetCallback(private val latch: CountDownLatch) : KinveyClientCallback<EntitySet> {
        var result: EntitySet? = null
        var error: Throwable? = null

        override fun onSuccess(result: EntitySet?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        fun finish() {
            latch.countDown()
        }
    }

    @Before
    @Throws(InterruptedException::class)
    fun setup() {
        val mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
//        client.enableDebugLogging();
        if (client?.isUserLoggedIn == true) {
            logout(client)
        }
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

    @Test
    fun testUserStoreLoginClassConstructors() {
        val userName = "user"
        val userPass = "pass"
        val accessToken = "accessToken"
        val loginType = UserStoreRequestManager.LoginType.CREDENTIALSTORE
        val refreshToken = "refreshToken"

        val accessSecret = "accessSecret"
        val consumerKey = "consumerKey"
        val consumerSecret = "consumerSecret"

        val clientId = "clientId"
        val refresh = "refresh"
        val id = "id"
        val authToken = "authToken"

        val credential = Credential(id, authToken, refresh)

        val latch = CountDownLatch(1)

        var login1: UserStore.Login<*>? = null
        var login2: UserStore.Login<*>? = null
        var login3: UserStore.Login<*>? = null
        var login4: UserStore.Login<*>? = null
        var login5: UserStore.Login<*>? = null
        var login6: UserStore.Login<*>? = null
        var login7: UserStore.Login<*>? = null

        val looperThread = LooperThread(Runnable {
            login1 = UserStore.Login(client as AbstractClient<User>, null)
            login2 = UserStore.Login(userName, userPass, client as AbstractClient<User>, null)
            login3 = UserStore.Login(accessToken, loginType, client as AbstractClient<User>, null)
            login4 = UserStore.Login(accessToken, refreshToken, loginType, client as AbstractClient<User>, null)
            login5 = UserStore.Login(accessToken, accessSecret, consumerKey, consumerSecret, client as AbstractClient<User>, loginType, null)
            login6 = UserStore.Login(accessToken, clientId, refresh, id, client as AbstractClient<User>, null)
            login7 = UserStore.Login(credential, client as AbstractClient<User>, null)
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, login1?.client)

        assertEquals(client, login2?.client)
        assertEquals(userName, login2?.username)
        assertEquals(userPass, login2?.password)

        assertEquals(client, login3?.client)
        assertEquals(accessToken, login3?.accessToken)
        assertEquals(loginType, login3?.type)

        assertEquals(client, login4?.client)
        assertEquals(accessToken, login4?.accessToken)
        assertEquals(refreshToken, login4?.refreshToken)
        assertEquals(loginType, login4?.type)

        assertEquals(client, login5?.client)
        assertEquals(accessToken, login5?.accessToken)
        assertEquals(loginType, login5?.type)
        assertEquals(accessSecret, login5?.accessSecret)
        assertEquals(consumerKey, login5?.consumerKey)
        assertEquals(consumerSecret, login5?.consumerSecret)

        assertEquals(client, login6?.client)
        assertEquals(accessToken, login6?.accessToken)
        assertEquals(clientId, login6?.clientId)
        assertEquals(refresh, login6?.refreshToken)
        assertEquals(id, login6?.id)

        assertEquals(client, login7?.client)
        assertEquals(credential, login7?.credential)
    }

    @Test
    fun testUserStoreCreateClassConstructors() {
        val userName = "user"
        val userPass = "pass"
        var create: UserStore.Create<*>? = null
        val latch = CountDownLatch(1)

        val looperThread = LooperThread(Runnable {
            create = UserStore.Create(userName, userPass, client?.activeUser, client as AbstractClient<User>, null)
            create?.username = userName
            create?.password = userPass
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, create?.client)
        assertEquals(userName, create?.username)
        assertEquals(userPass, create?.password)
    }

    @Test
    @Throws(RuntimeException::class)
    fun loginKinveyAuth() {
        var isExceptionThrown = false
        val latch = CountDownLatch(1)
        val looperThread: LooperThread
        looperThread = LooperThread(Runnable {
            try {
                UserStore().loginKinveyAuthToken("userId", "authToken",
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
    fun testUpdateObject() {
        login(TestManager.USERNAME, TestManager.PASSWORD)
        var isExceptionThrown = false
        val latch = CountDownLatch(1)
        val looperThread: LooperThread
        looperThread = LooperThread(Runnable {
            try {
                UserStore().save(client as AbstractClient<User>, object : KinveyClientCallback<User> {
                    override fun onSuccess(result: User?) {
                        Assert.assertNotNull(result)
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
        Assert.assertFalse(isExceptionThrown)
    }

    @Test
    fun testUserStoreDeleteClassConstructors() {
        val hardDel = true
        val latch = CountDownLatch(1)
        var delete: UserStore.Delete? = null

        val looperThread = LooperThread(Runnable {
            delete = UserStore.Delete(true, client as AbstractClient<BaseUser>, null)
            delete?.hardDelete = true
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, delete?.client)
        assertEquals(hardDel, delete?.hardDelete)
    }

    @Test
    fun testUserStoreLogoutClassConstructors() {
        val latch = CountDownLatch(1)
        var logout: UserStore.Logout? = null

        val looperThread = LooperThread(Runnable {
            logout = UserStore.Logout(client as AbstractClient<BaseUser>, null)
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, logout?.client)
    }

    @Test
    fun testUserStorePostForAccessTokenClassConstructors() {

        val redirectURI = "redirectURI"
        val token = "token"
        val clientId = "clientId"

        val latch = CountDownLatch(1)
        var post: UserStore.PostForAccessToken<User>? = null

        val looperThread = LooperThread(Runnable {
            post = UserStore.PostForAccessToken(client as Client<User>, redirectURI, token, clientId, null)
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, post?.client)
        assertEquals(redirectURI, post?.redirectURI)
        assertEquals(token, post?.token)
        assertEquals(clientId, post?.clientId)
    }

    @Test
    fun testUserStorePostForOAuthTokenClassConstructors() {

        val redirectURI = "redirectURI"
        val clientId = "clientId"
        val username = "username"
        val password = "password"

        val latch = CountDownLatch(1)
        var post: UserStore.PostForOAuthToken<User>? = null

        val looperThread = LooperThread(Runnable {
            post = UserStore.PostForOAuthToken(client as Client<User>, clientId, redirectURI, username, password, null)
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, post?.client)
        assertEquals(clientId, post?.clientId)
        assertEquals(redirectURI, post?.redirectURI)
        assertEquals(username, post?.username)
        assertEquals(password, post?.password)
    }

    @Test
    fun testUserStoreRetrieveClassConstructors() {
        val resolves = arrayOf("res1", "res2")
        val latch = CountDownLatch(1)
        var retrieve1: UserStore.Retrieve<User>? = null
        var retrieve2: UserStore.Retrieve<User>? = null

        val looperThread = LooperThread(Runnable {
            retrieve1 = UserStore.Retrieve(client as Client<User>, null)
            retrieve2 = UserStore.Retrieve(resolves, client as Client<User>, null)
            retrieve2?.resolves = resolves
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, retrieve1?.client)

        assertEquals(client, retrieve2?.client)
        assertEquals(resolves, retrieve2?.resolves)
    }

    @Test
    fun testUserStoreRetrieveUserListConstructors() {

        val resolves = arrayOf("res1", "res2")
        val query: Query = Query()
        val latch = CountDownLatch(1)
        var retrieve1: UserStore.RetrieveUserList<User>? = null
        var retrieve2: UserStore.RetrieveUserList<User>? = null

        val looperThread = LooperThread(Runnable {
            retrieve1 = UserStore.RetrieveUserList(query, client as Client<User>, null)
            retrieve2 = UserStore.RetrieveUserList(query, resolves, client as Client<User>, null)
            retrieve2?.query = query
            retrieve2?.resolves = resolves
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, retrieve1?.client)
        assertEquals(query, retrieve1?.query)

        assertEquals(client, retrieve2?.client)
        assertEquals(query, retrieve2?.query)
        assertEquals(resolves, retrieve2?.resolves)
    }

    @Test
    fun testUserStoreRetrieveUserArrayConstructors() {
        val resolves = arrayOf("res1", "res2")
        val query: Query = Query()
        val latch = CountDownLatch(1)
        var retrieve1: UserStore.RetrieveUserArray<User>? = null

        val looperThread = LooperThread(Runnable {
            retrieve1 = UserStore.RetrieveUserArray(query, resolves, client as Client<User>, null)
            retrieve1?.query = query
            retrieve1?.resolves = resolves
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, retrieve1?.client)
        assertEquals(query, retrieve1?.query)
        assertEquals(resolves, retrieve1?.resolves)
    }

    @Test
    fun testUserStoreRetrieveMetaDataConstructors() {
        val latch = CountDownLatch(1)
        var retrieve1: UserStore.RetrieveMetaData<User>? = null

        val looperThread = LooperThread(Runnable {
            retrieve1 = UserStore.RetrieveMetaData(client as Client<User>, null)
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, retrieve1?.client)
    }

    @Test
    fun testUserStoreUpdateConstructors() {
        val latch = CountDownLatch(1)
        var retrieve1: UserStore.Update<User>? = null

        val looperThread = LooperThread(Runnable {
            retrieve1 = UserStore.Update(client as Client<User>, null)
            retrieve1?.client = client as Client<User>
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, retrieve1?.client)
    }

    @Test
    fun testUserStoreChangePasswordConstructors() {
        val password = "password"
        val latch = CountDownLatch(1)
        var changePassword: UserStore.ChangePassword? = null

        val looperThread = LooperThread(Runnable {
            changePassword = UserStore.ChangePassword(password, client as Client<User>, null)
            changePassword?.client = client as Client<User>
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, changePassword?.client)
        assertEquals(password, changePassword?.password)
    }

    @Test
    @Throws(RuntimeException::class)
    fun loginKinveyAuthError() {
        var isExceptionThrown = false
        val latch = CountDownLatch(1)
        val looperThread: LooperThread
        looperThread = LooperThread(Runnable {
            try {
                UserStore().loginKinveyAuthToken("userId", "authToken",
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
    fun LoginKinveyAuthError() {
        var isExceptionThrown = false
        val latch = CountDownLatch(1)
        val looperThread: LooperThread
        looperThread = LooperThread(Runnable {
            try {
                UserStore.LoginKinveyAuth("id", "token",
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
    fun testUserStoreGetUser() {
        var isExceptionThrown = false
        if (client?.isUserLoggedIn == false) {
            val callback = login(TestManager.USERNAME, TestManager.PASSWORD)
            assertNull(callback.error)
        }
        val userId = client?.activeUser?.id ?: ""
        val latch = CountDownLatch(1)
        val looperThread = LooperThread(Runnable {
            UserStore.GetUser(userId, client as Client<User>, object : KinveyClientCallback<User> {
                override fun onSuccess(result: User?) {
                    Assert.assertNotNull(result)
                    latch.countDown()
                }

                override fun onFailure(error: Throwable?) {
                    isExceptionThrown = true
                    latch.countDown()
                }
            }).execute()
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        Assert.assertFalse(isExceptionThrown)
    }

    @Test
    @Throws(RuntimeException::class)
    fun testUpdateSuccess() {
        login(TestManager.USERNAME, TestManager.PASSWORD)
        var isExceptionThrown = false
        val latch = CountDownLatch(1)
        val looperThread: LooperThread
        looperThread = LooperThread(Runnable {
            try {
                UserStore().save(client as AbstractClient<User>, object : KinveyClientCallback<User> {
                    override fun onSuccess(result: User?) {
                        Assert.assertNotNull(result)
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
        Assert.assertFalse(isExceptionThrown)
    }

    @Test
    fun testUserStoreResetPasswordConstructors() {
        val usernameOrEmail = "test@test.com"
        val latch = CountDownLatch(1)
        var resetPassword: UserStore.ResetPassword? = null

        val looperThread = LooperThread(Runnable {
            resetPassword = UserStore.ResetPassword(usernameOrEmail, client as Client<User>, null)
            resetPassword?.usernameOrEmail = usernameOrEmail
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, resetPassword?.client)
        assertEquals(usernameOrEmail, resetPassword?.usernameOrEmail)
    }

    @Test
    fun testUserStoreExistsUserConstructors() {
        val username = "username"
        val latch = CountDownLatch(1)
        var existsUser: UserStore.ExistsUser? = null

        val looperThread = LooperThread(Runnable {
            existsUser = UserStore.ExistsUser(username, client as Client<User>, null)
            existsUser?.username  = username
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, existsUser?.client)
        assertEquals(username, existsUser?.username)
    }

    @Test
    fun testUserStoreGetUserConstructors() {
        val userId = "userId"
        val latch = CountDownLatch(1)
        var getUser: UserStore.GetUser<User>? = null

        val looperThread = LooperThread(Runnable {
            getUser = UserStore.GetUser(userId, client as Client<User>, null)
            getUser?.userId = userId
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, getUser?.client)
        assertEquals(userId, getUser?.userId)
    }

    @Test
    fun testUserStoreEmailVerificationConstructors() {
        val latch = CountDownLatch(1)
        var getUser: UserStore.EmailVerification? = null

        val looperThread = LooperThread(Runnable {
            getUser = UserStore.EmailVerification(client as Client<User>, null)
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, getUser?.client)
    }

    @Test
    fun testUserStoreForgotUsernameConstructors() {
        val email = "email"
        val latch = CountDownLatch(1)
        var forgotUser: UserStore.ForgotUsername? = null

        val looperThread = LooperThread(Runnable {
            forgotUser = UserStore.ForgotUsername(client as Client<User>, email, null)
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, forgotUser?.client)
        assertEquals(email, forgotUser?.email)
    }

    @Test
    fun testUserStoreLoginKinveyAuthConstructors() {
        val userID = "userID"
        val authToken = "authToken"

        val latch = CountDownLatch(1)
        var loginKinveyAuth: UserStore.LoginKinveyAuth<User>? = null

        val looperThread = LooperThread(Runnable {
            loginKinveyAuth = UserStore.LoginKinveyAuth(userID, authToken, client as Client<User>, null)
            latch.countDown()
        })

        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())

        assertEquals(client, loginKinveyAuth?.client)
        assertEquals(userID, loginKinveyAuth?.userID)
        assertEquals(authToken, loginKinveyAuth?.authToken)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLogin() {
        val callback = login(USERNAME, PASSWORD)
        assertNull(callback.error)
        assertNotNull(callback.result)
        assertNull(logout(client).error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun get() {
        val userId: String?
        login(USERNAME, PASSWORD)
        userId = client?.activeUser?.id
        val callback = get(userId)
        assertNull(callback.error)
        assertNotNull(callback.result)
        assertEquals(userId, callback.result?.id)
    }

    @Throws(InterruptedException::class)
    private operator fun get(userName: String?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable { get<User>(userName!!, client!!, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testCustomSignUp() {
        client?.userClass = TestUser::class.java as Class<User>
        val user = TestUser()
        user.companyName = "Test Company"
        val internalUserEntity = InternalUserEntity()
        internalUserEntity.street = "TestStreet"
        user.internalUserEntity = internalUserEntity
        val callback = signUp(user)
        assertNull(callback.error)
        assertNotNull(callback.result)
        assertEquals(user.companyName, callback.result?.companyName)
        assertEquals("TestStreet", callback.result?.internalUserEntity?.street)
        assertEquals("Test Company", (client?.activeUser as TestUser?)?.companyName)
        assertEquals("TestStreet", (client?.activeUser as TestUser?)?.internalUserEntity?.street)
        destroyUser()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDestroy() {
        val callback = signUp()
        assertNull(callback.error)
        assertNotNull(callback.result)
        val deleteCallback = destroyUser()
        assertNull(deleteCallback.error)
        assertFalse(client?.isUserLoggedIn == true)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testCustomDestroy() {
        client?.userClass = TestUser::class.java as Class<User>
        val user = TestUser()
        user.companyName = "Test Company"
        val callback = signUp(user)
        assertNull(callback.error)
        assertNotNull(callback.result)
        val deleteCallback = destroyUser()
        assertNull(deleteCallback.error)
        assertFalse(client?.isUserLoggedIn == true)
    }

    @Throws(InterruptedException::class)
    private fun login(userName: String?, password: String?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable {
            try {
                login<User>(userName!!, password!!, client as AbstractClient<User>, callback)
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
    private fun signUp(): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable { signUp(createRandomUserName(USERNAME), PASSWORD, client as AbstractClient<User>, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun signUp(user: TestUser): CustomKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = CustomKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable { signUp(createRandomUserName(USERNAME), PASSWORD, user, client as AbstractClient<User>, callback as KinveyClientCallback<User>) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun destroyUser(): DefaultKinveyUserDeleteCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyUserDeleteCallback(latch)
        val looperThread = LooperThread(Runnable { destroy(true, client as AbstractClient<BaseUser>, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginAsync() {
        val userCallback = login(client)
        assertNotNull(userCallback.result)
        assertTrue(client?.isUserLoggedIn == true)
        assertNull(logout(client).error)
    }

    @Throws(InterruptedException::class)
    private fun login(client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable {
            try {
                UserStore.login<User>(client as AbstractClient<User>, callback)
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
    private fun logout(client: Client<*>?): DefaultKinveyVoidCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyVoidCallback(latch)
        val looperThread = LooperThread(Runnable { logout(client as AbstractClient<BaseUser>, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSharedClientLoginAsync() {
        val userCallback = login(sharedInstance())
        assertNotNull(userCallback.result)
        assertTrue(client?.isUserLoggedIn == true)
        assertNull(logout(client).error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginAsyncBad() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        val fakeClient = Builder<User>("app_key_fake", "app_secret_fake", mMockContext).build()
        var userCallback = login(fakeClient)
        assertNotNull(userCallback.error)
        userCallback = login(USERNAME, PASSWORD, fakeClient)
        assertNotNull(userCallback.error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginUserPassAsync() {
        val userCallback = login(USERNAME, PASSWORD, client)
        assertNotNull(userCallback.result)
        assertTrue(client?.isUserLoggedIn == true)
        assertNull(logout(client).error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginUserPassAsyncBad() {
        val userCallback = login(USERNAME, "wrongPassword", client)
        assertNotNull(userCallback.error)
        assertFalse(client?.isUserLoggedIn == true)
    }

    @Test
    @Ignore // need facebookAccessToken
    @Throws(InterruptedException::class)
    fun testLoginFacebookAsync() {
        val facebookAccessToken = "YOUR_ACCESS_TOKEN_HERE"
        val userCallback = loginFacebook(facebookAccessToken, client)
        assertNotNull(userCallback.result)
        assertTrue(client?.isUserLoggedIn == true)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginFacebookAsyncBad() {
        val facebookAccessToken = "wrong_access_token"
        val userCallback = loginFacebook(facebookAccessToken, client)
        assertNotNull(userCallback.error)
        assertFalse(client?.isUserLoggedIn == true)
    }

    @Throws(InterruptedException::class)
    private fun loginFacebook(accessToken: String, client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable {
            try {
                loginFacebook<User>(accessToken, client as AbstractClient<User>, callback)
            } catch (e: IOException) {
                e.printStackTrace()
                return@Runnable
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Ignore // need googleAccessToken
    @Throws(InterruptedException::class)
    fun testLoginGoogleAsync() {
        val googleAccessToken = "YOUR_ACCESS_TOKEN_HERE"
        val userCallback = loginGoogle(googleAccessToken, client)
        assertNotNull(userCallback.result)
        assertTrue(client?.isUserLoggedIn == true)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginGoogleAsyncBad() {
        val googleAccessToken = "wrong_access_token"
        val userCallback = loginGoogle(googleAccessToken, client)
        assertNotNull(userCallback.error)
        assertFalse(client?.isUserLoggedIn == true)
    }

    @Throws(InterruptedException::class)
    private fun loginGoogle(accessToken: String, client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable {
            try {
                loginGoogle<User>(accessToken, client as AbstractClient<User>, callback)
            } catch (e: IOException) {
                e.printStackTrace()
                return@Runnable
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Ignore // need to add accessToken,  accessSecret, consumerKey, consumerSecret
    @Throws(InterruptedException::class)
    fun testLoginTwitterAsync() {
        val accessToken = "YOUR_ACCESS_TOKEN_HERE"
        val accessSecret = "YOUR_ACCESS_SECRET_HERE"
        val consumerKey = "YOUR_CONSUMER_KEY_HERE"
        val consumerSecret = "YOUR_CONSUMER_SECRET_HERE"
        val userCallback = loginTwitter(accessToken, accessSecret, consumerKey, consumerSecret, client)
        assertNotNull(userCallback.result)
        assertTrue(client?.isUserLoggedIn == true)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginTwitterAsyncBad() {
        val accessToken = "YOUR_ACCESS_TOKEN_HERE"
        val accessSecret = "YOUR_ACCESS_SECRET_HERE"
        val consumerKey = "YOUR_CONSUMER_KEY_HERE"
        val consumerSecret = "YOUR_CONSUMER_SECRET_HERE"
        val userCallback = loginTwitter(accessToken, accessSecret, consumerKey, consumerSecret, client)
        assertNotNull(userCallback.error)
        assertFalse(client?.isUserLoggedIn == true)
    }

    @Throws(InterruptedException::class)
    private fun loginTwitter(accessToken: String, accessSecret: String, consumerKey: String, consumerSecret: String, client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable {
            try {
                loginTwitter<User>(accessToken, accessSecret, consumerKey, consumerSecret, client as AbstractClient<User>, callback)
            } catch (e: IOException) {
                e.printStackTrace()
                return@Runnable
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    // TODO: 09.12.2016 should be checked
    @Test
    @Ignore // need to add accessToken,  accessSecret, consumerKey, consumerSecret
    @Throws(InterruptedException::class)
    fun testLoginLinkedInAsync() {
        val accessToken = "YOUR_ACCESS_TOKEN_HERE" //"AQXu60okmBXrQkBm5BOpBCBBpCYc3y9uKWHtF559A1j4ttwjf5bXNeq0nVOHtgPomuw9Wn661BYbZal-3IReW0zc-Ed8NvP0FNdOTQVt9c8qz9EL5sezCYKd_I2VPEEMSC-YOyvhi-7WsttjaPnU_9H_kCnfVJuU7Fyt8Ph1XTw66xZeu2U"

        val accessSecret = "YOUR_ACCESS_SECRET_HERE" //"ExAZxYxvo42UfOCN";

        val consumerKey = "YOUR_CONSUMER_KEY_HERE" //"86z99b0orhyt7s";

        val consumerSecret = "YOUR_CONSUMER_SECRET_HERE" //"ExAZxYxvo42UfOCN";

        val userCallback = loginLinkedIn(accessToken, accessSecret, consumerKey, consumerSecret, client)
        assertNotNull(userCallback.result)
        assertTrue(client?.isUserLoggedIn == true)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginLinkedInAsyncBad() {
        val accessToken = "wrongAccessToken"
        val accessSecret = "wrongAccessSecret"
        val consumerKey = "wrongConsumerKey"
        val consumerSecret = "wrongConsumerSecret"
        val userCallback = loginLinkedIn(accessToken, accessSecret, consumerKey, consumerSecret, client)
        assertNotNull(userCallback.error)
        assertFalse(client?.isUserLoggedIn == true)
    }

    @Throws(InterruptedException::class)
    private fun loginLinkedIn(accessToken: String, accessSecret: String, consumerKey: String, consumerSecret: String, client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable {
            try {
                loginLinkedIn<User>(accessToken, accessSecret, consumerKey, consumerSecret, client as AbstractClient<User>, callback)
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
    @Ignore // need to change accessToken, refreshToken, clientID, ID
    @Throws(InterruptedException::class)
    fun testLoginSalesforceAsync() {
        val accessToken = "YOUR_ACCESS_TOKEN_HERE" //"00D6F000000Dct5!AQkAQFAWrMjHboaD6Yn71NezV9yizZiM_MJLodm.ppn7TgypzET20QagfusU7UCAJw7jbnjWxjsWpYI2Xoa82ehmJum65Phd";

        val refreshToken = "YOUR_REFRESH_TOKEN_HERE" //"5Aep861..zRMyCurAW3YNVSrR4jYtnt9rDCBsqQ.ytSywG1HaexWXOn07YXPwep1YmQVmuuc9YM8sWS8pyFbC2G";

        val clientID = "YOUR_CLIENT_ID_HERE" //"3MVG9YDQS5WtC11o5afZtRCMB4EGBMjwb0MfQOBSW2u2EZ5r6fHt_sXtYx9i2.nJIkhzicIPWpyhm1zc3HlWw";

        val ID = "YOUR_SALESFORCE_ID_HERE" //"https://login.salesforce.com/id/00D6F000000Dct5UAC/0056F000006Xw0jQAC";

        val userCallback = loginSalesforce(accessToken, refreshToken, clientID, ID, client)
        if (userCallback.error != null) {
            Log.d("test: ", userCallback.error?.message)
        }
        assertNotNull(userCallback.result)
        assertTrue(client?.isUserLoggedIn == true)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginSalesforceAsyncBad() {
        val accessToken = "wrongAccessToken"
        val refreshToken = "wrongRefreshToken"
        val clientID = "wrongClientID"
        val ID = "wrongID"
        val userCallback = loginSalesforce(accessToken, refreshToken, clientID, ID, client)
        assertNotNull(userCallback.error)
        assertFalse(client?.isUserLoggedIn == true)
    }

    @Throws(InterruptedException::class)
    private fun loginSalesforce(accessToken: String, refreshToken: String, clientID: String, ID: String, client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable {
            try {
                loginSalesForce<User>(accessToken, refreshToken, clientID, ID, client as AbstractClient<User>, callback)
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
    @Throws(InterruptedException::class, IOException::class)
    fun testAddUsersToGroupWrongCredentials() {
        val asyncUserGroup = client?.userGroup()
        login(client)
        val callback = addUsersWrongCredentials(asyncUserGroup)
        assertEquals((callback.error as KinveyJsonResponseException?)?.details?.error, INSUFFICIENT_CREDENTIAL_TYPE)
    }

    @Throws(InterruptedException::class)
    private fun addUsersWrongCredentials(asyncUserGroup: AsyncUserGroup?): UserGroupResponseCallback {
        val latch = CountDownLatch(1)
        val callback = UserGroupResponseCallback(latch)
        val looperThread = LooperThread(Runnable { asyncUserGroup?.addAllUsersToGroup("group", "group", callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testAddAddUserToGroupWrong() {
        val asyncUserGroup = client?.userGroup()
        login(TestManager.USERNAME, TestManager.PASSWORD)
        val callback = addAddUserToGroupWrong(asyncUserGroup)
        Assert.assertEquals((callback.error as KinveyJsonResponseException?)?.details?.error, UserStoreTest.INSUFFICIENT_CREDENTIAL_TYPE)
    }

    @Throws(InterruptedException::class)
    private fun addAddUserToGroupWrong(asyncUserGroup: AsyncUserGroup?): UserStoreTest.UserGroupResponseCallback {
        val ids = ArrayList<String>()
        ids.add("first")
        val latch = CountDownLatch(1)
        val callback = UserStoreTest.UserGroupResponseCallback(latch)
        val looperThread = LooperThread(Runnable {
            asyncUserGroup?.addUserToGroup("group","user", "group", callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testAddUsersIdsGroupIdWrong() {
        val asyncUserGroup = client?.userGroup()
        login(TestManager.USERNAME, TestManager.PASSWORD)
        val callback = addUsersIdsGroupIdWrong(asyncUserGroup)
        Assert.assertEquals((callback.error as KinveyJsonResponseException?)?.details?.error, UserStoreTest.INSUFFICIENT_CREDENTIAL_TYPE)
    }

    @Throws(InterruptedException::class)
    private fun addUsersIdsGroupIdWrong(asyncUserGroup: AsyncUserGroup?): UserStoreTest.UserGroupResponseCallback {
        val ids = ArrayList<String>()
        ids.add("first")
        val latch = CountDownLatch(1)
        val callback = UserStoreTest.UserGroupResponseCallback(latch)
        val looperThread = LooperThread(Runnable {
            asyncUserGroup?.addUserListToGroup("group", ids, "group", callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testAddUsersGroupAndUserWrong() {
        val asyncUserGroup = client?.userGroup()
        login(TestManager.USERNAME, TestManager.PASSWORD)
        val callback = addUsersGroupAndUserWrong(asyncUserGroup)
        Assert.assertEquals((callback.error as KinveyJsonResponseException?)?.details?.error, UserStoreTest.INSUFFICIENT_CREDENTIAL_TYPE)
    }

    @Throws(InterruptedException::class)
    private fun addUsersGroupAndUserWrong(asyncUserGroup: AsyncUserGroup?): UserStoreTest.UserGroupResponseCallback {
        val ids = ArrayList<String>()
        ids.add("first")
        val latch = CountDownLatch(1)
        val callback = UserStoreTest.UserGroupResponseCallback(latch)
        val looperThread = LooperThread(Runnable {
            asyncUserGroup?.addUserToGroupList("group", "user", ids, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testAddUsersGroupToGroupWrongCredentials() {
        val asyncUserGroup = client?.userGroup()
        login(TestManager.USERNAME, TestManager.PASSWORD)
        val callback = addUsersGroupWrongCredentials(asyncUserGroup)
        Assert.assertEquals((callback.error as KinveyJsonResponseException?)?.details?.error, UserStoreTest.INSUFFICIENT_CREDENTIAL_TYPE)
    }

    @Throws(InterruptedException::class)
    private fun addUsersGroupWrongCredentials(asyncUserGroup: AsyncUserGroup?): UserStoreTest.UserGroupResponseCallback {
        val ids = ArrayList<String>()
        ids.add("first")
        val latch = CountDownLatch(1)
        val callback = UserStoreTest.UserGroupResponseCallback(latch)
        val looperThread = LooperThread(Runnable {
            asyncUserGroup?.addUserListToGroupList("group", ids, ids, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testAddUsersGroupWrongCred() {
        val asyncUserGroup = client?.userGroup()
        login(TestManager.USERNAME, TestManager.PASSWORD)
        val callback = addUsersGroupWrongCred(asyncUserGroup)
        Assert.assertEquals((callback.error as KinveyJsonResponseException?)?.details?.error, UserStoreTest.INSUFFICIENT_CREDENTIAL_TYPE)
    }

    @Throws(InterruptedException::class)
    private fun addUsersGroupWrongCred(asyncUserGroup: AsyncUserGroup?): UserStoreTest.UserGroupResponseCallback {
        val ids = ArrayList<String>()
        ids.add("first")
        val latch = CountDownLatch(1)
        val callback = UserStoreTest.UserGroupResponseCallback(latch)
        val looperThread = LooperThread(Runnable {
            asyncUserGroup?.addAllUsersToGroupList("group", ids, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    fun testKinveyLiveServiceCallbackHandler() {
        val person = Person()
        val latch = CountDownLatch(1)
        val looperThread: LooperThread
        val callback =  object : KinveyDataStoreLiveServiceCallback<Person> {
            override fun onNext(next: Person) {
                person.intVal = 3
                latch.countDown()
            }
            override fun onError(e: Exception) {

            }
            override fun onStatus(status: KinveyLiveServiceStatus) {

            }
        }
        looperThread = LooperThread(Runnable {
            Assert.assertEquals(person.intVal, 0)
            val handler = KinveyLiveServiceCallbackHandler<Person>()
            handler.onNext(person, callback)
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        Assert.assertEquals(person.intVal, 3)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCreateGroup() {
        val asyncUserGroup = client?.userGroup()
        login(TestManager.USERNAME, TestManager.PASSWORD)
        val callback = createGroup(asyncUserGroup)
        assertNull(callback.error)
        Logger.INFO("CREATED group")
    }

    @Throws(InterruptedException::class)
    private fun createGroup(asyncUserGroup: AsyncUserGroup?): UserStoreTest.UserGroupResponseCallback {
        val latch = CountDownLatch(1)
        val callback = UserStoreTest.UserGroupResponseCallback(latch)
        val looperThread = LooperThread(Runnable { asyncUserGroup?.create(UserGroup.UserGroupRequest(), callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testRetrieveGroup() {
        val asyncUserGroup = client?.userGroup()
        login(TestManager.USERNAME, TestManager.PASSWORD)
        val callback = retrieveGroup(asyncUserGroup)
        assertNull(callback.error)
        Assert.assertEquals(callback.result?.id, "group")
        Logger.DEBUG("DEBUG group")
    }

    @Throws(InterruptedException::class)
    private fun retrieveGroup(asyncUserGroup: AsyncUserGroup?): UserStoreTest.UserGroupResponseCallback {
        val latch = CountDownLatch(1)
        val callback = UserStoreTest.UserGroupResponseCallback(latch)
        val looperThread = LooperThread(Runnable { asyncUserGroup?.retrieve("group", callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDeleteGroup() {
        val asyncUserGroup = client?.userGroup()
        login(TestManager.USERNAME, TestManager.PASSWORD)
        val callback = deleteGroup(asyncUserGroup)
        Assert.assertEquals((callback.error as KinveyJsonResponseException?)?.details?.error, UserStoreTest.INSUFFICIENT_CREDENTIAL_TYPE)
        Logger.TRACE("INSUFFICIENT_CREDENTIAL FOR DELETING group")
    }

    @Throws(InterruptedException::class)
    private fun deleteGroup(asyncUserGroup: AsyncUserGroup?): UserStoreTest.UserGroupResponseCallback {
        val latch = CountDownLatch(1)
        val callback = UserStoreTest.UserGroupResponseCallback(latch)
        val looperThread = LooperThread(Runnable { asyncUserGroup?.delete("group", callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUserDiscovery() {
        val asyncUserGroup = client?.userDiscovery()
        val userLookup = asyncUserGroup?.userLookup()
        userLookup?.id = "id"
        assertEquals("id", userLookup?.id)
        userLookup?.email = "email"
        assertEquals("email", userLookup?.email)
        userLookup?.firstName = "first"
        assertEquals("first", userLookup?.firstName)
        userLookup?.lastName = "last"
        assertEquals("last", userLookup?.lastName)
        userLookup?.facebookID = "facebook"
        assertEquals("facebook", userLookup?.facebookID)
        userLookup?.username = "username"
        assertEquals("username", userLookup?.username)
    }

    @Test
    @Ignore("Ignored in case of server issue")
    @Throws(InterruptedException::class, IOException::class)
    fun testMICRefreshTokenAfterRetrieve() {
        val redirectURI = "kinveyAuthDemo://"
        var refreshToken: String? = null
        var newRefreshToken: String? = null
        val userCallback = loginMICCodeApi(client, redirectURI)

        assertNull(userCallback.error)
        assertNotNull(userCallback.result)
        val cred = client?.store?.load(userCallback.result?.id)
        if (cred != null) {
            refreshToken = cred.refreshToken
        }
        val clientCallback = retrieveMICTest(client)
        val credNew = client?.store?.load(clientCallback.result?.id)
        if (credNew != null) {
            newRefreshToken = credNew.refreshToken
        }
        assertEquals(refreshToken, newRefreshToken)
    }

    @Throws(InterruptedException::class)
    fun loginMICCodeApi(client: Client<*>?, redirectUrl: String): DefaultKinveyUserCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyUserCallback(latch)
        val looperThread = LooperThread(Runnable { loginWithAuthorizationCodeAPI(client as AbstractClient<User>, USERNAME, PASSWORD, redirectUrl, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun retrieveMICTest(client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable { UserStore.retrieve(client as AbstractClient<User>, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(IOException::class)
    fun testSharedPrefCredentialStore() {
        val userId = "userId"
        val authotoken = "authotoken"
        val refresh = "refreshToken"
        val testCredential = Credential(userId, authotoken, refresh)
        val store = SharedPrefCredentialStore(client?.context!!)
        assertNull(store.load(userId))
        store.store(userId, testCredential)
        var emptyCred = Credential(null, null, null)
        emptyCred = store.load(userId) ?: emptyCred
        assertNotNull(emptyCred)
        assertEquals(emptyCred.userId, userId)
        assertEquals(emptyCred.authToken, authotoken)
        assertEquals(emptyCred.refreshToken, refresh)
        store.delete(userId)
        assertNull(store.load(userId))
    }

    @Test
    @Throws(InterruptedException::class, KinveyException::class)
    fun testMICErrorMockResponse() {
        if (client?.isUserLoggedIn == true) {
            logout(client)
        }
        val redirectURIMock = "kinveyauthdemo://?error=credentialError"
        val intent = Intent()
        intent.data = Uri.parse(redirectURIMock)
        try {
            onOAuthCallbackReceived(intent, null, client as AbstractClient<User>)
        } catch (e: KinveyException) {
            e.printStackTrace()
        }
        assertNull(client?.activeUser)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testMIC_LoginWithAuthorizationCodeLoginPage() {
        val redirectURI = "http://test.redirect"
        val userCallback = loginWithAuthorizationCodeLoginPage(redirectURI, client)
        assertNotNull(userCallback.myURLToRender)
        assertTrue(!userCallback.myURLToRender.isNullOrEmpty())
        assertTrue(userCallback.myURLToRender?.startsWith(client?.micHostName + client?.micApiVersion + "/oauth/auth?client_id") == true)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testImitationUnauthorizedForRefreshToken() {
        if (client?.activeUser == null) {
            val callback = login(USERNAME, PASSWORD)
            assertNull(callback.error)
            assertNotNull(callback.result)
        }
        val cred = client?.store?.load(client?.activeUser?.id)
        var refreshToken: String? = null
        if (cred != null) {
            refreshToken = cred.refreshToken
        }
        if (refreshToken != null) {
            cred?.refreshToken = null
        }
        assertTrue(refreshToken == null)
        val storeAuto = collection(EntitySet.COLLECTION, EntitySet::class.java, StoreType.NETWORK, client)
        val findCallback = findIdEntitySet(storeAuto, "testId", 60)
        Assert.assertNotNull(findCallback.error)
        Assert.assertTrue(findCallback.error?.message?.contains("InsufficientCredentials") == true)
    }

    @Throws(InterruptedException::class)
    private fun loginWithAuthorizationCodeLoginPage(redirectUrl: String, client: Client<*>?): DefaultKinveyMICCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyMICCallback(latch)
        val looperThread = LooperThread(Runnable { loginWithAuthorizationCodeLoginPage(client!!, redirectUrl, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test //can be failed if application doesn't have permission for MIC Login

    @Ignore
    @Throws(InterruptedException::class)
    fun testMIC_LoginWithAuthorizationCodeAPI() {
        val redirectURI = "kinveyAuthDemo://"
        val clientId = "clientId"
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        if (client?.isUserLoggedIn == true) {
            logout(client)
        }
        val userCallback = loginWithAuthorizationCodeAPIAsync(USERNAME, PASSWORD, clientId, redirectURI, client as Client<*>)
        assertNotNull(userCallback.result)
        logout(client)
    }

    @Throws(InterruptedException::class)
    private fun loginWithAuthorizationCodeAPIAsync(username: String?, password: String?,
                                                   clientId: String, redirectUrl: String, client: Client<*>): DefaultKinveyUserCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyUserCallback(latch)
        val looperThread = LooperThread(Runnable { loginWithAuthorizationCodeAPI(client as AbstractClient<User>, username!!, password!!, clientId, redirectUrl, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLogout() {
        login(USERNAME, PASSWORD, client)
        val personStore = collection(Person.USER_STORE, Person::class.java, StoreType.SYNC, client)
        val p = Person()
        p.username = "TestUser"
        save(personStore, p)
        val userStore = collection("users", Person::class.java, StoreType.SYNC, client)
        val p2 = Person()
        p2.username = "TestUser2"
        save(userStore, p2)
        assertNull(logout(client).error)
        assertTrue(client?.isUserLoggedIn == false)
        assertTrue(client?.syncManager?.getCount(Person.USER_STORE) == 0L)
    }

    @Throws(InterruptedException::class)
    private fun save(store: DataStore<Person>, person: Person): DefaultPersonKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultPersonKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable { store.save(person, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLogoutWithNoDatabaseTables() {
        login(USERNAME, PASSWORD, client)
        assertNull(logout(client).error)
        assertTrue(client?.isUserLoggedIn == false)
        assertTrue(client?.syncManager?.getCount(Person.USER_STORE) == 0L)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLogoutWithDatabaseTablesButNoAPICalls() {
        login(USERNAME, PASSWORD, client)
        val personStore = collection(Person.USER_STORE, Person::class.java, StoreType.SYNC, client)
        assertNull(logout(client).error)
        assertTrue(client?.isUserLoggedIn == false)
        assertTrue(client?.syncManager?.getCount(Person.USER_STORE) == 0L)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testLogoutWithDatabaseTablesWithAPICalls() {
        login(USERNAME, PASSWORD, client)
        val personStore = collection(Person.USER_STORE, Person::class.java, StoreType.SYNC, client)
        save(personStore, Person())
        assertNull(logout(client).error)
        assertTrue(client?.isUserLoggedIn == false)
        assertTrue(client?.syncManager?.getCount(Person.USER_STORE) == 0L)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testCreateUserAsync() {
        val callback = signUp(createRandomUserName("CreateUser"), PASSWORD, client)
        assertNotNull(callback.result)
        assertNotNull(callback.result?.username)
        assertTrue(client?.isUserLoggedIn == true)
        assertNotNull(callback.result?.username == USERNAME)
        assertNull(logout(client).error)
    }

    @Throws(InterruptedException::class)
    private fun signUp(user: String?, password: String, client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable { signUp(user!!, password, client as AbstractClient<User>, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun login(user: String, password: String, client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable {
            try {
                login<User>(user, password, client as AbstractClient<User>, callback)
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
    @Throws(InterruptedException::class)
    fun testDoesUsernameExist() {
        val user = login(USERNAME, PASSWORD, client).result
        val isNameExists = exists(user?.username, client).result
        assertTrue(isNameExists)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDoesUsernameExistBad() {
        val isNameExists = exists("wrong_username", client).result
        assertFalse(isNameExists)
    }

    @Throws(InterruptedException::class)
    private fun exists(username: String?, client: Client<*>?): DefaultKinveyBooleanCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyBooleanCallback(latch)
        val looperThread = LooperThread(Runnable { exists(username!!, client!!, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test // this test always creates new user, to be careful
    @Throws(InterruptedException::class)
    fun testForgotUsername() {
        val user = signUp(createRandomUserName("forgotUserName"), PASSWORD, client).result
        assertNotNull(user)
        assertNotNull(user?.username)
        assertTrue(client?.isUserLoggedIn == true)
        val isForgotten = forgot(user?.username, client).result
        assertTrue(isForgotten)
        deleteUser(true, client)
    }

    @Throws(InterruptedException::class)
    private fun forgot(username: String?, client: Client<*>?): DefaultKinveyUserManagementCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyUserManagementCallback(latch)
        val looperThread = LooperThread(Runnable { forgotUsername(client!!, username!!, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test // this test always creates new user, to be careful
    @Throws(InterruptedException::class)

    fun testDeleteUserSoftAsync() {
        val user = signUp(createRandomUserName("deleteUserSoft"), PASSWORD, client).result
        assertNotNull(user)
        assertNotNull(user?.id)
        assertNull(deleteUser(false, client).error)
    }

    @Throws(InterruptedException::class)
    private fun deleteUser(isHard: Boolean, client: Client<*>?): DefaultKinveyUserDeleteCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyUserDeleteCallback(latch)
        val looperThread = LooperThread(Runnable { destroy(isHard, client as AbstractClient<BaseUser>, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteUserHardAsync() {
        val user = signUp(createRandomUserName("deleteUserHard"), PASSWORD, client).result
        assertNotNull(user)
        assertNotNull(user?.id)
        assertNull(deleteUser(true, client).error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testUserEmailVerification() {
        val user = login(USERNAME, PASSWORD, client).result
        assertNotNull(user)
        assertNotNull(user?.id)
        val isEmailVerificationSent = sentEmailVerification(client).result
        assertTrue(isEmailVerificationSent)
        assertNull(logout(client).error)
    }

    @Throws(InterruptedException::class)
    private fun sentEmailVerification(client: Client<*>?): DefaultKinveyUserManagementCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyUserManagementCallback(latch)
        val looperThread = LooperThread(Runnable { sendEmailConfirmation(client!!, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testUserPasswordReset() {
        val user = login(USERNAME, PASSWORD, client).result
        assertNotNull(user)
        assertNotNull(user?.id)
        assertNotNull(user?.username)
        val isPasswordReset = resetPassword(USERNAME, client).result
        assertTrue(isPasswordReset)
        assertNull(logout(client).error)
    }

    @Throws(InterruptedException::class)
    private fun resetPassword(username: String?, client: Client<*>?): DefaultKinveyUserManagementCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyUserManagementCallback(latch)
        val looperThread = LooperThread(Runnable { resetPassword(username!!, client!!, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Ignore //need to check
    @Throws(InterruptedException::class)

    fun testUserInitFromCredential() {
        val mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        val localBuilder = Builder<User>(mMockContext)
        val localClient: Client<*> = localBuilder.build()
        val callback = login(USERNAME, PASSWORD, localClient)
        assertNotNull(callback.result)

        val activeUser = callback.result
        val mMockContext2 = InstrumentationRegistry.getInstrumentation().targetContext
        val localBuilder2 = Builder<User>(mMockContext2)
        val localClient2 = localBuilder2.build()
        assertNotNull(localClient2.activeUser)

        assertTrue(activeUser?.id == localClient2.activeUser?.id)
        assertTrue(activeUser?.username == localClient2.activeUser?.username)
        assertNull(logout(localClient).error)
        assertNull(logout(localClient2).error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSignUpIfUserLoggedIn() {
        val user = login(USERNAME, PASSWORD, client).result
        assertNotNull(user)
        assertNotNull(user?.id)
        assertTrue(client?.isUserLoggedIn == true)
        val callback = signUp()
        assertNull(callback.result)
        assertNotNull(callback.error)
        assertNull(logout(client).error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSignUpIfUserExists() {
        val user = signUp(createRandomUserName("TestSignUp"), PASSWORD, client).result
        assertNotNull(user)
        assertNotNull(user?.id)
        assertTrue(client?.isUserLoggedIn == true)
        assertNull(logout(client).error)
        assertFalse(client?.isUserLoggedIn == true)
        val callback = signUp(user?.username, PASSWORD, client)
        assertNull(callback.result)
        assertNotNull(callback.error)
        assertFalse(client?.isUserLoggedIn == true)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSignUpWithEmptyUsername() {
        val callback = signUp("", PASSWORD, client)
        assertNotNull(callback.result)
        assertNull(callback.error)
        assertTrue(client?.isUserLoggedIn == true)
        assertNull(logout(client).error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSignUpWithEmptyPassword() {
        val callback = signUp(createRandomUserName("Test123"), "", client)
        assertNotNull(callback.result)
        assertNull(callback.error)
        assertTrue(client?.isUserLoggedIn == true)
        assertNull(logout(client).error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testUpdate() {
        val callback = login(client)
        assertNotNull(callback.result)
        assertTrue(client?.isUserLoggedIn == true)
        val oldUserName = client?.activeUser?.username
        client?.activeUser?.username = "NewUserName2"
        val userKinveyClientCallback = update(client)
        assertNotNull(userKinveyClientCallback.result)
        assertNotEquals(oldUserName, userKinveyClientCallback.result?.username)
        assertNotNull(deleteUser(true, client))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testUpdateCustomUser() {
        client?.userClass = TestUser::class.java as Class<User>
        val user = TestUser()
        user.companyName = "Test Company"
        val internalUserEntity = InternalUserEntity()
        internalUserEntity.street = "TestStreet"
        user.internalUserEntity = internalUserEntity
        val callback = signUp(user)
        assertNull(callback.error)
        assertNotNull(callback.result)
        assertEquals(user.companyName, callback.result?.companyName, client?.activeUser!!["companyName"])

        client?.activeUser?.set("companyName", "New Company")
        (client?.activeUser as TestUser?)?.internalUserEntity?.street = "TestStreet2"
        val userKinveyClientCallback = updateCustomUser(client)
        assertNotNull(userKinveyClientCallback.result)
        assertEquals("New Company", userKinveyClientCallback.result?.companyName)
        assertEquals("TestStreet2", userKinveyClientCallback.result?.internalUserEntity?.street)
        assertNotNull(deleteUser(true, client))
    }

    @Throws(InterruptedException::class)
    private fun update(client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        Thread(Runnable {
            Looper.prepare()
            client?.activeUser?.update<User>(callback)
            Looper.loop()
        }).start()
        latch.await()
        return callback
    }

    @Throws(InterruptedException::class)
    private fun updateCustomUser(client: Client<*>?): CustomKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = CustomKinveyClientCallback(latch)
        Thread(Runnable {
            Looper.prepare()
            client?.activeUser?.update(callback)
            Looper.loop()
        }).start()
        latch.await()
        return callback
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUserDiscoveryLookup() {
        val asyncUserGroup = client?.userDiscovery()
        login(USERNAME, PASSWORD)
        val callback = lookupByFullNameDiscovery("first", "last", asyncUserGroup)
        assertNull(callback.error)
        assertNotNull(callback.result)
    }

    @Throws(InterruptedException::class)
    private fun lookupByFullNameDiscovery(firstname: String, lastname: String, asyncUserDiscovery: AsyncUserDiscovery?): DefaultKinveyUserListCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyUserListCallback(latch)
        val looperThread = LooperThread(Runnable { asyncUserDiscovery?.lookupByFullName(firstname, lastname, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUsernameDiscoveryLookup() {
        val asyncUserGroup = client?.userDiscovery()
        login(USERNAME, PASSWORD)
        val callback = lookupByUsernameDiscovery(USERNAME, asyncUserGroup)
        assertNotNull(callback.result)
        assertTrue(callback.result?.size ?: 0 > 0)
    }

    @Throws(InterruptedException::class)
    private fun lookupByUsernameDiscovery(name: String?, asyncUserDiscovery: AsyncUserDiscovery?): DefaultKinveyUserListCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyUserListCallback(latch)
        val looperThread = LooperThread(Runnable { asyncUserDiscovery!!.lookupByUserName(name!!, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFacebookIdDiscoveryLookup() {
        val asyncUserGroup = client?.userDiscovery()
        login(USERNAME, PASSWORD)
        val callback = lookupByFacebookIdDiscovery("testID", asyncUserGroup)
        assertNull(callback.error)
        assertNotNull(callback.result)
    }

    @Throws(InterruptedException::class)
    private fun lookupByFacebookIdDiscovery(id: String, asyncUserDiscovery: AsyncUserDiscovery?): DefaultKinveyUserListCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyUserListCallback(latch)
        val looperThread = LooperThread(Runnable { asyncUserDiscovery?.lookupByFacebookID(id, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    fun testLiveServiceUser() {
        if (client?.activeUser == null) {
            login(TestManager.USERNAME, TestManager.PASSWORD)
        }
        liveServiceCustomUser(client)
        Assert.assertTrue(LiveServiceRouter.instance?.isInitialized!!)
        liveServiceCustomUserUnregister(client)
        Assert.assertFalse(LiveServiceRouter.instance?.isInitialized!!)
    }

    @Throws(InterruptedException::class)
    private fun liveServiceCustomUserUnregister(client: Client<*>?): KinveyClientCallback<Void> {
        val latch = CountDownLatch(1)
        val callback = object: KinveyClientCallback<Void> {
            override fun onSuccess(result: Void?) {
                latch.countDown()
            }
            override fun onFailure(error: Throwable?) {
                latch.countDown()
            }
        }
        val looperThread = LooperThread(Runnable {
            client?.activeUser?.unregisterLiveService(callback)
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun liveServiceCustomUser(client: Client<*>?): KinveyClientCallback<Void> {
        val latch = CountDownLatch(1)
        val callback = object: KinveyClientCallback<Void> {
            override fun onSuccess(result: Void?) {
                latch.countDown()
            }
            override fun onFailure(error: Throwable?) {
                latch.countDown()
            }
        }
        val looperThread = LooperThread(Runnable {
            client?.activeUser?.registerLiveService(callback)
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRetrieve() {
        val loginCallback = login(USERNAME, PASSWORD)
        assertNull(loginCallback.error)
        assertNotNull(loginCallback.result)
        val retrieveCallback = retrieve(client)
        assertNull(retrieveCallback.error)
        assertNotNull(retrieveCallback.result)
        assertTrue(client?.isUserLoggedIn == true)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRetrieveCustomUser() {
        client?.userClass = TestUser::class.java as Class<User>
        val user = TestUser()
        val callback = signUp(user)
        assertNull(callback.error)
        assertNotNull(callback.result)
        val retrieveCallback = retrieve(client)
        assertNotNull(deleteUser(true, client))
        assertNull(retrieveCallback.error)
        assertNotNull(retrieveCallback.result)
    }

    @Throws(InterruptedException::class)
    private fun retrieve(client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        Thread(Runnable {
            Looper.prepare()
            UserStore.retrieve(client as AbstractClient<User>, callback)
            Looper.loop()
        }).start()
        latch.await()
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRetrieveUsers() {
        val loginCallback = login(USERNAME, PASSWORD)
        assertNull(loginCallback.error)
        assertNotNull(loginCallback.result)
        val retrieveCallback = retrieveUsers(client)
        assertNull(retrieveCallback.error)
        assertNotNull(retrieveCallback.result)
        assertTrue(client?.isUserLoggedIn == true)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRetrieveUsersArrayDeprecated() {
        val loginCallback = login(USERNAME, PASSWORD)
        assertNull(loginCallback.error)
        assertNotNull(loginCallback.result)
        val retrieveCallback = retrieveUsersDeprecated(client)
        assertNull(retrieveCallback.error)
        assertNotNull(retrieveCallback.result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRetrieveCustomUsers() {
        client?.userClass = TestUser::class.java as Class<User>
        val user = TestUser()
        val callback = signUp(user)
        assertNull(callback.error)
        assertNotNull(callback.result)
        val retrieveCallback = retrieveUsers(client)
        assertNotNull(deleteUser(true, client))
        assertNull(retrieveCallback.error)
        assertNotNull(retrieveCallback.result)
    }

    @Throws(InterruptedException::class)
    private fun retrieveUsers(client: Client<*>?): DefaultKinveyListCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyListCallback(latch)
        Thread(Runnable {
            Looper.prepare()
            val query = Query()
            retrieve(query, arrayOf(USERNAME, PASSWORD), client as AbstractClient<User>, callback)
            Looper.loop()
        }).start()
        latch.await()
        return callback
    }

    @Throws(InterruptedException::class)
    private fun retrieveUsersDeprecated(client: Client<*>?): DefaultKinveyUserListCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyUserListCallback(latch)
        Thread(Runnable {
            Looper.prepare()
            val query = Query()
            retrieve(query, arrayOf(USERNAME, PASSWORD), client as AbstractClient<User>, callback)
            Looper.loop()
        }).start()
        latch.await()
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRetrieveUsersList() {
        val loginCallback = login(USERNAME, PASSWORD)
        assertNull(loginCallback.error)
        assertNotNull(loginCallback.result)
        val retrieveUsersList = retrieveUsersList(client)
        assertNull(retrieveUsersList.error)
        assertNotNull(retrieveUsersList.result)
        assertNull(logout(client).error)
    }

    @Throws(InterruptedException::class)
    private fun retrieveUsersList(client: Client<*>?): DefaultKinveyListCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyListCallback(latch)
        Thread(Runnable {
            Looper.prepare()
            val query = Query()
            retrieve(query, client as AbstractClient<User>, callback)
            Looper.loop()
        }).start()
        latch.await()
        return callback
    }

    private fun createRandomUserName(testName: String): String {
        return testName + "_" + System.currentTimeMillis()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginMobileIdentityError() {
        val authToken = "WrongToken"
        val callback = loginMobileIdentity(authToken, client)
        assertNotNull(callback.error)
        assertFalse(client?.isUserLoggedIn == true)
    }

    @Throws(InterruptedException::class)
    private fun loginMobileIdentity(authToken: String, client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable {
            try {
                loginMobileIdentity<User>(authToken, client as AbstractClient<User>, callback)
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
    @Throws(InterruptedException::class, InvocationTargetException::class, NoSuchMethodException::class,
            ClassNotFoundException::class, InstantiationException::class, IllegalAccessException::class)
    fun testLoginByCredential() {
        val user = login(USERNAME, PASSWORD, client).result
        Thread.sleep(2000)// This is time for the SDK to write user's data to the Credential Store
        val credential = getCredential(client?.context, user?.id)
        logout(client)
        assertFalse(client?.isUserLoggedIn == true)
        assertNotNull(credential)
        val callback = loginCredential(credential, client)
        assertNull(callback.error)
        assertNotNull(callback.result)
        assertTrue(client?.isUserLoggedIn == true)
    }

    @Throws(IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class,
            NoSuchMethodException::class, ClassNotFoundException::class)
    private fun getCredential(context: Context?, userId: String?): Credential {
        val c = Class.forName("com.kinvey.android.AndroidCredentialStore")
        val constructor = c.getDeclaredConstructor(Context::class.java)
        constructor.isAccessible = true
        val obj = constructor.newInstance(context)
        val m1 = c.getDeclaredMethod("load", *arrayOf<Class<*>>(String::class.java))
        return m1.invoke(obj, userId) as Credential
    }

    @Test
    @Throws(InterruptedException::class, InvocationTargetException::class, NoSuchMethodException::class,
            ClassNotFoundException::class, InstantiationException::class, IllegalAccessException::class)
    fun testLoginByCredentialError() {
        val callback = loginCredential(null, client)
        assertNotNull(callback.error)
        assertNull(callback.result)
        assertFalse(client?.isUserLoggedIn == true)
    }

    @Throws(InterruptedException::class)
    private fun loginCredential(credential: Credential?, client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable {
            try {
                UserStore.login<User>(credential!!, client as AbstractClient<User>, callback)
            } catch (e: Throwable) {
                e.printStackTrace()
                callback.onFailure(e)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testChangePassword() {
        val userName = String.format("testUser123-%s", UUID.randomUUID())
        val newPassword = "testUser123Password"

        signUp(userName, PASSWORD, client)
        assertTrue(client?.isUserLoggedIn == true)

        val callback = changePassword(newPassword, client)
        assertNull(callback.error)
        assertTrue(callback.result)
        assertTrue(client?.isUserLoggedIn == true)

        logout(client)
        login(userName, PASSWORD, client)
        assertFalse(client?.isUserLoggedIn == true)

        login(userName, newPassword, client)
        assertTrue(client?.isUserLoggedIn == true)
        assertNull(deleteUser(true, client).error)
        assertFalse(client?.isUserLoggedIn == true)
    }

    @Throws(InterruptedException::class)
    private fun changePassword(password: String, client: Client<*>?): DefaultKinveyUserManagementCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyUserManagementCallback(latch)
        val looperThread = LooperThread(Runnable { changePassword(password, client!!, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testGetRequestError() {
        login(USERNAME, PASSWORD)
        val callback = get("unexistUser", client)
        assertNotNull(callback.error)
        assertNull(callback.result)
        assertEquals("This user does not exist for this app backend.",
                (callback.error as KinveyJsonResponseException?)?.details?.description)
    }

    @Throws(InterruptedException::class)
    private operator fun get(userId: String, client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable { get<User>(userId, client!!, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testConvenience() {
        val user = login(USERNAME, PASSWORD).result
        val callback = convenience(client)
        assertNull(callback.error)
        assertNotNull(callback.result)
        assertEquals(user?.id, callback.result?.id)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testConvenienceError() {
        assertFalse(client?.isUserLoggedIn == true)
        val callback = convenience(client)
        assertNotNull(callback.error)
        assertNull(callback.result)
        assertEquals("currentUser must not be null", callback.error?.message)
    }

    @Throws(InterruptedException::class)
    private fun convenience(client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable { convenience<User>(client as AbstractClient<User>, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRetrieveResolves() {
        val user = login(USERNAME, PASSWORD).result
        val callback = retrieve(arrayOf(USERNAME, PASSWORD), client)
        assertNull(callback.error)
        assertNotNull(callback.result)
        assertEquals(user?.id, callback.result?.id)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRetrieveResolvesError() {
        assertFalse(client?.isUserLoggedIn == true)
        val callback = retrieve(arrayOf(USERNAME, PASSWORD), client)
        assertNotNull(callback.error)
        assertNull(callback.result)
        assertEquals("currentUser must not be null", callback.error?.message)
    }

    @Throws(InterruptedException::class)
    private fun retrieve(resolves: Array<String>, client: Client<*>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable { retrieve<User>(resolves, client as AbstractClient<User>, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoginWithAuthorizationCodeAPIError() {
        login(USERNAME, PASSWORD, client)
        val callback = loginWithAuthorizationCodeAPI(USERNAME, PASSWORD, "someClientId", "redirectURI")
        assertNotNull(callback.error)
        assertNull(callback.result)
        assertTrue((callback.error as HttpResponseException?)?.content?.contains("Client authentication failed") == true)
    }

    @Throws(InterruptedException::class)
    private fun loginWithAuthorizationCodeAPI(username: String?, password: String?,
                                              clientId: String, redirectURI: String): DefaultKinveyUserCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyUserCallback(latch)
        val looperThread = LooperThread(Runnable { loginWithAuthorizationCodeAPI(client as AbstractClient<User>, username!!, password!!, clientId, redirectURI, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testUserInfoInRealmTable() {
        val callback = login(USERNAME, PASSWORD)
        assertNull(callback.error)
        assertNotNull(callback.result)
        val user = client?.cacheManager?.getCache(ACTIVE_USER_COLLECTION_NAME, User::class.java, Long.MAX_VALUE)?.get()?.get(0)
        assertNotNull(user?.username)
        assertEquals(USERNAME, user?.username)
        assertEquals(USERNAME, client?.activeUser?.username)
        assertNull(user?.authToken) // check that realm doesn't keep auth_token

        assertNull((user?.get(KMD) as Map<String?, String?>)[AUTH_TOKEN]) // check that realm doesn't keep auth_token

        assertNotNull((client?.activeUser?.get(KMD) as Map<String?, String?>)[AUTH_TOKEN]) // check that active user has auth_token

        assertNull(logout(client).error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLastLoginTime() {
        val user = login(USERNAME, PASSWORD).result
        assertNotNull(user?.getLastLoginTime())
        val callback = convenience(client)
        assertNull(callback.error)
        assertNotNull(callback.result)
        assertEquals(user?.id, callback.result?.id)
        assertNotNull(callback.result?.getLastLoginTime())
        assertEquals(user?.getLastLoginTime(), callback.result?.getLastLoginTime())
    }

    companion object {
        const val INSUFFICIENT_CREDENTIAL_TYPE = "InsufficientCredentials"
        private const val ACTIVE_USER_COLLECTION_NAME = "active_user_info"
    }
}