package com.kinvey.java.sync.dto

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Created by yuliya on 10/04/17.
 */

class SyncItem : SyncRequest {
    @Key("requestMethod")
    private var requestMethod: String? = null

    constructor() {}
    constructor(httpMethod: HttpVerb, entityID: SyncMetaData, collectionName: String?) {
        requestMethod = httpMethod.name
        this.entityID = entityID
        this.collectionName = collectionName
    }

    fun getRequestMethod(): HttpVerb? {
        return HttpVerb.fromString(requestMethod)
    }

    fun setRequestMethod(requestMethod: HttpVerb) {
        this.requestMethod = requestMethod.name
    }

    val entity: GenericJson?
        get() {
            var entity: GenericJson? = null
            val entityId = entityID
            if (entityId != null) {
                entity = entityId.entity
            }
            return entity
        }
}