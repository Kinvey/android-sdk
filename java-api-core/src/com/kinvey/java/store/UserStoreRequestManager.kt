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

package com.kinvey.java.store

import com.google.api.client.http.UrlEncodedContent
import com.google.api.client.json.GenericJson
import com.google.api.client.util.GenericData
import com.google.common.base.Preconditions
import com.kinvey.java.AbstractClient
import com.kinvey.java.KinveyException
import com.kinvey.java.Logger
import com.kinvey.java.Query
import com.kinvey.java.auth.Credential
import com.kinvey.java.auth.CredentialManager
import com.kinvey.java.auth.KinveyAuthRequest
import com.kinvey.java.auth.KinveyAuthResponse
import com.kinvey.java.auth.ThirdPartyIdentity
import com.kinvey.java.core.KinveyClientRequestInitializer
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.dto.DeviceId
import com.kinvey.java.dto.Email
import com.kinvey.java.dto.PasswordRequest
import com.kinvey.java.dto.Username
import com.kinvey.java.store.requests.user.Delete
import com.kinvey.java.store.requests.user.EmailVerification
import com.kinvey.java.store.requests.user.ForgotUsername
import com.kinvey.java.store.requests.user.GetMICAccessToken
import com.kinvey.java.store.requests.user.GetMICTempURL
import com.kinvey.java.store.requests.user.LockDownUser
import com.kinvey.java.store.requests.user.LoginToTempURL
import com.kinvey.java.store.requests.user.LogoutRequest
import com.kinvey.java.store.requests.user.LogoutSoftRequest
import com.kinvey.java.store.requests.user.LiveServiceRegisterRequest
import com.kinvey.java.store.requests.user.LiveServiceUnregisterRequest
import com.kinvey.java.store.requests.user.ResetPassword
import com.kinvey.java.store.requests.user.Retrieve
import com.kinvey.java.store.requests.user.RetrieveUsers
import com.kinvey.java.store.requests.user.Update
import com.kinvey.java.store.requests.user.UserExists

import java.io.IOException
import java.util.HashMap

import com.kinvey.java.Constants.ACCESS_ERROR

/**
 * Created by Prots on 2/12/16.
 */
class UserStoreRequestManager<T : BaseUser> {
    private val myClazz: Class<T>
    private lateinit var user: T
    private val clientAppVersion: String?
    private val customRequestProperties: GenericData?
    private val authToken: String? = null

    /**
     * the redirect URI for MIC
     */
    var micRedirectURI: String? = null
        @JvmName("getMICRedirectURI")
        get
        @JvmName("setMICRedirectURI")
        set

    private val client: AbstractClient<T>
    private val builder: KinveyAuthRequest.Builder<T>

    constructor(client: AbstractClient<T>?, builder: KinveyAuthRequest.Builder<T>?) {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkNotNull(builder, "KinveyAuthRequest.Builder should not be null")
        this.client = client!!
        this.builder = builder!!
        this.myClazz = client.userClass
        this.builder.setUser(client.activeUser)
        this.clientAppVersion = client.clientAppVersion
        this.customRequestProperties = client.customRequestProperties
    }

    fun getBuilder(): KinveyAuthRequest.Builder<*>? {
        return builder
    }


    enum class LoginType {
        IMPLICIT,
        KINVEY,
        GOOGLE,
        TWITTER,
        FACEBOOK,
        LINKED_IN,
        AUTH_LINK,
        MOBILE_IDENTITY,
        CREDENTIALSTORE,
        SALESFORCE,
        THIRDPARTY
    }


    fun getCustomRequestProperties(): GenericData? {
        return client.customRequestProperties
    }

    fun getClientAppVersion(): String? {
        return client.clientAppVersion
    }

    fun getClient() : AbstractClient<T> {
        return client
    }

    constructor(user: T, client: AbstractClient<T>, builder: KinveyAuthRequest.Builder<T>) : this(client, builder) {
        this.user = user
    }

