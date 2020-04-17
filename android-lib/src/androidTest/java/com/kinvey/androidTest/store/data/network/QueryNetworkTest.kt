package com.kinvey.androidTest.store.data.network

import android.os.Message
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.api.client.json.GenericJson
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.callback.KinveyReadCallback
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore
import com.kinvey.android.store.DataStore.Companion.collection
import com.kinvey.android.store.UserStore.Companion.login
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.TestManager
import com.kinvey.androidTest.callback.DefaultKinveyDeleteCallback
import com.kinvey.androidTest.model.Location
import com.kinvey.androidTest.model.Person
import com.kinvey.java.AbstractClient
import com.kinvey.java.Query
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.model.KinveyReadResponse
import com.kinvey.java.query.AbstractQuery.SortOrder
import com.kinvey.java.store.StoreType
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@SmallTest
class QueryNetworkTest {

    private var client: Client<*>? = null

    @Before
    @Throws(InterruptedException::class, IOException::class)
    fun setUp() {
        val mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        client?.enableDebugLogging()
        val latch = CountDownLatch(1)
        var looperThread: LooperThread? = null
        if (client?.isUserLoggedIn == false) {
            looperThread = LooperThread(Runnable {
                try {
                    login<User>(TestManager.USERNAME, TestManager.PASSWORD, client as AbstractClient<User>,
                    object : KinveyClientCallback<User> {
                        override fun onSuccess(result: User?) {
                            assertNotNull(result)
                            latch.countDown()
                        }

                        override fun onFailure(error: Throwable?) {
                            assertNull(error)
                            latch.countDown()
                        }
                    })
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            })
            looperThread.start()
        } else {
            latch.countDown()
        }
        latch.await()
        looperThread?.mHandler?.sendMessage(Message())
    }

    @Throws(InterruptedException::class)
    private fun <T : GenericJson> clearBackend(store: DataStore<T>) {
        var query = client?.query() as Query
        query = query?.notEqual("age", "100500")
        val deleteCallback = delete(store, query)
    }

    @Throws(InterruptedException::class)
    private fun <T : GenericJson> delete(store: DataStore<T>, query: Query): DefaultKinveyDeleteCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyDeleteCallback(latch)
        val looperThread = LooperThread(Runnable { store.delete(query, callback) })
        looperThread.start()
        latch.await(120, TimeUnit.SECONDS)
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun find(store: DataStore<Person>, query: Query, seconds: Int): DefaultKinveyReadCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyReadCallback(latch)
        val looperThread = LooperThread(Runnable { store?.find(query, callback, null) })
        looperThread.start()
        latch.await(seconds.toLong(), TimeUnit.SECONDS)
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun findLoc(store: DataStore<Location>, query: Query, seconds: Int): DefaultKinveyReadLocCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyReadLocCallback(latch)
        val looperThread = LooperThread(Runnable { store.find(query, callback, null) })
        looperThread.start()
        latch.await(seconds.toLong(), TimeUnit.SECONDS)
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    private fun createPerson(name: String): Person {
        return Person(name)
    }

    private class DefaultKinveyClientCallback(val latch: CountDownLatch) : KinveyClientCallback<Person> {
        var result: Person? = null
        var error: Throwable? = null
        override fun onSuccess(result: Person?) {
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

    private class DefaultKinveyLocationCallback(val latch: CountDownLatch) : KinveyClientCallback<Location> {
        var result: Location? = null
        var error: Throwable? = null
        override fun onSuccess(result: Location?) {
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

    private class DefaultKinveyReadCallback (val latch: CountDownLatch) : KinveyReadCallback<Person> {
        var result: KinveyReadResponse<Person>? = null
        var error: Throwable? = null
        override fun onSuccess(result: KinveyReadResponse<Person>?) {
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

    private class DefaultKinveyReadLocCallback(val latch: CountDownLatch) : KinveyReadCallback<Location> {
        var result: KinveyReadResponse<Location>? = null
        var error: Throwable? = null
        override fun onSuccess(result: KinveyReadResponse<Location>?) {
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

    @Throws(InterruptedException::class)
    private fun save(store: DataStore<Person>, person: Person): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable { store?.save(person, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun saveLoc(store: DataStore<Location>, location: Location): DefaultKinveyLocationCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyLocationCallback(latch)
        val looperThread = LooperThread(Runnable { store.save(location, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Test
    @Throws(InterruptedException::class)
    fun testStartsWith() {
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(store)
        client?.syncManager?.clear(Person.COLLECTION)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        var query = client?.query()
        query = query?.startsWith(USERNAME, "Tes") as Query
        val kinveyListCallback = find(store, query, DEFAULT_TIMEOUT)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        assertTrue(kinveyListCallback.result?.result?.size ?: 0 > 0)
        delete(store, query)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testQueryString() {
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(store)
        client?.syncManager?.clear(Person.COLLECTION)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        var query = client?.query()
        query = query?.setQueryString("{\"username\":{\"\$regex\":\"^Tes\"}}") as Query
        val kinveyListCallback = find(store, query, DEFAULT_TIMEOUT)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        assertTrue(kinveyListCallback.result?.result?.size ?: 0 > 0)
        delete(store, query)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testAll() {
        val store = collection(Location.COLLECTION, Location::class.java, StoreType.NETWORK, client)
        clearBackend(store)
        client?.syncManager?.clear(Location.COLLECTION)
        val geo = arrayOf<Double>(3.0, 4.0)
        val location = Location()
        location.geo = geo
        val saveCallback = saveLoc(store, location)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        var query = client?.query()
        query = query?.all(GEOLOC, geo) as Query
        val kinveyListCallback = findLoc(store, query, DEFAULT_TIMEOUT)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        assertTrue(kinveyListCallback.result?.result?.size ?: 0 > 0)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSize() {
        val store = collection(Location.COLLECTION, Location::class.java, StoreType.NETWORK, client)
        clearBackend(store)
        client?.syncManager?.clear(Location.COLLECTION)
        val geo = arrayOf(3.0, 4.0)
        val location = Location()
        location.geo = geo
        val saveCallback = saveLoc(store, location)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        var query = client?.query()
        query = query?.size(GEOLOC, 2) as Query
        val kinveyListCallback = findLoc(store, query, DEFAULT_TIMEOUT)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        assertTrue(kinveyListCallback.result?.result?.size ?: 0 > 0)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLimit() {
        val store = collection(Location.COLLECTION, Location::class.java, StoreType.NETWORK, client)
        clearBackend(store)
        client?.syncManager?.clear(Location.COLLECTION)
        val geo = arrayOf(3.0, 4.0)
        val location = Location()
        location.geo = geo
        var saveCallback = saveLoc(store, location)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        saveCallback = saveLoc(store, location)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        var query = client?.query()
        query = query?.setLimit(2) as Query
        val kinveyListCallback = findLoc(store, query, DEFAULT_TIMEOUT)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        assertTrue(kinveyListCallback.result?.result?.size == 2)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRegEx() {
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(store)
        client?.syncManager?.clear(Person.COLLECTION)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        var query = client?.query() as Query
        query = query.regEx(USERNAME, "^T")
        val kinveyListCallback = find(store, query, DEFAULT_TIMEOUT)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        assertTrue(kinveyListCallback.result?.result?.size ?: 0 > 0)
        delete(store, query)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testQueryAddSort() {
        val heightPersonOne = 180f
        val heightPersonTwo = 150f
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(store)
        client?.syncManager?.clear(Person.COLLECTION)
        var person = createPerson(TEST_USERNAME)
        person.height = heightPersonOne
        var saveCallback = save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        person = createPerson(TEST_USERNAME_2)
        person.height = heightPersonTwo
        saveCallback = save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        var query = client?.query() as Query
        query = query?.addSort(HEIGHT, SortOrder.ASC)
        val kinveyListCallback = find(store, query, DEFAULT_TIMEOUT)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        assertTrue(kinveyListCallback.result?.result?.size ?: 0 > 1)
        assertTrue(kinveyListCallback.result?.result?.get(0)?.height == heightPersonTwo)
        delete(store, query)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testQueryGeoWithinBox() {
        val store = collection(Location.COLLECTION, Location::class.java, StoreType.NETWORK, client)
        clearBackend(store)
        client?.syncManager?.clear(Location.COLLECTION)
        val geo = arrayOf(30.0, 40.0)
        val location = Location()
        location.geo = geo
        val saveCallback = saveLoc(store, location)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val query = client?.query()
        val geoQuery = query?.withinBox(GEOLOC, 20.0, 20.0, 40.0, 50.0) as Query
        val kinveyListCallback = findLoc(store, geoQuery, DEFAULT_TIMEOUT)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        assertTrue(kinveyListCallback.result?.result?.size ?: 0 > 0)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testQueryGeoNearSphere() {
        val store = collection(Location.COLLECTION, Location::class.java, StoreType.NETWORK, client)
        clearBackend(store)
        client?.syncManager?.clear(Location.COLLECTION)
        val geo = arrayOf(30.0, 40.0)
        val location = Location()
        location.geo = geo
        val saveCallback = saveLoc(store, location)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val query1 = client?.query()
        val geoQueryNearSphere = query1?.nearSphere(GEOLOC, 20.0, 50.0) as Query
        val query2 = client?.query()
        val geoQueryNearSphereDistance = query2?.nearSphere(GEOLOC, 20.0, 50.0, 100000.0) as Query
        val kinveyFindCallback1 = findLoc(store, geoQueryNearSphere, DEFAULT_TIMEOUT)
        val kinveyFindCallback2 = findLoc(store, geoQueryNearSphereDistance, DEFAULT_TIMEOUT)
        assertNull(kinveyFindCallback1.error)
        assertNotNull(kinveyFindCallback1.result)
        assertTrue(kinveyFindCallback1.result?.result?.size ?: 0 > 0)
        assertNull(kinveyFindCallback2.error)
        assertNotNull(kinveyFindCallback2.result)
        assertTrue(kinveyFindCallback2.result?.result?.size ?: 0 > 0)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testQueryGeoWithinPolygon() {
        val store = collection(Location.COLLECTION, Location::class.java, StoreType.NETWORK, client)
        clearBackend(store)
        client!!.syncManager.clear(Location.COLLECTION)
        val geo = arrayOf(30.0, 40.0)
        val location = Location()
        location.geo = geo
        val saveCallback = saveLoc(store, location)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val query1 = client?.query()
        val geoQueryWithinPolygon = query1?.withinPolygon(GEOLOC,
                30.0, 20.0, 40.0, 30.0,
                50.0, 40.0, 60.0, 50.0) as Query
        val kinveyFindCallback = findLoc(store, geoQueryWithinPolygon, DEFAULT_TIMEOUT)
        assertNull(kinveyFindCallback.error)
        assertNotNull(kinveyFindCallback.result)
        assertTrue(kinveyFindCallback.result?.result?.size ?: 0 > 0)
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
        private const val TEST_USERNAME = "Test_UserName"
        private const val TEST_USERNAME_2 = "Test_UserName_2"
        private const val USERNAME = "username"
        private const val HEIGHT = "height"
        private const val GEOLOC = "_geoloc"
        private const val DEFAULT_TIMEOUT = 60
    }
}