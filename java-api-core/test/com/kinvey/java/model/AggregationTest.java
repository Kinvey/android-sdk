package com.kinvey.java.model;

import com.kinvey.java.core.KinveyMockUnitTest;

import java.util.ArrayList;

/**
 * Created by edward on 7/31/15.
 */
public class AggregationTest extends KinveyMockUnitTest {

    public Aggregation agg;

    public void testConstruction(){
        agg = new Aggregation(new ArrayList<Aggregation.Result>());
        assertNotNull(agg.results);
        agg = new Aggregation(null);
        assertNotNull(agg.results);
        Aggregation.Result res = new Aggregation.Result();
        assertNull(res.result);
    }

    public void testResults(){
        Aggregation.Result res = new Aggregation.Result();
        res.result = 1D;
        res.put("key", "value");

        ArrayList<Aggregation.Result> list = new ArrayList<Aggregation.Result>();
        list.add(res);
        agg = new Aggregation(list);
        assertEquals(1D, agg.getResultsFor("key", "value").get(0));
        assertEquals(0, agg.getResultsFor("key", "novalue").size());
        assertEquals(0, agg.getResultsFor("nokey", "novalue").size());


    }


}