    /**
     * Method to initialize the BaseUser after login, create a credential,
     * and add it to the KinveyClientRequestInitializer
     *
     * @param response KinveyAuthResponse object containing the login response
     * @throws IOException
     */
    @Deprecated("use {@link UserStoreRequestManager#initUser(BaseUser)} instead.")
    @Throws(IOException::class)
    fun initUser(response: KinveyAuthResponse, userObject: T): T {
        userObject.id = response.userId
        userObject.put("_kmd", response.metadata)
        userObject.putAll(response.unknownKeys)
        val currentUser: T
        try {
            currentUser = myClazz.newInstance()
        } catch (e: InstantiationException) {
            e.printStackTrace()
            throw KinveyException(e.message ?: "")
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            throw KinveyException(e.message ?: "")
        }

        currentUser.id = response.userId
        currentUser.put("_kmd", response.metadata)
        currentUser.putAll(response.unknownKeys)
        if (response.containsKey("username")) {
            currentUser.username = response["username"]!!.toString()
        }
        currentUser.authToken = response.authToken

        val credentialManager = CredentialManager(client.store)
        (client.kinveyRequestInitializer as KinveyClientRequestInitializer)
                .setCredential(credentialManager.createAndStoreCredential(response, userObject.id))
        client.clientUser?.user = currentUser.id
        client.activeUser = currentUser
        return currentUser
    }

    /**
     * Method to initialize the BaseUser after login, create a credential,
     * and add it to the KinveyClientRequestInitializer
     *
     * @param userObject user object for setting to active user and to save to Credential
     * @throws IOException exception
     */
    @Throws(IOException::class)
    fun initUser(userObject: T): T {
        val credentialManager = CredentialManager(client.store)
        (client.kinveyRequestInitializer as KinveyClientRequestInitializer)
                .setCredential(credentialManager.createAndStoreCredential(userObject))
        client.clientUser?.user = userObject.id
        client.activeUser = userObject
        return userObject
    }

    private fun initUser(credential: Credential, userObject: T): T {
        userObject.id = credential.userId
        userObject.authToken = credential.authToken
        client.activeUser = userObject
        return userObject
    }

    fun removeFromStore(userID: String) {
        val credentialManager = CredentialManager(client.store)
        credentialManager.removeCredential(userID)
    }


    /**
     * Login with the implicit user.  If implicit user does not exist, the user is created.  After calling this method,
     * the application should retrieve and store the userID using getId()
     *
     * @return LoginRequest object
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loginBlocking(): LoginRequest {
        return LoginRequest().buildAuthRequest()
    }

    /**
     * Login with Kinvey user and password.   If user does not exist, returns a error response.
     *
     * @param username userID of Kinvey BaseUser
     * @param password password of Kinvey user
     * @return LoginRequest object
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loginBlocking(username: String?, password: String?): LoginRequest {
        Preconditions.checkNotNull(username, "Username cannot be null.")
        Preconditions.checkNotNull(password, "Password cannot be null.")
        return LoginRequest(username, password, false).buildAuthRequest()
    }

    /**
     * Method to login via third party OAuth credentials
     *
     * @param thirdPartyType ThirdPartyIdentity Type enum
     * @param args Associated Keys for OAuth login
     * OAuth 2 providers (Google, Facebook) AccessToken
     * OAuth 1a providers (LinkedIn, Twitter) Access Token, Access Secret, Consumer Key, Consumer Secret
     * @return LoginRequest object
     * @throws IOException
     */
    @Throws(IOException::class)
    fun login(thirdPartyType: ThirdPartyIdentity.Type, vararg args: String): LoginRequest {
        Preconditions.checkNotNull(args)
        val identity = ThirdPartyIdentity.createThirdPartyIdentity(thirdPartyType, *args)
        return LoginRequest(identity).buildAuthRequest()
    }

