package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Created by yuliya on 11/28/17.
 */

data class Room(
    @Key
    var name: String? = null,
    @Key
    var roomAddress: RoomAddress? = null
) : GenericJson()