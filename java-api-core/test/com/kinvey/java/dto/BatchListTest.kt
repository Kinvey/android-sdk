package com.kinvey.java.dto

import junit.framework.TestCase

class BatchListTest : TestCase() {

    private val entityName = "test entity 1"
    private val jsonTest = "[{\"name\":\"test entity 1\"}]"

    fun testConstructor() {
        val list = listOf(Entity(entityName))
        val dto = BatchList(list)
        assertEquals(list, dto.itemsList)
    }

    fun testJsonGeneration() {
        val list = listOf(Entity(entityName))
        val dto = BatchList(list)
        val json = dto.toString()

        assertEquals(list, dto.itemsList)
        assertEquals(jsonTest, json)
    }

    data class Entity(
        var name: String = ""
    )
}