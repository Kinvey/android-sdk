package com.kinvey.androidTest.model

import com.google.api.client.util.Key

/**
 * Created by yuliya on 09/20/17.
 */

class ObjectListInPerson(
    @Key("stringGenericJsons")
    var stringGenericJsons: List<StringGenericJson>? = null
) : Person() {
    constructor(username: String?): this() {
        this.username = username
    }
}