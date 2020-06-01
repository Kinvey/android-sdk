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
package com.kinvey.java

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key
import com.google.common.base.Joiner
import com.google.gson.Gson
import com.kinvey.java.core.KinveyMockUnitTest
import com.kinvey.java.core.KinveyMockUnitTest.MockQueryFilter.MockBuilder
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.model.*
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.network.NetworkManager.*
import com.kinvey.java.sync.RequestMethod
import java.io.IOException
import java.util.*

/**
 * @author mjsalinger
 * @since 2.0
 */
class NetworkManagerTest : KinveyMockUnitTest<BaseUser>() {
    inner class Entity : GenericJson {
        @Key("_id")
        var title: String? = null
        @Key("Name")
        var name: String? = null

        constructor() {}
        constructor(title: String?) : super() {
            this.title = title
        }

        constructor(title: String?, name: String?) : super() {
            this.title = title
            this.name = name
        }

    }

    inner class EntityNoID : GenericJson {
        @Key("Name")
        var name: String? = null

        constructor() {}
        constructor(name: String?) : super() {
            this.name = name
        }

    }

    fun testDeltaGetConstructor() {
        val collection = "Entity"
        val netManager = NetworkManager(collection, Entity::class.java, client)
        val query = Query()
        val curItems = listOf(Entity(), Entity())
        val deltaGet  = DeltaGet(netManager, client, query, Entity::class.java, curItems)

        assertEquals(client, deltaGet.client)
        assertEquals(netManager, deltaGet.networkManager)
        assertEquals(query, deltaGet.query)
        assertEquals(Entity::class.java, deltaGet.myClass)
        assertEquals(curItems, deltaGet.currentItems)
    }

    fun testMetadataGetConstructor() {
        val collection = "Entity"
        val netManager = NetworkManager(collection, Entity::class.java, client)
        val query = Query()
        val curItems = listOf(Entity(), Entity())
        val deltaGet  = DeltaGet(netManager, client, query, Entity::class.java, curItems)
        val get = MetadataGet(netManager, client, deltaGet)

        assertEquals(client, get.client)
        assertEquals(netManager, get.netManager)
        assertEquals(deltaGet, get.getRequest)
    }

    fun testGetConstructor() {
        val collection = "Entity"
        val netManager = NetworkManager(collection, Entity::class.java, client)
        val query = Query()
        val curItems = listOf(Entity(), Entity())
        val  queryString = "queryString"
        val resolveDepth: Int = 1
        val retain = true
        val resolves = arrayOf("res1", "res2")
        val resolve = Joiner.on(",").join(resolves)

        val deltaGet1 = Get(netManager, client, query, Entity::class.java, resolves, resolveDepth, retain)
        assertEquals(client, deltaGet1.abstractKinveyClient)
        assertEquals(collection, deltaGet1.collectionName)
        assertEquals(resolveDepth.toString(), deltaGet1.resolveDepth)
        assertEquals(retain.toString(), deltaGet1.retainReferences)
        assertEquals(resolve, deltaGet1.resolve)
        assertEquals(Entity::class.java, deltaGet1.requestResponseClass)

        val deltaGet2 = Get(netManager, client, query, Entity::class.java)
        assertEquals(client, deltaGet2.abstractKinveyClient)
        assertEquals(Entity::class.java, deltaGet2.requestResponseClass)

        val deltaGet3 = Get(netManager, client, queryString, Entity::class.java)
        assertEquals(client, deltaGet3.abstractKinveyClient)
        assertEquals(queryString, deltaGet3.queryFilter)
        assertEquals(Entity::class.java, deltaGet3.requestResponseClass)
    }

    fun testQueryCacheGetConstructor() {
        val collection = "Entity"
        val netManager = NetworkManager(collection, Entity::class.java, client)
        val query = Query()
        val since = "since"

        val queryCacheGet1 = QueryCacheGet(netManager, client, query, Entity::class.java, since)
        assertEquals(client, queryCacheGet1.abstractKinveyClient)
        assertEquals(collection, queryCacheGet1.collectionName)
        assertEquals(since, queryCacheGet1.since)
    }

