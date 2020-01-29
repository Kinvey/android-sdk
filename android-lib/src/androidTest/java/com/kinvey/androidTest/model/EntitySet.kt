package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

data class EntitySet(
    @Key("_id")
    var id: String? = null,
    @Key
    var description: String? = null
) : GenericJson() {
    companion object {
        const val COLLECTION = "EntitySet"
        const val DESCRIPTION_KEY = "description"
    }
}