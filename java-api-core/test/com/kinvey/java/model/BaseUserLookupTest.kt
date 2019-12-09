package com.kinvey.java.model

import junit.framework.TestCase

/**
 * Created by edward on 7/31/15.
 */
class BaseUserLookupTest : TestCase() {

    var ul: UserLookup? = null

    fun testConstructor() {
        ul = UserLookup()
        assertNull(ul?.email)
    }

    fun testProperties() {
        ul = UserLookup()
        ul?.id = "id"
        assertEquals("id", ul?.id)
        ul?.email = "email"
        assertEquals("email", ul?.email)
        ul?.firstName = "first"
        assertEquals("first", ul?.firstName)
        ul?.lastName = "last"
        assertEquals("last", ul?.lastName)
        ul?.facebookID = "facebook"
        assertEquals("facebook", ul?.facebookID)
        ul?.username = "username"
        assertEquals("username", ul?.username)
    }
}