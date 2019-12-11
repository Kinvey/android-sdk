package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

class InternalUserEntity : GenericJson() {
    @Key
    var street: String? = null

}