    /**
     * Log in with existing credential
     *
     * @param credential
     * @return LoginRequest object
     * @throws IOException
     */
    fun login(credential: Credential): LoginRequest {
        return LoginRequest(credential).buildAuthRequest()
    }

    /**
     * Convenience Method to retrieve Metadata.
     *
     * @return Current user object with refreshed metadata
     * @throws IOException
     */
    @Throws(IOException::class)
    fun retrieveMetadataBlocking(): T {
        val ret = this.retrieveBlocking().execute()
        val currentUser: T
        if (client.activeUser == null) {
            try {
                currentUser = myClazz.newInstance()
            } catch (e: InstantiationException) {
                e.printStackTrace()
                throw KinveyException(e.message ?: "")
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                throw KinveyException(e.message ?: "")
            }
        } else {
            currentUser = client.activeUser as T
        }
        currentUser.putAll(ret!!.unknownKeys)
        currentUser.username = ret.username
        client.activeUser = currentUser
        return ret
    }

    /**
     * Login to Kinvey services using Facebook access token obtained through OAuth2.  If the user does not exist in the
     * Kinvey service, the user will be created.
     *
     * @param accessToken Facebook-generated access token.
     * @return LoginRequest Object
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loginFacebookBlocking(accessToken: String): LoginRequest {
        return login(ThirdPartyIdentity.Type.FACEBOOK, accessToken)
    }

    /**
     * Login to Kinvey services using Google access token obtained through OAuth2.  If the user does not exist in the
     * Kinvey service, the user will be created.
     *
     * @param accessToken Google-generated access token
     * @return LoginRequest Object
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loginGoogleBlocking(accessToken: String): LoginRequest {
        return login(ThirdPartyIdentity.Type.GOOGLE, accessToken)
    }

    /**
     * Login to Kinvey services using SalesForce access token obtained through OAuth2.  If the user does not exist in the
     * Kinvey service, the user will be created.
     *
     * @param accessToken SalesForce-generated access token
     * @return LoginRequest Object
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loginSalesForceBlocking(accessToken: String, Clientid: String, refreshToken: String, id: String): LoginRequest {
        return login(ThirdPartyIdentity.Type.SALESFORCE, accessToken, Clientid, refreshToken, id)
    }

    /**
     * Login to Kinvey services using Twitter-generated access token, access secret, consumer key, and consumer secret
     * obtained through OAuth1a.  If the user does not exist in the Kinvey service, the user will be created.
     *
     * @param accessToken Twitter-generated access token
     * @param accessSecret Twitter-generated access secret
     * @param consumerKey Twitter-generated consumer key
     * @param consumerSecret Twitter-generated consumer secret
     * @return LoginRequest Object
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loginTwitterBlocking(accessToken: String, accessSecret: String, consumerKey: String, consumerSecret: String): LoginRequest {
        return login(ThirdPartyIdentity.Type.TWITTER,
                accessToken, accessSecret, consumerKey, consumerSecret)
    }


    /**
     * Login to Kinvey services using a Kinvey user's _id and their valid Kinvey Auth Token.  This method is provided
     * to allow for cross-platform login, by reusing a session provided with another client library (or the REST api).
     *
     * @param userId the _id field of the user to login
     * @param authToken a valid Kinvey Auth token
     * @return a LoginRequest ready to be executed
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loginKinveyAuthTokenBlocking(userId: String, authToken: String): LoginRequest {
        var currentUser: T? = null
        try {
            currentUser = myClazz.newInstance()
        } catch (e: InstantiationException) {
            e.printStackTrace()
            throw KinveyException(e.message ?: "")
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            throw KinveyException(e.message ?: "")
        }

        currentUser!!.authToken = authToken
        currentUser.id = userId
        val c = Credential.from(userId, authToken)
        client.activeUser = currentUser
        return login(c)

    }

    /***
     * Login to Kinvey Services using Mobile Identity Connect
     *
     * @param authToken
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loginMobileIdentityBlocking(authToken: String): LoginRequest {
        return login(ThirdPartyIdentity.Type.MOBILE_IDENTITY, authToken)

    }

    /**
     * Login to Kinvey services using LinkedIn-generated access token, access secret, consumer key, and consumer secret
     * obtained through OAuth1a.  If the user does not exist in the Kinvey service, the user will be created.
     *
     * @param accessToken Linked In generated access token
     * @param accessSecret Linked In generated access secret
     * @param consumerKey Linked In generated consumer key
     * @param consumerSecret Linked In generated consumer secret
     * @return LoginRequest Object
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loginLinkedInBlocking(accessToken: String, accessSecret: String, consumerKey: String, consumerSecret: String): LoginRequest {
        return login(ThirdPartyIdentity.Type.LINKED_IN, accessToken, accessSecret, consumerKey, consumerSecret)
    }

    @Throws(IOException::class)
    fun loginAuthLinkBlocking(accessToken: String, refreshToken: String): LoginRequest {
        return login(ThirdPartyIdentity.Type.AUTH_LINK, accessToken, refreshToken)
    }

    /**
     * Logs the user out of the current app
     *
     * @return LogoutRequest object
     * @throws IOException
     */
    fun logout(): LogoutRequest {
        return LogoutRequest(client)
    }

