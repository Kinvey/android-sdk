package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * Created by yuliya on 11/28/17.
 */

public class RoomAddress extends GenericJson{

    @Key
    private String name;

    @Key
    private PersonRoomAddressPerson person;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PersonRoomAddressPerson getPerson() {
        return person;
    }

    public void setPerson(PersonRoomAddressPerson person) {
        this.person = person;
    }
}
