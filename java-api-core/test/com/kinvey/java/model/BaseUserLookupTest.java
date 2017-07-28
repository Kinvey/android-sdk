package com.kinvey.java.model;

import junit.framework.TestCase;

/**
 * Created by edward on 7/31/15.
 */
public class BaseUserLookupTest extends TestCase {

    public UserLookup ul;

    public void testConstructor() {
        ul = new UserLookup();
        assertNull(ul.getEmail());
    }

    public void testProperties(){
        ul = new UserLookup();
        ul.setId("id");
        assertEquals("id", ul.getId());
        ul.setEmail("email");
        assertEquals("email", ul.getEmail());
        ul.setFirstName("first");
        assertEquals("first", ul.getFirstName());
        ul.setLastName("last");
        assertEquals("last", ul.getLastName());
        ul.setFacebookID("facebook");
        assertEquals("facebook", ul.getFacebookID());
        ul.setUsername("username");
        assertEquals("username", ul.getUsername());

    }



}
