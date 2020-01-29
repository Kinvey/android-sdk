package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key
import java.util.*

data class DateExample(
    @Key("_id")
    var id: String? = null,
    @Key
    var field: String? = null,
    @Key
    var date: Date? = null
) : GenericJson() {

    constructor(field: String?, date: Date?): this() {
        this.field = field
        this.date = date
    }

    companion object {
        const val COLLECTION = "DateExample"
    }
}