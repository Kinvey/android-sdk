package com.kinvey.java.model

/**
 * Created by yuliya on 03/06/18.
 */

data class KinveyQueryCacheResponse<T>(
    var deleted: List<T>? = null,
    var changed: List<T>? = null,
    var lastRequestTime: String? = null
) : AbstractKinveyExceptionsListResponse()
