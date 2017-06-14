package com.kinvey.android;

import android.content.Context;
import android.test.RenamingDelegatingContext;

import com.kinvey.android.cache.QueryHelper;
import com.kinvey.android.util.LibraryProjectTestRunner;
import com.kinvey.java.Query;
import com.kinvey.java.query.MongoQueryFilter;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.annotation.Config;

import java.util.Date;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.log.RealmLog;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(LibraryProjectTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@SuppressStaticInitializationFor("io.realm.internal.Util")
@PrepareForTest({Realm.class, RealmLog.class, RealmQuery.class})
public class QueryTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();
    private RealmQuery<DynamicRealmObject> query;

    @Before
    public void setup() {
        mockStatic(RealmLog.class);
        mockStatic(RealmQuery.class);
        mockStatic(Realm.class);
        Realm mockRealm = PowerMockito.mock(Realm.class);
        query = PowerMockito.mock(RealmQuery.class);

        when(Realm.getDefaultInstance()).thenReturn(mockRealm);
        when(query.beginGroup()).thenReturn(query);
    }

    @Test
    public void testInClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.in("_id", new String[]{"1", "2"});
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).beginGroup();
        verify(query, times(2)).equalTo(eq("_id"), anyString());
        verify(query, times(1)).or();
        verify(query, times(1)).endGroup();
    }

    @Test
    public void testNotInClause() {
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
        q.greaterThan("field", 1);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).greaterThan("field", 1);
    }

    @Test
    public void testGreaterThanLongClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.greaterThan("field", 1L);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).greaterThan("field", 1L);
    }

    @Test
    public void testGreaterThanDoubleClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.greaterThan("field", 1.0d);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).greaterThan("field", 1.0d);

    }

    @Test
    public void testGreaterThanFloatClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.greaterThan("field", 1.0f);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).greaterThan("field", 1.0f);
    }


    @Test
    public void testGreaterThanOrEqualToClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.greaterThan("_id", 1);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).greaterThan("_id", 1);
    }

}
