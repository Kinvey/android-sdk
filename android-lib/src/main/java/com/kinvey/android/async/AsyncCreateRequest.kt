package com.kinvey.android.async

import com.google.api.client.json.GenericJson
import com.kinvey.android.AsyncClientRequest
import com.kinvey.android.store.DataStore
import com.kinvey.java.Logger
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.model.KinveySaveBatchResponse
import java.io.IOException

class AsyncCreateRequest<T: GenericJson>(val store: DataStore<T>, var entity: T, callback: KinveyClientCallback<T>)
    : AsyncClientRequest<T>(callback) {

    @Throws(IOException::class)
    override fun executeAsync(): T? {
        Logger.INFO("Calling CreateRequest#executeAsync()")
        return store.create(entity)
    }
}