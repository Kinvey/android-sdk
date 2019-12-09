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
package com.kinvey.java.core;

import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.GenericJson
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.testing.http.MockHttpTransport
import com.google.api.client.testing.json.MockJsonFactory
import com.kinvey.java.*
import com.kinvey.java.auth.ClientUser
import com.kinvey.java.cache.ICacheManager
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.query.MongoQueryFilter
import junit.framework.TestCase
import java.util.concurrent.ConcurrentHashMap

/**
 * @author edwardf
 * @since 2.0
 */
abstract class KinveyMockUnitTest<T : BaseUser> : TestCase() {

    private var mockClient: MockTestClient<T>? = null

    val client: MockTestClient<T>?
        get() {
            if (mockClient == null) {
                mockClient = MockTestClient.Builder<T>(MockHttpTransport(), MockJsonFactory(), null, MockKinveyClientRequestInitializer()).build()
            }
            return mockClient
        }

    open fun getClient(transport: HttpTransport?): MockTestClient<*> {
        return MockTestClient.Builder<T>(transport, GsonFactory(), null, MockKinveyClientRequestInitializer()).build()
    }

    open fun getKinveyRequestInitializer(): KinveyClientRequestInitializer {
        return mockClient?.kinveyRequestInitializer as KinveyClientRequestInitializer
    }

    class MockTestClient<T : BaseUser> (transport: HttpTransport?, httpRequestInitializer: HttpRequestInitializer?,
                                                             rootUrl: String?, servicePath: String?, objectParser: JsonObjectParser?,
                                                             kinveyRequestInitializer: KinveyClientRequestInitializer?)
        : AbstractClient<T>(transport, null, "https://baas.kinvey.com/", "",
            objectParser, kinveyRequestInitializer, null, null) {

        private val appDataInstanceCache: ConcurrentHashMap<String?, NetworkManager<*>>? = null
        override fun performLockDown() {
            //To change body of implemented methods use NetworkFileManager | Settings | NetworkFileManager Templates.

        }

        override fun userDiscovery(): UserDiscovery<*>? {
            return null  //To change body of implemented methods use NetworkFileManager | Settings | NetworkFileManager Templates.
        }

        override fun userGroup(): UserGroup? {
            return null  //To change body of implemented methods use NetworkFileManager | Settings | NetworkFileManager Templates.
        }// TODO Auto-generated method stub// TODO Auto-generated method stub// TODO Auto-generated method stub

        // TODO Auto-generated method stub
        // TODO Auto-generated method stub
        override var clientUser: ClientUser?
            get() = object : ClientUser {
                // TODO Auto-generated method stub
                // TODO Auto-generated method stub
                override var user: String?
                    get() =// TODO Auto-generated method stub
                        null
                    set(userID) {
                        // TODO Auto-generated method stub
                    }

                override fun clear() {
                    // TODO Auto-generated method stub
                }
            }
            set(clientUser) {
                super.clientUser = clientUser
            }

        override var activeUser: T?
            get() {
                synchronized(lock) { return user }
            }
            set(user) {
                synchronized(lock) { this.user = user }
            }

        override fun <I : GenericJson, O> customEndpoints(myClass: Class<O>?): CustomEndpoints<I, O> {
            return CustomEndpoints(this)
        }

/*        @Override
        public <I extends GenericJson, O> CustomEndpoints<I, O> customEndpoints(Class<O> myClass) {
            return null;
        }*/


        override var cacheManager: ICacheManager?
            get() {
                return null
            }
            set(cacheManager) {
                super.cacheManager = cacheManager
            }

        override var userCacheManager: ICacheManager?
            get() {
                return null
            }
            set(userCacheManager) {
                super.userCacheManager = userCacheManager
            }

        override var fileCacheFolder: String?
            get() {
                return null
            }
            set(fileCacheFolder) {
                super.fileCacheFolder = fileCacheFolder
            }

        override var syncCacheManager: ICacheManager?
            protected get() {
                return null
            }
            set(syncCacheManager) {
                super.syncCacheManager = syncCacheManager
            }

        override val deviceId: String
            get() {
                return ""
            }

        class Builder<T: BaseUser>(transport: HttpTransport?, jsonFactory: JsonFactory?,
                      httpRequestInitializer: HttpRequestInitializer?,
                      clientRequestInitializer: KinveyClientRequestInitializer?)
            : AbstractClient.Builder(transport as HttpTransport, null, null) {
            override fun build(): MockTestClient<T> {
                return MockTestClient(transport, httpRequestInitializer, baseUrl, servicePath,
                        objectParser, MockKinveyClientRequestInitializer())
            }
            init {
                setJsonFactory(jsonFactory)
            }
        }
    }

    class MockKinveyClientRequestInitializer internal constructor() : KinveyClientRequestInitializer("appkey", "appsecret", KinveyHeaders()) {
        var isCalled = false
        override fun initialize(request: AbstractKinveyClientRequest<*>) {
            isCalled = true
        }
    }

    class MockQuery(builder: MockQueryFilter.MockBuilder?) : Query(null) {
        override val queryFilterMap: LinkedHashMap<String, Any>
            get() {
                val filter: LinkedHashMap<String, Any> = LinkedHashMap<String, Any>()
                val innerFilter: LinkedHashMap<String, Any> = LinkedHashMap<String, Any>()
                filter["city"] = "boston"
                innerFilter["\$gt"] = 18
                innerFilter["\$lt"] = 21
                filter["age"] = innerFilter
                return filter
            }

        override fun getQueryFilterJson(factory: JsonFactory?): String {
            val filter: LinkedHashMap<String, Any> = LinkedHashMap<String, Any>()
            val innerFilter: LinkedHashMap<String, Any> = LinkedHashMap<String, Any>()
            filter["city"] = "boston"
            innerFilter["\$gt"] = 18
            innerFilter["\$lt"] = 21
            filter["age"] = innerFilter
            return filter.toString()
        }
    }

    class MockQueryFilter : MongoQueryFilter() {
        class MockBuilder : MongoQueryFilterBuilder()
    }

    class MockNetworkManager<T : GenericJson>
    /**
     * Constructor to instantiate the NetworkManager class.
     *
     * @param collectionName Name of the appData collection
     * @param myClass        Class Type to marshall data between.
     * @param client
     */
    protected constructor(collectionName: String?, myClass: Class<T>?, client: AbstractClient<*>?) : NetworkManager<T>(collectionName, myClass, client)
}
