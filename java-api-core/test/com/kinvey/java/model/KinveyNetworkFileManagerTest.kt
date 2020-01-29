package com.kinvey.java.model

import junit.framework.TestCase

/**
 * Created by edward on 7/31/15.
 */
class KinveyNetworkFileManagerTest : TestCase() {

    var kf: KinveyFile? = null

    fun testConstructor() {
        kf = KinveyFile()
        assertEquals("KinveyRef", kf!!["_type"])
        assertNull(kf?.id)
        kf?.id = "ok"
        assertEquals("ok", kf?.id)
        kf = KinveyFile("id")
        assertEquals("id", kf?.id)
    }
}