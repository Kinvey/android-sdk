package com.kinvey.java.annotations;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Prots on 3/11/16.
 */
public class AnnotationProcessingTest extends TestCase {


    @Test
    public void testInlineObjects(){
        GenericJson genericJson = ReferenceHelper.processReferences(new GenericJson() {
            @Key("test")
            @KinveyReference(collection = "test", itemClass = SampleGson.class)
            GenericJson innerObject = new SampleGson("test");
        }, new ReferenceHelper.ReferenceListener() {
            @Override
            public String onUnsavedReferenceFound(String collection, GenericJson object) {
                return "test";
            }
        });

        assertNotNull(genericJson.get("test"));
        assertTrue(Map.class.isAssignableFrom(genericJson.getClass()));
        Map reference = (GenericJson)genericJson.get("test");

        assertNotNull(reference.get("_id"));
        assertEquals(reference.get("_id"), "test");
        assertEquals(reference.get("_collection"), "test");
    }


    @Test
    public void testInlineCollection(){

        final List<SampleGson> inner = new ArrayList<SampleGson>();
        for (int i = 0 ; i < 10; i++){
            inner.add(new SampleGson(String.valueOf(i)));
        }

        GenericJson genericJson = ReferenceHelper.processReferences(new GenericJson() {
            @Key("test")
            @KinveyReference(collection = "test", itemClass = SampleGson.class)
            List<SampleGson> innerObject = inner;
        }, new ReferenceHelper.ReferenceListener() {
            @Override
            public String onUnsavedReferenceFound(String collection, GenericJson object) {
                return "test";
            }
        });

        assertNotNull(genericJson.get("test"));
        assertTrue(Map.class.isAssignableFrom(genericJson.getClass()));
        List reference = (List)genericJson.get("test");
        assertEquals(reference.size(), 10);

    }

    public static class SampleGson extends GenericJson{
        @Key("data")
        String id;

        public SampleGson(String id) {
            this.id = id;
        }
    }

}
