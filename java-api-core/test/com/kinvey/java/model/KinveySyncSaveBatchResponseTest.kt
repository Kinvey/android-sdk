package com.kinvey.java.model

import junit.framework.TestCase

class KinveySyncSaveBatchResponseTest : TestCase() {
    var response: KinveySyncSaveBatchResponse<Entity>? = null

    fun testConstructor() {

        val entities = listOf(Entity("test1"), Entity("test2"))
        val errors = listOf(KinveyBatchInsertError(0, 400, "test error"))

        response = KinveySyncSaveBatchResponse(entities, errors)

        assertEquals(entities, response?.entityList)
        assertEquals(errors, response?.errors)
    }

    data class Entity (
            var name: String = ""
    )
}