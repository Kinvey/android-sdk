package com.kinvey.android.util;


import io.realm.Realm;

public class PersonRepositoryImpl implements PersonRepository {
    @Override
    public void createPerson(final String name) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Person person = realm.createObject(Person.class);
                person.setUsername(name);
            }
        });
        realm.close();
    }
}
