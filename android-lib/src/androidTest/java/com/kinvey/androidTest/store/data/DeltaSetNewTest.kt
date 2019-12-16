package com.kinvey.androidTest.store.data

import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.Client.Companion.kinveyHandlerThread
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore
import com.kinvey.android.store.DataStore.Companion.collection
import com.kinvey.androidTest.TestManager
import com.kinvey.androidTest.TestManager.*
import com.kinvey.androidTest.model.Person
import com.kinvey.java.Constants
import com.kinvey.java.Query
import com.kinvey.java.model.KinveyCountResponse
import com.kinvey.java.store.BaseDataStore
import com.kinvey.java.store.QueryCacheItem
import com.kinvey.java.store.StoreType
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by yuliya on 05/28/18.
 */

/* MLIBZ-2470 Test Server-side Delta Set use cases */
@RunWith(AndroidJUnit4::class)
@SmallTest
class DeltaSetNewTest {
    private var client: Client<*>? = null
    private var testManager: TestManager<Person>? = null
    private var store: DataStore<Person>? = null
    private var emptyQuery: Query? = null
    private var usernameQuery: Query? = null

    @Before
    @Throws(InterruptedException::class)
    fun setUp() {
        val mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        testManager = TestManager()
        testManager?.login(USERNAME, PASSWORD, client)
        emptyQuery = client?.query()
        usernameQuery = client?.query()?.equals("username", TEST_USERNAME)
    }