    fun testGetEntityConstructor() {
        val collection = "Entity"
        val netManager = NetworkManager(collection, Entity::class.java, client)
        val entityId = "entityId"

        val resolveDepth: Int = 1
        val retain = true
        val resolves = arrayOf("res1", "res2")
        val resolve = Joiner.on(",").join(resolves)

        val getEntity1 = GetEntity(netManager, client, entityId, Entity::class.java)
        assertEquals(client, getEntity1.abstractKinveyClient)
        assertEquals(collection, getEntity1.collectionName)
        assertEquals(entityId, getEntity1.entityID)
        assertEquals(Entity::class.java, getEntity1.responseClass)

        val queryCacheGet2 = GetEntity(netManager, client, entityId, Entity::class.java, resolves, resolveDepth, retain)
        assertEquals(client, queryCacheGet2.abstractKinveyClient)
        assertEquals(collection, queryCacheGet2.collectionName)
        assertEquals(entityId, getEntity1.entityID)
        assertEquals(resolveDepth.toString(), queryCacheGet2.resolveDepth)
        assertEquals(retain.toString(), queryCacheGet2.retainReferences)
        assertEquals(resolve, queryCacheGet2.resolve)
    }

    fun testGetCountConstructor() {
        val collection = "Entity"
        val netManager = NetworkManager(collection, Entity::class.java, client)
        val query = Query()

        val getCount1 = GetCount(netManager, client, query)
        assertEquals(client, getCount1.abstractKinveyClient)
        assertEquals(collection, getCount1.collectionName)
        assertEquals(KinveyCountResponse::class.java, getCount1.responseClass)
    }

    fun testSaveConstructor() {
        val collection = "Entity"
        val entity = Entity("title")
        val entityId = "entityId"
        val saveMode = SaveMode.POST
        val netManager = NetworkManager(collection, Entity::class.java, client)

        val save1 = Save(netManager, client, entity, Entity::class.java, entityId, saveMode)

        assertEquals(client, save1.abstractKinveyClient)
        assertEquals(collection, save1.collectionName)
        assertEquals(Entity::class.java, save1.responseClass)
        //assertEquals(entityId, save1.entityID)
        assertEquals(entity, save1.jsonContent)
        assertEquals(saveMode.name, save1.requestMethod)

    }

    fun testSaveBatchConstructor() {
        val collection = "Entity"
        val entityList = listOf(Entity("title"))
        val entityListJson = Gson().toJson(entityList)
        val saveMode = SaveMode.POST

        val saveBatchResponse = KinveySaveBatchResponse::class.java

        val netManager = NetworkManager(collection, Entity::class.java, client)

        val saveBatch1 = SaveBatch(netManager, client, entityList,
                saveBatchResponse as Class<KinveySaveBatchResponse<Entity>>, Entity::class.java, saveMode)

        assertEquals(client, saveBatch1.abstractKinveyClient)
        assertEquals(collection, saveBatch1.collectionName)
        assertEquals(KinveySaveBatchResponse::class.java, saveBatch1.responseClass)
        assertEquals(entityListJson, saveBatch1.jsonContent)
        assertEquals(saveMode.name, saveBatch1.requestMethod)
    }

    fun testDeleteConstructor() {
        val collection = "Entity"
        val requestMethod = RequestMethod.DELETE.name
        val entityId = "entityId"

        val netManager = NetworkManager(collection, Entity::class.java, client)

        val delete1 = Delete(netManager, client, entityId)

        assertEquals(client, delete1.abstractKinveyClient)
        assertEquals(collection, delete1.collectionName)
        assertEquals(KinveyDeleteResponse::class.java, delete1.responseClass)
        assertEquals(requestMethod, delete1.requestMethod)
    }

