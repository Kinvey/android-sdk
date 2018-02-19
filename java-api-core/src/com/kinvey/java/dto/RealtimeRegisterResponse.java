package com.kinvey.java.dto;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * Created by yuliya on 2/19/17.
 */


public class RealtimeRegisterResponse extends GenericJson {

    @Key
    private String userChannelGroup;

    @Key
    private String publishKey;

    @Key
    private String subscribeKey;

    public String getUserChannelGroup() {
        return userChannelGroup;
    }

    public void setUserChannelGroup(String userChannelGroup) {
        this.userChannelGroup = userChannelGroup;
    }

    public String getPublishKey() {
        return publishKey;
    }

    public void setPublishKey(String publishKey) {
        this.publishKey = publishKey;
    }

    public String getSubscribeKey() {
        return subscribeKey;
    }

    public void setSubscribeKey(String subscribeKey) {
        this.subscribeKey = subscribeKey;
    }
}
