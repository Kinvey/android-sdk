package com.kinvey.android;

import com.kinvey.android.cache.QueryHelper;
import com.kinvey.android.util.LibraryProjectTestRunner;
import com.kinvey.java.Query;
import com.kinvey.java.query.MongoQueryFilter;

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

import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.log.RealmLog;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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

    private static final String TEST_FIELD = "field";
    private RealmQuery<DynamicRealmObject> query;

    @Rule
    public PowerMockRule rule = new PowerMockRule();

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
        q.in(TEST_FIELD, new String[]{"1", "2"});
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).beginGroup();
        verify(query, times(2)).equalTo(eq(TEST_FIELD), anyString());
        verify(query, times(1)).or();
        verify(query, times(1)).endGroup();
    }

    @Test
    public void testNotInClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.notIn(TEST_FIELD, new String[]{"1", "2"});
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(2)).beginGroup();
        verify(query, times(2)).equalTo(eq(TEST_FIELD), anyString());
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
    public void testNotEquqlStringClause() {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.notEqual(TEST_FIELD, TEST_FIELD);
        QueryHelper.prepareRealmQuery(query, q.getQueryFilterMap());
        verify(query, times(1)).notEqualTo(TEST_FIELD, TEST_FIELD);
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
}
