package com.kinvey.androidTest.store.data

import androidx.test.filters.SmallTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kinvey.android.callback.KinveyReadCallback
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
        Assert.assertFalse(store.isAddCountHeader)
        clearBackend(store)
        createAndSavePerson(store, TEST_USERNAME)
        createAndSavePerson(store, TEST_USERNAME_2)
        var findCallback = find(store, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertNull(findCallback.result?.count)
        store.isAddCountHeader = true
        findCallback = find(store, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertNotNull(findCallback.result?.count)
        Assert.assertEquals(findCallback.result?.result?.size, 2)
        Assert.assertEquals(findCallback.result?.count, 2)
        store.isAddCountHeader = false
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
        Assert.assertFalse(store.isAddCountHeader)
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

        store.isAddCountHeader = false
        findCallback = find(store, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertEquals(findCallback.result?.result?.size, 2)
        Assert.assertNull(findCallback.result?.count)
        clearBackend(store)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindWithCountAutoDelta() {

        val store = DataStore.collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        store.isDeltaSetCachingEnabled = true
        store.isAddCountHeader = false
        clearBackend(store)
        createAndSavePerson(store, TEST_USERNAME)
        createAndSavePerson(store, TEST_USERNAME_2)
        createAndSavePerson(store, TEST_USERNAME_3)
        val query = client?.query()
        query?.setSkip(1)
        var findCallback = findWithCount(store, client!!.query(), LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertNotNull(findCallback.result?.count)
        Assert.assertEquals(2, findCallback.result?.count)


        store.isAddCountHeader = true
        findCallback = findWithCount(store, client!!.query(), LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertNotNull(findCallback.result?.count)
        Assert.assertEquals(2, findCallback.result?.count)

        clearBackend(store)
    }

}