package com.kinvey.java.dto

import junit.framework.TestCase

class DeviceIdTest : TestCase() {

    private val deviceId = "7d32f65a510424f8"

    fun testConstructor() {
        val dto = DeviceId()
        assertNull(dto.deviceId)
        dto.deviceId = deviceId
        assertEquals(deviceId, dto.deviceId)
    }
}