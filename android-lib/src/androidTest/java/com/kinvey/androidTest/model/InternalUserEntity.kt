package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

data class InternalUserEntity(
    @Key
    var street: String? = null
): GenericJson()