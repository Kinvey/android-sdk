package com.kinvey.androidTest.store;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.store.DataStore;
import com.kinvey.androidTest.TestManager;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.Constants;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.model.KinveyAbstractReadResponse;
import com.kinvey.java.model.KinveyCountResponse;
import com.kinvey.java.model.KinveyQueryCacheResponse;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.BaseDataStore;
import com.kinvey.java.store.QueryCacheItem;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static com.kinvey.androidTest.TestManager.PASSWORD;
import static com.kinvey.androidTest.TestManager.USERNAME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by yuliya on 12/27/17.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DeltaCacheTest {

    private Client client;
    private TestManager<Person> testManager;
    private DataStore<Person> store;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        testManager = new TestManager<>();
        testManager.login(USERNAME, PASSWORD, client);
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
    public void testDeltaSyncPull() throws IOException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        Query query = client.query();
        String lastRequestTime = "Time";

        NetworkManager.QueryCacheGet mockCacheGet = mock(NetworkManager.QueryCacheGet.class);
        KinveyQueryCacheResponse<Person> mockResponse = new KinveyQueryCacheResponse<>();
        List<Person> people = new ArrayList<>();
        people.add(new Person("name_1"));
        people.add(new Person("name_2"));
        mockResponse.setLastRequestTime(lastRequestTime);
        mockResponse.setChanged(people);
        mockResponse.setListOfExceptions(new ArrayList<Exception>());
        when(mockCacheGet.execute()).thenReturn(mockResponse);

        NetworkManager<Person> spyNetworkManager = spy(new NetworkManager<>(Person.COLLECTION, Person.class, client));
        when(spyNetworkManager.queryCacheGetBlocking(query, lastRequestTime)).thenReturn(mockCacheGet);

        BaseDataStore<Person> store = testManager.mockBaseDataStore(client, Person.COLLECTION, Person.class, StoreType.SYNC, spyNetworkManager);
        store.setDeltaSetCachingEnabled(true);

        Field field = BaseDataStore.class.getDeclaredField("queryCache");
        field.setAccessible(true);
        ICache<QueryCacheItem> queryCache = client.getSyncManager().getCacheManager().getCache(Constants.QUERY_CACHE_COLLECTION, QueryCacheItem.class, Long.MAX_VALUE);

        queryCache.save(new QueryCacheItem(
                Person.COLLECTION,
                query.getQueryFilterMap().toString(),
                lastRequestTime));

        KinveyAbstractReadResponse<Person> response = store.pullBlocking(query);
        assertNotNull(response.getResult());
        assertEquals(0, response.getListOfExceptions().size());
        assertEquals(lastRequestTime, response.getLastRequestTime());
        assertEquals("name_1", response.getResult().get(0).getUsername());
        assertEquals("name_2", response.getResult().get(1).getUsername());
    }

    @Test
    public void testDeltaSyncPullWithAutoPagination() throws IOException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        Query query = client.query();
        String lastRequestTime = "Time";

        NetworkManager.QueryCacheGet mockCacheGet = mock(NetworkManager.QueryCacheGet.class);
        NetworkManager.GetCount mockGetCount = mock(NetworkManager.GetCount.class);
        KinveyQueryCacheResponse<Person> mockResponse = new KinveyQueryCacheResponse<>();
        KinveyCountResponse mockCountResponse = new KinveyCountResponse();
        mockCountResponse.setCount(2);
        List<Person> people = new ArrayList<>();
        people.add(new Person("name_1"));
        people.add(new Person("name_2"));
        mockResponse.setLastRequestTime(lastRequestTime);
        mockResponse.setChanged(people);
        mockResponse.setListOfExceptions(new ArrayList<Exception>());
        when(mockCacheGet.execute()).thenReturn(mockResponse);
        when(mockGetCount.execute()).thenReturn(mockCountResponse);

        NetworkManager<Person> spyNetworkManager = spy(new NetworkManager<>(Person.COLLECTION, Person.class, client));
        when(spyNetworkManager.queryCacheGetBlocking(query, lastRequestTime)).thenReturn(mockCacheGet);
        when(spyNetworkManager.getCountBlocking()).thenReturn(mockGetCount);

        BaseDataStore<Person> store = testManager.mockBaseDataStore(client, Person.COLLECTION, Person.class, StoreType.SYNC, spyNetworkManager);
        store.setDeltaSetCachingEnabled(true);
        store.setAutoPagination(true);
        store.setAutoPaginationPageSize(1);

        Field field = BaseDataStore.class.getDeclaredField("queryCache");
        field.setAccessible(true);
        ICache<QueryCacheItem> queryCache = client.getSyncManager().getCacheManager().getCache(Constants.QUERY_CACHE_COLLECTION, QueryCacheItem.class, Long.MAX_VALUE);

        queryCache.save(new QueryCacheItem(
                Person.COLLECTION,
                "{}{skip=0,limit=1,sorting=}{\"_kmd.ect\" : 1}",
                lastRequestTime));

        queryCache.save(new QueryCacheItem(
                Person.COLLECTION,
                "{}{skip=1,limit=1,sorting=}{\"_kmd.ect\" : 1}",
                lastRequestTime));

        KinveyAbstractReadResponse<Person> response = store.pullBlocking(query);
        assertNotNull(response.getResult());
        assertEquals(0, response.getListOfExceptions().size());
        assertEquals(lastRequestTime, response.getLastRequestTime());
        assertEquals("name_1", response.getResult().get(0).getUsername());
        assertEquals("name_2", response.getResult().get(1).getUsername());
    }

    /**
     * Check that Delta Sync Find works with StoreType.CACHE
     */
    @Test
    public void testDeltaSyncFindByEmptyQuery() throws IOException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        Query query = client.query();
        String lastRequestTime = "Time";

        NetworkManager.QueryCacheGet mockCacheGet = mock(NetworkManager.QueryCacheGet.class);
        KinveyQueryCacheResponse<Person> mockResponse = new KinveyQueryCacheResponse<>();
        List<Person> people = new ArrayList<>();
        people.add(new Person("name_1"));
        people.add(new Person("name_2"));
        mockResponse.setLastRequestTime(lastRequestTime);
        mockResponse.setChanged(people);
        mockResponse.setListOfExceptions(new ArrayList<Exception>());
        when(mockCacheGet.execute()).thenReturn(mockResponse);

        NetworkManager<Person> spyNetworkManager = spy(new NetworkManager<>(Person.COLLECTION, Person.class, client));
        when(spyNetworkManager.queryCacheGetBlocking(query, lastRequestTime)).thenReturn(mockCacheGet);

        BaseDataStore<Person> store = testManager.mockBaseDataStore(client, Person.COLLECTION, Person.class, StoreType.CACHE, spyNetworkManager);
        store.setDeltaSetCachingEnabled(true);


        Field field = BaseDataStore.class.getDeclaredField("queryCache");
        field.setAccessible(true);
        ICache<QueryCacheItem> queryCache = client.getSyncManager().getCacheManager().getCache(Constants.QUERY_CACHE_COLLECTION, QueryCacheItem.class, Long.MAX_VALUE);

        queryCache.save(new QueryCacheItem(
                Person.COLLECTION,
                query.getQueryFilterMap().toString(),
                lastRequestTime));

        List<Person> response = store.find(query);
        assertNotNull(response);
        assertEquals("name_1", response.get(0).getUsername());
        assertEquals("name_2", response.get(1).getUsername());
    }

    /**
     * Check Delta Sync Find by Query
     */
    @Test
    public void testDeltaSyncFindByQuery() throws IOException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        Query query = client.query().equals("name", "name_1");
        String lastRequestTime = "Time";

        NetworkManager.QueryCacheGet mockCacheGet = mock(NetworkManager.QueryCacheGet.class);
        KinveyQueryCacheResponse<Person> mockResponse = new KinveyQueryCacheResponse<>();
        List<Person> people = new ArrayList<>();
        people.add(new Person("name_1"));
        people.add(new Person("name_1"));
        mockResponse.setLastRequestTime(lastRequestTime);
        mockResponse.setChanged(people);
        mockResponse.setListOfExceptions(new ArrayList<Exception>());
        when(mockCacheGet.execute()).thenReturn(mockResponse);

        NetworkManager<Person> spyNetworkManager = spy(new NetworkManager<>(Person.COLLECTION, Person.class, client));
        when(spyNetworkManager.queryCacheGetBlocking(query, lastRequestTime)).thenReturn(mockCacheGet);

        BaseDataStore<Person> store = testManager.mockBaseDataStore(client, Person.COLLECTION, Person.class, StoreType.CACHE, spyNetworkManager);
        store.setDeltaSetCachingEnabled(true);


        Field field = BaseDataStore.class.getDeclaredField("queryCache");
        field.setAccessible(true);
        ICache<QueryCacheItem> queryCache = client.getSyncManager().getCacheManager().getCache(Constants.QUERY_CACHE_COLLECTION, QueryCacheItem.class, Long.MAX_VALUE);

        queryCache.save(new QueryCacheItem(
                Person.COLLECTION,
                query.getQueryFilterMap().toString(),
                lastRequestTime));

        List<Person> response = store.find(query);
        assertNotNull(response);
        assertEquals("name_1", response.get(0).getUsername());
        assertEquals("name_1", response.get(1).getUsername());
    }

    @Test
    public void testDefaultDeltaCacheValue() throws InterruptedException, IOException {
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertFalse(store.isDeltaSetCachingEnabled());
    }

    @Test
    public void testChangeDeltaCache() throws InterruptedException, IOException {
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        store.setDeltaSetCachingEnabled(true);
        assertTrue(store.isDeltaSetCachingEnabled());
        store.setDeltaSetCachingEnabled(false);
        assertFalse(store.isDeltaSetCachingEnabled());
    }

    @Test
    public void testDeprecatedWay() throws InterruptedException, IOException {
        client.setUseDeltaCache(true);
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.SYNC, client);
        assertTrue(store.isDeltaSetCachingEnabled());
    }

    @Test
    public void testErrorIfStoreTypeNetwork() throws InterruptedException, IOException {
        store = DataStore.collection(Person.COLLECTION, Person.class, StoreType.NETWORK, client);
        try {
            store.setDeltaSetCachingEnabled(true);
            assertTrue(false);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }



}
