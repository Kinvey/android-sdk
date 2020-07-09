package com.kinvey.java

/**
 * Created by yuliya on 2/20/18.
 */
object Constants {

    const val DEF_REQUEST_TIMEOUT = 30000

    const val COLLECTION = "collection"
    const val UNDERSCORE = "_"
    const val TIME_FORMAT = "%tFT%<tTZ"
    const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    const val Z = "Z"
    const val DOT = "."
    const val QUERY = "query"

    const val _ID = "_id"
    const val AUTH_TOKEN = "authtoken"
    const val _KMD = "_kmd"
    const val _LMT = "_lmt"
    const val _ACL = "_acl"
    const val LLT = "llt"

    const val META_ID = "meta.id"

    const val ACCESS_ERROR = "Access Error"

    const val DELETE = "DELETE"
    const val REQUEST_METHOD = "requestMethod"

    const val CHAR_PERIOD = "."
    const val STR_LIVE_SERVICE_COLLECTION_CHANNEL_PREPEND = "c-"

    //Delta Sync
    const val DELTA_SYNC_QUERY_CACHE_FORMAT = "%s{skip=%d,limit=%d,sorting=}%s"
    const val X_KINVEY_REQUEST_START = "x-kinvey-request-start"
    const val X_KINVEY_REQUEST_START_CAMEL_CASE = "X-Kinvey-Request-Start"
    const val CHANGED = "changed"
    const val DELETED = "deleted"

    //Tables
    const val QUERY_CACHE_COLLECTION = "_QueryCache"
    const val HYPHEN = "-"
    const val PROTOCOL_HTTPS = "https://"
    const val HOSTNAME_API = "baas.kinvey.com"
    const val HOSTNAME_AUTH = "auth.kinvey.com"

    //Count header
    const val X_KINVEY_INCLUDE_ITEMS_COUNT = "X-Kinvey-Include-Items-Count"
    const val X_KINVEY_ITEMS_COUNT_CAMEL_CASE = "X-Kinvey-Items-Count"
    const val X_KINVEY_ITEMS_COUNT = "x-kinvey-items-count"
}
