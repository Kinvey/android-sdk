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

    private fun testFindByQueryWithCount(storeType: StoreType) {
        val storeNetwork = DataStore.collection(COLLECTION, Person::class.java, storeType, client)
        clearBackend(storeNetwork)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        var findCallback = find(storeNetwork, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertNull(findCallback.result?.count)
        storeNetwork.isAddCountHeader = true
        findCallback = find(storeNetwork, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertNotNull(findCallback.result?.count)
        Assert.assertEquals(findCallback.result?.result?.size, 2)
        Assert.assertEquals(findCallback.result?.count, 2)
        storeNetwork.isAddCountHeader = false
        findCallback = find(storeNetwork, LONG_TIMEOUT)
        Assert.assertNull(findCallback.result?.count)
        clearBackend(storeNetwork)
    }

}