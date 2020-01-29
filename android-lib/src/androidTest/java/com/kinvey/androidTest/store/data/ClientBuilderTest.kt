package com.kinvey.androidTest.store.data

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.google.api.client.testing.http.MockHttpTransport
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.model.User
import com.kinvey.androidTest.model.TestUser
import com.kinvey.java.Constants
import com.kinvey.java.KinveyException
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Created by yuliya on 08/02/17.
 */

@RunWith(AndroidJUnit4::class)
class ClientBuilderTest {
    private var client: Client<*>? = null
    private var mContext: Context? = null
    @Before
    @Throws(InterruptedException::class, IOException::class)
    fun setUp() {
        mContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    @Throws(IOException::class)
    fun testSetDeltaSetCache() {
        assertTrue(Builder<User>(mContext).setDeltaSetCache(true) is Builder<*>)
    }

    @Test
    @Throws(IOException::class)
    fun testSetDeltaSetCacheTrue() {
        client = Builder<User>(mContext).setDeltaSetCache(true).build()
        assertTrue(client!!.isUseDeltaCache)
    }

    @Test
    @Throws(IOException::class)
    fun testSetDeltaSetCacheFalse() {
        client = Builder<User>(mContext).setDeltaSetCache(false).build()
        assertFalse(client?.isUseDeltaCache == true)
    }

    @Test
    @Throws(IOException::class)
    fun testSetDeltaSetCacheDefault() {
        client = Builder<User>(mContext).build()
        assertFalse(client?.isUseDeltaCache == true)
    }

    @Test
    @Throws(IOException::class)
    fun testSetUserClass() {
        client = Builder<TestUser>(mContext).setUserClass(TestUser::class.java).build()
        assertTrue(client?.userClass == TestUser::class.java)
    }

    @Test
    @Throws(IOException::class)
    fun testSetUserClassDefault() {
        client = Builder<User>(mContext).build()
        assertTrue(client?.userClass == User::class.java)
    }

    @Test
    @Throws(IOException::class)
    fun testSetBaseUrl() {
        val url = "https://base_url/"
        client = Builder<User>(mContext).setBaseUrl(url).build()
        assertTrue(client?.baseUrl == url)
    }

    @Test
    @Throws(IOException::class)
    fun testSetWrongBaseUrl() {
        val url = "base_url"
        try {
            client = Builder<User>(mContext).setBaseUrl(url).build()
            assertTrue(false)
        } catch (e: KinveyException) {
            assertNotNull(e.message?.contains("Kinvey requires `https` as the protocol when setting a base URL"))
        }
        assertNull(client)
    }

    @Test
    @Throws(IOException::class)
    fun testSetBaseUrlDefault() {
        client = Builder<User>(mContext).build()
        assertTrue(client?.baseUrl == BASE_URL_DEFAULT)
    }

    @Test
    @Throws(IOException::class)
    fun testBuilderConstructorsFirst() {
        client = Builder<User>(mContext).build()
        assertNotNull(client)
    }

    @Test
    @Throws(IOException::class)
    fun testBuilderConstructorsSecond() {
        client = Builder<User>(TEST_APP_KEY, TEST_APP_SECRET, mContext).build()
        assertNotNull(client)
    }

    @Test
    @Throws(IOException::class)
    fun testBuilderConstructorsThird() {
        client = Builder<User>(mContext, MockHttpTransport()).build()
        assertNotNull(client)
    }

    @Test
    @Throws(IOException::class)
    fun testBuilderConstructorsFourth() {
        client = Builder<User>(TEST_APP_KEY, TEST_APP_SECRET, mContext, MockHttpTransport()).build()
        assertNotNull(client)
    }

    @Test
    @Throws(IOException::class)
    fun testBuilderConstructorsFifth() {
        client = Builder<User>(mContext?.assets?.open(KINVEY_PROPERTIES), mContext).build()
        assertNotNull(client)
    }

    @Test
    @Throws(IOException::class)
    fun testBuilderConstructorsFifthCheckException() {
        try {
            client = Builder<User>(null, mContext).build()
            assertTrue(false)
        } catch (e: NullPointerException) {
            assertNotNull(e)
        }
        assertNull(client)
    }

    @Test
    @Throws(IOException::class)
    fun testBuilderConstructorsSixth() {
        client = Builder<User>(mContext?.assets?.open(KINVEY_PROPERTIES), MockHttpTransport(), mContext).build()
        assertNotNull(client)
    }

    @Test
    @Throws(IOException::class)
    fun testBuilderConstructorsSixthCheckException() {
        try {
            client = Builder<User>(mContext?.assets?.open(KINVEY_PROPERTIES + "_not exist"), MockHttpTransport(), mContext).build()
            assertTrue(false)
        } catch (e: FileNotFoundException) {
            assertNotNull(e)
        }
        assertNull(client)
    }

    @Test
    @Throws(IOException::class)
    fun testSetTimeoutRequest() {
        client = Builder<User>(mContext).setRequestTimeout(120000).build()
        assertNotNull(client)
        assertEquals(120000, client?.requestTimeout)
    }

    @Test
    @Throws(IOException::class)
    fun testSetInstanceId() {
        client = Builder<User>(mContext).setInstanceID("TestInstanceId").build()
        assertEquals(Constants.PROTOCOL_HTTPS + "TestInstanceId" + Constants.HYPHEN + Constants.HOSTNAME_API + "/", client?.baseUrl)
        assertEquals(Constants.PROTOCOL_HTTPS + "TestInstanceId" + Constants.HYPHEN + Constants.HOSTNAME_AUTH + "/", client?.micHostName)
    }

    @Test
    @Throws(IOException::class)
    fun testSetInstanceIdAndSetBaseUrl() {
        client = Builder<User>(mContext).setBaseUrl("https://baseurl.com").setInstanceID("TestInstanceId").build()
        assertEquals(Constants.PROTOCOL_HTTPS + "TestInstanceId" + Constants.HYPHEN + Constants.HOSTNAME_API + "/", client?.baseUrl)
        assertEquals(Constants.PROTOCOL_HTTPS + "TestInstanceId" + Constants.HYPHEN + Constants.HOSTNAME_AUTH + "/", client?.micHostName)
    }

    companion object {
        private const val BASE_URL_DEFAULT = "https://baas.kinvey.com/"
        private const val TEST_APP_KEY = "app_key"
        private const val TEST_APP_SECRET = "app_secret"
        private const val KINVEY_PROPERTIES = "kinvey.properties"
    }
}