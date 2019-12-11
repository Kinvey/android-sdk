package com.kinvey.androidTest.model

import com.google.api.client.util.Key

data class PersonList(
    @Key("list")
    var list: List<PersonList>? = null,
    @Key("personList")
    var personList: PersonList? = null
) : Person() {
    constructor(name: String?): this() {
        username = name
    }
}