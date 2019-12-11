package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Created by yuliya on 12/06/17.
 */
class PersonWithPersonAndList(
    @Key
    var person: PersonWithPersonAndList? = null,
    @Key
    var list: List<PersonWithPersonAndList>? = null,
    @Key
    var name: String? = null
) : GenericJson() {
    constructor(name: String?): this() {
        this.name = name
    }
}