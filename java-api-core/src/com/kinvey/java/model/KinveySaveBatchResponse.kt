package com.kinvey.java.model

import com.google.api.client.util.Key

data class KinveySaveBatchResponse<T>(
    @Key
    var entities: List<T>? = null,
    @Key
    var errors: List<KinveyBatchInsertError>? = null
) : KinveyErrorResponse() {
    val haveErrors: Boolean
        get() {
            return errors?.isNotEmpty() == true
        }
}
