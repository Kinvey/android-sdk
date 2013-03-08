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
package com.kinvey.java;

import java.io.IOException;

import com.kinvey.java.User;
import com.kinvey.java.auth.ThirdPartyIdentity;
import com.kinvey.java.core.KinveyMockUnitTest;
import com.kinvey.java.testing.MockKinveyAuthRequest;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class UserTest extends KinveyMockUnitTest {

    private User currentUser;

    private void initializeUser() {
        currentUser = new User(mockClient, new MockKinveyAuthRequest.MockBuilder(mockClient.getRequestFactory().getTransport(),
                mockClient.getJsonFactory(), "mockAppKey","mockAppSecret",null));
    }

    public void testInitializeUser() {
        User user = new User(mockClient, new MockKinveyAuthRequest.MockBuilder(mockClient.getRequestFactory().getTransport(),
                mockClient.getJsonFactory(), "mockAppKey","mockAppSecret",null));
        assertNotNull(user);
        assertEquals(mockClient,user.getClient());
        assertEquals(mockClient.getKinveyRequestInitializer(), user.getClient().getKinveyRequestInitializer());
    }

    public void testInitializeUserNullClient() {
        try {
            User user = new User(null, new MockKinveyAuthRequest.MockBuilder(mockClient.getRequestFactory().getTransport(),
                    mockClient.getJsonFactory(), "mockAppKey","mockAppSecret",null));
            fail("NullPointerException should be thrown");
        } catch (NullPointerException ex) {}
    }

    public void testInitializeNoBuilder() {
        try {
            User user = new User(mockClient, null);
            fail("NullPointerException should be thrown");
        } catch (NullPointerException ex) {}
    }


    public void testLoginKinveyUserNullUsername() throws IOException {
        initializeUser();
        try {
            currentUser.login(null,"myPassword").execute();
            fail("NullPointerException should be thrown");
        } catch (NullPointerException ex) {}
    }

    public void testLoginKinveyUserNullPassword() throws IOException {
        initializeUser();
        try {
            currentUser.login("myUserName", null).execute();
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
        currentUser.setId("testUser");
        User.Delete del = currentUser.delete(true);
        assertEquals(currentUser.getId(), del.get("userID").toString());
        assertEquals(true,del.get("hard"));
    }

    public void testDeleteHardDeleteFalse() throws IOException {
        initializeUser();
        currentUser.setId("testUser");
        User.Delete del = currentUser.delete(false);
        assertEquals(currentUser.getId(),del.get("userID").toString());
        assertEquals(false,del.get("hard"));
        assertEquals("DELETE",del.getRequestMethod());
    }

    public void testDeleteNullUser() throws IOException {
        initializeUser();
        try {
            User.Delete del = currentUser.delete(true);
            fail("NullPointerException should be thrown.");
        } catch (NullPointerException ex) {}
    }

    public void testRetrieve() throws IOException {
        initializeUser();
        currentUser.setId("testUser");
        User.Retrieve ret = currentUser.retrieve();
        assertEquals(currentUser.getId(),ret.get("userID").toString());
        assertEquals("GET", ret.getRequestMethod());
    }

    public void testRetrieveNullUser() throws IOException {
        initializeUser();
        try {
            User.Retrieve ret = currentUser.retrieve();
            fail("NullPointerException should be thrown.");
        } catch (NullPointerException ex) {}
    }

    public void testUpdate() throws IOException {
        initializeUser();
        currentUser.setId("testUser");
        User.Update update = currentUser.update();
        assertEquals(currentUser.getId(),update.get("userID").toString());
        assertEquals("PUT", update.getRequestMethod());
    }

    public void testUpdateNullUser() throws IOException {
        initializeUser();
        try {
            User.Update update = currentUser.update();
            fail("NullPointerException should be thrown.");
        } catch (NullPointerException ex) {}
    }

    public void testResetPassword() throws IOException {
        initializeUser();
        currentUser.setId("testUser");
        User.ResetPassword pwd = currentUser.resetPassword();
        assertEquals(currentUser.getId(),pwd.get("userID").toString());
        assertEquals("POST", pwd.getRequestMethod());
    }

    public void testResetPasswordNullUser() throws IOException {
        initializeUser();
        try {
            User.ResetPassword pwd = currentUser.resetPassword();
            fail("NullPointerException should be thrown.");
        } catch (NullPointerException ex) {}
    }

    public void testEmailVerification() throws IOException {
        initializeUser();
        currentUser.setId("testUser");
        User.EmailVerification email = currentUser.sendEmailVerification();
        assertEquals(currentUser.getId(),email.get("userID").toString());
        assertEquals("POST", email.getRequestMethod());
    }

    public void testEmailVerificationNullUser() throws IOException {
        initializeUser();
        try {
            User.EmailVerification email = currentUser.sendEmailVerification();
            fail("NullPointerException should be thrown.");
        } catch (NullPointerException ex) {}
    }
}
