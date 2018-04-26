package com.kinvey.androidTest.cache;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.store.DataStore;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.callback.CustomKinveyReadCallback;
import com.kinvey.androidTest.callback.DefaultKinveyClientCallback;
import com.kinvey.androidTest.callback.CustomKinveyClientCallback;
import com.kinvey.androidTest.callback.CustomKinveyPullCallback;
import com.kinvey.androidTest.callback.DefaultKinveyDeleteCallback;
import com.kinvey.androidTest.model.BooleanPrimitiveListInPerson;
import com.kinvey.androidTest.model.FloatPrimitiveListInPerson;
import com.kinvey.androidTest.model.IntegerPrimitiveListInPerson;
import com.kinvey.androidTest.model.ObjectListInPerson;
import com.kinvey.androidTest.model.Person;
import com.kinvey.androidTest.model.StringGenericJson;
import com.kinvey.androidTest.model.StringPrimitiveListInPerson;
import com.kinvey.java.Query;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.TEST_USERNAME;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by yuliya on 09/14/17.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class CacheRealDataTest {

    private static final String TEST_STRING = "123456789";
    private static final Float TEST_FLOAT = 123456789.1f;
    private static final Integer TEST_INTEGER = 123456789;
    private static final Boolean TEST_BOOLEAN = true;
    private static final String STRING_LIST_FIELD = "stringList";
    private static final String FLOAT_LIST_FIELD = "floatList";
    private static final String INTEGER_LIST_FIELD = "integerList";
    private static final String BOOLEAN_LIST_FIELD = "booleanList";
    private static final String STRING_GENERIC_JSON_LIST_FIELD = "stringGenericJsons";

    private Client client;

    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
    }

    @Test
    public void testSaveStringArrayToRealmLocally() throws InterruptedException {
        TestManager<StringPrimitiveListInPerson> testManager = new TestManager<StringPrimitiveListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<StringPrimitiveListInPerson> store = DataStore.collection(Person.COLLECTION, StringPrimitiveListInPerson.class, StoreType.SYNC, client);
        StringPrimitiveListInPerson person = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> phones = new ArrayList<>();
        phones.add(TEST_STRING);
        phones.add("987654321");
        person.setStringList(phones);
        CustomKinveyClientCallback<StringPrimitiveListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getStringList());
        assertTrue(saveCallback.getResult().getStringList().size() == 2);
    }

    @Test
    public void testSaveBooleanArrayToRealmLocally() throws InterruptedException {
        TestManager<BooleanPrimitiveListInPerson> testManager = new TestManager<BooleanPrimitiveListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<BooleanPrimitiveListInPerson> store = DataStore.collection(Person.COLLECTION, BooleanPrimitiveListInPerson.class, StoreType.SYNC, client);
        BooleanPrimitiveListInPerson person = new BooleanPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<Boolean> booleanArrayList = new ArrayList<>();
        booleanArrayList.add(true);
        booleanArrayList.add(false);
        person.setBooleanList(booleanArrayList);
        CustomKinveyClientCallback<BooleanPrimitiveListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getBooleanList());
        assertTrue(saveCallback.getResult().getBooleanList().size() == 2);
    }

    @Test
    public void testSaveStringArrayToServer() throws InterruptedException {
        TestManager<StringPrimitiveListInPerson> testManager = new TestManager<StringPrimitiveListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<StringPrimitiveListInPerson> store = DataStore.collection(Person.COLLECTION, StringPrimitiveListInPerson.class, StoreType.NETWORK, client);
        StringPrimitiveListInPerson person = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(TEST_STRING);
        stringList.add("987654321");
        person.setStringList(stringList);
        CustomKinveyClientCallback<StringPrimitiveListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getStringList());
        assertTrue(saveCallback.getResult().getStringList().size() == 2);
    }

    @Test
    public void testInQueryOperatorOnFindMethodLocallyInListOfString() throws InterruptedException {
        TestManager<StringPrimitiveListInPerson> testManager = new TestManager<StringPrimitiveListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<StringPrimitiveListInPerson> store = DataStore.collection(Person.COLLECTION, StringPrimitiveListInPerson.class, StoreType.SYNC, client);
        StringPrimitiveListInPerson person = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(TEST_STRING);
        stringList.add("987654321");
        person.setStringList(stringList);
        CustomKinveyClientCallback<StringPrimitiveListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getStringList());

        StringPrimitiveListInPerson person2 = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList2 = new ArrayList<>();
        stringList2.add(TEST_STRING);
        stringList2.add("11111");
        person2.setStringList(stringList2);
        saveCallback = testManager.saveCustom(store, person2);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getStringList());

        Query query = new Query().in(STRING_LIST_FIELD, new String[]{TEST_STRING});
        CustomKinveyReadCallback<StringPrimitiveListInPerson> findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 2);

        query = new Query().in(STRING_LIST_FIELD, new String[]{"987654321"});
        findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 1);
    }

    @Test
    public void testInQueryOperatorOnFindMethodLocallyInListOfStringWithTwoParameters() throws InterruptedException {
        TestManager<StringPrimitiveListInPerson> testManager = new TestManager<StringPrimitiveListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<StringPrimitiveListInPerson> store = DataStore.collection(Person.COLLECTION, StringPrimitiveListInPerson.class, StoreType.SYNC, client);
        StringPrimitiveListInPerson person = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(TEST_STRING);
        stringList.add("987654321");
        person.setStringList(stringList);
        CustomKinveyClientCallback<StringPrimitiveListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getStringList());

        StringPrimitiveListInPerson person2 = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList2 = new ArrayList<>();
        stringList2.add(TEST_STRING);
        stringList2.add("11111");
        person2.setStringList(stringList2);
        saveCallback = testManager.saveCustom(store, person2);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getStringList());

        StringPrimitiveListInPerson person3 = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList3 = new ArrayList<>();
        stringList3.add("2222");
        stringList3.add("11111");
        person3.setStringList(stringList3);
        saveCallback = testManager.saveCustom(store, person3);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getStringList());

        Query query;
        CustomKinveyReadCallback<StringPrimitiveListInPerson> findCallback;

        query = new Query().in(STRING_LIST_FIELD, new String[]{"987654321", TEST_STRING});
        findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 2);
    }

    @Test
    public void testInQueryOperatorOnFindMethodLocallyInListOfInteger() throws InterruptedException {
        TestManager<IntegerPrimitiveListInPerson> testManager = new TestManager<IntegerPrimitiveListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<IntegerPrimitiveListInPerson> store = DataStore.collection(Person.COLLECTION, IntegerPrimitiveListInPerson.class, StoreType.SYNC, client);
        IntegerPrimitiveListInPerson person = new IntegerPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<Integer> integerArrayList = new ArrayList<>();
        integerArrayList.add(TEST_INTEGER);
        integerArrayList.add(987654321);
        person.setIntegerList(integerArrayList);
        CustomKinveyClientCallback<IntegerPrimitiveListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getIntegerList());

        IntegerPrimitiveListInPerson person2 = new IntegerPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<Integer> integerArrayList2 = new ArrayList<>();
        integerArrayList2.add(TEST_INTEGER);
        integerArrayList2.add(11111);
        person2.setIntegerList(integerArrayList2);
        saveCallback = testManager.saveCustom(store, person2);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getIntegerList());

        Query query = new Query().in(INTEGER_LIST_FIELD, new Integer[]{TEST_INTEGER});
        CustomKinveyReadCallback<IntegerPrimitiveListInPerson> findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 2);

        query = new Query().in(INTEGER_LIST_FIELD, new Integer[]{987654321});
        findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 1);
    }

    @Test
    public void testInQueryOperatorOnFindMethodLocallyInListOfBoolean() throws InterruptedException {
        TestManager<BooleanPrimitiveListInPerson> testManager = new TestManager<BooleanPrimitiveListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<BooleanPrimitiveListInPerson> store = DataStore.collection(Person.COLLECTION, BooleanPrimitiveListInPerson.class, StoreType.SYNC, client);
        BooleanPrimitiveListInPerson person = new BooleanPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<Boolean> booleanArrayList = new ArrayList<>();
        booleanArrayList.add(TEST_BOOLEAN);
        booleanArrayList.add(false);
        person.setBooleanList(booleanArrayList);
        CustomKinveyClientCallback<BooleanPrimitiveListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getBooleanList());

        BooleanPrimitiveListInPerson person2 = new BooleanPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<Boolean> booleanArrayList2 = new ArrayList<>();
        booleanArrayList2.add(TEST_BOOLEAN);
        booleanArrayList2.add(true);
        person2.setBooleanList(booleanArrayList2);
        saveCallback = testManager.saveCustom(store, person2);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getBooleanList());

        Query query = new Query().in(BOOLEAN_LIST_FIELD, new Boolean[]{TEST_BOOLEAN});
        CustomKinveyReadCallback<BooleanPrimitiveListInPerson> findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 2);

        query = new Query().in(BOOLEAN_LIST_FIELD, new Boolean[]{false});
        findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 1);
    }

    @Test
    public void testInQueryOperatorOnFindMethodLocallyInListOfFloat() throws InterruptedException {
        TestManager<FloatPrimitiveListInPerson> testManager = new TestManager<FloatPrimitiveListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<FloatPrimitiveListInPerson> store = DataStore.collection(Person.COLLECTION, FloatPrimitiveListInPerson.class, StoreType.SYNC, client);
        FloatPrimitiveListInPerson person = new FloatPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<Float> floatArrayList = new ArrayList<>();
        floatArrayList.add(TEST_FLOAT);
        floatArrayList.add(987654321.1f);
        person.setFloatList(floatArrayList);
        CustomKinveyClientCallback<FloatPrimitiveListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getFloatList());

        FloatPrimitiveListInPerson person2 = new FloatPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<Float> floatArrayList2 = new ArrayList<>();
        floatArrayList2.add(TEST_FLOAT);
        floatArrayList2.add(11111.1f);
        person2.setFloatList(floatArrayList2);
        saveCallback = testManager.saveCustom(store, person2);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getFloatList());

        Query query = new Query().in(FLOAT_LIST_FIELD, new Float[]{TEST_FLOAT});
        CustomKinveyReadCallback<FloatPrimitiveListInPerson> findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 2);

        query = new Query().in(FLOAT_LIST_FIELD, new Float[]{987654321.1f});
        findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 1);
    }

    @Test
    public void testInWithOtherOperators() throws InterruptedException {
        TestManager<StringPrimitiveListInPerson> testManager = new TestManager<StringPrimitiveListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);

        DataStore<StringPrimitiveListInPerson> store = DataStore.collection(Person.COLLECTION, StringPrimitiveListInPerson.class, StoreType.SYNC, client);
        StringPrimitiveListInPerson person = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(TEST_STRING);
        stringList.add("987654321");
        person.setStringList(stringList);

        StringPrimitiveListInPerson person2 = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList2 = new ArrayList<>();
        stringList2.add(TEST_STRING);
        stringList2.add("567483912");
        person2.setStringList(stringList2);

        StringPrimitiveListInPerson person3 = new StringPrimitiveListInPerson("NEW_PERSON");
        ArrayList<String> stringList3 = new ArrayList<>();
        stringList3.add(TEST_STRING);
        stringList3.add("567483912");
        person3.setStringList(stringList3);

        StringPrimitiveListInPerson person4 = new StringPrimitiveListInPerson("NEW_PERSON");
        ArrayList<String> stringList4 = new ArrayList<>();
        stringList4.add("567483912");
        stringList4.add("567483912");
        person3.setStringList(stringList4);

        CustomKinveyClientCallback<StringPrimitiveListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person2);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person3);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person4);
        assertNotNull(saveCallback.getResult());

        CustomKinveyReadCallback findCallback;
        Query query;

        query = new Query().equals("username", "NEW_PERSON");
        findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 2);

        query = new Query().in(STRING_LIST_FIELD, new String[]{TEST_STRING}).equals("username", "NEW_PERSON");
        findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 1);
    }

    @Test
    public void testGetFirstMethodWithInOperator() throws InterruptedException {
        TestManager<StringPrimitiveListInPerson> testManager = new TestManager<StringPrimitiveListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);

        DataStore<StringPrimitiveListInPerson> store = DataStore.collection(Person.COLLECTION, StringPrimitiveListInPerson.class, StoreType.SYNC, client);
        StringPrimitiveListInPerson person = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(TEST_STRING);
        stringList.add("987654321");
        person.setStringList(stringList);

        StringPrimitiveListInPerson person2 = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList2 = new ArrayList<>();
        stringList2.add(TEST_STRING);
        stringList2.add("567483912");
        person2.setStringList(stringList2);

        StringPrimitiveListInPerson person3 = new StringPrimitiveListInPerson("NEW_PERSON");
        ArrayList<String> stringList3 = new ArrayList<>();
        stringList3.add(TEST_STRING);
        stringList3.add("567483912");
        person3.setStringList(stringList3);

        StringPrimitiveListInPerson person4 = new StringPrimitiveListInPerson("NEW_PERSON");
        ArrayList<String> stringList4 = new ArrayList<>();
        stringList4.add("567483912");
        stringList4.add("567483912");
        person4.setStringList(stringList4);

        StringPrimitiveListInPerson person5 = new StringPrimitiveListInPerson("NEW_PERSON");
        ArrayList<String> stringList5 = new ArrayList<>();
        stringList5.add(TEST_STRING);
        stringList5.add("567483912");
        person3.setStringList(stringList5);

        CustomKinveyClientCallback<StringPrimitiveListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person2);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person3);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person4);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person5);
        assertNotNull(saveCallback.getResult());

        Query query = new Query().in(STRING_LIST_FIELD, new String[]{TEST_STRING}).equals("username", "NEW_PERSON");

        StringPrimitiveListInPerson firstPerson;
        firstPerson = client.getCacheManager().getCache(Person.COLLECTION, StringPrimitiveListInPerson.class, Long.MAX_VALUE).getFirst(query);

        assertNotNull(firstPerson);
        assertTrue(firstPerson.getId().equals(person3.getId()));
    }

    @Test
    public void testDeleteMethod() throws InterruptedException {
        // Arrange
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(USERNAME, PASSWORD, client);

        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        Person person1 = new Person("Person1");
        person1.setAge("1");
        Person person2 = new Person("Person2");
        person2.setAge("2");
        Person person3 = new Person("Person3");
        person3.setAge("3");
        Person person4 = new Person("Person4");
        person4.setAge("4");

        DefaultKinveyClientCallback saveCallback = testManager.save(store, person1);
        assertNotNull(saveCallback.getResult());

        saveCallback = null;
        saveCallback = testManager.save(store, person2);
        assertNotNull(saveCallback.getResult());

        saveCallback = null;
        saveCallback = testManager.save(store, person3);
        assertNotNull(saveCallback.getResult());

        saveCallback = null;
        saveCallback = testManager.save(store, person4);
        assertNotNull(saveCallback.getResult());

        int allItemsBefore = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).get().size();
        assertTrue(allItemsBefore == 4);

        // Act
        Query query = new Query().equals("username", "Person2");
        int deletedPersonCount = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).delete(query);
        int allItemsAfter = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).get().size();
        List<Person> itemsAfter = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).get();

        // Assert
        assertTrue(deletedPersonCount == 1);
        assertTrue(allItemsAfter == 3);
        assertNotNull(itemsAfter);
        String username1 = itemsAfter.get(0).getUsername();
        String username2 = itemsAfter.get(1).getUsername();
        String username3 = itemsAfter.get(2).getUsername();
        assertTrue(username1.equalsIgnoreCase("Person1") || username1.equalsIgnoreCase("Person3") || username1.equalsIgnoreCase("Person4"));
        assertTrue(username2.equalsIgnoreCase("Person1") || username2.equalsIgnoreCase("Person3") || username2.equalsIgnoreCase("Person4"));
        assertTrue(username3.equalsIgnoreCase("Person1") || username3.equalsIgnoreCase("Person3") || username3.equalsIgnoreCase("Person4"));
    }

    @Test
    public void testDeleteMethodSkip1() throws InterruptedException {
        // Arrange
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(USERNAME, PASSWORD, client);

        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        Person person1 = new Person("Person1");
        person1.setAge("1");
        Person person2 = new Person("Person2");
        person2.setAge("2");
        Person person3 = new Person("Person3");
        person3.setAge("3");
        Person person4 = new Person("Person4");
        person4.setAge("4");

        DefaultKinveyClientCallback saveCallback = testManager.save(store, person1);
        assertNotNull(saveCallback.getResult());

        saveCallback = null;
        saveCallback = testManager.save(store, person2);
        assertNotNull(saveCallback.getResult());

        saveCallback = null;
        saveCallback = testManager.save(store, person3);
        assertNotNull(saveCallback.getResult());

        saveCallback = null;
        saveCallback = testManager.save(store, person4);
        assertNotNull(saveCallback.getResult());

        int allItemsBefore = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).get().size();
        assertTrue(allItemsBefore == 4);

        // Act
        Query query = new Query().setSkip(1);
        int deletedPersonCount = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).delete(query);
        int allItemsAfter = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).get().size();
        List<Person> itemsAfter = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).get();

        // Assert
        assertTrue(deletedPersonCount == 3);
        assertTrue(allItemsAfter == 1);
        assertNotNull(itemsAfter);
    }

    @Test
    public void testDeleteMethodSkip1Limit2() throws InterruptedException {
        // Arrange
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(USERNAME, PASSWORD, client);

        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        Person person1 = new Person("Person1");
        person1.setAge("1");
        Person person2 = new Person("Person2");
        person2.setAge("2");
        Person person3 = new Person("Person3");
        person3.setAge("3");
        Person person4 = new Person("Person4");
        person4.setAge("4");

        DefaultKinveyClientCallback saveCallback = testManager.save(store, person1);
        assertNotNull(saveCallback.getResult());

        saveCallback = null;
        saveCallback = testManager.save(store, person2);
        assertNotNull(saveCallback.getResult());

        saveCallback = null;
        saveCallback = testManager.save(store, person3);
        assertNotNull(saveCallback.getResult());

        saveCallback = null;
        saveCallback = testManager.save(store, person4);
        assertNotNull(saveCallback.getResult());

        int allItemsBefore = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).get().size();
        assertTrue(allItemsBefore == 4);

        // Act
        Query query = new Query().setSkip(1).setLimit(2);
        int deletedPersonCount = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).delete(query);
        int allItemsAfter = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).get().size();
        List<Person> itemsAfter = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).get();

        // Assert
        assertTrue(deletedPersonCount == 2);
        assertTrue(allItemsAfter == 2);
        assertNotNull(itemsAfter);
    }

    @Test
    public void testDeleteMethodSkip1LimitExceedsSize() throws InterruptedException {
        // Arrange
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(USERNAME, PASSWORD, client);

        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        Person person1 = new Person("Person1");
        person1.setAge("1");
        Person person2 = new Person("Person2");
        person2.setAge("2");
        Person person3 = new Person("Person3");
        person3.setAge("3");
        Person person4 = new Person("Person4");
        person4.setAge("4");

        DefaultKinveyClientCallback saveCallback = testManager.save(store, person1);
        assertNotNull(saveCallback.getResult());

        saveCallback = null;
        saveCallback = testManager.save(store, person2);
        assertNotNull(saveCallback.getResult());

        saveCallback = null;
        saveCallback = testManager.save(store, person3);
        assertNotNull(saveCallback.getResult());

        saveCallback = null;
        saveCallback = testManager.save(store, person4);
        assertNotNull(saveCallback.getResult());

        int allItemsBefore = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).get().size();
        assertTrue(allItemsBefore == 4);

        // Act
        Query query = new Query().setSkip(1).setLimit(5);
        int deletedPersonCount = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).delete(query);
        int allItemsAfter = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).get().size();
        List<Person> itemsAfter = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).get();

        // Assert
        assertTrue(deletedPersonCount == 3);
        assertTrue(allItemsAfter == 1);
        assertNotNull(itemsAfter);
    }

    @Test
    public void testDeleteMethodSkipExceedsSize() throws InterruptedException {
        // Arrange
        TestManager<Person> testManager = new TestManager<>();
        testManager.login(USERNAME, PASSWORD, client);

        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        Person person1 = new Person("Person1");
        person1.setAge("1");
        Person person2 = new Person("Person2");
        person2.setAge("2");
        Person person3 = new Person("Person3");
        person3.setAge("3");
        Person person4 = new Person("Person4");
        person4.setAge("4");

        DefaultKinveyClientCallback saveCallback = testManager.save(store, person1);
        assertNotNull(saveCallback.getResult());

        saveCallback = null;
        saveCallback = testManager.save(store, person2);
        assertNotNull(saveCallback.getResult());

        saveCallback = null;
        saveCallback = testManager.save(store, person3);
        assertNotNull(saveCallback.getResult());

        saveCallback = null;
        saveCallback = testManager.save(store, person4);
        assertNotNull(saveCallback.getResult());

        int allItemsBefore = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).get().size();
        assertTrue(allItemsBefore == 4);

        // Act
        Query query = new Query().setSkip(4);
        int deletedPersonCount = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).delete(query);
        int allItemsAfter = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).get().size();
        List<Person> itemsAfter = client.getCacheManager().getCache(Person.COLLECTION, Person.class, Long.MAX_VALUE).get();

        // Assert
        assertTrue(deletedPersonCount == 0);
        assertTrue(allItemsAfter == 4);
        assertNotNull(itemsAfter);
    }

    @Test
    public void testDeleteMethodWithInOperator() throws InterruptedException {
        TestManager<StringPrimitiveListInPerson> testManager = new TestManager<StringPrimitiveListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);

        DataStore<StringPrimitiveListInPerson> store = DataStore.collection(Person.COLLECTION, StringPrimitiveListInPerson.class, StoreType.SYNC, client);
        StringPrimitiveListInPerson person = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(TEST_STRING);
        stringList.add("987654321");
        person.setStringList(stringList);

        StringPrimitiveListInPerson person2 = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList2 = new ArrayList<>();
        stringList2.add(TEST_STRING);
        stringList2.add("567483912");
        person2.setStringList(stringList2);

        StringPrimitiveListInPerson person3 = new StringPrimitiveListInPerson("NEW_PERSON");
        ArrayList<String> stringList3 = new ArrayList<>();
        stringList3.add(TEST_STRING);
        stringList3.add("567483912");
        person3.setStringList(stringList3);

        StringPrimitiveListInPerson person4 = new StringPrimitiveListInPerson("NEW_PERSON");
        ArrayList<String> stringList4 = new ArrayList<>();
        stringList4.add("567483912");
        stringList4.add("567483912");
        person3.setStringList(stringList4);

        CustomKinveyClientCallback<StringPrimitiveListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person2);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person3);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person4);
        assertNotNull(saveCallback.getResult());

        int allItems = client.getCacheManager().getCache(Person.COLLECTION, StringPrimitiveListInPerson.class, Long.MAX_VALUE).get().size();
        assertTrue(allItems == 4);

        Query query = new Query().in(STRING_LIST_FIELD, new String[]{TEST_STRING}).equals("username", "NEW_PERSON");
        int deletedItems = client.getCacheManager().getCache(Person.COLLECTION, StringPrimitiveListInPerson.class, Long.MAX_VALUE).delete(query);
        assertTrue(deletedItems == 1);
        allItems = client.getCacheManager().getCache(Person.COLLECTION, StringPrimitiveListInPerson.class, Long.MAX_VALUE).get().size();
        assertTrue(allItems == 3);
    }

    @Test
    public void testCountMethodWithInOperator() throws InterruptedException {
        TestManager<StringPrimitiveListInPerson> testManager = new TestManager<StringPrimitiveListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);

        DataStore<StringPrimitiveListInPerson> store = DataStore.collection(Person.COLLECTION, StringPrimitiveListInPerson.class, StoreType.SYNC, client);
        StringPrimitiveListInPerson person = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(TEST_STRING);
        stringList.add("987654321");
        person.setStringList(stringList);

        StringPrimitiveListInPerson person2 = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList2 = new ArrayList<>();
        stringList2.add(TEST_STRING);
        stringList2.add("567483912");
        person2.setStringList(stringList2);

        StringPrimitiveListInPerson person3 = new StringPrimitiveListInPerson("NEW_PERSON");
        ArrayList<String> stringList3 = new ArrayList<>();
        stringList3.add(TEST_STRING);
        stringList3.add("567483912");
        person3.setStringList(stringList3);

        StringPrimitiveListInPerson person4 = new StringPrimitiveListInPerson("NEW_PERSON");
        ArrayList<String> stringList4 = new ArrayList<>();
        stringList4.add("567483912");
        stringList4.add("567483912");
        person3.setStringList(stringList4);

        CustomKinveyClientCallback<StringPrimitiveListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person2);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person3);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person4);
        assertNotNull(saveCallback.getResult());

        Query query = new Query().in(STRING_LIST_FIELD, new String[]{TEST_STRING}).equals("username", "NEW_PERSON");
        long i = client.getCacheManager().getCache(Person.COLLECTION, StringPrimitiveListInPerson.class, Long.MAX_VALUE).count(query);
        assertTrue(i == 1);
    }

    @Test
    public void testGetMethodWithInOperator() throws InterruptedException {
        TestManager<StringPrimitiveListInPerson> testManager = new TestManager<StringPrimitiveListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);

        DataStore<StringPrimitiveListInPerson> store = DataStore.collection(Person.COLLECTION, StringPrimitiveListInPerson.class, StoreType.SYNC, client);
        StringPrimitiveListInPerson person = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(TEST_STRING);
        stringList.add("987654321");
        person.setStringList(stringList);

        StringPrimitiveListInPerson person2 = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList2 = new ArrayList<>();
        stringList2.add(TEST_STRING);
        stringList2.add("567483912");
        person2.setStringList(stringList2);

        StringPrimitiveListInPerson person3 = new StringPrimitiveListInPerson("NEW_PERSON");
        ArrayList<String> stringList3 = new ArrayList<>();
        stringList3.add(TEST_STRING);
        stringList3.add("567483912");
        person3.setStringList(stringList3);

        StringPrimitiveListInPerson person4 = new StringPrimitiveListInPerson("NEW_PERSON");
        ArrayList<String> stringList4 = new ArrayList<>();
        stringList4.add("567483912");
        stringList4.add("567483912");
        person4.setStringList(stringList4);

        StringPrimitiveListInPerson person5 = new StringPrimitiveListInPerson("NEW_PERSON");
        ArrayList<String> stringList5 = new ArrayList<>();
        stringList5.add(TEST_STRING);
        stringList5.add("567483912");
        person3.setStringList(stringList5);

        CustomKinveyClientCallback<StringPrimitiveListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person2);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person3);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person4);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person5);
        assertNotNull(saveCallback.getResult());

        Query query = new Query().in(STRING_LIST_FIELD, new String[]{TEST_STRING}).equals("username", "NEW_PERSON");
        List<StringPrimitiveListInPerson> persons = client.getCacheManager().getCache(Person.COLLECTION, StringPrimitiveListInPerson.class, Long.MAX_VALUE).get(query);
        assertNotNull(persons);
        assertTrue(persons.size() == 2);
    }

    @Test
    @Ignore // TODO: 21.09.2017 "NOTIN" not updated yet for list of primitives field
    public void testGetMethodWithNotInOperator() throws InterruptedException {
        TestManager<StringPrimitiveListInPerson> testManager = new TestManager<StringPrimitiveListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);

        DataStore<StringPrimitiveListInPerson> store = DataStore.collection(Person.COLLECTION, StringPrimitiveListInPerson.class, StoreType.SYNC, client);
        StringPrimitiveListInPerson person = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(TEST_STRING);
        stringList.add("987654321");
        person.setStringList(stringList);

        StringPrimitiveListInPerson person2 = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList2 = new ArrayList<>();
        stringList2.add(TEST_STRING);
        stringList2.add("567483912");
        person2.setStringList(stringList2);

        StringPrimitiveListInPerson person3 = new StringPrimitiveListInPerson("NEW_PERSON");
        ArrayList<String> stringList3 = new ArrayList<>();
        stringList3.add(TEST_STRING);
        stringList3.add("567483912");
        person3.setStringList(stringList3);

        StringPrimitiveListInPerson person4 = new StringPrimitiveListInPerson("NEW_PERSON");
        ArrayList<String> stringList4 = new ArrayList<>();
        stringList4.add("567483912");
        stringList4.add("567483912");
        person4.setStringList(stringList4);

        StringPrimitiveListInPerson person5 = new StringPrimitiveListInPerson("NEW_PERSON");
        ArrayList<String> stringList5 = new ArrayList<>();
        stringList5.add(TEST_STRING);
        stringList5.add("567483912");
        person3.setStringList(stringList5);

        CustomKinveyClientCallback<StringPrimitiveListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person2);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person3);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person4);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.saveCustom(store, person5);
        assertNotNull(saveCallback.getResult());

        Query query = new Query().notIn(STRING_LIST_FIELD, new String[]{TEST_STRING});
        List<StringPrimitiveListInPerson> persons = client.getCacheManager().getCache(Person.COLLECTION, StringPrimitiveListInPerson.class, Long.MAX_VALUE).get(query);
        assertNotNull(persons);
        assertTrue(persons.size() == 1);
    }


    @Test
    public void testSaveObjectsArrayToRealmLocally() throws InterruptedException {
        TestManager<ObjectListInPerson> testManager = new TestManager<ObjectListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<ObjectListInPerson> store = DataStore.collection(Person.COLLECTION, ObjectListInPerson.class, StoreType.SYNC, client);
        ObjectListInPerson person = new ObjectListInPerson(TEST_USERNAME);
        ArrayList<StringGenericJson> stringGenericJsons = new ArrayList<>();
        stringGenericJsons.add(new StringGenericJson(TEST_STRING));
        stringGenericJsons.add(new StringGenericJson("987654321"));
        person.setStringGenericJsons(stringGenericJsons);

        ObjectListInPerson person2 = new ObjectListInPerson(TEST_USERNAME);
        ArrayList<StringGenericJson> stringGenericJsons2 = new ArrayList<>();
        stringGenericJsons2.add(new StringGenericJson("987654321"));
        stringGenericJsons2.add(new StringGenericJson("9876543211111"));
        person2.setStringGenericJsons(stringGenericJsons2);

        CustomKinveyClientCallback<ObjectListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getStringGenericJsons());
        assertTrue(saveCallback.getResult().getStringGenericJsons().size() == 2);

        saveCallback = testManager.saveCustom(store, person2);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getStringGenericJsons());
        assertTrue(saveCallback.getResult().getStringGenericJsons().size() == 2);

        Query query = new Query().in("stringGenericJsons.string", new String[]{TEST_STRING});
        CustomKinveyReadCallback<ObjectListInPerson> findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 1);
    }

    @Test
    public void testDeleteObjectsArrayToRealmLocally() throws InterruptedException, IOException {
        TestManager<ObjectListInPerson> testManager = new TestManager<ObjectListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<ObjectListInPerson> store = DataStore.collection(Person.COLLECTION, ObjectListInPerson.class, StoreType.SYNC, client);
        ObjectListInPerson person = new ObjectListInPerson(TEST_USERNAME);
        ArrayList<StringGenericJson> stringGenericJsons = new ArrayList<>();
        stringGenericJsons.add(new StringGenericJson(TEST_STRING));
        stringGenericJsons.add(new StringGenericJson("987654321"));
        person.setStringGenericJsons(stringGenericJsons);

        ObjectListInPerson person2 = new ObjectListInPerson(TEST_USERNAME);
        ArrayList<StringGenericJson> stringGenericJsons2 = new ArrayList<>();
        stringGenericJsons2.add(new StringGenericJson("987654321"));
        stringGenericJsons2.add(new StringGenericJson("9876543211111"));
        person2.setStringGenericJsons(stringGenericJsons2);

        CustomKinveyClientCallback<ObjectListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getStringGenericJsons());
        assertTrue(saveCallback.getResult().getStringGenericJsons().size() == 2);

        CustomKinveyClientCallback<ObjectListInPerson> saveCallback2 = testManager.saveCustom(store, person2);
        assertNotNull(saveCallback2.getResult());
        assertNotNull(saveCallback2.getResult().getStringGenericJsons());
        assertTrue(saveCallback2.getResult().getStringGenericJsons().size() == 2);

        Query query = new Query().in("stringGenericJsons.string", new String[]{TEST_STRING});
        CustomKinveyReadCallback<ObjectListInPerson> findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 1);

        DefaultKinveyDeleteCallback callback = testManager.deleteCustom(store, query);
        assertNotNull(callback);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());

        assertTrue(store.count() == 1);

        callback = testManager.deleteCustom(store, saveCallback2.getResult().getId());
        assertNotNull(callback);
        assertNotNull(callback.getResult());
        assertNull(callback.getError());

        assertTrue(store.count() == 0);
    }


    @Test
    public void testMissingLeftHandSideOfOR() throws InterruptedException {
        TestManager<StringPrimitiveListInPerson> testManager = new TestManager<StringPrimitiveListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);
        testSaveStringArrayToServer();
        DataStore<StringPrimitiveListInPerson> store = DataStore.collection(Person.COLLECTION, StringPrimitiveListInPerson.class, StoreType.SYNC, client);
        Query query = new Query().in(STRING_LIST_FIELD, new String[]{TEST_STRING});
        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, query);
        assertNotNull(pullCallback.getResult());
        CustomKinveyReadCallback listCallback = testManager.findCustom(store, query);
        assertNotNull(listCallback.getResult());
    }

    @Test
    public void testInQueryOperatorOnFindMethodLocallyComplexSearch() throws InterruptedException {
        TestManager<StringPrimitiveListInPerson> testManager = new TestManager<StringPrimitiveListInPerson>();
        testManager.login(USERNAME, PASSWORD, client);
        DataStore<StringPrimitiveListInPerson> store = DataStore.collection(Person.COLLECTION, StringPrimitiveListInPerson.class, StoreType.SYNC, client);
        StringPrimitiveListInPerson person = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(TEST_STRING);
        stringList.add("987654321");
        person.setStringList(stringList);
        CustomKinveyClientCallback<StringPrimitiveListInPerson> saveCallback = testManager.saveCustom(store, person);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getStringList());

        StringPrimitiveListInPerson person2 = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList2 = new ArrayList<>();
        person2.setAge("25");
        stringList2.add(TEST_STRING);
        stringList2.add("11111");
        person2.setStringList(stringList2);
        saveCallback = testManager.saveCustom(store, person2);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getStringList());

        StringPrimitiveListInPerson person3 = new StringPrimitiveListInPerson(TEST_USERNAME);
        ArrayList<String> stringList3 = new ArrayList<>();
        stringList3.add("7654321");
        stringList3.add("1234567");
        person3.setStringList(stringList3);
        saveCallback = testManager.saveCustom(store, person3);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getStringList());

        Query query;
        CustomKinveyReadCallback<StringPrimitiveListInPerson> findCallback;

        query = (new Query().equals("_acl.creator", client.getActiveUser().getId()));
        findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 3);

        query = new Query().in(STRING_LIST_FIELD, new String[]{"11111", "987654321"});
        findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 2);

        query = (new Query().equals("_acl.creator", client.getActiveUser().getId())).and(new Query().in(STRING_LIST_FIELD, new String[]{"11111", "987654321"}));
        findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 2);

        query = (new Query().equals("_acl.creator", client.getActiveUser().getId())).or(new Query().in(STRING_LIST_FIELD, new String[]{"11111", "987654321"}));
        findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 3);

        query = (new Query().equals("age", "25")).or(new Query().in(STRING_LIST_FIELD, new String[]{"11111", "987654321"}));
        findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 3);

        query = (new Query().equals("age", "25")).and(new Query().in(STRING_LIST_FIELD, new String[]{"11111", "987654321"}));
        findCallback = testManager.findCustom(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().getResult().size() == 1);

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
