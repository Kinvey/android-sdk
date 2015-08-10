/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.java.core.KinveyMockUnitTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class AppDataTest extends KinveyMockUnitTest {

    public class Entity extends GenericJson {

        @Key("_id")
        private String title;

        @Key("Name")
        private String name;

        public Entity() {}

        public Entity(String title) {
            super();
            this.title = title;
        }

        public Entity(String title, String name) {
            super();
            this.title = title;
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public class EntityNoID extends GenericJson {

        @Key("Name")
        private String name;

        public EntityNoID() {}

        public EntityNoID(String name) {
            super();
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public void testNewQuery() {
        Query myQuery = getClient().query();
        assertEquals(0, myQuery.getLimit());
        assertEquals(0, myQuery.getSkip());
        assertEquals("",myQuery.getSortString());
        LinkedHashMap<String,Object> expected = new LinkedHashMap<String,Object>();
        assertEquals(expected ,(LinkedHashMap<String, Object>) myQuery.getQueryFilterMap());
    }

    // String collectionName, Class<T> myClass, AbstractClient client,KinveyClientRequestInitializer initializer
    public void testAppdataInitialization() {
        AppData<Entity> appData = new AppData<Entity>("testCollection",Entity.class,
        		getClient());
        assertEquals("testCollection",appData.getCollectionName());
        assertEquals(Entity.class, appData.getCurrentClass());
    }

    public void testNullCollectionInitialization() {
        try {
            AppData<Entity> appData = new AppData<Entity>(null, Entity.class, getClient());
            fail("NullPointerException should be thrown");
        } catch (NullPointerException ex) {}
    }

    public void testNullClassInitialization() {
        AppData<Entity> appData = new AppData<Entity>("myCollection", null, getClient());
        // Null class types are allowed, should not throw an exception.
        assertNull(appData.getCurrentClass());
    }

    public void testNullClientInitialization() {
        try {
            AppData<Entity> appData = new AppData<Entity>("myCollection", Entity.class, null);
            fail("NullPointerException should be thrown");
        } catch (NullPointerException ex) {}
    }

//    public void testNullRequestInitializerInitialization() {
//        try {
//            AppData<Entity> appData = new AppData<Entity>("myCollection", Entity.class, mockClient);
//            fail("NullPointerException should be thrown.");
//        } catch (NullPointerException ex) {}
//    }

    public void testChangeCollectionName() {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        assertEquals("myCollection",appData.getCollectionName());
        appData.setCollectionName("myNewCollection");
        assertEquals("myNewCollection", appData.getCollectionName());
    }

    public void testChangeCollectionNameToNull() {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        assertEquals("myCollection", appData.getCollectionName());
        try {
            appData.setCollectionName(null);
            fail("NullPointerException should be thrown.");
        } catch (NullPointerException ex) {}
    }

    public void testGet() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        String entityID = "myEntity";
        AppData.GetEntity myGet = appData.getEntityBlocking(entityID);
        assertNotNull(myGet);
        assertEquals("myEntity", myGet.get("entityID"));
        assertEquals("myCollection",myGet.get("collectionName"));
        assertEquals("GET", myGet.getRequestMethod());
    }

    public void testGetWithNoEntityID() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        AppData.Get myGet = appData.getBlocking();
        assertNotNull(myGet);
        assertNull(myGet.get("entityID"));
        assertEquals("myCollection", myGet.get("collectionName"));
        assertEquals("GET", myGet.getRequestMethod());
    }

    public void testGetWithArrayType() throws IOException {
        Entity[] entityList = new Entity[]{};
        AppData<Entity[]> appData = getGenericAppData(entityList.getClass());
        AppData.Get myGet = appData.getBlocking();
        assertNotNull(myGet);
        assertNull(myGet.get("entityID"));
        assertEquals("myCollection",myGet.get("collectionName"));
        assertEquals("GET", myGet.getRequestMethod());
    }

    public void testSave() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        AppData<Entity>.Save mySave = appData.saveBlocking(entity);
        assertNotNull(mySave);
        assertEquals("myEntity", ((GenericJson) mySave.getJsonContent()).get("_id"));
        assertEquals("My Name", ((GenericJson) mySave.getJsonContent()).get("Name"));
        assertEquals("PUT",mySave.getRequestMethod());
    }

    public void testSaveNullEntity() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = null;
        try {
            AppData<Entity>.Save mySave = appData.saveBlocking(entity);
            fail("NullPointerException should be thrown.");
        } catch (NullPointerException ex) {}
    }

    public void testSaveNoID() throws IOException {
        AppData<EntityNoID> appData = getGenericAppData(EntityNoID.class);
        EntityNoID entity = new EntityNoID("My Name");

        AppData<EntityNoID>.Save mySave= appData.saveBlocking(entity);
        assertNull(((GenericJson) mySave.getJsonContent()).get("_id"));
        assertEquals("My Name",((GenericJson) mySave.getJsonContent()).get("Name"));
        assertEquals("POST",mySave.getRequestMethod());
    }

    public void testDelete() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        String entityID = "myEntity";
        AppData.Delete myDelete = appData.deleteBlocking(entityID);
        assertNotNull(myDelete);
        assertEquals("myEntity", myDelete.get("entityID"));
        assertEquals("myCollection",myDelete.get("collectionName"));
        assertEquals("DELETE", myDelete.getRequestMethod());
    }

