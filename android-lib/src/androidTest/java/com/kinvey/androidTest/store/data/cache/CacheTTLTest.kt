package com.kinvey.androidTest.store.data.cache

import android.content.Context
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client.Builder
import com.kinvey.android.Client.Companion.sharedInstance
import com.kinvey.android.model.User
import com.kinvey.java.cache.ICache
import com.kinvey.java.cache.ICacheManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * Created by Prots on 1/27/16.
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class CacheTTLTest {

    var cacheManager: ICacheManager? = null

    @Before
    fun setup() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        cacheManager = Builder<User>(mMockContext).build().cacheManager
        val user = User()
        user.id = "testId"
        sharedInstance().activeUser = user
    }

    @Test
    fun testSaveSingleInsertNoTTL() {
        val cache = cacheManager?.getCache("test", SampleGsonObject1::class.java, TTL_ZERO)
        val obj = SampleGsonObject1()
        val id = UUID.randomUUID().toString()
        obj.id = id
        obj.title = "test"
        assertEquals(cache?.save(obj)?.id, id)
        val ret = cache?.get(id)
        //test the same
        assertNull(ret)
    }

    @Test
    fun testSaveSingleInsertTTLOneMinute() {
        val cache = cacheManager?.getCache("test", SampleGsonObject1::class.java, TTL_MINUTE)
        val obj = SampleGsonObject1()
        val id = UUID.randomUUID().toString()
        obj.id = id
        obj.title = "test"
        assertEquals(cache?.save(obj)?.get("_id"), id)
        val ret = cache?.get(id)

        //test the same
        assertNotNull(ret)
        assertEquals(ret?.id, obj.id)
        assertEquals(ret?.title, obj.title)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveSingleInsertTTLOneMinuteExpired() {
        val cache = cacheManager?.getCache("test", SampleGsonObject1::class.java, TTL_MINUTE)
        val obj = SampleGsonObject1()
        val id = UUID.randomUUID().toString()
        obj.id = id
        obj.title = "test"
        assertEquals(cache?.save(obj)?.id, id)
        Thread.sleep(TTL_MINUTE)
        val ret = cache?.get(id)

        //test the same
        assertNull(ret)
    }

    companion object {
        private const val TTL_MINUTE = 60 * 1000.toLong()
        private const val TTL_ZERO: Long = 0
    }
}