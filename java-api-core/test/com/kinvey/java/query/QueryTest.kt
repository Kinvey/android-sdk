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
package com.kinvey.java.query

import com.kinvey.java.Query
import com.kinvey.java.query.MongoQueryFilter.MongoQueryFilterBuilder
import junit.framework.TestCase
import org.junit.Assert.assertArrayEquals
import java.io.IOException
import java.util.*

/**
 * @author mjsalinger
 * @since 2.0
 */

class QueryTest : TestCase() {

    private var myQuery: Query? = null

    override fun setUp() {
        myQuery = Query(MongoQueryFilterBuilder())
    }

    fun testGetJsonWithNullFactory() {
        try {
            myQuery?.equals("_id", "newEntity")
            myQuery?.getQueryFilterJson(null)
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    fun testEquals() {
        myQuery?.equals("_id", "newEntity")
        assertEquals("newEntity", myQuery?.queryFilterMap!!["_id"])
    }

    @Throws(IOException::class)
    fun testEqualsNullField() {
        try {
            myQuery?.equals(null, "newEntity")
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    fun testGreaterThan() {
        myQuery?.greaterThan("_id", "2")
        val lh = LinkedHashMap<String, Any>()
        lh["\$gt"] = "2"
        assertEquals(lh, myQuery?.queryFilterMap!!["_id"])
    }

    @Throws(IOException::class)
    fun testGreaterThanNullID() {
        try {
            myQuery?.greaterThan(null, 2)
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    fun testLessThan() {
        myQuery?.lessThan("_id", "2")
        val lh = LinkedHashMap<String, Any>()
        lh["\$lt"] = "2"
        assertEquals(lh, myQuery?.queryFilterMap!!["_id"])
    }

    @Throws(IOException::class)
    fun testLessThanNullID() {
        try {
            myQuery?.lessThan(null, 2)
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    fun testGreaterThanEqualTo() {
        myQuery?.greaterThanEqualTo("_id", "345")
        val expected = LinkedHashMap<String, Any>()
        expected["\$gte"] = "345"
        assertEquals(expected, myQuery?.queryFilterMap!!["_id"])
    }

    @Throws(IOException::class)
    fun testGreaterThanEqualToNullID() {
        try {
            myQuery?.greaterThanEqualTo(null, 345)
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    fun testLessThanEqualTo() {
        myQuery?.lessThanEqualTo("_id", "123")
        val expected = LinkedHashMap<String, Any>()
        expected["\$lte"] = "123"
        assertEquals(expected, myQuery?.queryFilterMap!!["_id"])
    }

    @Throws(IOException::class)
    fun testLessThanEqualToNullID() {
        try {
            myQuery?.lessThanEqualTo(null, 123)
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    fun testNotEqual() {
        myQuery?.notEqual("_id", "newEntity")
        val expected = LinkedHashMap<String, Any>()
        expected["\$ne"] = "newEntity"
        assertEquals(expected, myQuery?.queryFilterMap!!["_id"])
    }

    @Throws(IOException::class)
    fun testNotEqualNullID() {
        try {
            myQuery?.notEqual(null, "newEntity")
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    fun testGreaterThanLessThan() {
        myQuery?.greaterThan("_id", "100")
        myQuery?.lessThan("_id", "1000")
        val expected = LinkedHashMap<String, Any>()
        expected["\$gt"] = "100"
        expected["\$lt"] = "1000"
        assertEquals(expected, myQuery?.queryFilterMap!!["_id"])
    }

    @Throws(IOException::class)
    fun testTwoKeys() {
        myQuery?.equals("city", "Boston")
        myQuery?.greaterThan("age", "21")
        myQuery?.lessThan("age", "30")
        val expected = LinkedHashMap<String, Any>()
        val expectedAge = LinkedHashMap<String, Any>()
        expected["city"] = "Boston"
        expectedAge["\$gt"] = "21"
        expectedAge["\$lt"] = "30"
        expected["age"] = expectedAge
        assertEquals(expected["city"], myQuery?.queryFilterMap!!["city"])
        assertEquals(expected["age"], myQuery?.queryFilterMap!!["age"])
    }

    fun testIn() {
        val myArray = arrayOf("1", "2", "3")
        val myExpectedArray = arrayOf("1", "2", "3")
        val expected = LinkedHashMap<String, Any>()
        expected["\$in"] = myExpectedArray
        myQuery?.`in`("numbers", myArray)
        assertArrayEquals(expected["\$in"] as Array<Any?>,
                (myQuery?.queryFilterMap!!["numbers"] as LinkedHashMap<String?, Any?>)["\$in"] as Array<Any?>)
    }

    fun testInNullField() {
        try {
            val myArray = arrayOf(1, 2, 3)
            myQuery?.`in`(null, myArray)
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    fun testNotIn() {
        val myArray = arrayOf<String?>("1", "2", "3")
        val myExpectedArray = arrayOf("1", "2", "3")
        val expected = LinkedHashMap<String, Any>()
        expected["\$nin"] = myExpectedArray
        myQuery?.notIn("numbers", myArray)
        assertArrayEquals(expected["\$nin"] as Array<Any?>,
                (myQuery?.queryFilterMap!!["numbers"] as LinkedHashMap<String?, Any?>)["\$nin"] as Array<Any?>)
    }

    fun testNotInNullField() {
        try {
            val myArray = arrayOf<Int?>(1, 2, 3)
            myQuery?.notIn(null, myArray)
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    fun testAll() {
        val myArray = arrayOf<String?>("1", "2", "3")
        val myExpectedArray = arrayOf("1", "2", "3")
        val expected = LinkedHashMap<String, Any>()
        expected["\$all"] = myExpectedArray
        myQuery?.all("numbers", myArray)
        assertArrayEquals(expected["\$all"] as Array<Any?>,
                (myQuery?.queryFilterMap!!["numbers"] as LinkedHashMap<String?, Any?>)["\$all"] as Array<Any?>)
    }

    fun testAllNullField() {
        try {
            val myArray = arrayOf<Int?>(1, 2, 3)
            myQuery?.all(null, myArray)
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    fun testSize() {
        myQuery?.size("_id", 3)
        val expected = LinkedHashMap<String, Any>()
        expected["\$size"] = 3
        assertEquals(expected, myQuery?.queryFilterMap!!["_id"])
    }

    fun setSizeNullKey() {
        try {
            myQuery?.size(null, 3)
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    fun testAnd() {
        val myQuery2 = Query(MongoQueryFilterBuilder())
        myQuery?.equals("City", "Boston")
        myQuery2.greaterThan("Age", "21")
        myQuery?.and(myQuery2)
        val city = LinkedHashMap<String, Any>()
        val age = LinkedHashMap<String, Any>()
        val ageWrapper = LinkedHashMap<String, Any>()
        city["City"] = "Boston"
        age["\$gt"] = "21"
        ageWrapper["Age"] = age
        val expected = arrayOfNulls<LinkedHashMap<String, Any>?>(2)
        expected[0] = city
        expected[1] = ageWrapper
        assertArrayEquals(expected, myQuery?.queryFilterMap!!["\$and"] as Array<Any?>)
    }

    fun testAndNullQuery() {
        try {
            myQuery?.and(null)
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    fun testOr() {
        val myQuery2 = Query(MongoQueryFilterBuilder())
        myQuery?.equals("City", "Boston")
        myQuery2.greaterThan("Age", "21")
        myQuery?.or(myQuery2)
        val city = LinkedHashMap<String, Any>()
        val age = LinkedHashMap<String, Any>()
        val ageWrapper = LinkedHashMap<String, Any>()
        city["City"] = "Boston"
        age["\$gt"] = "21"
        ageWrapper["Age"] = age
        val expected = arrayOfNulls<LinkedHashMap<String, Any>?>(2)
        expected[0] = city
        expected[1] = ageWrapper
        assertArrayEquals(expected, myQuery?.queryFilterMap!!["\$or"] as Array<Any?>)
    }

    fun testOrNullQuery() {
        try {
            myQuery?.or(null)
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    fun testNot() {
        myQuery?.equals("City", "Boston")
        myQuery?.greaterThan("Age", 21)
        myQuery?.lessThan("Age", 65)
        myQuery?.notEqual("Status", "deleted")
        myQuery?.not()
        val newMap = LinkedHashMap<String, LinkedHashMap<String, Any>>()
        val expected = LinkedHashMap<String, Any>()
        val city = LinkedHashMap<String, Any>()
        val age = LinkedHashMap<String, Any>()
        city["\$ne"] = "Boston"
        age["\$gt"] = 21
        age["\$lt"] = 65
        newMap["\$not"] = age
        expected["City"] = city
        expected["Age"] = newMap
        expected["Status"] = "deleted"
        assertEquals(expected, myQuery?.queryFilterMap)
    }

    fun testSort() {
        myQuery?.greaterThan("Age", "18")
        myQuery?.addSort("State", AbstractQuery.SortOrder.ASC)
        myQuery?.addSort("City", AbstractQuery.SortOrder.ASC)
        myQuery?.addSort("Age", AbstractQuery.SortOrder.DESC)
        val expected = "{\"State\" : 1,\"City\" : 1,\"Age\" : -1}"
        assertEquals(expected, myQuery!!.sortString)
    }

    fun testSortNullField() {
        myQuery?.greaterThan("Age", "18")
        try {
            myQuery?.addSort(null, AbstractQuery.SortOrder.ASC)
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    fun testSortNullOrder() {
        myQuery?.greaterThan("Age", "18")
        myQuery?.addSort("age", null)
        val actual = myQuery?.sortString
        val expected = "{\"age\" : 1}"
        assertEquals(expected, actual)
    }

    fun testSortDoubleNull() {
        myQuery?.greaterThan("Age", "18")
        try {
            myQuery?.addSort(null, null)
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    fun testNearSphere() {
        myQuery?.nearSphere("pointOfInterest", -71.056868, 42.360583)
        val expected = LinkedHashMap<String, Any>()
        val point = arrayOf(42.360583, -71.056868)
        expected["\$nearSphere"] = point
        val actual = myQuery?.queryFilterMap!!["pointOfInterest"] as LinkedHashMap<String, Any>
        assertEquals(expected.keys, actual.keys)
        assertArrayEquals(expected["\$nearSphere"] as Array<Double?>, actual["\$nearSphere"] as Array<Double?>)
    }

    fun testNearSphereWithDistance() {
        myQuery?.nearSphere("pointOfInterest", -71.056868, 42.360583, .05)
        val expected = LinkedHashMap<String, Any>()
        val point = arrayOf(42.360583, -71.056868)
        expected["\$nearSphere"] = point
        expected["\$maxDistance"] = .05
        val actual = myQuery?.queryFilterMap!!["pointOfInterest"] as LinkedHashMap<String, Any>
        assertEquals(expected.keys, actual.keys)
        assertArrayEquals(expected["\$nearSphere"] as Array<Double?>, actual["\$nearSphere"] as Array<Double?>)
    }

    fun testNearSphereHighLat() {
        try {
            myQuery?.nearSphere("pointOfInterest", 90.000000, -71.324324)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testNearSphereExceedsHighLat() {
        try {
            myQuery?.nearSphere("pointOfInterest", 90.000001, -71.324324)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testNearSphereLowLat() {
        try {
            myQuery?.nearSphere("pointOfInterest", -90.000000, -71.324324)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testNearSphereExceedsLowLat() {
        try {
            myQuery?.nearSphere("pointOfInterest", -90.000001, -71.324324)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testNearSphereHighLon() {
        try {
            myQuery?.nearSphere("pointOfInterest", 42.523232, 180.00000)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testNearSphereExceedsHighdLon() {
        try {
            myQuery?.nearSphere("pointOfInterest", 42.523232, 180.00001)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testNearSphereLowLon() {
        try {
            myQuery?.nearSphere("pointOfInterest", 42.523232, -180.00000)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testNearSphereExceedsLowLon() {
        try {
            myQuery?.nearSphere("pointOfInterest", 42.523232, -180.00001)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testNearSphereNullField() {
        try {
            myQuery?.nearSphere(null, 42.523234, -73.234234)
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    fun testWithinBox() {
        myQuery?.withinBox("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583)
        var within = LinkedHashMap<String, Any>()
        val inner = LinkedHashMap<String, Any>()
        val point = Array(2) { arrayOfNulls<Double?>(2) }
        point[0][0] = 42.360583
        point[0][1] = -71.056868
        point[1][0] = 40.360583
        point[1][1] = -70.544966
        inner["\$box"] = point
        within["\$within"] = inner
        var actual = myQuery?.queryFilterMap!!["pointOfInterest"] as LinkedHashMap<String, Any>
        actual = actual["\$within"] as LinkedHashMap<String, Any>
        within = within["\$within"] as LinkedHashMap<String, Any>
        assertEquals(within.keys, actual.keys)
        assertArrayEquals(within["\$box"] as Array<Array<Double?>?>, actual["\$box"] as Array<Array<Double?>?>)
    }

    fun testWithinBoxFieldNull() {
        try {
            myQuery?.withinBox(null, -71.056868, 42.360583, -70.544966, 40.360583)
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    fun testWithinBoxLatHighPoint1() {
        try {
            myQuery?.withinBox("pointOfInterest", 90.0000000, 42.360583, -70.544966, 40.360583)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinBoxLatExceedsHighPoint1() {
        try {
            myQuery?.withinBox("pointOfInterest", 90.0000001, 42.360583, -70.544966, 40.360583)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinBoxLatLowPoint1() {
        try {
            myQuery?.withinBox("pointOfInterest", -90.0000000, 42.360583, -70.544966, 40.360583)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinBoxLatExceedsLowPoint1() {
        try {
            myQuery?.withinBox("pointOfInterest", -90.0000001, 42.360583, -70.544966, 40.360583)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinBoxLonHighPoint1() {
        try {
            myQuery?.withinBox("pointOfInterest", 71.056868, 180.000000, -70.544966, 40.360583)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinBoxLonExceedsHighPoint1() {
        try {
            myQuery?.withinBox("pointOfInterest", 71.056868, 180.000001, -70.544966, 40.360583)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinBoxLonLowPoint1() {
        try {
            myQuery?.withinBox("pointOfInterest", 71.056868, -180.000000, -70.544966, 40.360583)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinBoxLonExceedsLowPoint1() {
        try {
            myQuery?.withinBox("pointOfInterest", 71.056868, -180.000001, -70.544966, 40.360583)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinBoxLatHighPoint2() {
        try {
            myQuery?.withinBox("pointOfInterest", -71.056868, 42.360583, 90.000000, 40.360583)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinBoxLatExceedsHighPoint2() {
        try {
            myQuery?.withinBox("pointOfInterest", -71.056868, 42.360583, 90.000001, 40.360583)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinBoxLatLowPoint2() {
        try {
            myQuery?.withinBox("pointOfInterest", -71.056868, 42.360583, -90.000000, 40.360583)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinBoxLatExceedsLowPoint2() {
        try {
            myQuery?.withinBox("pointOfInterest", -71.056868, 42.360583, -90.000001, 40.360583)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinBoxLonHighPoint2() {
        try {
            myQuery?.withinBox("pointOfInterest", 71.056868, -179.234234, -70.544966, 180.0000000)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinBoxLonExceedsHighPoint2() {
        try {
            myQuery?.withinBox("pointOfInterest", 71.056868, -179.234234, -70.544966, 180.0000001)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinBoxLonLowPoint2() {
        try {
            myQuery?.withinBox("pointOfInterest", 71.056868, -179.234234, -70.544966, -180.0000000)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinBoxLonExceedsLowPoint2() {
        try {
            myQuery?.withinBox("pointOfInterest", 71.056868, -179.234234, -70.544966, -180.0000001)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinPolygon() {
        myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                -72.523423, 43.234343, -68.423423, 38.234323)
        var within = LinkedHashMap<String, Any>()
        val inner = LinkedHashMap<String, Any>()
        val point = Array(4) { arrayOfNulls<Double?>(2) }
        point[0][0] = 42.360583
        point[0][1] = -71.056868
        point[1][0] = 40.360583
        point[1][1] = -70.544966
        point[2][0] = 43.234343
        point[2][1] = -72.523423
        point[3][0] = 38.234323
        point[3][1] = -68.423423
        inner["\$polygon"] = point
        within["\$within"] = inner
        var actual = myQuery?.queryFilterMap!!["pointOfInterest"] as LinkedHashMap<String, Any>
        assertEquals(within.keys, actual.keys)
        actual = actual["\$within"] as LinkedHashMap<String, Any>
        within = within["\$within"] as LinkedHashMap<String, Any>
        assertEquals(within.keys, actual.keys)
        assertArrayEquals(within["\$polygon"] as Array<Array<Double?>?>, actual["\$polygon"] as Array<Array<Double?>?>)
    }

    fun testWithinPolygonNullField() {
        try {
            myQuery?.withinPolygon(null, -71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323)
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    fun testWithinPolygonLatHighPoint1() {
        try {
            myQuery?.withinPolygon("pointOfInterest", 90.000000, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinPolygonLatExceedsHighPoint1() {
        try {
            myQuery?.withinPolygon("pointOfInterest", 90.000001, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinPolygonLatLowPoint1() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -90.000000, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinPolygonLatExceedsLowPoint1() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -90.000001, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinPolygonLonHighPoint1() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 180.000000, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinPolygonLonExceedsHighPoint1() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 180.000001, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinPolygonLonLowPoint1() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, -180.000000, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinPolygonLonExceedsLowPoint1() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, -180.000001, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinPolygonLatHighPoint2() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, 90.000000, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinPolygonLatExceedsHighPoint2() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, 90.0000001, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinPolygonLatLowPoint2() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -90.000000, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinPolygonLatExceedsLowPoint2() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -90.0000001, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinPolygonLonHighPoint2() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 180.000000,
                    -72.523423, 43.234343, -68.423423, 38.234323)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinPolygonLonExceedsHighPoint2() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 180.0000001,
                    -72.523423, 43.234343, -68.423423, 38.234323)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinPolygonLonLowPoint2() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, -180.000000,
                    -72.523423, 43.234343, -68.423423, 38.234323)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinPolygonLonExceedsLowPoint2() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, -180.0000001,
                    -72.523423, 43.234343, -68.423423, 38.234323)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinPolygonLatHighPoint3() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                    90.0000000, 43.234343, -68.423423, 38.234323)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinPolygonLatExceedsHighPoint3() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                    90.0000001, 43.234343, -68.423423, 38.234323)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinPolygonLatLowPoint3() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                    -90.0000000, 43.234343, -68.423423, 38.234323)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinPolygonLatExceedsLowPoint3() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                    -90.0000001, 43.234343, -68.423423, 38.234323)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinPolygonLonHighPoint3() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 180.0000000, -68.423423, 38.234323)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinPolygonLonExceedsHighPoint3() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 180.0000001, -68.423423, 38.234323)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinPolygonLonLowPoint3() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, -180.0000000, -68.423423, 38.234323)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinPolygonLonExceedsLowPoint3() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, -180.0000001, -68.423423, 38.234323)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinPolygonLatHighPoint4() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, 90.0000000, 38.234323)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinPolygonLatExceedsHighPoint4() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, 90.0000001, 38.234323)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinPolygonLatLowPoint4() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, -90.0000000, 38.234323)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinPolygonLatExceedsLowPoint4() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, -90.0000001, 38.234323)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinPolygonLonHighPoint4() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 38.234323, -68.423423, 180.0000000)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinPolygonLonExceedsHighPoint4() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 38.234323, -68.423423, 180.0000001)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testWithinPolygonLonLowPoint4() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 38.234323, -68.423423, -180.0000000)
        } catch (ex: IllegalArgumentException) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED)
        }
    }

    fun testWithinPolygonLonExceedsLowPoint4() {
        try {
            myQuery?.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 38.234323, -68.423423, -180.0000001)
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE)
        } catch (ex: IllegalArgumentException) {
        }
    }

    fun testRegEx() {
        myQuery?.regEx("email", "^.*\\b(gmail|yahoo|hotmail)\\b.*$")
        val expected = LinkedHashMap<String, Any>()
        expected["\$regex"] = "^.*\\b(gmail|yahoo|hotmail)\\b.*$"
        assertEquals(expected, myQuery?.queryFilterMap!!["email"])
    }

    fun testRegExNullKey() {
        try {
            myQuery?.regEx(null, "^.*\\b(gmail|yahoo|hotmail)\\b.*$")
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

    fun testStartsWith() {
        myQuery?.startsWith("firstname", "Jo")
        val expected = LinkedHashMap<String, Any>()
        expected["\$regex"] = "^Jo"
        assertEquals(expected, myQuery!!.queryFilterMap!!["firstname"])
    }

    fun testStartsWithNullKey() {
        try {
            myQuery?.startsWith(null, "Jo")
            fail(NULL_EXCEPTION_FAILURE)
        } catch (ex: NullPointerException) {
        }
    }

//    public void testEndsWith() {
//        myQuery.endsWith("lastname","oe");
//        LinkedHashMap<String, Object> expected = new LinkedHashMap<String,Object>();
//        expected.put("$regex","oe$");
//        assertEquals(expected,myQuery.getQueryFilterMap().get("lastname"));
//    }

//    public void testEndsWithNullKey() {
//        try {
//            myQuery.endsWith(null,"oe");
//            fail(NULL_EXCEPTION_FAILURE);
//        } catch (NullPointerException ex) {}
//    }

    companion object {
        private const val NULL_EXCEPTION_FAILURE = "NullPointerException should be thrown"
        private const val ILLEGAL_ARGUMENT_EXCEPTION_FAILURE = "IllegalArgumentException should be thrown"
        private const val ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED = "IllegalArgumentException should not be thrown"
    }
}