package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * Created by yuliya on 11/28/17.
 */

public class Room extends GenericJson{

    @Key
    private String name;

    @Key
    private RoomAddress roomAddress;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoomAddress getRoomAddress() {
        return roomAddress;
    }

    public void setRoomAddress(RoomAddress roomAddress) {
        this.roomAddress = roomAddress;
    }
}
