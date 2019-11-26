package com.kinvey.java.sync

import com.google.api.client.http.GenericUrl
import com.google.api.client.json.jackson.JacksonFactory
import com.kinvey.java.sync.dto.SyncRequest
import junit.framework.TestCase

class SyncRequestTest : TestCase() {

    private val HTTP_VERB =  SyncRequest.HttpVerb.POST
    private val ENTITY_ID = "1234567890"
    private val CLIENT_APP_VERSION = "v1.0.0"
    private val CUSTOM_PROPERTIES = "custom props"
    private val URL = GenericUrl("https://google.com")
    private val COLLECTION_NAME = "Person"
    private val TEST_JSON = "{\"_id\": \"testId\"}"

    fun testSyncRequestConstructor1() {
        val request = SyncRequest(HTTP_VERB, ENTITY_ID, CLIENT_APP_VERSION, CUSTOM_PROPERTIES, URL, COLLECTION_NAME)
        assertEquals(HTTP_VERB, request.httpVerb)
        assertEquals(ENTITY_ID, request.entityID?.id)
        assertEquals(CLIENT_APP_VERSION, request.entityID?.customerVersion)
        assertEquals(CUSTOM_PROPERTIES, request.entityID?.customheader)
        assertEquals(URL.toString(), request.url)
        assertEquals(COLLECTION_NAME, request.collectionName)
    }

    fun testSyncRequestConstructor2() {
        val syncMetaData = SyncRequest.SyncMetaData(ENTITY_ID, CLIENT_APP_VERSION, CUSTOM_PROPERTIES)
        val request = SyncRequest(HTTP_VERB, syncMetaData, URL, COLLECTION_NAME)

        assertEquals(HTTP_VERB, request.httpVerb)
        assertEquals(URL.toString(), request.url)
        assertEquals(COLLECTION_NAME, request.collectionName)

        assertEquals(syncMetaData, request.entityID)
        assertEquals(syncMetaData.id, request.entityID?.id)
        assertEquals(syncMetaData.customerVersion, request.entityID?.customerVersion)
        assertEquals(syncMetaData.customheader, request.entityID?.customheader)
    }

    fun testHttpVerb() {
        val httpVerbGet1 = "GeT"
        val httpVerbGet2 = "get"
        val httpVerbGet3 = "GET"
        val httpVerbPost1 = "PoSt"
        val httpVerbPost2 = "post"
        val httpVerbPost3 = "POST"
        assertEquals(SyncRequest.HttpVerb.GET, SyncRequest.HttpVerb.fromString(httpVerbGet1))
        assertEquals(SyncRequest.HttpVerb.GET, SyncRequest.HttpVerb.fromString(httpVerbGet2))
        assertEquals(SyncRequest.HttpVerb.GET, SyncRequest.HttpVerb.fromString(httpVerbGet3))
        assertEquals(SyncRequest.HttpVerb.POST, SyncRequest.HttpVerb.fromString(httpVerbPost1))
        assertEquals(SyncRequest.HttpVerb.POST, SyncRequest.HttpVerb.fromString(httpVerbPost2))
        assertEquals(SyncRequest.HttpVerb.POST, SyncRequest.HttpVerb.fromString(httpVerbPost3))
    }

    fun testSyncMetaData() {
        val syncMetaData = SyncRequest.SyncMetaData(ENTITY_ID, CLIENT_APP_VERSION, CUSTOM_PROPERTIES, true)
        assertEquals(ENTITY_ID, syncMetaData.id)
        assertEquals(CLIENT_APP_VERSION, syncMetaData.customerVersion)
        assertEquals(CUSTOM_PROPERTIES, syncMetaData.customheader)
        assertTrue(syncMetaData.bunchData)
    }

    fun testSyncMetaDataParsing() {
        val syncMetaData = SyncRequest.SyncMetaData(ENTITY_ID, CLIENT_APP_VERSION, CUSTOM_PROPERTIES, true)
        syncMetaData.factory = JacksonFactory()
        syncMetaData.data = TEST_JSON
        val entity = syncMetaData.entity
        assertEquals("testId", entity?.get("_id"))
    }
}