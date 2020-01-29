package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Created by yuliya on 11/28/17.
 */

data class PersonRoomPerson(
    @Key
    var name: String? = null,
    @Key
    var room: RoomPerson? = null,
    @Key
    var personList: List<PersonRoomPerson>? = null
) : GenericJson()