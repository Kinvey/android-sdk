package com.kinvey.java.model

import com.kinvey.java.Query
import junit.framework.TestCase
import org.apache.commons.logging.Log
import java.util.*

/**
 * Created by edward on 7/31/15.
 */
class AggregateEntityTest : TestCase() {

    var ae: AggregateEntity? = null

    fun testConstructor() {
        val fields = ArrayList<String>()
        fields.add("key")
        ae = AggregateEntity(fields, AggregateType.COUNT, "field", Query(), null)
//        public AggregateEntity(ArrayList<String> fields, AggregateType type, String aggregateField, Query query,
//                AbstractKinveyJsonClient client) {
        assertEquals(true, ae?.key!!["key"] as Boolean)
        assertEquals(0, ae?.initial!!["_result"])
        assertEquals("function(doc,out){ out._result++;}", ae?.reduce)
        assertNotNull(ae?.condition)
        try {
            ae = AggregateEntity(fields, null, "field", Query(), null)
            fail("should have errored out on null!")
        } catch (e: Exception) {
            print(e)
        }
    }

    fun testEnums() {
        assertEquals(AggregateType.COUNT, AggregateType.valueOf("COUNT"))
    }
}