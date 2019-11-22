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

import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.GenericJson
import com.kinvey.java.AbstractClient
import com.kinvey.java.Logger.Companion.INFO
import com.kinvey.java.MimeTypeFinder
import com.kinvey.java.core.AbstractKinveyJsonClientRequest
import com.kinvey.java.core.MediaHttpUploader
import com.kinvey.java.core.UploaderProgressListener
import com.kinvey.java.model.FileMetaData
import com.kinvey.java.store.BaseFileStore
import com.kinvey.java.store.StoreType
import java.io.ByteArrayInputStream
import java.io.IOException

/**
 * Implementation of a Client Request, which can upload linked resources.
 *
 *
 * On the call to execute, if a file is a LinkedGenericJson, then this iterates through all the attachments and uploads them.
 * Once all files have been uploaded, a call to super.execute() is made.
 *
 *
 *
 * call setUploadProgressListener to get callbacks for all file uploads.
 *
 *
 * @author edwardf
 * @since 2.0.7
 */
open class SaveLinkedResourceClientRequest<T>
/**
 * @param abstractKinveyJsonClient kinvey credential JSON client
 * @param requestMethod            HTTP Method
 * @param uriTemplate              URI template for the path relative to the base URL. If it starts with a "/"
 * the base path from the base URL will be stripped out. The URI template can also be a
 * full URL. URI template expansion is done using
 * [com.google.api.client.http.UriTemplate.expand]
 * @param jsonContent              POJO that can be serialized into JSON content or `null` for none
 * @param responseClass            response class to parse into
 */
protected constructor(abstractKinveyJsonClient: AbstractClient<*>?, requestMethod: String?,
                      uriTemplate: String?, jsonContent: GenericJson?, responseClass: Class<T>?)
    : AbstractKinveyJsonClientRequest<T>(abstractKinveyJsonClient, requestMethod ?: "",
        uriTemplate ?: "", jsonContent, responseClass) {
    var upload: UploaderProgressListener? = null
    private var mimeTypeFinder: MimeTypeFinder? = null
    fun setMimeTypeFinder(finder: MimeTypeFinder?) {
        mimeTypeFinder = finder
    }

    @Throws(IOException::class)
    override fun execute(): T? {
        //TODO edwardf possible optimization-- if file hasn't changed, don't bother uploading it...? not sure if possible

        return if (jsonContent is LinkedGenericJson) {
            INFO("Kinvey - LR, " + "linked resource found, file count at: " + jsonContent.allFiles.keys.size)
            for (key in jsonContent.allFiles.keys) {
                if (jsonContent.getFile(key) != null) {
                    INFO("Kinvey - LR, found a LinkedGenericJson: $key")// + " -> " + ((LinkedGenericJson) getJsonContent()).getFile(key).getId());

                    if (jsonContent.getFile(key)?.isResolve == true) {
                        val inStream = jsonContent.getFile(key)?.input
                        var mediaContent: InputStreamContent? = null
                        val mimetype = "application/octet-stream"
                        inStream?.let {
                            mediaContent = InputStreamContent(mimetype, it)
                            mediaContent?.closeInputStream = false
                            mediaContent?.setRetrySupported(false)
                        }
                        val metaUploadListener: MetaUploadListener = object : MetaUploadListener {
                            override fun metaDataUploaded(metaData: FileMetaData?) {}
                            @Throws(IOException::class)
                            override fun progressChanged(uploader: MediaHttpUploader) {
                            }
                        }
                        val fileStore = abstractKinveyClient.getFileStore(StoreType.SYNC)
                        val linkedFile = jsonContent.getFile(key)
                        val meta = FileMetaData(linkedFile?.id ?: "")
                        meta.fileName = linkedFile?.fileName
                        if (linkedFile?.hasExtras() == true) {
                            linkedFile.extras?.keys?.onEach { k -> meta[k] = linkedFile.getExtra(k) }
                        }
                        inStream?.run { mimeTypeFinder?.getMimeType(meta, this) }
                        val stream = mediaContent?.inputStream ?: ByteArrayInputStream(byteArrayOf())
                        val file = fileStore?.upload(stream, meta, object : UploaderProgressListener {
                            @Throws(IOException::class)
                            override fun progressChanged(uploader: MediaHttpUploader) {
                                if (upload != null) {
                                    upload?.progressChanged(uploader)
                                }
                            }
                        })
                    }
                }
            }
            INFO("Kinvey - LR, " + "saving the entity!")
            super.execute()
        } else {
            super.execute()
        }
    }

    interface MetaUploadListener : UploaderProgressListener {
        /**
         * Called to notify that metadata has been successfully uploaded to the /blob/
         *
         *
         *
         *
         * This method is called once, before the file upload actually begins but after the metadata has been set in the
         * blob collection.  This metadata is used by the NetworkFileManager API to determine the upload URL, and contains the id of the file.
         *
         *
         * @param metaData - The NetworkFileManager MetaData associated with the upload about to occur.
         */
        fun metaDataUploaded(metaData: FileMetaData?)
    }
}