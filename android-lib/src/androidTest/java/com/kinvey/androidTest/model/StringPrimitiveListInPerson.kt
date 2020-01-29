package com.kinvey.androidTest.model

import com.google.api.client.util.Key

/**
 * Created by yuliya on 09/20/17.
 */

data class StringPrimitiveListInPerson(
    @Key("stringList")
    var stringList: List<String>? = null
): Person() {
    constructor(username: String?): this() {
        this.username = username
    }
}