package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Created by yuliya on 09/21/17.
 */

data class StringGenericJson(
    @Key("string")
    var string: String? = null
): GenericJson()