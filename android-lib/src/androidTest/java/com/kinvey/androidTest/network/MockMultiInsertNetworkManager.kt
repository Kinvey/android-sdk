package com.kinvey.androidTest.network

import com.google.api.client.json.GenericJson
import com.kinvey.java.AbstractClient
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.sync.dto.SyncRequest.HttpVerb
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class MockMultiInsertNetworkManager<T : GenericJson>
/**
 * Constructor to instantiate the NetworkManager class.
 *
 * @param collectionName Name of the appData collection
 * @param myClass        Class Type to marshall data between.
 * @param client
 */(collectionName: String?, myClass: Class<T>, client: AbstractClient<*>?) : NetworkManager<T>(collectionName, myClass, client) {

    private var useSingleSaveVal = false
    private var useMultiInsertSaveVal = false
    private val multiPostCountVal = AtomicInteger(0)

    @Throws(IOException::class)
    override fun saveBlocking(entity: T?): Save? {
        val result = super.saveBlocking(entity)
        if (HttpVerb.POST.toString() == result?.requestMethod?.toUpperCase()) {
            useSingleSaveVal = true
        }
        return result
    }

    @Throws(IOException::class)
    override fun saveBatchBlocking(list: List<T>?): SaveBatch? {
        useMultiInsertSaveVal = true
        multiPostCountVal.incrementAndGet()
        return super.saveBatchBlocking(list)
    }

    fun clear() {
        multiPostCountVal.set(0)
        useSingleSaveVal = false
        useMultiInsertSaveVal = false
    }

    val multiPostCount: Int
        get() {
            return multiPostCountVal.get()
        }

    val useMultiInsertSave: Boolean
        get() {
            return useMultiInsertSaveVal && !useSingleSaveVal
        }
}