    /**
     * Logs the user out of the current app without removing the user credential. For internal use.
     *
     * @return LogoutRequest object
     * @throws IOException
     */
    fun logoutSoft(): LogoutSoftRequest {
        return LogoutSoftRequest(client)
    }

    /**
     * Creates an explicit Kinvey BaseUser.
     *
     * @param username userName of Kinvey user
     * @param password password of Kinvey user
     * @return LoginRequest Object
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createBlocking(username: String, password: String): LoginRequest {
        return LoginRequest(username, password, true).buildAuthRequest()
    }

    /**
     * Creates an explicit Kinvey BaseUser.
     *
     * @param username userName of Kinvey user
     * @param password password of Kinvey user
     * @return LoginRequest Object
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createBlocking(username: String, password: String, user: T): LoginRequest {
        return LoginRequest(username, password, user, true).buildAuthRequest()
    }

    /**
     * Delete's the given user from the server.
     *
     * @param hardDelete if true, physically deletes the user. If false, marks user as inactive.
     * @return Delete Request
     * @throws IOException
     */
    @Throws(IOException::class)
    fun deleteBlocking(hardDelete: Boolean): Delete {

        Preconditions.checkNotNull(client.activeUser, "currentUser must not be null")
        Preconditions.checkNotNull(client.activeUser?.id, "currentUser ID must not be null")
        val delete = Delete(this, client.activeUser?.id ?: "", hardDelete)
        client.initializeRequest(delete)
        return delete
    }

    /**
     * Retrieves current user's metadata.
     *
     * @return Retrieve Request
     * @throws IOException
     */
    @Throws(IOException::class)
    fun retrieveBlocking(): Retrieve<T> {
        val userId = client.activeUser?.id
        Preconditions.checkNotNull(client.activeUser, "currentUser must not be null")
        Preconditions.checkNotNull(client.activeUser?.id, "currentUser ID must not be null")
        val retrieve = Retrieve<T>(this, client.activeUser?.id!!)
        client.initializeRequest(retrieve)
        return retrieve
    }

    /**
     * Retrieves an array of BaseUser[] based on a Query.
     *
     * @return a Retrieve Request ready to be executed
     * @throws IOException
     */
    @Throws(IOException::class)
    fun retrieveBlocking(query: Query): RetrieveUsers<*> {
        val retrieve = RetrieveUsers(this, query)
        client.initializeRequest(retrieve)
        return retrieve
    }

