package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by yuliya on 11/28/17.
 */

public class PersonRoomPerson extends GenericJson {

    @Key
    private String name;

    @Key
    private RoomPerson room;

    @Key
    private List<PersonRoomPerson> personList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoomPerson getRoom() {
        return room;
    }

    public void setRoom(RoomPerson room) {
        this.room = room;
    }

    public List<PersonRoomPerson> getPersonList() {
        return personList;
    }

    public void setPersonList(List<PersonRoomPerson> personList) {
        this.personList = personList;
    }
}
