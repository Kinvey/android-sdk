package com.kinvey.androidTest.store.data

import androidx.test.filters.SmallTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kinvey.android.store.DataStore
import com.kinvey.androidTest.model.Person
import com.kinvey.androidTest.store.datastore.BaseDataStoreTest
import com.kinvey.java.store.StoreType
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@SmallTest
class DataStoreCountHeaderTest : BaseDataStoreTest() {

    @Test
    @Throws(InterruptedException::class)
    fun testFindByQueryWithCountNetwork() {
        testFindByQueryWithCount(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindByQueryWithCountAuto() {
        testFindByQueryWithCount(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindByQueryWithCountSync() {
        testFindByQueryWithCount(StoreType.SYNC)
    }

    private fun testFindByQueryWithCount(storeType: StoreType) {
        val store = DataStore.collection(COLLECTION, Person::class.java, storeType, client)
        Assert.assertFalse(store.hasCountHeader)
        clearBackend(store)
        createAndSavePerson(store, TEST_USERNAME)
        createAndSavePerson(store, TEST_USERNAME_2)
        var findCallback = find(store, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertNull(findCallback.result?.count)
        store.hasCountHeader = true
        findCallback = find(store, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertNotNull(findCallback.result?.count)
        Assert.assertEquals(findCallback.result?.result?.size, 2)
        Assert.assertEquals(findCallback.result?.count, 2)
        store.hasCountHeader = false
        findCallback = find(store, LONG_TIMEOUT)
        Assert.assertNull(findCallback.result?.count)
        clearBackend(store)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindWithCountSync() {
        testFindWithCount(StoreType.SYNC)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindWithCountNetwork() {
        testFindWithCount(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindWithCountAuto() {
        testFindWithCount(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindWithCountCache() {
        val store = DataStore.collection(COLLECTION, Person::class.java, StoreType.CACHE, client)
        try {
            store.findWithCount(client?.query()!!)
            Assert.assertFalse(true)
        } catch (e: IllegalArgumentException) {
            Assert.assertTrue(e.message?.contains("StoreType.CACHE isn't supported") == true)
        }
        try {
            store.findWithCount(client?.query()!!,  DefaultKinveyReadCallback(CountDownLatch(0)))
            Assert.assertFalse(true)
        } catch (e: IllegalArgumentException) {
            Assert.assertTrue(e.message?.contains("StoreType.CACHE isn't supported") == true)
        }
    }

    private fun testFindWithCount(storeType: StoreType) {
        val store = DataStore.collection(COLLECTION, Person::class.java, storeType, client)
        Assert.assertFalse(store.hasCountHeader)
        clearBackend(store)
        createAndSavePerson(store, TEST_USERNAME)
        createAndSavePerson(store, TEST_USERNAME_2)
        var findCallback = findWithCount(store, client!!.query(), LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertEquals(findCallback.result?.count, 2)

        findCallback = find(store, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertEquals(findCallback.result?.result?.size, 2)
        Assert.assertNull(findCallback.result?.count)

        store.hasCountHeader = false
        findCallback = find(store, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertEquals(findCallback.result?.result?.size, 2)
        Assert.assertNull(findCallback.result?.count)
        clearBackend(store)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindWithCountDeltaAuto() {
        testFindWithCountDelta(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindWithCountDeltaSync() {
        testFindWithCountDelta(StoreType.SYNC)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindWithCountDeltaNetwork() {
        testFindWithCountDelta(StoreType.NETWORK)
    }

    private fun testFindWithCountDelta(storeType: StoreType) {
        val store = DataStore.collection(COLLECTION, Person::class.java, storeType, client)
        store.isDeltaSetCachingEnabled = true
        store.hasCountHeader = false
        clearBackend(store)
        createAndSavePerson(store, TEST_USERNAME)
        createAndSavePerson(store, TEST_USERNAME_2)
        createAndSavePerson(store, TEST_USERNAME_3)
        var query = client?.query()

        //query contains skip and limit
        query?.setSkip(1)?.setLimit(2)
        var findCallback = findWithCount(store, query!!, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertNotNull(findCallback.result?.count)
        Assert.assertEquals(3, findCallback.result?.count)
        Assert.assertEquals(2, findCallback.result?.result?.size)

        //query does not contain skip and limit
        query = client?.query()
        findCallback = findWithCount(store, query!!, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertNotNull(findCallback.result?.count)
        Assert.assertEquals(3, findCallback.result?.count)
        Assert.assertEquals(3, findCallback.result?.result?.size)

        store.isDeltaSetCachingEnabled = false
        query.setSkip(1).setLimit(2)
        findCallback = findWithCount(store, query, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertNotNull(findCallback.result?.count)
        Assert.assertEquals(3, findCallback.result?.count)
        Assert.assertEquals(2, findCallback.result?.result?.size)

        query = client?.query()
        findCallback = find(store, query, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertNull(findCallback.result?.count)
        Assert.assertEquals(3, findCallback.result?.result?.size)

        clearBackend(store)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testQueryNetwork() {
        testQuery(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testQuerySync() {
        testQuery(StoreType.SYNC)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testQueryAuto() {
        testQuery(StoreType.AUTO)
    }

    private fun testQuery(storeType: StoreType) {
        val store = DataStore.collection(COLLECTION, Person::class.java, storeType, client)
        clearBackend(store)
        createAndSavePerson(store, TEST_USERNAME)
        createAndSavePerson(store, TEST_USERNAME)
        createAndSavePerson(store, TEST_USERNAME)

        var query = client?.query()
        query?.equals("username", TEST_USERNAME)
        var findCallback = findWithCount(store, query!!, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertNotNull(findCallback.result?.count)
        Assert.assertEquals(3, findCallback.result?.count)
        Assert.assertEquals(3, findCallback.result?.result?.size)

        //check that request ignores `limit` parameter for count
        query = client?.query()
        query?.equals("username", TEST_USERNAME)?.setLimit(1)
        findCallback = findWithCount(store, query!!, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertNotNull(findCallback.result?.count)
        Assert.assertEquals(3, findCallback.result?.count)
        Assert.assertEquals(1, findCallback.result?.result?.size)

        //check that request ignores `skip` parameter for count
        query = client?.query()
        query?.equals("username", TEST_USERNAME)?.setSkip(1)
        findCallback = findWithCount(store, query!!, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertNotNull(findCallback.result?.count)
        Assert.assertEquals(3, findCallback.result?.count)
        Assert.assertEquals(2, findCallback.result?.result?.size)

        clearBackend(store)
    }

}