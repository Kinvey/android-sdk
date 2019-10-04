package com.kinvey.android.sync

import com.kinvey.java.model.AbstractKinveyExceptionsListResponse

open class KinveyPushResponse : AbstractKinveyExceptionsListResponse() {
    var successCount = 0
}