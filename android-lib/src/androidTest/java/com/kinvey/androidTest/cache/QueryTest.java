package com.kinvey.androidTest.cache;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.cache.QueryHelper;
import com.kinvey.android.cache.RealmCache;
import com.kinvey.android.cache.RealmCacheManager;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.cache.ICacheManager;
import com.kinvey.java.query.MongoQueryFilter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Prots on 30/01/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class QueryTest {

    RealmQuery<DynamicRealmObject> query;

    ICacheManager cacheManager;
    @Before
    public void setup(){
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        cacheManager = new Client.Builder(mMockContext).build().getCacheManager();

        DynamicRealm realm = DynamicRealm.getInstance(new RealmConfiguration.Builder(mMockContext).build());


        if (!realm.getSchema().contains("test")){
            realm.beginTransaction();
            realm.getSchema().create("test").addField("_id", Integer.class);
            realm.commitTransaction();
        }

        RealmQuery query = spy(RealmQuery.class);
        doReturn(query).when(query).beginGroup();
        doReturn(query).when(query).equalTo(anyString(), anyString());
        doReturn(query).when(query).endGroup();
        doReturn(query).when(query).or();
        doReturn(query).when(query).not();

    }

    @After
    public void tearDown() {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        DynamicRealm realm = DynamicRealm.getInstance(new RealmConfiguration.Builder(mMockContext).build());

        if (realm.getSchema().contains("test")){
            realm.beginTransaction();

            realm.getSchema().remove("test");
            realm.commitTransaction();
        }
    }


    @Test
    public void testInClause(){

        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.in("_id", new String[]{"1", "2"});

        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());

        verify(query, times(1)).beginGroup();
        verify(query, times(2)).equalTo(eq("_id"), anyString());
        verify(query, times(1)).or();
        verify(query, times(1)).endGroup();


    }

    @Test
    public void testNotInClause(){

        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.notIn("_id", new String[]{"1", "2"});

        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());

        verify(query, times(2)).beginGroup();
        verify(query, times(2)).equalTo(eq("_id"), anyString());
        verify(query, times(1)).or();
        verify(query, times(2)).endGroup();
        verify(query, times(1)).not();


    }


}
