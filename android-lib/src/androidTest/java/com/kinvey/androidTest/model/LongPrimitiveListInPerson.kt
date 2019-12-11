package com.kinvey.androidTest.model

import com.google.api.client.util.Key

class LongPrimitiveListInPerson : Person {
    @Key("longList")
    var longList: List<Long>? = null

    constructor() {}
    constructor(username: String?) {
        this.username = username
    }

}