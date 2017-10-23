package com.kinvey.androidTest.model;

import com.google.api.client.util.Key;

/**
 * Created by yuliya on 10/20/17.
 */

public class SelfReferencePerson extends Person {

    @Key("SelfReferencePerson")
    private SelfReferencePerson SelfReferencePerson;

    public SelfReferencePerson() {
    }

    public SelfReferencePerson getPerson() {
        return SelfReferencePerson;
    }

    public void setPerson(SelfReferencePerson person) {
        this.SelfReferencePerson = person;
    }
}
