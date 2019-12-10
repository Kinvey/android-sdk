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
package com.kinvey.java.testing

import com.google.api.client.http.BackOffPolicy
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.GenericJson
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.testing.http.MockHttpTransport
import com.google.api.client.testing.json.MockJsonFactory
import com.kinvey.java.AbstractClient
import com.kinvey.java.CustomEndpoints
import com.kinvey.java.UserDiscovery
import com.kinvey.java.UserGroup
import com.kinvey.java.auth.ClientUser
import com.kinvey.java.auth.CredentialStore
import com.kinvey.java.cache.ICacheManager
import com.kinvey.java.core.AbstractKinveyJsonClient
import com.kinvey.java.core.KinveyClientRequestInitializer
import com.kinvey.java.dto.BaseUser

/**
 * @author m0rganic
 * @since 2.0
 */
class MockKinveyJsonClient constructor(transport: HttpTransport?, httpRequestInitializer: HttpRequestInitializer?, rootUrl: String?,
                                                 servicePath: String?, objectParser: JsonObjectParser?,
                                                 kinveyRequestInitializer: KinveyClientRequestInitializer?, store: CredentialStore?,
                                                 requestPolicy: BackOffPolicy?)
    : AbstractClient<BaseUser>(transport, httpRequestInitializer, rootUrl, servicePath,
        objectParser, kinveyRequestInitializer, store, requestPolicy) {

    override fun performLockDown() {
        //To change body of implemented methods use NetworkFileManager | Settings | NetworkFileManager Templates.

    }

    class Builder : AbstractKinveyJsonClient.Builder(MockHttpTransport(),
            "https://www.google.com", "", null, SpyKinveyClientRequestInitializer()) {
        override fun build(): MockKinveyJsonClient? {
            return MockKinveyJsonClient(transport, httpRequestInitializer, baseUrl, servicePath, objectParser,
                    kinveyClientRequestInitializer, null, null)
        }

        init {
            setJsonFactory(MockJsonFactory())
        }
    }

    override fun userDiscovery(): UserDiscovery<*>? {
        // TODO Auto-generated method stub

        return null
    }

    override fun userGroup(): UserGroup? {
        // TODO Auto-generated method stub

        return null
    }

    // TODO Auto-generated method stub
    override var clientUser: ClientUser?
        get() =// TODO Auto-generated method stub

            null
        set(clientUser) {
            super.clientUser = clientUser
        }

    override var activeUser: BaseUser?
        get() = null
        set(user) {}

    override fun <I : GenericJson, O> customEndpoints(myClass: Class<O>?): CustomEndpoints<I, O> {
        return CustomEndpoints(this)
    }

/*  @Override
    public <I extends GenericJson, O> CustomEndpoints<I, O> customEndpoints(Class<O> myClass) {
        return null;
    }*/

    override var cacheManager: ICacheManager?
        get() = null
        set(cacheManager) {
            super.cacheManager = cacheManager
        }

    override var userCacheManager: ICacheManager?
        get() = null
        set(userCacheManager) {
            super.userCacheManager = userCacheManager
        }

    override var fileCacheFolder: String?
        get() = null
        set(fileCacheFolder) {
            super.fileCacheFolder = fileCacheFolder
        }

    override var syncCacheManager: ICacheManager?
        protected get() = null
        set(syncCacheManager) {
            super.syncCacheManager = syncCacheManager
        }

    override val deviceId: String
        get() = ""
}