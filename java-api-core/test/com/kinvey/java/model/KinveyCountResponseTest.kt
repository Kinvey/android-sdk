package com.kinvey.java.model

import junit.framework.TestCase

class KinveyCountResponseTest : TestCase() {

    var response: KinveyCountResponse? = null

    fun testConstructor() {
        response = KinveyCountResponse()
        assertEquals(0, response?.count)
        response?.count = 1
        assertEquals(1, response?.count)
    }
}