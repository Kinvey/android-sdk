/**
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 *
 */
package com.kinvey.java

import com.google.api.client.json.GenericJson
import com.kinvey.java.core.KinveyMockUnitTest
import java.io.IOException
import java.util.*

/**
 * @author edwardf
 * @since 2.0
 */
class BaseUserGroupTest : KinveyMockUnitTest<*>() {
    fun testUserGroupUpdate() {
        val group = UserGroup(client!!, getKinveyRequestInitializer())
        try {
            val update = group.addUserToGroupBlocking("Group1", "user1", "subgroup1")
            assertNotNull(update)
            assertEquals("Group1", update.jsonContent!![ID])
            assertEquals(1, ((update.jsonContent!![USERS] as GenericJson?)!![LIST] as ArrayList<*>?)!!.size)
            assertEquals("user1", (((update.jsonContent!![USERS] as GenericJson?)!![LIST] as ArrayList<*>?)!![0] as GenericJson)[ID])
        } catch (io: IOException) {
            fail("IO -> " + io.message)
        }
    }

    fun testUserGroupRetrieve() {
        val group = UserGroup(client!!, getKinveyRequestInitializer())
        try {
            val ret = group.retrieve("Group1")
            assertNotNull(ret)
            assertEquals("Group1", ret["groupID"])
        } catch (io: IOException) {
            fail("IO -> " + io.message)
        }
    }

    fun testUserGroupDelete() {
        val group = UserGroup(client!!, getKinveyRequestInitializer())
        try {
            val delete = group.delete("Group1")
            assertNotNull(delete)
            assertEquals("Group1", delete["groupID"])
        } catch (io: IOException) {
            fail("IO -> " + io.message)
        }
    }

    companion object {
        private val ID: String? = "_id"
        private val USERS: String? = "users"
        private val LIST: String? = "list"
    }
}