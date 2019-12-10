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
import com.google.api.client.json.gson.GsonFactory
import com.kinvey.java.auth.KinveyAuthRequest
import com.kinvey.java.auth.KinveyAuthRequest.Builder
import com.kinvey.java.auth.ThirdPartyIdentity.Type
import com.kinvey.java.core.KinveyClientRequestInitializer
import com.kinvey.java.core.KinveyHeaders
import com.kinvey.java.core.KinveyMockUnitTest
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.store.UserStoreRequestManager
import com.kinvey.java.store.UserStoreRequestManager.LoginRequest
import com.kinvey.java.store.requests.user.Retrieve
import com.kinvey.java.store.requests.user.Update
import com.kinvey.java.testing.MockHttpForMIC
import com.kinvey.java.testing.MockKinveyAuthRequest.MockBuilder
import java.io.IOException
import java.lang.reflect.Field

/**
 * @author mjsalinger
 * @since 2.0
 */
class BaseUserTest : KinveyMockUnitTest<*>() {
    private var requestManager: UserStoreRequestManager<BaseUser?>? = null
    private fun initializeRequestManager(isNeedCreateUser: Boolean) {
        requestManager = UserStoreRequestManager<BaseUser?>(client, MockBuilder(client!!.requestFactory!!.transport,
                client!!.jsonFactory, "mockAppKey", "mockAppSecret", null))
        if (isNeedCreateUser) {
            requestManager!!.getClient()!!.activeUser = BaseUser()
        }
    }

    fun testInitializeUser() {
        val user = UserStoreRequestManager<BaseUser?>(client, MockBuilder(client!!.requestFactory!!.transport,
                client!!.jsonFactory, "mockAppKey", "mockAppSecret", null))
        assertNotNull(user)
        assertEquals(client, user.getClient())
        assertEquals(client!!.kinveyRequestInitializer, user.getClient()!!.kinveyRequestInitializer)
    }

    fun testInitializeUserNullClient() {
        try {
            val user = UserStoreRequestManager<BaseUser?>(null, MockBuilder(client!!.requestFactory!!.transport,
                    client!!.jsonFactory, "mockAppKey", "mockAppSecret", null))
            fail("NullPointerException should be thrown")
        } catch (ex: NullPointerException) {
        }
    }

