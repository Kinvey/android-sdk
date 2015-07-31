package com.kinvey.java.model;

import junit.framework.TestCase;

/**
 * Created by edward on 7/31/15.
 */
public class KinveyFileTest extends TestCase {

    public KinveyFile kf;

    public void testConstructor() {
        kf = new KinveyFile();
        assertEquals("KinveyRef", kf.get("_type"));
        assertNull(kf.getId());
        kf.setId("ok");
        assertEquals("ok", kf.getId());

        kf = new KinveyFile("id");
        assertEquals("id", kf.getId());




    }


}
