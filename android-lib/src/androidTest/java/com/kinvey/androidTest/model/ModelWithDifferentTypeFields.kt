package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key
import java.util.*

data class ModelWithDifferentTypeFields(
    @Key("_id")
    var id: String? = null,
    @Key
    var username: String? = null,
    @Key
    var carNumber: Int = 0,
    @Key
    var isUseAndroid: Boolean = false,
    @Key
    var date: Date? = null,
    @Key
    var height: Float = 0f,
    @Key
    var time: Double = 0.0
) : GenericJson() {

    constructor(username: String?, carNumber: Int, isUseAndroid: Boolean, date: Date?, height: Float, time: Double)
        : this(null, username, carNumber, isUseAndroid, date, height, time) {
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