    fun testAggregateConstructor() {
        val collection = "Entity"
        val saveMode = SaveMode.POST
        val entityId = "entityId"
        val entity = AggregateEntity(null, AggregateType.COUNT, "field", Query(), null)
        val netManager = NetworkManager(collection, Entity::class.java, client)

        val delete1 = Aggregate(netManager, client, entity, Entity::class.java as Class<Array<Entity>>)

        assertEquals(client, delete1.abstractKinveyClient)
        assertEquals(collection, delete1.collectionName)
        assertEquals(Entity::class.java, delete1.responseClass)
        assertEquals(saveMode.name, delete1.requestMethod)
    }

    fun testNewQuery() {
        val myQuery = client?.query()
        assertEquals(0, myQuery?.limit)
        assertEquals(0, myQuery?.skip)
        assertEquals("", myQuery?.sortString)
        val expected = LinkedHashMap<String?, Any?>()
        assertEquals(expected, myQuery?.queryFilterMap as LinkedHashMap<String?, Any?>?)
    }

    // String collectionName, Class<T> myClass, AbstractClient client,KinveyClientRequestInitializer initializer
    fun testAppdataInitialization() {
        val appData = NetworkManager<Entity>("testCollection", Entity::class.java, client)
        assertEquals("testCollection", appData.collectionName)
        assertEquals(Entity::class.java, appData.currentClass)
    }

    fun testNullCollectionInitialization() {
        try {
            val appData = NetworkManager<Entity>(null, Entity::class.java, client)
            fail("NullPointerException should be thrown")
        } catch (ex: NullPointerException) {
        }
    }

    fun testNullClassInitialization() {
        val appData = NetworkManager<Entity>("myCollection", null, client)
        // Null class types are allowed, should not throw an exception.


        assertNull(appData.currentClass)
    }

    fun testNullClientInitialization() {
        try {
            val appData = NetworkManager<Entity>("myCollection", Entity::class.java, null)
            fail("NullPointerException should be thrown")
        } catch (ex: NullPointerException) {
        }
    }

//    public void testNullRequestInitializerInitialization() {
//        try {
//            NetworkManager<Entity> appData = new NetworkManager<Entity>("myCollection", Entity.class, mockClient);
//            fail("NullPointerException should be thrown.");
//        } catch (NullPointerException ex) {}
//    }


    fun testChangeCollectionName() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        assertEquals("myCollection", appData?.collectionName)
        appData?.collectionName = "myNewCollection"
        assertEquals("myNewCollection", appData?.collectionName)
    }

    fun testChangeCollectionNameToNull() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        assertEquals("myCollection", appData?.collectionName)
        try {
            appData?.collectionName = null
            fail("NullPointerException should be thrown.")
        } catch (ex: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    fun testGet() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val entityID = "myEntity"
        val myGet = appData?.getEntityBlocking(entityID)
        assertNotNull(myGet)
        assertEquals("myEntity", myGet?.get("entityID"))
        assertEquals("myCollection", myGet?.get("collectionName"))
        assertEquals("GET", myGet?.requestMethod)
    }

    @Throws(IOException::class)
    fun testGetWithNoEntityID() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val myGet = appData?.getBlocking()
        assertNotNull(myGet)
        assertNull(myGet?.get("entityID"))
        assertEquals("myCollection", myGet?.get("collectionName"))
        assertEquals("GET", myGet?.requestMethod)
    }

    @Throws(IOException::class)
    fun testGetWithArrayType() {
        val entityList = arrayOf<Entity>()
        val appData = getGenericAppData<Entity>(entityList.javaClass as Class<Entity>)
        val myGet = appData?.getBlocking()
        assertNotNull(myGet)
        assertNull(myGet?.get("entityID"))
        assertEquals("myCollection", myGet?.get("collectionName"))
        assertEquals("GET", myGet?.requestMethod)
    }

    @Throws(IOException::class)
    fun testSave() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val entity = Entity("myEntity", "My Name")
        val mySave = appData?.saveBlocking(entity)
        assertNotNull(mySave)
        assertEquals("myEntity", (mySave?.jsonContent as GenericJson)["_id"])
        assertEquals("My Name", (mySave?.jsonContent as GenericJson)["Name"])
        assertEquals("PUT", mySave?.requestMethod)
    }

    @Throws(IOException::class)
    fun testSaveNullEntity() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val entity: Entity? = null
        try {
            val mySave = appData?.saveBlocking(entity)
            fail("NullPointerException should be thrown.")
        } catch (ex: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    fun testSaveNoID() {
        val appData = getGenericAppData<EntityNoID>(EntityNoID::class.java)
        val entity = EntityNoID("My Name")
        val mySave = appData?.saveBlocking(entity)
        assertNull((mySave?.jsonContent as GenericJson)["_id"])
        assertEquals("My Name", (mySave?.jsonContent as GenericJson)["Name"])
        assertEquals("POST", mySave?.requestMethod)
    }

    @Throws(IOException::class)
    fun testDelete() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val entityID = "myEntity"
        val myDelete = appData?.deleteBlocking(entityID)
        assertNotNull(myDelete)
        assertEquals("myEntity", myDelete?.get("entityID"))
        assertEquals("myCollection", myDelete?.get("collectionName"))
        assertEquals("DELETE", myDelete?.requestMethod)
    }