    /**
     * Retrieve current user's metadata with support for resolving KinveyReferences
     *
     * @param resolves - List of [com.kinvey.java.model.KinveyReference] fields to resolve
     * @return a Retrieve Request ready to be executed
     * @throws IOException
     */
    @Throws(IOException::class)
    fun retrieveBlocking(resolves: Array<String>): Retrieve<T> {
        Preconditions.checkNotNull(client.activeUser, "currentUser must not be null")
        Preconditions.checkNotNull(client.activeUser?.id, "currentUser ID must not be null")
        val retrieve = Retrieve(this, client.activeUser?.id!!, resolves, 1, true)
        client.initializeRequest(retrieve)
        return retrieve
    }

    /**
     * Retrieves an array of BaseUser[] based on a Query with support for resolving KinveyReferences
     *
     * @param query the query to execute
     * @param resolves - List of [com.kinvey.java.model.KinveyReference] fields to resolve
     * @return a Retrieve Request ready to be executed
     * @throws IOException
     */
    @Throws(IOException::class)
    fun retrieveBlocking(query: Query, resolves: Array<String>): RetrieveUsers<*> {
        Preconditions.checkNotNull(query, "query must not be null")
        val retrieve = RetrieveUsers(this, query, resolves, 1, true)
        client.initializeRequest(retrieve)
        return retrieve
    }

    /**
     * Updates the current user's profile
     *
     * @return Update request
     * @throws IOException
     */
    @Throws(IOException::class)
    fun updateBlocking(): Update<T> {
        Preconditions.checkNotNull(client.activeUser, "currentUser must not be null")
        Preconditions.checkNotNull(client.activeUser?.id, "currentUser ID must not be null")
        val update = Update(this, client.activeUser as T, myClazz)
        client.initializeRequest(update)
        return update
    }

    /**
     * Updates a provided baseUser's profile
     *
     * @param baseUser the baseUser to update
     * @return an Update request ready to be executed
     * @throws IOException
     */
    @Throws(IOException::class)
    fun updateBlocking(baseUser: BaseUser): Update<T> {
        Preconditions.checkNotNull(baseUser, "currentUser must not be null")
        Preconditions.checkNotNull(baseUser.id, "currentUser ID must not be null")
        val update = Update(this, baseUser, myClazz)
        client.initializeRequest(update)
        return update
    }

    @Throws(IOException::class)
    fun changePassword(newPassword: String): Update<T> {
        Preconditions.checkNotNull(client.activeUser, "currentUser must not be null")
        Preconditions.checkNotNull(client.activeUser?.id, "currentUser ID must not be null")
        val passwordRequest = PasswordRequest()
        passwordRequest.password = newPassword
        val update = Update(this, client.activeUser as T, passwordRequest, myClazz)
        client.initializeRequest(update)
        return update
    }

    @Throws(IOException::class)
    fun exists(username: String): UserExists {
        Preconditions.checkNotNull(username, "username must not be null")
        val name = Username()
        name.username = username
        val userExists = UserExists(client, name)
        client.initializeRequest(userExists)
        return userExists
    }

    @Throws(IOException::class)
    fun getUser(userId: String): Update<T> {
        Preconditions.checkNotNull(userId, "username must not be null")
        val update = Update(this, userId, myClazz)
        client.initializeRequest(update)
        return update
    }

    @Throws(IOException::class)
    fun save(): Update<T> {
        return updateBlocking()
    }

    /**
     * Initiates a password reset request for a provided username
     *
     * @param usernameOrEmail the username to request a password reset for
     * @return ResetPassword request
     * @throws IOException
     */
    @Throws(IOException::class)
    fun resetPasswordBlocking(usernameOrEmail: String?): ResetPassword {
        Preconditions.checkNotNull(usernameOrEmail, "username must not be null!")
        val reset = ResetPassword(this, usernameOrEmail)
        client.initializeRequest(reset)
        return reset
    }

