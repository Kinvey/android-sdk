package com.kinvey.java.deltaset

import junit.framework.TestCase

class DeltaSetItemTest : TestCase() {

    val idTest = "5d9753c32e2950651455677f"
    val lmtTest = "2019-10-04T14:14:27.094Z"
    var dto: DeltaSetItem? = null

    fun testConstructor() {
        val kmd: DeltaSetItem.KMD = DeltaSetItem.KMD(lmtTest)
        dto = DeltaSetItem()

        assertNull(dto?.id)
        assertNull(dto?.kmd)

        dto?.id = idTest
        dto?.kmd = kmd

        assertEquals(idTest, dto?.id)
        assertEquals(lmtTest, kmd.lmt)
        assertEquals(kmd, dto?.kmd)
    }
}