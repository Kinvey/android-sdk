package com.kinvey.java.core

import com.google.api.client.http.HttpMethods
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpStatusCodes
import com.google.api.client.json.GenericJson
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.util.Charsets
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kinvey.java.AbstractClient
import com.kinvey.java.Constants
import com.kinvey.java.KinveyException
import com.kinvey.java.Logger
import com.kinvey.java.model.KinveyQueryCacheResponse

import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.ArrayList
import java.util.Locale

/**
 * Created by yuliya on 03/05/17.
 */

abstract class AbstractKinveyQueryCacheReadRequest<T>
/**
 * @param abstractKinveyJsonClient kinvey credential JSON client
 * @param requestMethod            HTTP Method
 * @param uriTemplate              URI template for the path relative to the base URL. If it starts with a "/"
 * the base path from the base URL will be stripped out. The URI template can also be a
 * full URL.
 * @param jsonContent              POJO that can be serialized into JSON content or `null` for none
 * @param responseClass            response class to parse into
 */
protected constructor(abstractKinveyJsonClient: AbstractClient<*>, requestMethod: String, uriTemplate: String, jsonContent: GenericJson, private val queryResponseClass: Class<T>?)
    : AbstractKinveyJsonClientRequest<KinveyQueryCacheResponse<*>>(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonContent, null) {

    @Throws(IOException::class)
    override fun execute(): KinveyQueryCacheResponse<*>? {
        val response = executeUnparsed()
        if (overrideRedirect) {
            return onRedirect(response.headers.location)
        }
        // special class to handle void or empty responses
        if (response.content == null) {
            response.ignore()
            return null
        }
        try {
            val ret = KinveyQueryCacheResponse<T>()
            val statusCode = response.statusCode
            if (response.request.requestMethod == HttpMethods.HEAD || statusCode / 100 == 1
                    || statusCode == HttpStatusCodes.STATUS_CODE_NO_CONTENT
                    || statusCode == HttpStatusCodes.STATUS_CODE_NOT_MODIFIED) {
                response.ignore()
                return null

            } else {

                val jsonString = response.parseAsString()
                val jsonParser = JsonParser()
                val jsonObject = jsonParser.parse(jsonString) as JsonObject
                val jsonArrayChanged = jsonObject.getAsJsonArray(Constants.CHANGED)
                val objectParser = abstractKinveyClient.objectParser
                val changed = ArrayList<T>()
                val exceptions = ArrayList<Exception>()

                jsonArrayChanged.onEach { element ->
                    try {
                        changed.add(objectParser.parseAndClose(ByteArrayInputStream(element.toString().toByteArray(Charsets.UTF_8)), Charsets.UTF_8, queryResponseClass))
                    } catch (e: IllegalArgumentException) {
                        Logger.ERROR("unable to parse response -> $e")
                        exceptions.add(KinveyException("Unable to parse the JSON in the response", "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)", e.toString()))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        exceptions.add(e)
                    }
                }
                ret.changed = changed

                val jsonArrayDeleted = jsonObject.getAsJsonArray(Constants.DELETED)
                val deleted = ArrayList<T>()
                for (element in jsonArrayDeleted) {
                    try {
                        deleted.add(objectParser.parseAndClose(ByteArrayInputStream(element.toString().toByteArray(Charsets.UTF_8)), Charsets.UTF_8, queryResponseClass))
                    } catch (e: IllegalArgumentException) {
                        Logger.ERROR("unable to parse response -> $e")
                        exceptions.add(KinveyException("Unable to parse the JSON in the response", "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)", e.toString()))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        exceptions.add(e)
                    }

                }
                ret.deleted = deleted
                ret.listOfExceptions = exceptions
                if (response.headers.containsKey(Constants.X_KINVEY_REQUEST_START)) {
                    ret.lastRequestTime = response.headers.getHeaderStringValues(Constants.X_KINVEY_REQUEST_START)[0].toUpperCase(Locale.US)
                } else if (response.headers.containsKey(Constants.X_KINVEY_REQUEST_START_CAMEL_CASE)) {
                    ret.lastRequestTime = response.headers.getHeaderStringValues(Constants.X_KINVEY_REQUEST_START_CAMEL_CASE)[0].toUpperCase(Locale.US)
                }
                return ret
            }

        } catch (e: IllegalArgumentException) {
            Logger.ERROR("unable to parse response -> $e")
            throw KinveyException("Unable to parse the JSON in the response", "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)", e.toString())
        } catch (ex: NullPointerException) {
            return null
        }
    }
}
