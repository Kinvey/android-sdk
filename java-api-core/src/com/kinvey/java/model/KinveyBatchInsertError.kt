package com.kinvey.java.model

import com.google.api.client.util.Key

data class KinveyBatchInsertError (
    @Key
    var index: Int = 0

) : KinveyErrorResponse()
