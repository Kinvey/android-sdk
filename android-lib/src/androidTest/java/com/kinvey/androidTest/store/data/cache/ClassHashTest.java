package com.kinvey.androidTest.store.data.cache;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.android.Client;
import com.kinvey.android.cache.ClassHash;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmSchema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ClassHashTest {

    Context context;

    @Before
    public void setup(){
        context = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        Realm.init(context);
    }

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
    public void testInnerObjects(){
        RealmConfiguration rc = new RealmConfiguration.Builder()
                .name("test_inner")
                .build();
        DynamicRealm realm = DynamicRealm.getInstance(rc);

        realm.beginTransaction();
        try {
            ClassHash.createScheme("sample", realm, SampleGsonWithInner.class);
        } catch (Exception e){
            realm.commitTransaction();
        }

        RealmSchema schema = realm.getSchema();

        assertNotNull(schema);
    }




}