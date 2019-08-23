package com.kinvey.java.model;


import com.kinvey.java.Query;
import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * Created by edward on 7/31/15.
 */
public class AggregateEntityTest extends TestCase {

    AggregateEntity ae;

    public void testConstructor(){
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("key");
        ae = new AggregateEntity(fields, AggregateType.COUNT, "field", new Query(), null);


//        public AggregateEntity(ArrayList<String> fields, AggregateType type, String aggregateField, Query query,
//                AbstractKinveyJsonClient client) {

        assertEquals(true, (boolean)ae.getKey().get("key"));
        assertEquals(0, ae.getInitial().get("_result"));
        assertEquals("function(doc,out){ out._result++;}", ae.getReduce());
        assertNotNull(ae.getCondition());

        try {
            ae = new AggregateEntity(fields, null, "field", new Query(), null);
            fail("should have errored out on null!");
        }catch(Exception e){}


    }

    public void testEnums(){
        assertEquals(AggregateType.COUNT, AggregateType.valueOf("COUNT"));
    }
}
