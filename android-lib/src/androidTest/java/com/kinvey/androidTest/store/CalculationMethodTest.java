package com.kinvey.androidTest.store;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.store.DataStore;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.callback.CustomKinveyClientCallback;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.model.Aggregation;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.TEST_USERNAME;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by yuliya on 10/06/17.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class CalculationMethodTest {

    private Client client;

    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
    }

    @Test
    public void testSaveStringArrayToRealmLocally() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        ICache<Person> cache = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE);

        Person person = new Person(TEST_USERNAME);
        person.setHeight(170);

        Person person2 = new Person(TEST_USERNAME);
        person2.setHeight(180);

        testManager.saveCustom(store, person);
        testManager.saveCustom(store, person2);

        Query query = client.query();
        query = query.notEqual("age", "100200300");
        ArrayList<String> fields = new ArrayList<>();
        fields.add("height");
        Aggregation.Result[] results = cache.count(fields, query);

        assertNotNull(results);
    }
    @Test
    public void testCountLocally() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);

        Person person = new Person(TEST_USERNAME);
        person.setHeight(170);

        CustomKinveyClientCallback<Person> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        Query query = client.query();
        query = query.notEqual("age", "100200300");
        ArrayList<String> fields = new ArrayList<>();
        fields.add("height");
        Aggregation results = store.count(fields, query, null);
        assertNotNull(results);
    }

    @Test
    public void testCountNetwork() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);

        Person person = new Person(TEST_USERNAME);
        person.setWeight(50L);

        CustomKinveyClientCallback<Person> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        Query query = client.query();
        query = query.equals("username", "Test_UserName");
        ArrayList<String> fields = new ArrayList<>();
        fields.add("weight");
        Aggregation results = store.count(fields, query, null);
        assertNotNull(results);
        assertNotNull(results.getResultsFor("weight", "50"));
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
