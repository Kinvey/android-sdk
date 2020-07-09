package com.kinvey.java.model

/**
 * Created by yuliya on 10/30/17.
 */

data class KinveyReadResponse<T>(
    var lastRequestTime: String? = null,
    var result: List<T>? = null,
    var count: Int? = null
) : AbstractKinveyExceptionsListResponse()
