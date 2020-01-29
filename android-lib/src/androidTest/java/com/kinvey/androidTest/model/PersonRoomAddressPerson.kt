package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Created by yuliya on 11/28/17.
 */

data class PersonRoomAddressPerson(
    @Key
    var name: String? = null,
    @Key
    var room: Room? = null
) : GenericJson()