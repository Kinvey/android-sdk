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
package com.kinvey.java.auth;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import junit.framework.TestCase;

import java.io.IOException;

import static com.kinvey.java.auth.ThirdPartyIdentity.Type.FACEBOOK;
import static com.kinvey.java.auth.ThirdPartyIdentity.Type.GOOGLE;
import static com.kinvey.java.auth.ThirdPartyIdentity.Type.TWITTER;
import static com.kinvey.java.auth.ThirdPartyIdentity.Type.LINKED_IN;

/**
 * @author m0rganic
 * @since 2.0
 */
public class ThirdPartyIdentityTest extends TestCase {


    public void testNullPassedToCreateIdentity() {

        try {
            ThirdPartyIdentity.createThirdPartyIdentity(null, "testtokenstring");
            fail("createThirdPartyIdentity should throw an exception when null is passed in");
        } catch (NullPointerException e) {
            assertEquals("Type argument must not be null", e.getMessage());
        }

        try {
            ThirdPartyIdentity.createThirdPartyIdentity(FACEBOOK, null);
            fail("createThirdPartyIdentity should throw an exception when null is passed in");
        } catch (NullPointerException e) {
            assertEquals("facebook", "Params must not be null", e.getMessage());
        }

    }

    public void testEmptyAccessToken() {
        String testaccesstoken = "";
        String testaccessecret = "testaccessecret";
        String testconsumerkey = "testconsumerkey";
        String testconsumersecret = "testconsumersecret";

        try {
            ThirdPartyIdentity.createThirdPartyIdentity(FACEBOOK, testaccesstoken);
            fail("createThirdPartyIdentity for facebook should throw an exception when empty accessToken is passed");
        } catch (IllegalArgumentException e) {
            assertEquals("facebook", "accessToken must not be empty", e.getMessage());
        }

        try {
            ThirdPartyIdentity.createThirdPartyIdentity(GOOGLE, testaccesstoken);
            fail("createThirdPartyIdentity for google should throw an exception when empty accessToken is passed");
        } catch (IllegalArgumentException e) {
            assertEquals("google", "accessToken must not be empty", e.getMessage());
        }

        try {
            ThirdPartyIdentity.createThirdPartyIdentity(LINKED_IN, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret);
            fail("createThirdPartyIdentity for linkedIn should throw an exception when empty accessToken is passed");
        } catch (IllegalArgumentException e) {
            assertEquals("linkedIn", "accessToken must not be empty", e.getMessage());
        }

        try {
            ThirdPartyIdentity.createThirdPartyIdentity(TWITTER, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret);
            fail("createThirdPartyIdentity for twitter should throw an exception when empty accessToken is passed");
        } catch (IllegalArgumentException e) {
            assertEquals("twitter", "accessToken must not be empty", e.getMessage());
        }

    }


    public void testEmptyParamOauth1() {
        String testaccesstoken = "testaccesstoken";
        String testaccessecret = "";
        String testconsumerkey = "testconsumerkey";
        String testconsumersecret = "testconsumersecret";

        //  empty accessSecret
        try {
            ThirdPartyIdentity.createThirdPartyIdentity(LINKED_IN, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret);
            fail("createThirdPartyIdentity for linkedIn should throw an exception when empty accessSecret is passed");
        } catch (IllegalArgumentException e) {
            assertEquals("linkedIn", "accessTokenSecret must not be empty", e.getMessage());
        }

        try {
            ThirdPartyIdentity.createThirdPartyIdentity(TWITTER, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret);
            fail("createThirdPartyIdentity for twitter should throw an exception when empty accessTokenSecret is passed");
        } catch (IllegalArgumentException e) {
            assertEquals("twitter", "accessTokenSecret must not be empty", e.getMessage());
        }


        // empty consumerKey
        testaccessecret = "testaccesssecret";
        testconsumerkey = "";
        try {
            ThirdPartyIdentity.createThirdPartyIdentity(LINKED_IN, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret);
            fail("createThirdPartyIdentity for linkedIn should throw an exception when empty consumerKey is passed");
        } catch (IllegalArgumentException e) {
            assertEquals("linkedIn", "consumerKey must not be empty", e.getMessage());
        }

        try {
            ThirdPartyIdentity.createThirdPartyIdentity(TWITTER, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret);
            fail("createThirdPartyIdentity for twitter should throw an exception when empty consumerKey is passed");
        } catch (IllegalArgumentException e) {
            assertEquals("twitter", "consumerKey must not be empty", e.getMessage());
        }


        // empty consumerSecret
        testconsumerkey = "testconsumerkey";
        testconsumersecret = "";
        try {
            ThirdPartyIdentity.createThirdPartyIdentity(LINKED_IN, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret);
            fail("createThirdPartyIdentity for linkedIn should throw an exception when empty consumersecret is passed");
        } catch (IllegalArgumentException e) {
            assertEquals("linkedIn", "consumerSecret must not be empty", e.getMessage());
        }

        try {
            ThirdPartyIdentity.createThirdPartyIdentity(TWITTER, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret);
            fail("createThirdPartyIdentity for twitter should throw an exception when empty consumersecret is passed");
        } catch (IllegalArgumentException e) {
            assertEquals("twitter", "consumerSecret must not be empty", e.getMessage());
        }

    }


    public void testThirdPartyIdentityWithGsonGenerator() throws IOException {

        JsonFactory jsonFactory = new GsonFactory();
        String testaccesstoken = "a";
        String testaccessecret = "b";
        String testconsumerkey = "c";
        String testconsumersecret = "d";

        ThirdPartyIdentity identity = ThirdPartyIdentity.createThirdPartyIdentity(FACEBOOK, testaccesstoken);
        assertEquals("facebook", "{\"_socialIdentity\":{\"facebook\":{\"access_token\":\"a\"}}}", jsonFactory.toString(identity));

        identity = ThirdPartyIdentity.createThirdPartyIdentity(TWITTER, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret);
        assertEquals("twitter", "{\"_socialIdentity\":{\"twitter\":{\"access_token\":\"a\",\"access_token_secret\":\"b\",\"consumer_key\":\"c\",\"consumer_secret\":\"d\"}}}", jsonFactory.toString(identity));

        identity = ThirdPartyIdentity.createThirdPartyIdentity(LINKED_IN, testaccesstoken, testaccessecret, testconsumerkey, testconsumersecret);
        assertEquals("linked_id", "{\"_socialIdentity\":{\"linkedIn\":{\"access_token\":\"a\",\"access_token_secret\":\"b\",\"consumer_key\":\"c\",\"consumer_secret\":\"d\"}}}", jsonFactory.toString(identity));

        identity = ThirdPartyIdentity.createThirdPartyIdentity(GOOGLE, testaccesstoken);
        assertEquals("google", "{\"_socialIdentity\":{\"google\":{\"access_token\":\"a\"}}}", jsonFactory.toString(identity));


    }

}
