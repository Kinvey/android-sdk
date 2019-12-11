package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Created by yuliya on 11/28/17.
 */

data class RoomPerson(
    @Key
    var name: String? = null,
    @Key
    var person: PersonRoomPerson? = null
) : GenericJson()