package com.kinvey.java.store.request

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

data class Person(
    @Key
    var age: String? = null,
    @Key("_id")
    var id: String? = null,
    @Key
    var height: Float = 0f,
    @Key
    var weight: Long = 0,
    @Key
    var intVal: Int = 0,
    @Key
    var carNumber: Int? = null,
    @Key("username")
    var username: String? = null,
    @Key("_geoloc")
    var geoloc: String? = null
): GenericJson() {
    companion object {
        const val COLLECTION = "Persons"
    }
}