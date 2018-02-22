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

class LiveServiceRouter {
    private static final Object lock = new Object();
    private static volatile LiveServiceRouter liveServiceRouter;
    private PubNub pubnubClient;
    private String channelGroup;
    private AbstractClient client;
    private SubscribeCallback subscribeCallback;

    private LiveServiceRouter() {

    }


    static LiveServiceRouter getInstance() {
        if (liveServiceRouter == null) {
            synchronized (lock) {
                if (liveServiceRouter == null) {
                    liveServiceRouter = new LiveServiceRouter();
                }
            }
        }
        return liveServiceRouter;
    }

    void initialize(String channelGroup, String publishKey, String subscribeKey, String authKey, AbstractClient client) {
        if (!isInitialized()) {
            synchronized (lock) {
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

    void unInitialize() {
        if (isInitialized()) {
            synchronized (lock) {
                pubnubClient.removeListener(subscribeCallback);
                pubnubClient.unsubscribe().channelGroups(Collections.singletonList(channelGroup)).execute();
                pubnubClient.destroy();
                pubnubClient = null;
                channelGroup = null;
                client = null;
                liveServiceRouter = null;
            }
        }
    }

    public boolean isInitialized() {
        return pubnubClient != null;
    }

    private void subscribeCallback(String channel, String message) {

    }

    private void handleStatusMessage(PNStatus status) {

    }

}
