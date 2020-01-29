package com.kinvey.androidTest.model

import com.google.api.client.util.Key

/**
 * Created by yuliya on 09/20/17.
 */

data class BooleanPrimitiveListInPerson(
    @Key("booleanList")
    var booleanList: List<Boolean>? = null
) : Person() {
    constructor(username: String?): this() {
        this.username = username
    }
}