    /**
     * Initiates an EmailVerification request for the current user
     *
     * @return EMail Verification Request
     * @throws IOException
     */
    @Throws(IOException::class)
    fun sendEmailVerificationBlocking(): EmailVerification {
        Preconditions.checkNotNull(client.activeUser, "currentUser must not be null")
        Preconditions.checkNotNull(client.activeUser?.id, "currentUser ID must not be null")
        val verify =  EmailVerification(this, client.activeUser?.id!!)
        client.initializeRequest(verify)
        return verify
    }


    /**
     * modify the locked down state of the provided user id.
     *
     *
     * This operation must be performed with the master secret
     *
     *
     * Locking down a user will prevent them from logging in and remove all locally stored content on their device
     *
     * @param userid  the id to lockdown
     * @param setLockdownStateTo true to lockdown, false to remove lockdown state
     * @return a LockDownUser request ready to execute
     * @throws IOException
     */
    @Throws(IOException::class)
    fun lockDownUserBlocking(userid: String, setLockdownStateTo: Boolean): LockDownUser {
        Preconditions.checkNotNull(userid, "userID must not be null")
        val lock = GenericJson()
        lock["userId"] = userid
        lock["setLockdownStateTo"] = setLockdownStateTo
        val lockdown = LockDownUser(this, lock)
        client.initializeRequest(lockdown)
        return lockdown
    }

    @Throws(IOException::class)
    fun forgotUsername(email: String): ForgotUsername {
        Preconditions.checkNotNull(email, "email must not be null")
        val userEmail = Email()
        userEmail.email = email
        val forgotUsername = ForgotUsername(client, userEmail)
        client.initializeRequest(forgotUsername)
        return forgotUsername
    }

    @Throws(IOException::class)
    fun getMICToken(code: String, clientId: String?): GetMICAccessToken {

        //        grant_type: "authorization_code" - this is always set to this value
        //        code: use the 'code' returned in the callback
        //        redirect_uri: The same redirect uri used when obtaining the auth grant.
        //        client_id:  The appKey (kid) of the app

        val data = HashMap<String, String?>()
        data["grant_type"] = "authorization_code"
        data["code"] = code
        data["redirect_uri"] = micRedirectURI
        var fullClientIdField = (client.kinveyRequestInitializer as KinveyClientRequestInitializer).appKey
        if (clientId != null) {
            fullClientIdField = "$fullClientIdField.$clientId"
        }
        data["client_id"] = fullClientIdField

        val content = UrlEncodedContent(data)
        val getToken = GetMICAccessToken(this, content)
        getToken.isRequireAppCredentials = false
        getToken.isRequiredClientIdAuth = true
        (client.kinveyRequestInitializer as KinveyClientRequestInitializer).setClientId(fullClientIdField)
        client.initializeRequest(getToken)
        return getToken
    }

    @Throws(IOException::class)
    fun getOAuthToken(clientId: String?, username: String, password: String): GetMICAccessToken {
        val data = HashMap<String, String>()
        data[GRANT_TYPE] = PASSWORD_TYPE
        data[USERNAME_PARAM] = username
        data[PASSWORD_PARAM] = password
        var fullClientIdField = (client.kinveyRequestInitializer as KinveyClientRequestInitializer).appKey
        if (clientId != null) {
            fullClientIdField = "$fullClientIdField.$clientId"
        }
        data[CLIENT_ID] = fullClientIdField
        val content = UrlEncodedContent(data)
        val getToken = GetMICAccessToken(this, content)
        getToken.isRequireAppCredentials = true
        client.initializeRequest(getToken)
        return getToken
    }

