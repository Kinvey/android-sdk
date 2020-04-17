package com.kinvey.androidTest.store.data.request

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore
import com.kinvey.android.store.DataStore.Companion.collection
import com.kinvey.androidTest.TestManager
import com.kinvey.androidTest.TestManager.Companion.PASSWORD
import com.kinvey.androidTest.TestManager.Companion.TEST_USERNAME
import com.kinvey.androidTest.TestManager.Companion.USERNAME
import com.kinvey.androidTest.model.Person
import com.kinvey.java.store.StoreType
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Created by yuliya on 1/5/17.
 */

@RunWith(AndroidJUnit4::class)
@SmallTest
class RequestTest {
    private var client: Client<*>? = null
    private var testManager: TestManager<Person>? = null
    private var store: DataStore<Person>? = null
    @Before
    @Throws(InterruptedException::class, IOException::class)
    fun setUp() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        testManager = TestManager()
        testManager?.login(USERNAME, PASSWORD, client)
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
    @Throws(InterruptedException::class, IOException::class)
    fun testDefaultTimeOutValue() {
        store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager?.cleanBackend(store, StoreType.SYNC)
        client?.enableDebugLogging()
        assertEquals(DEFAULT_TIMEOUT, client?.requestTimeout)
        val person = Person()
        person.username = TEST_USERNAME
        val callback = testManager?.save(store, person)
        assertNotNull(callback)
        assertNotNull(callback?.result)
        assertNull(callback?.error)
        assertNotNull(callback?.result?.username)
        assertEquals(TEST_USERNAME, callback?.result?.username)
        val pushCallback = testManager?.push(store)
        assertNotNull(pushCallback)
        assertNotNull(pushCallback?.result)
        assertNull(pushCallback?.error)
        assertEquals(1, pushCallback?.result?.successCount)
    }

    @Test
    @Ignore
    @Throws(InterruptedException::class, IOException::class)
    fun testSetCustomTimeoutAndCheckException() {
        store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager?.cleanBackend(store, StoreType.SYNC)
        assertEquals(DEFAULT_TIMEOUT, client?.requestTimeout)
        client?.requestTimeout = 1
        assertEquals(1, client?.requestTimeout)
        val person = Person()
        person.username = TEST_USERNAME
        val callback = testManager?.save(store, person)
        assertNotNull(callback)
        assertNotNull(callback?.result)
        assertNull(callback?.error)
        assertNotNull(callback?.result?.username)
        assertEquals(TEST_USERNAME, callback?.result?.username)
        val pushCallback = testManager?.push(store)
        assertNotNull(pushCallback)
        assertNull(pushCallback?.result)
        assertNotNull(pushCallback?.error)
        assertEquals("SocketTimeoutException", pushCallback?.error?.javaClass?.simpleName)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSetCustomTimeout() {
        store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager?.cleanBackend(store, StoreType.SYNC)
        assertEquals(DEFAULT_TIMEOUT, client?.requestTimeout)
        client?.requestTimeout = DEFAULT_TIMEOUT * 2
        assertEquals(DEFAULT_TIMEOUT * 2, client?.requestTimeout)
        val person = Person()
        person.username = TEST_USERNAME
        val callback = testManager?.save(store, person)
        assertNotNull(callback)
        assertNotNull(callback?.result)
        assertNull(callback?.error)
        assertNotNull(callback?.result?.username)
        assertEquals(TEST_USERNAME, callback?.result?.username)
        val pushCallback = testManager?.push(store)
        assertNotNull(pushCallback)
        assertNotNull(pushCallback?.result)
        assertNull(pushCallback?.error)
        assertEquals(1, pushCallback?.result?.successCount)
    }

    companion object {
        private const val DEFAULT_TIMEOUT = 60 * 1000
    }
}