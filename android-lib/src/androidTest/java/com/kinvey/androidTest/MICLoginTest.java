package com.kinvey.androidTest;


import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.GenericJson;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyMICCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.model.TestUser;
import com.kinvey.androidTest.store.UserStoreTest;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.User;
import com.kinvey.java.store.UserStoreRequestManager;
import com.kinvey.java.store.requests.user.GetMICAccessToken;
import com.kinvey.java.store.requests.user.GetMICTempURL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class MICLoginTest {

    private Client client = null;
    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";
    private static final String APP_KEY = "kid_BkssrV2eW";
    private static final String APP_SECRET = "YOUR_APP_SECRET_HERE";
    private static final String CLIENT_ID = "CLIENT_ID";


    @Before
    public void setup() throws InterruptedException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).setUserClass(TestUser.class).build();
    }

    // Check clientId in auth link for MICLoginPage
    @Test
    public void testMIC_LoginWithAuthorizationCodeLoginPageWithClientId() throws InterruptedException {
        String redirectURI = "http://test.redirect";
        DefaultKinveyMICCallback userCallback = loginWithAuthorizationCodeLoginPage(CLIENT_ID, redirectURI, client);
        assertNotNull(userCallback.myURLToRender);
        assertTrue(!userCallback.myURLToRender.isEmpty());
        assertTrue(userCallback.myURLToRender.startsWith(client.getMICHostName() + "oauth/auth?client_id=" + APP_KEY + ":" + CLIENT_ID));
    }

    // Check clientId (should be absent second part of client_id) in auth link for MICLoginPage
    @Test
    public void testMIC_LoginWithAuthorizationCodeLoginPage() throws InterruptedException {
        String redirectURI = "http://test.redirect";
        DefaultKinveyMICCallback userCallback = loginWithAuthorizationCodeLoginPage(null, redirectURI, client);
        assertNotNull(userCallback.myURLToRender);
        assertTrue(!userCallback.myURLToRender.isEmpty());
        assertTrue(userCallback.myURLToRender.startsWith(client.getMICHostName() + "oauth/auth?client_id=" + APP_KEY + "&"));
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
        String redirectURI = "http://test.redirect";
        UserStoreRequestManager requestManager = new UserStoreRequestManager(client, createBuilder(client));
        requestManager.setMICRedirectURI(redirectURI);
        client.setClientId(CLIENT_ID);
        GetMICTempURL micTempURL = requestManager.getMICTempURL();
        assertEquals(APP_KEY + ":" + CLIENT_ID, ((HashMap) ((UrlEncodedContent) micTempURL.getHttpContent()).getData()).get("client_id"));
    }

    // Check clientId (should be absent second part of client_id) for getting Temp Link
    @Test
    public void testGetMICTempURL() throws IOException {
        String redirectURI = "http://test.redirect";
        UserStoreRequestManager requestManager = new UserStoreRequestManager(client, createBuilder(client));
        requestManager.setMICRedirectURI(redirectURI);
        GetMICTempURL micTempURL = requestManager.getMICTempURL();
        assertEquals(APP_KEY, ((HashMap) ((UrlEncodedContent) micTempURL.getHttpContent()).getData()).get("client_id"));
    }

    // Check clientId for using refresh token
    @Test
    public void testMICLoginUseRefreshTokenWithClientId() throws IOException {
        UserStoreRequestManager requestManager = new UserStoreRequestManager(client, createBuilder(client));
        client.setClientId(CLIENT_ID);
        GetMICAccessToken getMICAccessToken = requestManager.useRefreshToken("refresh_token");
        assertEquals(APP_KEY + ":" + CLIENT_ID, ((HashMap) ((UrlEncodedContent) getMICAccessToken.getHttpContent()).getData()).get("client_id"));
    }

    // Check clientId (should be absent second part of client_id) for using refresh token
    @Test
    public void testMICLoginUseRefreshToken() throws IOException {
        UserStoreRequestManager requestManager = new UserStoreRequestManager(client, createBuilder(client));
        GetMICAccessToken getMICAccessToken = requestManager.useRefreshToken("refresh_token");
        assertEquals(APP_KEY, ((HashMap) ((UrlEncodedContent) getMICAccessToken.getHttpContent()).getData()).get("client_id"));
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

}
