package com.kinvey.java.model

class KinveySyncSaveBatchResponse<T>(
    var entityList: Collection<T?>?,
    var errors: List<KinveyBatchInsertError>?)
