package com.kinvey.androidTest.model

import com.google.api.client.util.Key

class LongPrimitiveListInPerson(
    @Key("longList")
    var longList: List<Long>? = null
): Person() {
    constructor(username: String?): this() {
        this.username = username
    }
}