package com.kinvey.androidTest.model

import com.google.api.client.util.Key

class PersonArray : Person {
    @Key
    var array: Array<PersonArray>
    @Key
    var personArray: PersonArray? = null

    constructor() {}
    constructor(name: String?) {
        username = name
    }

    companion object {
        const val COLLECTION = "PersonArray"
    }
}