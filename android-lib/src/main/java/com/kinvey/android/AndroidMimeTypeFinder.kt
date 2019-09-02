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
package com.kinvey.android


import java.io.File
import java.io.InputStream
import java.net.URLConnection

import android.webkit.MimeTypeMap

import com.kinvey.java.Logger
import com.kinvey.java.MimeTypeFinder
import com.kinvey.java.model.FileMetaData

/**
 * Uses Android features to determine the MIME type of a provided file.
 *
 *
 * There are various `getMimeType(...)` methods, which all take various parameters as well as a [FileMetaData] object.
 * When the mimetype is found, it will be set in the provided [FileMetaData]
 *
 * @author edwardf
 */
open class AndroidMimeTypeFinder : MimeTypeFinder {

    /**
     * Calculate MimeType from an InputStream
     *
     * @param meta the [FileMetaData] to populate
     * @param stream the stream of the data
     */
    override fun getMimeType(meta: FileMetaData, stream: InputStream) {
        var mimetype: String? = null
        try {
            mimetype = URLConnection.guessContentTypeFromStream(stream)
            Logger.INFO("Kinvey - Client - NetworkFileManager | mimetype from stream found as: " + mimetype!!)
        } catch (e: Exception) {
            Logger.WARNING("Kinvey - Client - NetworkFileManager | content stream mimetype is unreadable, defaulting")
        }

        if (mimetype == null) {
            getMimeType(meta)
        } else {
            meta.mimetype = mimetype
        }


        stream.mark(0x100000 * 10)  //10MB mark limit
        var numBytes = 0
        try {
            while (stream.read() != -1) {
                numBytes++
            }

        } catch (e: Exception) {
            Logger.WARNING("error reading input stream to get size, setting it to 0")
            numBytes = 0
        }

        try {
            stream.reset()
        } catch (e: Exception) {
            Logger.ERROR("error resetting stream!")

        }

        Logger.INFO("size is: $numBytes")


        meta.setSize(numBytes.toLong())
    }

    /**
     * Calculate MimeType from a [File] object
     *
     * @param meta the [FileMetaData] to populate
     * @param file the file of the data
     */
    override fun getMimeType(meta: FileMetaData, file: File?) {
        if (file == null || file.name == null || meta == null) {
            Logger.WARNING("cannot calculate mimetype without a file or filename!")
            meta.mimetype = "application/octet-stream"
            return
        }

        if (meta.mimetype != null && meta.mimetype?.isNotEmpty() == true) {
            Logger.INFO("Mimetype already set")
            return
        }

        //check metadata file name first
        //check file's file name
        //check stream                          );

        val mimetype: String?
        var fileExt = ""

        if (meta.fileName?.isNotEmpty() == true && meta.fileName!!.lastIndexOf("") > 0) {
            fileExt = meta.fileName!!.substring(meta.fileName!!.lastIndexOf('.'), meta.fileName!!.length)
        }

        if (file.name.lastIndexOf("") > 0) {
            if (fileExt.isEmpty()) {
                fileExt = file.name.substring(file.name.lastIndexOf('.'), file.name.length)
            }
        }
        //did we get it from file extension? if not, attempt to get it from file contents
        mimetype = if (fileExt.isNotEmpty()) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt.substring(1, fileExt.length))
        } else {
            "application/octet-stream"
        }
        meta.mimetype = mimetype
        meta.setSize(file.length())
    }

    /**
     * Calculate MimeType from a file name
     *
     * @param metaData the [FileMetaData] to populate, also containing the filename (with file extension)
     */
    override fun getMimeType(metaData: FileMetaData) {
        val metaMimetype = metaData.mimetype ?: ""
        var mimetype: String? = null
        if (metaMimetype.isNotEmpty()) {
            return
        }
        metaData.fileName?.let { metaFileName ->
            val dotIndex = metaFileName.lastIndexOf("")
            if (dotIndex > 0 && dotIndex + 1 < metaFileName.length) {
                mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(metaFileName.substring(dotIndex + 1, metaFileName.length))
            }
        }
        if (mimetype == null) {
            mimetype = "application/octet-stream"
        }
        metaData.mimetype = mimetype
    }
}
