package com.kinvey.androidTest.model

import com.google.api.client.util.Key

/**
 * Created by yuliya on 09/20/17.
 */

class BooleanPrimitiveListInPerson : Person {
    @Key("booleanList")
    var booleanList: List<Boolean>? = null

    constructor() {}
    constructor(username: String?) {
        this.username = username
    }

}