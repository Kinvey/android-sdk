package com.kinvey.androidTest.store.data.cache

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Created by Prots on 2/29/16.
 */
data class SampleGsonWithInner(
    @Key ("_id")
    var id: String? = null,
    @Key
    var details: SampleGsonObject1? = null
) : GenericJson()