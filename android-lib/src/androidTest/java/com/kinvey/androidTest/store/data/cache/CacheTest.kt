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
class CacheTest {

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
    fun testSaveSingleInsert() {
        val cache = cacheManager?.getCache("test", SampleGsonObject1::class.java, Long.MAX_VALUE)
        val obj = SampleGsonObject1()
        val id = UUID.randomUUID().toString()
        obj.id = id
        obj.title = "test"
        assertEquals(cache?.save(obj)?.id, id)
        val ret = cache?.get(id)
        //test the same
        assertNotNull(ret)
        assertEquals(ret?.id, obj.id)
        assertEquals(ret?.title, obj.title)
    }

    @Test
    fun testSaveSingleUpdate() {
        val cache = cacheManager?.getCache("test", SampleGsonObject1::class.java, Long.MAX_VALUE)
        val id = "test"
        val obj = SampleGsonObject1()
        obj["_id"] = id
        obj["title"] = "testTile"
        assertEquals(cache?.save(obj)?.id, id)
        obj["title"] = "testTitle2"
        assertEquals(cache?.save(obj)?.id, id)
        val ret = cache?.get(id)
        assertNotNull(ret)
        assertEquals(ret?.title, "testTitle2")
    }

    @Test
    fun testMultipleInsert() {
        val cache = cacheManager?.getCache("test", SampleGsonObject1::class.java, Long.MAX_VALUE)
        val id = "test"
        val items: MutableList<SampleGsonObject1> = ArrayList()
        for (i in 0..99) {
            items.add(SampleGsonObject1(i.toString(), "multipleInsert$i"))
        }
        val saved = cache?.save(items) ?: listOf()
        assertNotNull(saved)
        assertEquals(100L, saved.size.toLong())
        var idsOk = true
        (0..99).forEach { i ->
            idsOk = idsOk and i.toString().equals(saved[i]["_id"].toString(), ignoreCase = true)
        }
        assertTrue("all ids are right and in right order", idsOk)
        val ids: MutableList<String> = ArrayList()
        for (obj in saved) {
            ids.add(obj.id.toString())
        }
        val cachedObjects = cache?.get(ids)
        assertEquals(100L, cachedObjects?.size?.toLong())
    }

    @Test
    fun testDelete() {
        val cache = cacheManager?.getCache("test", SampleGsonObject1::class.java, Long.MAX_VALUE)
        val items: MutableList<SampleGsonObject1> = ArrayList()
        val ids: MutableList<String> = ArrayList()
        (0..99).forEach { i ->
            items.add(SampleGsonObject1(i.toString(), "deleteTest$i"))
            ids.add(i.toString())
        }
        val saved = cache?.save(items)
        assertNotNull(saved)
        cache?.delete(ids)
        val cachedObjects = cache?.get(ids)
        assertEquals(0L, cachedObjects?.size?.toLong())
    }

    @Test
    fun testSaveWithInner() {
        val cache = cacheManager?.getCache("testInner2", SampleGsonWithInner::class.java, Long.MAX_VALUE)
        val items: MutableList<SampleGsonWithInner> = ArrayList()
        val ids: MutableList<String> = ArrayList()
        (0..99).forEach { i ->
            items.add(SampleGsonWithInner(i.toString(), SampleGsonObject1(i.toString(), "test$i")))
            ids.add(i.toString())
        }
        val saved = cache?.save(items)
        assertNotNull(saved)
        val cachedObjects = cache?.get(ids)
        assertEquals(100L, cachedObjects?.size?.toLong())
        cachedObjects?.indices?.forEach { i ->
            val res = cachedObjects[i]
            assertEquals(res.details?.id, i.toString())
        }
    }

    @Test
    fun testSaveWithInnerList() {
        val cache = cacheManager?.getCache("testInnerWithList3", SampleGsonWithInnerList::class.java, Long.MAX_VALUE)
        val items: MutableList<SampleGsonWithInnerList> = ArrayList()
        val ids: MutableList<String> = ArrayList()
        (0..99).forEach { i ->
            val details: MutableList<SampleGsonObject1> = ArrayList()
            details.add(SampleGsonObject1(i.toString(), "test$i"))
            items.add(SampleGsonWithInnerList(i.toString(), details))
            ids.add(i.toString())
        }
        val saved = cache?.save(items)
        assertNotNull(saved)
        val cachedObjects = cache?.get(ids)
        assertEquals(100L, cachedObjects?.size?.toLong())
        cachedObjects?.indices?.forEach { i ->
            val res = cachedObjects[i]
            assertEquals(res.details?.get(0)?.id, i.toString())
        }
    }

    @Test
    fun testGetTTL() {
        val ttl = 123456L
        assertEquals(ttl, cacheManager?.getCache("testInnerWithList3", SampleGsonWithInnerList::class.java, ttl)?.ttl)
    }
}