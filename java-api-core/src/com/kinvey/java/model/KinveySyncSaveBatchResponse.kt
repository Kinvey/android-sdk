package com.kinvey.java.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

class KinveySyncSaveBatchResponse<T>(
    var entityList: Collection<T?>?,
    var errors: List<KinveyBatchInsertError>?) : GenericJson()
