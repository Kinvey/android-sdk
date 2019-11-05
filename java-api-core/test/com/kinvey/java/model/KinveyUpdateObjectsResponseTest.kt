package com.kinvey.java.model

import com.google.api.client.json.GenericJson
import junit.framework.TestCase
import java.lang.Exception

class KinveyUpdateObjectsResponseTest : TestCase() {

    var response: KinveyUpdateObjectsResponse<Entity>? = null

    fun testConstructor() {

        val errMsg = "test exception"
        val entities = listOf(Entity("test1"), Entity("test2"))
        val errors = listOf(KinveyUpdateSingleItemError(Exception(errMsg), GenericJson()))

        response = KinveyUpdateObjectsResponse(entities, errors)

        assertEquals(entities, response?.entities)
        assertEquals(errors, response?.errors)
        assertTrue(response?.haveErrors == true)
    }

    data class Entity (
            var name: String = ""
    )
}