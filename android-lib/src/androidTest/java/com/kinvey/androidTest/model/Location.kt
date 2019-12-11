package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

data class Location(
    @Key
    var age: String? = null,
    @Key("_id")
    var id: String? = null,
    @Key
    var description: String? = null,
    @Key
    var address: String? = null,
    @Key("_geoloc")
    var geo: Array<Double>
) : GenericJson() {

    companion object {
        const val COLLECTION = "Location"
    }
}