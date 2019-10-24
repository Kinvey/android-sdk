/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
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

package com.kinvey.java.auth

import com.google.api.client.http.*
import com.google.api.client.http.json.JsonHttpContent
import com.google.api.client.json.GenericJson
import com.google.api.client.json.JsonFactory
import com.google.api.client.util.Key
import com.google.common.base.Preconditions
import com.kinvey.java.core.KinveyHeaders
import com.kinvey.java.core.KinveyJsonResponseException.Companion.from
import com.kinvey.java.dto.BaseUser
import java.io.EOFException
import java.io.IOException
import java.util.*

/**
 * @author m0rganic
 * @since 2.0
 */
open class KinveyAuthRequest<T : BaseUser?> : GenericJson {
    enum class LoginType { IMPLICIT, KINVEY, THIRDPARTY }
    /**
     * used to construct the request body
     */
    private class AuthRequestPayload(@Key private val username: String, @Key private val password: String) : GenericJson()

    private var create: Boolean
    private var type: LoginType
    private var eofRetryAttempts = 3
    //TODO:  Set eofRetryAttempts value as builder method


    /**
     * http transport to utilize for the request *
     */
    private val transport: HttpTransport?
    /**
     * json factory from with the object parser is built *
     */
    private val jsonFactory: JsonFactory
    /** kcs base url  */
    private val baseUrl: String?
    /** backoff policy to use  */
    private var policy: BackOffPolicy
    /**
     * appkey and secret RequestInitializer for the initial POST to the user endpoint *
     */
    private val appKeyAuthentication: BasicAuthentication
    /**
     * used to formulate the uri in [.buildHttpRequestUrl] *
     */
    @Key
    private val appKey: String?
    /**
     * payload containing the json object Kinvey expects *
     */
    private val requestPayload: GenericJson?
    /**
     * the response returned from the Kinvey server on [.executeUnparsed] *
     */
    private var response: HttpResponse? = null
    /**
     * standard headers included in all requests
     */
    private var kinveyHeaders: KinveyHeaders? = null

    fun setKinveyHeaders(headers: KinveyHeaders?) {
        kinveyHeaders = headers
    }

    /**
     * Keep protected for testing support.
     *
     * @param transport            http transport layer
     * @param jsonFactory          json object parser factory
     * @param baseUrl
     * @param appKeyAuthentication app key and secret used to initialize the auth request
     * @param username             user provided username or `null` if none is known
     * @param password             password for the user or `null` if none is known
     */
    protected constructor(transport: HttpTransport?, jsonFactory: JsonFactory,
                          baseUrl: String?, appKeyAuthentication: BasicAuthentication, username: String?, password: String?,
                          user: GenericJson?, create: Boolean) {
        this.transport = transport
        this.jsonFactory = jsonFactory
        this.baseUrl = baseUrl
        this.appKeyAuthentication = appKeyAuthentication
        appKey = appKeyAuthentication.username
        requestPayload = if (username == null || password == null) GenericJson() else AuthRequestPayload(username, password)
        if (user != null) {
            requestPayload.putAll(user)
        }
        this.create = create
        type = if (requestPayload == null) LoginType.IMPLICIT else LoginType.KINVEY
        policy = ExponentialBackOffPolicy()
    }

    protected constructor(transport: HttpTransport?, jsonFactory: JsonFactory,
                          baseUrl: String?, appKeyAuthentication: BasicAuthentication, thirdPartyIdentity: ThirdPartyIdentity?,
                          user: GenericJson?, create: Boolean) {
        this.transport = transport
        this.jsonFactory = jsonFactory
        this.appKeyAuthentication = appKeyAuthentication
        this.baseUrl = baseUrl
        appKey = appKeyAuthentication.username
        requestPayload = thirdPartyIdentity
        if (user != null) {
            for (key in user.keys) {
                if (key != "_kmd" && key != "access_token" && key != "_socialIdentity") {
                    requestPayload!![key] = user[key]
                }
            }
        }
        this.create = create
        type = LoginType.THIRDPARTY
        policy = ExponentialBackOffPolicy()
    }

    /**
     * @return properly formed url for submitting requests to Kinvey authentication module
     */
    private fun buildHttpRequestUrl(): GenericUrl {
        // we hit different end points depending on whether 3rd party auth is utilize
        //TODO: change this.create boolean to a parameterized string, representing "login" portion of the url

        return GenericUrl(UriTemplate.expand(baseUrl
                , "/user/{appKey}/" + if (create) "" else "login"
                , this
                , false))
    }

    /**
     * @return low level http response
     */
    @Throws(IOException::class)
    fun executeUnparsed(): HttpResponse? {
        val content: HttpContent? = if (requestPayload != null) JsonHttpContent(jsonFactory, requestPayload) else null
        val request = transport?.createRequestFactory(appKeyAuthentication)?.
                buildPostRequest(buildHttpRequestUrl(), content)?.
                setSuppressUserAgentSuffix(true)?.
                setNumberOfRetries(3)?.
                setThrowExceptionOnExecuteError(false)?.
                setParser(jsonFactory.createJsonObjectParser())?.
                setBackOffPolicy(policy)?.
                setRetryOnExecuteIOException(true)
        if (kinveyHeaders != null) {
            for ((key, value) in kinveyHeaders!!) {
                request?.headers?.set(key.toLowerCase(Locale.US), value)
            }
        }
        try {
            response = request?.execute()
        } catch (ex: EOFException) {
            if (eofRetryAttempts > 0) {
                eofRetryAttempts--
                return executeUnparsed()
            } else {
                throw ex
            }
        }
        if (response?.isSuccessStatusCode == true) {
            return response
        } else if (response?.statusCode == 404 && type == LoginType.THIRDPARTY && !create) {
            create = true
            return executeUnparsed()
        }
        throw from(jsonFactory, response)
    }

