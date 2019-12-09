package com.kinvey.java.sync

import com.kinvey.java.sync.dto.SyncItem
import com.kinvey.java.sync.dto.SyncRequest
import junit.framework.TestCase

class SyncItemTest : TestCase() {
    private val HTTP_VERB =  SyncRequest.HttpVerb.POST
    private val ENTITY_ID = "1234567890"
    private val CLIENT_APP_VERSION = "v1.0.0"
    private val CUSTOM_PROPERTIES = "custom props"
    private val COLLECTION_NAME = "Person"

    fun testConstructor() {
        val syncMetaData = SyncRequest.SyncMetaData(ENTITY_ID, CLIENT_APP_VERSION, CUSTOM_PROPERTIES)
        val syncItem = SyncItem(HTTP_VERB, syncMetaData, COLLECTION_NAME)

        assertEquals(HTTP_VERB, syncItem.requestMethod)
        assertEquals(COLLECTION_NAME, syncItem.collectionName)

        assertEquals(syncMetaData, syncItem.entityID)
        assertEquals(syncMetaData.id, syncItem.entityID?.id)
        assertEquals(syncMetaData.customerVersion, syncItem.entityID?.customerVersion)
        assertEquals(syncMetaData.customheader, syncItem.entityID?.customheader)
    }
}