package com.kinvey.java.store;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.Constants;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuliya on 2/15/17.
 */

public class RealtimeRouter {

    private static final Object lock = new Object();
    private static RealtimeRouter realtimeRouter;
    private PubNub pubnubClient;
    private String channelGroup;
    private AbstractClient client;
    private SubscribeCallback subscribeCallback;
    private Map<String, KinveyRealtimeCallback<String>> mapChannelToCallback;

    private RealtimeRouter() {

    }

    public static RealtimeRouter getInstance() {
        synchronized (lock) {
            if (realtimeRouter == null) {
                realtimeRouter = new RealtimeRouter();
            }
            return realtimeRouter;
        }
    }

    void initialize(String channelGroup, String publishKey, String subscribeKey, String authKey, AbstractClient client) {
        synchronized (lock) {
            if (!isInitialized()) {
                this.channelGroup = channelGroup;
                this.client = client;
                this.mapChannelToCallback = new HashMap<>();
                PNConfiguration pnConfiguration = new PNConfiguration();
                pnConfiguration.setSubscribeKey(subscribeKey);
                pnConfiguration.setPublishKey(publishKey);
                pnConfiguration.setAuthKey(authKey);
                pnConfiguration.setSecure(true);
                pubnubClient = new PubNub(pnConfiguration);
                subscribeCallback = new SubscribeCallback() {
                    @Override
                    public void status(PubNub pubnub, PNStatus status) {
                        handleStatusMessage(status);
                    }

                    @Override
                    public void message(PubNub pubnub, PNMessageResult message) {
                        subscribeCallback(message.getChannel(), message.getMessage().getAsString());
                    }

                    @Override
                    public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                    /* presence not currently supported */
                    }
                };
                pubnubClient.addListener(subscribeCallback);
                pubnubClient.subscribe().channelGroups(Collections.singletonList(channelGroup)).execute();
            }
        }

    }

    void uninitialize() {
        synchronized (lock) {
            if (isInitialized()) {
                pubnubClient.removeListener(subscribeCallback);
                pubnubClient.unsubscribe().channelGroups(Collections.singletonList(channelGroup)).execute();
                this.mapChannelToCallback = null;
                pubnubClient.destroy();
                pubnubClient = null;
                channelGroup = null;
                client = null;
                realtimeRouter = null;
            }
        }
    }

    void subscribeCollection(String collectionName, KinveyRealtimeCallback<String> realtimeCallback) {
        addChannel(getChannel(collectionName), realtimeCallback);
    }

    void unsubscribeCollection(String collectionName) {
        removeChannel(getChannel(collectionName));
    }

    private String getChannel(String collectionName) {
        String appKey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String channel = appKey + Constants.CHAR_PERIOD + Constants.STR_REALTIME_COLLECTION_CHANNEL_PREPEND + collectionName;
        return channel;
    }

    private void addChannel(String channel, KinveyRealtimeCallback<String> realtimeCallback) {
        mapChannelToCallback.put(channel, realtimeCallback);
    }

    private void removeChannel(String channel) {
        mapChannelToCallback.remove(channel);
    }


    public boolean isInitialized() {
        return pubnubClient != null;
    }

    private void subscribeCallback(String channel, String message) {
        mapChannelToCallback.get(channel).onNext(message);
    }

    private void handleStatusMessage(PNStatus status) {

    }

}
