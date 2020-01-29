package com.kinvey.androidTest.store.data.cache

import android.content.Context
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.Client.Companion.kinveyHandlerThread
import com.kinvey.android.Client.Companion.sharedInstance
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore.Companion.collection
import com.kinvey.androidTest.TestManager
import com.kinvey.androidTest.TestManager.Companion.TEST_USERNAME
import com.kinvey.androidTest.model.ModelWithDifferentTypeFields
import com.kinvey.androidTest.model.Person
import com.kinvey.java.Query
import com.kinvey.java.model.AggregateType
import com.kinvey.java.store.StoreType
import junit.framework.Assert.assertTrue
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
class CacheAggregationTest {
    private var client: Client<*>? = null
    @Before
    @Throws(InterruptedException::class, IOException::class)
    fun setUp() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCount() {
        val testManager = TestManager<Person>()
//      testManager.login(USERNAME, PASSWORD, client);
        val user = User()
        user.id = "testId"
        sharedInstance().activeUser = user
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val cache = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)
        val person = Person(TEST_USERNAME)
        person.height = 170f
        val person2 = Person("test")
        person2.height = 180f
        val person3 = Person(TEST_USERNAME)
        person3.height = 180f
        testManager.saveCustom(store, person)
        testManager.saveCustom(store, person2)
        testManager.saveCustom(store, person3)
        val query = client?.query()?.equals("username", TEST_USERNAME) as Query
        val fields = ArrayList<String>()
        fields.add("username")
        val results = cache?.group(AggregateType.COUNT, fields, null, query)
        assertTrue(results?.get(0)?.result?.toInt() == 2)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testQueryToFloatField() {
        val testManager = TestManager<Person>()
//      testManager.login(USERNAME, PASSWORD, client);
        val user = User()
        user.id = "testId"
        sharedInstance().activeUser = user
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val cache = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)
        val person = Person(TEST_USERNAME)
        person.height = 170f
        val person2 = Person(TEST_USERNAME)
        person2.height = 180f
        val person3 = Person(TEST_USERNAME)
        person3.height = 180f
        testManager.saveCustom(store, person)
        testManager.saveCustom(store, person2)
        testManager.saveCustom(store, person3)
        val query = client?.query()?.equals("height", 170f) as Query
        val fields = ArrayList<String>()
        fields.add("username")
        val results = cache?.group(AggregateType.COUNT, fields, null, query)
        assertTrue(results?.get(0)?.result?.toInt() == 1)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testQueryToLongField() {
        val testManager = TestManager<Person>()
//      testManager.login(USERNAME, PASSWORD, client);
        val user = User()
        user.id = "testId"
        sharedInstance().activeUser = user
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val cache = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)
        val person = Person(TEST_USERNAME)
        person.weight = 170L
        val person2 = Person(TEST_USERNAME)
        person2.weight = 180L
        val person3 = Person(TEST_USERNAME)
        person3.weight = 180L
        testManager.saveCustom(store, person)
        testManager.saveCustom(store, person2)
        testManager.saveCustom(store, person3)
        val query = client!!.query().equals("weight", 170L)
        val fields = ArrayList<String>()
        fields.add("username")
        val results = cache?.group(AggregateType.COUNT, fields, null, query)
        assertTrue(results?.get(0)?.result?.toInt() == 1)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testQueryToIntField() {
        val testManager = TestManager<Person>()
//        testManager.login(USERNAME, PASSWORD, client);


        val user = User()
        user.id = "testId"
        sharedInstance().activeUser = user
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val cache = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)
        val person = Person(TEST_USERNAME)
        person.carNumber = 1
        val person2 = Person(TEST_USERNAME)
        person2.carNumber = 2
        val person3 = Person(TEST_USERNAME)
        person3.carNumber = 2
        testManager.saveCustom(store, person)
        testManager.saveCustom(store, person2)
        testManager.saveCustom(store, person3)
        val query = client!!.query().equals("carNumber", 1)
        val fields = ArrayList<String>()
        fields.add("username")
        val results = cache?.group(AggregateType.COUNT, fields, null, query)
        assertTrue(results?.get(0)?.result?.toInt() == 1)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testMin() {
        val testManager = TestManager<Person>()
//      testManager.login(USERNAME, PASSWORD, client);
        val user = User()
        user.id = "testId"
        sharedInstance().activeUser = user
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val cache = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)
        val person = Person(TEST_USERNAME)
        person.height = 170f
        val person2 = Person("test")
        person2.height = 180f
        val person3 = Person(TEST_USERNAME)
        person3.height = 180f
        testManager.saveCustom(store, person)
        testManager.saveCustom(store, person2)
        testManager.saveCustom(store, person3)
        val query = client?.query()?.equals("username", TEST_USERNAME) as Query
        val fields = ArrayList<String>()
        fields.add("username")
        val results = cache?.group(AggregateType.MIN, fields, "height", query)
        assertTrue(results?.get(0)?.result?.toFloat() == 170f)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testMax() {
        val testManager = TestManager<Person>()
//      testManager.login(USERNAME, PASSWORD, client);
        val user = User()
        user.id = "testId"
        sharedInstance().activeUser = user
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val cache = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)
        val person = Person(TEST_USERNAME)
        person.height = 170f
        val person2 = Person(TEST_USERNAME)
        person2.height = 180f
        val person3 = Person(TEST_USERNAME)
        person3.height = 180f
        testManager.saveCustom(store, person)
        testManager.saveCustom(store, person2)
        testManager.saveCustom(store, person3)
        val query = client?.query()?.equals("username", TEST_USERNAME) as Query
        val fields = ArrayList<String>()
        fields.add("username")
        val results = cache?.group(AggregateType.MAX, fields, "height", query)
        assertTrue(results?.get(0)?.result?.toFloat() == 180f)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testAverage() {
        val testManager = TestManager<Person>()
//      testManager.login(USERNAME, PASSWORD, client);
        val user = User()
        user.id = "testId"
        sharedInstance().activeUser = user
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val cache = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)
        val person = Person(TEST_USERNAME)
        person.height = 170f
        val person2 = Person("test")
        person2.height = 180f
        val person3 = Person(TEST_USERNAME)
        person3.height = 180f
        testManager.saveCustom(store, person)
        testManager.saveCustom(store, person2)
        testManager.saveCustom(store, person3)
        val query = client?.query()?.equals("username", TEST_USERNAME) as Query
        val fields = ArrayList<String>()
        fields.add("username")
        val results = cache?.group(AggregateType.AVERAGE, fields, "height", query)
        assertTrue(results?.get(0)?.result?.toFloat() == 175f)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSum() {
        val testManager = TestManager<Person>()
//        testManager.login(USERNAME, PASSWORD, client);


        val user = User()
        user.id = "testId"
        sharedInstance().activeUser = user
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val cache = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)
        val person = Person(TEST_USERNAME)
        person.height = 170f
        val person2 = Person("test")
        person2.height = 180f
        val person3 = Person(TEST_USERNAME)
        person3.height = 180f
        testManager.saveCustom(store, person)
        testManager.saveCustom(store, person2)
        testManager.saveCustom(store, person3)
        val query = client?.query()?.equals("username", TEST_USERNAME) as Query
        val fields = ArrayList<String>()
        fields.add("username")
        val results = cache?.group(AggregateType.SUM, fields, "height", query)
        assertTrue(results?.get(0)?.result?.toFloat() == 350f)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testCalculationFields() {
        val testManager = TestManager<ModelWithDifferentTypeFields>()
//        testManager.login(USERNAME, PASSWORD, client);


        val user = User()
        user.id = "testId"
        sharedInstance().activeUser = user
        val store = collection(ModelWithDifferentTypeFields.COLLECTION, ModelWithDifferentTypeFields::class.java, StoreType.SYNC, client)
        val cache = client?.cacheManager?.getCache(ModelWithDifferentTypeFields.COLLECTION, ModelWithDifferentTypeFields::class.java, Long.MAX_VALUE)
        val carNumber = 1000
        val isUseAndroid = true
        val date = Date()
        date.time = 1
        val height = 180f
        val time = 1.2
        val person = ModelWithDifferentTypeFields(TEST_USERNAME, carNumber, isUseAndroid, date, height, time)
        val person2 = ModelWithDifferentTypeFields("test", carNumber, isUseAndroid, date, height, time)
        val person3 = ModelWithDifferentTypeFields(TEST_USERNAME, carNumber, isUseAndroid, date, height, time)
        testManager.saveCustom(store, person)
        testManager.saveCustom(store, person2)
        testManager.saveCustom(store, person3)
        val query = client?.query()?.equals("username", TEST_USERNAME) as Query
        val fields = ArrayList<String>()
        fields.add("carNumber")
        var results = cache?.group(AggregateType.SUM, fields, "height", query)
        assertTrue(results?.get(0)?.result?.toFloat() == 360f)
        fields.clear()
        fields.add("isUseAndroid")
        results = cache?.group(AggregateType.SUM, fields, "height", query)
        assertTrue(results?.get(0)?.result?.toFloat() == 360f)
        fields.clear()
        fields.add("height")
        results = cache?.group(AggregateType.SUM, fields, "height", query)
        assertTrue(results?.get(0)?.result?.toFloat() == 360f)
        fields.clear()
        fields.add("time")
        results = cache?.group(AggregateType.SUM, fields, "height", query)
        assertTrue(results?.get(0)?.result?.toFloat() == 360f)


//commented out because of MLIBZ-2643
/*        fields.clear();
        fields.add("date");
        results = cache.group(AggregateType.SUM, fields, "height", query);
        assertTrue(results[0].result.intValue() == 360);*/

    }

    @After
    fun tearDown() {
        client!!.performLockDown()
        if (kinveyHandlerThread != null) {
            try {
                client!!.stopKinveyHandlerThread()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }
}