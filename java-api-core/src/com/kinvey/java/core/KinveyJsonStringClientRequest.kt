package com.kinvey.java.core

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.HttpMethods
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpStatusCodes
import com.google.api.client.json.Json
import com.kinvey.java.AbstractClient
import com.kinvey.java.KinveyException
import com.kinvey.java.Logger.Companion.ERROR
import com.kinvey.java.Logger.Companion.INFO
import com.kinvey.java.Logger.Companion.WARNING
import com.kinvey.java.core.KinveyJsonResponseException.Companion.from
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

open class KinveyJsonStringClientRequest<T> protected constructor(abstractKinveyJsonClient: AbstractClient<*>, requestMethod: String?,
                                                                  uriTemplate: String?,
                                                                  /** raw json data  */
                                                                  val jsonContent: String?,
                                                                  responseClass: Class<T>, myClass: Class<*>)
    : AbstractKinveyClientRequest<T>(abstractKinveyJsonClient, requestMethod, uriTemplate,
        if (jsonContent == null) null
        else ByteArrayContent(Json.MEDIA_TYPE, jsonContent.toByteArray()), responseClass) {
    /**
     * @return the jsonContent
     */
    var executor: AsyncExecutor<*>? = null
    private val responseClassType: Type
    private fun getType(rawClass: Class<*>, parameter: Class<*>): Type {
        return object : ParameterizedType {
            override fun getActualTypeArguments(): Array<Type> {
                return arrayOf(parameter)
            }

            override fun getRawType(): Type {
                return rawClass
            }

            override fun getOwnerType(): Type? {
                return null
            }
        }
    }

    override val abstractKinveyClient: AbstractKinveyJsonClient
        get() = super.abstractKinveyClient as AbstractKinveyJsonClient

    override fun newExceptionOnError(response: HttpResponse): KinveyJsonResponseException {
        return from(abstractKinveyClient.jsonFactory, response)
    }

    /**
     *
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun execute(): T? {
        INFO("Start execute for network request")
        val response = executeUnparsed()
        if (overrideRedirect) {
            INFO("overrideRedirect == true")
            return onRedirect(response?.headers?.location ?: "")
        }

        // special class to handle void or empty responses
        if (Void::class.java == responseClass || response?.content == null) {
            response?.ignore()
            return null
        }
        return try {
            val statusCode = response.statusCode
            if (response.request.requestMethod == HttpMethods.HEAD || statusCode / 100 == 1
                || statusCode == HttpStatusCodes.STATUS_CODE_NO_CONTENT || statusCode == HttpStatusCodes.STATUS_CODE_NOT_MODIFIED) {
                response.ignore()
                null
            } else {
                val parsedContent: Any = response.parseAs(responseClassType)
                response.disconnect()
                parsedContent as T
            }
        } catch (e: IllegalArgumentException) {
            ERROR("unable to parse response -> $e")
            throw KinveyException("Unable to parse the JSON in the response", "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)", e.toString())
        } catch (ex: NullPointerException) {
            WARNING(ex.message)
            null
        }
    }

    companion object {
        val uUID: String
            get() {
                val id = UUID.randomUUID().toString()
                return id.replace("-", "")
            }
    }

    /**
     * @param abstractKinveyJsonClient kinvey credential JSON client
     * @param requestMethod            HTTP Method
     * @param uriTemplate              URI template for the path relative to the base URL. If it starts with a "/"
     * the base path from the base URL will be stripped out. The URI template can also be a
     * full URL. URI template expansion is done using
     * @param jsonString              POJO that can be serialized into JSON content or `null` for none
     */

    init {
        if (jsonContent != null) {
            super.getRequestHeaders().contentType = Json.MEDIA_TYPE
        }
        responseClassType = getType(responseClass, myClass)
    }
}