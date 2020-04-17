package com.kinvey.androidTest.store.datastore

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.kinvey.android.AndroidCredentialStoreException
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore.Companion.collection
import com.kinvey.androidTest.TestManager
import com.kinvey.androidTest.TestManager.Companion.PASSWORD
import com.kinvey.androidTest.TestManager.Companion.TEST_USERNAME
import com.kinvey.androidTest.TestManager.Companion.USERNAME
import com.kinvey.androidTest.callback.DefaultKinveyAggregateCallback
import com.kinvey.androidTest.model.Person
import com.kinvey.java.model.AggregateType
import com.kinvey.java.store.StoreType
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

/**
 * Created by yuliya on 10/06/17.
 */

@RunWith(AndroidJUnit4::class)
@SmallTest
class AggregationTest {

    private var client: Client<*>? = null

    @Before
    @Throws(InterruptedException::class, IOException::class)
    fun setUp() {
        val mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCountLocally() {
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person = Person(TEST_USERNAME)
        person.height = 170f
        val person2 = Person(TEST_USERNAME)
        person2.height = 170f
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result?.username == person.username)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result?.username == person2.username)
        var query = client?.query()
        query = query?.notEqual("age", "100200300")
        val fields = ArrayList<String>()
        fields.add("username")
        val callback = testManager.calculation(store, AggregateType.COUNT, fields, null, query, null)
        assertNotNull(callback)
        assertNotNull(callback.result?.getResultsFor("username", TEST_USERNAME))
        assertTrue(callback.result?.getResultsFor("username", TEST_USERNAME)?.get(0) == 2)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCountNetwork() {
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager.cleanBackendDataStore(store)
        val person = Person(TEST_USERNAME)
        person.height = 170f
        val person2 = Person(TEST_USERNAME)
        person2.height = 170f
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result?.username == person.username)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result?.username == person2.username)
        var query = client?.query()
        query = query?.notEqual("age", "100200300")
        val fields = ArrayList<String>()
        fields.add("username")
        val callback = testManager.calculation(store, AggregateType.COUNT, fields, null, query, null)
        assertNotNull(callback)
        assertNotNull(callback.result?.getResultsFor("username", TEST_USERNAME))
        assertTrue(callback.result?.getResultsFor("username", TEST_USERNAME)?.get(0) == 2)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testMinLocally() {
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager.cleanBackendDataStore(store)
        val person = Person(TEST_USERNAME)
        person.carNumber = 1
        val person2 = Person(TEST_USERNAME)
        person2.carNumber = 2
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result?.username == person.username)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result?.username == person2.username)
        var query = client!!.query()
        query = query.notEqual("age", "100200300")
        val fields = ArrayList<String>()
        fields.add("username")
        val callback: DefaultKinveyAggregateCallback = testManager.calculation(store, AggregateType.MIN, fields, "carNumber", query, null)
        assertNotNull(callback)
        assertNull(callback.error)
        assertNotNull(callback.result?.getResultsFor("username", TEST_USERNAME))
        assertTrue(callback.result?.getResultsFor("username", TEST_USERNAME)?.get(0) == 1)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testMinNetwork() {
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager.cleanBackendDataStore(store)
        val person = Person(TEST_USERNAME)
        person.carNumber = 1
        val person2 = Person(TEST_USERNAME)
        person2.carNumber = 2
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result?.username == person.username)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result?.username == person2.username)
        var query = client?.query()
        query = query?.notEqual("age", "100200300")
        val fields = ArrayList<String>()
        fields.add("username")
        val callback = testManager.calculation(store, AggregateType.MIN, fields, "carNumber", query, null)
        assertNotNull(callback)
        assertNotNull(callback.result?.getResultsFor("username", TEST_USERNAME))
        assertTrue(callback.result?.getResultsFor("username", TEST_USERNAME)?.get(0) == 1)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCountAuto() {
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        testManager.cleanBackendDataStore(store)
        val person = Person(TEST_USERNAME)
        person.height = 170f
        val person2 = Person(TEST_USERNAME)
        person2.height = 170f
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result?.username == person.username)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result?.username == person2.username)
        var query = client?.query()
        query = query?.notEqual("age", "100200300")
        val fields = ArrayList<String>()
        fields.add("username")
        val callback = testManager.calculation(store, AggregateType.COUNT, fields, null, query, null)
        assertNotNull(callback)
        assertNotNull(callback.result?.getResultsFor("username", TEST_USERNAME))
        assertTrue(callback.result?.getResultsFor("username", TEST_USERNAME)?.get(0) == 2)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testMaxLocally() {
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person = Person(TEST_USERNAME)
        person.carNumber = 1
        val person2 = Person(TEST_USERNAME)
        person2.carNumber = 2
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result?.username == person.username)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result?.username == person2.username)
        var query = client?.query()
        query = query?.notEqual("age", "100200300")
        val fields = ArrayList<String>()
        fields.add("username")
        val callback = testManager.calculation(store, AggregateType.MAX, fields, "carNumber", query, null)
        assertNotNull(callback)
        assertNull(callback.error)
        assertNotNull(callback.result?.getResultsFor("username", TEST_USERNAME))
        assertTrue(callback.result?.getResultsFor("username", TEST_USERNAME)?.get(0) == 2)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testMaxNetwork() {
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager.cleanBackendDataStore(store)
        val person = Person(TEST_USERNAME)
        person.carNumber = 1
        val person2 = Person(TEST_USERNAME)
        person2.carNumber = 2
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result?.username == person.username)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result?.username == person2.username)
        var query = client?.query()
        query = query?.notEqual("age", "100200300")
        val fields = ArrayList<String>()
        fields.add("username")
        val callback = testManager.calculation(store, AggregateType.MAX, fields, "carNumber", query, null)
        assertNotNull(callback)
        assertNull(callback.error)
        assertNotNull(callback.result?.getResultsFor("username", TEST_USERNAME))
        assertTrue(callback.result?.getResultsFor("username", TEST_USERNAME)?.get(0) == 2)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testAverageLocally() {
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person = Person(TEST_USERNAME)
        person.carNumber = 1
        val person2 = Person(TEST_USERNAME)
        person2.carNumber = 2
        val person3 = Person(TEST_USERNAME)
        person3.carNumber = 3
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result?.username == person.username)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result?.username == person2.username)
        saveCallback = testManager.saveCustom(store, person3)
        assertNotNull(saveCallback.result?.username == person3.username)
        var query = client?.query()
        query = query?.notEqual("age", "100200300")
        val fields = ArrayList<String>()
        fields.add("username")
        val callback = testManager.calculation(store, AggregateType.AVERAGE, fields, "carNumber", query, null)
        assertNotNull(callback)
        assertNull(callback.error)
        assertNotNull(callback.result?.getResultsFor("username", TEST_USERNAME))
        assertTrue(callback.result?.getResultsFor("username", TEST_USERNAME)?.get(0) == 2)
    }

    @Test
    fun testAndroidCredentialStoreExceptionMessage() {
        val androidCredentialStoreException = AndroidCredentialStoreException("Credential store corrupted and was rebuilt")
        assertEquals(androidCredentialStoreException.message, "Credential store corrupted and was rebuilt")
    }

    @Test
    fun testAndroidCredentialStoreExceptionConstructor() {
        val androidCredentialStoreException = AndroidCredentialStoreException()
        assertNotNull(androidCredentialStoreException)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testAverageNetwork() {
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager.cleanBackendDataStore(store)
        val person = Person(TEST_USERNAME)
        person.carNumber = 1
        val person2 = Person(TEST_USERNAME)
        person2.carNumber = 2
        val person3 = Person(TEST_USERNAME)
        person3.carNumber = 3
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result?.username == person.username)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result?.username == person2.username)
        saveCallback = testManager.saveCustom(store, person3)
        assertNotNull(saveCallback.result?.username == person3.username)
        var query = client?.query()
        query = query?.notEqual("age", "100200300")
        val fields = ArrayList<String>()
        fields.add("username")
        val callback = testManager.calculation(store, AggregateType.AVERAGE, fields, "carNumber", query, null)
        assertNotNull(callback)
        assertNull(callback.error)
        assertNotNull(callback.result?.getResultsFor("username", TEST_USERNAME))
        assertTrue(callback.result?.getResultsFor("username", TEST_USERNAME)?.get(0) == 2)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSumLocally() {
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person = Person(TEST_USERNAME)
        person.carNumber = 1
        val person2 = Person(TEST_USERNAME)
        person2.carNumber = 2
        val person3 = Person(TEST_USERNAME)
        person3.carNumber = 3
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result?.username == person.username)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result?.username == person2.username)
        saveCallback = testManager.saveCustom(store, person3)
        assertNotNull(saveCallback.result?.username == person3.username)
        var query = client?.query()
        query = query?.notEqual("age", "100200300")
        val fields = ArrayList<String>()
        fields.add("username")
        val callback = testManager.calculation(store, AggregateType.SUM, fields, "carNumber", query, null)
        assertNotNull(callback)
        assertNull(callback.error)
        assertNotNull(callback.result?.getResultsFor("username", TEST_USERNAME))
        assertTrue(callback.result?.getResultsFor("username", TEST_USERNAME)?.get(0) == 6)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSumNetwork() {
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager.cleanBackendDataStore(store)
        val person = Person(TEST_USERNAME)
        person.carNumber = 1
        val person2 = Person(TEST_USERNAME)
        person2.carNumber = 2
        val person3 = Person(TEST_USERNAME)
        person3.carNumber = 3
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result?.username == person.username)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result?.username == person2.username)
        saveCallback = testManager.saveCustom(store, person3)
        assertNotNull(saveCallback.result?.username == person3.username)
        var query = client?.query()
        query = query?.notEqual("age", "100200300")
        val fields = ArrayList<String>()
        fields.add("username")
        val callback = testManager.calculation(store, AggregateType.SUM, fields, "carNumber", query, null)
        assertNotNull(callback)
        assertNull(callback.error)
        assertNotNull(callback.result?.getResultsFor("username", TEST_USERNAME))
        assertTrue(callback.result?.getResultsFor("username", TEST_USERNAME)?.get(0) == 6)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSumForStringFieldsError() {
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person = Person(TEST_USERNAME)
        person.carNumber = 1
        val person2 = Person(TEST_USERNAME)
        person2.carNumber = 2
        val person3 = Person(TEST_USERNAME)
        person3.carNumber = 3
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result?.username == person.username)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result?.username == person2.username)
        saveCallback = testManager.saveCustom(store, person3)
        assertNotNull(saveCallback.result?.username == person3.username)
        var query = client?.query()
        query = query?.notEqual("age", "100200300")
        val fields = ArrayList<String>()
        fields.add("username")
        val callback = testManager.calculation(store, AggregateType.SUM, fields, "username", query, null)
        assertNotNull(callback)
        assertNotNull(callback.error)
        assertEquals(callback.error?.message, "Field 'username': type mismatch - int, float or double expected.")
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

    companion object {
        private const val COLLECTION = "PersonsNew"
    }
}