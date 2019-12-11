package com.kinvey.androidTest.model

import com.google.api.client.util.Key

/**
 * Created by yuliya on 09/20/17.
 */

class FloatPrimitiveListInPerson : Person {
    @Key("floatList")
    var floatList: List<Float>? = null

    constructor() {}
    constructor(username: String?) {
        this.username = username
    }

}