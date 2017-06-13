package com.kinvey.android;

import com.kinvey.android.util.LibraryProjectTestRunner;
import com.kinvey.android.util.Person;
import com.kinvey.android.util.PersonRepository;
import com.kinvey.android.util.PersonRepositoryImpl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.annotation.Config;

import io.realm.Realm;
import io.realm.log.RealmLog;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(LibraryProjectTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@SuppressStaticInitializationFor("io.realm.internal.Util")
@PrepareForTest({Realm.class, RealmLog.class})
public class RealmTest {

    private static final String TEST_USERNAME = "Test_UserName";

    @Rule
    public PowerMockRule rule = new PowerMockRule();
    private Realm mockRealm;

    @Before
    public void setup() {
        mockStatic(RealmLog.class);
        mockStatic(Realm.class);
        Realm mockRealm = PowerMockito.mock(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(mockRealm);
        this.mockRealm = mockRealm;
    }

    @Test
    public void shouldBeAbleToGetDefaultInstance() {
        assertThat(Realm.getDefaultInstance(), is(mockRealm));
    }

    @Test
    public void shouldBeAbleToMockRealmMethods() {
        when(mockRealm.isAutoRefresh()).thenReturn(true);
        assertThat(mockRealm.isAutoRefresh(), is(true));
        when(mockRealm.isAutoRefresh()).thenReturn(false);
        assertThat(mockRealm.isAutoRefresh(), is(false));
    }

    @Test
    public void shouldBeAbleToCreateARealmObject() {
        Person person = new Person();
        when(mockRealm.createObject(Person.class)).thenReturn(person);
        Person output = mockRealm.createObject(Person.class);
        assertThat(output, is(person));
    }

    @Test
    public void shouldVerifyThatPersonWasCreated() {
        doCallRealMethod().when(mockRealm).executeTransaction(Mockito.any(Realm.Transaction.class));
        Person Person = mock(Person.class);
        when(mockRealm.createObject(Person.class)).thenReturn(Person);
        PersonRepository PersonRepo = new PersonRepositoryImpl();
        PersonRepo.createPerson(TEST_USERNAME);

        // Attempting to verify that a method was called (executeTransaction) on a partial
        // mock will return unexpected results due to the partial mock. For example,
        // verifying that `executeTransaction` was called only once will fail as Powermock
        // actually calls the method 3 times for some reason. I cannot determine why at this
        // point.

        // Verify that Realm#createObject was called only once
        verify(mockRealm, times(1)).createObject(Person.class); // Verify that a Person was in fact created.

        // Verify that Person#setName() is called only once
        verify(Person, times(1)).setUsername(Mockito.anyString()); // Any string will do

        // Verify that the Realm was closed only once.
        verify(mockRealm, times(1)).close();
    }

    /**
     * Have to verify the {@link Realm#executeTransaction(Realm.Transaction)} call in a different
     * test because of a problem with Powermock: https://github.com/jayway/powermock/issues/649
     */
    @Test
    public void shouldVerifyThatTransactionWasExecuted() {
        PersonRepository PersonRepo = new PersonRepositoryImpl();
        PersonRepo.createPerson(TEST_USERNAME);

        // Verify that the begin transaction was called only once
        verify(mockRealm, times(1)).executeTransaction(Mockito.any(Realm.Transaction.class));

        // Verify that the Realm was closed only once.
        verify(mockRealm, times(1)).close();
    }

}
