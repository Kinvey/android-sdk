package com.kinvey.java.model

import com.google.api.client.util.Key

open class KinveyErrorResponse {
    @Key
    var error: String? = null
    @Key
    var description: String? = null
    @Key
    var debug: String? = null
}
