package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * Created by yuliya on 11/28/17.
 */

public class RoomPerson extends GenericJson {

    @Key
    private String name;

    @Key
    private PersonRoomPerson person;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PersonRoomPerson getPerson() {
        return person;
    }

    public void setPerson(PersonRoomPerson person) {
        this.person = person;
    }
}
