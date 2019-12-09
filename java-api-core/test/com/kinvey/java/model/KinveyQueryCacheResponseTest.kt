package com.kinvey.java.model

import junit.framework.TestCase

class KinveyQueryCacheResponseTest : TestCase() {

    var response: KinveyQueryCacheResponse<Entity>? = null

    fun testConstructor() {
        response = KinveyQueryCacheResponse()

        assertEquals(null, response?.changed)
        assertEquals(null, response?.deleted)
        assertEquals(null, response?.lastRequestTime)

        val deleted = listOf(Entity("test1"), Entity("test2"))
        val changed = listOf(Entity("test3"), Entity("test4"))
        val lastRequestTime = "lastRequestTime"

        response?.changed = changed
        response?.deleted = deleted
        response?.lastRequestTime = lastRequestTime

        assertEquals(changed, response?.changed)
        assertEquals(deleted, response?.deleted)
        assertEquals(lastRequestTime, response?.lastRequestTime)
    }

    data class Entity (
        var name: String = ""
    )
}