    @Throws(IOException::class)
    fun useRefreshToken(refreshToken: String): GetMICAccessToken {
        //        grant_type: "refresh_token" - this is always set to this value  - note the difference
        //        refresh_token: use the refresh token
        //        redirect_uri: The same redirect uri used when obtaining the auth grant.
        //        client_id:  The appKey (kid) of the app

        val data = HashMap<String, String?>()
        data["grant_type"] = "refresh_token"
        data["refresh_token"] = refreshToken
        data["redirect_uri"] = micRedirectURI
        var fullClientIdField = (client.kinveyRequestInitializer as KinveyClientRequestInitializer).appKey
        val clientId = client.store?.load(client.activeUser?.id)?.clientId
        if (clientId != null) {
            fullClientIdField = "$fullClientIdField.$clientId"
        }
        data["client_id"] = fullClientIdField

        val content = UrlEncodedContent(data)
        val getToken = GetMICAccessToken(this, content)
        getToken.isRequireAppCredentials = true
        client.initializeRequest(getToken)
        return getToken
    }

    @Throws(IOException::class)
    fun getMICTempURL(clientId: String?): GetMICTempURL {

        //    	client_id:  this is the app’s appKey (the KID)
        //    	redirect_uri:  the uri that the grant will redirect to on authentication, as set in the console. Note, this much exactly match one of the redirect URIs configured in the console.
        //    	response_type:  this is always set to “code”

        val data = HashMap<String, String?>()
        data["response_type"] = "code"
        data["redirect_uri"] = micRedirectURI
        var fullClientIdField = (client.kinveyRequestInitializer as KinveyClientRequestInitializer).appKey
        if (clientId != null) {
            fullClientIdField = "$fullClientIdField.$clientId"
        }
        data["client_id"] = fullClientIdField

        val content = UrlEncodedContent(data)
        val getTemp = GetMICTempURL(client, content)
        getTemp.isRequireAppCredentials = true
        client.initializeRequest(getTemp)
        return getTemp

    }


    @Throws(IOException::class)
    fun MICLoginToTempURL(username: String, password: String, clientId: String, tempURL: String): LoginToTempURL {

        //    	client_id:  this is the app’s appKey (the KID)
        //    	redirect_uri:  the uri that the grant will redirect to on authentication, as set in the console. Note, this much exactly match one of the redirect URIs configured in the console.
        //    	response_type:  this is always set to “code”
        //    	username
        //    	password


        val data = HashMap<String, String?>()
        var fullClientIdField = (client.kinveyRequestInitializer as KinveyClientRequestInitializer).appKey
        if (clientId != null) {
            fullClientIdField = "$fullClientIdField.$clientId"
        }
        data["client_id"] = fullClientIdField
        data["redirect_uri"] = micRedirectURI
        data["response_type"] = "code"
        data["username"] = username
        data["password"] = password

        val content = UrlEncodedContent(data)
        val loginTemp = LoginToTempURL(this, clientId, tempURL, content)
        loginTemp.isRequireAppCredentials = true
        client.initializeRequest(loginTemp)
        return loginTemp

    }


