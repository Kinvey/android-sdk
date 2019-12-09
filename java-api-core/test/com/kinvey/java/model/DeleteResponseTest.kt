package com.kinvey.java.model

import junit.framework.TestCase

/**
 * Created by edward on 7/31/15.
 */
class DeleteResponseTest : TestCase() {

    var kdr: KinveyDeleteResponse? = null

    fun testConstructor() {
        kdr = KinveyDeleteResponse()
        assertEquals(0, kdr?.count)
        kdr?.count = 1
        assertEquals(1, kdr?.count)
    }
}