package com.kinvey.java.model

import com.kinvey.java.core.KinveyMockUnitTest
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.model.KinveyMetaData.AccessControlList

/**
 * Created by edward on 7/31/15.
 */
class ACLTest : KinveyMockUnitTest<BaseUser>() {

    private var kmd: KinveyMetaData? = null
    private var acl: AccessControlList? = null

    fun testConstructor() {
        kmd = KinveyMetaData()
        assertEquals(null, kmd?.entityCreationTime)
        acl = AccessControlList()
        assertEquals(null, acl?.getCreator())
    }

    fun testKMDFields() {
        kmd = KinveyMetaData()
        kmd!!["ect"] = "yesterday"
        assertEquals("yesterday", kmd?.entityCreationTime)
        kmd!!["lmt"] = "today"
        assertEquals("today", kmd?.lastModifiedTime)
    }

    fun testACLFields() {
        acl = AccessControlList()
        acl?.setGloballyReadable(true)
        assertTrue(acl?.isGloballyReadable == true)
        acl?.setGloballyWritable(true)
        assertTrue(acl?.isGloballyReadable == true)
        acl?.setCreator("creator")
        assertEquals("creator", acl?.getCreator())
    }
}