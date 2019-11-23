package com.kinvey.androidTest.store.data.network

import com.google.api.client.util.Key
import com.kinvey.java.linkedResources.LinkedGenericJson

class LinkedPerson : LinkedGenericJson() {
    @Key("_id")
    var id: String? = null
    @Key("username")
    var username: String? = null

    init {
        putFile("attachment")
    }

    companion object {
        const val COLLECTION = "LinkedPersonCollection"
    }

}