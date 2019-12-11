package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

data class Person51(
    @Key("person")
    var person: Person? = null
) : GenericJson()