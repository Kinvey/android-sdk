package com.kinvey.java.model

import junit.framework.TestCase

class KinveyErrorResponseTest : TestCase() {

    var response: KinveyErrorResponse? = null

    fun testConstructor() {
        response = KinveyErrorResponse()

        assertNull(response?.debug)
        response?.debug = "debug"
        assertEquals("debug", response?.debug)

        assertNull(response?.description)
        response?.description = "description"
        assertEquals("description", response?.description)

        assertNull(response?.error)
        response?.error = "error"
        assertEquals("error", response?.error)
    }
}