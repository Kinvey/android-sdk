package com.kinvey.androidTest.model

import com.google.api.client.util.Key

class PersonList : Person {
    @Key("list")
    var list: List<PersonList>? = null
    @Key("personList")
    var personList: PersonList? = null

    constructor() {}
    constructor(name: String?) {
        username = name
    }

}