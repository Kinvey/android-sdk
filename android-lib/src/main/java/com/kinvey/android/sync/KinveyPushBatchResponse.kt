package com.kinvey.android.sync

import com.google.api.client.json.GenericJson
import com.kinvey.java.model.KinveyBatchInsertError

class KinveyPushBatchResponse : KinveyPushResponse() {
    var entities: List<GenericJson>? = null
    var errors: List<KinveyBatchInsertError>? = null
}