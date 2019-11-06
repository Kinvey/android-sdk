package com.kinvey.java.dto

import junit.framework.TestCase

class UserNameExistsTest : TestCase() {
    
    var dto: UserNameExists? = null

    fun testSetUserName() {
        dto = UserNameExists()
        assertTrue(dto?.doesUsernameExist() == false)
        dto?.setUsernameExists(true)
        assertTrue(dto?.doesUsernameExist() == true)
    }
}