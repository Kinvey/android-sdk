package com.kinvey.androidTest.store.data.cache

import android.content.Context
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore
import com.kinvey.android.store.DataStore.Companion.collection
import com.kinvey.androidTest.TestManager
import com.kinvey.androidTest.TestManager.*
import com.kinvey.androidTest.callback.DefaultKinveyClientCallback
import com.kinvey.androidTest.callback.DefaultKinveyReadCallback
import com.kinvey.androidTest.model.Person
import com.kinvey.java.store.StoreType
import junit.framework.Assert.*
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
@SmallTest
class TempIdTest {

    private var client: Client<*>? = null
    private var testManager: TestManager<Person>? = null
    private var store: DataStore<Person>? = null

    @Before
    @Throws(InterruptedException::class)
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

    /* check that temp item is created with 'temp_' prefix for local keeping and after pushing, item id is updated */
    @Test
    @Throws(InterruptedException::class)
    fun testCreatingPrefixForTempId() {
        store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager?.cleanBackend(store, StoreType.SYNC)

        val person = Person(TEST_USERNAME)
        val saveCallback = testManager?.save(store, person)
        assertNotNull(saveCallback)
        assertNull(saveCallback?.error)
        assertNotNull(saveCallback?.result)
        val savedPerson = saveCallback?.result
        assertTrue(savedPerson?.id?.startsWith(TEMP_ID) == true)

        var readCallback = testManager?.find(store, client?.query())
        assertNotNull(readCallback)
        assertNull(readCallback?.error)
        assertNotNull(readCallback?.result?.result)
        assertEquals(1, readCallback?.result?.result?.size)
        var fromCache = readCallback?.result?.result?.get(0)
        assertTrue(fromCache?.id?.startsWith(TEMP_ID) == true)
        assertEquals(savedPerson?.id, fromCache?.id)

        testManager?.push(store)
        readCallback = testManager?.find(store, client?.query())
        assertNotNull(readCallback)
        assertNull(readCallback?.error)
        assertNotNull(readCallback?.result?.result)
        assertEquals(1, readCallback?.result?.result?.size)
        fromCache = readCallback?.result?.result?.get(0)
        assertFalse(fromCache?.id?.startsWith(TEMP_ID) == true)
        assertNotEquals(savedPerson?.id, fromCache?.id)
        assertEquals(0L, client?.syncManager?.getCount(Person.COLLECTION))
    }

    /* check that old temp item which was created without 'temp_' prefix for local keeping will be pushed as expect */
    @Test
    @Throws(InterruptedException::class)
    fun testBackwardCompatibility() {

        store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager?.cleanBackend(store, StoreType.SYNC)

        val person = Person(TEST_USERNAME)
        person.id = UUID.randomUUID().toString()
        val saveCallback = testManager?.save(store, person)
        assertNotNull(saveCallback)
        assertNull(saveCallback?.error)
        assertNotNull(saveCallback?.result)
        val savedPerson = saveCallback?.result
        assertFalse(savedPerson?.id?.startsWith(TEMP_ID) == true)
        assertEquals(person.id, savedPerson?.id)

        var readCallback = testManager?.find(store, client?.query())
        assertNotNull(readCallback)
        assertNull(readCallback?.error)
        assertNotNull(readCallback?.result?.result)
        assertEquals(1, readCallback?.result?.result?.size)
        var fromCache = readCallback?.result?.result?.get(0)
        assertFalse(fromCache?.id?.startsWith(TEMP_ID) == true)
        assertEquals(person.id, fromCache?.id)
        testManager?.push(store)

        readCallback = testManager?.find(store, client?.query())
        assertNotNull(readCallback)
        assertNull(readCallback?.error)
        assertNotNull(readCallback?.result?.result)
        assertEquals(1, readCallback?.result?.result?.size)
        fromCache = readCallback?.result?.result?.get(0)
        assertFalse(fromCache?.id?.startsWith(TEMP_ID) == true)
        assertEquals(person.id, fromCache?.id)
        assertEquals(0L, client?.syncManager?.getCount(Person.COLLECTION))
    }

    /* check that old temp item which was created without 'temp_' prefix
    and item with 'temp_' prefix  will be pushed booth as expect */
    @Test
    @Throws(InterruptedException::class)
    fun testMixItemsWithOldAndNewTempIdPatterns() {

        store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager?.cleanBackend(store, StoreType.SYNC)

        val person = Person(TEST_USERNAME)
        person.id = UUID.randomUUID().toString()
        val person2 = Person(TEST_USERNAME_2)

        var saveCallback = testManager?.save(store, person)
        assertNotNull(saveCallback)
        assertNull(saveCallback?.error)
        assertNotNull(saveCallback?.result)
        val savedPerson = saveCallback?.result

        saveCallback = testManager?.save(store, person2)
        assertNotNull(saveCallback)
        assertNull(saveCallback?.error)
        assertNotNull(saveCallback?.result)
        val savedPerson2 = saveCallback?.result

        assertFalse(savedPerson?.id?.startsWith(TEMP_ID) == true)
        assertEquals(person.id, savedPerson?.id)
        assertTrue(savedPerson2?.id?.startsWith(TEMP_ID) == true)
        assertEquals(person2.id, savedPerson2?.id)

        var readCallback = testManager?.find(store, client?.query())
        assertNotNull(readCallback)
        assertNull(readCallback?.error)
        assertNotNull(readCallback?.result?.result)
        assertEquals(2, readCallback?.result?.result?.size)
        assertEquals(2L, client?.syncManager?.getCount(Person.COLLECTION))

        readCallback = testManager?.find(store, client?.query()?.equals("username", TEST_USERNAME))
        assertNotNull(readCallback)
        assertNull(readCallback?.error)
        assertNotNull(readCallback?.result?.result)
        assertEquals(1, readCallback?.result?.result?.size)
        val fromCache = readCallback?.result?.result?.get(0)
        assertFalse(fromCache?.id?.startsWith(TEMP_ID) == true)
        assertEquals(person.id, fromCache?.id)

        readCallback = testManager?.find(store, client?.query()?.equals("username", TEST_USERNAME_2))
        assertNotNull(readCallback)
        assertNull(readCallback?.error)
        assertNotNull(readCallback?.result?.result)
        assertEquals(1, readCallback?.result?.result?.size)
        val fromCache2 = readCallback?.result?.result?.get(0)
        assertTrue(fromCache2?.id?.startsWith(TEMP_ID) == true)
        assertEquals(person2.id, fromCache2?.id)
        testManager?.push(store)

        readCallback = testManager?.find(store, client?.query())
        assertNotNull(readCallback)
        assertNull(readCallback?.error)
        assertNotNull(readCallback?.result?.result)
        assertEquals(2, readCallback?.result?.result?.size)
        assertFalse(readCallback?.result?.result?.get(0)?.id?.startsWith(TEMP_ID) == true)
        assertFalse(readCallback?.result?.result?.get(1)?.id?.startsWith(TEMP_ID) == true)
        assertEquals(0L, client?.syncManager?.getCount(Person.COLLECTION))
    }

    companion object {
        private const val TEMP_ID = "temp_"
    }
}