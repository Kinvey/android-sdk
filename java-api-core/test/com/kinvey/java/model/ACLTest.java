package com.kinvey.java.model;

import com.kinvey.java.core.KinveyMockUnitTest;

import java.util.ArrayList;

/**
 * Created by edward on 7/31/15.
 */
public class ACLTest extends KinveyMockUnitTest {

    private KinveyMetaData kmd;
    private KinveyMetaData.AccessControlList acl;

    public void testConstructor(){
        kmd = new KinveyMetaData();
        assertEquals(null, kmd.getEntityCreationTime());
        acl = new KinveyMetaData.AccessControlList();
        assertEquals(null, acl.getCreator());
    }

    public void testKMDFields(){
        kmd = new KinveyMetaData();
        kmd.put("ect", "yesterday");
        assertEquals("yesterday", kmd.getEntityCreationTime());
        kmd.put("lmt", "today");
        assertEquals("today", kmd.getLastModifiedTime());
    }

    public void testACLFields(){
        acl = new KinveyMetaData.AccessControlList();
        acl.setGloballyReadable(true);
        assertTrue(acl.isGloballyReadable());
        acl.setGloballyWritable(true);
        assertTrue(acl.isGloballyReadable());
        acl.setCreator("creator");
        assertEquals("creator", acl.getCreator());

//        ArrayList<String> readers = new ArrayList<String>();
//        readers.add("reader");
//        acl.setRead(readers);
//        assertEquals(1, acl.getRead().size());
//        assertEquals("reader", acl.getRead().get(0));
//
//        ArrayList<String> writers = new ArrayList<String>();
//        writers.add("writer");
//        acl.setWrite(writers);
//        assertEquals(1, acl.getWrite().size());
//        assertEquals("writer", acl.getWrite().get(0));
    }

//    public void testACLGroup(){
//        acl = new KinveyMetaData.AccessControlList();
//        KinveyMetaData.AccessControlList.AclGroups gr = new KinveyMetaData.AccessControlList.AclGroups();
//        gr.setRead("read");
//        gr.setWrite("write");
//
//        ArrayList groups = new ArrayList();
//        groups.add(gr);
//
//        acl.setGroups(groups);
//        assertEquals("read", acl.getGroups().get(0).getRead());
//        assertEquals("write", acl.getGroups().get(0).getWrite());
//
//    }
}
