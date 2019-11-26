package com.kinvey.java.sync

import com.kinvey.java.sync.dto.SyncCollections
import junit.framework.TestCase

class SyncCollectionsTest : TestCase() {

    private val SYNC_COLLECTION = "SyncCollection"

    fun testConstructor() {
        val collection = SyncCollections(SYNC_COLLECTION)
        assertEquals(SYNC_COLLECTION, collection.collectionName)
    }
}