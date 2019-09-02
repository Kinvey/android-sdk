package com.kinvey.java.dto

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key
import com.google.api.client.util.Throwables
import com.google.gson.Gson

class BatchList<T>(@Key var itemsList: List<T>?) : GenericJson() {

    private val gson = Gson()

    override fun toString(): String {
        var result = ""
        try {
            result = gson.toJson(itemsList)
        } catch (e: Exception) {
            throw Throwables.propagate(e)
        }

        return result
    }
}
