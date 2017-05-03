package com.kinvey.androidTest.store;

import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.dto.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class UserStoreTest {

    private Client client = null;

    private static class DefaultKinveyClientCallback implements KinveyClientCallback<User> {

        private CountDownLatch latch;
        User user;
        Throwable error;

        DefaultKinveyClientCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(User user) {
            this.user = user;
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
        client = new Client.Builder(mMockContext).build();
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
    public void testSignUp() throws InterruptedException {
        DefaultKinveyClientCallback callback = login();
        assertNull(callback.error);
        assertNotNull(callback.user);
    }

    private DefaultKinveyClientCallback login() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final UserStoreTest.DefaultKinveyClientCallback callback = new UserStoreTest.DefaultKinveyClientCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    UserStore.login("test", "test", client, callback);
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
                UserStore.signUp("test", "test", client, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

}
