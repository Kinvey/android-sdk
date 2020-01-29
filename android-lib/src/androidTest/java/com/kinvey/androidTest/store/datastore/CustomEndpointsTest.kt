package com.kinvey.androidTest.store.datastore

import android.content.Context
import android.os.Looper
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.google.api.client.json.GenericJson
import com.kinvey.android.AsyncCustomEndpoints
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.callback.KinveyListCallback
import com.kinvey.android.model.User
import com.kinvey.android.store.UserStore.Companion.login
import com.kinvey.androidTest.TestManager
import com.kinvey.androidTest.model.Person
import com.kinvey.java.AbstractClient
import com.kinvey.java.CustomEndpoints
import com.kinvey.java.CustomEndpoints.CustomCommand
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.core.KinveyJsonResponseException
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@SmallTest
class CustomEndpointsTest {
    private var client: Client<User>? = null
    @Before
    @Throws(InterruptedException::class, IOException::class)
    fun setUp() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        val latch = CountDownLatch(1)
        if (client?.isUserLoggedIn == false) {
            Thread(Runnable {
                Looper.prepare()
                try {
                    login<User>(TestManager.USERNAME, TestManager.PASSWORD, client as AbstractClient<User>, object : KinveyClientCallback<User> {
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
                Looper.loop()
            }).start()
        } else {
            latch.countDown()
        }
        latch.await()
    }

    /*
    * CustomEndpoint = 'x2'
    * function onRequest(request, response, modules) {
    *     response.body = {RESULT:request.body.id*2};
    *     response.complete();
    * }
    */
    @Test
    @Throws(IOException::class)
    fun testCustomEndpoints() {
        val endpoints: CustomEndpoints<GenericJson, GenericJson>? = client?.customEndpoints(GenericJson::class.java)
        val genericJson = GenericJson()
        val i = 1
        genericJson.set(ID, i)
        val command = endpoints?.callEndpointBlocking(X2, genericJson)
        val response = command?.execute()
        assertEquals(i * 2.toLong(), (response?.get(RESULT) as BigDecimal).intValueExact().toLong())
    }

    @Test
    @Throws(IOException::class)
    fun testCustomEndpointsDeprecatedMethod() {
        val endpoints: CustomEndpoints<GenericJson, GenericJson>? = client?.customEndpoints()
        val genericJson = GenericJson()
        val i = 1
        genericJson.set(ID, i)
        val command = endpoints?.callEndpointBlocking(X2, genericJson)
        val response = command?.execute()
        assertEquals(i * 2.toLong(), (response?.get(RESULT) as BigDecimal).intValueExact().toLong())
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testCustomEndpointsNotExist() {
        val endpoints: CustomEndpoints<GenericJson, GenericJson>? = client?.customEndpoints(GenericJson::class.java)
        val genericJson = GenericJson()
        val command = endpoints?.callEndpointBlocking(NOT_EXIST_CUSTOM_ENDPOINT, genericJson)
        try {
            command?.execute()
        } catch (exception: KinveyJsonResponseException) {
            assertNotNull(exception)
        }
    }

    /*
    * CustomEndpoint = 'x2'
    * function onRequest(request, response, modules) {
    *     response.body = {RESULT:request.body.id*2};
    *     response.complete();
    * }
    */
    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testAsyncCustomEndpoints() {
        val endpoints: CustomEndpoints<GenericJson, GenericJson>? = client?.customEndpoints(GenericJson::class.java)
        val genericJson = GenericJson()
        val i = 1
        genericJson.set(ID, i)
        val callback = callEndpoint(X2, endpoints as AsyncCustomEndpoints<GenericJson, GenericJson>, genericJson)
        assertEquals(i * 2.toLong(), (callback.result!![RESULT] as BigDecimal).intValueExact().toLong())
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testAsyncCustomEndpointsDeprecatedMethod() {
        val endpoints: CustomEndpoints<GenericJson, GenericJson>? = client?.customEndpoints()
        val genericJson = GenericJson()
        val i = 1
        genericJson.set(ID, i)
        val callback = callEndpoint(X2, endpoints as AsyncCustomEndpoints<GenericJson, GenericJson>, genericJson)
        assertEquals(i * 2.toLong(), (callback.result!![RESULT] as BigDecimal).intValueExact().toLong())
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testAsyncCustomEndpointsNotExist() {
        val endpoints: CustomEndpoints<GenericJson, GenericJson>? = client?.customEndpoints(GenericJson::class.java)
        val genericJson = GenericJson()
        val i = 1
        genericJson.set(ID, i)
        val callback = callEndpoint(NOT_EXIST_CUSTOM_ENDPOINT, endpoints as AsyncCustomEndpoints<GenericJson, GenericJson>, genericJson)
        assertNotNull(callback.error)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testAsyncCustomEndpointsInputNotExist() {
        val endpoints: AsyncCustomEndpoints<GenericJson, GenericJson>? = client?.customEndpoints(GenericJson::class.java)
        val callback = callEndpoint(X2, endpoints as AsyncCustomEndpoints<GenericJson, GenericJson>, null)
        assertNotNull(callback.result)
    }

    /**
     * CustomEndpoint = 'getPerson'
     * function onRequest(request, response, modules) {
     * response.body = {"name":"TestName"};
     * response.complete();
     * }
     */
    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testCustomEndpointsCustomClass() {
        val endpoints: AsyncCustomEndpoints<GenericJson, Person>? = client?.customEndpoints(Person::class.java)
        val callback = callCustomJsonEndpointAsync(GET_PERSON, endpoints as AsyncCustomEndpoints<GenericJson, Person>?, null)
        assertNotNull(callback.result)
        assertTrue(callback.result is Person)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testCustomEndpointsWrongCustomClass() {
        val endpoints: AsyncCustomEndpoints<GenericJson, Array<Person>>? = client?.customEndpoints(Array<Person>::class.java)
        val callback = callCustomJsonEndpointAsync(GET_PERSON, endpoints as AsyncCustomEndpoints<GenericJson, Person>?, null)
        assertNotNull(callback.error)
    }

    /**
     * CustomEndpoint = 'getPersonList'
     * function onRequest(request, response, modules) {
     * response.body = [{"name":"TestName"}];
     * response.complete();
     * }
     */
    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testCustomEndpointsCustomClassArray() {
        val endpoints: AsyncCustomEndpoints<GenericJson, Person>? = client?.customEndpoints(Person::class.java)
        val callback = callCustomJsonEndpointListAsync(GET_PERSON_LIST, endpoints as AsyncCustomEndpoints<GenericJson, Person>, null)
        assertNotNull(callback.result)
        assertTrue(callback.result is List<*>)
        assertTrue(callback.result?.get(0) is Person)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testCustomEndpointsWrongCustomClassArray() {
        val endpoints: AsyncCustomEndpoints<GenericJson, *>? = client?.customEndpoints(Array<Person>::class.java)
        val callback = callCustomJsonEndpointListAsync(GET_PERSON_LIST, endpoints as AsyncCustomEndpoints<GenericJson, Person>?, null)
        assertNotNull(callback.error)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testCustomEndpointsCustomClassArrayNotExist() {
        val endpoints: AsyncCustomEndpoints<GenericJson, Person>? = client?.customEndpoints(Person::class.java)
        val callback = callCustomJsonEndpointListAsync(NOT_EXIST_CUSTOM_ENDPOINT, endpoints, null)
        assertNotNull(callback.error)
    }

    @Throws(InterruptedException::class)
    private fun callEndpoint(endPointName: String, customEndpoints: AsyncCustomEndpoints<GenericJson, GenericJson>?, json: GenericJson?): DefaultKinveyClientCallback<GenericJson> {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback<GenericJson>(latch)
        Thread(Runnable {
            Looper.prepare()
            customEndpoints?.callEndpoint(endPointName, json, callback)
            Looper.loop()
        }).start()
        latch.await()
        return callback
    }

    @Throws(InterruptedException::class)
    private fun callCustomJsonEndpointAsync(endPointName: String, customEndpoints: AsyncCustomEndpoints<GenericJson, Person>?, json: GenericJson?): DefaultKinveyClientCallback<Person> {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback<Person>(latch)
        Thread(Runnable {
            Looper.prepare()
            customEndpoints?.callEndpoint(endPointName, json, callback)
            Looper.loop()
        }).start()
        latch.await()
        return callback
    }

    @Throws(InterruptedException::class)
    private fun callCustomJsonEndpointListAsync(endPointName: String, customEndpoints: AsyncCustomEndpoints<GenericJson, Person>?, json: GenericJson?): DefaultKinveyListCallback<Person> {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyListCallback<Person>(latch)
        Thread(Runnable {
            Looper.prepare()
            customEndpoints?.callEndpoint(endPointName, json, callback)
            Looper.loop()
        }).start()
        latch.await()
        return callback
    }

    private class DefaultKinveyClientCallback<T>(private val latch: CountDownLatch) : KinveyClientCallback<T> {
        var result: T? = null
        var error: Throwable? = null
        override fun onSuccess(result: T?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        internal fun finish() {
            latch.countDown()
        }

    }

    private class DefaultKinveyListCallback<T>(private val latch: CountDownLatch) : KinveyListCallback<T> {
        var result: List<*>? = null
        var error: Throwable? = null
        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        internal fun finish() {
            latch.countDown()
        }

        override fun onSuccess(result: List<T>?) {
            this.result = result
            finish()
        }

    }

    @After
    fun tearDown() {
        if (Client.kinveyHandlerThread != null) {
            try {
                client?.stopKinveyHandlerThread()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    companion object {
        private const val X2 = "x2"
        private const val GET_PERSON = "getPerson"
        private const val GET_PERSON_LIST = "getPersonList"
        private const val NOT_EXIST_CUSTOM_ENDPOINT = "notExistCustomEndpoint"
        private const val ID = "id"
        private const val RESULT = "res"
    }
}