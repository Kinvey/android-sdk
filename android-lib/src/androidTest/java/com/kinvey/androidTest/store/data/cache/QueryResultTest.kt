package com.kinvey.androidTest.store.data.cache

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.kinvey.android.Client.Builder
import com.kinvey.android.Client.Companion.sharedInstance
import com.kinvey.android.model.User
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.cache.ICacheManager
import com.kinvey.java.query.AbstractQuery.SortOrder
import com.kinvey.java.query.MongoQueryFilter.MongoQueryFilterBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by Prots on 2/2/16.
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class QueryResultTest {

    var cacheManager: ICacheManager? = null
    private var cache: ICache<SampleGsonObject2>? = null

    @Before
    fun setup() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        cacheManager = Builder<User>(mMockContext).build().cacheManager
        cache = cacheManager?.getCache(TEST_TABLE, SampleGsonObject2::class.java, Long.MAX_VALUE)
        cache?.clear()
        val user = User()
        user.id = "testId"
        sharedInstance().activeUser = user
    }

    @Test
    fun testInClause() {
        cache?.save(SampleGsonObject2("1", "test1", 0))
        cache?.save(SampleGsonObject2("2", "test2", 0))
        assertEquals(2, cache!![Query(MongoQueryFilterBuilder()).`in`("_id", arrayOf("1", "2"))].size.toLong())
        assertEquals(1, cache!![Query(MongoQueryFilterBuilder()).`in`("_id", arrayOf("1", "3"))].size.toLong())
        assertEquals(0, cache!![Query(MongoQueryFilterBuilder()).`in`("_id", arrayOf("4", "3"))].size.toLong())
        assertEquals(2, cache!![Query(MongoQueryFilterBuilder()).`in`("_id", arrayOf("1", "2", "3", "4"))].size.toLong())
    }

    @Test
    fun testNotIn() {
        cache?.save(SampleGsonObject2("1", "test1", 0))
        cache?.save(SampleGsonObject2("2", "test2", 0))
        assertEquals(0, cache!![Query(MongoQueryFilterBuilder()).notIn("_id", arrayOf<String?>("1", "2"))].size.toLong())
        assertEquals(1, cache!![Query(MongoQueryFilterBuilder()).notIn("_id", arrayOf<String?>("1", "3"))].size.toLong())
        assertEquals(2, cache!![Query(MongoQueryFilterBuilder()).notIn("_id", arrayOf<String?>("4", "3"))].size.toLong())
        assertEquals(0, cache!![Query(MongoQueryFilterBuilder()).notIn("_id", arrayOf<String?>("1", "2", "3", "4"))].size.toLong())
    }

    @Test
    fun testNumeric() {
        cache?.save(SampleGsonObject2("1", "test1", 1))
        cache?.save(SampleGsonObject2("2", "test2", 2))
        cache?.save(SampleGsonObject2("3", "test2", 3))
        assertEquals(1, cache!![Query(MongoQueryFilterBuilder()).lessThan("test", 2)].size.toLong())
        assertEquals(1, cache!![Query(MongoQueryFilterBuilder()).greaterThan("test", 2)].size.toLong())
        assertEquals(2, cache!![Query(MongoQueryFilterBuilder()).lessThanEqualTo("test", 2)].size.toLong())
        assertEquals(2, cache!![Query(MongoQueryFilterBuilder()).greaterThanEqualTo("test", 2)].size.toLong())
        assertEquals(2, cache!![Query(MongoQueryFilterBuilder()).notEqual("test", 2)].size.toLong())
    }

    @Test
    fun testNot() {
        cache?.save(SampleGsonObject2("1", "test1", 1))
        cache?.save(SampleGsonObject2("2", "test2", 2))
        cache?.save(SampleGsonObject2("3", "test2", 3))
        assertEquals(2, cache!![Query(MongoQueryFilterBuilder()).lessThan("test", 2).not()].size.toLong())
        assertEquals(2, cache!![Query(MongoQueryFilterBuilder()).equals("test", 2).not()].size.toLong())
    }

    @Test
    fun testOr() {
        cache?.save(SampleGsonObject2("1", "test1", 1))
        cache?.save(SampleGsonObject2("2", "test2", 2))
        cache?.save(SampleGsonObject2("3", "test2", 3))
        val q = Query(MongoQueryFilterBuilder()).lessThan("test", 2)
        val q2 = Query(MongoQueryFilterBuilder()).greaterThan("test", 2)
        val q3 = Query(MongoQueryFilterBuilder()).lessThan("test", 0).greaterThan("test", 5)
        val q4 = Query(MongoQueryFilterBuilder()).equals("test", 1)
        assertEquals(2, cache!![q.or(q2)].size.toLong())
        assertEquals(1, cache!![q3.or(q4)].size.toLong())
    }

    @Test
    fun testSort() {
        cache?.save(SampleGsonObject2("1", "test1", 1))
        cache?.save(SampleGsonObject2("2", "test2", 2))
        cache?.save(SampleGsonObject2("3", "test2", 3))
        val q = Query(MongoQueryFilterBuilder()).greaterThanEqualTo("test", 1).addSort("test", SortOrder.DESC)
        val objects = cache!![q]
        assertEquals(objects.size.toLong(), 3)
        for (i in 0..2) {
            assertTrue(objects[i].test == (3 - i))
        }
    }

    companion object {
        private const val TEST_TABLE = "queryResultTest3"
    }
}