    @Throws(IOException::class)
    open fun execute(): KinveyAuthResponse? {
        return executeUnparsed()?.parseAs(KinveyAuthResponse::class.java)
    }

    @Throws(IOException::class)
    fun execute(aClass: Class<T>): T? {
        return executeUnparsed()?.parseAs(aClass)
    }

    /**
     * Used to construct a [KinveyAuthRequest]. The result will be an auth request that adjusts for the
     * authentication scenario.
     *
     *
     * There are three scenarios that this builder will support for Kinvey authentication.
     *
     *
     *
     * The first is where the user is known
     * and the both the username and password have been provided.
     *
     *
     * <pre>
     * KinveyAuthResponse response = new KinveyAuthRequest.Builder(transport,jsonfactory,baseUrl, appKey,appSecret)
     * .setUserIdAndPassword(userid, password)
     * .build()
     * .execute();
    </pre> *
     *
     *
     *
     *
     * The second is where the user has established their identity with one of the supported 3rd party authentication
     * systems like Facebook, LinkedInCredential, etc.
     *
     * <pre>
     * KinveyAuthResponse response = new KinveyAuthRequest.Builder(transport,jsonfactory,baseUrl, appKey,appSecret)
     * .setThirdPartyAuthToken(thirdpartytoken)
     * .build()
     * .execute();
    </pre> *
     *
     *
     *
     *
     * The third and final way authentication can occur is with just the appKey and appSecret, in this case, an
     * implicit user is created when the [com.kinvey.java.auth.KinveyAuthRequest.execute] is called.
     *
     *
     *
     * <pre>
     * KinveyAuthResponse response = new KinveyAuthRequest.Builder(transport,jsonfactory,baseUrl, appKey,appSecret)
     * .build()
     * .execute();
    </pre> *
     */
    open class Builder<T : BaseUser?>(transport: HttpTransport?, jsonFactory: JsonFactory,
                                      val baseUrl: String?, appKey: String, appSecret: String, user: GenericJson?) {
        /**
         * @return the http trasport
         */
        val transport: HttpTransport?
        /**
         * @return json factory
         */
        val jsonFactory: JsonFactory
        /**
         * @return appkey and appsecret RequestInitializer used to create the BasicAuthentication for the POST request
         */
        val appKeyAuthentication: BasicAuthentication

        var create: Boolean = false

        /**
         * @return username or `null` in none is set
         */
        /**
         * @param username uniquely identifies the user
         */
        var username: String? = null
        protected var user: GenericJson?
            private set
        /**
         * @return password or `null` if none is set
         */
        /**
         * @param password user provided password for the authentication request
         */
        var password: String? = null

        protected var thirdPartyAuthStatus: Boolean = false
            private set

        var thirdPartyIdentity: ThirdPartyIdentity? = null
            set (value) {
                field = value
                thirdPartyAuthStatus = value != null
            }

        //private fun setThirdPartyIdentity(identity: ThirdPartyIdentity?): Builder<*> {
        //    thirdPartyIdentity = identity
        //    thirdPartyAuthStatus = true
        //    return this
        //}

        //fun setCreate(create: Boolean): Builder<*> {
        //    this.create = create
        //    return this
        //}

        constructor(transport: HttpTransport, jsonFactory: JsonFactory, baseUrl: String, appKey: String, appSecret: String,
                    username: String, password: String, user: GenericJson) : this(transport, jsonFactory, baseUrl, appKey, appSecret, user) {
            Preconditions.checkArgument("" != username, "username cannot be empty, use null if no username is known")
            Preconditions.checkArgument("" != password, "password cannot be empty, use null if no password is known")
            this.username = username
            this.password = password
        }

        constructor(transport: HttpTransport, jsonFactory: JsonFactory, baseUrl: String, appKey: String, appSecret: String,
                    identity: ThirdPartyIdentity?, user: GenericJson) : this(transport, jsonFactory, baseUrl, appKey, appSecret, user) {
            Preconditions.checkNotNull(identity, "identity must not be null")
            thirdPartyAuthStatus = identity != null
            thirdPartyIdentity = identity
        }

        open fun build(): KinveyAuthRequest<T> {
            return if (!thirdPartyAuthStatus) {
                KinveyAuthRequest(transport
                        , jsonFactory
                        , baseUrl, appKeyAuthentication
                        , username
                        , password, user, create)
            } else KinveyAuthRequest(transport
                    , jsonFactory
                    , baseUrl, appKeyAuthentication
                    , thirdPartyIdentity, user, create)
        }

        fun setUsernameAndPassword(username: String?, password: String?): Builder<*> {
            this.username = username
            this.password = password
            thirdPartyAuthStatus = false
            return this
        }

        fun setUser(user: GenericJson?): Builder<*> {
            this.user = user
            return this
        }

        init {
            this.transport = Preconditions.checkNotNull(transport)
            this.jsonFactory = Preconditions.checkNotNull(jsonFactory)
            appKeyAuthentication = BasicAuthentication(Preconditions.checkNotNull(appKey), Preconditions.checkNotNull(appSecret))
            this.user = user
        }
    }
}