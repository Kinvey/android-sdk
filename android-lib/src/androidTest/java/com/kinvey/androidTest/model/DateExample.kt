package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key
import java.util.*

class DateExample : GenericJson {
    @Key("_id")
    var id: String? = null
    @Key
    private var field: String? = null
    @Key
    var date: Date? = null

    constructor() {}
    constructor(field: String?, date: Date?) {
        this.field = field
        this.date = date
    }

    companion object {
        const val COLLECTION = "DateExample"
    }
}