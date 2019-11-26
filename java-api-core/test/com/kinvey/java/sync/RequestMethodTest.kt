package com.kinvey.java.sync

import junit.framework.TestCase

class RequestMethodTest : TestCase() {

    private val saveMethod1 = "SAVE"
    private val saveMethod2 = "SaVe"
    private val deleteMethod1 = "DELETE"
    private val deleteMethod2 = "DelEtE"

    fun testGetEntityFromString() {
        assertEquals(RequestMethod.SAVE, RequestMethod.fromString(saveMethod1))
        assertEquals(RequestMethod.SAVE, RequestMethod.fromString(saveMethod2))
        assertEquals(RequestMethod.DELETE, RequestMethod.fromString(deleteMethod1))
        assertEquals(RequestMethod.DELETE, RequestMethod.fromString(deleteMethod2))
    }
}