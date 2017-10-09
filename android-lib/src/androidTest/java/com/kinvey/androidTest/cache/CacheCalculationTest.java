package com.kinvey.androidTest.cache;

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
import static junit.framework.Assert.assertTrue;

/**
 * Created by yuliya on 10/06/17.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class CacheCalculationTest {


    private Client client;

    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
    }

    @Test
    public void testCount() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        ICache<Person> cache = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE);

        Person person = new Person(TEST_USERNAME);
        person.setHeight(170);
        Person person2 = new Person("test");
        person2.setHeight(180);
        Person person3 = new Person(TEST_USERNAME);
        person3.setHeight(180);
        testManager.saveCustom(store, person);
        testManager.saveCustom(store, person2);
        testManager.saveCustom(store, person3);

        Query query = client.query().equals("username", TEST_USERNAME);
        ArrayList<String> fields = new ArrayList<>();
        fields.add("username");
        List<Aggregation.Result> results = cache.count(fields, query);

        assertTrue(results.get(0).result.intValue() == 2);
    }

    @Test
    public void testQueryToFloatField() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        ICache<Person> cache = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE);

        Person person = new Person(TEST_USERNAME);
        person.setHeight(170f);
        Person person2 = new Person(TEST_USERNAME);
        person2.setHeight(180f);
        Person person3 = new Person(TEST_USERNAME);
        person3.setHeight(180f);
        testManager.saveCustom(store, person);
        testManager.saveCustom(store, person2);
        testManager.saveCustom(store, person3);

        Query query = client.query().equals("height", 170f);
        ArrayList<String> fields = new ArrayList<>();
        fields.add("username");
        List<Aggregation.Result> results = cache.count(fields, query);

        assertTrue(results.get(0).result.intValue() == 1);
    }

    @Test
    public void testQueryToLongField() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        ICache<Person> cache = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE);

        Person person = new Person(TEST_USERNAME);
        person.setWeight(170L);
        Person person2 = new Person(TEST_USERNAME);
        person2.setWeight(180L);
        Person person3 = new Person(TEST_USERNAME);
        person3.setWeight(180L);
        testManager.saveCustom(store, person);
        testManager.saveCustom(store, person2);
        testManager.saveCustom(store, person3);

        Query query = client.query().equals("weight", 170L);
        ArrayList<String> fields = new ArrayList<>();
        fields.add("username");
        List<Aggregation.Result> results = cache.count(fields, query);

        assertTrue(results.get(0).result.intValue() == 1);
    }

    @Test
    public void testQueryToIntField() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        ICache<Person> cache = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE);

        Person person = new Person(TEST_USERNAME);
        person.setCarNumber(1);
        Person person2 = new Person(TEST_USERNAME);
        person2.setCarNumber(2);
        Person person3 = new Person(TEST_USERNAME);
        person3.setCarNumber(2);
        testManager.saveCustom(store, person);
        testManager.saveCustom(store, person2);
        testManager.saveCustom(store, person3);

        Query query = client.query().equals("carNumber", 1);
        ArrayList<String> fields = new ArrayList<>();
        fields.add("username");
        List<Aggregation.Result> results = cache.count(fields, query);

        assertTrue(results.get(0).result.intValue() == 1);
    }

    @Test
    public void testMin() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        ICache<Person> cache = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE);

        Person person = new Person(TEST_USERNAME);
        person.setHeight(170);
        Person person2 = new Person("test");
        person2.setHeight(180);
        Person person3 = new Person(TEST_USERNAME);
        person3.setHeight(180);
        testManager.saveCustom(store, person);
        testManager.saveCustom(store, person2);
        testManager.saveCustom(store, person3);

        Query query = client.query().equals("username", TEST_USERNAME);
        ArrayList<String> fields = new ArrayList<>();
        fields.add("username");
        List<Aggregation.Result> results = cache.min(fields, "height", query);

        assertTrue(results.get(0).result.intValue() == 170);
    }

    @Test
    public void testMax() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        ICache<Person> cache = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE);

        Person person = new Person(TEST_USERNAME);
        person.setHeight(170);
        Person person2 = new Person(TEST_USERNAME);
        person2.setHeight(180);
        Person person3 = new Person(TEST_USERNAME);
        person3.setHeight(180);
        testManager.saveCustom(store, person);
        testManager.saveCustom(store, person2);
        testManager.saveCustom(store, person3);

        Query query = client.query().equals("username", TEST_USERNAME);
        ArrayList<String> fields = new ArrayList<>();
        fields.add("username");
        List<Aggregation.Result> results = cache.max(fields, "height", query);

        assertTrue(results.get(0).result.intValue() == 180);
    }

    @Test
    public void testAverage() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        ICache<Person> cache = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE);

        Person person = new Person(TEST_USERNAME);
        person.setHeight(170);
        Person person2 = new Person("test");
        person2.setHeight(180);
        Person person3 = new Person(TEST_USERNAME);
        person3.setHeight(180);
        testManager.saveCustom(store, person);
        testManager.saveCustom(store, person2);
        testManager.saveCustom(store, person3);

        Query query = client.query().equals("username", TEST_USERNAME);
        ArrayList<String> fields = new ArrayList<>();
        fields.add("username");
        List<Aggregation.Result> results = cache.average(fields, "height", query);

        assertTrue(results.get(0).result.intValue() == 175);
    }

    @Test
    public void testSum() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        ICache<Person> cache = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE);

        Person person = new Person(TEST_USERNAME);
        person.setHeight(170);
        Person person2 = new Person("test");
        person2.setHeight(180);
        Person person3 = new Person(TEST_USERNAME);
        person3.setHeight(180);
        testManager.saveCustom(store, person);
        testManager.saveCustom(store, person2);
        testManager.saveCustom(store, person3);

        Query query = client.query().equals("username", TEST_USERNAME);
        ArrayList<String> fields = new ArrayList<>();
        fields.add("username");
        List<Aggregation.Result> results = cache.sum(fields, "height", query);

        assertTrue(results.get(0).result.intValue() == 350);
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
