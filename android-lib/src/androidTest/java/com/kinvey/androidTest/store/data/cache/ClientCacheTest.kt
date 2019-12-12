package com.kinvey.androidTest.store.data.cache

import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.cache.RealmCacheManager
import com.kinvey.android.model.User
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by Prots on 1/27/16.
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class ClientCacheTest {

    var client: Client<*>? = null

    @Before
    fun setUp() {
        val builder = Builder<User>(InstrumentationRegistry.getInstrumentation().context)
        client = builder.build()
    }

    @Test
    fun testGetCacheManagerShouldNotFail() {
        client?.cacheManager
    }

    @Test
    fun testGetCacheManagerShouldReturnNotNull() {
        assertNotNull(client?.cacheManager)
    }

    @Test
    fun testgetCacheManagerShouldRetuntRealmCacheManager() {
        assertTrue("cahe is not realm instance", client?.cacheManager is RealmCacheManager)
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
}