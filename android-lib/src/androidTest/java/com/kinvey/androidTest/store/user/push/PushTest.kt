package com.kinvey.androidTest.store.user.push

import android.app.Application
import android.os.Message
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.google.api.client.json.GenericJson
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.model.User
import com.kinvey.android.push.AbstractPush
import com.kinvey.android.push.AbstractPush.*
import com.kinvey.android.push.FCMPush
import com.kinvey.android.push.FCMPush.PushConfig
import com.kinvey.android.push.FCMPush.PushConfigField
import com.kinvey.android.push.KinveyFCMService
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.TestManager
import com.kinvey.java.KinveyException
import com.kinvey.java.core.KinveyClientCallback
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@SmallTest
class PushTest {

    private var client: Client<*>? = null
    private var testManager: TestManager<*>? = null

    @Before
    fun setUp() {
        val mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        client?.enableDebugLogging()
        testManager = TestManager<GenericJson>()
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
    fun testFCMPushDefaultValues() {
        val senderIds = arrayOf(ID_1, ID_2)
        val push = FCMPush(client, false, senderIds)
        assertFalse(push.isInProduction)
        assertFalse(push.isPushEnabled)
        assertEquals(senderIds, push.senderIDs)
        assertEquals("", push.pushId)
        //assertEquals("com.kinvey.android.push.AbstractPush", FCMPush::class.java.name)
        push.pushServiceClass = FCMService::class.java
        assertEquals(FCMService::class.java.name, push.pushServiceClass?.name)
        var method: Method? = null
        try {
            method = AbstractPush::class.java.getDeclaredMethod("getClient")
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
        assert(method != null)
        method?.isAccessible = true
        var client: Client<*>? = null
        try {
            client = method?.invoke(push) as Client<*>
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        assertNotNull(client)
    }

    @Test
    fun testPushRegistrationConstructors() {
        val pushRegistration = PushRegistration()
        assertNotNull(pushRegistration)
        val pushRegistration2 = PushRegistration(DEVICE_ID)
        assertNotNull(pushRegistration2)
    }

    @Test
    fun testPushRegistrationServiceParameter() {
        val pushRegistration = PushRegistration()
        assertNotNull(pushRegistration)
        assertTrue(pushRegistration.containsKey("service"))
        assertTrue(pushRegistration.containsValue("firebase"))
    }

    @Test
    fun testRegisterPushRequestConstructor() {
        val push = createFCMPush()
        var method: Method? = null
        try {
            method = AbstractPush::class.java.getDeclaredMethod("createRegisterPushRequest", PushRegistration::class.java)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
        assert(method != null)
        method?.isAccessible = true
        var registerPush: RegisterPush? = null
        try {
            registerPush = method?.invoke(push, PushRegistration(DEVICE_ID)) as RegisterPush
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        assertNotNull(registerPush)
    }

    @Test
    fun testUnRegisterPushRequestConstructor() {
        val push = FCMPush(client, false, arrayOf(ID_1, ID_2))
        var method: Method? = null
        try {
            method = AbstractPush::class.java.getDeclaredMethod("createUnregisterPushRequest", PushRegistration::class.java)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
        assert(method != null)
        method?.isAccessible = true
        var unregisterPush: UnregisterPush? = null
        try {
            unregisterPush = method?.invoke(push, PushRegistration(DEVICE_ID)) as UnregisterPush
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        assertNotNull(unregisterPush)
    }

    @Test
    fun testKinveyFCMServiceConstructor() {
        val fcmService: KinveyFCMService = FCMService()
        assertNotNull(fcmService)
    }

    @Test
    fun testPushConfigField() {
        val configField = PushConfigField()
        assertNotNull(configField)
        val senderIds = arrayOf(ID_1, ID_2)
        configField.ids = senderIds
        assertEquals(senderIds, configField.ids)
        configField.notificationKey = "NotificationKey"
        assertEquals("NotificationKey", configField.notificationKey)
    }

    @Test
    fun testPushConfig() {
        val pushConfig = PushConfig()
        assertNotNull(pushConfig)
        val senderIds = arrayOf(ID_1, ID_2)
        val configField = PushConfigField()
        pushConfig.gcm = configField
        assertEquals(configField, pushConfig.gcm)
        val configField2 = PushConfigField()
        pushConfig.gcmDev = configField2
        assertEquals(configField2, pushConfig.gcmDev)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFCMPushUserIsNotLoggedIn() {
        if (client?.isUserLoggedIn == true) {
            testManager?.logout(client)
        }
        try {
            client?.push(FCMService::class.java)?.initialize(InstrumentationRegistry.getInstrumentation().context.applicationContext as Application)
            assertTrue(false)
        } catch (ex: KinveyException) {
            assertNotNull(ex)
            assertEquals("No user is currently logged in", ex.reason)
        }
    }

    private fun createFCMPush(): FCMPush {
        return FCMPush(client, false, arrayOf(ID_1, ID_2))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testAsyncEnablePushRequestConstructor() {
        val latch = CountDownLatch(1)
        val looperThread = LooperThread(Runnable {
            val push = FCMPush(client, false, arrayOf(ID_1, ID_2))
            var method: Method? = null
            try {
                method = FCMPush::class.java.getDeclaredMethod("createAsyncEnablePushRequest", KinveyClientCallback::class.java, String::class.java)
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            }
            assert(method != null)
            method?.isAccessible = true
            var enablePushRequest: Any? = null
            try {
                enablePushRequest = method?.invoke(push, null, DEVICE_ID)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
            assertNotNull(enablePushRequest)
            latch.countDown()
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    @Test
    @Throws(InterruptedException::class)
    fun testAsyncDisablePushRequestConstructor() {
        val latch = CountDownLatch(1)
        val looperThread = LooperThread(Runnable {
            val push = FCMPush(client, false, arrayOf(ID_1, ID_2))
            var method: Method? = null
            try {
                method = FCMPush::class.java.getDeclaredMethod("createAsyncDisablePushRequest", KinveyClientCallback::class.java, String::class.java)
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            }
            assert(method != null)
            method?.isAccessible = true
            var enablePushRequest: Any? = null
            try {
                enablePushRequest = method?.invoke(push, null, DEVICE_ID)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
            assertNotNull(enablePushRequest)
            latch.countDown()
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    companion object {
        private const val ID_1 = "id1"
        private const val ID_2 = "id2"
        private const val DEVICE_ID = "DeviceID"
    }
}