package com.kinvey.androidTest.store.data.network;

import android.content.Context;
import android.os.Message;

import androidx.test.runner.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyReadCallback;
import com.kinvey.android.model.User;
import com.kinvey.android.store.DataStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.callback.DefaultKinveyDeleteCallback;
import com.kinvey.androidTest.model.Location;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyReadResponse;
import com.kinvey.java.query.AbstractQuery;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class QueryNetworkTest {

    private static final String TEST_USERNAME = "Test_UserName";
    private static final String TEST_USERNAME_2 = "Test_UserName_2";
    private static final String USERNAME = "username";
    private static final String HEIGHT = "height";
    private static final String GEOLOC = "_geoloc";
    private static final int DEFAULT_TIMEOUT = 60;

    private Client client;

    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        client = new Client.Builder(mMockContext).build();
        client.enableDebugLogging();
        final CountDownLatch latch = new CountDownLatch(1);
        LooperThread looperThread = null;
        if (!client.isUserLoggedIn()) {
            looperThread = new LooperThread(new Runnable() {
                @Override
                public void run() {
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
                }
            });
            looperThread.start();
        } else {
            latch.countDown();
        }
        latch.await();
        if (looperThread != null) {
            looperThread.mHandler.sendMessage(new Message());
        }
    }

    private <T extends GenericJson> void clearBackend(DataStore<T> store) throws InterruptedException {
        Query query = client.query();
        query = query.notEqual("age", "100500");
        DefaultKinveyDeleteCallback deleteCallback = delete(store, query);
    }

    private <T extends GenericJson> DefaultKinveyDeleteCallback delete(final DataStore<T> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyDeleteCallback callback = new DefaultKinveyDeleteCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.delete(query, callback);
            }
        });
        looperThread.start();
        latch.await(120, TimeUnit.SECONDS);
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private DefaultKinveyReadCallback find(final DataStore<Person> store, final Query query, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyReadCallback callback = new DefaultKinveyReadCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.find(query, callback, null);
            }
        });
        looperThread.start();
        latch.await(seconds, TimeUnit.SECONDS);
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private DefaultKinveyReadLocCallback findLoc(final DataStore<Location> store, final Query query, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyReadLocCallback callback = new DefaultKinveyReadLocCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.find(query, callback, null);
            }
        });
        looperThread.start();
        latch.await(seconds, TimeUnit.SECONDS);
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private Person createPerson(String name) {
        return new Person(name);
    }

    private static class DefaultKinveyClientCallback implements KinveyClientCallback<Person> {

        private CountDownLatch latch;
        Person result;
        Throwable error;

        DefaultKinveyClientCallback(CountDownLatch latch) {
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

        void finish() {
            latch.countDown();
        }
    }

    private static class DefaultKinveyLocationCallback implements KinveyClientCallback<Location> {

        private CountDownLatch latch;
        Location result;
        Throwable error;

        DefaultKinveyLocationCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Location result) {
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

    private static class DefaultKinveyReadCallback implements KinveyReadCallback<Person> {

        private CountDownLatch latch;
        KinveyReadResponse<Person> result;
        Throwable error;

        DefaultKinveyReadCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(KinveyReadResponse<Person> result) {
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

    private static class DefaultKinveyReadLocCallback implements KinveyReadCallback<Location> {

        private CountDownLatch latch;
        KinveyReadResponse<Location> result;
        Throwable error;

        DefaultKinveyReadLocCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(KinveyReadResponse<Location> result) {
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

    private DefaultKinveyClientCallback save(final DataStore<Person> store, final Person person) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
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

    private DefaultKinveyLocationCallback saveLoc(final DataStore<Location> store, final Location location) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyLocationCallback callback = new DefaultKinveyLocationCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                store.save(location, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    @Test
    public void testStartsWith() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(store);
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = createPerson(TEST_USERNAME);
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        Query query = client.query();
        query = query.startsWith(USERNAME, "Tes");
        DefaultKinveyReadCallback kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.getResult().size() > 0);
        delete(store, query);
    }

    @Test
    public void testQueryString() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(store);
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = createPerson(TEST_USERNAME);
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        Query query = client.query();
        query = query.setQueryString("{\"username\":{\"$regex\":\"^Tes\"}}");
        DefaultKinveyReadCallback kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.getResult().size() > 0);
        delete(store, query);
    }

    @Test
    public void testAll() throws InterruptedException {
        DataStore<Location> store = DataStore.collection(Location.COLLECTION, Location.class, StoreType.NETWORK, client);
        clearBackend(store);
        client.getSyncManager().clear(Location.COLLECTION);
        Double[] geo = {3.0, 4.0};
        Location location = new Location();
        location.setGeo(geo);
        DefaultKinveyLocationCallback saveCallback = saveLoc(store, location);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        Query query = client.query();
        query = query.all(GEOLOC, geo);
        DefaultKinveyReadLocCallback kinveyListCallback = findLoc(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.getResult().size() > 0);
    }

    @Test
    public void testSize() throws InterruptedException {
        DataStore<Location> store = DataStore.collection(Location.COLLECTION, Location.class, StoreType.NETWORK, client);
        clearBackend(store);
        client.getSyncManager().clear(Location.COLLECTION);
        Double[] geo = {3.0, 4.0};
        Location location = new Location();
        location.setGeo(geo);
        DefaultKinveyLocationCallback saveCallback = saveLoc(store, location);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        Query query = client.query();
        query = query.size(GEOLOC, 2);
        DefaultKinveyReadLocCallback kinveyListCallback = findLoc(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.getResult().size() > 0);
    }

    @Test
    public void testLimit() throws InterruptedException {
        DataStore<Location> store = DataStore.collection(Location.COLLECTION, Location.class, StoreType.NETWORK, client);
        clearBackend(store);
        client.getSyncManager().clear(Location.COLLECTION);
        Double[] geo = {3.0, 4.0};
        Location location = new Location();
        location.setGeo(geo);
        DefaultKinveyLocationCallback saveCallback = saveLoc(store, location);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        saveCallback = saveLoc(store, location);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        Query query = client.query();
        query = query.setLimit(2);
        DefaultKinveyReadLocCallback kinveyListCallback = findLoc(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.getResult().size() == 2);
    }

    @Test
    public void testRegEx() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(store);
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = createPerson(TEST_USERNAME);
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        Query query = client.query();
        query = query.regEx(USERNAME, "^T");
        DefaultKinveyReadCallback kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.getResult().size() > 0);
        delete(store, query);
    }

    @Test
    public void testQueryAddSort() throws InterruptedException {
        float heightPersonOne = 180;
        float heightPersonTwo = 150;
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        clearBackend(store);
        client.getSyncManager().clear(Person.COLLECTION);
        Person person = createPerson(TEST_USERNAME);
        person.setHeight(heightPersonOne);
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        person = createPerson(TEST_USERNAME_2);
        person.setHeight(heightPersonTwo);
        saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        Query query = client.query();
        query = query.addSort(HEIGHT, AbstractQuery.SortOrder.ASC);
        DefaultKinveyReadCallback kinveyListCallback = find(store, query, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.getResult().size() > 1);
        assertTrue(kinveyListCallback.result.getResult().get(0).getHeight() == heightPersonTwo);
        delete(store, query);
    }

    @Test
    public void testQueryGeoWithinBox() throws InterruptedException {
        DataStore<Location> store = DataStore.collection(Location.COLLECTION, Location.class, StoreType.NETWORK, client);
        clearBackend(store);
        client.getSyncManager().clear(Location.COLLECTION);

        Double[] geo = {30.0, 40.0};
        Location location = new Location();
        location.setGeo(geo);

        DefaultKinveyLocationCallback saveCallback = saveLoc(store, location);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);

        Query query = client.query();
        Query geoQuery = query.withinBox(GEOLOC, 20, 20, 40, 50);
        DefaultKinveyReadLocCallback kinveyListCallback = findLoc(store, geoQuery, DEFAULT_TIMEOUT);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.getResult().size() > 0);
    }

    @Test
    public void testQueryGeoNearSphere() throws InterruptedException {
        DataStore<Location> store = DataStore.collection(Location.COLLECTION, Location.class, StoreType.NETWORK, client);
        clearBackend(store);
        client.getSyncManager().clear(Location.COLLECTION);

        Double[] geo = {30.0, 40.0};
        Location location = new Location();
        location.setGeo(geo);

        DefaultKinveyLocationCallback saveCallback = saveLoc(store, location);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);

        Query query1 = client.query();
        Query geoQueryNearSphere = query1.nearSphere(GEOLOC, 20, 50);
        Query query2 = client.query();
        Query geoQueryNearSphereDistance = query2.nearSphere(GEOLOC, 20, 50, 100000);

        DefaultKinveyReadLocCallback kinveyFindCallback1 = findLoc(store, geoQueryNearSphere, DEFAULT_TIMEOUT);
        DefaultKinveyReadLocCallback kinveyFindCallback2 = findLoc(store, geoQueryNearSphereDistance, DEFAULT_TIMEOUT);

        assertNull(kinveyFindCallback1.error);
        assertNotNull(kinveyFindCallback1.result);
        assertTrue(kinveyFindCallback1.result.getResult().size() > 0);

        assertNull(kinveyFindCallback2.error);
        assertNotNull(kinveyFindCallback2.result);
        assertTrue(kinveyFindCallback2.result.getResult().size() > 0);
    }

    @Test
    public void testQueryGeoWithinPolygon() throws InterruptedException {
        DataStore<Location> store = DataStore.collection(Location.COLLECTION, Location.class, StoreType.NETWORK, client);
        clearBackend(store);
        client.getSyncManager().clear(Location.COLLECTION);

        Double[] geo = {30.0, 40.0};
        Location location = new Location();
        location.setGeo(geo);

        DefaultKinveyLocationCallback saveCallback = saveLoc(store, location);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);

        Query query1 = client.query();
        Query geoQueryWithinPolygon = query1.withinPolygon(GEOLOC,
                30, 20,
                40, 30,
                50, 40,
                60, 50);

        DefaultKinveyReadLocCallback kinveyFindCallback = findLoc(store, geoQueryWithinPolygon, DEFAULT_TIMEOUT);

        assertNull(kinveyFindCallback.error);
        assertNotNull(kinveyFindCallback.result);
        assertTrue(kinveyFindCallback.result.getResult().size() > 0);
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
}