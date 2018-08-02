package com.kinvey.androidTest.cache;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.cache.QueryHelper;
import com.kinvey.java.Query;
import com.kinvey.java.query.AbstractQuery;
import com.kinvey.java.query.MongoQueryFilter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.Sort;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Prots on 30/01/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class QueryTest {

    private RealmQuery<DynamicRealmObject> query;
    private static final String TEST_FIELD = "field";

    @Before
    public void setup(){
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        Realm.init(mMockContext);
        DynamicRealm realm = DynamicRealm.getInstance(new RealmConfiguration.Builder().build());

        if (!realm.getSchema().contains("test")){
            realm.beginTransaction();
            realm.getSchema().create("test").addField("_id", Integer.class);
            realm.commitTransaction();
        }

        RealmQuery query = mock(RealmQuery.class);
        doReturn(query).when(query).beginGroup();
        doReturn(query).when(query).equalTo(anyString(), anyString());
        doReturn(query).when(query).endGroup();
        doReturn(query).when(query).or();
        doReturn(query).when(query).not();
        this.query = query;
    }

    @After
    public void tearDown() {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        Realm.init(mMockContext);
        DynamicRealm realm = DynamicRealm.getInstance(new RealmConfiguration.Builder().build());
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

    @Test
    public void testGreaterThanIntClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.greaterThan(TEST_FIELD, 1);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).greaterThan(TEST_FIELD, 1);
    }

    @Test
    public void testGreaterThanLongClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.greaterThan(TEST_FIELD, 1L);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).greaterThan(TEST_FIELD, 1L);
    }

    @Test
    public void testGreaterThanDoubleClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.greaterThan(TEST_FIELD, 1.0d);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).greaterThan(TEST_FIELD, 1.0d);
    }

    @Test
    public void testGreaterThanFloatClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.greaterThan(TEST_FIELD, 1.0f);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).greaterThan(TEST_FIELD, 1.0f);
    }

    @Test
    public void testGreaterThanOrEqualToIntClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.greaterThanEqualTo(TEST_FIELD, 1);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).greaterThanOrEqualTo(TEST_FIELD, 1);
    }

    @Test
    public void testGreaterThanOrEqualToLongClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.greaterThanEqualTo(TEST_FIELD, 1L);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).greaterThanOrEqualTo(TEST_FIELD, 1L);
    }

    @Test
    public void testGreaterThanOrEqualToDoubleClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.greaterThanEqualTo(TEST_FIELD, 1.0d);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).greaterThanOrEqualTo(TEST_FIELD, 1.0d);
    }

    @Test
    public void testGreaterThanOrEqualToFloatClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.greaterThanEqualTo(TEST_FIELD, 1.0f);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).greaterThanOrEqualTo(TEST_FIELD, 1.0f);
    }


    @Test
    public void testLessThanIntClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.lessThan(TEST_FIELD, 1);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).lessThan(TEST_FIELD, 1);
    }

    @Test
    public void testLessThanLongClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.lessThan(TEST_FIELD, 1L);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).lessThan(TEST_FIELD, 1L);
    }

    @Test
    public void testLessThanDoubleClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.lessThan(TEST_FIELD, 1.0d);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).lessThan(TEST_FIELD, 1.0d);
    }

    @Test
    public void testLessThanFloatClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.lessThan(TEST_FIELD, 1.0f);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).lessThan(TEST_FIELD, 1.0f);
    }

    @Test
    public void testLessThanOrEqualToIntClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.lessThanEqualTo(TEST_FIELD, 1);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).lessThanOrEqualTo(TEST_FIELD, 1);
    }

    @Test
    public void testLessThanOrEqualToLongClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.lessThanEqualTo(TEST_FIELD, 1L);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).lessThanOrEqualTo(TEST_FIELD, 1L);
    }

    @Test
    public void testLessThanOrEqualToDoubleClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.lessThanEqualTo(TEST_FIELD, 1.0d);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).lessThanOrEqualTo(TEST_FIELD, 1.0d);
    }

    @Test
    public void testLessThanOrEqualToFloatClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.lessThanEqualTo(TEST_FIELD, 1.0f);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).lessThanOrEqualTo(TEST_FIELD, 1.0f);
    }

    @Test
    public void testNotEqualBooleanClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.notEqual(TEST_FIELD, true);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).notEqualTo(TEST_FIELD, true);
    }

    @Test
    public void testNotEqualByteClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        Byte aByte = 1;
        q.notEqual(TEST_FIELD, aByte);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).notEqualTo(TEST_FIELD, aByte);
    }

    @Test
    public void testNotEqualByteArrayClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        byte[] bytes = {1};
        q.notEqual(TEST_FIELD, bytes);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).notEqualTo(TEST_FIELD, bytes);
    }

    @Test
    public void testNotEqualShortClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        Short aShort = 12345;
        q.notEqual(TEST_FIELD, aShort);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).notEqualTo(TEST_FIELD, aShort);
    }

    @Test
    public void testNotEqualIntClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.notEqual(TEST_FIELD, 1);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).notEqualTo(TEST_FIELD, 1);
    }

    @Test
    public void testNotEqualLongClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.notEqual(TEST_FIELD, 1L);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).notEqualTo(TEST_FIELD, 1L);
    }

    @Test
    public void testNotEqualDoubleClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.notEqual(TEST_FIELD, 1d);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).notEqualTo(TEST_FIELD, 1d);
    }

    @Test
    public void testNotEqualFloatClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.notEqual(TEST_FIELD, 1f);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).notEqualTo(TEST_FIELD, 1f);
    }

    @Test
    public void testNotEqualStringClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.notEqual(TEST_FIELD, TEST_FIELD);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).notEqualTo(TEST_FIELD, TEST_FIELD);
    }

    @Test
    public void tesEqualByteClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        Byte aByte = 1;
        q.equals(TEST_FIELD, aByte);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).equalTo(TEST_FIELD, aByte);
    }

    @Test
    public void testEqualByteArrayClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        byte[] bytes = {1};
        q.equals(TEST_FIELD, bytes);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).equalTo(TEST_FIELD, bytes);
    }

    @Test
    public void testEqualShortClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        Short aShort = 12345;
        q.equals(TEST_FIELD, aShort);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).equalTo(TEST_FIELD, aShort);
    }

    @Test
    public void testEqualIntClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.equals(TEST_FIELD, 1);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).equalTo(TEST_FIELD, 1);
    }

    @Test
    public void testEqualLongClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.equals(TEST_FIELD, 1L);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).equalTo(TEST_FIELD, 1L);
    }

    @Test
    public void testEqualDoubleClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.equals(TEST_FIELD, 1d);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).equalTo(TEST_FIELD, 1d);
    }

    @Test
    public void testEqualFloatClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.equals(TEST_FIELD, 1f);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).equalTo(TEST_FIELD, 1f);
    }

    @Test
    public void testEqualStringClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.equals(TEST_FIELD, TEST_FIELD);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).equalTo(TEST_FIELD, TEST_FIELD);
    }

    @Test
    public void testAddSortASC() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.addSort(TEST_FIELD, AbstractQuery.SortOrder.ASC);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());

        //check that addSort isn't used in prepareRealmQuery
        verify(query, times(0)).beginGroup();
        verify(query, times(0)).equalTo(eq(TEST_FIELD), anyString());
        verify(query, times(0)).or();
        verify(query, times(0)).not();
        verify(query, times(0)).endGroup();

        assertEquals("{\"field\" : 1}", q.getSortString());
    }

    @Test
    public void testAddSortDESC() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.addSort(TEST_FIELD, AbstractQuery.SortOrder.DESC);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());

        //check that addSort isn't used in prepareRealmQuery
        verify(query, times(0)).beginGroup();
        verify(query, times(0)).equalTo(eq(TEST_FIELD), anyString());
        verify(query, times(0)).or();
        verify(query, times(0)).not();
        verify(query, times(0)).endGroup();

        assertEquals("{\"field\" : -1}", q.getSortString());
    }

    @Test
    public void testOrClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        Query q2 = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.in(TEST_FIELD, new String[]{"1", "2"});
        q2.in(TEST_FIELD, new String[]{"1", "2"});
        q.or(q2);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(5)).beginGroup();
        verify(query, times(4)).equalTo(eq(TEST_FIELD), anyString());
        verify(query, times(3)).or();
        verify(query, times(5)).endGroup();
    }

    @Test
    public void testAndClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        Query q2 = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.notEqual(TEST_FIELD, 1);
        q2.notEqual(TEST_FIELD, 2);
        q.and(q2);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).notEqualTo(TEST_FIELD, 1);
        verify(query, times(1)).notEqualTo(TEST_FIELD, 2);
        verify(query, times(3)).beginGroup();
        verify(query, times(3)).endGroup();
    }

    @Test
    public void testUnsupportedOperationException() {
        Query q = new Query();
        q.startsWith(TEST_FIELD, 1);
        try {
            QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        } catch (UnsupportedOperationException e) {
            assertEquals("this query is not supported by cache", e.getMessage());
        }
    }
    
}
