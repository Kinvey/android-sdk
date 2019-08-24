package com.kinvey.java.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

import java.io.IOException

class KinveyUpdateSingleItemError(e: Exception, entity: GenericJson?) : IOException() {

    @Key
    var entity: GenericJson? = entity
        private set
    @Key
    var code: Long = 0
    @Key
    var errorMessage: String? = e.message

    fun setIndex(entity: GenericJson) {
        this.entity = entity
    }
}
