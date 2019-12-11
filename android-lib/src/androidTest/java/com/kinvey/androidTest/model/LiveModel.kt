package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

class LiveModel : GenericJson {
    constructor() {}
    constructor(username: String?) {
        this.username = username
    }

    @Key
    var age: String? = null
    @Key("_id")
    var id: String? = null
    @Key
    var height = 0f
    @Key
    var author: Author? = null
    @Key
    var weight: Long = 0
    @Key
    var carNumber: Int? = null
    @Key
    var username: String? = null

    companion object {
        const val COLLECTION = "LiveCollection"
    }
}