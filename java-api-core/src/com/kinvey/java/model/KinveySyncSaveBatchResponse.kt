package com.kinvey.java.model

data class KinveySyncSaveBatchResponse<T>(
    var entityList: Collection<T>?,
    var errors: List<KinveyBatchInsertError>?
)
