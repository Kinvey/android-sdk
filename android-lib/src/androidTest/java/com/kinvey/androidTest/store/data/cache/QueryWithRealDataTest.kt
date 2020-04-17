package com.kinvey.androidTest.store.data.cache

import android.content.Context
import android.os.Message
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.callback.KinveyDeleteCallback
import com.kinvey.android.callback.KinveyReadCallback
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore
import com.kinvey.android.store.DataStore.Companion.collection
import com.kinvey.android.store.UserStore.Companion.login
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.TestManager
import com.kinvey.androidTest.model.Person
import com.kinvey.java.AbstractClient
import com.kinvey.java.Query
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.model.KinveyReadResponse
import com.kinvey.java.store.StoreType
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@SmallTest
class QueryWithRealDataTest {

    private var client: Client<*>? = null

    private class DefaultKinveyDeleteCallback (private val latch: CountDownLatch) : KinveyDeleteCallback {

        var result: Int? = null
        var error: Throwable? = null

        override fun onSuccess(result: Int?) {
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

    private class DefaultKinveyReadCallback (private val latch: CountDownLatch) : KinveyReadCallback<Person> {

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

    @Before
    @Throws(InterruptedException::class, IOException::class)
    fun setUp() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        val latch = CountDownLatch(1)
        var looperThread: LooperThread? = null
        if (client?.isUserLoggedIn == false) {
            looperThread = LooperThread(Runnable {
                try {
                    login(TestManager.USERNAME, TestManager.PASSWORD, client as AbstractClient<User>, object : KinveyClientCallback<User> {
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

    @Test
    @Ignore
    @Throws(InterruptedException::class, IOException::class)
    fun testACLInNETWORK() {
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        client?.syncManager?.clear(Person.COLLECTION)
        (0..2).forEach { i -> store.save(createPerson(TEST_TEMP_USERNAME)) }
        val query = Query()
        query.`in`("_acl.creator", arrayOf(client?.activeUser?.id ?: ""))
        val findCallback = find(store, query)
        delete(store, client?.query()?.notEqual("name", "no_exist_field") as Query)
        assertTrue(findCallback.result!!.result!!.size == 3)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testACLInSYNC() {
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(Person.COLLECTION)
        (0..2).forEach { i -> store.save(createPerson(TEST_TEMP_USERNAME)) }
        val query = Query()
        query.`in`("_acl.creator", arrayOf(client?.activeUser?.id ?: ""))
        val findCallback = find(store, query)
        assertTrue(findCallback.result?.result?.size == 3)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testACLInOneParameterForQuery() {
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(Person.COLLECTION)
        (0..2).forEach { i -> store.save(createPerson(TEST_TEMP_USERNAME)) }
        store.save(createPerson(TEST_USERNAME))
        val query = Query()
        query.`in`("username", arrayOf(TEST_TEMP_USERNAME))
        val findCallback = find(store, query)
        assertTrue(findCallback.result?.result?.size == 3)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testACLInTwoParametersForQuery() {
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(Person.COLLECTION)
        (0..2).forEach { i -> store.save(createPerson(TEST_TEMP_USERNAME)) }
        store.save(createPerson(TEST_USERNAME))
        val query = Query()
        query.`in`("username", arrayOf(TEST_TEMP_USERNAME, TEST_USERNAME))
        val findCallback = find(store, query)
        assertTrue(findCallback.result?.result?.size == 4)
    }

    @Test
    @Ignore
    @Throws(InterruptedException::class, IOException::class)
    fun testACLInCACHE() {
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.CACHE, client)
        client?.syncManager?.clear(Person.COLLECTION)
        (0..2).forEach { i -> store.save(createPerson(TEST_TEMP_USERNAME)) }
        val query = Query()
        query.`in`("_acl.creator", arrayOf<String>(client?.activeUser?.id ?: ""))
        val findCallback = find(store, query)
        delete(store, client?.query()?.notEqual("name", "no_exist_field") as Query)
        assertTrue(findCallback.result?.result?.size == 3)
    }

    private fun createPerson(name: String): Person {
        val person = Person()
        person.username = name
        return person
    }

    @Throws(InterruptedException::class)
    private fun find(store: DataStore<Person>, query: Query): DefaultKinveyReadCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyReadCallback(latch)
        val looperThread = LooperThread(Runnable { store.find(query, callback, null) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun delete(store: DataStore<Person>, query: Query): DefaultKinveyDeleteCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyDeleteCallback(latch)
        val looperThread = LooperThread(Runnable { store.delete(query, callback) })
        looperThread.start()
        latch.await(120, TimeUnit.SECONDS)
        looperThread.mHandler?.sendMessage(Message())
        return callback
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
        private const val TEST_TEMP_USERNAME = "Temp_UserName"
    }
}