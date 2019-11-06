package com.kinvey.java.dto

import junit.framework.TestCase

class UsernameTest : TestCase() {

    private var dto: Username? = null
    private val userName = "test user"

    fun testConstructor() {
        dto = Username()
        assertNull(dto?.username)
        dto?.username = userName
        assertEquals(userName, dto?.username)
    }
}