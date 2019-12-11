package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

data class LiveModel(
    @Key
    var age: String? = null,
    @Key("_id")
    var id: String? = null,
    @Key
    var height: Float = 0f,
    @Key
    var author: Author? = null,
    @Key
    var weight: Long = 0,
    @Key
    var carNumber: Int? = null,
    @Key
    var username: String? = null
) : GenericJson() {

    constructor(username: String?): this() {
        this.username = username
    }

    companion object {
        const val COLLECTION = "LiveCollection"
    }
}