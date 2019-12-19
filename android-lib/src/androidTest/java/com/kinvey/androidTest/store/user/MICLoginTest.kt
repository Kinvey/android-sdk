package com.kinvey.androidTest.store.user

import android.os.Looper
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.google.api.client.http.UrlEncodedContent
import com.google.api.client.json.JsonFactory
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.callback.KinveyMICCallback
import com.kinvey.android.model.User
import com.kinvey.android.store.UserStore.Companion.loginWithAuthorizationCodeLoginPage
import com.kinvey.androidTest.TestManager.PASSWORD
import com.kinvey.androidTest.TestManager.USERNAME
import com.kinvey.androidTest.model.TestUser
import com.kinvey.java.AbstractClient
import com.kinvey.java.auth.Credential
import com.kinvey.java.auth.KinveyAuthRequest
import com.kinvey.java.store.UserStoreRequestManager
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@SmallTest
class MICLoginTest {

    private var client: Client<TestUser>? = null

    @Before
    @Throws(InterruptedException::class)
    fun setup() {
        val mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<TestUser>(APP_KEY, APP_SECRET, mMockContext).setUserClass(TestUser::class.java).build()
    }

    // Check clientId in auth link for MICLoginPage
    @Test
    @Throws(InterruptedException::class)
    fun testMIC_LoginWithAuthorizationCodeLoginPageWithClientId() {
        val userCallback = loginWithAuthorizationCodeLoginPage(CLIENT_ID, REDIRECT_URI, client)
        assertNotNull(userCallback.myURLToRender)
        assertTrue(!userCallback.myURLToRender.isNullOrEmpty())
        assertTrue(userCallback.myURLToRender?.startsWith(client?.micHostName + client?.micApiVersion + "/oauth/auth?client_id=" + APP_KEY + "." + CLIENT_ID) == true)
    }

    // Check clientId (should be absent second part of client_id) in auth link for MICLoginPage
    @Test
    @Throws(InterruptedException::class)
    fun testMIC_LoginWithAuthorizationCodeLoginPage() {
        val userCallback = loginWithAuthorizationCodeLoginPage(null, REDIRECT_URI, client)
        assertNotNull(userCallback.myURLToRender)
        assertTrue(!userCallback.myURLToRender.isNullOrEmpty())
        assertTrue(userCallback.myURLToRender?.startsWith(client?.micHostName + client?.micApiVersion + "/oauth/auth?client_id=" + APP_KEY + "&") == true)
    }

    // Check that myURLToRender contains openId parameter
    @Test
    @Throws(InterruptedException::class)
    fun testOpenIdExists() {
        val userCallback = loginWithAuthorizationCodeLoginPage(null, REDIRECT_URI, client)
        assertNotNull(userCallback.myURLToRender)
        assertTrue(!userCallback.myURLToRender.isNullOrEmpty())
        assertTrue(userCallback.myURLToRender?.endsWith("&response_type=code" + "&scope=openid") == true)
    }

