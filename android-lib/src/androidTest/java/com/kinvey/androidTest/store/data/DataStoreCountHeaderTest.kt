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

}