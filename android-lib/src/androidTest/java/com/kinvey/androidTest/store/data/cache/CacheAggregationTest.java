package com.kinvey.androidTest.store.data.cache;

import android.content.Context;

import androidx.test.runner.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.kinvey.android.Client;
import com.kinvey.android.model.User;
import com.kinvey.android.store.DataStore;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.model.ModelWithDifferentTypeFields;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.model.AggregateType;
import com.kinvey.java.model.Aggregation;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.TEST_USERNAME;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by yuliya on 10/06/17.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class CacheAggregationTest {


    private Client client;

    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        client = new Client.Builder(mMockContext).build();
    }

    @Test
    public void testCount() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
//        testManager.login(USERNAME, PASSWORD, client);
        User user = new User();
        user.setId("testId");
        Client.sharedInstance.setActiveUser(user);

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
        Aggregation.Result[] results = cache.group(AggregateType.COUNT, fields, null, query);

        assertTrue(results[0].getResult().intValue() == 2);
    }

    @Test
    public void testQueryToFloatField() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
//        testManager.login(USERNAME, PASSWORD, client);
        User user = new User();
        user.setId("testId");
        Client.sharedInstance.setActiveUser(user);
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
        Aggregation.Result[] results = cache.group(AggregateType.COUNT, fields, null, query);

        assertTrue(results[0].getResult().intValue() == 1);
    }

    @Test
    public void testQueryToLongField() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
//        testManager.login(USERNAME, PASSWORD, client);
        User user = new User();
        user.setId("testId");
        Client.sharedInstance.setActiveUser(user);
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
        Aggregation.Result[] results = cache.group(AggregateType.COUNT, fields, null, query);

        assertTrue(results[0].getResult().intValue() == 1);
    }

    @Test
    public void testQueryToIntField() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
//        testManager.login(USERNAME, PASSWORD, client);
        User user = new User();
        user.setId("testId");
        Client.sharedInstance.setActiveUser(user);
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
        Aggregation.Result[] results = cache.group(AggregateType.COUNT, fields, null, query);

        assertTrue(results[0].getResult().intValue() == 1);
    }

    @Test
    public void testMin() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
//        testManager.login(USERNAME, PASSWORD, client);
        User user = new User();
        user.setId("testId");
        Client.sharedInstance.setActiveUser(user);
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
        Aggregation.Result[] results = cache.group(AggregateType.MIN, fields, "height", query);

        assertTrue(results[0].getResult().intValue() == 170);
    }

    @Test
    public void testMax() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
//        testManager.login(USERNAME, PASSWORD, client);
        User user = new User();
        user.setId("testId");
        Client.sharedInstance.setActiveUser(user);
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
        Aggregation.Result[] results = cache.group(AggregateType.MAX, fields, "height", query);

        assertTrue(results[0].getResult().intValue() == 180);
    }

    @Test
    public void testAverage() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
//        testManager.login(USERNAME, PASSWORD, client);
        User user = new User();
        user.setId("testId");
        Client.sharedInstance.setActiveUser(user);
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
        Aggregation.Result[] results = cache.group(AggregateType.AVERAGE, fields, "height", query);

        assertTrue(results[0].getResult().intValue() == 175);
    }

    @Test
    public void testSum() throws InterruptedException, IOException {
        TestManager<Person> testManager = new TestManager<Person>();
//        testManager.login(USERNAME, PASSWORD, client);
        User user = new User();
        user.setId("testId");
        Client.sharedInstance.setActiveUser(user);
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
        Aggregation.Result[] results = cache.group(AggregateType.SUM, fields, "height", query);

        assertTrue(results[0].getResult().intValue() == 350);
    }

    @Test
    public void testCalculationFields() throws InterruptedException {
        TestManager<ModelWithDifferentTypeFields> testManager = new TestManager<ModelWithDifferentTypeFields>();
//        testManager.login(USERNAME, PASSWORD, client);
        User user = new User();
        user.setId("testId");
        Client.sharedInstance.setActiveUser(user);
        DataStore<ModelWithDifferentTypeFields> store = DataStore.collection(ModelWithDifferentTypeFields.COLLECTION, ModelWithDifferentTypeFields.class, StoreType.SYNC, client);
        ICache<ModelWithDifferentTypeFields> cache = client.getCacheManager().getCache(ModelWithDifferentTypeFields.COLLECTION, ModelWithDifferentTypeFields.class, Long.MAX_VALUE);

        int carNumber = 1000;
        boolean isUseAndroid = true;
        Date date = new Date();
        date.setTime(1);
        float height = 180f;
        double time = 1.2d;

        ModelWithDifferentTypeFields person = new ModelWithDifferentTypeFields(TEST_USERNAME, carNumber, isUseAndroid, date, height, time);
        ModelWithDifferentTypeFields person2 = new ModelWithDifferentTypeFields("test", carNumber, isUseAndroid, date, height, time);
        ModelWithDifferentTypeFields person3 = new ModelWithDifferentTypeFields(TEST_USERNAME, carNumber, isUseAndroid, date, height, time);

        testManager.saveCustom(store, person);
        testManager.saveCustom(store, person2);
        testManager.saveCustom(store, person3);

        Query query = client.query().equals("username", TEST_USERNAME);
        ArrayList<String> fields = new ArrayList<>();

        fields.add("carNumber");
        Aggregation.Result[] results = cache.group(AggregateType.SUM, fields, "height", query);
        assertTrue(results[0].getResult().intValue() == 360);

        fields.clear();
        fields.add("isUseAndroid");
        results = cache.group(AggregateType.SUM, fields, "height", query);
        assertTrue(results[0].getResult().intValue() == 360);

        fields.clear();
        fields.add("height");
        results = cache.group(AggregateType.SUM, fields, "height", query);
        assertTrue(results[0].getResult().intValue() == 360);

        fields.clear();
        fields.add("time");
        results = cache.group(AggregateType.SUM, fields, "height", query);
        assertTrue(results[0].getResult().intValue() == 360);


//commented out because of MLIBZ-2643
/*        fields.clear();
        fields.add("date");
        results = cache.group(AggregateType.SUM, fields, "height", query);
        assertTrue(results[0].result.intValue() == 360);*/

    }


    @After
    public void tearDown() {
        client.performLockDown();
        if (Client.getKinveyHandlerThread() != null) {
            try {
                client.stopKinveyHandlerThread();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }


}
