package com.kinvey.androidTest.cache;


import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.android.cache.ClassHash;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ClassHashTest {

    @Test
    public void testHashNotFail(){
        //Correct case
        ClassHash.getClassHash(new GenericJson(){
            @Key("_id")
            private String _id;
        }.getClass());

        //Correct case
        ClassHash.getClassHash(new GenericJson(){
            @Key("_id")
            protected  String _id;
        }.getClass());

        //Correct case
        ClassHash.getClassHash(new GenericJson(){
            @Key("_id")
            String _id;
        }.getClass());

        //Correct case Object should be skipped
        ClassHash.getClassHash(new GenericJson() {
            @Key("_id")
            Object _id;
        }.getClass());

        //Correct case Context should be skipped
        ClassHash.getClassHash(new GenericJson() {
            @Key("_id")
            Context _id;
        }.getClass());

        //Correct case field without annotation should be skipped
        ClassHash.getClassHash(new GenericJson() {
            String _id;
        }.getClass());


    }


    @Test
    public void testHashShouldMatch(){
        //Correct case
        assertEquals(
                ClassHash.getClassHash(new GenericJson() {
                    @Key("_id")
                    private String _id;
                }.getClass()),
                ClassHash.getClassHash(new GenericJson() {
                    @Key("_id")
                    protected String _id;
                    Object test;
                }.getClass())
        );

        assertNotEquals(
                ClassHash.getClassHash(new GenericJson() {
                    @Key("_id")
                    private String _id;
                }.getClass()),
                ClassHash.getClassHash(new GenericJson() {
                    @Key("_id")
                    protected String _id;
                    @Key("_test")
                    String test;
                }.getClass())
        );


    }

    @Test
    public void testAllowedFieldsCorrect(){
        Class<? extends GenericJson> c = new GenericJson() {
            @Key("_id")
            private String _id;
        }.getClass();
        assertTrue(
                ClassHash.getAllowedFields(c).containsKey("_id")
        );
        assertEquals(ClassHash.getAllowedFields(c).get("_id"), String.class);


        c = new GenericJson() {
            private Object test;
        }.getClass();
        assertTrue(
                ClassHash.getAllowedFields(c).containsKey("_id")
        );

        //id should be auto generated
        c = new GenericJson() {
            private Byte[] _id;
        }.getClass();
        assertTrue(
                ClassHash.getAllowedFields(c).containsKey("_id")
        );
        assertEquals(ClassHash.getAllowedFields(c).get("_id"), String.class);

        //id should be auto generated
        c = new GenericJson() {
            @Key("_id")
            private String _id;
            @Key("test")
            private String different;
        }.getClass();
        assertTrue(
                ClassHash.getAllowedFields(c).containsKey("test")
        );
        assertEquals(ClassHash.getAllowedFields(c).get("test"), String.class);

        //id should be auto generated
        c = new GenericJson() {
            @Key("_id")
            private String _id;
            private String different;
        }.getClass();
        assertTrue(
                !ClassHash.getAllowedFields(c).containsKey("test")
        );

    }



}
