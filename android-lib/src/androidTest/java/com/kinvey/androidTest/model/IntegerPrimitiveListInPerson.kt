package com.kinvey.androidTest.model

import com.google.api.client.util.Key

/**
 * Created by yuliya on 09/20/17.
 */

class IntegerPrimitiveListInPerson : Person {
    @Key("integerList")
    var integerList: List<Int>? = null

    constructor() {}
    constructor(username: String?) {
        this.username = username
    }

}