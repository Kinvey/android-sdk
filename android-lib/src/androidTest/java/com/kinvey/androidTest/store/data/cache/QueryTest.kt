package com.kinvey.androidTest.store.data.cache

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.kinvey.android.cache.QueryHelper.prepareRealmQuery
import com.kinvey.java.Query
import com.kinvey.java.query.AbstractQuery.SortOrder
import com.kinvey.java.query.MongoQueryFilter.MongoQueryFilterBuilder
import io.realm.DynamicRealm
import io.realm.DynamicRealmObject
import io.realm.Realm
import io.realm.RealmConfiguration.Builder
import io.realm.RealmQuery
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*

/**
 * Created by Prots on 30/01/16.
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class QueryTest {
    private var query: RealmQuery<DynamicRealmObject>? = null
    @Before
    fun setup() {
        val mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        Realm.init(mMockContext)
        val realm: DynamicRealm = DynamicRealm.getInstance(Builder().build())
        if (!realm.schema.contains("test")) {
            realm.beginTransaction()
            realm.schema.create("test").addField("_id", Int::class.java)
            realm.commitTransaction()
        }
        val query = mock(RealmQuery::class.java)
        doReturn(query).`when`(query).beginGroup()
        doReturn(query).`when`(query).equalTo(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
        doReturn(query).`when`(query).endGroup()
        doReturn(query).`when`(query).or()
        doReturn(query).`when`(query).not()
        this.query = query as RealmQuery<DynamicRealmObject>?
    }

    @After
    fun tearDown() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        Realm.init(mMockContext)
        val realm: DynamicRealm = DynamicRealm.getInstance(Builder().build())
        if (realm.schema.contains("test")) {
            realm.beginTransaction()
            realm.schema.remove("test")
            realm.commitTransaction()
        }
    }

    @Test
    fun testInClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.`in`("_id", arrayOf("1", "2"))
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.beginGroup()
        verify(query, times(2))?.equalTo(ArgumentMatchers.eq("_id"), ArgumentMatchers.anyString())
        verify(query, times(1))?.or()
        verify(query, times(1))?.endGroup()
    }

    @Test
    fun testNotInClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.notIn("_id", arrayOf<String?>("1", "2"))
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(2))?.beginGroup()
        verify(query, times(2))?.equalTo(ArgumentMatchers.eq("_id"), ArgumentMatchers.anyString())
        verify(query, times(1))?.or()
        verify(query, times(2))?.endGroup()
        verify(query, times(1))?.not()
    }

    @Test
    fun testGreaterThanIntClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.greaterThan(TEST_FIELD, 1)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.greaterThan(TEST_FIELD, 1)
    }

    @Test
    fun testGreaterThanLongClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.greaterThan(TEST_FIELD, 1L)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.greaterThan(TEST_FIELD, 1L)
    }

    @Test
    fun testGreaterThanDoubleClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.greaterThan(TEST_FIELD, 1.0)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.greaterThan(TEST_FIELD, 1.0)
    }

    @Test
    fun testGreaterThanFloatClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.greaterThan(TEST_FIELD, 1.0f)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.greaterThan(TEST_FIELD, 1.0f)
    }

    @Test
    fun testGreaterThanOrEqualToIntClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.greaterThanEqualTo(TEST_FIELD, 1)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.greaterThanOrEqualTo(TEST_FIELD, 1)
    }

    @Test
    fun testGreaterThanOrEqualToLongClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.greaterThanEqualTo(TEST_FIELD, 1L)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.greaterThanOrEqualTo(TEST_FIELD, 1L)
    }

    @Test
    fun testGreaterThanOrEqualToDoubleClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.greaterThanEqualTo(TEST_FIELD, 1.0)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.greaterThanOrEqualTo(TEST_FIELD, 1.0)
    }

    @Test
    fun testGreaterThanOrEqualToFloatClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.greaterThanEqualTo(TEST_FIELD, 1.0f)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.greaterThanOrEqualTo(TEST_FIELD, 1.0f)
    }

    @Test
    fun testLessThanIntClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.lessThan(TEST_FIELD, 1)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.lessThan(TEST_FIELD, 1)
    }

    @Test
    fun testLessThanLongClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.lessThan(TEST_FIELD, 1L)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.lessThan(TEST_FIELD, 1L)
    }

    @Test
    fun testLessThanDoubleClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.lessThan(TEST_FIELD, 1.0)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.lessThan(TEST_FIELD, 1.0)
    }

    @Test
    fun testLessThanFloatClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.lessThan(TEST_FIELD, 1.0f)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.lessThan(TEST_FIELD, 1.0f)
    }

    @Test
    fun testLessThanOrEqualToIntClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.lessThanEqualTo(TEST_FIELD, 1)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.lessThanOrEqualTo(TEST_FIELD, 1)
    }

    @Test
    fun testLessThanOrEqualToLongClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.lessThanEqualTo(TEST_FIELD, 1L)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.lessThanOrEqualTo(TEST_FIELD, 1L)
    }

    @Test
    fun testLessThanOrEqualToDoubleClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.lessThanEqualTo(TEST_FIELD, 1.0)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.lessThanOrEqualTo(TEST_FIELD, 1.0)
    }

    @Test
    fun testLessThanOrEqualToFloatClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.lessThanEqualTo(TEST_FIELD, 1.0f)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.lessThanOrEqualTo(TEST_FIELD, 1.0f)
    }

    @Test
    fun testNotEqualBooleanClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.notEqual(TEST_FIELD, true)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.notEqualTo(TEST_FIELD, true)
    }

    @Test
    fun testNotEqualByteClause() {
        val q = Query(MongoQueryFilterBuilder())
        val aByte: Byte = 1
        q.notEqual(TEST_FIELD, aByte)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.notEqualTo(TEST_FIELD, aByte)
    }

    @Test
    fun testNotEqualByteArrayClause() {
        val q = Query(MongoQueryFilterBuilder())
        val bytes = byteArrayOf(1)
        q.notEqual(TEST_FIELD, bytes)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.notEqualTo(TEST_FIELD, bytes)
    }

    @Test
    fun testNotEqualShortClause() {
        val q = Query(MongoQueryFilterBuilder())
        val aShort: Short = 12345
        q.notEqual(TEST_FIELD, aShort)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.notEqualTo(TEST_FIELD, aShort)
    }

    @Test
    fun testNotEqualIntClause() {
        val q = Query(MongoQueryFilterBuilder())
        val intVal: Int = 1
        q.notEqual(TEST_FIELD, intVal)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.notEqualTo(TEST_FIELD, intVal)
    }

    @Test
    fun testNotEqualLongClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.notEqual(TEST_FIELD, 1L)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.notEqualTo(TEST_FIELD, 1L)
    }

    @Test
    fun testNotEqualDoubleClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.notEqual(TEST_FIELD, 1.0)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.notEqualTo(TEST_FIELD, 1.0)
    }

    @Test
    fun testNotEqualFloatClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.notEqual(TEST_FIELD, 1f)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.notEqualTo(TEST_FIELD, 1f)
    }

    @Test
    fun testNotEqualStringClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.notEqual(TEST_FIELD, TEST_FIELD)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.notEqualTo(TEST_FIELD, TEST_FIELD)
    }

    @Test
    fun tesEqualByteClause() {
        val q = Query(MongoQueryFilterBuilder())
        val aByte: Byte = 1
        q.equals(TEST_FIELD, aByte)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.equalTo(TEST_FIELD, aByte)
    }

    @Test
    fun testEqualByteArrayClause() {
        val q = Query(MongoQueryFilterBuilder())
        val bytes = byteArrayOf(1)
        q.equals(TEST_FIELD, bytes)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.equalTo(TEST_FIELD, bytes)
    }

    @Test
    fun testEqualShortClause() {
        val q = Query(MongoQueryFilterBuilder())
        val aShort: Short = 12345
        q.equals(TEST_FIELD, aShort)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.equalTo(TEST_FIELD, aShort)
    }

    @Test
    fun testEqualIntClause() {
        val q = Query(MongoQueryFilterBuilder())
        val intVal: Int = 1
        q.equals(TEST_FIELD, intVal)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.equalTo(TEST_FIELD, intVal)
    }

    @Test
    fun testEqualLongClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.equals(TEST_FIELD, 1L)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.equalTo(TEST_FIELD, 1L)
    }

    @Test
    fun testEqualDoubleClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.equals(TEST_FIELD, 1.0)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.equalTo(TEST_FIELD, 1.0)
    }

    @Test
    fun testEqualFloatClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.equals(TEST_FIELD, 1f)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.equalTo(TEST_FIELD, 1f)
    }

    @Test
    fun testEqualStringClause() {
        val q = Query(MongoQueryFilterBuilder())
        q.equals(TEST_FIELD, TEST_FIELD)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.equalTo(TEST_FIELD, TEST_FIELD)
    }

    @Test
    fun testAddSortASC() {
        val q = Query(MongoQueryFilterBuilder())
        q.addSort(TEST_FIELD, SortOrder.ASC)
        prepareRealmQuery(query!!, q.queryFilterMap)

        //check that addSort isn't used in prepareRealmQuery
        verify(query, times(0))?.beginGroup()
        verify(query, times(0))?.equalTo(ArgumentMatchers.eq(TEST_FIELD), ArgumentMatchers.anyString())
        verify(query, times(0))?.or()
        verify(query, times(0))?.not()
        verify(query, times(0))?.endGroup()
        assertEquals("{\"field\" : 1}", q.sortString)
    }

    @Test
    fun testAddSortDESC() {
        val q = Query(MongoQueryFilterBuilder())
        q.addSort(TEST_FIELD, SortOrder.DESC)
        prepareRealmQuery(query!!, q.queryFilterMap)

        //check that addSort isn't used in prepareRealmQuery
        verify(query, times(0))?.beginGroup()
        verify(query, times(0))?.equalTo(ArgumentMatchers.eq(TEST_FIELD), ArgumentMatchers.anyString())
        verify(query, times(0))?.or()
        verify(query, times(0))?.not()
        verify(query, times(0))?.endGroup()
        assertEquals("{\"field\" : -1}", q.sortString)
    }

    @Test
    fun testOrClause() {
        val q = Query(MongoQueryFilterBuilder())
        val q2 = Query(MongoQueryFilterBuilder())
        q.`in`(TEST_FIELD, arrayOf("1", "2"))
        q2.`in`(TEST_FIELD, arrayOf("1", "2"))
        q.or(q2)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(5))?.beginGroup()
        verify(query, times(4))?.equalTo(ArgumentMatchers.eq(TEST_FIELD), ArgumentMatchers.anyString())
        verify(query, times(3))?.or()
        verify(query, times(5))?.endGroup()
    }

    @Test
    fun testAndClause() {
        val q = Query(MongoQueryFilterBuilder())
        val q2 = Query(MongoQueryFilterBuilder())
        val intVal1 = 1
        val intVal2 = 2
        q.notEqual(TEST_FIELD, intVal1)
        q2.notEqual(TEST_FIELD, intVal2)
        q.and(q2)
        prepareRealmQuery(query!!, q.queryFilterMap)
        verify(query, times(1))?.notEqualTo(TEST_FIELD, intVal1)
        verify(query, times(1))?.notEqualTo(TEST_FIELD, intVal2)
        verify(query, times(3))?.beginGroup()
        verify(query, times(3))?.endGroup()
    }

    @Test
    fun testUnsupportedOperationException() {
        val q = Query()
        q.startsWith(TEST_FIELD, 1)
        try {
            prepareRealmQuery(query!!, q.queryFilterMap)
        } catch (e: UnsupportedOperationException) {
            assertEquals("this query is not supported by cache", e.message)
        }
    }

    companion object {
        private const val TEST_FIELD = "field"
    }
}