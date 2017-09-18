package com.kinvey.androidTest.cache;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.store.DataStore;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.callback.DefaultKinveyClientCallback;
import com.kinvey.androidTest.callback.DefaultKinveyListCallback;
import com.kinvey.androidTest.callback.DefaultKinveyPullCallback;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.Query;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;

import static com.kinvey.androidTest.TestManager.TEST_USERNAME;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by yuliya on 09/14/17.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class CacheRealDataTest {

    private static final String TEST_PHONE = "123456789";
    private static final String PHONES_FIELD = "phones";

    private Client client;
    private TestManager testManager;

    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        testManager = new TestManager();
        testManager.login("test", "test", client);
    }

    @Test
    public void testSaveArrayToRealmLocally() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        Person person = testManager.createPerson();
        ArrayList<String> phones = new ArrayList<>();
        phones.add(TEST_PHONE);
        phones.add("987654321");
        person.setPhones(phones);
        DefaultKinveyClientCallback saveCallback = testManager.save(store, person);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getPhones());
        assertTrue(saveCallback.getResult().getPhones().size() == 2);
    }

    @Test
    public void testSaveArrayToServer() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        Person person = testManager.createPerson();
        ArrayList<String> phones = new ArrayList<>();
        phones.add(TEST_PHONE);
        phones.add("987654321");
        person.setPhones(phones);
        DefaultKinveyClientCallback saveCallback = testManager.save(store, person);
        assertNotNull(saveCallback.getResult());
        assertNotNull(saveCallback.getResult().getPhones());
        assertTrue(saveCallback.getResult().getPhones().size() == 2);
    }

    @Test
    public void testSaveArrayOfStringToRealmLocally() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        Person person = testManager.createPerson();
        ArrayList<String> phones = new ArrayList<>();
        phones.add("123456789");
        phones.add("987654321");
        person.setPhones(phones);
        DefaultKinveyClientCallback callback = testManager.save(store, person);
        assertNotNull(callback.getResult());
        assertNotNull(callback.getResult().getPhones());
        Query query = new Query().equals("username", TEST_USERNAME);
        DefaultKinveyListCallback listCallback = testManager.find(store, query);
        assertNotNull(listCallback.getResult());
        assertNotNull(callback.getResult().getPhones());
        assertTrue(callback.getResult().getPhones().size() == 2);
    }

    @Test
    public void testInQueryOnFindMethodLocally() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        Person person = testManager.createPerson();
        ArrayList<String> phones = new ArrayList<>();
        phones.add("123456789");
        phones.add("987654321");
        person.setPhones(phones);
        DefaultKinveyClientCallback callback = testManager.save(store, person);
        assertNotNull(callback.getResult());
        assertNotNull(callback.getResult().getPhones());
        Query query = new Query().in("username", new String[]{TEST_USERNAME});
        DefaultKinveyListCallback listCallback = testManager.find(store, query);
        assertNotNull(listCallback.getResult());
        assertTrue(listCallback.getResult().size() == 1);
    }

    @Test
    @Ignore // TODO: 18.09.2017 not implemented yet
    public void testInQueryInListField() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        Person person = testManager.createPerson();
        ArrayList<String> phones = new ArrayList<>();
        phones.add(TEST_PHONE);
        phones.add("987654321");
        person.setPhones(phones);
        Person person2 = testManager.createPerson();
        ArrayList<String> phones2 = new ArrayList<>();
        phones2.add(TEST_PHONE);
        phones2.add("567483912");
        person2.setPhones(phones2);

        DefaultKinveyClientCallback saveCallback = testManager.save(store, person);
        assertNotNull(saveCallback.getResult());
        saveCallback = testManager.save(store, person2);
        assertNotNull(saveCallback.getResult().getPhones());
        Query query = new Query().in(PHONES_FIELD, new String[]{TEST_PHONE});
        DefaultKinveyListCallback findCallback = testManager.find(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().size() == 2);

        query = new Query().in(PHONES_FIELD, new String[]{"987654321"});
        findCallback = testManager.find(store, query);
        assertNotNull(findCallback.getResult());
        assertTrue(findCallback.getResult().size() == 1);
    }


    @Test
    @Ignore // TODO: 18.09.2017 not implemented yet
    public void testMissingLeftHandSideOfOR() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);

        Query query = new Query().in(PHONES_FIELD, new String[]{TEST_PHONE});
        DefaultKinveyPullCallback pullCallback = testManager.pull(store, query);
        assertNotNull(pullCallback.getResult());

        DefaultKinveyListCallback listCallback = testManager.find(store, query);
        assertNotNull(listCallback.getResult());

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
