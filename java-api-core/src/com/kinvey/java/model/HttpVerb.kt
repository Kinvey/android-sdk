package com.kinvey.java.model

import com.kinvey.java.sync.dto.SyncRequest

enum class HttpVerb(val verb: String) {
    GET("GET"),
    PUT("PUT"),
    POST("POST"),
    DELETE("DELETE"),
    SAVE("SAVE"), // for backward compatibility with previous versions of keeping Sync requests
    QUERY("QUERY");

    companion object {
        fun fromString(verb: String?): SyncRequest.HttpVerb? {
            if (verb != null) {
                for (v in SyncRequest.HttpVerb.values()) {
                    if (v.query.equals(verb, ignoreCase = true)) {
                        return v
                    }
                }
            }
            return null
        }
    }
}