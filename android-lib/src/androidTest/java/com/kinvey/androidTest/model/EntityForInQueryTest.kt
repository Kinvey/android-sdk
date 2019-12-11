package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

class EntityForInQueryTest(
    @Key
    var longVal: Long = 0,
    @Key
    var stringVal: String? = null,
    @Key
    var isBooleanVal: Boolean = false,
    @Key
    var intVal: Int = 0,
    @Key
    var floatVal: Float = 0f
) : GenericJson() {

    companion object {
        const val COLLECTION = "InQueryTestCollection"
    }
}