package com.kinvey.java.model

import com.google.api.client.util.Key

data class KinveyBatchInsertError (
    @Key
    var index: Int = 0,
    @Key
    var code: Long = 0,
    @Key
    var errorMessage: String? = null
)
