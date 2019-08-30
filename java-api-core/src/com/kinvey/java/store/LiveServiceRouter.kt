package com.kinvey.java.store

import com.kinvey.java.AbstractClient
import com.kinvey.java.Constants
import com.kinvey.java.core.KinveyClientRequestInitializer
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult

import java.util.HashMap

/**
 * Created by yuliya on 2/15/17.
 */

class LiveServiceRouter {
    private var pubnubClient: PubNub? = null
    private var channelGroup: String? = null
    private var client: AbstractClient<*>? = null
    private var subscribeCallback: SubscribeCallback? = null
    private var mapChannelToCallback: MutableMap<String, KinveyLiveServiceCallback<String>>? = null


    val isInitialized: Boolean
        get() = pubnubClient != null

    fun initialize(channelGroup: String?, publishKey: String?, subscribeKey: String?, authKey: String?, client: AbstractClient<*>?) {
        if (!isInitialized) {
            synchronized(lock) {
                this.channelGroup = channelGroup
                this.client = client
                this.mapChannelToCallback = HashMap()
                val pnConfiguration = PNConfiguration()
                pnConfiguration.subscribeKey = subscribeKey
                pnConfiguration.publishKey = publishKey
                pnConfiguration.authKey = authKey
                pnConfiguration.isSecure = true
                pubnubClient = PubNub(pnConfiguration)
                subscribeCallback = object : SubscribeCallback() {
                    override fun status(pubnub: PubNub, status: PNStatus) {
                        handleStatusMessage(status)
                    }

                    override fun message(pubnub: PubNub, message: PNMessageResult) {
                        subscribeCallback(message.channel, message.message.toString())
                    }

                    override fun presence(pubnub: PubNub, presence: PNPresenceEventResult) {
                        /* presence not currently supported */
                    }

                    override fun signal(pubnub: PubNub, signal: PNSignalResult) {}

                }
                pubnubClient?.addListener(subscribeCallback)
                pubnubClient?.subscribe()?.channelGroups(listOf(channelGroup))?.execute()
            }
        }

    }


    fun uninitialize() {
        if (isInitialized) {
            synchronized(lock) {
                pubnubClient?.removeListener(subscribeCallback)
                pubnubClient?.unsubscribe()?.channelGroups(listOf<String>(channelGroup ?: ""))?.execute()
                this.mapChannelToCallback = null
                pubnubClient!!.destroy()
                pubnubClient = null
                channelGroup = null
                client = null
                liveServiceRouter = null
            }
        }
    }

    internal fun subscribeCollection(collectionName: String, liveServiceCallback: KinveyLiveServiceCallback<String>): Boolean {
        if (isInitialized) {
            synchronized(lock) {
                addChannel(getChannel(collectionName), liveServiceCallback)
                return true
            }
        }
        return false
    }

    internal fun unsubscribeCollection(collectionName: String) {
        if (isInitialized) {
            synchronized(lock) {
                removeChannel(getChannel(collectionName))
            }
        }
    }

    private fun getChannel(collectionName: String): String {
        val appKey = (client?.kinveyRequestInitializer as KinveyClientRequestInitializer).appKey
        return appKey + Constants.CHAR_PERIOD + Constants.STR_LIVE_SERVICE_COLLECTION_CHANNEL_PREPEND + collectionName
    }

    private fun addChannel(channel: String, liveServiceCallback: KinveyLiveServiceCallback<String>) {
        mapChannelToCallback?.let { map -> map[channel] = liveServiceCallback }
    }

    private fun removeChannel(channel: String) {
        mapChannelToCallback?.remove(channel)
    }

    private fun subscribeCallback(channel: String, message: String) {
        mapChannelToCallback?.let { map -> map[channel]?.onNext(message) }
    }

    private fun handleStatusMessage(status: PNStatus) {}

    companion object {
        private val lock = Any()
        @Volatile
        private var liveServiceRouter: LiveServiceRouter? = null


        @JvmStatic
        val instance: LiveServiceRouter?
            get() {
                if (liveServiceRouter == null) {
                    synchronized(lock) {
                        if (liveServiceRouter == null) {
                            liveServiceRouter = LiveServiceRouter()
                        }
                    }
                }
                return liveServiceRouter
            }
    }

}