    @Throws(InterruptedException::class)
    private fun loginWithAuthorizationCodeLoginPage(clientId: String?, redirectUrl: String, client: Client<*>?): DefaultKinveyMICCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyMICCallback(latch)
        Thread(Runnable {
            Looper.prepare()
            loginWithAuthorizationCodeLoginPage(client!!, clientId, redirectUrl, callback)
            Looper.loop()
        }).start()
        latch.await()
        return callback
    }

    // Check clientId for getting Temp Link
    @Test
    @Throws(IOException::class)
    fun testGetMICTempURLWithClientId() {
        val requestManager = UserStoreRequestManager(client, createBuilder(client) as KinveyAuthRequest.Builder<TestUser>)
        requestManager.micRedirectURI = REDIRECT_URI
        val micTempURL = requestManager.getMICTempURL(CLIENT_ID)
        assertTrue(micTempURL.uriTemplate?.endsWith("scope=openid") == true)
        assertEquals("$APP_KEY.$CLIENT_ID", ((micTempURL.httpContent as UrlEncodedContent?)?.data as HashMap<*, *>)[CLIENT_ID_FIELD])
    }

    @Test
    @Throws(IOException::class)
    fun testLoginToTempURL() {
        val requestManager = UserStoreRequestManager<TestUser>(client, createBuilder(client) as KinveyAuthRequest.Builder<TestUser>)
        val loginToTempURL = requestManager.MICLoginToTempURL(USERNAME, PASSWORD, CLIENT_ID, "tempURL")
        requestManager.micRedirectURI = REDIRECT_URI
        val micTempURL = requestManager.getMICTempURL(CLIENT_ID)
        assertTrue(micTempURL.uriTemplate?.endsWith("scope=openid") == true)
        assertEquals("$APP_KEY.$CLIENT_ID", ((loginToTempURL.httpContent as UrlEncodedContent?)?.data as HashMap<*, *>)[CLIENT_ID_FIELD])
    }

    // Check clientId (should be absent second part of client_id) for getting Temp Link
    @Test
    @Throws(IOException::class)
    fun testGetMICTempURL() {
        val requestManager = UserStoreRequestManager(client, createBuilder(client) as KinveyAuthRequest.Builder<TestUser>)
        requestManager.micRedirectURI = REDIRECT_URI
        val micTempURL = requestManager.getMICTempURL(null)
        assertEquals(APP_KEY, ((micTempURL.httpContent as UrlEncodedContent?)?.data as HashMap<*, *>)[CLIENT_ID_FIELD])
    }

    // Check clientId for using refresh token
    @Test
    @Throws(IOException::class)
    fun testMICLoginUseRefreshTokenWithClientId() {
        val requestManager = UserStoreRequestManager(client, createBuilder(client) as KinveyAuthRequest.Builder<TestUser>)
        val user = TestUser()
        user.id = "userId"
        client?.activeUser = user
        val credential = Credential(client?.activeUser?.id, AUTH_TOKEN, REFRESH_TOKEN)
        credential.clientId = CLIENT_ID
        client?.store?.store(client?.activeUser?.id, credential)
        val getMICAccessToken = requestManager.useRefreshToken(REFRESH_TOKEN)
        assertEquals("$APP_KEY.$CLIENT_ID", ((getMICAccessToken.httpContent as UrlEncodedContent?)?.data as HashMap<*, *>)[CLIENT_ID_FIELD])
    }

    // Check clientId (should be absent second part of client_id) for using refresh token
    @Test
    @Throws(IOException::class)
    fun testMICLoginUseRefreshToken() {
        val requestManager = UserStoreRequestManager(client, createBuilder(client) as KinveyAuthRequest.Builder<TestUser>)
        val user = TestUser()
        user.id = "userId"
        client?.activeUser = user
        val credential = Credential(client?.activeUser?.id, AUTH_TOKEN, REFRESH_TOKEN)
        client?.store?.store(client?.activeUser?.id, credential)
        val getMICAccessToken = requestManager.useRefreshToken(REFRESH_TOKEN)
        assertEquals(APP_KEY, ((getMICAccessToken.httpContent as UrlEncodedContent?)?.data as HashMap<*, *>)[CLIENT_ID_FIELD])
    }

    private fun createBuilder(client: AbstractClient<*>?): KinveyAuthRequest.Builder<*> {
        return KinveyAuthRequest.Builder<TestUser>(client?.requestFactory?.transport,
                client?.jsonFactory as JsonFactory, client?.baseUrl, APP_KEY, APP_SECRET, null)
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

    @Test
    @Throws(IOException::class)
    fun testMICDefaultAPIVersion() {
        assertEquals("v3", client?.micApiVersion)
    }

    @Test
    @Throws(IOException::class)
    fun testMICCustomAPIVersion() {
        client?.micApiVersion = "1"
        assertEquals("v1", client?.micApiVersion)
    }

    companion object {
        private const val APP_KEY = "YOUR_APP_KEY_HERE"
        private const val APP_SECRET = "YOUR_APP_SECRET_HERE"
        private const val CLIENT_ID = "CLIENT_ID"
        private const val REFRESH_TOKEN = "refresh_token"
        private const val AUTH_TOKEN = "auth_token"
        private const val REDIRECT_URI = "http://test.redirect"
        private const val CLIENT_ID_FIELD = "client_id"
    }
}