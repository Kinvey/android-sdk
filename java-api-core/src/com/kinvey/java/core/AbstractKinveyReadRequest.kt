package com.kinvey.java.core

import com.google.api.client.http.HttpMethods
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpStatusCodes
import com.google.api.client.json.GenericJson
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.util.Charsets
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.kinvey.java.AbstractClient
import com.kinvey.java.Constants
import com.kinvey.java.KinveyException
import com.kinvey.java.Logger
import com.kinvey.java.model.KinveyReadResponse

import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.ArrayList
import java.util.Locale

/**
 * Created by yuliya on 10/26/17.
 */

abstract class AbstractKinveyReadRequest<T>
/**
 * @param abstractKinveyJsonClient kinvey credential JSON client
 * @param requestMethod            HTTP Method
 * @param uriTemplate              URI template for the path relative to the base URL. If it starts with a "/"
 * the base path from the base URL will be stripped out. The URI template can also be a
 * full URL.
 * @param jsonContent              POJO that can be serialized into JSON content or `null` for none
 * @param responseClass            response class to parse into
 */
protected constructor(abstractKinveyJsonClient: AbstractClient<*>, requestMethod: String, uriTemplate: String,
                      jsonContent: GenericJson?, var requestResponseClass: Class<*>?)
    : AbstractKinveyJsonClientRequest<KinveyReadResponse<T>>(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonContent, null) {

    @Throws(IOException::class)
    override fun execute(): KinveyReadResponse<T>? {

        val response = executeUnparsed()

        val results = ArrayList<T>()
        val exceptions = ArrayList<Exception>()

        val ret = KinveyReadResponse<T>()

        if (overrideRedirect) {
            return onRedirect(response.headers.location)
        }

        // special class to handle void or empty responses
        if (Void::class.java == requestResponseClass || response.content == null) {
            response.ignore()
            return null
        }

        try {
            val statusCode = response.statusCode
            if (response.request.requestMethod == HttpMethods.HEAD || statusCode / 100 == 1
                    || statusCode == HttpStatusCodes.STATUS_CODE_NO_CONTENT
                    || statusCode == HttpStatusCodes.STATUS_CODE_NOT_MODIFIED) {
                response.ignore()
                return null
            } else {
                val jsonString = response.parseAsString()
                val jsonParser = JsonParser()
                val jsonArray = jsonParser.parse(jsonString) as JsonArray
                val objectParser = abstractKinveyClient.objectParser
                jsonArray.onEach { element ->
                    try {
                        results.add(objectParser.parseAndClose(ByteArrayInputStream(element.toString().toByteArray(Charsets.UTF_8)), Charsets.UTF_8, requestResponseClass) as T)
                    } catch (e: IllegalArgumentException) {
                        Logger.ERROR("unable to parse response -> $e")
                        exceptions.add(KinveyException("Unable to parse the JSON in the response", "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)", e.toString()))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        exceptions.add(e)
                    }
                }
                if (response.headers.containsKey(Constants.X_KINVEY_REQUEST_START)) {
                    ret.lastRequestTime = response.headers.getHeaderStringValues(Constants.X_KINVEY_REQUEST_START)[0].toUpperCase(Locale.US)
                } else if (response.headers.containsKey(Constants.X_KINVEY_REQUEST_START_CAMEL_CASE)) {
                    ret.lastRequestTime = response.headers.getHeaderStringValues(Constants.X_KINVEY_REQUEST_START_CAMEL_CASE)[0].toUpperCase(Locale.US)
                }
                ret.result = results
                ret.listOfExceptions = exceptions
                return ret
            }

        } catch (e: IllegalArgumentException) {
            Logger.ERROR("unable to parse response -> $e")
            throw KinveyException("Unable to parse the JSON in the response", "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)", e.toString())
        } catch (ex: NullPointerException) {
            Logger.WARNING(ex.message)
            return null
        }
    }
}
