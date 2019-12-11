package com.kinvey.androidTest.model

import com.google.api.client.util.Key

/**
 * Created by yuliya on 11/23/17.
 */

data class PersonWithAddress(
    @Key
    var address: Address? = null
) : Person() {
    constructor(username: String?): this() {
        this.username = username
    }
}