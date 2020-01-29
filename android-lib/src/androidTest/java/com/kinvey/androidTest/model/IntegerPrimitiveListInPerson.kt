package com.kinvey.androidTest.model

import com.google.api.client.util.Key

/**
 * Created by yuliya on 09/20/17.
 */

data class IntegerPrimitiveListInPerson(
    @Key("integerList")
    var integerList: List<Int>? = null
): Person() {
    constructor(username: String?): this() {
        this.username = username
    }
}