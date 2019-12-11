package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

class Author : GenericJson {
    @Key
    var name: String? = null

    constructor() {}
    constructor(name: String?) {
        this.name = name
    }

}