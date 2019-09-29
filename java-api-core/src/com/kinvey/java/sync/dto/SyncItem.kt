package com.kinvey.java.sync.dto

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Created by yuliya on 10/04/17.
 */

class SyncItem : SyncRequest {
    @Key("requestMethod")
    private var requestMethodStr: String? = null

    constructor() {}
    constructor(httpMethod: HttpVerb?, entityID: SyncMetaData, collectionName: String?) {
        requestMethod = httpMethod
        this.entityID = entityID
        this.collectionName = collectionName
    }

    var requestMethod: HttpVerb? = null
        get() {
            return HttpVerb.fromString(requestMethodStr)
        }
        set (value: HttpVerb?) {
            field = value
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