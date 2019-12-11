package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

class EntityForInQueryTest : GenericJson() {
    @Key
    var longVal: Long = 0
    @Key
    var stringVal: String? = null
    @Key
    var isBooleanVal = false
    @Key
    var intVal = 0
    @Key
    var floatVal = 0f

    companion object {
        const val COLLECTION = "InQueryTestCollection"
    }
}