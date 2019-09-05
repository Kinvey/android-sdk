package com.kinvey.java.store

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Created by yuliya on 03/06/18.
 */

class QueryCacheItem() : GenericJson() {

    @Key
    var collectionName: String? = null
    @Key
    var query: String? = null
    @Key
    var lastRequestTime: String? = null

    constructor(collectionName: String, query: String, lastRequestTime: String): this() {
        this.collectionName = collectionName
        this.query = query
        this.lastRequestTime = lastRequestTime
    }
}
