package com.kinvey.java.model

import com.kinvey.java.core.KinveyMockUnitTest
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.model.Aggregation.Result
import java.util.*

/**
 * Created by edward on 7/31/15.
 */
class AggregationTest : KinveyMockUnitTest<BaseUser>() {

    private var agg: Aggregation? = null

    fun testConstruction() {
        agg = Aggregation(ArrayList())
        assertNotNull(agg?.results)
        agg = Aggregation(null)
        assertNotNull(agg?.results)
        val res = Result()
        assertNull(res.result)
    }

    fun testResults() {
        val res = Result()
        res.result = 1
        res["key"] = "value"
        val list = listOf(res)
        agg = Aggregation(list)
        assertEquals(1, agg?.getResultsFor("key", "value")!![0])
        assertEquals(0, agg?.getResultsFor("key", "novalue")?.size)
        assertEquals(0, agg?.getResultsFor("nokey", "novalue")?.size)
    }
}