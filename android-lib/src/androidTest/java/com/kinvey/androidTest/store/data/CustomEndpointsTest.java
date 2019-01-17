package com.kinvey.androidTest.store.data;

import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncCustomEndpoints;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.model.User;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.CustomEndpoints;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.KinveyJsonResponseException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class CustomEndpointsTest {

    private static final String X2 = "x2";
    private static final String GET_PERSON = "getPerson";
    private static final String GET_PERSON_LIST = "getPersonList";
    private static final String NOT_EXIST_CUSTOM_ENDPOINT = "notExistCustomEndpoint";    
    private static final String ID = "id";    
    private static final String RESULT = "res";
    private Client client;
    
    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        final CountDownLatch latch = new CountDownLatch(1);
        if (!client.isUserLoggedIn()) {
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    try {
                        UserStore.login(TestManager.USERNAME, TestManager.PASSWORD, client, new KinveyClientCallback<User>() {
                            @Override
                            public void onSuccess(User result) {
                                assertNotNull(result);
                                latch.countDown();
                            }

                            @Override
                            public void onFailure(Throwable error) {
                                assertNull(error);
                                latch.countDown();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Looper.loop();
                }
            }).start();
        } else {
            latch.countDown();
        }
        latch.await();
    }

    /*
    * CustomEndpoint = 'x2'
    * function onRequest(request, response, modules) {
    *     response.body = {RESULT:request.body.id*2};
    *     response.complete();
    * }
    */
    @Test
    public void testCustomEndpoints() throws IOException {
        CustomEndpoints endpoints = client.customEndpoints(GenericJson.class);
        GenericJson genericJson = new GenericJson();
        int i = 1;
        genericJson.set(ID, i);
        CustomEndpoints.CustomCommand command = endpoints.callEndpointBlocking(X2, genericJson);
        GenericJson response = (GenericJson) command.execute();
        assertEquals(i * 2, ((BigDecimal) response.get(RESULT)).intValueExact());
    }

    @Test
    public void testCustomEndpointsDeprecatedMethod() throws IOException {
        CustomEndpoints endpoints = client.customEndpoints();
        GenericJson genericJson = new GenericJson();
        int i = 1;
        genericJson.set(ID, i);
        CustomEndpoints.CustomCommand command = endpoints.callEndpointBlocking(X2, genericJson);
        GenericJson response = (GenericJson) command.execute();
        assertEquals(i * 2, ((BigDecimal) response.get(RESULT)).intValueExact());
    }

    @Test
    public void testCustomEndpointsNotExist() throws IOException, InterruptedException {
        CustomEndpoints endpoints = client.customEndpoints(GenericJson.class);
        GenericJson genericJson = new GenericJson();
        CustomEndpoints.CustomCommand command = endpoints.callEndpointBlocking(NOT_EXIST_CUSTOM_ENDPOINT, genericJson);
        try {
            command.execute();
        } catch (KinveyJsonResponseException exception) {
            assertNotNull(exception);
        }
    }

    /*
    * CustomEndpoint = 'x2'
    * function onRequest(request, response, modules) {
    *     response.body = {RESULT:request.body.id*2};
    *     response.complete();
    * }
    */
    @Test
    public void testAsyncCustomEndpoints() throws IOException, InterruptedException {
        AsyncCustomEndpoints endpoints = client.customEndpoints(GenericJson.class);
        GenericJson genericJson = new GenericJson();
        final int i = 1;
        genericJson.set(ID, i);
        DefaultKinveyClientCallback<GenericJson> callback = callEndpoint(X2, endpoints, genericJson);
        assertEquals(i * 2, ((BigDecimal) callback.result.get(RESULT)).intValueExact());
    }

    @Test
    public void testAsyncCustomEndpointsDeprecatedMethod() throws IOException, InterruptedException {
        AsyncCustomEndpoints endpoints = client.customEndpoints();
        GenericJson genericJson = new GenericJson();
        int i = 1;
        genericJson.set(ID, i);
        DefaultKinveyClientCallback<GenericJson> callback = callEndpoint(X2, endpoints, genericJson);
        assertEquals(i * 2, ((BigDecimal) callback.result.get(RESULT)).intValueExact());
    }

    @Test
    public void testAsyncCustomEndpointsNotExist() throws IOException, InterruptedException {
        AsyncCustomEndpoints endpoints = client.customEndpoints(GenericJson.class);
        GenericJson genericJson = new GenericJson();
        final int i = 1;
        genericJson.set(ID, i);
        DefaultKinveyClientCallback<GenericJson> callback = callEndpoint(NOT_EXIST_CUSTOM_ENDPOINT, endpoints, genericJson);
        assertNotNull(callback.error);
    }

    @Test
    public void testAsyncCustomEndpointsInputNotExist() throws IOException, InterruptedException {
        AsyncCustomEndpoints endpoints = client.customEndpoints(GenericJson.class);
        DefaultKinveyClientCallback<GenericJson> callback = callEndpoint(X2, endpoints, null);
        assertNotNull(callback.result);
    }

    /**
     CustomEndpoint = 'getPerson'
     function onRequest(request, response, modules) {
         response.body = {"name":"TestName"};
         response.complete();
     }
     */
    @Test
    public void testCustomEndpointsCustomClass() throws IOException, InterruptedException {
        AsyncCustomEndpoints endpoints = client.customEndpoints(Person.class);
        DefaultKinveyClientCallback<Person> callback = callCustomJsonEndpointAsync(GET_PERSON, endpoints, null);
        assertNotNull(callback.result);
        assertTrue(callback.result instanceof Person);
    }

    @Test
    public void testCustomEndpointsWrongCustomClass() throws IOException, InterruptedException {
        AsyncCustomEndpoints endpoints = client.customEndpoints(Person[].class);
        DefaultKinveyClientCallback<Person> callback = callCustomJsonEndpointAsync(GET_PERSON, endpoints, null);
        assertNotNull(callback.error);
    }

    /**
     CustomEndpoint = 'getPersonList'
     function onRequest(request, response, modules) {
         response.body = [{"name":"TestName"}];
         response.complete();
     }
     */
    @Test
    public void testCustomEndpointsCustomClassArray() throws IOException, InterruptedException {
        AsyncCustomEndpoints endpoints = client.customEndpoints(Person.class);
        DefaultKinveyListCallback<Person> callback = callCustomJsonEndpointListAsync(GET_PERSON_LIST, endpoints, null);
        assertNotNull(callback.result);
        assertTrue(callback.result instanceof List);
        assertTrue(callback.result.get(0) instanceof Person);
    }

    @Test
    public void testCustomEndpointsWrongCustomClassArray() throws IOException, InterruptedException {
        AsyncCustomEndpoints endpoints = client.customEndpoints(Person[].class);
        DefaultKinveyListCallback<Person> callback = callCustomJsonEndpointListAsync(GET_PERSON_LIST, endpoints, null);
        assertNotNull(callback.error);
    }

    @Test
    public void testCustomEndpointsCustomClassArrayNotExist() throws IOException, InterruptedException {
        AsyncCustomEndpoints endpoints = client.customEndpoints(Person.class);
        DefaultKinveyListCallback<Person> callback = callCustomJsonEndpointListAsync(NOT_EXIST_CUSTOM_ENDPOINT, endpoints, null);
        assertNotNull(callback.error);
    }

    private DefaultKinveyClientCallback<GenericJson> callEndpoint(final String endPointName, final AsyncCustomEndpoints customEndpoints, final GenericJson json) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback<GenericJson> callback = new DefaultKinveyClientCallback<>(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                customEndpoints.callEndpoint(endPointName, json, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    private DefaultKinveyClientCallback<Person> callCustomJsonEndpointAsync(final String endPointName, final AsyncCustomEndpoints customEndpoints, final GenericJson json) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback<Person> callback = new DefaultKinveyClientCallback<>(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                customEndpoints.callEndpoint(endPointName, json, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    private DefaultKinveyListCallback<Person> callCustomJsonEndpointListAsync(final String endPointName, final AsyncCustomEndpoints customEndpoints, final GenericJson json) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyListCallback<Person> callback = new DefaultKinveyListCallback<>(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                customEndpoints.callEndpoint(endPointName, json, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    private static class DefaultKinveyClientCallback<T> implements KinveyClientCallback<T> {

        private CountDownLatch latch;
        private T result;
        private Throwable error;

        private DefaultKinveyClientCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(T result) {
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

    private static class DefaultKinveyListCallback<T> implements KinveyListCallback<T> {

        private CountDownLatch latch;
        private List<T> result;
        private Throwable error;

        private DefaultKinveyListCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(List<T> result) {
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

    @After
    public void tearDown() {
        if (client.getKinveyHandlerThread() != null) {
            try {
                client.stopKinveyHandlerThread();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

}
