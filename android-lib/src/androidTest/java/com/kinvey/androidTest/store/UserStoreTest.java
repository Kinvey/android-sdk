package com.kinvey.androidTest.store;

import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserDeleteCallback;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.model.TestUser;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.dto.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class UserStoreTest {

    private Client client = null;
    private final String testUser = "test20";
    private final String testPassword = testUser;

    private static class DefaultKinveyClientCallback implements KinveyClientCallback<User> {

        private CountDownLatch latch;
        User result;
        Throwable error;

        DefaultKinveyClientCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(User user) {
            this.result = user;
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

    private static class CustomKinveyClientCallback implements KinveyClientCallback<TestUser> {

        private CountDownLatch latch;
        TestUser result;
        Throwable error;

        CustomKinveyClientCallback(CountDownLatch latch) {
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

        void finish() {
            latch.countDown();
        }
    }

    private static class DefaultKinveyUserDeleteCallback implements KinveyUserDeleteCallback {
        private CountDownLatch latch;
        Throwable error;

        DefaultKinveyUserDeleteCallback(CountDownLatch latch) {
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

        void finish() {
            latch.countDown();
        }
    }

    @Before
    public void setup() throws InterruptedException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).setUserClass(TestUser.class).build();
        final CountDownLatch latch = new CountDownLatch(1);
        if (client.isUserLoggedIn()) {
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    UserStore.logout(client, new KinveyClientCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            latch.countDown();
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            latch.countDown();
                        }
                    });
                    Looper.loop();
                }
            }).start();
        } else {
            latch.countDown();
        }
        latch.await();
    }

    @Test
    public void testLogin() throws InterruptedException {
        DefaultKinveyClientCallback callback = login("test", "test");
        assertNull(callback.error);
        assertNotNull(callback.result);
    }

    @Test
    public void testCustomSignUp() throws InterruptedException {
        TestUser user = new TestUser();
        user.setCompanyName("Test Company");
        CustomKinveyClientCallback callback = signUp(user);
        assertNull(callback.error);
        assertNotNull(callback.result);
        destroyUser();
        assertEquals(user.getCompanyName(), callback.result.getCompanyName());
    }

    @Test
    public void testDestroy() throws InterruptedException {
        DefaultKinveyClientCallback callback = signUp();
        assertNull(callback.error);
        assertNotNull(callback.result);
        DefaultKinveyUserDeleteCallback deleteCallback = destroyUser();
        assertNull(deleteCallback.error);
    }

    @Test
    public void testCustomDestroy() throws InterruptedException {
        TestUser user = new TestUser();
        user.setCompanyName("Test Company");
        CustomKinveyClientCallback callback = signUp(user);
        assertNull(callback.error);
        assertNotNull(callback.result);
        DefaultKinveyUserDeleteCallback deleteCallback = destroyUser();
        assertNull(deleteCallback.error);
    }

    private DefaultKinveyClientCallback login(final String userName, final String password) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final UserStoreTest.DefaultKinveyClientCallback callback = new UserStoreTest.DefaultKinveyClientCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    UserStore.login(userName, password, client, callback);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    private DefaultKinveyClientCallback signUp() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final UserStoreTest.DefaultKinveyClientCallback callback = new UserStoreTest.DefaultKinveyClientCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                UserStore.signUp(testUser, testPassword, client, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    private CustomKinveyClientCallback signUp(final TestUser user) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final UserStoreTest.CustomKinveyClientCallback callback = new UserStoreTest.CustomKinveyClientCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                UserStore.signUp(testUser, testPassword, user, client, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    private DefaultKinveyUserDeleteCallback destroyUser() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyUserDeleteCallback callback = new DefaultKinveyUserDeleteCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                UserStore.destroy(true, client, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

}