//    public void testDeleteNullEntityID() throws IOException {
//        NetworkManager<Entity> appData = getGenericAppData(Entity.class);
//        String entityID = "myEntity";
//        try {
//            NetworkManager<Entity>.Delete<Entity> myDelete = appData.delete(null);   TODO now ambigious because of query support...
//            fail("NullPointerException should be thrown.");
//        } catch (NullPointerException ex) {}
//
//    }


    @Throws(IOException::class)
    fun testAggregateCountNoCondition() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val entity = Entity("myEntity", "My Name")
        val fields = ArrayList<String>()
        fields.add("state")
        val myAggregate = appData?.countBlocking(fields, Array<Entity>::class.java, null)
        val expectedFields = HashMap<String?, Boolean?>()
        expectedFields["state"] = true
        val expectedInitial = HashMap<String?, Any?>()
        expectedInitial["_result"] = 0
        val expectedReduce = "function(doc,out){ out._result++;}"
        assertNotNull(myAggregate)
        assertEquals(expectedFields, (myAggregate?.jsonContent as GenericJson)["key"])
        assertEquals("POST", myAggregate?.requestMethod)
        assertEquals(expectedInitial, (myAggregate?.jsonContent as GenericJson)["initial"])
        assertEquals(expectedReduce, (myAggregate?.jsonContent as GenericJson)["reduce"])
    }

    @Throws(IOException::class)
    fun testAggregateCountCondition() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val entity = Entity("myEntity", "My Name")
        val fields = ArrayList<String>()
        fields.add("state")
        val query = MockQuery(MockBuilder())
        val myAggregate = appData?.countBlocking(fields, Array<Entity>::class.java, query)
        val expectedFields = HashMap<String?, Boolean?>()
        expectedFields["state"] = true
        val expectedInitial = HashMap<String?, Any?>()
        expectedInitial["_result"] = 0
        val expectedReduce = "function(doc,out){ out._result++;}"
        val expectedCondition = "{city=boston, age={\$gt=18, \$lt=21}}"
        assertNotNull(myAggregate)
        assertEquals(expectedFields, (myAggregate?.jsonContent as GenericJson)["key"])
        assertEquals("POST", myAggregate?.requestMethod)
        assertEquals(expectedInitial, (myAggregate?.jsonContent as GenericJson)["initial"])
        assertEquals(expectedReduce, (myAggregate?.jsonContent as GenericJson)["reduce"])
        assertEquals(expectedCondition, (myAggregate?.jsonContent as GenericJson)["condition"].toString())
    }

    @Throws(IOException::class)
    fun testAggregateSumNoCondition() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val entity = Entity("myEntity", "My Name")
        val fields = ArrayList<String>()
        fields.add("state")
        val myAggregate = appData?.sumBlocking(fields, "total", Array<Entity>::class.java, null)
        val expectedFields = HashMap<String?, Boolean?>()
        expectedFields["state"] = true
        val expectedInitial = HashMap<String?, Any?>()
        expectedInitial["_result"] = 0
        val expectedReduce = "function(doc,out){ out._result= out._result + doc.total;}"
        assertNotNull(myAggregate)
        assertEquals(expectedFields, (myAggregate?.jsonContent as GenericJson)["key"])
        assertEquals("POST", myAggregate?.requestMethod)
        assertEquals(expectedInitial, (myAggregate?.jsonContent as GenericJson)["initial"])
        assertEquals(expectedReduce, (myAggregate?.jsonContent as GenericJson)["reduce"])
    }

    @Throws(IOException::class)
    fun testAggregateSumCondition() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val entity = Entity("myEntity", "My Name")
        val fields = ArrayList<String>()
        fields.add("state")
        val query = MockQuery(MockBuilder())
        val myAggregate = appData?.sumBlocking(fields, "total", Array<Entity>::class.java, query)
        val expectedFields = HashMap<String?, Boolean?>()
        expectedFields["state"] = true
        val expectedInitial = HashMap<String?, Any?>()
        expectedInitial["_result"] = 0
        val expectedReduce = "function(doc,out){ out._result= out._result + doc.total;}"
        val expectedCondition = "{city=boston, age={\$gt=18, \$lt=21}}"
        assertNotNull(myAggregate)
        assertEquals(expectedFields, (myAggregate?.jsonContent as GenericJson)["key"])
        assertEquals("POST", myAggregate?.requestMethod)
        assertEquals(expectedInitial, (myAggregate?.jsonContent as GenericJson)["initial"])
        assertEquals(expectedReduce, (myAggregate?.jsonContent as GenericJson)["reduce"])
        assertEquals(expectedCondition, (myAggregate?.jsonContent as GenericJson)["condition"].toString())
    }

    @Throws(IOException::class)
    fun testAggregateMaxNoCondition() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val entity = Entity("myEntity", "My Name")
        val fields = ArrayList<String>()
        fields.add("state")
        val myAggregate = appData?.maxBlocking(fields, "total", Array<Entity>::class.java, null)
        val expectedFields = HashMap<String?, Boolean?>()
        expectedFields["state"] = true
        val expectedInitial = HashMap<String?, Any?>()
        expectedInitial["_result"] = "-Infinity"
        val expectedReduce = "function(doc,out){ out._result = Math.max(out._result, doc.total);}"
        assertNotNull(myAggregate)
        assertEquals(expectedFields, (myAggregate?.jsonContent as GenericJson)["key"])
        assertEquals("POST", myAggregate?.requestMethod)
        assertEquals(expectedInitial, (myAggregate?.jsonContent as GenericJson)["initial"])
        assertEquals(expectedReduce, (myAggregate?.jsonContent as GenericJson)["reduce"])
    }

    @Throws(IOException::class)
    fun testAggregateMaxCondition() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val entity = Entity("myEntity", "My Name")
        val fields = ArrayList<String>()
        fields.add("state")
        val query = MockQuery(MockBuilder())
        val myAggregate = appData?.maxBlocking(fields, "total", Array<Entity>::class.java, query)
        val expectedFields = HashMap<String?, Boolean?>()
        expectedFields["state"] = true
        val expectedInitial = HashMap<String?, Any?>()
        expectedInitial["_result"] = "-Infinity"
        val expectedReduce = "function(doc,out){ out._result = Math.max(out._result, doc.total);}"
        val expectedCondition = "{city=boston, age={\$gt=18, \$lt=21}}"
        assertNotNull(myAggregate)
        assertEquals(expectedFields, (myAggregate?.jsonContent as GenericJson)["key"])
        assertEquals("POST", myAggregate?.requestMethod)
        assertEquals(expectedInitial, (myAggregate?.jsonContent as GenericJson)["initial"])
        assertEquals(expectedReduce, (myAggregate?.jsonContent as GenericJson)["reduce"])
        assertEquals(expectedCondition, (myAggregate?.jsonContent as GenericJson)["condition"].toString())
    }

    @Throws(IOException::class)
    fun testAggregateMinNoCondition() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val entity = Entity("myEntity", "My Name")
        val fields = ArrayList<String>()
        fields.add("state")
        val myAggregate = appData!!.minBlocking(fields, "total", Array<Entity>::class.java, null)
        val expectedFields = HashMap<String?, Boolean?>()
        expectedFields["state"] = true
        val expectedInitial = HashMap<String?, Any?>()
        expectedInitial["_result"] = "Infinity"
        val expectedReduce = "function(doc,out){ out._result = Math.min(out._result, doc.total);}"
        assertNotNull(myAggregate)
        assertEquals(expectedFields, (myAggregate?.jsonContent as GenericJson)["key"])
        assertEquals("POST", myAggregate?.requestMethod)
        assertEquals(expectedInitial, (myAggregate?.jsonContent as GenericJson)["initial"])
        assertEquals(expectedReduce, (myAggregate?.jsonContent as GenericJson)["reduce"])
    }

    @Throws(IOException::class)
    fun testAggregateMinCondition() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val entity = Entity("myEntity", "My Name")
        val fields = ArrayList<String>()
        fields.add("state")
        val query = MockQuery(MockBuilder())
        val myAggregate = appData?.minBlocking(fields, "total", Array<Entity>::class.java, query)
        val expectedFields = HashMap<String?, Boolean?>()
        expectedFields["state"] = true
        val expectedInitial = HashMap<String?, Any?>()
        expectedInitial["_result"] = "Infinity"
        val expectedReduce = "function(doc,out){ out._result = Math.min(out._result, doc.total);}"
        val expectedCondition = "{city=boston, age={\$gt=18, \$lt=21}}"
        assertNotNull(myAggregate)
        assertEquals(expectedFields, (myAggregate?.jsonContent as GenericJson)["key"])
        assertEquals("POST", myAggregate?.requestMethod)
        assertEquals(expectedInitial, (myAggregate?.jsonContent as GenericJson)["initial"])
        assertEquals(expectedReduce, (myAggregate?.jsonContent as GenericJson)["reduce"])
        assertEquals(expectedCondition, (myAggregate?.jsonContent as GenericJson)["condition"].toString())
    }

    @Throws(IOException::class)
    fun testAggregateAverageNoCondition() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val entity = Entity("myEntity", "My Name")
        val fields = ArrayList<String>()
        fields.add("state")
        val myAggregate = appData?.averageBlocking(fields, "total", Array<Entity>::class.java, null)
        val expectedFields = HashMap<String?, Boolean?>()
        expectedFields["state"] = true
        val expectedInitial = HashMap<String?, Any?>()
        expectedInitial["_result"] = 0
        val expectedReduce = "function(doc,out){ var count = (out._kcs_count == undefined) ? 0 : out._kcs_count; " +
                "out._result =(out._result * count + doc.total) " +
                "/ (count + 1); out._kcs_count = count+1;}"
        assertNotNull(myAggregate)
        assertEquals(expectedFields, (myAggregate?.jsonContent as GenericJson)["key"])
        assertEquals("POST", myAggregate?.requestMethod)
        assertEquals(expectedInitial, (myAggregate?.jsonContent as GenericJson)["initial"])
        assertEquals(expectedReduce, (myAggregate?.jsonContent as GenericJson)["reduce"])
    }

    @Throws(IOException::class)
    fun testAggregateAverageCondition() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val entity = Entity("myEntity", "My Name")
        val fields = ArrayList<String>()
        fields.add("state")
        val query = MockQuery(MockBuilder())
        val myAggregate = appData?.averageBlocking(fields, "total", Array<Entity>::class.java, query)
        val expectedFields = HashMap<String?, Boolean?>()
        expectedFields["state"] = true
        val expectedInitial = HashMap<String?, Any?>()
        expectedInitial["_result"] = 0
        val expectedReduce = "function(doc,out){ var count = (out._kcs_count == undefined) ? 0 : out._kcs_count; " +
                "out._result =(out._result * count + doc.total) " +
                "/ (count + 1); out._kcs_count = count+1;}"
        val expectedCondition = "{city=boston, age={\$gt=18, \$lt=21}}"
        assertNotNull(myAggregate)
        assertEquals(expectedFields, (myAggregate?.jsonContent as GenericJson)["key"])
        assertEquals("POST", myAggregate?.requestMethod)
        assertEquals(expectedInitial, (myAggregate?.jsonContent as GenericJson)["initial"])
        assertEquals(expectedReduce, (myAggregate?.jsonContent as GenericJson)["reduce"])
        assertEquals(expectedCondition, (myAggregate?.jsonContent as GenericJson)["condition"].toString())
    }

    @Throws(IOException::class)
    fun testAppDataCustomVersion() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        appData?.clientAppVersion = "1.2.3"
        val request = appData?.getEntityBlocking("OK")
        val header = request?.getRequestHeaders()?.get("X-Kinvey-Client-App-Version")
        assertEquals("1.2.3", header as String)
    }

    @Throws(IOException::class)
    fun testAppDataCustomHeader() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val custom = GenericJson()
        custom["First"] = 1
        custom["Second"] = "two"
        appData?.setCustomRequestProperties(custom)
        val request = appData?.getEntityBlocking("OK")
        val header = request?.getRequestHeaders()?.get("X-Kinvey-Custom-Request-Properties")
        assertEquals("{\"First\":1,\"Second\":\"two\"}", header as String)
    }

    @Throws(IOException::class)
    fun testAppDataCustomVersionNull() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        appData?.clientAppVersion = null
        val request = appData?.getEntityBlocking("OK")
        val header = request?.getRequestHeaders()?.get("X-Kinvey-Client-App-Version")
        assertEquals(null, header)
    }

    @Throws(IOException::class)
    fun testAppDataCustomHeaderNull() {
        val appData = getGenericAppData<Entity>(Entity::class.java)
        appData?.setCustomRequestProperties(null)
        val request = appData?.getEntityBlocking("OK")
        val header = request?.getRequestHeaders()?.get("X-Kinvey-Custom-Request-Properties")
        assertEquals(null, header)
    }

    @Throws(IOException::class)
    fun testClientVersion() {
        client?.clientAppVersion = "123"
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val request = appData!!.getEntityBlocking("OK")
        val header = request?.getRequestHeaders()?.get("X-Kinvey-Client-App-Version")
        assertEquals("123", header as String)
    }

    @Throws(IOException::class)
    fun testClientCustomHeader() {
        client?.setCustomRequestProperty("hello", "hey")
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val request = appData?.getEntityBlocking("OK")
        val header = request?.getRequestHeaders()?.get("X-Kinvey-Custom-Request-Properties")
        assertEquals("{\"hello\":\"hey\"}", header as String)
    }

    @Throws(IOException::class)
    fun testClientAppendCustomHeader() {
        client?.setCustomRequestProperty("hello", "hey")
        val appData = getGenericAppData<Entity>(Entity::class.java)
        appData?.setCustomRequestProperty("bye", "bye")
        val request = appData?.getEntityBlocking("OK")
        val header = request?.getRequestHeaders()?.get("X-Kinvey-Custom-Request-Properties")
        assertEquals("{\"hello\":\"hey\",\"bye\":\"bye\"}", header as String)
    }

    @Throws(IOException::class)
    fun testLargeCustomHeaders() {
        (0..199).forEach { i ->
            client?.setCustomRequestProperty("hello$i", "this needs to be rather large")
        }
        val appData = getGenericAppData<Entity>(Entity::class.java)
        val request = appData?.getEntityBlocking("OK")
        val header = request?.getRequestHeaders()?.get("X-Kinvey-Custom-Request-Properties")
        //assertEquals("{\"hello\":\"hey\"}", (String) header);
        try {
            request?.buildHttpRequest()
            assertFalse("Should have thrown a 2k size exception!", true)
        } catch (e: Exception) {
            assertTrue(e.message?.contains("Cannot attach more than 2000 bytes of Custom Request Properties") == true)
        }
    }

    private fun <T : GenericJson> getGenericAppData(myClass: Class<T>): NetworkManager<T>? {
        return NetworkManager("myCollection", myClass, client)
    }
}