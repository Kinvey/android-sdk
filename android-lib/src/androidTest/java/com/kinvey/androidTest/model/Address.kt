package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Created by yuliya on 11/23/17.
 */

class Address : GenericJson {
    @Key
    var person: Person? = null
    @Key
    private var addressField: String? = null

    constructor(addressField: String?) {
        this.addressField = addressField
    }

    constructor() {}

}