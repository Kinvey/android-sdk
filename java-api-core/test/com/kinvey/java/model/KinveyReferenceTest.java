package com.kinvey.java.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.Key;
import com.kinvey.java.core.KinveyMockUnitTest;
import junit.framework.Test;
import sun.net.www.content.text.Generic;

/**
 * Created by edward on 7/31/15.
 */
public class KinveyReferenceTest extends KinveyMockUnitTest {

    KinveyReference ref;

    public void testInit(){
        this.ref = new KinveyReference();
        assertEquals(this.ref.getType(), "KinveyRef");
        assertEquals(this.ref.getId(), null);
        assertEquals(this.ref.getCollection(), null);

        this.ref = new KinveyReference("collection", "id");
        assertEquals(this.ref.getType(), "KinveyRef");
        assertEquals(this.ref.getId(), "id");
        assertEquals(this.ref.getCollection(), "collection");
    }

    public void testSetters(){
        this.ref = new KinveyReference();
        this.ref.setCollection("collection");
        this.ref.setId("id");
        this.ref.setType("");
        assertEquals(this.ref.getType(), "KinveyRef");
        assertEquals(this.ref.getId(), "id");
        assertEquals(this.ref.getCollection(), "collection");
    }

    public void testResolvedObject(){
        this.ref = new KinveyReference();
        assertNull(this.ref.getResolvedObject());

        ArrayMap some = new ArrayMap();
        some.put("hello", "hello");
        this.ref.put("_obj", some);

        assertEquals(this.ref.getResolvedObject().get("hello"), "hello" );
    }

    public void testTypedObject(){
        this.ref = new KinveyReference();
        assertNull(this.ref.getTypedObject(refTest.class));

        ArrayMap some = new ArrayMap();
        some.put("Hello", "hi");
        this.ref.put("_obj", some);

        refTest check = this.ref.getTypedObject(refTest.class);

        ArrayMap direct = (ArrayMap) this.ref.get("_obj");
        assertNotNull(direct);
        try {
            refTest ok = refTest.class.newInstance();
        }catch (Exception e){
            fail(e.getMessage());
        }
        assertEquals(check.Hello, "hi");
    }

    public static class refTest extends GenericJson{

        @Key
        public String Hello;

        public refTest(String hello){this.Hello = hello;}
        public refTest(){}
    }
}
