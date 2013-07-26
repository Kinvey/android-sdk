/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java;

import junit.framework.TestCase;
import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.util.LinkedHashMap;

import com.kinvey.java.Query;
import com.kinvey.java.query.MongoQueryFilter.MongoQueryFilterBuilder;

/**
 * @author mjsalinger
 * @since 2.0
 */

public class QueryTest extends TestCase {
    private Query myQuery;

    private static final String NULL_EXCEPTION_FAILURE = "NullPointerException should be thrown";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_FAILURE = "IllegalArgumentException should be thrown";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED = "IllegalArgumentException should not be thrown";


    @Override
    protected void setUp() {
        myQuery = new Query(new MongoQueryFilterBuilder());
    }

    public void testGetJsonWithNullFactory() {
        try {
            myQuery.equals("_id","newEntity");
            myQuery.getQueryFilterJson(null);
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testEquals() throws IOException {
        myQuery.equals("_id","newEntity");
        assertEquals("newEntity", myQuery.getQueryFilterMap().get("_id"));
    }

    public void testEqualsNullField() throws IOException {
        try {
            myQuery.equals(null,"newEntity");
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testGreaterThan() throws IOException {
        myQuery.greaterThan("_id","2");
        LinkedHashMap<String, Object> lh = new LinkedHashMap<String,Object>();
        lh.put("$gt","2");
        assertEquals(lh, myQuery.getQueryFilterMap().get("_id"));
    }

    public void testGreaterThanNullID() throws IOException {
        try {
            myQuery.greaterThan(null,2);
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testLessThan() throws IOException {
        myQuery.lessThan("_id","2");
        LinkedHashMap<String, Object> lh = new LinkedHashMap<String,Object>();
        lh.put("$lt", "2");
        assertEquals(lh, myQuery.getQueryFilterMap().get("_id"));
    }

    public void testLessThanNullID() throws IOException {
        try {
            myQuery.lessThan(null,2);
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testGreaterThanEqualTo() throws IOException {
        myQuery.greaterThanEqualTo("_id","345");
        LinkedHashMap<String, Object> expected = new LinkedHashMap<String,Object>();
        expected.put("$gte", "345");
        assertEquals(expected, myQuery.getQueryFilterMap().get("_id"));
    }

    public void testGreaterThanEqualToNullID() throws IOException {
        try {
            myQuery.greaterThanEqualTo(null,345);
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testLessThanEqualTo() throws IOException {
        myQuery.lessThanEqualTo("_id", "123");
        LinkedHashMap<String, Object> expected = new LinkedHashMap<String,Object>();
        expected.put("$lte","123");
        assertEquals(expected, myQuery.getQueryFilterMap().get("_id"));
    }

    public void testLessThanEqualToNullID() throws IOException {
        try {
            myQuery.lessThanEqualTo(null,123);
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testNotEqual() throws IOException {
        myQuery.notEqual("_id", "newEntity");
        LinkedHashMap<String, Object> expected = new LinkedHashMap<String,Object>();
        expected.put("$ne","newEntity");
        assertEquals(expected, myQuery.getQueryFilterMap().get("_id"));
    }

    public void testNotEqualNullID() throws IOException {
        try {
            myQuery.notEqual(null,"newEntity");
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testGreaterThanLessThan() {
        myQuery.greaterThan("_id","100");
        myQuery.lessThan("_id","1000");
        LinkedHashMap<String, Object> expected = new LinkedHashMap<String,Object>();
        expected.put("$gt","100");
        expected.put("$lt","1000");
        assertEquals(expected, myQuery.getQueryFilterMap().get("_id"));
    }

    public void testTwoKeys() throws IOException {
        myQuery.equals("city","Boston");
        myQuery.greaterThan("age","21");
        myQuery.lessThan("age","30");
        LinkedHashMap<String, Object> expected = new LinkedHashMap<String,Object>();
        LinkedHashMap<String,Object> expectedAge = new LinkedHashMap<String, Object>();
        expected.put("city","Boston");
        expectedAge.put("$gt","21");
        expectedAge.put("$lt","30");
        expected.put("age", expectedAge);
        assertEquals(expected.get("city"), myQuery.getQueryFilterMap().get("city"));
        assertEquals(expected.get("age"), myQuery.getQueryFilterMap().get("age"));

    }

    public void testIn() {
        String[] myArray = new String[] {"1","2","3"};
        String[] myExpectedArray = new String[] {"1","2","3"};
        LinkedHashMap<String, Object> expected = new LinkedHashMap<String,Object>();
        expected.put("$in",myExpectedArray);
        myQuery.in("numbers",myArray);
        assertArrayEquals((Object[]) (expected.get("$in")),
                (Object[]) ((LinkedHashMap<String, Object>) myQuery.getQueryFilterMap().get("numbers")).get("$in"));
    }

    public void testInNullField() {
        try {
            Integer[] myArray = new Integer[] {1,2,3};
            myQuery.in(null,myArray);
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testNotIn() {
        String[] myArray = new String[] {"1","2","3"};
        String[] myExpectedArray = new String[] {"1","2","3"};
        LinkedHashMap<String, Object> expected = new LinkedHashMap<String,Object>();
        expected.put("$nin",myExpectedArray);
        myQuery.notIn("numbers",myArray);
        assertArrayEquals((Object[]) (expected.get("$nin")),
                (Object[]) ((LinkedHashMap<String, Object>) myQuery.getQueryFilterMap().get("numbers")).get("$nin"));
    }

    public void testNotInNullField() {
        try {
            Integer[] myArray = new Integer[] {1,2,3};
            myQuery.notIn(null,myArray);
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testAll() {
        String[] myArray = new String[] {"1","2","3"};
        String[] myExpectedArray = new String[] {"1","2","3"};
        LinkedHashMap<String, Object> expected = new LinkedHashMap<String,Object>();
        expected.put("$all",myExpectedArray);
        myQuery.all("numbers",myArray);
        assertArrayEquals((Object[]) (expected.get("$all")),
                (Object[]) ((LinkedHashMap<String, Object>) myQuery.getQueryFilterMap().get("numbers")).get("$all"));
    }

    public void testAllNullField() {
        try {
            Integer[] myArray = new Integer[] {1,2,3};
            myQuery.all(null,myArray);
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testSize() {
        myQuery.size("_id",3);
        LinkedHashMap<String, Object> expected = new LinkedHashMap<String,Object>();
        expected.put("$size",3);
        assertEquals(expected, myQuery.getQueryFilterMap().get("_id"));
    }

    public void setSizeNullKey() {
        try {
            myQuery.size(null,3);
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testAnd() {
        Query myQuery2 = new Query(new MongoQueryFilterBuilder());
        myQuery.equals("City", "Boston");
        myQuery2.greaterThan("Age", "21");
        myQuery.and(myQuery2);
        LinkedHashMap<String,Object> city = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> age = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> ageWrapper = new LinkedHashMap<String, Object>();
        city.put("City","Boston");
        age.put("$gt", "21");
        ageWrapper.put("Age",age);
        LinkedHashMap<String, Object>[] expected = new LinkedHashMap[2];
        expected[0] = city;
        expected[1] = ageWrapper;
        assertArrayEquals(expected, (Object[]) myQuery.getQueryFilterMap().get("$and"));
    }

    public void testAndNullQuery() {
        try {
            myQuery.and(null);
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testOr() {
        Query myQuery2 = new Query(new MongoQueryFilterBuilder());
        myQuery.equals("City", "Boston");
        myQuery2.greaterThan("Age", "21");
        myQuery.or(myQuery2);
        LinkedHashMap<String,Object> city = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> age = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> ageWrapper = new LinkedHashMap<String, Object>();
        city.put("City","Boston");
        age.put("$gt", "21");
        ageWrapper.put("Age",age);
        LinkedHashMap<String, Object>[] expected = new LinkedHashMap[2];
        expected[0] = city;
        expected[1] = ageWrapper;
        assertArrayEquals(expected, (Object[]) myQuery.getQueryFilterMap().get("$or"));
    }

    public void testOrNullQuery() {
        try {
            myQuery.or(null);
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testNot() {
        myQuery.equals("City","Boston");
        myQuery.greaterThan("Age", 21);
        myQuery.lessThan("Age", 65);
        myQuery.notEqual("Status","deleted");
        myQuery.not();
        LinkedHashMap<String,Object> expected = new LinkedHashMap<String,Object>();
        LinkedHashMap<String,Object> city = new LinkedHashMap<String,Object>();
        LinkedHashMap<String,Object> age = new LinkedHashMap<String,Object>();

        city.put("$ne","Boston");
        age.put("$lt",21);
        age.put("$gt",65);

        expected.put("City",city);
        expected.put("Age", age);
        expected.put("Status","deleted");
        assertEquals(expected, myQuery.getQueryFilterMap());
    }

    public void testSort() {
        myQuery.greaterThan("Age","18");
        myQuery.addSort("State", Query.SortOrder.ASC);
        myQuery.addSort("City", Query.SortOrder.ASC);
        myQuery.addSort("Age", Query.SortOrder.DESC);
        String expected = "{\"State\" : 1,\"City\" : 1,\"Age\" : -1}";
        assertEquals(expected,myQuery.getSortString());
    }

    public void testSortNullField() {
        myQuery.greaterThan("Age","18");
        try {
            myQuery.addSort(null,Query.SortOrder.ASC);
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testSortNullOrder() {
        myQuery.greaterThan("Age","18");
        myQuery.addSort("age", null);
        String actual = myQuery.getSortString();
        String expected = "{\"age\" : 1}";
        assertEquals(expected,actual);
    }

    public void testSortDoubleNull() {
        myQuery.greaterThan("Age","18");
        try {
            myQuery.addSort(null,null);
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testNearSphere() {
        myQuery.nearSphere("pointOfInterest", -71.056868,42.360583);
        LinkedHashMap<String,Object> expected = new LinkedHashMap<String, Object>();
        Double[] point = new Double[] {42.360583,-71.056868};
        expected.put("$nearSphere",point);
        LinkedHashMap<String,Object> actual =
                (LinkedHashMap<String,Object>) myQuery.getQueryFilterMap().get("pointOfInterest");
        assertEquals(expected.keySet(),actual.keySet());
        assertArrayEquals((Double[]) expected.get("$nearSphere"), (Double[]) actual.get("$nearSphere"));
    }

    public void testNearSphereWithDistance() {
        myQuery.nearSphere("pointOfInterest", -71.056868,42.360583,.05);
        LinkedHashMap<String,Object> expected = new LinkedHashMap<String, Object>();
        Double[] point = new Double[] {42.360583,-71.056868};
        expected.put("$nearSphere",point);
        expected.put("$maxDistance", .05);
        LinkedHashMap<String,Object> actual =
                (LinkedHashMap<String,Object>) myQuery.getQueryFilterMap().get("pointOfInterest");
        assertEquals(expected.keySet(),actual.keySet());
        assertArrayEquals((Double[]) expected.get("$nearSphere"), (Double[]) actual.get("$nearSphere"));
    }

    public void testNearSphereHighLat() {
        try {
            myQuery.nearSphere("pointOfInterest",90.000000,-71.324324);
        } catch (IllegalArgumentException ex) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testNearSphereExceedsHighLat() {
        try {
            myQuery.nearSphere("pointOfInterest",90.000001,-71.324324);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex) {}
    }

    public void testNearSphereLowLat() {
        try {
            myQuery.nearSphere("pointOfInterest",-90.000000,-71.324324);
        } catch (IllegalArgumentException ex) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testNearSphereExceedsLowLat() {
        try {
            myQuery.nearSphere("pointOfInterest",-90.000001,-71.324324);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex) {}
    }

    public void testNearSphereHighLon() {
        try {
            myQuery.nearSphere("pointOfInterest",42.523232,180.00000);
        } catch (IllegalArgumentException ex) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testNearSphereExceedsHighdLon() {
        try {
            myQuery.nearSphere("pointOfInterest",42.523232,180.00001);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex) {}
    }

    public void testNearSphereLowLon() {
        try {
            myQuery.nearSphere("pointOfInterest",42.523232,-180.00000);
        } catch (IllegalArgumentException ex) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testNearSphereExceedsLowLon() {
        try {
            myQuery.nearSphere("pointOfInterest",42.523232,-180.00001);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex) {}
    }

    public void testNearSphereNullField() {
        try {
            myQuery.nearSphere(null,42.523234,-73.234234);
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testWithinBox() {
        myQuery.withinBox("pointOfInterest", -71.056868,42.360583, -70.544966,40.360583);
        LinkedHashMap<String,Object> within = new LinkedHashMap<String, Object>();
        LinkedHashMap<String,Object> inner = new LinkedHashMap<String, Object>();
        Double[][] point = new Double[2][2];
        point[0][0] = 42.360583;
        point[0][1] = -71.056868;
        point[1][0] = 40.360583;
        point[1][1] = -70.544966;
        inner.put("$box",point);
        within.put("$within",inner);
        LinkedHashMap<String,Object> actual =
                (LinkedHashMap<String,Object>) myQuery.getQueryFilterMap().get("pointOfInterest");
        actual = (LinkedHashMap<String, Object>) actual.get("$within");
        within = (LinkedHashMap<String, Object>) within.get("$within");
        assertEquals(within.keySet(), actual.keySet());
        assertArrayEquals((Double[][]) within.get("$box"), (Double[][]) actual.get("$box"));
    }

    public void testWithinBoxFieldNull() {
        try {
            myQuery.withinBox(null,-71.056868,42.360583, -70.544966,40.360583);
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testWithinBoxLatHighPoint1() {
        try {
            myQuery.withinBox("pointOfInterest",90.0000000,42.360583, -70.544966,40.360583);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinBoxLatExceedsHighPoint1() {
        try {
            myQuery.withinBox("pointOfInterest",90.0000001,42.360583, -70.544966,40.360583);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinBoxLatLowPoint1() {
        try {
            myQuery.withinBox("pointOfInterest",-90.0000000,42.360583, -70.544966,40.360583);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinBoxLatExceedsLowPoint1() {
        try {
            myQuery.withinBox("pointOfInterest",-90.0000001,42.360583, -70.544966,40.360583);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinBoxLonHighPoint1() {
        try {
            myQuery.withinBox("pointOfInterest",71.056868,180.000000, -70.544966,40.360583);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinBoxLonExceedsHighPoint1() {
        try {
            myQuery.withinBox("pointOfInterest",71.056868,180.000001, -70.544966,40.360583);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinBoxLonLowPoint1() {
        try {
            myQuery.withinBox("pointOfInterest",71.056868,-180.000000, -70.544966,40.360583);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinBoxLonExceedsLowPoint1() {
        try {
            myQuery.withinBox("pointOfInterest",71.056868,-180.000001, -70.544966,40.360583);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinBoxLatHighPoint2() {
        try {
            myQuery.withinBox("pointOfInterest",-71.056868,42.360583, 90.000000,40.360583);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinBoxLatExceedsHighPoint2() {
        try {
            myQuery.withinBox("pointOfInterest",-71.056868,42.360583, 90.000001 ,40.360583);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinBoxLatLowPoint2() {
        try {
            myQuery.withinBox("pointOfInterest",-71.056868,42.360583, -90.000000,40.360583);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinBoxLatExceedsLowPoint2() {
        try {
            myQuery.withinBox("pointOfInterest",-71.056868,42.360583, -90.000001 ,40.360583);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinBoxLonHighPoint2() {
        try {
            myQuery.withinBox("pointOfInterest",71.056868,-179.234234, -70.544966,180.0000000);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinBoxLonExceedsHighPoint2() {
        try {
            myQuery.withinBox("pointOfInterest",71.056868,-179.234234, -70.544966,180.0000001);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinBoxLonLowPoint2() {
        try {
            myQuery.withinBox("pointOfInterest",71.056868,-179.234234, -70.544966,-180.0000000);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinBoxLonExceedsLowPoint2() {
        try {
            myQuery.withinBox("pointOfInterest",71.056868,-179.234234, -70.544966,-180.0000001);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinPolygon() {
        myQuery.withinPolygon("pointOfInterest", -71.056868, 42.360583, -70.544966, 40.360583,
                -72.523423, 43.234343, -68.423423, 38.234323);
        LinkedHashMap<String,Object> within = new LinkedHashMap<String, Object>();
        LinkedHashMap<String,Object> inner = new LinkedHashMap<String, Object>();
        Double[][] point = new Double[4][2];
        point[0][0] = 42.360583;
        point[0][1] = -71.056868;
        point[1][0] = 40.360583;
        point[1][1] = -70.544966;
        point[2][0] = 43.234343;
        point[2][1] = -72.523423;
        point[3][0] = 38.234323;
        point[3][1] = -68.423423;
        inner.put("$polygon",point);
        within.put("$within",inner);
        LinkedHashMap<String,Object> actual =
                (LinkedHashMap<String,Object>) myQuery.getQueryFilterMap().get("pointOfInterest");
        assertEquals(within.keySet(),actual.keySet());
        actual = (LinkedHashMap<String, Object>) actual.get("$within");
        within = (LinkedHashMap<String, Object>) within.get("$within");
        assertEquals(within.keySet(), actual.keySet());
        assertArrayEquals((Double[][]) within.get("$polygon"), (Double[][]) actual.get("$polygon"));
    }

    public void testWithinPolygonNullField() {
        try {
            myQuery.withinPolygon(null,-71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323);
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testWithinPolygonLatHighPoint1() {
        try {
            myQuery.withinPolygon("pointOfInterest",90.000000, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinPolygonLatExceedsHighPoint1() {
        try {
            myQuery.withinPolygon("pointOfInterest",90.000001, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinPolygonLatLowPoint1() {
        try {
            myQuery.withinPolygon("pointOfInterest",-90.000000, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinPolygonLatExceedsLowPoint1() {
        try {
            myQuery.withinPolygon("pointOfInterest",-90.000001, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinPolygonLonHighPoint1() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 180.000000, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinPolygonLonExceedsHighPoint1() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 180.000001, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinPolygonLonLowPoint1() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, -180.000000, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinPolygonLonExceedsLowPoint1() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, -180.000001, -70.544966, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinPolygonLatHighPoint2() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, 90.000000, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinPolygonLatExceedsHighPoint2() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, 90.0000001, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinPolygonLatLowPoint2() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -90.000000, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinPolygonLatExceedsLowPoint2() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -90.0000001, 40.360583,
                    -72.523423, 43.234343, -68.423423, 38.234323);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinPolygonLonHighPoint2() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 180.000000,
                    -72.523423, 43.234343, -68.423423, 38.234323);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinPolygonLonExceedsHighPoint2() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 180.0000001,
                    -72.523423, 43.234343, -68.423423, 38.234323);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinPolygonLonLowPoint2() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, -180.000000,
                    -72.523423, 43.234343, -68.423423, 38.234323);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinPolygonLonExceedsLowPoint2() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, -180.0000001,
                    -72.523423, 43.234343, -68.423423, 38.234323);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinPolygonLatHighPoint3() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 40.360583,
                    90.0000000, 43.234343, -68.423423, 38.234323);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinPolygonLatExceedsHighPoint3() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 40.360583,
                    90.0000001, 43.234343, -68.423423, 38.234323);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinPolygonLatLowPoint3() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 40.360583,
                    -90.0000000, 43.234343, -68.423423, 38.234323);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinPolygonLatExceedsLowPoint3() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 40.360583,
                    -90.0000001, 43.234343, -68.423423, 38.234323);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinPolygonLonHighPoint3() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 180.0000000, -68.423423, 38.234323);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinPolygonLonExceedsHighPoint3() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 180.0000001, -68.423423, 38.234323);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinPolygonLonLowPoint3() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, -180.0000000, -68.423423, 38.234323);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinPolygonLonExceedsLowPoint3() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, -180.0000001, -68.423423, 38.234323);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinPolygonLatHighPoint4() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, 90.0000000, 38.234323);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinPolygonLatExceedsHighPoint4() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, 90.0000001, 38.234323);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinPolygonLatLowPoint4() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, -90.0000000, 38.234323);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinPolygonLatExceedsLowPoint4() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 43.234343, -90.0000001, 38.234323);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinPolygonLonHighPoint4() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 38.234323, -68.423423, 180.0000000);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinPolygonLonExceedsHighPoint4() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 38.234323, -68.423423, 180.0000001);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testWithinPolygonLonLowPoint4() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 38.234323, -68.423423, -180.0000000);
        } catch (IllegalArgumentException ex ) {
            fail(ILLEGAL_ARGUMENT_EXCEPTION_UNEXPECTED);
        }
    }

    public void testWithinPolygonLonExceedsLowPoint4() {
        try {
            myQuery.withinPolygon("pointOfInterest",-71.056868, 42.360583, -70.544966, 40.360583,
                    -72.523423, 38.234323, -68.423423, -180.0000001);
            fail(ILLEGAL_ARGUMENT_EXCEPTION_FAILURE);
        } catch (IllegalArgumentException ex ) {}
    }

    public void testRegEx() {
        myQuery.regEx("email","^.*\\b(gmail|yahoo|hotmail)\\b.*$");
        LinkedHashMap<String, Object> expected = new LinkedHashMap<String,Object>();
        expected.put("$regex","^.*\\b(gmail|yahoo|hotmail)\\b.*$");
        assertEquals(expected,myQuery.getQueryFilterMap().get("email"));
    }

    public void testRegExNullKey() {
        try {
            myQuery.regEx(null,"^.*\\b(gmail|yahoo|hotmail)\\b.*$");
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testStartsWith() {
        myQuery.startsWith("firstname","Jo");
        LinkedHashMap<String, Object> expected = new LinkedHashMap<String,Object>();
        expected.put("$regex","^Jo");
        assertEquals(expected,myQuery.getQueryFilterMap().get("firstname"));
    }

    public void testStartsWithNullKey() {
        try {
            myQuery.startsWith(null,"Jo");
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }

    public void testEndsWith() {
        myQuery.endsWith("lastname","oe");
        LinkedHashMap<String, Object> expected = new LinkedHashMap<String,Object>();
        expected.put("$regex","oe$");
        assertEquals(expected,myQuery.getQueryFilterMap().get("lastname"));
    }

    public void testEndsWithNullKey() {
        try {
            myQuery.endsWith(null,"oe");
            fail(NULL_EXCEPTION_FAILURE);
        } catch (NullPointerException ex) {}
    }
}
