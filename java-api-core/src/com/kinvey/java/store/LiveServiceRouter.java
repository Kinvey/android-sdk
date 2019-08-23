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
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuliya on 2/15/17.
 */

public class LiveServiceRouter {
    private static final Object lock = new Object();
    private static volatile LiveServiceRouter liveServiceRouter;
    private PubNub pubnubClient;
    private String channelGroup;
    private AbstractClient client;
    private SubscribeCallback subscribeCallback;
    private Map<String, KinveyLiveServiceCallback<String>> mapChannelToCallback;

    private LiveServiceRouter() {

    }


    public static LiveServiceRouter getInstance() {
        if (liveServiceRouter == null) {
            synchronized (lock) {
                if (liveServiceRouter == null) {
                    liveServiceRouter = new LiveServiceRouter();
                }
            }
        }
        return liveServiceRouter;
    }

    public void initialize(String channelGroup, String publishKey, String subscribeKey, String authKey, AbstractClient client) {
        if (!isInitialized()) {
            synchronized (lock) {
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
                        subscribeCallback(message.getChannel(), message.getMessage().toString());
                    }

                    @Override
                    public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                        /* presence not currently supported */
                    }

                    @Override
                    public void signal(PubNub pubnub, PNSignalResult signal) {
                        /* signal not currently supported */
                    }
                };
                pubnubClient.addListener(subscribeCallback);
                pubnubClient.subscribe().channelGroups(Collections.singletonList(channelGroup)).execute();
            }
        }

    }


    public void uninitialize() {
        if (isInitialized()) {
            synchronized (lock) {
                pubnubClient.removeListener(subscribeCallback);
                pubnubClient.unsubscribe().channelGroups(Collections.singletonList(channelGroup)).execute();
                this.mapChannelToCallback = null;
                pubnubClient.destroy();
                pubnubClient = null;
                channelGroup = null;
                client = null;
                liveServiceRouter = null;
            }
        }
    }

    boolean subscribeCollection(String collectionName, KinveyLiveServiceCallback<String> liveServiceCallback) {
        if (isInitialized()) {
            synchronized (lock) {
                addChannel(getChannel(collectionName), liveServiceCallback);
                return true;
            }
        }
        return false;
    }

    void unsubscribeCollection(String collectionName) {
        if (isInitialized()) {
            synchronized (lock) {
                removeChannel(getChannel(collectionName));
            }
        }
    }

    private String getChannel(String collectionName) {
        String appKey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String channel = appKey + Constants.CHAR_PERIOD + Constants.STR_LIVE_SERVICE_COLLECTION_CHANNEL_PREPEND + collectionName;
        return channel;
    }

    private void addChannel(String channel, KinveyLiveServiceCallback<String> liveServiceCallback) {
        mapChannelToCallback.put(channel, liveServiceCallback);
    }

    private void removeChannel(String channel) {
        mapChannelToCallback.remove(channel);
    }


    public boolean isInitialized() {
        return pubnubClient != null;
    }

    private void subscribeCallback(String channel, String message) {
        if (mapChannelToCallback.get(channel) != null) {
            mapChannelToCallback.get(channel).onNext(message);
        }
    }

    private void handleStatusMessage(PNStatus status) {

    }

}