    inner class LoginRequest {
        internal lateinit var credential: Credential
        internal var type: LoginType
        internal lateinit var request: KinveyAuthRequest<T>

        constructor() {
            builder.setCreate(true)
            this.type = LoginType.IMPLICIT
        }

        constructor(username: String?, password: String?, setCreate: Boolean) {
            builder.setUsernameAndPassword(username, password)
            builder.setCreate(setCreate)
            builder.setUser(client.activeUser)
            this.type = LoginType.KINVEY
        }

        constructor(username: String?, password: String?, user: T, setCreate: Boolean) {
            builder.setUsernameAndPassword(username, password)
            builder.setCreate(setCreate)
            builder.setUser(user)
            this.type = LoginType.KINVEY
        }

        constructor(identity: ThirdPartyIdentity) {
            builder.thirdPartyIdentity = identity
            builder.setUser(client.activeUser)
            builder.setCreate(false)
            this.type = LoginType.THIRDPARTY
        }

        constructor(credential: Credential) {
            this.credential = credential
            this.type = LoginType.CREDENTIALSTORE
        }

        fun buildAuthRequest(): LoginRequest {
            this.request = builder.build()
            val kinveyHeaders = (client.kinveyRequestInitializer as KinveyClientRequestInitializer).kinveyHeaders
            if (clientAppVersion != null) {
                kinveyHeaders.set("X-Kinvey-Client-App-Version", clientAppVersion)
            }
            this.request.setKinveyHeaders(kinveyHeaders)
            return this
        }

        @Throws(IOException::class)
        fun execute(): T {
            if (client.isUserLoggedIn) {
                throw KinveyException("Attempting to login when a user is already logged in",
                        "call `UserStore.logout(myClient, kinveyClientCallback)` first -or- check `myClient.isUserLoggedIn()` before attempting to login again",
                        "Only one user can be active at a time, and logging in a new user will replace the current user which might not be intended")
            }
            var loggedUser: T
            try {
                loggedUser = myClazz.newInstance()
            } catch (e: Exception) {
                //                e.printStackTrace();
                throw NullPointerException(e.message)
            }

            if (this.type == LoginType.CREDENTIALSTORE) {
                initUser(credential, loggedUser) //only token and user_id is initialized here
                var savedUser: T? = null
                try {
                    savedUser = client.userCacheManager?.getCache(ACTIVE_USER_COLLECTION_NAME,
                                client.userClass, java.lang.Long.MAX_VALUE)?.get(loggedUser.id!!) //getting full user info from cache
                    if (savedUser != null) {
                        savedUser.authToken = loggedUser.authToken
                        savedUser.setAuthTokenToKmd(loggedUser.authToken!!)
                    }
                } catch (e: KinveyException) {
                    Logger.ERROR(e.reason)
                    if (e.reason != ACCESS_ERROR) {
                        throw e
                    }
                }

                return savedUser?.let { initUser(it) } ?: loggedUser
            }
            loggedUser = this.request.execute(myClazz)
            initUser(loggedUser)
            try {
                if (client.userCacheManager != null) {
                    client.userCacheManager?.getCache(ACTIVE_USER_COLLECTION_NAME,
                            client.userClass, java.lang.Long.MAX_VALUE)?.save(loggedUser)
                }
            } catch (e: KinveyException) {
                Logger.ERROR(e.reason)
                if (e.reason != ACCESS_ERROR) {
                    throw e
                }
            }

            return loggedUser
        }
    }

    @Throws(IOException::class)
    internal fun liveServiceRegister(userId: String, deviceId: String): LiveServiceRegisterRequest {
        Preconditions.checkNotNull(deviceId, "deviceId must not be null")
        Preconditions.checkNotNull(userId, "userId must not be null")
        val deviceID = DeviceId()
        deviceID.deviceId = deviceId
        val liveServiceRegisterRequest = LiveServiceRegisterRequest(client, userId, deviceID)
        client.initializeRequest(liveServiceRegisterRequest)
        return liveServiceRegisterRequest
    }

    @Throws(IOException::class)
    internal fun liveServiceUnregister(userId: String, deviceId: String): LiveServiceUnregisterRequest {
        Preconditions.checkNotNull(deviceId, "deviceId must not be null")
        Preconditions.checkNotNull(userId, "userId must not be null")
        val deviceID = DeviceId()
        deviceID.deviceId = deviceId
        val liveServiceUnregisterRequest = LiveServiceUnregisterRequest(client, userId, deviceID)
        client.initializeRequest(liveServiceUnregisterRequest)
        return liveServiceUnregisterRequest
    }

    companion object {


        val USER_COLLECTION_NAME = "user"
        private val ACTIVE_USER_COLLECTION_NAME = "active_user_info"
        val GRANT_TYPE = "grant_type"
        val USERNAME_PARAM = "username"
        val PASSWORD_PARAM = "password"
        val PASSWORD_TYPE = "password"
        val CLIENT_ID = "client_id"
    }
}
