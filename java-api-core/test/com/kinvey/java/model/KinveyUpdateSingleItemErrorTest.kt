package com.kinvey.java.model

import com.google.api.client.json.GenericJson
import junit.framework.TestCase
import java.lang.Exception

class KinveyUpdateSingleItemErrorTest: TestCase() {

    var response: KinveyUpdateSingleItemError? = null

    fun testConstructor() {

        val errMsg = "test exception"
        val code = 400L
        val entity = Entity("test entity")

        response = KinveyUpdateSingleItemError(Exception(errMsg), entity)
        response?.code = code

        assertEquals(code, response?.code)
        assertEquals(entity, response?.entity)
        assertEquals(errMsg, response?.errorMessage)
    }

    data class Entity (
        var name: String = ""
    ): GenericJson()
}