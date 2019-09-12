package com.kinvey.java.dto

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Created by yuliya on 2/19/17.
 */

class DeviceId : GenericJson() {
    @Key("deviceId")
    var deviceId: String? = null
}
