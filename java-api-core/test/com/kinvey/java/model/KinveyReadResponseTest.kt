package com.kinvey.java.model

import junit.framework.TestCase

class KinveyReadResponseTest : TestCase() {

    var response: KinveyReadResponse<Entity>? = null

    fun testConstructor() {
        response = KinveyReadResponse()

        assertEquals(null, response?.result)
        assertEquals(null, response?.lastRequestTime)

        val result = listOf(Entity("test1"), Entity("test2"))
        val lastRequestTime = "lastRequestTime"

        response?.result = result
        response?.lastRequestTime = lastRequestTime

        assertEquals(result, response?.result)
        assertEquals(lastRequestTime, response?.lastRequestTime)
    }

    data class Entity (
        var name: String = ""
    )
}