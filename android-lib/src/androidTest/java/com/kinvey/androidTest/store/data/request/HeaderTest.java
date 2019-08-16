package com.kinvey.androidTest.store.data.request;

import android.content.Context;

import androidx.test.runner.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.kinvey.android.Client;
import com.kinvey.androidTest.TestManager;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyHeaders;
import com.kinvey.java.store.UserStoreRequestManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.Field;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class HeaderTest {

    private Client client;
    private TestManager testManager;

    @Before
    public void setUp() throws InterruptedException {
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        client = new Client.Builder(mMockContext).build();
        testManager = new TestManager<>();
        if (client.isUserLoggedIn()) {
            testManager.logout(client);
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
    public void testHeader() throws IOException, NoSuchFieldException, IllegalAccessException {
        UserStoreRequestManager user = new UserStoreRequestManager(client, testManager.createBuilder(client));
        assertNotNull(user);
        UserStoreRequestManager.LoginRequest loginRequest = user.createBlocking("test_name", "test_login").buildAuthRequest();
        Field kinveyAuthRequestField = loginRequest.getClass().getDeclaredField("request"); //NoSuchFieldException
        kinveyAuthRequestField.setAccessible(true);
        KinveyAuthRequest request = (KinveyAuthRequest) kinveyAuthRequestField.get(loginRequest);
        Field kinveyHeadersField = request.getClass().getDeclaredField("kinveyHeaders"); //NoSuchFieldException
        kinveyHeadersField.setAccessible(true);
        KinveyHeaders kinveyHeaders = (KinveyHeaders) kinveyHeadersField.get(request);
        String header = (String) kinveyHeaders.get("x-kinvey-device-info");
        assertTrue(header.contains("hv"));
        assertTrue(header.contains("md"));
        assertTrue(header.contains("os"));
        assertTrue(header.contains("ov"));
        assertTrue(header.contains("sdk"));
        assertTrue(header.contains("pv"));
        assertTrue(header.contains("id"));
        assertTrue(header.contains("{"));
        assertTrue(header.contains("}"));
        assertTrue(header.contains("Android"));
        assertTrue(header.contains(client.getDeviceId()));
    }


}
