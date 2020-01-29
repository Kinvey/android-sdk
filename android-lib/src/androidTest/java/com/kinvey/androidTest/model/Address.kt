package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Created by yuliya on 11/23/17.
 */

data class Address(
    @Key
    var person: Person? = null,
    @Key
    var addressField: String? = null
) : GenericJson() {
    constructor(addressField: String?): this() {
        this.addressField = addressField
    }
}