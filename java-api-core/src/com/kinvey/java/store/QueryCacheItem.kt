package com.kinvey.java.store

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Created by yuliya on 03/06/18.
 */

class QueryCacheItem(collectionName: String, query: String, lastRequestTime: String) : GenericJson() {

    @Key
    var collectionName: String? = collectionName

    @Key
    var query: String? = query

    @Key
    var lastRequestTime: String? = lastRequestTime

}
