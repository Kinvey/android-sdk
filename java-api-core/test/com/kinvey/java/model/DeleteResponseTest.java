package com.kinvey.java.model;

import junit.framework.TestCase;

/**
 * Created by edward on 7/31/15.
 */
public class DeleteResponseTest extends TestCase{

    KinveyDeleteResponse kdr;

    public void testConstructor(){
        kdr = new KinveyDeleteResponse();
        assertEquals(0, kdr.getCount());
        kdr.setCount(1);
        assertEquals(1, kdr.getCount());
    }
}
