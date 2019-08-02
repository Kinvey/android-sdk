package com.kinvey.androidTest.store.data;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.api.client.testing.http.MockHttpTransport;
import com.kinvey.android.Client;
import com.kinvey.android.model.User;
import com.kinvey.androidTest.model.TestUser;
import com.kinvey.java.Constants;
import com.kinvey.java.KinveyException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by yuliya on 08/02/17.
 */

@RunWith(AndroidJUnit4.class)
public class ClientBuilderTest {

    private static final String BASE_URL_DEFAULT = "https://baas.kinvey.com/";
    private static final String TEST_APP_KEY = "app_key";
    private static final String TEST_APP_SECRET = "app_secret";
    private static final String KINVEY_PROPERTIES = "kinvey.properties";

    private Client client;
    private Context mContext;

    @Before
    public void setUp() throws InterruptedException, IOException {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void testSetDeltaSetCache() throws IOException {
        assertTrue(new Client.Builder(mContext).setDeltaSetCache(true) instanceof Client.Builder);
    }

    @Test
    public void testSetDeltaSetCacheTrue() throws IOException {
        client = new Client.Builder(mContext).setDeltaSetCache(true).build();
        assertTrue(client.isUseDeltaCache());
    }

    @Test
    public void testSetDeltaSetCacheFalse() throws IOException {
        client = new Client.Builder(mContext).setDeltaSetCache(false).build();
        assertFalse(client.isUseDeltaCache());
    }

    @Test
    public void testSetDeltaSetCacheDefault() throws IOException {
        client = new Client.Builder(mContext).build();
        assertFalse(client.isUseDeltaCache());
    }

    @Test
    public void testSetUserClass() throws IOException {
        client = new Client.Builder(mContext).setUserClass(TestUser.class).build();
        assertTrue(client.getUserClass() == TestUser.class);
    }

    @Test
    public void testSetUserClassDefault() throws IOException {
        client = new Client.Builder(mContext).build();
        assertTrue(client.getUserClass() == User.class);
    }

    @Test
    public void testSetBaseUrl() throws IOException {
        String url = "https://base_url/";
        client = new Client.Builder(mContext).setBaseUrl(url).build();
        assertTrue(client.getBaseUrl().equals(url));
    }

    @Test
    public void testSetWrongBaseUrl() throws IOException {
        String url = "base_url";
        try {
            client = new Client.Builder(mContext).setBaseUrl(url).build();
            assertTrue(false);
        } catch (KinveyException e) {
            assertNotNull(e.getMessage().contains("Kinvey requires `https` as the protocol when setting a base URL"));
        }
        assertNull(client);
    }

    @Test
    public void testSetBaseUrlDefault() throws IOException {
        client = new Client.Builder(mContext).build();
        assertTrue(client.getBaseUrl().equals(BASE_URL_DEFAULT));
    }

    @Test
    public void testBuilderConstructorsFirst() throws IOException {
        client = new Client.Builder(mContext).build();
        assertNotNull(client);
    }

    @Test
    public void testBuilderConstructorsSecond() throws IOException {
        client = new Client.Builder(TEST_APP_KEY, TEST_APP_SECRET, mContext).build();
        assertNotNull(client);
    }

    @Test
    public void testBuilderConstructorsThird() throws IOException {
        client = new Client.Builder(mContext, new MockHttpTransport()).build();
        assertNotNull(client);
    }

    @Test
    public void testBuilderConstructorsFourth() throws IOException {
        client = new Client.Builder(TEST_APP_KEY, TEST_APP_SECRET, mContext, new MockHttpTransport()).build();
        assertNotNull(client);
    }

    @Test
    public void testBuilderConstructorsFifth() throws IOException {
        client = new Client.Builder(mContext.getAssets().open(KINVEY_PROPERTIES), mContext).build();
        assertNotNull(client);
    }

    @Test
    public void testBuilderConstructorsFifthCheckException() throws IOException {
        try {
            client = new Client.Builder(null, mContext).build();
            assertTrue(false);
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
        assertNull(client);
    }

    @Test
    public void testBuilderConstructorsSixth() throws IOException {
        client = new Client.Builder(mContext.getAssets().open(KINVEY_PROPERTIES), new MockHttpTransport(),  mContext).build();
        assertNotNull(client);
    }

    @Test
    public void testBuilderConstructorsSixthCheckException() throws IOException {
        try {
            client = new Client.Builder(mContext.getAssets().open(KINVEY_PROPERTIES +"_not exist"), new MockHttpTransport(), mContext).build();
            assertTrue(false);
        } catch (FileNotFoundException e) {
            assertNotNull(e);
        }
        assertNull(client);
    }

    @Test
    public void testSetTimeoutRequest() throws IOException {
        client = new Client.Builder(mContext).setRequestTimeout(120000).build();
        assertNotNull(client);
        assertEquals(120000, client.getRequestTimeout());
    }

    @Test
    public void testSetInstanceId() throws IOException {
        client = new Client.Builder(mContext).setInstanceID("TestInstanceId").build();
        assertEquals(Constants.PROTOCOL_HTTPS + "TestInstanceId" + Constants.HYPHEN + Constants.HOSTNAME_API + "/", client.getBaseUrl());
        assertEquals(Constants.PROTOCOL_HTTPS + "TestInstanceId" + Constants.HYPHEN + Constants.HOSTNAME_AUTH + "/", client.getMICHostName());
    }

    @Test
    public void testSetInstanceIdAndSetBaseUrl() throws IOException {
        client = new Client.Builder(mContext).setBaseUrl("https://baseurl.com").setInstanceID("TestInstanceId").build();
        assertEquals(Constants.PROTOCOL_HTTPS + "TestInstanceId" + Constants.HYPHEN + Constants.HOSTNAME_API + "/", client.getBaseUrl());
        assertEquals(Constants.PROTOCOL_HTTPS + "TestInstanceId" + Constants.HYPHEN + Constants.HOSTNAME_AUTH + "/", client.getMICHostName());
    }

}
