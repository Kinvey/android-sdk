/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 *
 */

package com.kinvey.java.linkedResources

import com.google.api.client.json.GenericJson
import com.kinvey.java.AbstractClient
import com.kinvey.java.Logger.Companion.INFO
import com.kinvey.java.core.AbstractKinveyJsonClientRequest
import com.kinvey.java.core.DownloaderProgressListener
import com.kinvey.java.model.FileMetaData
import com.kinvey.java.store.BaseFileStore
import com.kinvey.java.store.StoreType
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Implementation of a Client Request, which can download linked resources through the NetworkFileManager API as well as the NetworkManager API in one request.
 *
 *
 * On the call to execute, if a file is a LinkedGenericJson, then first it gets the entity.  Then it iterates through all the attachments and downloads them.
 * Once all files have been downloaded, the entity is returned
 *
 *
 *
 * call setDownloadProgressListener to get callbacks for all file downloads.
 *
 *
 * @author edwardf
 * @since 2.0.7
 */
open class GetLinkedResourceClientRequest<T>
/**
 * @param abstractKinveyJsonClient kinvey credential JSON client
 * @param uriTemplate              URI template for the path relative to the base URL. If it starts with a "/"
 * the base path from the base URL will be stripped out. The URI template can also be a
 * full URL. URI template expansion is done using
 * [com.google.api.client.http.UriTemplate.expand]
 * @param jsonContent              POJO that can be serialized into JSON content or `null` for none
 * @param responseClass            response class to parse into
 */
protected constructor(abstractKinveyJsonClient: AbstractClient<*>?, uriTemplate: String, jsonContent: GenericJson?, responseClass: Class<T>?)
    : AbstractKinveyJsonClientRequest<T>(abstractKinveyJsonClient, "GET", uriTemplate, jsonContent, responseClass) {
    var downloadProgressListener: DownloaderProgressListener? = null
    @Throws(IOException::class)
    override fun execute(): T? {
        val entity = super.execute()
        return if (entity is Array<*>) {
            INFO("Kinvey - LR, " + "linked resource array found")
            val casted = entity as Array<LinkedGenericJson>
            for (ent in casted) {
                downloadResources(ent)
            }
            entity
        } else if (entity is LinkedGenericJson) {
            INFO("Kinvey - LR, " + "linked resource instance found")
            downloadResources(entity as LinkedGenericJson)
            entity
        } else {
            INFO("Kinvey - LR, " + "not a linked resource, behaving as usual!")
            entity
        }
    }

    @Throws(IOException::class)
    private fun downloadResources(entity: LinkedGenericJson) {
        INFO("Kinvey - LR, " + "linked resource found, file count at: " + entity.allFiles.keys.size)
        for (key in entity.allFiles.keys) {
            if (entity[key] != null) {
                INFO("Kinvey - LR, getting a LinkedGenericJson: $key")//-> " + ((Map) entity.get(key)).get("_loc").toString());
                if (entity.getFile(key) == null) {
                    if ((entity[key] as Map<*, *>).containsKey("_id")) {
                        entity.putFile(key, LinkedFile((entity[key] as Map<*, *>)["_id"].toString()))
                    } else if ((entity[key] as Map<*, *>).containsKey("_loc")) {     //TODO backwards compt for v2 of NetworkFileManager API, this condition can be removed when it's done
                        val lf = LinkedFile()
                        lf.fileName = (entity[key] as Map<*, *>)["_loc"].toString()
                        entity.putFile(key, lf)
                    }
                }
                val stream = ByteArrayOutputStream()
                entity.getFile(key)?.output = stream
                val store = abstractKinveyClient.getFileStore(StoreType.SYNC)
                val meta = FileMetaData()
                if ((entity[key] as Map<*, *>).containsKey("_id")) {
                    meta.id = (entity[key] as Map<*, *>)["_id"].toString()
                    store?.download(meta, stream, null, null, downloadProgressListener)
                }
            }
        }
    }
}