package com.kinvey.java.dto

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Created by yuliya on 2/19/17.
 */


class LiveServiceRegisterResponse : GenericJson() {
    @Key
    var userChannelGroup: String? = null
    @Key
    var publishKey: String? = null
    @Key
    var subscribeKey: String? = null
}
