package com.kinvey.java.model;

import com.kinvey.java.core.KinveyMockUnitTest;

/**
 * Created by edward on 7/31/15.
 */
public class AggregationTest extends KinveyMockUnitTest {

    public Aggregation agg;

    public void testConstruction(){
        agg = new Aggregation(new Aggregation.Result[1]);
        assertNotNull(agg.results);
        agg = new Aggregation(null);
        assertNotNull(agg.results);
        Aggregation.Result res = new Aggregation.Result();
        assertNull(res.result);
    }

    public void testResults(){
        Aggregation.Result res = new Aggregation.Result();
        res.result = 1;
        res.put("key", "value");


        agg = new Aggregation(new Aggregation.Result[]{res});
        assertEquals(1, agg.getResultsFor("key", "value").get(0));
        assertEquals(0, agg.getResultsFor("key", "novalue").size());
        assertEquals(0, agg.getResultsFor("nokey", "novalue").size());


    }


}