    @After
    fun tearDown() {
        client?.performLockDown()
        if (kinveyHandlerThread != null) {
            try {
                client?.stopKinveyHandlerThread()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    /* The goal of the test is to make sure delta set is not used when not enabled */
    @Test
    @Throws(InterruptedException::class)
    fun testDeltaSetIsNotUsed() {
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager?.cleanBackend(store, StoreType.SYNC)
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.push(store)
        var pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(1, pullCallback?.result?.count)
        testManager?.save(store, Person(TEST_USERNAME_2))
        testManager?.push(store)
        pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(2, pullCallback?.result?.count)
        assertFalse(store?.isDeltaSetCachingEnabled == true)
    }

    /* The goal of the test is to confirm that empty array will be returned
    if the user has no changes and the since is respected */
    @Test
    @Throws(InterruptedException::class)
    fun testEmptyArrayIsReturnedIfNoChanges() {
        initDeltaSetCachedCollection(StoreType.SYNC)
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.push(store)
        var pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(1, pullCallback?.result?.count)
        pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(0, pullCallback?.result?.count)
        assertEquals(1, testManager?.find(store, emptyQuery)?.result?.result?.size)
    }

    /* The test aims to confirm that since is respected and changes are handled properly */
    @Test
    @Throws(InterruptedException::class)
    fun testNewItemHandling() {
        initDeltaSetCachedCollection(StoreType.SYNC)
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.push(store)
        var pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(1, pullCallback?.result?.count)
        testManager?.save(store, Person(TEST_USERNAME_2))
        testManager?.push(store)
        pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(1, pullCallback?.result?.count)
        assertEquals(2, testManager?.find(store, emptyQuery)?.result?.result?.size)
    }

    /* The test aims to confirm the correct use of the since param and
    its update in the queryCache table */
    /* with enabled deltaset should return correct number of items when deleting and updating */
    @Test
    @Throws(InterruptedException::class)
    fun testPullStoreTypeSync_DeletingUpdating() {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        initDeltaSetCachedCollection(StoreType.SYNC)
        testManager?.save(networkStore, Person(TEST_USERNAME))
        testManager?.save(networkStore, Person(TEST_USERNAME_2))
        var pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(2, pullCallback?.result?.count)
        deleteItem(networkStore)
        updateItem(networkStore)
        pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(1, pullCallback?.result?.count)
        assertEquals(1, testManager?.find(store, emptyQuery)?.result?.result?.size)
    }

    /* The test aims to confirm the correct use of deltaset in combination with queries */
    // TODO: 28.5.18 It would be good to add tests for complex and nested queries, to assure they are recorded and used properly in the queryCache table
    @Test
    @Throws(InterruptedException::class)
    fun testQueryAfterDeleting() {
        initDeltaSetCachedCollection(StoreType.SYNC)
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME_2))
        testManager?.push(store)
        var pullCallback = testManager?.pullCustom(store, usernameQuery)
        assertEquals(2, pullCallback?.result?.count)
        deleteItem(usernameQuery)
        testManager?.push(store)
        pullCallback = testManager?.pullCustom(store, usernameQuery)
        assertEquals(0, pullCallback?.result?.count)
        assertEquals(1, testManager?.find(store, usernameQuery)?.result?.result?.size)
    }

    /* The test aims to confirm the correct use of deltaset in combination with queries */
    // TODO: 28.5.18 It would be good to add tests for complex and nested queries, to assure they are recorded and used properly in the queryCache table
    @Test
    @Throws(InterruptedException::class)
    fun testQueryAfterUpdating() {
        initDeltaSetCachedCollection(StoreType.SYNC)
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME_2))
        testManager?.push(store)
        var pullCallback = testManager?.pullCustom(store, usernameQuery)
        assertEquals(2, pullCallback?.result?.count)
        assertEquals(2, testManager?.find(store, usernameQuery)?.result?.result?.size)
        updateItem(usernameQuery)
        testManager?.push(store)
        pullCallback = testManager?.pullCustom(store, usernameQuery)
        assertEquals(1, pullCallback?.result?.count)
        assertEquals(2, testManager?.find(store, usernameQuery)?.result?.result?.size)
    }

    /* The test aims to confirm the correct behavior for disabling deltaset */
    @Test
    @Throws(InterruptedException::class)
    fun testDisablingDeltaSet() {
        initDeltaSetCachedCollection(StoreType.SYNC)
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.push(store)
        var pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(1, pullCallback?.result?.count)
        pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(0, pullCallback?.result?.count)
        store?.isDeltaSetCachingEnabled = false
        var syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(1, syncCallback?.result?.count)
        syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(1, syncCallback?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncAfterCreating() {
        initDeltaSetCachedCollection(StoreType.SYNC)
        testManager?.save(store, Person(TEST_USERNAME))
        var syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(1, syncCallback?.result?.count)
        assertEquals(1, testManager?.find(store, emptyQuery)?.result?.result?.size)
        testManager?.save(store, Person(TEST_USERNAME_2))
        syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(1, syncCallback?.result?.count)
        assertEquals(2, testManager?.find(store, emptyQuery)?.result?.result?.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncAfterUpdating() {
        initDeltaSetCachedCollection(StoreType.SYNC)
        testManager?.save(store, Person(TEST_USERNAME))
        var syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(1, syncCallback?.result?.count)
        updateItem()
        syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(1, syncCallback?.result?.count)
        assertEquals(1, testManager?.find(store, emptyQuery)?.result?.result?.size)
    }

    /* with enabled deltaset should return correct number of items when deleting and updating */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncStoreTypeSync_DeletingUpdating() {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        var syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(2, syncCallback?.result?.count)
        assertEquals(2, store?.count())
        deleteItem(networkStore)
        updateItem(networkStore)
        syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(1, syncCallback?.result?.count)
        assertEquals(1, store?.count())
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncWithQueryAfterDeleting() {
        initDeltaSetCachedCollection(StoreType.SYNC)
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME_2))
        var syncCallback = testManager?.sync(store, usernameQuery)
        assertEquals(2, syncCallback?.result?.count)
        deleteItem(usernameQuery)
        syncCallback = testManager?.sync(store, usernameQuery)
        assertEquals(0, syncCallback?.result?.count)
        assertEquals(1, testManager?.find(store, usernameQuery)?.result?.result?.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncWithQueryAfterUpdating() {
        initDeltaSetCachedCollection(StoreType.SYNC)
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME_2))
        var syncCallback = testManager?.sync(store, usernameQuery)
        assertEquals(2, syncCallback?.result?.count)
        updateItem(usernameQuery)
        syncCallback = testManager?.sync(store, usernameQuery)
        assertEquals(1, syncCallback?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncAfterDisablingDeltaSet() {
        initDeltaSetCachedCollection(StoreType.SYNC)
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.push(store)
        var syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(1, syncCallback?.result?.count)
        syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(0, syncCallback?.result?.count)
        store?.isDeltaSetCachingEnabled = false
        syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(1, syncCallback?.result?.count)
        syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(1, syncCallback?.result?.count)
    }

    /* The test aims at confirming that find with forceNetwork works as intended */
    @Test
    @Throws(InterruptedException::class)
    fun testForceNetwork() {
        initDeltaSetCachedCollection(StoreType.SYNC)
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME_2))
        testManager?.push(store)
        store?.storeType = StoreType.NETWORK
        val people = testManager?.find(store, emptyQuery)?.result?.result
        assertEquals(2, people?.size)
        val person = people?.get(0)
        person?.age = "20"
        testManager?.save(store, person)
        assertEquals(2, testManager?.find(store, emptyQuery)?.result?.result?.size)
        testManager?.delete(store, person?.id)
        assertEquals(1, testManager?.find(store, emptyQuery)?.result?.result?.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testForceNetworkWithQuery() {
        initDeltaSetCachedCollection(StoreType.SYNC)
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME_2))
        testManager?.push(store)
        store?.storeType = StoreType.NETWORK
        val people = testManager?.find(store, usernameQuery)?.result?.result
        assertEquals(2, people?.size)
        val person = people?.get(0)
        person?.age = "20"
        testManager?.save(store, person)
        assertEquals(2, testManager?.find(store, usernameQuery)?.result?.result?.size)
        testManager?.delete(store, person?.id)
        assertEquals(1, testManager?.find(store, usernameQuery)?.result?.result?.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullStoreTypeCacheDisabledDeltaSet() {
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.CACHE, client)
        testManager?.cleanBackend(store, StoreType.CACHE)
        testManager?.save(store, Person(TEST_USERNAME))
        var pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(1, pullCallback?.result?.count)
        testManager?.save(store, Person(TEST_USERNAME_2))
        pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(2, pullCallback?.result?.count)
        assertFalse(store?.isDeltaSetCachingEnabled == true)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullStoreTypeCache() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        testManager?.save(store, Person(TEST_USERNAME))
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        assertEquals(0, testManager?.pullCustom(store, emptyQuery)?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullStoreTypeCache_CreatingNewItem() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        testManager?.save(store, Person(TEST_USERNAME))
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        testManager?.save(store, Person(TEST_USERNAME))
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullStoreTypeCache_UpdatingItem() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        val person = testManager?.save(store, Person(TEST_USERNAME))?.result
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        person?.age = "20"
        testManager?.save(store, person)
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        testManager?.delete(store, person?.id)
        assertEquals(0, testManager?.pullCustom(store, emptyQuery)?.result?.count)
    }

    /* with enabled deltaset should return correct number of items when deleting and updating */
    @Test
    @Throws(InterruptedException::class)
    fun testPullStoreTypeCache_DeletingUpdating() {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        initDeltaSetCachedCollection(StoreType.CACHE)
        testManager?.save(networkStore, Person(TEST_USERNAME))
        testManager?.save(networkStore, Person(TEST_USERNAME_2))
        var pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(2, pullCallback?.result?.count)
        deleteItem(networkStore)
        updateItem(networkStore)
        pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(1, pullCallback?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullStoreTypeCache_DeletingOneOfThreeItems() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        val person = testManager?.save(store, Person(TEST_USERNAME))?.result
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME_2))
        assertEquals(2, testManager?.pullCustom(store, usernameQuery)?.result?.count)
        testManager?.delete(store, person?.id)
        assertEquals(0, testManager?.pullCustom(store, usernameQuery)?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullStoreTypeCache_Update() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        val person = testManager?.save(store, Person(TEST_USERNAME))?.result
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME_2))
        assertEquals(2, testManager?.pullCustom(store, usernameQuery)?.result?.count)
        person!!.age = "20"
        testManager?.save(store, person)
        assertEquals(1, testManager?.pullCustom(store, usernameQuery)?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullStoreTypeCache_DisablingDeltaSet() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        testManager?.save(store, Person(TEST_USERNAME))
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        assertEquals(0, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        store?.isDeltaSetCachingEnabled = false
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullStoreTypeCache_DoublePull() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        testManager?.save(store, Person(TEST_USERNAME))
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        assertEquals(0, testManager?.pullCustom(store, emptyQuery)?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncStoreTypeCache_CreateItem() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        val person = testManager?.save(store, Person(TEST_USERNAME))?.result
        assertEquals(1, testManager?.sync(store, emptyQuery)?.result?.count)
        testManager?.save(store, person)
        assertEquals(1, testManager?.sync(store, emptyQuery)?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncStoreTypeCache_UpdateItem() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        val person = testManager?.save(store, Person(TEST_USERNAME))?.result
        assertEquals(1, testManager?.sync(store, emptyQuery)?.result?.count)
        person?.age = "20"
        testManager?.save(store, person)
        assertEquals(1, testManager?.sync(store, emptyQuery)?.result?.count)
    }

    /* with enabled deltaset should return correct number of items when deleting and updating */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncStoreTypeCache_DeletingUpdating() {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.CACHE, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        var syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(2, syncCallback?.result?.count)
        assertEquals(2, store?.count())
        deleteItem(networkStore)
        updateItem(networkStore)
        syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(1, syncCallback?.result?.count)
        assertEquals(1, store?.count())
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncStoreTypeCache_DeleteItemWithQuery() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        val person = testManager?.save(store, Person(TEST_USERNAME))?.result
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME_2))
        assertEquals(2, testManager?.sync(store, usernameQuery)?.result?.count)
        testManager?.delete(store, person?.id)
        assertEquals(0, testManager?.sync(store, usernameQuery)?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncStoreTypeCache_UpdateItemWithQuery() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        val person = testManager?.save(store, Person(TEST_USERNAME))?.result
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME_2))
        assertEquals(2, testManager?.sync(store, usernameQuery)?.result?.count)
        person?.age = "20"
        testManager?.save(store, person)
        assertEquals(1, testManager?.sync(store, usernameQuery)?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncStoreTypeCache_DisablingDeltaSet() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        testManager?.save(store, Person(TEST_USERNAME))
        assertEquals(1, testManager?.sync(store, emptyQuery)?.result?.count)
        assertEquals(0, testManager?.sync(store, emptyQuery)?.result?.count)
        store?.isDeltaSetCachingEnabled = false
        assertEquals(1, testManager?.sync(store, emptyQuery)?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindStoreTypeCache_CreateItem() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        testManager?.save(store, Person(TEST_USERNAME))
        assertEquals(1, testManager?.find(store, emptyQuery)?.result?.result?.size)
        testManager?.save(store, Person(TEST_USERNAME_2))
        assertEquals(2, testManager?.find(store, emptyQuery)?.result?.result?.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindStoreTypeCache_UpdateItem() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        val person = testManager?.save(store, Person(TEST_USERNAME))?.result
        assertEquals(1, testManager?.find(store, emptyQuery)?.result?.result?.size)
        person?.age = "20"
        testManager?.save(store, person)
        assertEquals(1, testManager?.find(store, emptyQuery)?.result?.result?.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindStoreTypeCache_DeleteOneOfThreeItems() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        val person = testManager?.save(store, Person(TEST_USERNAME))?.result
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME_2))
        assertEquals(2, testManager?.find(store, usernameQuery)?.result?.result?.size)
        testManager?.delete(store, person?.id)
        val people = testManager?.find(store, usernameQuery)?.result?.result
        assertEquals(1, people?.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindStoreTypeCache_UpdateOneOfThreeItems() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        val person = testManager?.save(store, Person(TEST_USERNAME))?.result
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.save(store, Person(TEST_USERNAME_2))
        assertEquals(2, testManager?.find(store, usernameQuery)?.result?.result?.size)
        person?.age = "20"
        testManager?.save(store, person)
        assertEquals(2, testManager?.find(store, usernameQuery)?.result?.result?.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindStoreTypeCache_DisablingDeltaSet() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        testManager?.save(store, Person(TEST_USERNAME))
        assertEquals(1, testManager?.find(store, emptyQuery)?.result?.result?.size)
        assertEquals(1, testManager?.find(store, emptyQuery)?.result?.result?.size)
        store?.isDeltaSetCachingEnabled = false
        assertEquals(1, testManager?.find(store, emptyQuery)?.result?.result?.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindStoreTypeCache_FindById() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        val person = testManager?.save(store, Person(TEST_USERNAME))?.result
        assertEquals(1, testManager?.find(store, client?.query()?.equals("_id", person?.id))?.result?.result?.size)
        assertEquals(1, testManager?.find(store, client?.query()?.equals("_id", person?.id))?.result?.result?.size)
        assertEquals(person?.id, testManager?.find(store, person?.id)?.result?.id)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindStoreTypeNetwork() {
        initDeltaSetCachedCollection(StoreType.NETWORK)
        testManager?.save(store, Person(TEST_USERNAME))
        assertEquals(1, testManager?.find(store, emptyQuery)?.result?.result?.size)
        assertEquals(1, testManager?.find(store, emptyQuery)?.result?.result?.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testChangeStoreTypeSyncToCache() {
        initDeltaSetCachedCollection(StoreType.SYNC)
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.push(store)
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        testManager?.save(store, Person(TEST_USERNAME_2))
        testManager?.push(store)
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.CACHE, client)
        store?.isDeltaSetCachingEnabled = true
        testManager?.save(store, Person(TEST_USERNAME_2))
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testChangeStoreTypeCacheToSync() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        testManager?.save(store, Person(TEST_USERNAME))
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        testManager?.save(store, Person(TEST_USERNAME_2))
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        store?.isDeltaSetCachingEnabled = true
        testManager?.save(store, Person(TEST_USERNAME_2))
        testManager?.push(store)
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testChangeStoreTypeNetworkToSync() {
        initDeltaSetCachedCollection(StoreType.NETWORK)
        val firstPerson = testManager?.save(store, Person(TEST_USERNAME))?.result
        assertEquals(1, testManager?.find(store, emptyQuery)?.result?.result?.size)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        store?.isDeltaSetCachingEnabled = true
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        testManager?.save(store, Person(TEST_USERNAME_2))
        testManager?.delete(store, firstPerson?.id)
        testManager?.push(store)
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testChangeStoreTypeNetworkToCache() {
        initDeltaSetCachedCollection(StoreType.NETWORK)
        val firstPerson = testManager?.save(store, Person(TEST_USERNAME))?.result
        assertEquals(1, testManager?.find(store, emptyQuery)?.result?.result?.size)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.CACHE, client)
        store?.isDeltaSetCachingEnabled = true
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        testManager?.save(store, Person(TEST_USERNAME_2))
        testManager?.delete(store, firstPerson?.id)
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
    }

    /* The test aims to confirm that second request after clearing the cache would use deltaset */
    @Test
    @Throws(InterruptedException::class)
    fun testSecondRequestAfterClearingCacheStoreTypeSync() {
        initDeltaSetCachedCollection(StoreType.SYNC)
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.push(store)
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.push(store)
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        store?.clear()
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.push(store)
        assertEquals(3, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        testManager?.save(store, Person(TEST_USERNAME))
        testManager?.push(store)
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSecondRequestAfterClearingCacheStoreTypeCache() {
        initDeltaSetCachedCollection(StoreType.CACHE)
        testManager?.save(store, Person(TEST_USERNAME))
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        testManager?.save(store, Person(TEST_USERNAME))
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        store?.clear()
        testManager?.save(store, Person(TEST_USERNAME))
        assertEquals(3, testManager?.pullCustom(store, emptyQuery)?.result?.count)
        testManager?.save(store, Person(TEST_USERNAME))
        assertEquals(1, testManager?.pullCustom(store, emptyQuery)?.result?.count)
    }

    /* The test aims to confirm that the autopagination is envoked only at the regular GET request when deltaset is on */
    /* with enabled deltaset and autopagination should use AP for first request and DS for the next */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPullAutoPaginationStoreTypeSync() {
        testPullAutoPagination(StoreType.SYNC)
    }

    /* with enabled deltaset and autopagination should use AP for first request and DS for the next */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncAutoPaginationStoreTypeSync() {
        testSyncAutoPagination(StoreType.SYNC)
    }

    /* with enabled deltaset and autopagination should use AP for first request and DS for the next */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPullAutoPaginationStoreTypeCache() {
        testPullAutoPagination(StoreType.CACHE)
    }

    /* with enabled deltaset and autopagination should use AP for first request and DS for the next */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncAutoPaginationStoreTypeCache() {
        testSyncAutoPagination(StoreType.CACHE)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun testPullAutoPagination(storeType: StoreType) {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, storeType, client)
        store?.isDeltaSetCachingEnabled = true
        testManager?.createPersons(networkStore, 4)
        assertEquals(4, testManager?.pullCustom(store, client?.query(), 2)?.result?.count)
        var people = testManager?.find(store, emptyQuery)?.result?.result
        assertEquals(4, people?.size)
        testManager?.save(networkStore, Person(TEST_USERNAME))
        assertEquals(1, testManager?.pullCustom(store, client?.query(), 2)?.result?.count)
        people = testManager?.find(store, emptyQuery)?.result?.result
        assertEquals(5, people?.size)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun testSyncAutoPagination(storeType: StoreType) {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, storeType, client)
        store?.isDeltaSetCachingEnabled = true
        testManager?.createPersons(networkStore, 4)
        assertEquals(4, testManager?.sync(store, client?.query(), 2)?.result?.count)
        var people = testManager?.find(store, emptyQuery)?.result?.result
        assertEquals(4, people?.size)
        testManager?.save(networkStore, Person(TEST_USERNAME))
        assertEquals(1, testManager?.sync(store, client?.query(), 2)?.result?.count)
        people = testManager?.find(store, emptyQuery)?.result?.result
        assertEquals(5, people?.size)
    }

    /* should use regular GET when deltaset configuration is missing on the backend */
    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testStoreTypeSyncWithMissedConfigurationAtTheBackend() {
        testMissedConfigurationAtTheBackend(StoreType.SYNC)
    }

    /* should use regular GET when deltaset configuration is missing on the backend */
    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testStoreTypeCacheWithMissedConfigurationAtTheBackend() {
        testMissedConfigurationAtTheBackend(StoreType.CACHE)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun testMissedConfigurationAtTheBackend(storeType: StoreType) {
        val networkStore = collection(Person.DELTA_SET_OFF_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_OFF_COLLECTION, Person::class.java, storeType, client)
        store?.isDeltaSetCachingEnabled = true
        testManager?.createPersons(networkStore, 1)
        var pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(1, pullCallback?.result?.count)
        testManager?.createPersons(networkStore, 1)
        pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(2, pullCallback?.result?.count)
    }

    /* should use regular auto-pagination when deltaset configuration is missing on the backend and AP is on */
    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testStoreTypeSyncWithMissedConfigurationAtTheBackendAPisOn() {
        testMissedConfigurationAtTheBackendAPisOn(StoreType.SYNC)
    }

    /* should use regular auto-pagination when deltaset configuration is missing on the backend and AP is on */
    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testStoreTypeCacheWithMissedConfigurationAtTheBackendAPisOn() {
        testMissedConfigurationAtTheBackendAPisOn(StoreType.CACHE)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun testMissedConfigurationAtTheBackendAPisOn(storeType: StoreType) {
        val networkStore = collection(Person.DELTA_SET_OFF_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_OFF_COLLECTION, Person::class.java, storeType, client)
        store?.isDeltaSetCachingEnabled = true
        testManager?.createPersons(networkStore, 3)
        var pullCallback = testManager?.pullCustom(store, emptyQuery, 2)
        assertEquals(3, pullCallback?.result?.count)
        testManager?.createPersons(networkStore, 1)
        pullCallback = testManager?.pullCustom(store, emptyQuery, 2)
        assertEquals(4, pullCallback?.result?.count)
    }

    /* check that if query contains skip or limit then delta set is ignored */
    /* with enable deltaset and limit and skip should not use deltaset and should not override lastRunAt */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPullSkipLimitStoreTypeSync() {
        testPullSkipLimit(StoreType.SYNC)
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not override lastRunAt */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPullSkipLimitStoreTypeCache() {
        testPullSkipLimit(StoreType.CACHE)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun testPullSkipLimit(storeType: StoreType) {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, storeType, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME_2))
        assertEquals(4, testManager?.pullCustom(store, client?.query())?.result?.count)
        val query = Query().equals("username", TEST_USERNAME)
        assertEquals(2, testManager?.pullCustom(store, query.setLimit(2).setSkip(1))?.result?.count)
        updateItem(networkStore, Query().equals("username", TEST_USERNAME_2))
        assertEquals(2, testManager?.pullCustom(store, query.setLimit(2).setSkip(1))?.result?.count)
        assertEquals(1, testManager?.pullCustom(store, client?.query())?.result?.count)
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not override lastRunAt */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncSkipLimitStoreTypeSync() {
        testSyncSkipLimit(StoreType.SYNC)
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not override lastRunAt */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncSkipLimitStoreTypeCache() {
        testSyncSkipLimit(StoreType.CACHE)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun testSyncSkipLimit(storeType: StoreType) {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, storeType, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME_2))
        assertEquals(4, testManager?.sync(store, client?.query())?.result?.count)
        val query = Query().equals("username", TEST_USERNAME)
        assertEquals(2, testManager?.sync(store, query.setLimit(2).setSkip(1))?.result?.count)
        updateItem(networkStore, Query().equals("username", TEST_USERNAME_2))
        assertEquals(2, testManager?.sync(store, query.setLimit(2).setSkip(1))?.result?.count)
        assertEquals(1, testManager?.sync(store, client?.query())?.result?.count)
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not override lastRunAt */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFindSkipLimitStoreTypeCache() {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.CACHE, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME_2))
        assertEquals(4, testManager?.find(store, client?.query())?.result?.result?.size)
        val query = Query().equals("username", TEST_USERNAME)
        assertEquals(2, testManager?.find(store, query.setLimit(2).setSkip(1))?.result?.result?.size)
        updateItem(networkStore, Query().equals("username", TEST_USERNAME_2))
        assertEquals(2, testManager?.find(store, query.setLimit(2).setSkip(1))?.result?.result?.size)
        assertEquals(4, testManager?.find(store, client?.query())?.result?.result?.size)
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not cause inconsistent data */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPullSkipLimitStoreTypeSync2() {
        testPullSkipLimit2(StoreType.SYNC)
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not cause inconsistent data */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPullSkipLimitStoreTypeCache2() {
        testPullSkipLimit2(StoreType.CACHE)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun testPullSkipLimit2(storeType: StoreType) {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, storeType, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME_2))
        assertEquals(4, testManager?.pullCustom(store, client?.query())?.result?.count)
        val query = Query().equals("username", TEST_USERNAME)
        assertEquals(2, testManager?.pullCustom(store, query.setLimit(2).setSkip(1))?.result?.count)
        updateItem(networkStore, Query().equals("username", TEST_USERNAME_2))
        assertEquals(2, testManager?.pullCustom(store, query.setLimit(2).setSkip(1))?.result?.count)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        assertEquals(4, store?.count())
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not cause inconsistent data */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncSkipLimitStoreTypeSync2() {
        testSyncSkipLimit2(StoreType.SYNC)
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not cause inconsistent data */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncSkipLimitStoreTypeCache2() {
        testSyncSkipLimit2(StoreType.CACHE)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun testSyncSkipLimit2(storeType: StoreType) {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, storeType, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME_2))
        assertEquals(4, testManager?.sync(store, client?.query())?.result?.count)
        val query = Query().equals("username", TEST_USERNAME)
        assertEquals(2, testManager?.sync(store, query.setLimit(2).setSkip(1))?.result?.count)
        updateItem(networkStore, Query().equals("username", TEST_USERNAME_2))
        assertEquals(2, testManager?.sync(store, query.setLimit(2).setSkip(1))?.result?.count)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        assertEquals(4, store?.count())
    }

    /* with enable deltaset and limit and skip should not use deltaset and should not cause inconsistent data */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFindSkipLimitStoreTypeCache2() {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.CACHE, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME_2))
        assertEquals(4, testManager?.find(store, client?.query())?.result?.result?.size)
        val query = Query().equals("username", TEST_USERNAME)
        assertEquals(2, testManager?.find(store, query.setLimit(2).setSkip(1))?.result?.result?.size)
        updateItem(networkStore, Query().equals("username", TEST_USERNAME_2))
        assertEquals(2, testManager?.find(store, query.setLimit(2).setSkip(1))?.result?.result?.size)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        assertEquals(4, store?.count())
    }

    /* support methods */


    @Throws(InterruptedException::class)
    private fun initDeltaSetCachedCollection(storeType: StoreType) {
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, storeType, client)
        testManager?.cleanBackend(store, storeType)
        store?.isDeltaSetCachingEnabled = true
    }
/* change one random person's user name to UPDATED_USERNAME */


    /* change one random person's user name to UPDATED_USERNAME */
    @Throws(InterruptedException::class)
    private fun updateItem(query: Query? = emptyQuery) {
        updateItem(store, query)
    }
/* change one random person's user name to UPDATED_USERNAME */


    /* change one random person's user name to UPDATED_USERNAME */
    @Throws(InterruptedException::class)
    private fun updateItem(store: DataStore<Person>?, query: Query? = emptyQuery) {
        val personsInCache = testManager?.find(store, query)?.result?.result
        val person = personsInCache?.get(0)
        person?.age = "20"
        testManager?.save(store, person)
    }
/* delete one random item from the cache */


    /* delete one random item from the cache */
    @Throws(InterruptedException::class)
    private fun deleteItem(query: Query? = emptyQuery) {
        deleteItem(store, query)
    }
/* delete one random item */


    /* delete one random item from the cache */
    @Throws(InterruptedException::class)
    private fun deleteItem(store: DataStore<Person>?, query: Query? = emptyQuery) {
        val personsInCache = testManager?.find(store, query)?.result?.result
        val person = personsInCache?.get(0)
        testManager?.delete(store, person?.id)
    }

    /* end support methods*/


    /* updated tests*/


    /* with enabled deltaset should return correct number of items when creating (SYNC)*/
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPullStoreTypeSync_Creating() {
        testPull_Creating(StoreType.SYNC)
    }

    /* with enabled deltaset should return correct number of items when creating (CACHE)*/
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPullStoreTypeCache_Creating() {
        testPull_Creating(StoreType.CACHE)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun testPull_Creating(storeType: StoreType) {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, storeType, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        var pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(1, pullCallback?.result?.count)
        networkStore.save(Person(TEST_USERNAME_2))
        pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(1, pullCallback?.result?.count)
        networkStore.save(Person(TEST_USERNAME_2))
        networkStore.save(Person(TEST_USERNAME_2))
        pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(2, pullCallback?.result?.count)
        assertEquals(4, store?.count())
    }

    /* with enabled deltaset should return correct number of items when updating (SYNC)*/
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPullStoreTypeSync_Updating() {
        testPull_Updating(StoreType.SYNC)
    }

    /* with enabled deltaset should return correct number of items when updating (CACHE)*/
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPullStoreTypeCache_Updating() {
        testPull_Updating(StoreType.CACHE)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun testPull_Updating(storeType: StoreType) {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, storeType, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        var pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(3, pullCallback?.result?.count)
        val people = store?.find()?.result
        val personToUpdate1 = people?.get(0) as Person
        val personToUpdate2 = people?.get(1) as Person
        personToUpdate1.age = "40"
        networkStore.save(personToUpdate1)
        pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(1, pullCallback?.result?.count)
        personToUpdate1.age = "50"
        personToUpdate2.age = "50"
        networkStore.save(personToUpdate1)
        networkStore.save(personToUpdate2)
        pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(2, pullCallback?.result?.count)
        assertEquals(3, store?.count())
    }

    /* with enabled deltaset should return correct number of items when deleting */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPullStoreTypeSync_Deleting() {
        testPull_Deleting(StoreType.SYNC)
    }

    /* with enabled deltaset should return correct number of items when deleting */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPullStoreTypeCache_Deleting() {
        testPull_Deleting(StoreType.CACHE)
    }

    @Throws(InterruptedException::class, IOException::class)
    fun testPull_Deleting(storeType: StoreType) {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, storeType, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        var pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(3, pullCallback?.result?.count)
        assertEquals(3, store?.count())
        deleteItem(networkStore)
        pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(0, pullCallback?.result?.count)
        assertEquals(2, store?.count())
        deleteItem(networkStore)
        deleteItem(networkStore)
        pullCallback = testManager?.pullCustom(store, emptyQuery)
        assertEquals(0, pullCallback?.result?.count)
        assertEquals(0, store?.count())
    }

    /* with enabled deltaset should return correct number of items when creating (SYNC)*/
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun tesSyncStoreTypeSync_Creating() {
        tesSync_Creating(StoreType.SYNC)
    }

    /* with enabled deltaset should return correct number of items when creating (CACHE)*/
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun tesSyncStoreTypeCache_Creating() {
        tesSync_Creating(StoreType.CACHE)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun tesSync_Creating(storeType: StoreType) {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, storeType, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        var syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(1, syncCallback?.result?.count)
        networkStore.save(Person(TEST_USERNAME_2))
        syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(1, syncCallback?.result?.count)
        networkStore.save(Person(TEST_USERNAME_2))
        networkStore.save(Person(TEST_USERNAME_2))
        syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(2, syncCallback?.result?.count)
        assertEquals(4, store?.count())
    }

    /* with enabled deltaset should return correct number of items when updating (SYNC)*/
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncStoreTypeSync_Updating() {
        testSync_Updating(StoreType.SYNC)
    }

    /* with enabled deltaset should return correct number of items when updating (CACHE)*/
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncStoreTypeCache_Updating() {
        testSync_Updating(StoreType.CACHE)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun testSync_Updating(storeType: StoreType) {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, storeType, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        var syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(3, syncCallback?.result?.count)
        assertEquals(3, store?.count())
        updateItem(networkStore)
        syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(1, syncCallback?.result?.count)
        assertEquals(3, store?.count())
    }

    /* with enabled deltaset should return correct number of items when deleting (SYNC)*/
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncStoreTypeSync_Deleting() {
        testSync_Deleting(StoreType.SYNC)
    }

    /* with enabled deltaset should return correct number of items when deleting (CACHE)*/
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncStoreTypeCache_Deleting() {
        testSync_Deleting(StoreType.CACHE)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun testSync_Deleting(storeType: StoreType) {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, storeType, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        var syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(3, syncCallback?.result?.count)
        assertEquals(3, store?.count())
        deleteItem(networkStore)
        syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(0, syncCallback?.result?.count)
        assertEquals(2, store?.count())
        deleteItem(networkStore)
        deleteItem(networkStore)
        syncCallback = testManager?.sync(store, emptyQuery)
        assertEquals(0, syncCallback?.result?.count)
        assertEquals(0, store?.count())
    }

    /* with enabled deltaset should return correct number of items when creating */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFindStoreTypeCache_Creating() {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.CACHE, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        var readCallback = testManager?.find(store, emptyQuery)
        assertEquals(1, readCallback?.result?.result?.size)
        networkStore.save(Person(TEST_USERNAME_2))
        readCallback = testManager?.find(store, emptyQuery)
        assertEquals(2, readCallback?.result?.result?.size)
        assertEquals(2, store?.count())
    }

    /* with enabled deltaset should return correct number of items when updating */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFindStoreTypeCache_Updating() {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.CACHE, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        var readCallback = testManager?.find(store, emptyQuery)
        assertEquals(1, readCallback?.result?.result?.size)
        updateItem(networkStore)
        readCallback = testManager?.find(store, emptyQuery)
        assertEquals(1, readCallback?.result?.result?.size)
    }

    /* with enabled deltaset should return correct number of items when deleting v2 */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFindStoreTypeCache_Deleting() {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.CACHE, client)
        store?.isDeltaSetCachingEnabled = true
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        networkStore.save(Person(TEST_USERNAME))
        var readCallback = testManager?.find(store, emptyQuery)
        assertEquals(3, readCallback?.result?.result?.size)
        deleteItem(networkStore)
        readCallback = testManager?.find(store, emptyQuery)
        assertEquals(2, readCallback?.result?.result?.size)
        deleteItem(networkStore)
        deleteItem(networkStore)
        readCallback = testManager?.find(store, emptyQuery)
        assertEquals(0, readCallback?.result?.result?.size)
    }

    /* with enabled deltaset should return correct number of items when deleting and updating */
    @Test
    @Throws(InterruptedException::class)
    fun testFindStoreTypeCache_DeletingUpdating() {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.CACHE, client)
        store?.isDeltaSetCachingEnabled = true
        testManager?.save(networkStore, Person(TEST_USERNAME))
        testManager?.save(networkStore, Person(TEST_USERNAME_2))
        testManager?.save(networkStore, Person(TEST_USERNAME_2))
        var readCallback = testManager?.find(store, emptyQuery)
        assertEquals(3, readCallback?.result?.result?.size)
        deleteItem(networkStore)
        updateItem(networkStore)
        readCallback = testManager?.find(store, emptyQuery)
        assertEquals(2, readCallback?.result?.result?.size)
    }

    /* should delete old items when lastRunAt is outdated */
    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testParameterValueOutOfRangeErrorHandling_StoreTypeSync() {
        testParameterValueOutOfRangeErrorHandling(StoreType.SYNC)
    }

    /* should delete old items when lastRunAt is outdated */
    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testParameterValueOutOfRangeErrorHandling_StoreTypeCache() {
        testParameterValueOutOfRangeErrorHandling(StoreType.CACHE)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun testParameterValueOutOfRangeErrorHandling(storeType: StoreType) {
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, storeType, client)
        store?.isDeltaSetCachingEnabled = true
        testManager?.createPersons(networkStore, 10)
        var pullCallback = testManager?.pullCustom(store, client?.query())
        assertEquals(10, pullCallback?.result?.count)
        deleteItem(networkStore)
        deleteItem(networkStore)
        deleteItem(networkStore)
        deleteItem(networkStore)
        deleteItem(networkStore)
        val lastRequestTime = "2018-05-14T09:40:44.470Z"
        val queryCache = client?.syncManager?.cacheManager
                ?.getCache(Constants.QUERY_CACHE_COLLECTION, QueryCacheItem::class.java, Long.MAX_VALUE)
        val cacheItem = queryCache?.first
        cacheItem?.lastRequestTime = lastRequestTime
        queryCache?.save(cacheItem)
        pullCallback = testManager?.pullCustom(store, client?.query())
        assertEquals(5, pullCallback?.result?.count)
        assertEquals(5, testManager?.find(store, emptyQuery)?.result?.result?.size)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testDeltaSetDoRequestAfter2FailedAttempts() {
        val networkStore = collection(Person.DELTA_SET_OFF_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)
        store = collection(Person.DELTA_SET_OFF_COLLECTION, Person::class.java, StoreType.SYNC, client)
        store?.isDeltaSetCachingEnabled = true
        testManager?.createPersons(networkStore, 2)
        var pullCallback = testManager?.pullCustom(store, client?.query())
        assertEquals(2, pullCallback?.result?.count)
        val queryCache = client?.syncManager?.cacheManager
                ?.getCache(Constants.QUERY_CACHE_COLLECTION, QueryCacheItem::class.java, Long.MAX_VALUE)
        assertEquals(1, queryCache?.get()?.size)
        pullCallback = testManager?.pullCustom(store, client?.query())
        assertEquals(2, pullCallback?.result?.count)
        assertEquals(1, queryCache?.get()?.size)
        pullCallback = testManager?.pullCustom(store, client?.query())
        assertEquals(2, pullCallback?.result?.count)
        assertEquals(1, queryCache?.get()?.size)
    }

    @Test
    fun testCountHaveTimeStamp() {
        var method: Method? = null
        try {
            method = BaseDataStore::class.java.getDeclaredMethod("internalCountNetwork")
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
        assert(method != null)
        method?.isAccessible = true
        var response: KinveyCountResponse? = null
        try {
            response = method?.invoke(BaseDataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)) as KinveyCountResponse
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        assertNotNull(response)
        val lastRequestTime = response?.lastRequestTime
        assertNotNull(lastRequestTime)
        val p: Pattern = Pattern.compile("\\b\\d{4}-\\d{2}-\\d{2}T\\d{1,2}:\\d{2}:\\d{2}.\\d{3}Z") // check data pattern "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

        val m: Matcher = p.matcher(lastRequestTime)
        assertTrue(m.matches())
    }
}