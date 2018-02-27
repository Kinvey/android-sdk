package com.kinvey.java.dto;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * Created by yuliya on 2/19/17.
 */

public class DeviceId extends GenericJson{

    @Key("deviceId")
    private String deviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
