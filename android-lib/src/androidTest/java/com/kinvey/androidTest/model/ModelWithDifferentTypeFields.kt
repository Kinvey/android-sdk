package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key
import java.util.*

class ModelWithDifferentTypeFields : GenericJson {
    @Key("_id")
    var id: String? = null
    @Key
    var username: String? = null
    @Key
    var carNumber = 0
    @Key
    var isUseAndroid = false
    @Key
    var date: Date? = null
    @Key
    var height = 0f
    @Key
    var time = 0.0

    constructor() {}
    constructor(username: String?, carNumber: Int, isUseAndroid: Boolean, date: Date?, height: Float, time: Double) {
        this.username = username
        this.carNumber = carNumber
        this.isUseAndroid = isUseAndroid
        this.date = date
        this.height = height
        this.time = time
    }

    companion object {
        const val COLLECTION = "CustomCollection"
    }
}