package com.kinvey.androidTest.store.user;


import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.api.client.http.UrlEncodedContent;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyMICCallback;
import com.kinvey.android.model.User;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.model.TestUser;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.store.UserStoreRequestManager;
import com.kinvey.java.store.requests.user.GetMICAccessToken;
import com.kinvey.java.store.requests.user.GetMICTempURL;
import com.kinvey.java.store.requests.user.LoginToTempURL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class MICLoginTest {

    private Client client = null;
    private static final String APP_KEY = "YOUR_APP_KEY_HERE";
    private static final String APP_SECRET = "YOUR_APP_SECRET_HERE";
    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String AUTH_TOKEN = "auth_token";
    private static final String REDIRECT_URI = "http://test.redirect";
    private static final String CLIENT_ID_FIELD = "client_id";


    @Before
    public void setup() throws InterruptedException {
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        client = new Client.Builder(APP_KEY, APP_SECRET, mMockContext).setUserClass(TestUser.class).build();
    }

    // Check clientId in auth link for MICLoginPage
    @Test
    public void testMIC_LoginWithAuthorizationCodeLoginPageWithClientId() throws InterruptedException {
        DefaultKinveyMICCallback userCallback = loginWithAuthorizationCodeLoginPage(CLIENT_ID, REDIRECT_URI, client);
        assertNotNull(userCallback.myURLToRender);
        assertTrue(!userCallback.myURLToRender.isEmpty());
        assertTrue(userCallback.myURLToRender.startsWith(client.getMICHostName() + client.getMICApiVersion() + "/oauth/auth?client_id=" + APP_KEY + "." + CLIENT_ID));
    }

    // Check clientId (should be absent second part of client_id) in auth link for MICLoginPage
    @Test
    public void testMIC_LoginWithAuthorizationCodeLoginPage() throws InterruptedException {
        DefaultKinveyMICCallback userCallback = loginWithAuthorizationCodeLoginPage(null, REDIRECT_URI, client);
        assertNotNull(userCallback.myURLToRender);
        assertTrue(!userCallback.myURLToRender.isEmpty());
        assertTrue(userCallback.myURLToRender.startsWith(client.getMICHostName() + client.getMICApiVersion() + "/oauth/auth?client_id=" + APP_KEY + "&"));
    }

    // Check that myURLToRender contains openId parameter
    @Test
    public void testOpenIdExists() throws InterruptedException {
        DefaultKinveyMICCallback userCallback = loginWithAuthorizationCodeLoginPage(null, REDIRECT_URI, client);
        assertNotNull(userCallback.myURLToRender);
        assertTrue(!userCallback.myURLToRender.isEmpty());
        assertTrue(userCallback.myURLToRender.endsWith("&response_type=code" + "&scope=openid"));
    }

    private DefaultKinveyMICCallback loginWithAuthorizationCodeLoginPage(final String clientId, final String redirectUrl, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyMICCallback callback = new DefaultKinveyMICCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                UserStore.loginWithAuthorizationCodeLoginPage(client, clientId, redirectUrl, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    // Check clientId for getting Temp Link
    @Test
    public void testGetMICTempURLWithClientId() throws IOException {
        UserStoreRequestManager requestManager = new UserStoreRequestManager(client, createBuilder(client));
        requestManager.setMICRedirectURI(REDIRECT_URI);
        GetMICTempURL micTempURL = requestManager.getMICTempURL(CLIENT_ID);
        assertTrue(micTempURL.getUriTemplate().endsWith("scope=openid"));
        assertEquals(APP_KEY + "." + CLIENT_ID, ((HashMap) ((UrlEncodedContent) micTempURL.getHttpContent()).getData()).get(CLIENT_ID_FIELD));
    }

    @Test
    public void testLoginToTempURL() throws IOException {
        UserStoreRequestManager requestManager = new UserStoreRequestManager(client, createBuilder(client));
        LoginToTempURL loginToTempURL = requestManager.MICLoginToTempURL(USERNAME, PASSWORD, CLIENT_ID, "tempURL");
        requestManager.setMICRedirectURI(REDIRECT_URI);
        GetMICTempURL micTempURL = requestManager.getMICTempURL(CLIENT_ID);
        assertTrue(micTempURL.getUriTemplate().endsWith("scope=openid"));
        assertEquals(APP_KEY + "." + CLIENT_ID, ((HashMap) ((UrlEncodedContent) loginToTempURL.getHttpContent()).getData()).get(CLIENT_ID_FIELD));
    }

    // Check clientId (should be absent second part of client_id) for getting Temp Link
    @Test
    public void testGetMICTempURL() throws IOException {
        UserStoreRequestManager requestManager = new UserStoreRequestManager(client, createBuilder(client));
        requestManager.setMICRedirectURI(REDIRECT_URI);
        GetMICTempURL micTempURL = requestManager.getMICTempURL(null);
        assertEquals(APP_KEY, ((HashMap) ((UrlEncodedContent) micTempURL.getHttpContent()).getData()).get(CLIENT_ID_FIELD));
    }

    // Check clientId for using refresh token
    @Test
    public void testMICLoginUseRefreshTokenWithClientId() throws IOException {
        UserStoreRequestManager requestManager = new UserStoreRequestManager(client, createBuilder(client));
        User user = new User();
        user.setId("userId");
        client.setActiveUser(user);
        Credential credential = new Credential(client.getActiveUser().getId(), AUTH_TOKEN, REFRESH_TOKEN);
        credential.setClientId(CLIENT_ID);
        client.getStore().store(client.getActiveUser().getId(), credential);
        GetMICAccessToken getMICAccessToken = requestManager.useRefreshToken(REFRESH_TOKEN);
        assertEquals(APP_KEY + "." + CLIENT_ID, ((HashMap) ((UrlEncodedContent) getMICAccessToken.getHttpContent()).getData()).get(CLIENT_ID_FIELD));
    }

    // Check clientId (should be absent second part of client_id) for using refresh token
    @Test
    public void testMICLoginUseRefreshToken() throws IOException {
        UserStoreRequestManager requestManager = new UserStoreRequestManager(client, createBuilder(client));
        User user = new User();
        user.setId("userId");
        client.setActiveUser(user);
        Credential credential = new Credential(client.getActiveUser().getId(), AUTH_TOKEN, REFRESH_TOKEN);
        client.getStore().store(client.getActiveUser().getId(), credential);
        GetMICAccessToken getMICAccessToken = requestManager.useRefreshToken(REFRESH_TOKEN);
        assertEquals(APP_KEY, ((HashMap) ((UrlEncodedContent) getMICAccessToken.getHttpContent()).getData()).get(CLIENT_ID_FIELD));
    }

    private KinveyAuthRequest.Builder createBuilder(AbstractClient client) {
        return new KinveyAuthRequest.Builder(client.getRequestFactory().getTransport(),
                client.getJsonFactory(), client.getBaseUrl(), APP_KEY, APP_SECRET, null);
    }


    private static class DefaultKinveyMICCallback implements KinveyMICCallback<User> {

        private CountDownLatch latch;
        private User result;
        private Throwable error;
        private String myURLToRender;

        private DefaultKinveyMICCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(User result) {
            this.result = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        private void finish() {
            latch.countDown();
        }

        @Override
        public void onReadyToRender(String myURLToRender) {
            this.myURLToRender = myURLToRender;
            finish();
        }
    }

    @Test
    public void testMICDefaultAPIVersion() throws IOException {
        assertEquals("v3", client.getMICApiVersion());
    }

    @Test
    public void testMICCustomAPIVersion() throws IOException {
        client.setMICApiVersion("1");
        assertEquals("v1", client.getMICApiVersion());
    }

}
