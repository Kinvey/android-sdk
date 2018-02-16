package com.kinvey.java.store;

import com.kinvey.java.AbstractClient;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.Collections;

/**
 * Created by yuliya on 2/15/17.
 */

class RealtimeRouter {

    private static final Object lock = new Object();
    private static RealtimeRouter realtimeRouter;
    private PubNub pubnubClient;
    private String channelGroup;
    private AbstractClient client;
    private SubscribeCallback subscribeCallback;

    private RealtimeRouter() {

    }

    protected static RealtimeRouter getInstance() {
        synchronized (lock) {
            if (realtimeRouter == null) {
                realtimeRouter = new RealtimeRouter();
            }
            return realtimeRouter;
        }
    }

    protected void initialize(String channelGroup, String publishKey, String subscribeKey, String authKey, AbstractClient client) {
        synchronized (lock) {
            if (pubnubClient == null) {
                this.channelGroup = channelGroup;
                this.client = client;
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

    protected void unInitialize() {
        synchronized (lock) {
            if (pubnubClient != null) {
                pubnubClient.removeListener(subscribeCallback);
                pubnubClient.unsubscribe().channelGroups(Collections.singletonList(channelGroup)).execute();
                pubnubClient.destroy();
                pubnubClient = null;
                channelGroup = null;
                client = null;
                realtimeRouter = null;
            }
        }
    }

    private void subscribeCallback(String channel, String message) {

    }

    private void handleStatusMessage(PNStatus status) {

    }

}
