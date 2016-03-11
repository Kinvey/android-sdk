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
package com.kinvey.java;

import java.io.IOException;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.gson.GsonFactory;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.auth.ThirdPartyIdentity;
import com.kinvey.java.core.KinveyMockUnitTest;
import com.kinvey.java.dto.User;
import com.kinvey.java.store.UserStore;
import com.kinvey.java.store.requests.user.Delete;
import com.kinvey.java.store.requests.user.EmailVerification;
import com.kinvey.java.store.requests.user.GetMICAccessToken;
import com.kinvey.java.store.requests.user.ResetPassword;
import com.kinvey.java.store.requests.user.Retrieve;
import com.kinvey.java.store.requests.user.Update;
import com.kinvey.java.testing.MockHttpForMIC;
import com.kinvey.java.testing.MockKinveyAuthRequest;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class UserTest extends KinveyMockUnitTest {

    private UserStore<User> currentUser;

    private void initializeUser() {
        currentUser = new UserStore<User>(getClient(), User.class, new MockKinveyAuthRequest.MockBuilder(getClient().getRequestFactory().getTransport(),
                getClient().getJsonFactory(), "mockAppKey","mockAppSecret",null));
    }

    public void testInitializeUser() {
        UserStore<User> user = new UserStore<User>(getClient(), User.class, new MockKinveyAuthRequest.MockBuilder(getClient().getRequestFactory().getTransport(),
                getClient().getJsonFactory(), "mockAppKey","mockAppSecret",null));
        assertNotNull(user);
        assertEquals(getClient(),user.getClient());
        assertEquals(getClient().getKinveyRequestInitializer(), user.getClient().getKinveyRequestInitializer());
    }

    public void testInitializeUserNullClient() {
        try {
            UserStore<User> user = new UserStore<User>(null, User.class, new MockKinveyAuthRequest.MockBuilder(getClient().getRequestFactory().getTransport(),
                    getClient().getJsonFactory(), "mockAppKey","mockAppSecret",null));
            fail("NullPointerException should be thrown");
        } catch (NullPointerException ex) {}
    }

    public void testInitializeNoBuilder() {
        try {
            UserStore<User> user = new UserStore<User>(getClient(), User.class, null);
            fail("NullPointerException should be thrown");
        } catch (NullPointerException ex) {}
    }


    public void testLoginKinveyUserNullUsername() throws IOException {
        initializeUser();
        try {
            currentUser.loginBlocking(null, "myPassword").execute();
            fail("NullPointerException should be thrown");
        } catch (NullPointerException ex) {}
    }

    public void testLoginKinveyUserNullPassword() throws IOException {
        initializeUser();
        try {
            currentUser.loginBlocking("myUserName", null).execute();
            fail("NullPointerException should be thrown");
        } catch (NullPointerException ex) {}
    }


    public void testLoginFacebookUserNullArguments() throws IOException {
        initializeUser();
        try {
            currentUser.login(ThirdPartyIdentity.Type.FACEBOOK, null).execute();
            fail("NullPointerException should be thrown");
        } catch (NullPointerException ex) {}
    }

    public void testLoginFacebookTooFewArguments() throws IOException {
        initializeUser();
        try {
            currentUser.login(ThirdPartyIdentity.Type.FACEBOOK, new String[] {}).execute();
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException ex) {}
    }

    public void testLoginFacebookTooManyArguments() throws IOException {
        initializeUser();
        try {
            currentUser.login(ThirdPartyIdentity.Type.FACEBOOK, new String[] {"arg1","arg2"}).execute();
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException ex) {}
    }


    public void testDeleteHardDeleteTrue() throws IOException {
        initializeUser();
        currentUser.getCurrentUser().setId("testUser");
        Delete del = currentUser.deleteBlocking(true);
        assertEquals(currentUser.getCurrentUser().getId(), del.get("userID").toString());
        assertEquals(true, del.get("hard"));
    }

    public void testDeleteHardDeleteFalse() throws IOException {
        initializeUser();
        currentUser.getCurrentUser().setId("testUser");
        Delete del = currentUser.deleteBlocking(false);
        assertEquals(currentUser.getCurrentUser().getId(),del.get("userID").toString());
        assertEquals(false,del.get("hard"));
        assertEquals("DELETE",del.getRequestMethod());
    }

    public void testDeleteNullUser() throws IOException {
        initializeUser();
        try {
            Delete del = currentUser.deleteBlocking(true);
            fail("NullPointerException should be thrown.");
        } catch (NullPointerException ex) {}
    }

    public void testRetrieve() throws IOException {
        initializeUser();
        currentUser.getCurrentUser().setId("testUser");
        Retrieve ret = currentUser.retrieveBlocking();
        assertEquals(currentUser.getCurrentUser().getId(),ret.get("userID").toString());
        assertEquals("GET", ret.getRequestMethod());
    }

    public void testRetrieveNullUser() throws IOException {
        initializeUser();
        try {
            Retrieve ret = currentUser.retrieveBlocking();
            fail("NullPointerException should be thrown.");
        } catch (NullPointerException ex) {}
    }

    public void testUpdate() throws IOException {
        initializeUser();
        currentUser.getCurrentUser().setId("testUser");
        Update update = currentUser.updateBlocking();
        assertEquals(currentUser.getCurrentUser().getId(),update.get("userID").toString());
        assertEquals("PUT", update.getRequestMethod());
    }

    public void testUpdateNullUser() throws IOException {
        initializeUser();
        try {
            Update update = currentUser.updateBlocking();
            fail("NullPointerException should be thrown.");
        } catch (NullPointerException ex) {}
    }

    public void testResetPassword() throws IOException {
        initializeUser();
        currentUser.getCurrentUser().setId("testUser");
        currentUser.getCurrentUser().setUsername("test");
        ResetPassword pwd = currentUser.resetPasswordBlocking(currentUser.getCurrentUser().getUsername());
        assertEquals(currentUser.getCurrentUser().getUsername(),pwd.get("userID").toString());
        assertEquals("POST", pwd.getRequestMethod());
    }

    public void testResetPasswordNullUser() throws IOException {
        initializeUser();
        try {
            ResetPassword pwd = currentUser.resetPasswordBlocking(null);
            fail("NullPointerException should be thrown.");
        } catch (NullPointerException ex) {}
    }

    public void testEmailVerification() throws IOException {
        initializeUser();
        currentUser.getCurrentUser().setId("testUser");
        EmailVerification email = currentUser.sendEmailVerificationBlocking();
        assertEquals(currentUser.getCurrentUser().getId(),email.get("userID").toString());
        assertEquals("POST", email.getRequestMethod());
    }

    public void testEmailVerificationNullUser() throws IOException {
        initializeUser();
        try {
            EmailVerification email = currentUser.sendEmailVerificationBlocking();
            fail("NullPointerException should be thrown.");
        } catch (NullPointerException ex) {}
    }
    
    public void testUserCustomVersion() throws IOException {
        initializeUser();
        currentUser.getCurrentUser().setId("testUser");
    	currentUser.getClient().setClientAppVersion("1.2.3");
    	Retrieve request = currentUser.retrieveBlocking();
    	Object header = request.getRequestHeaders().get("X-Kinvey-Client-App-Version");
    	assertEquals("1.2.3", (String) header);
    }

    public void testUserCustomVesionAsNumber() throws IOException {
        initializeUser();
        currentUser.getCurrentUser().setId("testUser");
        currentUser.getClient().setClientAppVersion(1, 2, 3);
        Retrieve request = currentUser.retrieveBlocking();
        Object header = request.getRequestHeaders().get("X-Kinvey-Client-App-Version");
        assertEquals("1.2.3", (String) header);

    }
    
    public void testUserCustomHeader() throws IOException {
        initializeUser();
        currentUser.getCurrentUser().setId("testUser");
    	GenericJson custom = new GenericJson();
    	custom.put("First", 1);
    	custom.put("Second", "two");
    	currentUser.getClient().setCustomRequestProperties(custom);
    	Retrieve request = currentUser.retrieveBlocking();
    	Object header = request.getRequestHeaders().get("X-Kinvey-Custom-Request-Properties");
    	assertEquals("{\"First\":1,\"Second\":\"two\"}", (String) header);    	
    	
    }

    public void testUserCustomHeaderOverload() throws IOException {
        initializeUser();
        currentUser.getCurrentUser().setId("testUser");

        currentUser.getClient().setCustomRequestProperty("First", 1);
        currentUser.getClient().setCustomRequestProperty("Second", "two");

        Retrieve request = currentUser.retrieveBlocking();
        Object header = request.getRequestHeaders().get("X-Kinvey-Custom-Request-Properties");
        assertEquals("{\"First\":1,\"Second\":\"two\"}", (String) header);

    }
    
    public void testUserCustomVersionNull() throws IOException {
        initializeUser();
        currentUser.getCurrentUser().setId("testUser");
    	currentUser.getClient().setClientAppVersion(null);
    	Retrieve request = currentUser.retrieveBlocking();
    	Object header = request.getRequestHeaders().get("X-Kinvey-Client-App-Version");
    	assertEquals(null, header);    	
    }
    
    public void testUserCustomHeaderNull() throws IOException {
        initializeUser();
        currentUser.getCurrentUser().setId("testUser");
        currentUser.getClient().clearCustomRequestProperties();
    	Retrieve request = currentUser.retrieveBlocking();
    	Object header = request.getRequestHeaders().get("X-Kinvey-Custom-Request-Properties");
    	assertEquals(null, header);      	
    }
    
    public void testCustomMICBase() throws IOException{
    	initializeUser();
    	getClient().userStore().setMICHostName("https://www.google.com");
    	
    	try{
    		getClient().userStore().setMICHostName("http://www.google.com");
    		fail("Library should throw an exception when setting non https base url for MIC");
    	}catch(Exception e){}


    	
    	GetMICAccessToken getToken = getClient().userStore().getMICToken("myCODE");
    	assertEquals("https://www.google.com/oauth/token", getToken.buildHttpRequest().getUrl().toString());
    }
    
    public void testMICLoginWithAccessToken() throws IOException{
    	
    	currentUser = new UserStore<>(getClient(new MockHttpForMIC()), User.class, new KinveyAuthRequest.Builder(new MockHttpForMIC(),
                new GsonFactory(),"https://baas.kinvey.com",  "mockAppKey","mockAppSecret",null));
    	
    	GetMICAccessToken token = currentUser.getMICToken("MyToken");
    	GenericJson result =  (GenericJson) token.execute();
	
		User ret =  currentUser.loginMobileIdentityBlocking(result.get("access_token").toString()).execute();
		  
    	assertEquals(true, currentUser.isUserLoggedIn());
    	
    }

    public void testMICAPIVersionAppendsV() throws IOException{
        initializeUser();
        currentUser.setMICApiVersion("2");
        assertEquals(currentUser.MICApiVersion, "v2");
    }


}