package com.kinvey.java.dto

import junit.framework.TestCase

class EmailTest : TestCase() {

    private val emailTest = "test@email.com"

    fun testConstructor() {
        val dto = Email()
        assertNull(dto.email)
        dto.email = emailTest
        assertEquals(emailTest, dto.email)
    }
}