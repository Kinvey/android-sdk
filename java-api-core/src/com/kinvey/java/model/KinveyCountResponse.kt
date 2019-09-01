package com.kinvey.java.model

import com.google.api.client.util.Key

data class KinveyCountResponse(
    @Key
    var count: Int = 0
) : AbstractKinveyHeadersResponse()
