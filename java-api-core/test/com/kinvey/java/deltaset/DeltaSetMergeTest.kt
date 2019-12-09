package com.kinvey.java.deltaset

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key
import junit.framework.TestCase

class DeltaSetMergeTest : TestCase() {

    val idTest1 = "5d9753c32e3050651455677f"
    val idTest2 = "5d9753c32e2950651455677f"
    val idTest3 = "5d9753c32e2850651455677f"
    val idTest4 = "5d9753c32e2750651455677f"

    val lmtTest1 = "2019-10-04T14:14:27.094Z"
    val lmtTest1_cache = "2019-10-04T10:10:00.000Z"
    val lmtTest2 = "2019-10-05T14:14:27.094Z"
    val lmtTest2_cache = "2019-10-04T10:10:00.000Z"
    val lmtTest3 = "2019-10-06T14:14:27.094Z"
    val lmtTest4 = "2019-10-07T14:14:27.094Z"

    val LMT = "_lmt"

    fun testGetIdsForUpdate_IfSameItemInCache() {
        val idsToUpdateTest = setOf(idTest1, idTest2)
        val cacheList = listOf(
            Entity(id=idTest1, name="test 1", kmd=mapOf(Pair(LMT, lmtTest1_cache))),
            Entity(id=idTest4, name="test 4", kmd=mapOf(Pair(LMT, lmtTest4)))
        )
        val itemsList = listOf(
            DeltaSetItem(idTest1, DeltaSetItem.KMD(lmtTest1)),
            DeltaSetItem(idTest3, DeltaSetItem.KMD(lmtTest3))
        )
        val idsToUpdate = DeltaSetMerge.getIdsForUpdate(cacheList, itemsList)
        assertNotNull(idsToUpdate)
        assertEquals(2, idsToUpdate.size)
        assertEquals(idTest1, idsToUpdate[0])
        assertEquals(idTest3, idsToUpdate[1])
    }

    fun testGetIdsForUpdate_IfDiffItemInCache() {
        val cacheList = listOf(
                //Entity(id=idTest2, name="test 2"),
                Entity(id=idTest2, name="test 2", kmd=mapOf(Pair(LMT, lmtTest2_cache))),
                Entity(id=idTest1, name="test 1", kmd=mapOf(Pair(LMT, lmtTest1_cache))),
                Entity(id=idTest3, name="test 3", kmd=mapOf(Pair(LMT, lmtTest3)))
        )
        val itemsList = listOf(
                DeltaSetItem(idTest1, DeltaSetItem.KMD(lmtTest1)),
                DeltaSetItem(idTest2, DeltaSetItem.KMD(lmtTest2)),
                DeltaSetItem(idTest3, DeltaSetItem.KMD(lmtTest3))
        )
        val idsToUpdate = DeltaSetMerge.getIdsForUpdate(cacheList, itemsList)
        assertNotNull(idsToUpdate)
        assertEquals(2, idsToUpdate.size)
        assertEquals(idTest1, idsToUpdate[0])
        assertEquals(idTest2, idsToUpdate[1])
    }

    fun testMerge() {
        val orderList = listOf(
            DeltaSetItem(idTest1, DeltaSetItem.KMD(lmtTest1)),
            DeltaSetItem(idTest2, DeltaSetItem.KMD(lmtTest2)),
            DeltaSetItem(idTest3, DeltaSetItem.KMD(lmtTest3)),
            DeltaSetItem(idTest4, DeltaSetItem.KMD(lmtTest4)))

        val onlineList = listOf(
                //Entity(id=idTest2, name="test 2"),
                Entity(id=idTest2, name="test 2"),
                Entity(id=idTest1, name="test 1")
        )
        val cacheList = listOf(
                //Entity(id=idTest2, name="test 2"),
                Entity(id=idTest3, name="test 3"),
                Entity(id=idTest4, name="test 4")
        )
        val result = DeltaSetMerge.merge(orderList, cacheList, onlineList)
        assertNotNull(result)
        assertEquals(4, result.size)
        assertEquals(idTest1, result[0].id)
        assertEquals(idTest2, result[1].id)
        assertEquals(idTest3, result[2].id)
        assertEquals(idTest4, result[3].id)
    }

    data class Entity (
        @Key("_id")
        var id: String? = null,
        @Key("_kmd")
        var kmd: Map<String, String>? = null,
        var name: String = ""
    ) : GenericJson()
}