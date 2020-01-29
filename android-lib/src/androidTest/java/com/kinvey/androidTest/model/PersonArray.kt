package com.kinvey.androidTest.model

import com.google.api.client.util.Key

data class PersonArray(
    @Key
    var array: Array<PersonArray>? = null,
    @Key
    var personArray: PersonArray? = null
) : Person() {

    constructor(name: String?): this() {
        username = name
    }

    companion object {
        const val COLLECTION = "PersonArray"
    }
}