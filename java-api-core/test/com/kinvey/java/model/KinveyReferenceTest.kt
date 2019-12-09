package com.kinvey.java.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.ArrayMap
import com.google.api.client.util.Key
import com.kinvey.java.core.KinveyMockUnitTest
import com.kinvey.java.dto.BaseUser

/**
 * Created by edward on 7/31/15.
 */
class KinveyReferenceTest : KinveyMockUnitTest<BaseUser>() {

    var ref: KinveyReference? = null

    fun testInit() {
        ref = KinveyReference()
        assertEquals(ref?.type, "KinveyRef")
        assertEquals(ref?.id, null)
        assertEquals(ref?.collection, null)
        ref = KinveyReference("collection", "id")
        assertEquals(ref?.type, "KinveyRef")
        assertEquals(ref?.id, "id")
        assertEquals(ref?.collection, "collection")
    }

    fun testSetters() {
        ref = KinveyReference()
        ref?.collection = "collection"
        ref?.id = "id"
        ref?.type = ""
        assertEquals(ref?.type, "KinveyRef")
        assertEquals(ref?.id, "id")
        assertEquals(ref?.collection, "collection")
    }

    fun testResolvedObject() {
        ref = KinveyReference()
        assertNull(ref!!.resolvedObject)
        val some = ArrayMap<Any?, Any?>()
        some["hello"] = "hello"
        ref!!["_obj"] = some
        assertEquals(ref?.resolvedObject!!["hello"], "hello")
    }

    fun testTypedObject() {
        ref = KinveyReference()
        assertNull(ref!!.getTypedObject(RefTest::class.java))
        val some = ArrayMap<Any?, Any?>()
        some["Hello"] = "hi"
        ref!!["_obj"] = some
        val check = ref?.getTypedObject(RefTest::class.java)
        val direct = ref!!["_obj"] as ArrayMap<*, *>?
        assertNotNull(direct)
        try {
            val ok = RefTest::class.java.newInstance()
        } catch (e: Exception) {
            fail(e.message)
        }
        assertEquals(check?.Hello, "hi")
    }

    class RefTest() : GenericJson() {
        @Key
        var Hello: String? = null
        constructor(hello: String?) : this() {
            Hello = hello
        }
    }
}