package com.kinvey.java.dto

import junit.framework.TestCase

class LiveServiceRegisterResponseTest : TestCase() {

    private var dto: LiveServiceRegisterResponse? = null

    private val userChannelGroupTest = "testChannel"
    private val publishKeyTest = "12345678901234567890"
    private val subscribeKeyTest = "09876543210987654321"

    fun testConstructor() {
        dto = LiveServiceRegisterResponse()

        assertNull(dto?.userChannelGroup)
        assertNull(dto?.publishKey)
        assertNull(dto?.subscribeKey)

        dto?.userChannelGroup = userChannelGroupTest
        dto?.publishKey = publishKeyTest
        dto?.subscribeKey = subscribeKeyTest

        assertEquals(userChannelGroupTest, dto?.userChannelGroup)
        assertEquals(publishKeyTest, dto?.publishKey)
        assertEquals(subscribeKeyTest, dto?.subscribeKey)
    }
}