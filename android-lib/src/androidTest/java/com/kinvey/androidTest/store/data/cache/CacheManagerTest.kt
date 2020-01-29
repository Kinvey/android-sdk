package com.kinvey.androidTest.store.data.cache

import android.content.Context
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.cache.RealmCache
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore
import com.kinvey.android.store.DataStore.Companion.collection
import com.kinvey.androidTest.model.Person
import com.kinvey.java.KinveyException
import com.kinvey.java.cache.ICacheManager
import com.kinvey.java.store.StoreType
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Created by Prots on 1/27/16.
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class CacheManagerTest {

    var manager: ICacheManager? = null
    var mMockContext: Context? = null

    @Before
    fun setUp() {
        mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        val builder = Builder<User>(mMockContext)
        manager = builder.build().cacheManager
    }

    @Test
    fun getCacheShouldNotFail() {
        manager?.getCache("test", SampleGsonObject1::class.java, Long.MAX_VALUE)
    }

    @Test
    fun getCacheShouldFailOnRuntimeChanges() {
        manager?.getCache("test", SampleGsonObject1::class.java, Long.MAX_VALUE)
        try {
            manager?.getCache("test", SampleGsonObject2::class.java, Long.MAX_VALUE)
        } catch (e: KinveyException) {
            return
        }
        assertTrue("expecting exception to be thrown", false)
    }

    @Test
    fun getCacheShouldNotBeNull() {
        assertNotNull(manager?.getCache("test", SampleGsonObject1::class.java, Long.MAX_VALUE))
    }

    @Test
    fun getCacheShouldBeInstanceOfRealmCache() {
        assertTrue(manager?.getCache("test", SampleGsonObject1::class.java, Long.MAX_VALUE) is RealmCache<*>)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun clearCollectionShouldNotFail() {
        val client = Builder<User>(mMockContext).build()
        val dataStore = collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        dataStore.clear()
        client.performLockDown()
    }
}