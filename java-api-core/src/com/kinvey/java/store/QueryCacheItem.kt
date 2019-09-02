package com.kinvey.java.store

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Created by yuliya on 03/06/18.
 */

class QueryCacheItem(@Key var collectionName: String?, @Key var query: String?, @Key var lastRequestTime: String?) : GenericJson() {
    constructor(): this(null, null, null)
}
