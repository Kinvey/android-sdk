package com.kinvey.java.model

import com.kinvey.java.core.KinveyMockUnitTest
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.model.KinveyMetaData.AccessControlList

/**
 * Created by edward on 7/31/15.
 */
class NetworkFileManagerMetaDataTest : KinveyMockUnitTest<BaseUser>() {

    var fdm: FileMetaData? = null

    fun testConstructor() {
        fdm = FileMetaData()
        assertEquals(null, fdm?.id)
        fdm = FileMetaData("id")
        assertEquals("id", fdm?.id)
    }

    fun testUploadDownloadURL() {
        fdm = FileMetaData()
        fdm?.downloadURL = "download"
        assertEquals("download", fdm?.downloadURL)
        fdm?.uploadUrl = "upload"
        assertEquals("upload", fdm?.uploadUrl)
    }

    fun testOtherFields() {
        fdm = FileMetaData()
        fdm?.fileName = "myfile"
        assertEquals("myfile", fdm?.fileName)
        fdm?.isPublic = true
        assertEquals(true, fdm?.isPublic)
        val acl = AccessControlList()
        acl.setCreator("123")
        fdm?.acl = acl
        assertEquals("123", fdm?.acl?.getCreator())
        fdm?.mimetype = "mime"
        assertEquals("mime", fdm?.mimetype)
        fdm?.fileName = "name"
        assertEquals("name", fdm?.fileName)
        fdm?.setSize(100)
        assertEquals(100, fdm?.size)
    }
}