    fun testInitializeNoBuilder() {
        try {
            val user: UserStoreRequestManager<*> = UserStoreRequestManager<Any?>(client, null)
            fail("NullPointerException should be thrown")
        } catch (ex: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    fun testLoginKinveyUserNullUsername() {
        initializeRequestManager(false)
        try {
            requestManager!!.loginBlocking(null, "myPassword").execute()
            fail("NullPointerException should be thrown")
        } catch (ex: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    fun testLoginKinveyUserNullPassword() {
        initializeRequestManager(false)
        try {
            requestManager!!.loginBlocking("myUserName", null).execute()
            fail("NullPointerException should be thrown")
        } catch (ex: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    fun testLoginFacebookUserNullArguments() {
        initializeRequestManager(false)
        try {
            requestManager!!.login(Type.FACEBOOK, null).execute()
            fail("NullPointerException should be thrown")
        } catch (ex: IllegalArgumentException) {
            assertEquals("Parameter specified as non-null is null: method com.kinvey.java.store.UserStoreRequestManager.login, parameter args", ex.message)
        }
    }

    @Throws(IOException::class)
    fun testLoginFacebookTooFewArguments() {
        initializeRequestManager(false)
        try {
            requestManager!!.login(Type.FACEBOOK, *arrayOf<String?>()).execute()
            fail("IllegalArgumentException should be thrown")
        } catch (ex: IllegalArgumentException) {
        }
    }

    @Throws(IOException::class)
    fun testLoginFacebookTooManyArguments() {
        initializeRequestManager(false)
        try {
            requestManager!!.login(Type.FACEBOOK, *arrayOf<String?>("arg1", "arg2")).execute()
            fail("IllegalArgumentException should be thrown")
        } catch (ex: IllegalArgumentException) {
        }
    }

    @Throws(IOException::class)
    fun testDeleteHardDeleteTrue() {
        initializeRequestManager(true)
        val baseUser = requestManager!!.getClient()!!.activeUser
        baseUser!!.id = "testUser"
        val del = requestManager!!.deleteBlocking(true)
        assertEquals(requestManager!!.getClient()!!.activeUser!!.id, del[USER_ID].toString())
        assertEquals(true, del["hard"])
    }

    @Throws(IOException::class)
    fun testDeleteHardDeleteFalse() {
        initializeRequestManager(true)
        requestManager!!.getClient()!!.activeUser!!.id = "testUser"
        val del = requestManager!!.deleteBlocking(false)
        assertEquals(requestManager!!.getClient()!!.activeUser!!.id, del[USER_ID].toString())
        assertEquals(false, del["hard"])
        assertEquals("DELETE", del.requestMethod)
    }

    @Throws(IOException::class)
    fun testDeleteNullUser() {
        initializeRequestManager(false)
        try {
            val del = requestManager!!.deleteBlocking(true)
            fail("NullPointerException should be thrown.")
        } catch (ex: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    fun testRetrieve() {
        initializeRequestManager(true)
        requestManager!!.getClient()!!.activeUser!!.id = "testUser"
        val ret: Retrieve<*> = requestManager!!.retrieveBlocking()
        assertEquals(requestManager!!.getClient()!!.activeUser!!.id, ret[USER_ID].toString())
        assertEquals("GET", ret.requestMethod)
    }

    @Throws(IOException::class)
    fun testRetrieveNullUser() {
        initializeRequestManager(false)
        try {
            val ret: Retrieve<*> = requestManager!!.retrieveBlocking()
            fail("NullPointerException should be thrown.")
        } catch (ex: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    fun testUpdate() {
        initializeRequestManager(true)
        requestManager!!.getClient()!!.activeUser!!.id = "testUser"
        val update: Update<*> = requestManager!!.updateBlocking()
        assertEquals(requestManager!!.getClient()!!.activeUser!!.id, update[USER_ID].toString())
        assertEquals("PUT", update.requestMethod)
    }

    @Throws(IOException::class)
    fun testUpdateNullUser() {
        initializeRequestManager(false)
        try {
            val update: Update<*> = requestManager!!.updateBlocking()
            fail("NullPointerException should be thrown.")
        } catch (ex: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    fun testResetPassword() {
        initializeRequestManager(true)
        val baseUser = requestManager!!.getClient()!!.activeUser
        baseUser!!.id = "testUser"
        requestManager!!.getClient()!!.activeUser!!.username = "test"
        val pwd = requestManager!!.resetPasswordBlocking(requestManager!!.getClient()!!.activeUser!!.username)
        assertEquals(requestManager!!.getClient()!!.activeUser!!.username, pwd[USER_ID].toString())
        assertEquals("POST", pwd.requestMethod)
    }

    @Throws(IOException::class)
    fun testResetPasswordNullUser() {
        initializeRequestManager(false)
        try {
            val pwd = requestManager!!.resetPasswordBlocking(null)
            fail("NullPointerException should be thrown.")
        } catch (ex: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    fun testEmailVerification() {
        initializeRequestManager(true)
        requestManager!!.getClient()!!.activeUser!!.id = "testUser"
        val email = requestManager!!.sendEmailVerificationBlocking()
        assertEquals(requestManager!!.getClient()!!.activeUser!!.id, email[USER_ID].toString())
        assertEquals("POST", email.requestMethod)
    }

    @Throws(IOException::class)
    fun testEmailVerificationNullUser() {
        initializeRequestManager(false)
        try {
            val email = requestManager!!.sendEmailVerificationBlocking()
            fail("NullPointerException should be thrown.")
        } catch (ex: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    fun testUserCustomVersion() {
        initializeRequestManager(true)
        requestManager!!.getClient()!!.activeUser!!.id = "testUser"
        requestManager!!.getClient()!!.clientAppVersion = "1.2.3"
        val request: Retrieve<*> = requestManager!!.retrieveBlocking()
        val header = request.getRequestHeaders()[X_KINVEY_CLIENT_APP_VERSION]
        assertEquals("1.2.3", header as String?)
    }

    @Throws(IOException::class)
    fun testUserCustomVesionAsNumber() {
        initializeRequestManager(true)
        requestManager!!.getClient()!!.activeUser!!.id = "testUser"
        requestManager!!.getClient()!!.setClientAppVersion(1, 2, 3)
        val request: Retrieve<*> = requestManager!!.retrieveBlocking()
        val header = request.getRequestHeaders()[X_KINVEY_CLIENT_APP_VERSION]
        assertEquals("1.2.3", header as String?)
    }

    @Throws(IOException::class)
    fun testUserCustomHeader() {
        initializeRequestManager(true)
        requestManager!!.getClient()!!.activeUser!!.id = "testUser"
        val custom = GenericJson()
        custom["First"] = 1
        custom["Second"] = "two"
        requestManager!!.getClient()!!.setCustomRequestProperties(custom)
        val request: Retrieve<*> = requestManager!!.retrieveBlocking()
        val header = request.getRequestHeaders()[X_KINVEY_CUSTOM_REQUEST_PROPERTIES]
        assertEquals("{\"First\":1,\"Second\":\"two\"}", header as String?)
    }

    @Throws(IOException::class)
    fun testUserCustomHeaderOverload() {
        initializeRequestManager(true)
        requestManager!!.getClient()!!.activeUser!!.id = "testUser"
        requestManager!!.getClient()!!.setCustomRequestProperty("First", 1)
        requestManager!!.getClient()!!.setCustomRequestProperty("Second", "two")
        val request: Retrieve<*> = requestManager!!.retrieveBlocking()
        val header = request.getRequestHeaders()[X_KINVEY_CUSTOM_REQUEST_PROPERTIES]
        assertEquals("{\"First\":1,\"Second\":\"two\"}", header as String?)
    }

    @Throws(IOException::class)
    fun testUserCustomVersionNull() {
        initializeRequestManager(true)
        requestManager!!.getClient()!!.activeUser!!.id = "testUser"
        requestManager!!.getClient()!!.clientAppVersion = null
        val request: Retrieve<*> = requestManager!!.retrieveBlocking()
        val header = request.getRequestHeaders()[X_KINVEY_CLIENT_APP_VERSION]
        assertEquals(null, header)
    }

    @Throws(IOException::class)
    fun testUserCustomHeaderNull() {
        initializeRequestManager(true)
        requestManager!!.getClient()!!.activeUser!!.id = "testUser"
        requestManager!!.getClient()!!.clearCustomRequestProperties()
        val request: Retrieve<*> = requestManager!!.retrieveBlocking()
        val header = request.getRequestHeaders()[X_KINVEY_CUSTOM_REQUEST_PROPERTIES]
        assertEquals(null, header)
    }

    @Throws(IOException::class)
    fun testCustomMICBase() {
        initializeRequestManager(false)
        client!!.micHostName = "https://www.google.com"
        try {
            client!!.micHostName = "http://www.google.com"
            fail("Library should throw an exception when setting non https base url for MIC")
        } catch (e: Exception) {
        }


        // Construct the full token path. Might be affected by the MIC API version.


        val fullTokenPath = "https://www.google.com/" + client!!.micApiVersion + "/oauth/token"
        val getToken = requestManager!!.getMICToken("myCODE", null)
        assertEquals(fullTokenPath, getToken.buildHttpRequest()!!.url.toString())
    }

    @Throws(IOException::class)
    fun testMICLoginWithAccessToken() {
        requestManager = UserStoreRequestManager<BaseUser?>(getClient(MockHttpForMIC()), Builder(MockHttpForMIC(),
                GsonFactory(), "https://baas.kinvey.com", "mockAppKey", "mockAppSecret", null))
        val token = requestManager!!.getMICToken("MyToken", null)

        //check
/*        if (getClient().isUserLoggedIn()) {
            requestManager.logout().execute();
        }*/

        val ret = requestManager!!.loginMobileIdentityBlocking(token.execute()!!["access_token"].toString()).execute()
        client!!.activeUser = ret
        assertEquals(true, client!!.isUserLoggedIn)
    }

    @Throws(IOException::class)
    fun testMICAPIVersionAppendsV() {
        initializeRequestManager(false)
        client!!.micApiVersion = "2"
        assertEquals(client!!.micApiVersion, "v2")
    }

    @Throws(IOException::class, NoSuchFieldException::class, IllegalAccessException::class)
    fun testClientAppVersionHeader() {
        val clientAppVersion = "1.2.3"
        client!!.clientAppVersion = clientAppVersion
        val user: UserStoreRequestManager<*> = UserStoreRequestManager<Any?>(client, createBuilder(client))
        assertNotNull(user)
        val loginRequest: LoginRequest = user.createBlocking("test_name", "test_login").buildAuthRequest()
        val kinveyAuthRequestField: Field = loginRequest.javaClass.getDeclaredField("request") //NoSuchFieldException

        kinveyAuthRequestField.isAccessible = true
        val request = kinveyAuthRequestField.get(loginRequest) as KinveyAuthRequest<*>
        val kinveyHeadersField: Field? = request.javaClass.getDeclaredField("kinveyHeaders") //NoSuchFieldException

        kinveyHeadersField!!.isAccessible = true
        val kinveyHeaders = kinveyHeadersField.get(request) as KinveyHeaders
        val clientAppVersionHeader = kinveyHeaders["X-Kinvey-Client-App-Version"] as String?
        assertEquals(clientAppVersion, clientAppVersionHeader)
    }

    @Throws(IOException::class, NoSuchFieldException::class, IllegalAccessException::class)
    fun testClientAppVersionHeaderDefault() {
        val user: UserStoreRequestManager<*> = UserStoreRequestManager<Any?>(client, createBuilder(client))
        assertNotNull(user)
        val loginRequest: LoginRequest = user.createBlocking("test_name", "test_login").buildAuthRequest()
        val kinveyAuthRequestField: Field = loginRequest.javaClass.getDeclaredField("request") //NoSuchFieldException

        kinveyAuthRequestField.isAccessible = true
        val request = kinveyAuthRequestField.get(loginRequest) as KinveyAuthRequest<*>
        val kinveyHeadersField: Field? = request.javaClass.getDeclaredField("kinveyHeaders") //NoSuchFieldException

        kinveyHeadersField!!.isAccessible = true
        val kinveyHeaders = kinveyHeadersField.get(request) as KinveyHeaders
        val clientAppVersionHeader = kinveyHeaders["X-Kinvey-Client-App-Version"] as String?
        assertNull(clientAppVersionHeader)
    }

    private fun createBuilder(client: AbstractClient<*>?): Builder<*>? {
        val appKey = (client!!.kinveyRequestInitializer as KinveyClientRequestInitializer?)!!.appKey
        val appSecret = (client.kinveyRequestInitializer as KinveyClientRequestInitializer?)!!.appSecret
        return Builder<Any?>(client.requestFactory!!.transport,
                client.jsonFactory, client.baseUrl, appKey, appSecret, null)
    }

    companion object {
        private val X_KINVEY_CUSTOM_REQUEST_PROPERTIES: String? = "X-Kinvey-Custom-Request-Properties"
        private val X_KINVEY_CLIENT_APP_VERSION: String? = "X-Kinvey-Client-App-Version"
        private val USER_ID: String? = "userID"
    }
}