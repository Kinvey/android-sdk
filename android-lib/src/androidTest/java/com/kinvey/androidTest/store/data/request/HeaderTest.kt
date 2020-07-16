package com.kinvey.androidTest.store.data.request

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.google.api.client.json.GenericJson
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.model.User
import com.kinvey.androidTest.TestManager
import com.kinvey.java.AbstractClient
import com.kinvey.java.auth.KinveyAuthRequest
import com.kinvey.java.core.KinveyHeaders
import com.kinvey.java.store.UserStoreRequestManager
import com.kinvey.java.store.UserStoreRequestManager.LoginRequest
import junit.framework.Assert.*
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.lang.reflect.Field

@RunWith(AndroidJUnit4::class)
class HeaderTest {
    private var client: Client<*>? = null
    private var testManager: TestManager<*>? = null
    @Before
    @Throws(InterruptedException::class)
    fun setUp() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        testManager = TestManager<GenericJson>()
        if (client?.isUserLoggedIn == true) {
            testManager?.logout(client)
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
    fun kinveyApiVersionDefaultTest() {
        val headers = KinveyHeaders()
        var field: Field? = null
        try {
            field = KinveyHeaders::class.java.getDeclaredField("kinveyApiVersion")
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
        assert(field != null)
        field?.isAccessible = true
        var version = "0"
        try {
           version =  field?.get(headers) as String
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        assertNotEquals(version, "0")
        if (AbstractClient.kinveyApiVersion.isEmpty()) {
            assertEquals(version, "5")
        }
    }

    @Test
    @Throws(IOException::class, NoSuchFieldException::class, IllegalAccessException::class)
    fun testHeader() {
        val user = UserStoreRequestManager<User>(client, testManager?.createBuilder(client) as KinveyAuthRequest.Builder<User>)
        assertNotNull(user)
        val loginRequest = user.createBlocking("test_name", "test_login").buildAuthRequest()
        val kinveyAuthRequestField: Field = loginRequest.javaClass.getDeclaredField("request") //NoSuchFieldException

        kinveyAuthRequestField.isAccessible = true
        val request = kinveyAuthRequestField.get(loginRequest) as KinveyAuthRequest<*>
        val kinveyHeadersField = request.javaClass.getDeclaredField("kinveyHeaders") //NoSuchFieldException

        kinveyHeadersField.isAccessible = true
        val kinveyHeaders = kinveyHeadersField.get(request) as KinveyHeaders
        val header = kinveyHeaders["x-kinvey-device-info"] as String
        assertTrue(header.contains("hv"))
        assertTrue(header.contains("md"))
        assertTrue(header.contains("os"))
        assertTrue(header.contains("ov"))
        assertTrue(header.contains("sdk"))
        assertTrue(header.contains("pv"))
        assertTrue(header.contains("id"))
        assertTrue(header.contains("{"))
        assertTrue(header.contains("}"))
        assertTrue(header.contains("Android"))
        assertTrue(header.contains(client!!.deviceId))
    }
}