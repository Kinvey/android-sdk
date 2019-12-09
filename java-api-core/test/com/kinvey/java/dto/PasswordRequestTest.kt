package com.kinvey.java.dto

import junit.framework.TestCase

class PasswordRequestTest : TestCase() {

    private var dto: PasswordRequest? = null
    private val testPasswd = "test"

    fun testConstructor() {
        dto = PasswordRequest()
        assertNull(dto?.password)
        dto?.password = testPasswd
        assertEquals(testPasswd, dto?.password)
    }
}