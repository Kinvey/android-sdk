package com.kinvey.java.annotations

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key
import com.kinvey.java.annotations.ReferenceHelper.ReferenceListener
import com.kinvey.java.annotations.ReferenceHelper.processReferences
import junit.framework.TestCase
import org.junit.Test
import java.util.*

/**
 * Created by Prots on 3/11/16.
 */
class AnnotationProcessingTest : TestCase() {
    @Test
    @Throws(InstantiationException::class, IllegalAccessException::class)
    fun testInlineObjects() {
        val genericJson = processReferences(object : GenericJson() {
            @KinveyReference(fieldName = "test", collection = "test", itemClass = AnnotationProcessingTest.SampleGson::class)
            var innerObject = SampleGson("test")
        }, object : ReferenceListener {
            override fun onUnsavedReferenceFound(collection: String, item: GenericJson?): String {
                return "test"
            }
        })
        assertNotNull(genericJson!!["test"])
        assertTrue(Map::class.java.isAssignableFrom(genericJson.javaClass))
        val reference = genericJson["test"] as GenericJson
        assertNotNull(reference["_id"])
        assertEquals(reference["_id"], "test")
        assertEquals(reference["_collection"], "test")
    }

    @Test
    @Throws(InstantiationException::class, IllegalAccessException::class)
    fun testInlineCollection() {
        val inner = (0..9).mapNotNull { i -> SampleGson(i.toString()) }
        val genericJson = processReferences(object : GenericJson() {
            @KinveyReference(fieldName = "test", collection = "test", itemClass = AnnotationProcessingTest.SampleGson::class)
            var innerObject: List<SampleGson> = inner
        }, object : ReferenceListener {
            override fun onUnsavedReferenceFound(collection: String, item: GenericJson?): String {
                return "test"
            }
        })
        assertNotNull(genericJson!!["test"])
        assertTrue(Map::class.java.isAssignableFrom(genericJson.javaClass))
        val reference = genericJson["test"] as List<*>
        assertEquals(reference.size, 10)
    }

    class SampleGson(@field:Key("data") internal var id: String) : GenericJson()
}