package com.kinvey.java.model

import com.google.api.client.util.Key

data class KinveySaveBatchResponse<T>(
    @Key
    var entities: MutableList<T>? = null,
    @Key
    var errors: MutableList<KinveyBatchInsertError>? = null
) : KinveyErrorResponse() {
    val haveErrors: Boolean
        get() {
            return errors?.isNotEmpty() == true
        }
}
