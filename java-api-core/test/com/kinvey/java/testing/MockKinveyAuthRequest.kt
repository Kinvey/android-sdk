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

import com.google.api.client.http.BasicAuthentication
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.GenericJson
import com.google.api.client.json.JsonFactory
import com.google.api.client.testing.http.MockHttpTransport
import com.google.api.client.testing.json.MockJsonFactory
import com.google.api.client.util.Key
import com.kinvey.java.auth.KinveyAuthRequest
import com.kinvey.java.auth.KinveyAuthResponse
import com.kinvey.java.auth.KinveyAuthResponse.KinveyUserMetadata
import com.kinvey.java.auth.ThirdPartyIdentity
import com.kinvey.java.dto.BaseUser
import java.io.IOException

/**
 * @author m0rganic
 * @since 2.0
 */
class MockKinveyAuthRequest : KinveyAuthRequest<BaseUser> {

    constructor(transport: HttpTransport?, jsonFactory: JsonFactory?,
                appKeyAuthentication: BasicAuthentication?, username: String?, password: String?,
                metaData: Map<String?, Any?>?, create: Boolean) : super(MockHttpTransport(), MockJsonFactory(),
            null, appKeyAuthentication, username, password,
            null, create) {
    }

    constructor(transport: HttpTransport?, jsonFactory: JsonFactory?,
                appKeyAuthentication: BasicAuthentication?,
                thirdPartyIdentity: ThirdPartyIdentity?, metaData: Map<String?, Any?>?)
            : super(MockHttpTransport(), MockJsonFactory(), null,
            appKeyAuthentication, thirdPartyIdentity, null, true) {
    }

    @Throws(IOException::class)
    override fun execute(): KinveyAuthResponse? {
        val mockResponse = MockKinveyAuthResponse()
        mockResponse.metadata = KinveyUserMetadata()
        mockResponse.metadata!!["authKey"] = "mockAuthKey"
        mockResponse.userId = "mockUserId"
        mockResponse.metadata!!.putAll(unknownKeys)
        return mockResponse
    }

    class MockBuilder : Builder<BaseUser> {
        constructor(transport: HttpTransport?, jsonFactory: JsonFactory?, appKey: String?, appSecret: String?, user: GenericJson?)
                : super(transport, jsonFactory!!, null, appKey!!, appSecret!!, null) {}

        constructor(transport: HttpTransport?, jsonFactory: JsonFactory?, appKey: String?, appSecret: String?,
                    username: String?, password: String?, user: GenericJson?)
                : super(transport!!, jsonFactory!!, null, appKey!!, appSecret!!, username!!, password!!, null) {
        }

        constructor(transport: HttpTransport?, jsonFactory: JsonFactory?, appKey: String?, appSecret: String?,
                    identity: ThirdPartyIdentity?, user: GenericJson?)
                : super(transport, jsonFactory!!, null, appKey!!, appSecret!!, identity) {
        }

        override fun build(): MockKinveyAuthRequest {
            return if (!thirdPartyAuthStatus) {
                MockKinveyAuthRequest(transport
                        , jsonFactory
                        , appKeyAuthentication
                        , username
                        , password, null, create)
            } else MockKinveyAuthRequest(transport
                    , jsonFactory
                    , appKeyAuthentication
                    , thirdPartyIdentity, null)
        }
    }

    class MockKinveyAuthResponse : KinveyAuthResponse() {
        @Key("_id")
        override var userId: String? = null
        override var metadata: KinveyUserMetadata? = null

    }
}