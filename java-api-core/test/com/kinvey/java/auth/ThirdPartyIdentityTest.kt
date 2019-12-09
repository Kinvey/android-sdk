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
package com.kinvey.java.auth

import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.kinvey.java.auth.ThirdPartyIdentity.Companion.createThirdPartyIdentity
import com.kinvey.java.auth.ThirdPartyIdentity.Type.*
import junit.framework.TestCase
import java.io.IOException

/**
 * @author m0rganic
 * @since 2.0
 */
class ThirdPartyIdentityTest : TestCase() {
    fun testNullPassedToCreateIdentity() {
        try {
            createThirdPartyIdentity(null, "testtokenstring")
            fail("createThirdPartyIdentity should throw an exception when null is passed in")
        } catch (e: NullPointerException) {
            assertEquals("Type argument must not be null", e.message)
        }

        //Ignored, because vararg parameters are, as a rule, never nullable, because in Java there is no good way
        //to distinguish between passing null as the entire vararg array versus passing null
        //as a single element of a non-null vararg array.

        //try {
        //    ThirdPartyIdentity.createThirdPartyIdentity(FACEBOOK, null);
        //    fail("createThirdPartyIdentity should throw an exception when null is passed in");
        //} catch (NullPointerException e) {
        //    assertEquals("facebook", "Params must not be null", e.getMessage());
        //}

    }

    fun testEmptyAccessToken() {
        val testaccesstoken = ""
        val testaccessecret = "testaccessecret"
        val testconsumerkey = "testconsumerkey"
        val testconsumersecret = "testconsumersecret"
        try {
            createThirdPartyIdentity(FACEBOOK, testaccesstoken)
            fail("createThirdPartyIdentity for facebook should throw an exception when empty accessToken is passed")
        } catch (e: IllegalArgumentException) {
            assertEquals("facebook", "accessToken must not be empty", e.message)
        }
        try {
            createThirdPartyIdentity(GOOGLE, testaccesstoken)
            fail("createThirdPartyIdentity for google should throw an exception when empty accessToken is passed")
        } catch (e: IllegalArgumentException) {
            assertEquals("google", "accessToken must not be empty", e.message)
        }
        try {
            createThirdPartyIdentity(LINKED_IN, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret)
            fail("createThirdPartyIdentity for linkedIn should throw an exception when empty accessToken is passed")
        } catch (e: IllegalArgumentException) {
            assertEquals("linkedIn", "accessToken must not be empty", e.message)
        }
        try {
            createThirdPartyIdentity(TWITTER, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret)
            fail("createThirdPartyIdentity for twitter should throw an exception when empty accessToken is passed")
        } catch (e: IllegalArgumentException) {
            assertEquals("twitter", "accessToken must not be empty", e.message)
        }
    }

    fun testEmptyParamOauth1() {
        val testaccesstoken = "testaccesstoken"
        var testaccessecret = ""
        var testconsumerkey = "testconsumerkey"
        var testconsumersecret = "testconsumersecret"

        //  empty accessSecret


        try {
            createThirdPartyIdentity(LINKED_IN, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret)
            fail("createThirdPartyIdentity for linkedIn should throw an exception when empty accessSecret is passed")
        } catch (e: IllegalArgumentException) {
            assertEquals("linkedIn", "accessTokenSecret must not be empty", e.message)
        }
        try {
            createThirdPartyIdentity(TWITTER, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret)
            fail("createThirdPartyIdentity for twitter should throw an exception when empty accessTokenSecret is passed")
        } catch (e: IllegalArgumentException) {
            assertEquals("twitter", "accessTokenSecret must not be empty", e.message)
        }


        // empty consumerKey

        testaccessecret = "testaccesssecret"
        testconsumerkey = ""
        try {
            createThirdPartyIdentity(LINKED_IN, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret)
            fail("createThirdPartyIdentity for linkedIn should throw an exception when empty consumerKey is passed")
        } catch (e: IllegalArgumentException) {
            assertEquals("linkedIn", "consumerKey must not be empty", e.message)
        }
        try {
            createThirdPartyIdentity(TWITTER, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret)
            fail("createThirdPartyIdentity for twitter should throw an exception when empty consumerKey is passed")
        } catch (e: IllegalArgumentException) {
            assertEquals("twitter", "consumerKey must not be empty", e.message)
        }


        // empty consumerSecret

        testconsumerkey = "testconsumerkey"
        testconsumersecret = ""
        try {
            createThirdPartyIdentity(LINKED_IN, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret)
            fail("createThirdPartyIdentity for linkedIn should throw an exception when empty consumersecret is passed")
        } catch (e: IllegalArgumentException) {
            assertEquals("linkedIn", "consumerSecret must not be empty", e.message)
        }
        try {
            createThirdPartyIdentity(TWITTER, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret)
            fail("createThirdPartyIdentity for twitter should throw an exception when empty consumersecret is passed")
        } catch (e: IllegalArgumentException) {
            assertEquals("twitter", "consumerSecret must not be empty", e.message)
        }
    }

    @Throws(IOException::class)
    fun testThirdPartyIdentityWithGsonGenerator() {
        val jsonFactory: JsonFactory = GsonFactory()
        val testaccesstoken = "a"
        val testaccessecret = "b"
        val testconsumerkey = "c"
        val testconsumersecret = "d"
        var identity = createThirdPartyIdentity(FACEBOOK, testaccesstoken)
        assertEquals("facebook", "{\"_socialIdentity\":{\"facebook\":{\"access_token\":\"a\"}}}", jsonFactory.toString(identity))
        identity = createThirdPartyIdentity(TWITTER, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret)
        assertEquals("twitter", "{\"_socialIdentity\":{\"twitter\":{\"access_token\":\"a\",\"access_token_secret\":\"b\",\"consumer_key\":\"c\",\"consumer_secret\":\"d\"}}}", jsonFactory.toString(identity))
        identity = createThirdPartyIdentity(LINKED_IN, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret)
        assertEquals("linked_id", "{\"_socialIdentity\":{\"linkedIn\":{\"access_token\":\"a\",\"access_token_secret\":\"b\",\"consumer_key\":\"c\",\"consumer_secret\":\"d\"}}}", jsonFactory.toString(identity))
        identity = createThirdPartyIdentity(GOOGLE, testaccesstoken)
        assertEquals("google", "{\"_socialIdentity\":{\"google\":{\"access_token\":\"a\"}}}", jsonFactory.toString(identity))
    }
}