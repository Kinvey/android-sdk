/*
 * Copyright (c) 2013 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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
