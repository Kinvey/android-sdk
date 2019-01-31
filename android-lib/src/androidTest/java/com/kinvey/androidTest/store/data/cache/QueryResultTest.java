package com.kinvey.androidTest.store.data.cache;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.util.SimpleArrayMap;


import com.google.api.client.json.GenericJson;
import com.kinvey.android.Client;
import com.kinvey.android.cache.RealmCache;
import com.kinvey.android.cache.RealmCacheManager;
import com.kinvey.android.model.User;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.cache.ICacheManager;
import com.kinvey.java.query.AbstractQuery;
import com.kinvey.java.query.MongoQueryFilter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.List;


/**
 * Created by Prots on 2/2/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class QueryResultTest {
    ICacheManager cacheManager;

    private static final String TEST_TABLE = "queryResultTest3";
    private ICache<SampleGsonObject2> cache;



    @Before
    public void setup(){
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        cacheManager = new Client.Builder(mMockContext).build().getCacheManager();

        cache = cacheManager.getCache(TEST_TABLE, SampleGsonObject2.class, Long.MAX_VALUE);
        cache.clear();

        User user = new User();
        user.setId("testId");
        Client.sharedInstance().setActiveUser(user);
    }

    @Test
    public void testInClause(){


        cache.save(new SampleGsonObject2("1", "test1",0));
        cache.save(new SampleGsonObject2("2", "test2",0));

        assertEquals(2, cache.get(new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).in("_id", new String[]{"1", "2"})).size());
        assertEquals(1, cache.get(new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).in("_id", new String[]{"1", "3"})).size());
        assertEquals(0, cache.get(new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).in("_id", new String[]{"4", "3"})).size());
        assertEquals(2, cache.get(new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).in("_id", new String[]{"1", "2", "3", "4"})).size());
    }

    @Test
    public void testNotIn(){


        cache.save(new SampleGsonObject2("1", "test1",0));
        cache.save(new SampleGsonObject2("2", "test2",0));

        assertEquals(0, cache.get(new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).notIn("_id", new String[]{"1", "2"})).size());
        assertEquals(1, cache.get(new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).notIn("_id", new String[]{"1", "3"})).size());
        assertEquals(2, cache.get(new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).notIn("_id", new String[]{"4", "3"})).size());
        assertEquals(0, cache.get(new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).notIn("_id", new String[]{"1", "2", "3", "4"})).size());
    }


    @Test
    public void testNumeric(){


        cache.save(new SampleGsonObject2("1", "test1",1));
        cache.save(new SampleGsonObject2("2", "test2",2));
        cache.save(new SampleGsonObject2("3", "test2",3));

        assertEquals(1, cache.get(new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).lessThan("test", 2)).size());
        assertEquals(1, cache.get(new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).greaterThan("test", 2)).size());
        assertEquals(2, cache.get(new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).lessThanEqualTo("test", 2)).size());
        assertEquals(2, cache.get(new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).greaterThanEqualTo("test", 2)).size());
        assertEquals(2, cache.get(new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).notEqual("test", 2)).size());
    }

    @Test
    public void testNot(){
        cache.save(new SampleGsonObject2("1", "test1", 1));
        cache.save(new SampleGsonObject2("2", "test2", 2));
        cache.save(new SampleGsonObject2("3", "test2", 3));

        assertEquals(2, cache.get(new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).lessThan("test", 2).not()).size());
        assertEquals(2, cache.get(new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).equals("test", 2).not()).size());
    }

    @Test
    public void testOr(){

        cache.save(new SampleGsonObject2("1", "test1",1));
        cache.save(new SampleGsonObject2("2", "test2",2));
        cache.save(new SampleGsonObject2("3", "test2",3));

        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).lessThan("test", 2);
        Query q2 = new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).greaterThan("test", 2);


        Query q3 = new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).lessThan("test", 0).greaterThan("test", 5);
        Query q4 = new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).equals("test", 1);

        assertEquals(2, cache.get(q.or(q2)).size());
        assertEquals(1, cache.get(q3.or(q4)).size());

    }

    @Test
    public void testSort(){

        cache.save(new SampleGsonObject2("1", "test1",1));
        cache.save(new SampleGsonObject2("2", "test2", 2));
        cache.save(new SampleGsonObject2("3", "test2", 3));

        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).greaterThanEqualTo("test", 1).addSort("test", AbstractQuery.SortOrder.DESC);


        List<SampleGsonObject2> objects = cache.get(q);

        assertEquals(objects.size(), 3);
        for (int i = 0 ; i < 3; i++){
            assertTrue(objects.get(i).test == (int)(3-i));
        }

    }



}