//    public void testDeleteNullEntityID() throws IOException {
//        AppData<Entity> appData = getGenericAppData(Entity.class);
//        String entityID = "myEntity";
//        try {
//            AppData<Entity>.Delete<Entity> myDelete = appData.delete(null);   TODO now ambigious because of query support...
//            fail("NullPointerException should be thrown.");
//        } catch (NullPointerException ex) {}
//
//    }

    public void testAggregateCountNoCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        AppData<Entity>.Aggregate myAggregate = appData.countBlocking(fields, null);
        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result",0);

        String expectedReduce = "function(doc,out){ out._result++;}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
    }

    public void testAggregateCountCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        MockQuery query = new MockQuery(new MockQueryFilter.MockBuilder());

        AppData<Entity>.Aggregate myAggregate = appData.countBlocking(fields, query);

        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result",0);

        String expectedReduce = "function(doc,out){ out._result++;}";

        String expectedCondition="{city=boston, age={$gt=18, $lt=21}}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
        assertEquals(expectedCondition, ((GenericJson) myAggregate.getJsonContent()).get("condition").toString());
    }

    public void testAggregateSumNoCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        AppData<Entity>.Aggregate myAggregate = appData.sumBlocking(fields, "total", null);
        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result",0);

        String expectedReduce = "function(doc,out){ out._result= out._result + doc.total;}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
    }

    public void testAggregateSumCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        MockQuery query = new MockQuery(new MockQueryFilter.MockBuilder());

        AppData<Entity>.Aggregate myAggregate = appData.sumBlocking(fields, "total", query);

        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result",0);

        String expectedReduce = "function(doc,out){ out._result= out._result + doc.total;}";

        String expectedCondition="{city=boston, age={$gt=18, $lt=21}}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
        assertEquals(expectedCondition, ((GenericJson) myAggregate.getJsonContent()).get("condition").toString());
    }

    public void testAggregateMaxNoCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        AppData<Entity>.Aggregate myAggregate = appData.maxBlocking(fields, "total", null);
        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result","-Infinity");

        String expectedReduce = "function(doc,out){ out._result = Math.max(out._result, doc.total);}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
    }

    public void testAggregateMaxCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        MockQuery query = new MockQuery(new MockQueryFilter.MockBuilder());

        AppData<Entity>.Aggregate myAggregate = appData.maxBlocking(fields, "total", query);

        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result","-Infinity");

        String expectedReduce = "function(doc,out){ out._result = Math.max(out._result, doc.total);}";

        String expectedCondition="{city=boston, age={$gt=18, $lt=21}}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
        assertEquals(expectedCondition, ((GenericJson) myAggregate.getJsonContent()).get("condition").toString());
    }

    public void testAggregateMinNoCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        AppData<Entity>.Aggregate myAggregate = appData.minBlocking(fields, "total", null);
        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result","Infinity");

        String expectedReduce = "function(doc,out){ out._result = Math.min(out._result, doc.total);}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
    }

    public void testAggregateMinCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        MockQuery query = new MockQuery(new MockQueryFilter.MockBuilder());

        AppData<Entity>.Aggregate myAggregate = appData.minBlocking(fields, "total", query);

        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result","Infinity");

        String expectedReduce = "function(doc,out){ out._result = Math.min(out._result, doc.total);}";

        String expectedCondition="{city=boston, age={$gt=18, $lt=21}}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
        assertEquals(expectedCondition, ((GenericJson) myAggregate.getJsonContent()).get("condition").toString());
    }

    public void testAggregateAverageNoCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        AppData<Entity>.Aggregate myAggregate = appData.averageBlocking(fields, "total", null);
        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result",0);

        String expectedReduce = "function(doc,out){ var count = (out._kcs_count == undefined) ? 0 : out._kcs_count; " +
            "out._result =(out._result * count + doc.total) " +
                    "/ (count + 1); out._kcs_count = count+1;}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
    }

    public void testAggregateAverageCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        MockQuery query = new MockQuery(new MockQueryFilter.MockBuilder());

        AppData<Entity>.Aggregate myAggregate = appData.averageBlocking(fields, "total", query);

        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result",0);

        String expectedReduce = "function(doc,out){ var count = (out._kcs_count == undefined) ? 0 : out._kcs_count; " +
                "out._result =(out._result * count + doc.total) " +
                "/ (count + 1); out._kcs_count = count+1;}";

        String expectedCondition="{city=boston, age={$gt=18, $lt=21}}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
        assertEquals(expectedCondition, ((GenericJson) myAggregate.getJsonContent()).get("condition").toString());
    }

    public void testAppDataCustomVersion() throws IOException {
    	AppData<Entity> appData = getGenericAppData(Entity.class);
    	appData.setClientAppVersion("1.2.3");
    	AppData<Entity>.GetEntity request = appData.getEntityBlocking("OK");
    	Object header = request.getRequestHeaders().get("X-Kinvey-Client-App-Version");
    	assertEquals("1.2.3", (String) header);
    }
    
    public void testAppDataCustomHeader() throws IOException {
    	AppData<Entity> appData = getGenericAppData(Entity.class);
    	GenericJson custom = new GenericJson();
    	custom.put("First", 1);
    	custom.put("Second", "two");
    	appData.setCustomRequestProperties(custom);
    	AppData<Entity>.GetEntity request = appData.getEntityBlocking("OK");
    	Object header = request.getRequestHeaders().get("X-Kinvey-Custom-Request-Properties");
    	assertEquals("{\"First\":1,\"Second\":\"two\"}", (String) header);    	
    	
    }
    
    public void testAppDataCustomVersionNull() throws IOException {
    	AppData<Entity> appData = getGenericAppData(Entity.class);
    	appData.setClientAppVersion(null);
    	AppData<Entity>.GetEntity request = appData.getEntityBlocking("OK");
    	Object header = request.getRequestHeaders().get("X-Kinvey-Client-App-Version");
    	assertEquals(null, header);    	
    }
    
    public void testAppDataCustomHeaderNull() throws IOException {
    	AppData<Entity> appData = getGenericAppData(Entity.class);
    	appData.setCustomRequestProperties(null);
    	AppData<Entity>.GetEntity request = appData.getEntityBlocking("OK");
    	Object header = request.getRequestHeaders().get("X-Kinvey-Custom-Request-Properties");
    	assertEquals(null, header);      	
    }
    
    public void testClientVersion() throws IOException {
    	getClient().setClientAppVersion("123");
    	AppData<Entity> appData = getGenericAppData(Entity.class);
    	AppData<Entity>.GetEntity request = appData.getEntityBlocking("OK");
    	Object header = request.getRequestHeaders().get("X-Kinvey-Client-App-Version");
    	assertEquals("123", (String) header); 
    }
    
    public void testClientCustomHeader() throws IOException{
    	getClient().setCustomRequestProperty("hello", "hey");
    	AppData<Entity> appData = getGenericAppData(Entity.class);
    	AppData<Entity>.GetEntity request = appData.getEntityBlocking("OK");
    	Object header = request.getRequestHeaders().get("X-Kinvey-Custom-Request-Properties");
    	assertEquals("{\"hello\":\"hey\"}", (String) header);    	
    }
    
    public void testClientAppendCustomHeader() throws IOException{
    	getClient().setCustomRequestProperty("hello", "hey");
    	AppData<Entity> appData = getGenericAppData(Entity.class);
    	appData.setCustomRequestProperty("bye", "bye");
    	AppData<Entity>.GetEntity request = appData.getEntityBlocking("OK");
    	Object header = request.getRequestHeaders().get("X-Kinvey-Custom-Request-Properties");
    	assertEquals("{\"hello\":\"hey\",\"bye\":\"bye\"}", (String) header);    	
    }
    
    
    
    public void testLargeCustomHeaders() throws IOException{
    	for (int i = 0; i < 200; i++){
    		getClient().setCustomRequestProperty("hello" + i, "this needs to be rather large");
    	}
    	
    	AppData<Entity> appData = getGenericAppData(Entity.class);
    	AppData<Entity>.GetEntity request = appData.getEntityBlocking("OK");
    	Object header = request.getRequestHeaders().get("X-Kinvey-Custom-Request-Properties");
    	//assertEquals("{\"hello\":\"hey\"}", (String) header); 
    	
    	try{
        	request.buildHttpRequest();
        	assertFalse("Should have thrown a 2k size exception!", true);
    	}catch(Exception e){
    		assertTrue(e.getMessage().contains("Cannot attach more than 2000 bytes of Custom Request Properties"));    
    	}
    	
    }
    
    private <T> AppData<T> getGenericAppData(Class<? extends Object> myClass) {
        AppData appData = new AppData("myCollection", myClass, getClient());
        return appData;
    }
    
    


}
