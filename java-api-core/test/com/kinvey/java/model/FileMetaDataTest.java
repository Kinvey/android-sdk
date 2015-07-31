package com.kinvey.java.model;

import com.kinvey.java.core.KinveyMockUnitTest;

/**
 * Created by edward on 7/31/15.
 */
public class FileMetaDataTest extends KinveyMockUnitTest {

    public FileMetaData fdm;

    public void testConstructor(){
        fdm = new FileMetaData();
        assertEquals(null, fdm.getId());
        fdm = new FileMetaData("id");
        assertEquals("id" ,fdm.getId());
    }

    public void testUploadDownloadURL(){
        fdm = new FileMetaData();
        fdm.setDownloadURL("download");
        assertEquals("download", fdm.getDownloadURL());
        fdm.setUploadUrl("upload");
        assertEquals("upload", fdm.getUploadUrl());
    }

    public void testOtherFields(){
        fdm = new FileMetaData();
        fdm.setFileName("myfile");
        assertEquals("myfile", fdm.getFileName());
        fdm.setPublic(true);
        assertEquals(true, fdm.isPublic());
        KinveyMetaData.AccessControlList acl = new KinveyMetaData.AccessControlList();
        acl.setCreator("123");
        fdm.setAcl(acl);
        assertEquals("123", fdm.getAcl().getCreator());
        fdm.setMimetype("mime");
        assertEquals("mime", fdm.getMimetype());
        fdm.setFileName("name");
        assertEquals("name", fdm.getFileName());
        fdm.setSize(100);
        assertEquals(100, fdm.getSize());
    }


}
