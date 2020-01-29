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
import com.kinvey.androidTest.TestManager.Companion.PASSWORD
import com.kinvey.androidTest.TestManager.Companion.TEST_USERNAME
import com.kinvey.androidTest.TestManager.Companion.USERNAME
import com.kinvey.androidTest.callback.*
import com.kinvey.androidTest.model.*
import com.kinvey.java.Query
import com.kinvey.java.store.StoreType
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

/**
 * Created by yuliya on 09/14/17.
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class CacheRealDataTest {
    private var client: Client<*>? = null
    @Before
    @Throws(InterruptedException::class, IOException::class)
    fun setUp() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
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
    @Throws(InterruptedException::class)
    fun testSaveStringArrayToRealmLocally() {
        val testManager = TestManager<StringPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, StringPrimitiveListInPerson::class.java, StoreType.SYNC, client)
        val person = StringPrimitiveListInPerson(TEST_USERNAME)
        val phones = ArrayList<String>()
        phones.add(TEST_STRING)
        phones.add("987654321")
        person.stringList = phones
        val saveCallback: CustomKinveyClientCallback<StringPrimitiveListInPerson> = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.stringList)
        assertTrue(saveCallback.result?.stringList?.size == 2)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveBooleanArrayToRealmLocally() {
        val testManager = TestManager<BooleanPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, BooleanPrimitiveListInPerson::class.java, StoreType.SYNC, client)
        val person = BooleanPrimitiveListInPerson(TEST_USERNAME)
        val booleanArrayList = ArrayList<Boolean>()
        booleanArrayList.add(true)
        booleanArrayList.add(false)
        person.booleanList = booleanArrayList
        val saveCallback: CustomKinveyClientCallback<BooleanPrimitiveListInPerson> = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.booleanList)
        assertTrue(saveCallback.result?.booleanList?.size == 2)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveStringArrayToServer() {
        val testManager = TestManager<StringPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, StringPrimitiveListInPerson::class.java, StoreType.NETWORK, client)
        val person = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList = ArrayList<String>()
        stringList.add(TEST_STRING)
        stringList.add("987654321")
        person.stringList = stringList
        val saveCallback: CustomKinveyClientCallback<StringPrimitiveListInPerson> = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.stringList)
        assertTrue(saveCallback.result?.stringList?.size == 2)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testInQueryOperatorOnFindMethodLocallyInListOfString() {
        val testManager = TestManager<StringPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, StringPrimitiveListInPerson::class.java, StoreType.SYNC, client)
        val person = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList = ArrayList<String>()
        stringList.add(TEST_STRING)
        stringList.add("987654321")
        person.stringList = stringList
        var saveCallback: CustomKinveyClientCallback<StringPrimitiveListInPerson> = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.stringList)
        val person2 = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList2 = ArrayList<String>()
        stringList2.add(TEST_STRING)
        stringList2.add("11111")
        person2.stringList = stringList2
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.stringList)
        var query = Query().`in`(STRING_LIST_FIELD, arrayOf(TEST_STRING))
        var findCallback: CustomKinveyReadCallback<StringPrimitiveListInPerson> = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 2)
        query = Query().`in`(STRING_LIST_FIELD, arrayOf("987654321"))
        findCallback = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testInQueryOperatorOnFindMethodLocallyInListOfStringWithTwoParameters() {
        val testManager = TestManager<StringPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, StringPrimitiveListInPerson::class.java, StoreType.SYNC, client)
        val person = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList = ArrayList<String>()
        stringList.add(TEST_STRING)
        stringList.add("987654321")
        person.stringList = stringList
        var saveCallback: CustomKinveyClientCallback<StringPrimitiveListInPerson> = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.stringList)
        val person2 = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList2 = ArrayList<String>()
        stringList2.add(TEST_STRING)
        stringList2.add("11111")
        person2.stringList = stringList2
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.stringList)
        val person3 = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList3 = ArrayList<String>()
        stringList3.add("2222")
        stringList3.add("11111")
        person3.stringList = stringList3
        saveCallback = testManager.saveCustom(store, person3)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.stringList)
        val query: Query
        val findCallback: CustomKinveyReadCallback<StringPrimitiveListInPerson>
        query = Query().`in`(STRING_LIST_FIELD, arrayOf("987654321", TEST_STRING))
        findCallback = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 2)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testInQueryOperatorOnFindMethodLocallyInListOfInteger() {
        val testManager = TestManager<IntegerPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, IntegerPrimitiveListInPerson::class.java, StoreType.SYNC, client)
        val person = IntegerPrimitiveListInPerson(TEST_USERNAME)
        val integerArrayList = ArrayList<Int>()
        integerArrayList.add(TEST_INTEGER)
        integerArrayList.add(987654321)
        person.integerList = integerArrayList
        var saveCallback: CustomKinveyClientCallback<IntegerPrimitiveListInPerson> = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.integerList)
        val person2 = IntegerPrimitiveListInPerson(TEST_USERNAME)
        val integerArrayList2 = ArrayList<Int>()
        integerArrayList2.add(TEST_INTEGER)
        integerArrayList2.add(11111)
        person2.integerList = integerArrayList2
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.integerList)
        var query = Query().`in`(INTEGER_LIST_FIELD, arrayOf(TEST_INTEGER))
        var findCallback: CustomKinveyReadCallback<IntegerPrimitiveListInPerson> = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 2)
        query = Query().`in`(INTEGER_LIST_FIELD, arrayOf(987654321))
        findCallback = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testInQueryOperatorOnFindMethodLocallyInListOfLong() {
        val testManager = TestManager<LongPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, LongPrimitiveListInPerson::class.java, StoreType.SYNC, client)
        val person = LongPrimitiveListInPerson(TEST_USERNAME)
        val longArrayList = ArrayList<Long>()
        longArrayList.add(TEST_LONG)
        longArrayList.add(987654321L)
        person.longList = longArrayList
        var saveCallback: CustomKinveyClientCallback<LongPrimitiveListInPerson> = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.longList)
        val person2 = LongPrimitiveListInPerson(TEST_USERNAME)
        val longArrayList1 = ArrayList<Long>()
        longArrayList1.add(TEST_LONG)
        longArrayList1.add(11111L)
        person2.longList = longArrayList1
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.longList)
        var query = Query().`in`(LONG_LIST_FIELD, arrayOf(TEST_LONG))
        var findCallback: CustomKinveyReadCallback<LongPrimitiveListInPerson> = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 2)
        query = Query().`in`(LONG_LIST_FIELD, arrayOf(987654321L))
        findCallback = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testInQueryOperatorOnFindMethodLocally() {
        val testManager = TestManager<EntityForInQueryTest>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(EntityForInQueryTest.COLLECTION, EntityForInQueryTest::class.java, StoreType.SYNC, client)
        val firstTestEntity = EntityForInQueryTest()
        firstTestEntity.longVal = 1L
        firstTestEntity.stringVal = "test_string"
        firstTestEntity.booleanVal = true
        firstTestEntity.intVal = 2
        firstTestEntity.floatVal = 3f
        var saveCallback: CustomKinveyClientCallback<EntityForInQueryTest> = testManager.saveCustom(store, firstTestEntity)
        assertNotNull(saveCallback.result)
        val secondTestEntity = EntityForInQueryTest()
        secondTestEntity.longVal = 4L
        secondTestEntity.stringVal = "test_string_2"
        secondTestEntity.booleanVal = false
        secondTestEntity.intVal = 5
        secondTestEntity.floatVal = 6f
        saveCallback = testManager.saveCustom(store, secondTestEntity)
        assertNotNull(saveCallback.result)
        var query = Query().`in`("longVal", arrayOf(1L))
        var findCallback: CustomKinveyReadCallback<EntityForInQueryTest> = testManager.findCustom(store, query)
        var resultList = findCallback.result?.result
        assertNotNull(resultList)
        assertTrue(resultList?.size == 1)
        query = Query().`in`("stringVal", arrayOf("test_string"))
        findCallback = testManager.findCustom(store, query)
        resultList = findCallback.result?.result
        assertNotNull(resultList)
        assertTrue(resultList?.size == 1)
        query = Query().`in`("booleanVal", arrayOf(true))
        findCallback = testManager.findCustom(store, query)
        resultList = findCallback.result?.result
        assertNotNull(resultList)
        assertTrue(resultList?.size == 1)
        query = Query().`in`("intVal", arrayOf(5))
        findCallback = testManager.findCustom(store, query)
        resultList = findCallback.result?.result
        assertNotNull(resultList)
        assertTrue(resultList?.size == 1)
        query = Query().`in`("floatVal", arrayOf(3f))
        findCallback = testManager.findCustom(store, query)
        resultList = findCallback.result?.result
        assertNotNull(resultList)
        assertTrue(resultList?.size == 1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testInQueryOperatorOnFindMethodLocallyInListOfBoolean() {
        val testManager = TestManager<BooleanPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, BooleanPrimitiveListInPerson::class.java, StoreType.SYNC, client)
        val person = BooleanPrimitiveListInPerson(TEST_USERNAME)
        val booleanArrayList = ArrayList<Boolean>()
        booleanArrayList.add(TEST_BOOLEAN)
        booleanArrayList.add(false)
        person.booleanList = booleanArrayList
        var saveCallback: CustomKinveyClientCallback<BooleanPrimitiveListInPerson> = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.booleanList)
        val person2 = BooleanPrimitiveListInPerson(TEST_USERNAME)
        val booleanArrayList2 = ArrayList<Boolean>()
        booleanArrayList2.add(TEST_BOOLEAN)
        booleanArrayList2.add(true)
        person2.booleanList = booleanArrayList2
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.booleanList)
        var query = Query().`in`(BOOLEAN_LIST_FIELD, arrayOf(TEST_BOOLEAN))
        var findCallback: CustomKinveyReadCallback<BooleanPrimitiveListInPerson> = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 2)
        query = Query().`in`(BOOLEAN_LIST_FIELD, arrayOf(false))
        findCallback = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testInQueryOperatorOnFindMethodLocallyInListOfFloat() {
        val testManager = TestManager<FloatPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, FloatPrimitiveListInPerson::class.java, StoreType.SYNC, client)
        val person = FloatPrimitiveListInPerson(TEST_USERNAME)
        val floatArrayList = ArrayList<Float>()
        floatArrayList.add(TEST_FLOAT)
        floatArrayList.add(987654321.1f)
        person.floatList = floatArrayList
        var saveCallback: CustomKinveyClientCallback<FloatPrimitiveListInPerson> = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.floatList)
        val person2 = FloatPrimitiveListInPerson(TEST_USERNAME)
        val floatArrayList2 = ArrayList<Float>()
        floatArrayList2.add(TEST_FLOAT)
        floatArrayList2.add(11111.1f)
        person2.floatList = floatArrayList2
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.floatList)
        var query = Query().`in`(FLOAT_LIST_FIELD, arrayOf(TEST_FLOAT))
        var findCallback: CustomKinveyReadCallback<FloatPrimitiveListInPerson> = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 2)
        query = Query().`in`(FLOAT_LIST_FIELD, arrayOf(987654321.1f))
        findCallback = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testInWithOtherOperators() {
        val testManager = TestManager<StringPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, StringPrimitiveListInPerson::class.java, StoreType.SYNC, client)
        val person = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList = ArrayList<String>()
        stringList.add(TEST_STRING)
        stringList.add("987654321")
        person.stringList = stringList
        val person2 = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList2 = ArrayList<String>()
        stringList2.add(TEST_STRING)
        stringList2.add("567483912")
        person2.stringList = stringList2
        val person3 = StringPrimitiveListInPerson("NEW_PERSON")
        val stringList3 = ArrayList<String>()
        stringList3.add(TEST_STRING)
        stringList3.add("567483912")
        person3.stringList = stringList3
        val person4 = StringPrimitiveListInPerson("NEW_PERSON")
        val stringList4 = ArrayList<String>()
        stringList4.add("567483912")
        stringList4.add("567483912")
        person3.stringList = stringList4
        var saveCallback: CustomKinveyClientCallback<StringPrimitiveListInPerson> = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person3)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person4)
        assertNotNull(saveCallback.result)
        var findCallback: CustomKinveyReadCallback<*>
        var query: Query
        query = Query().equals("username", "NEW_PERSON")
        findCallback = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 2)
        query = Query().`in`(STRING_LIST_FIELD, arrayOf(TEST_STRING)).equals("username", "NEW_PERSON")
        findCallback = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testGetFirstMethodWithInOperator() {
        val query = Query().`in`(STRING_LIST_FIELD, arrayOf(TEST_STRING)).equals("username", "NEW_PERSON")
        testGetFirst(query)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testGetFirstMethodWithOutInOperator() {
        val query = Query().equals("username", "NEW_PERSON")
        testGetFirst(query)
    }

    @Throws(InterruptedException::class)
    private fun testGetFirst(query: Query) {
        val testManager = TestManager<StringPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, StringPrimitiveListInPerson::class.java, StoreType.SYNC, client)
        val person = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList = ArrayList<String>()
        stringList.add(TEST_STRING)
        stringList.add("987654321")
        person.stringList = stringList
        val person2 = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList2 = ArrayList<String>()
        stringList2.add(TEST_STRING)
        stringList2.add("567483912")
        person2.stringList = stringList2
        val person3 = StringPrimitiveListInPerson("NEW_PERSON")
        val stringList3 = ArrayList<String>()
        stringList3.add(TEST_STRING)
        stringList3.add("567483912")
        person3.stringList = stringList3
        val person4 = StringPrimitiveListInPerson("NEW_PERSON")
        val stringList4 = ArrayList<String>()
        stringList4.add("567483912")
        stringList4.add("567483912")
        person4.stringList = stringList4
        val person5 = StringPrimitiveListInPerson("NEW_PERSON")
        val stringList5 = ArrayList<String>()
        stringList5.add(TEST_STRING)
        stringList5.add("567483912")
        person3.stringList = stringList5
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person3)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person4)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person5)
        assertNotNull(saveCallback.result)
        val firstPerson = client?.cacheManager?.getCache(Person.COLLECTION,
                StringPrimitiveListInPerson::class.java, Long.MAX_VALUE)?.getFirst(query)
        assertNotNull(firstPerson)
        assertTrue(firstPerson?.id == person3.id)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteMethod() {
        // Arrange
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person1 = Person("Person1")
        person1.age = "1"
        val person2 = Person("Person2")
        person2.age = "2"
        val person3 = Person("Person3")
        person3.age = "3"
        val person4 = Person("Person4")
        person4.age = "4"
        var saveCallback = testManager.save(store, person1)
        assertNotNull(saveCallback?.result)
        saveCallback = testManager.save(store, person2)
        assertNotNull(saveCallback?.result)
        saveCallback = testManager.save(store, person3)
        assertNotNull(saveCallback?.result)
        saveCallback = testManager.save(store, person4)
        assertNotNull(saveCallback?.result)
        val allItemsBefore = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.get()?.size
        assertTrue(allItemsBefore == 4)

        // Act
        val query = Query().equals("username", "Person2")
        val deletedPersonCount = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.delete(query)
        val allItemsAfter = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.get()?.size
        val itemsAfter = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.get()

        // Assert
        assertTrue(deletedPersonCount == 1)
        assertTrue(allItemsAfter == 3)
        assertNotNull(itemsAfter)
        val username1 = itemsAfter?.get(0)?.username
        val username2 = itemsAfter?.get(1)?.username
        val username3 = itemsAfter?.get(2)?.username
        assertTrue(username1.equals("Person1", ignoreCase = true) || username1.equals("Person3", ignoreCase = true) || username1.equals("Person4", ignoreCase = true))
        assertTrue(username2.equals("Person1", ignoreCase = true) || username2.equals("Person3", ignoreCase = true) || username2.equals("Person4", ignoreCase = true))
        assertTrue(username3.equals("Person1", ignoreCase = true) || username3.equals("Person3", ignoreCase = true) || username3.equals("Person4", ignoreCase = true))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteMethodSkip1() {
        // Arrange
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person1 = Person("Person1")
        person1.age = "1"
        val person2 = Person("Person2")
        person2.age = "2"
        val person3 = Person("Person3")
        person3.age = "3"
        val person4 = Person("Person4")
        person4.age = "4"
        var saveCallback = testManager.save(store, person1)
        assertNotNull(saveCallback?.result)
        saveCallback = testManager.save(store, person2)
        assertNotNull(saveCallback?.result)
        saveCallback = testManager.save(store, person3)
        assertNotNull(saveCallback?.result)
        saveCallback = testManager.save(store, person4)
        assertNotNull(saveCallback?.result)
        val allItemsBefore = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.get()?.size
        assertTrue(allItemsBefore == 4)

        // Act
        val query = Query().setSkip(1)
        val deletedPersonCount = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.delete(query)
        val allItemsAfter = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.get()?.size
        val itemsAfter = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.get()

        // Assert
        assertTrue(deletedPersonCount == 3)
        assertTrue(allItemsAfter == 1)
        assertNotNull(itemsAfter)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteMethodSkip1Limit2() {
        // Arrange
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person1 = Person("Person1")
        person1.age = "1"
        val person2 = Person("Person2")
        person2.age = "2"
        val person3 = Person("Person3")
        person3.age = "3"
        val person4 = Person("Person4")
        person4.age = "4"
        var saveCallback = testManager.save(store, person1)
        assertNotNull(saveCallback?.result)
        saveCallback = testManager.save(store, person2)
        assertNotNull(saveCallback?.result)
        saveCallback = testManager.save(store, person3)
        assertNotNull(saveCallback?.result)
        saveCallback = testManager.save(store, person4)
        assertNotNull(saveCallback?.result)
        val allItemsBefore = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.get()?.size
        assertTrue(allItemsBefore == 4)

        // Act
        val query = Query().setSkip(1).setLimit(2)
        val deletedPersonCount = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.delete(query)
        val allItemsAfter = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.get()?.size
        val itemsAfter = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.get()

        // Assert
        assertTrue(deletedPersonCount == 2)
        assertTrue(allItemsAfter == 2)
        assertNotNull(itemsAfter)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteMethodSkip1LimitExceedsSize() {
        // Arrange
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person1 = Person("Person1")
        person1.age = "1"
        val person2 = Person("Person2")
        person2.age = "2"
        val person3 = Person("Person3")
        person3.age = "3"
        val person4 = Person("Person4")
        person4.age = "4"
        var saveCallback = testManager.save(store, person1)
        assertNotNull(saveCallback?.result)
        saveCallback = testManager.save(store, person2)
        assertNotNull(saveCallback?.result)
        saveCallback = testManager.save(store, person3)
        assertNotNull(saveCallback?.result)
        saveCallback = testManager.save(store, person4)
        assertNotNull(saveCallback?.result)
        val allItemsBefore = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.get()?.size
        assertTrue(allItemsBefore == 4)

        // Act
        val query = Query().setSkip(1).setLimit(5)
        val deletedPersonCount = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.delete(query)
        val allItemsAfter = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.get()?.size
        val itemsAfter = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.get()

        // Assert
        assertTrue(deletedPersonCount == 3)
        assertTrue(allItemsAfter == 1)
        assertNotNull(itemsAfter)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteMethodSkipExceedsSize() {
        // Arrange
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person1 = Person("Person1")
        person1.age = "1"
        val person2 = Person("Person2")
        person2.age = "2"
        val person3 = Person("Person3")
        person3.age = "3"
        val person4 = Person("Person4")
        person4.age = "4"
        var saveCallback = testManager.save(store, person1)
        assertNotNull(saveCallback?.result)
        saveCallback = testManager.save(store, person2)
        assertNotNull(saveCallback?.result)
        saveCallback = testManager.save(store, person3)
        assertNotNull(saveCallback?.result)
        saveCallback = testManager.save(store, person4)
        assertNotNull(saveCallback?.result)
        val allItemsBefore = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.get()?.size
        assertTrue(allItemsBefore == 4)

        // Act
        val query = Query().setSkip(4)
        val deletedPersonCount = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.delete(query)
        val allItemsAfter = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.get()?.size
        val itemsAfter = client?.cacheManager?.getCache(Person.COLLECTION, Person::class.java, Long.MAX_VALUE)?.get()

        // Assert
        assertTrue(deletedPersonCount == 0)
        assertTrue(allItemsAfter == 4)
        assertNotNull(itemsAfter)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteMethodWithInOperator() {
        val testManager = TestManager<StringPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, StringPrimitiveListInPerson::class.java, StoreType.SYNC, client)
        val person = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList = ArrayList<String>()
        stringList.add(TEST_STRING)
        stringList.add("987654321")
        person.stringList = stringList
        val person2 = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList2 = ArrayList<String>()
        stringList2.add(TEST_STRING)
        stringList2.add("567483912")
        person2.stringList = stringList2
        val person3 = StringPrimitiveListInPerson("NEW_PERSON")
        val stringList3 = ArrayList<String>()
        stringList3.add(TEST_STRING)
        stringList3.add("567483912")
        person3.stringList = stringList3
        val person4 = StringPrimitiveListInPerson("NEW_PERSON")
        val stringList4 = ArrayList<String>()
        stringList4.add("567483912")
        stringList4.add("567483912")
        person3.stringList = stringList4
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person3)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person4)
        assertNotNull(saveCallback.result)
        var allItems = client?.cacheManager?.getCache(Person.COLLECTION, StringPrimitiveListInPerson::class.java, Long.MAX_VALUE)?.get()?.size
        assertTrue(allItems == 4)
        val query = Query().`in`(STRING_LIST_FIELD, arrayOf(TEST_STRING)).equals("username", "NEW_PERSON")
        val deletedItems = client?.cacheManager?.getCache(Person.COLLECTION, StringPrimitiveListInPerson::class.java, Long.MAX_VALUE)?.delete(query)
        assertTrue(deletedItems == 1)
        allItems = client?.cacheManager?.getCache(Person.COLLECTION, StringPrimitiveListInPerson::class.java, Long.MAX_VALUE)?.get()?.size
        assertTrue(allItems == 3)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testCountMethodWithInOperator() {
        val testManager = TestManager<StringPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, StringPrimitiveListInPerson::class.java, StoreType.SYNC, client)
        val person = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList = ArrayList<String>()
        stringList.add(TEST_STRING)
        stringList.add("987654321")
        person.stringList = stringList
        val person2 = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList2 = ArrayList<String>()
        stringList2.add(TEST_STRING)
        stringList2.add("567483912")
        person2.stringList = stringList2
        val person3 = StringPrimitiveListInPerson("NEW_PERSON")
        val stringList3 = ArrayList<String>()
        stringList3.add(TEST_STRING)
        stringList3.add("567483912")
        person3.stringList = stringList3
        val person4 = StringPrimitiveListInPerson("NEW_PERSON")
        val stringList4 = ArrayList<String>()
        stringList4.add("567483912")
        stringList4.add("567483912")
        person3.stringList = stringList4
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person3)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person4)
        assertNotNull(saveCallback.result)
        val query = Query().`in`(STRING_LIST_FIELD, arrayOf(TEST_STRING)).equals("username", "NEW_PERSON")
        val i = client?.cacheManager?.getCache(Person.COLLECTION, StringPrimitiveListInPerson::class.java, Long.MAX_VALUE)?.count(query)
        assertTrue(i == 1L)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testGetMethodWithInOperator() {
        val testManager = TestManager<StringPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, StringPrimitiveListInPerson::class.java, StoreType.SYNC, client)
        val person = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList = ArrayList<String>()
        stringList.add(TEST_STRING)
        stringList.add("987654321")
        person.stringList = stringList
        val person2 = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList2 = ArrayList<String>()
        stringList2.add(TEST_STRING)
        stringList2.add("567483912")
        person2.stringList = stringList2
        val person3 = StringPrimitiveListInPerson("NEW_PERSON")
        val stringList3 = ArrayList<String>()
        stringList3.add(TEST_STRING)
        stringList3.add("567483912")
        person3.stringList = stringList3
        val person4 = StringPrimitiveListInPerson("NEW_PERSON")
        val stringList4 = ArrayList<String>()
        stringList4.add("567483912")
        stringList4.add("567483912")
        person4.stringList = stringList4
        val person5 = StringPrimitiveListInPerson("NEW_PERSON")
        val stringList5 = ArrayList<String>()
        stringList5.add(TEST_STRING)
        stringList5.add("567483912")
        person3.stringList = stringList5
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person3)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person4)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person5)
        assertNotNull(saveCallback.result)
        val query = Query().`in`(STRING_LIST_FIELD, arrayOf(TEST_STRING)).equals("username", "NEW_PERSON")
        val persons = client?.cacheManager
                ?.getCache(Person.COLLECTION, StringPrimitiveListInPerson::class.java, Long.MAX_VALUE)?.get(query)
        assertNotNull(persons)
        assertTrue(persons?.size == 2)
    }

    @Test
    @Ignore // TODO: 21.09.2017 "NOTIN" not updated yet for list of primitives field
    @Throws(InterruptedException::class)

    fun testGetMethodWithNotInOperator() {
        val testManager = TestManager<StringPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, StringPrimitiveListInPerson::class.java, StoreType.SYNC, client)
        val person = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList = ArrayList<String>()
        stringList.add(TEST_STRING)
        stringList.add("987654321")
        person.stringList = stringList
        val person2 = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList2 = ArrayList<String>()
        stringList2.add(TEST_STRING)
        stringList2.add("567483912")
        person2.stringList = stringList2
        val person3 = StringPrimitiveListInPerson("NEW_PERSON")
        val stringList3 = ArrayList<String>()
        stringList3.add(TEST_STRING)
        stringList3.add("567483912")
        person3.stringList = stringList3
        val person4 = StringPrimitiveListInPerson("NEW_PERSON")
        val stringList4 = ArrayList<String>()
        stringList4.add("567483912")
        stringList4.add("567483912")
        person4.stringList = stringList4
        val person5 = StringPrimitiveListInPerson("NEW_PERSON")
        val stringList5 = ArrayList<String>()
        stringList5.add(TEST_STRING)
        stringList5.add("567483912")
        person3.stringList = stringList5
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person3)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person4)
        assertNotNull(saveCallback.result)
        saveCallback = testManager.saveCustom(store, person5)
        assertNotNull(saveCallback.result)
        val query = Query().notIn(STRING_LIST_FIELD, arrayOf<String?>(TEST_STRING))
        val persons = client?.cacheManager?.getCache(Person.COLLECTION, StringPrimitiveListInPerson::class.java, Long.MAX_VALUE)?.get(query)
        assertNotNull(persons)
        assertTrue(persons?.size == 1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveObjectsArrayToRealmLocally() {
        val testManager = TestManager<ObjectListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, ObjectListInPerson::class.java, StoreType.SYNC, client)
        val person = ObjectListInPerson(TEST_USERNAME)
        val stringGenericJsons = ArrayList<StringGenericJson>()
        stringGenericJsons.add(StringGenericJson(TEST_STRING))
        stringGenericJsons.add(StringGenericJson("987654321"))
        person.stringGenericJsons = stringGenericJsons
        val person2 = ObjectListInPerson(TEST_USERNAME)
        val stringGenericJsons2 = ArrayList<StringGenericJson>()
        stringGenericJsons2.add(StringGenericJson("987654321"))
        stringGenericJsons2.add(StringGenericJson("9876543211111"))
        person2.stringGenericJsons = stringGenericJsons2
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.stringGenericJsons)
        assertTrue(saveCallback.result?.stringGenericJsons?.size == 2)
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.stringGenericJsons)
        assertTrue(saveCallback.result?.stringGenericJsons?.size == 2)
        val query = Query().`in`("stringGenericJsons.string", arrayOf(TEST_STRING))
        val findCallback = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 1)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDeleteObjectsArrayToRealmLocally() {
        val testManager = TestManager<ObjectListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, ObjectListInPerson::class.java, StoreType.SYNC, client)
        val person = ObjectListInPerson(TEST_USERNAME)
        val stringGenericJsons = ArrayList<StringGenericJson>()
        stringGenericJsons.add(StringGenericJson(TEST_STRING))
        stringGenericJsons.add(StringGenericJson("987654321"))
        person.stringGenericJsons = stringGenericJsons
        val person2 = ObjectListInPerson(TEST_USERNAME)
        val stringGenericJsons2 = ArrayList<StringGenericJson>()
        stringGenericJsons2.add(StringGenericJson("987654321"))
        stringGenericJsons2.add(StringGenericJson("9876543211111"))
        person2.stringGenericJsons = stringGenericJsons2
        val saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.stringGenericJsons)
        assertTrue(saveCallback.result?.stringGenericJsons?.size == 2)
        val saveCallback2 = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback2.result)
        assertNotNull(saveCallback2.result?.stringGenericJsons)
        assertTrue(saveCallback2.result?.stringGenericJsons?.size == 2)
        val query = Query().`in`("stringGenericJsons.string", arrayOf(TEST_STRING))
        val findCallback = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 1)
        var callback = testManager.deleteCustom(store, query)
        assertNotNull(callback)
        assertNotNull(callback.result)
        assertNull(callback.error)
        assertTrue(store.count() == 1)
        callback = testManager.deleteCustom(store, saveCallback2.result?.id)
        assertNotNull(callback)
        assertNotNull(callback.result)
        assertNull(callback.error)
        assertTrue(store.count() == 0)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testMissingLeftHandSideOfOR() {
        val testManager = TestManager<StringPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        testSaveStringArrayToServer()
        val store = collection(Person.COLLECTION, StringPrimitiveListInPerson::class.java, StoreType.SYNC, client)
        val query = Query().`in`(STRING_LIST_FIELD, arrayOf(TEST_STRING))
        val pullCallback = testManager.pullCustom(store, query)
        assertNotNull(pullCallback.result)
        val listCallback = testManager.findCustom(store, query)
        assertNotNull(listCallback.result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testInQueryOperatorOnFindMethodLocallyComplexSearch() {
        val testManager = TestManager<StringPrimitiveListInPerson>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, StringPrimitiveListInPerson::class.java, StoreType.SYNC, client)
        val person = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList = ArrayList<String>()
        stringList.add(TEST_STRING)
        stringList.add("987654321")
        person.stringList = stringList
        var saveCallback = testManager.saveCustom(store, person)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.stringList)
        val person2 = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList2 = ArrayList<String>()
        person2.age = "25"
        stringList2.add(TEST_STRING)
        stringList2.add("11111")
        person2.stringList = stringList2
        saveCallback = testManager.saveCustom(store, person2)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.stringList)
        val person3 = StringPrimitiveListInPerson(TEST_USERNAME)
        val stringList3 = ArrayList<String>()
        stringList3.add("7654321")
        stringList3.add("1234567")
        person3.stringList = stringList3
        saveCallback = testManager.saveCustom(store, person3)
        assertNotNull(saveCallback.result)
        assertNotNull(saveCallback.result?.stringList)
        var query: Query
        var findCallback: CustomKinveyReadCallback<StringPrimitiveListInPerson>
        query = Query().equals("_acl.creator", client?.activeUser?.id)
        findCallback = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 3)
        query = Query().`in`(STRING_LIST_FIELD, arrayOf("11111", "987654321"))
        findCallback = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 2)
        query = Query().equals("_acl.creator", client?.activeUser?.id).and(Query().`in`(STRING_LIST_FIELD, arrayOf("11111", "987654321")))
        findCallback = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 2)
        query = Query().equals("_acl.creator", client?.activeUser?.id).or(Query().`in`(STRING_LIST_FIELD, arrayOf("11111", "987654321")))
        findCallback = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 3)
        query = Query().equals("age", "25").or(Query().`in`(STRING_LIST_FIELD, arrayOf("11111", "987654321")))
        findCallback = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 3)
        query = Query().equals("age", "25").and(Query().`in`(STRING_LIST_FIELD, arrayOf("11111", "987654321")))
        findCallback = testManager.findCustom(store, query)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 1)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFillingIntField() {
        val testManager = TestManager<Person>()
        testManager.login(USERNAME, PASSWORD, client)
        val store = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person = Person(TEST_USERNAME)
        person.intVal = 1
        val clientCallback: DefaultKinveyClientCallback = testManager.save(store, person)
        assertEquals(person.intVal, clientCallback.result?.intVal)
        val readCallback: DefaultKinveyReadCallback = testManager.find(store, client?.query())
        assertNotNull(readCallback)
        assertNull(readCallback.error)
        assertNotNull(readCallback.result)
        assertEquals(1, readCallback.result?.result?.size)
        assertEquals(1, readCallback.result?.result?.get(0)?.intVal)
    }

    companion object {
        private const val TEST_STRING = "123456789"
        private const val TEST_FLOAT = 123456789.1f
        private const val TEST_INTEGER = 123456789
        private const val TEST_LONG = 123456789L
        private const val TEST_BOOLEAN = true
        private const val STRING_LIST_FIELD = "stringList"
        private const val FLOAT_LIST_FIELD = "floatList"
        private const val INTEGER_LIST_FIELD = "integerList"
        private const val LONG_LIST_FIELD = "longList"
        private const val BOOLEAN_LIST_FIELD = "booleanList"
        private const val STRING_GENERIC_JSON_LIST_FIELD = "stringGenericJsons"
    }
}