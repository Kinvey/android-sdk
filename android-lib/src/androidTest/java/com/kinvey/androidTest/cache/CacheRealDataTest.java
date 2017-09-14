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
import com.kinvey.androidTest.callback.DefaultKinveyPushCallback;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.Query;
import com.kinvey.java.store.StoreType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by yuliya on 09/14/17.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class CacheRealDataTest {

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
    public void testSaveArrayToRealm() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        Person person = testManager.createPerson();
        ArrayList<String> phones = new ArrayList<>();
        phones.add("123456789");
        phones.add("987654321");
        person.setPhones(phones);
        DefaultKinveyClientCallback callback = testManager.save(store, person);
        assertNotNull(callback.getResult());
        assertNotNull(callback.getResult().getPhones());
    }

    @Test
    public void testInQueryWithArray() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);

        Person person = testManager.createPerson();
        ArrayList<String> phones = new ArrayList<>();
        phones.add("123456789");
        phones.add("987654321");
        person.setPhones(phones);
        DefaultKinveyClientCallback callback = testManager.save(store, person);
        assertNotNull(callback.getResult());
        assertNotNull(callback.getResult().getPhones());

        DefaultKinveyPushCallback pushCallback = testManager.push(store);
        assertNotNull(pushCallback.getResult());

        DefaultKinveyPullCallback pullCallback = testManager.pull(store, client.query().in("Phones1", new String[]{"123456789"}));
        assertNotNull(pullCallback.getResult());

    }

    @Test
    public void testMissingLeftHandSideOfOR() throws InterruptedException {
        DataStore<Person> store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);

        DefaultKinveyPullCallback pullCallback = testManager.pull(store, new Query().in("phones", new String[]{"123456789"}));
        assertNotNull(pullCallback.getResult());

    }

}
