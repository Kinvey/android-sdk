package com.kinvey.androidTest.model

import com.google.api.client.util.Key

/**
 * Created by yuliya on 09/20/17.
 */

class ObjectListInPerson : Person {
    @Key("stringGenericJsons")
    var stringGenericJsons: List<StringGenericJson>? = null

    constructor() {}
    constructor(username: String?) {
        this.username = username
    }

}