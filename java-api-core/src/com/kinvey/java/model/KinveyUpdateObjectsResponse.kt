package com.kinvey.java.model

import com.google.api.client.util.Key

data class KinveyUpdateObjectsResponse<T>(
    @Key
    var entities: List<T>? = null,
    @Key
    var errors: List<KinveyUpdateSingleItemError>? = null
) : KinveyErrorResponse() {
    val haveErrors: Boolean
        get() {
            return errors?.isEmpty() == false
        }
}
