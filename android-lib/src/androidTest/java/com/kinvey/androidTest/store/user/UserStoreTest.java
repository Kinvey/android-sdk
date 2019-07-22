package com.kinvey.androidTest.store.user;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.google.api.client.http.HttpResponseException;
import com.kinvey.android.AsyncUserDiscovery;
import com.kinvey.android.AsyncUserGroup;
import com.kinvey.android.Client;
import com.kinvey.android.SharedPrefCredentialStore;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyMICCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.callback.KinveyUserDeleteCallback;
import com.kinvey.android.callback.KinveyUserListCallback;
import com.kinvey.android.callback.KinveyUserManagementCallback;
import com.kinvey.android.model.User;
import com.kinvey.android.store.DataStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.model.EntitySet;
import com.kinvey.androidTest.model.InternalUserEntity;
import com.kinvey.androidTest.model.Person;
import com.kinvey.androidTest.model.TestUser;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Query;
import com.kinvey.java.UserGroup;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.KinveyJsonResponseException;
import com.kinvey.java.model.UserLookup;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static com.kinvey.java.Constants.AUTH_TOKEN;
import static com.kinvey.java.model.KinveyMetaData.KMD;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class UserStoreTest {

    private Client client = null;

    public static final String INSUFFICIENT_CREDENTIAL_TYPE = "InsufficientCredentials";
    private static final String ACTIVE_USER_COLLECTION_NAME = "active_user_info";

    private static class DefaultKinveyUserListCallback implements KinveyUserListCallback {

        private CountDownLatch latch;
        private User[] result;
        private Throwable error;

        private DefaultKinveyUserListCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(User[] result) {
            this.result = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            error.printStackTrace();
            this.error = error;
            finish();
        }

        private void finish() {
            latch.countDown();
        }
    }

    private static class DefaultKinveyListCallback implements KinveyListCallback<User> {

        private CountDownLatch latch;
        private List<User> result;
        private Throwable error;

        private DefaultKinveyListCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(List<User> result) {
            this.result = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            error.printStackTrace();
            this.error = error;
            finish();
        }

        private void finish() {
            latch.countDown();
        }
    }

    private static class CustomKinveyClientCallback implements KinveyClientCallback<TestUser> {

        private CountDownLatch latch;
        private TestUser result;
        private Throwable error;

        private CustomKinveyClientCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(TestUser user) {
            this.result = user;
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
    }

    private static class DefaultKinveyUserDeleteCallback implements KinveyUserDeleteCallback {
        private CountDownLatch latch;
        private Throwable error;

        private DefaultKinveyUserDeleteCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Void result) {
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
    }

    private static class UserGroupResponseCallback implements KinveyClientCallback<UserGroup.UserGroupResponse> {

        private CountDownLatch latch;
        private UserGroup.UserGroupResponse result;
        private Throwable error;

        private UserGroupResponseCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(UserGroup.UserGroupResponse user) {
            this.result = user;
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

    private static class DefaultKinveyUserCallback implements KinveyUserCallback<User> {

        private CountDownLatch latch;
        private User result;
        private Throwable error;

        private DefaultKinveyUserCallback(CountDownLatch latch) {
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
    }

    private static class DefaultKinveyBooleanCallback implements KinveyClientCallback<Boolean> {

        private CountDownLatch latch;
        private boolean result;
        private Throwable error;

        private DefaultKinveyBooleanCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Boolean result) {
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
    }

    private static class DefaultPersonKinveyClientCallback implements KinveyClientCallback<Person> {

        private CountDownLatch latch;
        private Person result;
        private Throwable error;

        DefaultPersonKinveyClientCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Person result) {
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
    }

    private static class DefaultKinveyUserManagementCallback implements KinveyUserManagementCallback {

        private CountDownLatch latch;
        private boolean result;
        private Throwable error;

        private DefaultKinveyUserManagementCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Void v) {
            result = true;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            result = false;
            this.error = error;
            finish();
        }

        private void finish() {
            latch.countDown();
        }
    }

    private DefaultKinveyClientEntitySetCallback findIdEntitySet(final DataStore<EntitySet> store, final String id, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientEntitySetCallback callback = new DefaultKinveyClientEntitySetCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.find(id, callback, null);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private static class DefaultKinveyClientEntitySetCallback implements KinveyClientCallback<EntitySet> {

        private CountDownLatch latch;
        EntitySet result;
        Throwable error;

        DefaultKinveyClientEntitySetCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(EntitySet result) {
            this.result = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        void finish() {
            latch.countDown();
        }
    }

    @Before
    public void setup() throws InterruptedException {
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        client = new Client.Builder(mMockContext).build();
//        client.enableDebugLogging();
        if (client.isUserLoggedIn()) {
            logout(client);
        }
    }

    @After
    public void tearDown() {
        client.performLockDown();
        if (client.getKinveyHandlerThread() != null) {
            try {
                client.stopKinveyHandlerThread();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    @Test
    public void testLogin() throws InterruptedException {
        DefaultKinveyClientCallback callback = login(USERNAME, PASSWORD);
        assertNull(callback.error);
        assertNotNull(callback.result);
        assertNull(logout(client).error);
    }

    @Test
    public void get() throws InterruptedException {
        String userId;
        login(USERNAME, PASSWORD);
        userId = client.getActiveUser().getId();
        DefaultKinveyClientCallback callback = get(userId);
        assertNull(callback.error);
        assertNotNull(callback.result);
        assertEquals(userId, callback.result.getId());
    }

    private DefaultKinveyClientCallback get(final String userName) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.get(userName, client, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }


    @Test
    public void testCustomSignUp() throws InterruptedException {
        client.setUserClass(TestUser.class);
        TestUser user = new TestUser();
        user.setCompanyName("Test Company");
        InternalUserEntity internalUserEntity = new InternalUserEntity();
        internalUserEntity.setStreet("TestStreet");
        user.setInternalUserEntity(internalUserEntity);
        CustomKinveyClientCallback callback = signUp(user);
        assertNull(callback.error);
        assertNotNull(callback.result);
        assertEquals(user.getCompanyName(), callback.result.getCompanyName());
        assertEquals("TestStreet", callback.result.getInternalUserEntity().getStreet());
        assertEquals("Test Company", ((TestUser)client.getActiveUser()).getCompanyName());
        assertEquals("TestStreet", ((TestUser)client.getActiveUser()).getInternalUserEntity().getStreet());
        destroyUser();
    }

    @Test
    public void testDestroy() throws InterruptedException {
        DefaultKinveyClientCallback callback = signUp();
        assertNull(callback.error);
        assertNotNull(callback.result);
        DefaultKinveyUserDeleteCallback deleteCallback = destroyUser();
        assertNull(deleteCallback.error);
        assertFalse(client.isUserLoggedIn());
    }

    @Test
    public void testCustomDestroy() throws InterruptedException {
        client.setUserClass(TestUser.class);
        TestUser user = new TestUser();
        user.setCompanyName("Test Company");
        CustomKinveyClientCallback callback = signUp(user);
        assertNull(callback.error);
        assertNotNull(callback.result);
        DefaultKinveyUserDeleteCallback deleteCallback = destroyUser();
        assertNull(deleteCallback.error);
        assertFalse(client.isUserLoggedIn());
    }

    private DefaultKinveyClientCallback login(final String userName, final String password) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    UserStore.login(userName, password, client, callback);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private DefaultKinveyClientCallback signUp() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.signUp(createRandomUserName(USERNAME), PASSWORD, client, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private CustomKinveyClientCallback signUp(final TestUser user) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CustomKinveyClientCallback callback = new CustomKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.signUp(createRandomUserName(USERNAME), PASSWORD, user, client, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private DefaultKinveyUserDeleteCallback destroyUser() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyUserDeleteCallback callback = new DefaultKinveyUserDeleteCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.destroy(true, client, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testLoginAsync() throws InterruptedException {
        DefaultKinveyClientCallback userCallback = login(client);
        assertNotNull(userCallback.result);
        assertTrue(client.isUserLoggedIn());
        assertNull(logout(client).error);
    }

    private DefaultKinveyClientCallback login(final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(() -> {
            try {
                UserStore.login(client, callback);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private DefaultKinveyVoidCallback logout(final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyVoidCallback callback = new DefaultKinveyVoidCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.logout(client, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testSharedClientLoginAsync() throws InterruptedException {
        DefaultKinveyClientCallback userCallback = login(Client.sharedInstance());
        assertNotNull(userCallback.result);
        assertTrue(client.isUserLoggedIn());
        assertNull(logout(client).error);
    }

    @Test
    public void testLoginAsyncBad() throws InterruptedException {
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Client fakeClient = new Client.Builder("app_key_fake", "app_secret_fake", mMockContext).build();
        DefaultKinveyClientCallback userCallback = login(fakeClient);
        assertNotNull(userCallback.error);
        userCallback = login(USERNAME, PASSWORD, fakeClient);
        assertNotNull(userCallback.error);
    }

    @Test
    public void testLoginUserPassAsync() throws InterruptedException {
        DefaultKinveyClientCallback userCallback = login(USERNAME, PASSWORD, client);
        assertNotNull(userCallback.result);
        assertTrue(client.isUserLoggedIn());
        assertNull(logout(client).error);
    }

    @Test
    public void testLoginUserPassAsyncBad() throws InterruptedException {
        DefaultKinveyClientCallback userCallback = login(USERNAME, "wrongPassword", client);
        assertNotNull(userCallback.error);
        assertFalse(client.isUserLoggedIn());
    }

    @Test
    @Ignore // need facebookAccessToken
    public void testLoginFacebookAsync() throws InterruptedException {
        String facebookAccessToken = "YOUR_ACCESS_TOKEN_HERE";
        DefaultKinveyClientCallback userCallback = loginFacebook(facebookAccessToken, client);
        assertNotNull(userCallback.result);
        assertTrue(client.isUserLoggedIn());
    }

    @Test
    public void testLoginFacebookAsyncBad() throws InterruptedException {
        String facebookAccessToken = "wrong_access_token";
        DefaultKinveyClientCallback userCallback = loginFacebook(facebookAccessToken, client);
        assertNotNull(userCallback.error);
        assertFalse(client.isUserLoggedIn());
    }

    private DefaultKinveyClientCallback loginFacebook(final String accessToken, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    UserStore.loginFacebook(accessToken, client, callback);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    @Ignore // need googleAccessToken
    public void testLoginGoogleAsync() throws InterruptedException {
        String googleAccessToken = "YOUR_ACCESS_TOKEN_HERE";
        DefaultKinveyClientCallback userCallback = loginGoogle(googleAccessToken, client);
        assertNotNull(userCallback.result);
        assertTrue(client.isUserLoggedIn());
    }

    @Test
    public void testLoginGoogleAsyncBad() throws InterruptedException {
        String googleAccessToken = "wrong_access_token";
        DefaultKinveyClientCallback userCallback = loginGoogle(googleAccessToken, client);
        assertNotNull(userCallback.error);
        assertFalse(client.isUserLoggedIn());
    }

    private DefaultKinveyClientCallback loginGoogle(final String accessToken, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    UserStore.loginGoogle(accessToken, client, callback);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    @Ignore // need to add accessToken,  accessSecret, consumerKey, consumerSecret
    public void testLoginTwitterAsync() throws InterruptedException {
        String accessToken = "YOUR_ACCESS_TOKEN_HERE";
        String accessSecret = "YOUR_ACCESS_SECRET_HERE";
        String consumerKey = "YOUR_CONSUMER_KEY_HERE";
        String consumerSecret = "YOUR_CONSUMER_SECRET_HERE";
        DefaultKinveyClientCallback userCallback = loginTwitter(accessToken, accessSecret, consumerKey, consumerSecret, client);
        assertNotNull(userCallback.result);
        assertTrue(client.isUserLoggedIn());
    }

    @Test
    public void testLoginTwitterAsyncBad() throws InterruptedException {
        String accessToken = "YOUR_ACCESS_TOKEN_HERE";
        String accessSecret = "YOUR_ACCESS_SECRET_HERE";
        String consumerKey = "YOUR_CONSUMER_KEY_HERE";
        String consumerSecret = "YOUR_CONSUMER_SECRET_HERE";
        DefaultKinveyClientCallback userCallback = loginTwitter(accessToken, accessSecret, consumerKey, consumerSecret, client);
        assertNotNull(userCallback.error);
        assertFalse(client.isUserLoggedIn());
    }

    private DefaultKinveyClientCallback loginTwitter(final String accessToken, final String accessSecret, final String consumerKey, final String consumerSecret, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    UserStore.loginTwitter(accessToken, accessSecret, consumerKey, consumerSecret, client, callback);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    // TODO: 09.12.2016 should be checked
    @Test
    @Ignore // need to add accessToken,  accessSecret, consumerKey, consumerSecret
    public void testLoginLinkedInAsync() throws InterruptedException {
        String accessToken = "YOUR_ACCESS_TOKEN_HERE"; //"AQXu60okmBXrQkBm5BOpBCBBpCYc3y9uKWHtF559A1j4ttwjf5bXNeq0nVOHtgPomuw9Wn661BYbZal-3IReW0zc-Ed8NvP0FNdOTQVt9c8qz9EL5sezCYKd_I2VPEEMSC-YOyvhi-7WsttjaPnU_9H_kCnfVJuU7Fyt8Ph1XTw66xZeu2U"
        String accessSecret = "YOUR_ACCESS_SECRET_HERE"; //"ExAZxYxvo42UfOCN";
        String consumerKey = "YOUR_CONSUMER_KEY_HERE"; //"86z99b0orhyt7s";
        String consumerSecret = "YOUR_CONSUMER_SECRET_HERE"; //"ExAZxYxvo42UfOCN";
        DefaultKinveyClientCallback userCallback = loginLinkedIn(accessToken, accessSecret, consumerKey, consumerSecret, client);
        assertNotNull(userCallback.result);
        assertTrue(client.isUserLoggedIn());
    }

    @Test
    public void testLoginLinkedInAsyncBad() throws InterruptedException {
        String accessToken = "wrongAccessToken";
        String accessSecret = "wrongAccessSecret";
        String consumerKey = "wrongConsumerKey";
        String consumerSecret = "wrongConsumerSecret";
        DefaultKinveyClientCallback userCallback = loginLinkedIn(accessToken, accessSecret, consumerKey, consumerSecret, client);
        assertNotNull(userCallback.error);
        assertFalse(client.isUserLoggedIn());
    }

    private DefaultKinveyClientCallback loginLinkedIn(final String accessToken, final String accessSecret, final String consumerKey, final String consumerSecret, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    UserStore.loginLinkedIn(accessToken, accessSecret, consumerKey, consumerSecret, client, callback);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    @Ignore // need to change accessToken, refreshToken, clientID, ID
    public void testLoginSalesforceAsync() throws InterruptedException {
        String accessToken = "YOUR_ACCESS_TOKEN_HERE"; //"00D6F000000Dct5!AQkAQFAWrMjHboaD6Yn71NezV9yizZiM_MJLodm.ppn7TgypzET20QagfusU7UCAJw7jbnjWxjsWpYI2Xoa82ehmJum65Phd";
        String refreshToken = "YOUR_REFRESH_TOKEN_HERE"; //"5Aep861..zRMyCurAW3YNVSrR4jYtnt9rDCBsqQ.ytSywG1HaexWXOn07YXPwep1YmQVmuuc9YM8sWS8pyFbC2G";
        String clientID = "YOUR_CLIENT_ID_HERE"; //"3MVG9YDQS5WtC11o5afZtRCMB4EGBMjwb0MfQOBSW2u2EZ5r6fHt_sXtYx9i2.nJIkhzicIPWpyhm1zc3HlWw";
        String ID = "YOUR_SALESFORCE_ID_HERE"; //"https://login.salesforce.com/id/00D6F000000Dct5UAC/0056F000006Xw0jQAC";
        DefaultKinveyClientCallback userCallback = loginSalesforce(accessToken, refreshToken, clientID, ID, client);
        if (userCallback.error != null) {
            Log.d("test: ", userCallback.error.getMessage());
        }
        assertNotNull(userCallback.result);
        assertTrue(client.isUserLoggedIn());
    }

    @Test
    public void testLoginSalesforceAsyncBad() throws InterruptedException {
        String accessToken = "wrongAccessToken";
        String refreshToken = "wrongRefreshToken";
        String clientID = "wrongClientID";
        String ID = "wrongID";
        DefaultKinveyClientCallback userCallback = loginSalesforce(accessToken, refreshToken, clientID, ID, client);
        assertNotNull(userCallback.error);
        assertFalse(client.isUserLoggedIn());
    }

    private DefaultKinveyClientCallback loginSalesforce(final String accessToken, final String refreshToken, final String clientID, final String ID, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    UserStore.loginSalesForce(accessToken, refreshToken, clientID, ID, client, callback);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testAddUsersToGroupWrongCredentials() throws InterruptedException, IOException {
        AsyncUserGroup asyncUserGroup = client.userGroup();
        login(client);
        UserGroupResponseCallback callback = addUsersWrongCredentials(asyncUserGroup);
        assertEquals(((KinveyJsonResponseException)callback.error).getDetails().getError(), INSUFFICIENT_CREDENTIAL_TYPE);
    }

    private UserGroupResponseCallback addUsersWrongCredentials(final AsyncUserGroup asyncUserGroup) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final UserGroupResponseCallback callback = new UserGroupResponseCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                asyncUserGroup.addAllUsersToGroup("group","group", callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testUserDiscovery() throws InterruptedException, IOException {
        AsyncUserDiscovery asyncUserGroup = client.userDiscovery();
        UserLookup userLookup = asyncUserGroup.userLookup();
        userLookup.setId("id");
        assertEquals("id", userLookup.getId());
        userLookup.setEmail("email");
        assertEquals("email", userLookup.getEmail());
        userLookup.setFirstName("first");
        assertEquals("first", userLookup.getFirstName());
        userLookup.setLastName("last");
        assertEquals("last", userLookup.getLastName());
        userLookup.setFacebookID("facebook");
        assertEquals("facebook", userLookup.getFacebookID());
        userLookup.setUsername("username");
        assertEquals("username", userLookup.getUsername());
    }

    @Test
    public void testMICRefreshTokenAfterRetrieve() throws InterruptedException, IOException {
        String redirectURI = "kinveyAuthDemo://";
        String refreshToken = null;
        String newRefreshToken = null;
        DefaultKinveyUserCallback userCallback = loginMICCodeApi(client, redirectURI);
        Credential cred = client.getStore().load(userCallback.result.getId());
        if (cred != null) {
            refreshToken = cred.getRefreshToken();
        }
        DefaultKinveyClientCallback clientCallback = retrieveMICTest(client);
        Credential credNew = client.getStore().load(clientCallback.result.getId());
        if (credNew != null) {
            newRefreshToken = credNew.getRefreshToken();
        }
        assertEquals(refreshToken, newRefreshToken);
    }

    public DefaultKinveyUserCallback loginMICCodeApi(final Client client, final String redirectUrl) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyUserCallback callback = new DefaultKinveyUserCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.loginWithAuthorizationCodeAPI(client, USERNAME, PASSWORD, redirectUrl, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private DefaultKinveyClientCallback retrieveMICTest(final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.retrieve(client, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testSharedPrefCredentialStore() throws IOException {
        String userId = "userId";
        String authotoken = "authotoken";
        String refresh = "refreshToken";
        Credential testCredential = new Credential(userId, authotoken, refresh);
        SharedPrefCredentialStore store = new SharedPrefCredentialStore(client.getContext());
        assertNull(store.load(userId));
        store.store(userId, testCredential);
        Credential emptyCred = new Credential(null, null, null);
        emptyCred = store.load(userId);
        assertNotNull(emptyCred);
        assertEquals(emptyCred.getUserId(), userId);
        assertEquals(emptyCred.getAuthToken(), authotoken);
        assertEquals(emptyCred.getRefreshToken(), refresh);
        store.delete(userId);
        assertNull(store.load(userId));
    }


    @Test
    public void testMICErrorMockResponse() throws InterruptedException, KinveyException {
        if (client.isUserLoggedIn()) {
            logout(client);
        }
        String redirectURIMock = "kinveyauthdemo://?error=credentialError";
        Intent intent = new Intent();
        intent.setData(Uri.parse(redirectURIMock));
        try {
            UserStore.onOAuthCallbackReceived(intent, null, client);
        } catch (KinveyException e) {
            e.printStackTrace();
        }
        assertNull(client.getActiveUser());
    }

    @Test
    public void testMIC_LoginWithAuthorizationCodeLoginPage() throws InterruptedException {
        String redirectURI = "http://test.redirect";
        DefaultKinveyMICCallback userCallback = loginWithAuthorizationCodeLoginPage(redirectURI, client);
        assertNotNull(userCallback.myURLToRender);
        assertTrue(!userCallback.myURLToRender.isEmpty());
        assertTrue(userCallback.myURLToRender.startsWith(client.getMICHostName() + client.getMICApiVersion() + "/oauth/auth?client_id"));
    }

    @Test
    public void testImitationUnauthorizedForRefreshToken() throws InterruptedException, IOException {
        if (client.getActiveUser() == null) {
        DefaultKinveyClientCallback callback = login(USERNAME, PASSWORD);
        assertNull(callback.error);
        assertNotNull(callback.result);
        }
        Credential cred = client.getStore().load(client.getActiveUser().getId());
        String refreshToken = null;
        if (cred != null) {
            refreshToken = cred.getRefreshToken();
        }
        if (refreshToken != null) {
            cred.setRefreshToken(null);
        }
        assertTrue(refreshToken == null);
        DataStore<EntitySet> storeAuto = DataStore.collection(EntitySet.COLLECTION, EntitySet.class, StoreType.NETWORK, client);
        DefaultKinveyClientEntitySetCallback findCallback = findIdEntitySet(storeAuto, "testId", 60);
        Assert.assertNotNull(findCallback.error);
        Assert.assertTrue(findCallback.error.getMessage().contains("InsufficientCredentials"));
    }

    private DefaultKinveyMICCallback loginWithAuthorizationCodeLoginPage(final String redirectUrl, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyMICCallback callback = new DefaultKinveyMICCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.loginWithAuthorizationCodeLoginPage(client, redirectUrl, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test //can be failed if application doesn't have permission for MIC Login
    @Ignore
    public void testMIC_LoginWithAuthorizationCodeAPI() throws InterruptedException {
        String redirectURI = "kinveyAuthDemo://";
        String clientId = "clientId";
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        client = new Client.Builder(mMockContext).build();
        if (client.isUserLoggedIn()) {
            logout(client);
        }
        DefaultKinveyUserCallback userCallback = loginWithAuthorizationCodeAPIAsync(USERNAME, PASSWORD, clientId, redirectURI, client);
        assertNotNull(userCallback.result);
        logout(client);
    }

    private DefaultKinveyUserCallback loginWithAuthorizationCodeAPIAsync(final String username, final String password, final String clientId, final String redirectUrl, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyUserCallback callback = new DefaultKinveyUserCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.loginWithAuthorizationCodeAPI(client, username, password, clientId, redirectUrl, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testLogout() throws InterruptedException {
        login(USERNAME, PASSWORD, client);
        DataStore<Person> personStore = DataStore.collection(Person.USER_STORE, Person.class, StoreType.SYNC, client);
        Person p = new Person();
        p.setUsername("TestUser");
        save(personStore, p);
        DataStore<Person> userStore = DataStore.collection("users", Person.class, StoreType.SYNC, client);
        Person p2 = new Person();
        p2.setUsername("TestUser2");
        save(userStore, p2);
        assertNull(logout(client).error);
        assertTrue(!client.isUserLoggedIn());
        assertTrue(client.getSyncManager().getCount(Person.USER_STORE) == 0);
    }

    private DefaultPersonKinveyClientCallback save(final DataStore<Person> store, final Person person) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultPersonKinveyClientCallback callback = new DefaultPersonKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.save(person, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testLogoutWithNoDatabaseTables() throws InterruptedException {
        login(USERNAME, PASSWORD, client);
        assertNull(logout(client).error);
        assertTrue(!client.isUserLoggedIn());
        assertTrue(client.getSyncManager().getCount(Person.USER_STORE) == 0);
    }

    @Test
    public void testLogoutWithDatabaseTablesButNoAPICalls() throws InterruptedException {
        login(USERNAME, PASSWORD, client);
        DataStore<Person> personStore = DataStore.collection(Person.USER_STORE, Person.class, StoreType.SYNC, client);
        assertNull(logout(client).error);
        assertTrue(!client.isUserLoggedIn());
        assertTrue(client.getSyncManager().getCount(Person.USER_STORE) == 0);
    }

    @Test
    public void testLogoutWithDatabaseTablesWithAPICalls() throws InterruptedException, IOException {
        login(USERNAME, PASSWORD, client);
        DataStore<Person> personStore = DataStore.collection(Person.USER_STORE, Person.class, StoreType.SYNC, client);
        save(personStore, new Person());
        assertNull(logout(client).error);
        assertTrue(!client.isUserLoggedIn());
        assertTrue(client.getSyncManager().getCount(Person.USER_STORE) == 0);
    }

    @Test
    public void testCreateUserAsync() throws InterruptedException {
        DefaultKinveyClientCallback callback = signUp(createRandomUserName("CreateUser"), PASSWORD, client);
        assertNotNull(callback.result);
        assertNotNull(callback.result.getUsername());
        assertTrue(client.isUserLoggedIn());
        assertNotNull(callback.result.getUsername().equals(USERNAME));
        assertNull(logout(client).error);
    }

    private DefaultKinveyClientCallback signUp(final String user, final String password, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.signUp(user, password, client, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private DefaultKinveyClientCallback login(final String user, final String password, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    UserStore.login(user, password, client, callback);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testDoesUsernameExist() throws InterruptedException {
        User user = login(USERNAME, PASSWORD, client).result;
        boolean isNameExists = exists(user.getUsername(), client).result;
        assertTrue(isNameExists);
    }

    @Test
    public void testDoesUsernameExistBad() throws InterruptedException {
        boolean isNameExists = exists("wrong_username", client).result;
        assertFalse(isNameExists);
    }

    private DefaultKinveyBooleanCallback exists(final String username, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyBooleanCallback callback = new DefaultKinveyBooleanCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.exists(username,  client, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test // this test always creates new user, to be careful
    public void testForgotUsername() throws InterruptedException {
        User user = signUp(createRandomUserName("forgotUserName"), PASSWORD, client).result;
        assertNotNull(user);
        assertNotNull(user.getUsername());
        assertTrue(client.isUserLoggedIn());
        boolean isForgotten = forgot(user.getUsername(), client).result;
        assertTrue(isForgotten);
        deleteUser(true, client);
    }

    private DefaultKinveyUserManagementCallback forgot(final String username, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyUserManagementCallback callback = new DefaultKinveyUserManagementCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.forgotUsername(client, username, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test // this test always creates new user, to be careful
    public void testDeleteUserSoftAsync() throws InterruptedException {
        User user = signUp(createRandomUserName("deleteUserSoft"), PASSWORD, client).result;
        assertNotNull(user);
        assertNotNull(user.getId());
        assertNull(deleteUser(false, client).error);
    }

    private DefaultKinveyUserDeleteCallback deleteUser(final boolean isHard, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyUserDeleteCallback callback = new DefaultKinveyUserDeleteCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.destroy(isHard, client, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testDeleteUserHardAsync() throws InterruptedException {
        User user = signUp(createRandomUserName("deleteUserHard"), PASSWORD, client).result;
        assertNotNull(user);
        assertNotNull(user.getId());
        assertNull(deleteUser(true, client).error);
    }

    @Test
    public void testUserEmailVerification() throws InterruptedException {
        User user = login(USERNAME, PASSWORD, client).result;
        assertNotNull(user);
        assertNotNull(user.getId());
        boolean isEmailVerificationSent = sentEmailVerification(client).result;
        assertTrue(isEmailVerificationSent);
        assertNull(logout(client).error);
    }

    private DefaultKinveyUserManagementCallback sentEmailVerification(final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyUserManagementCallback callback = new DefaultKinveyUserManagementCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.sendEmailConfirmation(client, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testUserPasswordReset() throws InterruptedException {
        User user = login(USERNAME, PASSWORD, client).result;
        assertNotNull(user);
        assertNotNull(user.getId());
        assertNotNull(user.getUsername());
        boolean isPasswordReset = resetPassword(USERNAME, client).result;
        assertTrue(isPasswordReset);
        assertNull(logout(client).error);
    }

    private DefaultKinveyUserManagementCallback resetPassword(final String username, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyUserManagementCallback callback = new DefaultKinveyUserManagementCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.resetPassword(username, client, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    @Ignore //need to check
    public void testUserInitFromCredential() throws InterruptedException {
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Client.Builder localBuilder = new Client.Builder(mMockContext);
        Client localClient = localBuilder.build();
        DefaultKinveyClientCallback callback = login(USERNAME, PASSWORD, localClient);
        assertNotNull(callback.result);
        User activeUser = callback.result;
        Context mMockContext2 = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Client.Builder localBuilder2 = new Client.Builder(mMockContext2);
        Client localClient2 = localBuilder2.build();
        assertNotNull(localClient2.getActiveUser());
        assertTrue(activeUser.getId().equals(localClient2.getActiveUser().getId()));
        assertTrue(activeUser.getUsername().equals(localClient2.getActiveUser().getUsername()));
        assertNull(logout(localClient).error);
        assertNull(logout(localClient2).error);
    }

    @Test
    public void testSignUpIfUserLoggedIn() throws InterruptedException {
        User user = login(USERNAME, PASSWORD, client).result;
        assertNotNull(user);
        assertNotNull(user.getId());
        assertTrue(client.isUserLoggedIn());
        DefaultKinveyClientCallback callback = signUp();
        assertNull(callback.result);
        assertNotNull(callback.error);
        assertNull(logout(client).error);
    }

    @Test
    public void testSignUpIfUserExists() throws InterruptedException {
        User user = signUp(createRandomUserName("TestSignUp"), PASSWORD, client).result;
        assertNotNull(user);
        assertNotNull(user.getId());
        assertTrue(client.isUserLoggedIn());
        assertNull(logout(client).error);
        assertFalse(client.isUserLoggedIn());
        DefaultKinveyClientCallback callback = signUp(user.getUsername(), PASSWORD, client);
        assertNull(callback.result);
        assertNotNull(callback.error);
        assertFalse(client.isUserLoggedIn());
    }

    @Test
    public void testSignUpWithEmptyUsername() throws InterruptedException {
        DefaultKinveyClientCallback callback = signUp("", PASSWORD, client);
        assertNotNull(callback.result);
        assertNull(callback.error);
        assertTrue(client.isUserLoggedIn());
        assertNull(logout(client).error);
    }

    @Test
    public void testSignUpWithEmptyPassword() throws InterruptedException {
        DefaultKinveyClientCallback callback = signUp(createRandomUserName("Test123"), "", client);
        assertNotNull(callback.result);
        assertNull(callback.error);
        assertTrue(client.isUserLoggedIn());
        assertNull(logout(client).error);
    }

    @Test
    public void testUpdate() throws InterruptedException {
        DefaultKinveyClientCallback callback = login(client);
        assertNotNull(callback.result);
        assertTrue(client.isUserLoggedIn());
        String oldUserName = client.getActiveUser().getUsername();
        client.getActiveUser().setUsername("NewUserName2");
        DefaultKinveyClientCallback userKinveyClientCallback = update(client);
        assertNotNull(userKinveyClientCallback.result);
        assertNotEquals(oldUserName, userKinveyClientCallback.result.getUsername());
        assertNotNull(deleteUser(true, client));
    }

    @Test
    public void testUpdateCustomUser() throws InterruptedException {
        client.setUserClass(TestUser.class);
        TestUser user = new TestUser();
        user.setCompanyName("Test Company");
        InternalUserEntity internalUserEntity = new InternalUserEntity();
        internalUserEntity.setStreet("TestStreet");
        user.setInternalUserEntity(internalUserEntity);
        CustomKinveyClientCallback callback = signUp(user);
        assertNull(callback.error);
        assertNotNull(callback.result);

        assertEquals(user.getCompanyName(), callback.result.getCompanyName(), client.getActiveUser().get("companyName"));

        client.getActiveUser().set("companyName", "New Company");
        ((TestUser) client.getActiveUser()).getInternalUserEntity().setStreet("TestStreet2");
        CustomKinveyClientCallback userKinveyClientCallback = updateCustomUser(client);
        assertNotNull(userKinveyClientCallback.result);
        assertEquals("New Company", userKinveyClientCallback.result.getCompanyName());
        assertEquals("TestStreet2", userKinveyClientCallback.result.getInternalUserEntity().getStreet());
        assertNotNull(deleteUser(true, client));
    }

    private DefaultKinveyClientCallback update(final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                client.getActiveUser().update(callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    private CustomKinveyClientCallback updateCustomUser(final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CustomKinveyClientCallback callback = new CustomKinveyClientCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                client.getActiveUser().update(callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    @Test
    public void testUserDiscoveryLookup() throws InterruptedException, IOException {
        AsyncUserDiscovery asyncUserGroup = client.userDiscovery();
        login(USERNAME, PASSWORD);
        DefaultKinveyUserListCallback callback = lookupByFullNameDiscovery("first", "last", asyncUserGroup);
        assertNull(callback.error);
        assertNotNull(callback.result);
    }

    private DefaultKinveyUserListCallback lookupByFullNameDiscovery(final String firstname, final String lastname, final AsyncUserDiscovery asyncUserDiscovery) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyUserListCallback callback = new DefaultKinveyUserListCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                asyncUserDiscovery.lookupByFullName(firstname, lastname, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testUsernameDiscoveryLookup() throws InterruptedException, IOException {
        AsyncUserDiscovery asyncUserGroup = client.userDiscovery();
        login(USERNAME, PASSWORD);
        DefaultKinveyUserListCallback callback = lookupByUsernameDiscovery(USERNAME, asyncUserGroup);
        assertNotNull(callback.result);
        assertTrue(callback.result.length > 0);
    }

    private DefaultKinveyUserListCallback lookupByUsernameDiscovery(final String name, final AsyncUserDiscovery asyncUserDiscovery) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyUserListCallback callback = new DefaultKinveyUserListCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                asyncUserDiscovery.lookupByUserName(name, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testFacebookIdDiscoveryLookup() throws InterruptedException, IOException {
        AsyncUserDiscovery asyncUserGroup = client.userDiscovery();
        login(USERNAME, PASSWORD);
        DefaultKinveyUserListCallback callback = lookupByFacebookIdDiscovery("testID", asyncUserGroup);
        assertNull(callback.error);
        assertNotNull(callback.result);
    }

    private DefaultKinveyUserListCallback lookupByFacebookIdDiscovery(final String id, final AsyncUserDiscovery asyncUserDiscovery) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyUserListCallback callback = new DefaultKinveyUserListCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                asyncUserDiscovery.lookupByFacebookID(id, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testRetrieve() throws InterruptedException {
        DefaultKinveyClientCallback loginCallback = login(USERNAME, PASSWORD);
        assertNull(loginCallback.error);
        assertNotNull(loginCallback.result);
        DefaultKinveyClientCallback retrieveCallback = retrieve(client);
        assertNull(retrieveCallback.error);
        assertNotNull(retrieveCallback.result);
        assertTrue(client.isUserLoggedIn());
    }

    @Test
    public void testRetrieveCustomUser() throws InterruptedException {
        client.setUserClass(TestUser.class);
        TestUser user = new TestUser();
        CustomKinveyClientCallback callback = signUp(user);
        assertNull(callback.error);
        assertNotNull(callback.result);
        DefaultKinveyClientCallback retrieveCallback = retrieve(client);
        assertNotNull(deleteUser(true, client));
        assertNull(retrieveCallback.error);
        assertNotNull(retrieveCallback.result);
    }

    private DefaultKinveyClientCallback retrieve(final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                UserStore.retrieve(client, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    @Test
    public void testRetrieveUsers() throws InterruptedException {
        DefaultKinveyClientCallback loginCallback = login(USERNAME, PASSWORD);
        assertNull(loginCallback.error);
        assertNotNull(loginCallback.result);
        DefaultKinveyListCallback retrieveCallback = retrieveUsers(client);
        assertNull(retrieveCallback.error);
        assertNotNull(retrieveCallback.result);
        assertTrue(client.isUserLoggedIn());
    }

    @Test
    public void testRetrieveUsersArrayDeprecated() throws InterruptedException {
        DefaultKinveyClientCallback loginCallback = login(USERNAME, PASSWORD);
        assertNull(loginCallback.error);
        assertNotNull(loginCallback.result);
        DefaultKinveyUserListCallback retrieveCallback = retrieveUsersDeprecated(client);
        assertNull(retrieveCallback.error);
        assertNotNull(retrieveCallback.result);
    }

    @Test
    public void testRetrieveCustomUsers() throws InterruptedException {
        client.setUserClass(TestUser.class);
        TestUser user = new TestUser();
        CustomKinveyClientCallback callback = signUp(user);
        assertNull(callback.error);
        assertNotNull(callback.result);
        DefaultKinveyListCallback retrieveCallback = retrieveUsers(client);
        assertNotNull(deleteUser(true, client));
        assertNull(retrieveCallback.error);
        assertNotNull(retrieveCallback.result);
    }

    private DefaultKinveyListCallback retrieveUsers(final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyListCallback callback = new DefaultKinveyListCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                Query query = new Query();
                UserStore.retrieve(query, new String[]{USERNAME, PASSWORD} ,client, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    private DefaultKinveyUserListCallback retrieveUsersDeprecated(final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyUserListCallback callback = new DefaultKinveyUserListCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                Query query = new Query();
                UserStore.retrieve(query, new String[]{USERNAME, PASSWORD} ,client, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    @Test
    public void testRetrieveUsersList() throws InterruptedException {
        DefaultKinveyClientCallback loginCallback = login(USERNAME, PASSWORD);
        assertNull(loginCallback.error);
        assertNotNull(loginCallback.result);
        DefaultKinveyListCallback retrieveUsersList = retrieveUsersList(client);
        assertNull(retrieveUsersList.error);
        assertNotNull(retrieveUsersList.result);
        assertNull(logout(client).error);
    }

    private DefaultKinveyListCallback retrieveUsersList(final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyListCallback callback = new DefaultKinveyListCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                Query query = new Query();
                UserStore.retrieve(query, client, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    private String createRandomUserName(String testName) {
        return testName + "_" +System.currentTimeMillis();
    }

//   MLIBZ-2654 client.isUserLoggedIn is true if not correct auth token is used
/*    @Test
    public void testLoginKinveyAuthTokenError() throws InterruptedException {
        client.enableDebugLogging();
        User user = login(USERNAME, PASSWORD, client).result;
        assertNotNull(user);
        assertNotNull(user.getId());
        String authToken = "InvalidAuthToken";
        String clientID = user.getId();
        logout(client);

        DefaultKinveyClientCallback callback = loginKinveyAuthToken(clientID, authToken, client);
        assertNotNull(callback.error);
        assertFalse(client.isUserLoggedIn());
    }

    private DefaultKinveyClientCallback loginKinveyAuthToken(final String userId, final String authToken, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                new UserStore().loginKinveyAuthToken(userId, authToken, client, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }*/

    @Test
    public void testLoginMobileIdentityError() throws InterruptedException {
        String authToken = "WrongToken";
        DefaultKinveyClientCallback callback = loginMobileIdentity(authToken, client);
        assertNotNull(callback.error);
        assertFalse(client.isUserLoggedIn());
    }

    private DefaultKinveyClientCallback loginMobileIdentity(final String authToken, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    UserStore.loginMobileIdentity(authToken, client, callback);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testLoginByCredential() throws InterruptedException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        User user = login(USERNAME, PASSWORD, client).result;
        Thread.sleep(2000);// This is time for the SDK to write user's data to the Credential Store
        Credential credential = getCredential(client.getContext(), user.getId());
        logout(client);
        assertFalse(client.isUserLoggedIn());
        assertNotNull(credential);
        DefaultKinveyClientCallback callback = loginCredential(credential, client);
        assertNull(callback.error);
        assertNotNull(callback.result);
        assertTrue(client.isUserLoggedIn());
    }

    private Credential getCredential(Context context, String userId) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
        Class c = Class.forName("com.kinvey.android.AndroidCredentialStore");
        Constructor constructor = c.getDeclaredConstructor(Context.class);
        constructor.setAccessible(true);
        Object obj  = constructor.newInstance(context);
        Method m1 = c.getDeclaredMethod("load", new Class[]{String.class});
        return (Credential) m1.invoke(obj,userId);
    }

    @Test
    public void testLoginByCredentialError() throws InterruptedException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        DefaultKinveyClientCallback callback = loginCredential(null, client);
        assertNotNull(callback.error);
        assertNull(callback.result);
        assertFalse(client.isUserLoggedIn());
    }

    private DefaultKinveyClientCallback loginCredential(final Credential credential, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    UserStore.login(credential, client, callback);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testChangePassword() throws InterruptedException {
        String userName = "testUser123";
        String newPassword = "testUser123Password";
        signUp(userName, PASSWORD, client);
        assertTrue(client.isUserLoggedIn());
        DefaultKinveyUserManagementCallback callback = changePassword(newPassword, client);
        assertNull(callback.error);
        assertTrue(callback.result);
        assertTrue(client.isUserLoggedIn());
        logout(client);
        login(userName, PASSWORD, client);
        assertFalse(client.isUserLoggedIn());
        login(userName, newPassword, client);
        assertTrue(client.isUserLoggedIn());
        assertNull(deleteUser(true, client).error);
        assertFalse(client.isUserLoggedIn());
    }

    private DefaultKinveyUserManagementCallback changePassword(final String password, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyUserManagementCallback callback = new DefaultKinveyUserManagementCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.changePassword(password, client, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

//  MLIBZ-2653  UserStore#get doesn't return User object
/*    @Test
    public void testGet() throws InterruptedException {
        User user = login(USERNAME, PASSWORD).result;
        DefaultKinveyClientCallback callback = get(user.getId(), client);
        assertNull(callback.error);
        assertNotNull(callback.result);
        assertEquals(user.getId(), callback.result.getId());
    }*/

    @Test
    public void testGetRequestError() throws InterruptedException {
        login(USERNAME, PASSWORD);
        DefaultKinveyClientCallback callback = get("unexistUser", client);
        assertNotNull(callback.error);
        assertNull(callback.result);
        assertEquals("This user does not exist for this app backend.",
                ((KinveyJsonResponseException) callback.error).getDetails().getDescription());
    }

    private DefaultKinveyClientCallback get(final String userId, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.get(userId, client, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testConvenience() throws InterruptedException {
        User user = login(USERNAME, PASSWORD).result;
        DefaultKinveyClientCallback callback = convenience(client);
        assertNull(callback.error);
        assertNotNull(callback.result);
        assertEquals(user.getId(), callback.result.getId());
    }

    @Test
    public void testConvenienceError() throws InterruptedException {
        assertFalse(client.isUserLoggedIn());
        DefaultKinveyClientCallback callback = convenience(client);
        assertNotNull(callback.error);
        assertNull(callback.result);
        assertEquals("currentUser must not be null", callback.error.getMessage());
    }

    private DefaultKinveyClientCallback convenience(final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.convenience(client, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }


    @Test
    public void testRetrieveResolves() throws InterruptedException {
        User user = login(USERNAME, PASSWORD).result;
        DefaultKinveyClientCallback callback = retrieve(new String[]{USERNAME, PASSWORD}, client);
        assertNull(callback.error);
        assertNotNull(callback.result);
        assertEquals(user.getId(), callback.result.getId());
    }

    @Test
    public void testRetrieveResolvesError() throws InterruptedException {
        assertFalse(client.isUserLoggedIn());
        DefaultKinveyClientCallback callback = retrieve(new String[]{USERNAME, PASSWORD}, client);
        assertNotNull(callback.error);
        assertNull(callback.result);
        assertEquals("currentUser must not be null", callback.error.getMessage());
    }

    private DefaultKinveyClientCallback retrieve(final String[] resolves, final Client client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.retrieve(resolves, client, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testLoginWithAuthorizationCodeAPIError() throws InterruptedException {
        login(USERNAME, PASSWORD, client);
        DefaultKinveyUserCallback callback = loginWithAuthorizationCodeAPI(USERNAME, PASSWORD, "someClientId", "redirectURI");
        assertNotNull(callback.error);
        assertNull(callback.result);
        assertTrue(((HttpResponseException) callback.error).getContent().contains("Client authentication failed"));
    }

    private DefaultKinveyUserCallback loginWithAuthorizationCodeAPI(final String username, final String password
            , final String clientId, final String redirectURI) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyUserCallback callback = new DefaultKinveyUserCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                UserStore.loginWithAuthorizationCodeAPI(client, username, password, clientId, redirectURI, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testUserInfoInRealmTable() throws InterruptedException {
        DefaultKinveyClientCallback callback = login(USERNAME, PASSWORD);
        assertNull(callback.error);
        assertNotNull(callback.result);
        User user = client.getCacheManager().getCache(ACTIVE_USER_COLLECTION_NAME, User.class, Long.MAX_VALUE).get().get(0);
        assertNotNull(user.getUsername());
        assertEquals(USERNAME, user.getUsername());
        assertEquals(USERNAME, client.getActiveUser().getUsername());
        assertNull(user.getAuthToken()); // check that realm doesn't keep auth_token
        assertNull(((Map<String, String>) user.get(KMD)).get(AUTH_TOKEN)); // check that realm doesn't keep auth_token
        assertNotNull(((Map<String, String>) client.getActiveUser().get(KMD)).get(AUTH_TOKEN)); // check that active user has auth_token
        assertNull(logout(client).error);
    }

}
