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

package com.kinvey.android.store

import com.kinvey.android.KinveyCallbackHandler
import com.kinvey.android.async.AsyncDownloadRequest
import com.kinvey.android.async.AsyncRequest
import com.kinvey.android.async.AsyncUploadRequest
import com.kinvey.android.callback.AsyncDownloaderProgressListener
import com.kinvey.android.callback.AsyncUploaderProgressListener
import com.kinvey.android.callback.KinveyDeleteCallback
import com.kinvey.java.KinveyException
import com.kinvey.java.Query
import com.kinvey.java.cache.ICacheManager
import com.kinvey.java.cache.KinveyCachedClientCallback
import com.kinvey.java.core.DownloaderProgressListener
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.core.UploaderProgressListener
import com.kinvey.java.model.FileMetaData
import com.kinvey.java.network.NetworkFileManager
import com.kinvey.java.store.BaseFileStore
import com.kinvey.java.store.StoreType

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Method
import java.util.HashMap

/**
 * Created by Prots on 2/22/16.
 */
class FileStore(networkFileManager: NetworkFileManager,
                cacheManager: ICacheManager?, ttl: Long?, storeType: StoreType, cacheFolder: String?) : BaseFileStore(networkFileManager, cacheManager, ttl, storeType, cacheFolder) {


    private enum class FileMethods(val method: Method) {

        //UPLOAD METHODS
        UPLOAD_FILE(BaseFileStore::class.java.getDeclaredMethod("upload", File::class.java, UploaderProgressListener::class.java)),
        UPLOAD_FILE_METADATA(BaseFileStore::class.java.getDeclaredMethod("upload", File::class.java, FileMetaData::class.java, UploaderProgressListener::class.java)),
        UPLOAD_STREAM_METADATA(BaseFileStore::class.java.getDeclaredMethod("upload", InputStream::class.java, FileMetaData::class.java, UploaderProgressListener::class.java)),
        UPLOAD_STREAM_FILENAME(BaseFileStore::class.java.getDeclaredMethod("upload", String::class.java, InputStream::class.java, UploaderProgressListener::class.java)),

        //DELETE
        REMOVE_ID(BaseFileStore::class.java.getDeclaredMethod("remove", FileMetaData::class.java)),

        //DOWNLOAD
        DOWNLOAD_METADATA(BaseFileStore::class.java.getDeclaredMethod("download", FileMetaData::class.java, OutputStream::class.java, KinveyCachedClientCallback::class.java, DownloaderProgressListener::class.java)),
        CACHED_DOWNLOAD_METADATA(BaseFileStore::class.java.getDeclaredMethod("download", FileMetaData::class.java, OutputStream::class.java, OutputStream::class.java, KinveyCachedClientCallback::class.java, DownloaderProgressListener::class.java)),

        //REFRESH
        REFRESH_FILE(BaseFileStore::class.java.getDeclaredMethod("refresh", FileMetaData::class.java, KinveyCachedClientCallback::class.java)),

        //FIND
        FIND_QUERY(BaseFileStore::class.java.getDeclaredMethod("find", Query::class.java, KinveyCachedClientCallback::class.java))

    }

    @Throws(IOException::class, KinveyException::class)
    fun upload(file: File, listener: AsyncUploaderProgressListener<FileMetaData>) {
        AsyncUploadRequest(this, FileMethods.UPLOAD_FILE.method, listener, file).execute()
    }

    @Throws(IOException::class)
    fun upload(file: File, metadata: FileMetaData,
               listener: AsyncUploaderProgressListener<FileMetaData>) {

        AsyncUploadRequest(this, FileMethods.UPLOAD_FILE_METADATA.method, listener,
                file, metadata).execute()
    }

    @Throws(IOException::class)
    fun upload(`is`: InputStream, metadata: FileMetaData, listener: AsyncUploaderProgressListener<FileMetaData>) {
        AsyncUploadRequest(this, FileMethods.UPLOAD_STREAM_METADATA.method, listener,
                `is`, metadata).execute()
    }

    @Throws(IOException::class)
    fun upload(filename: String, `is`: InputStream,
               listener: AsyncUploaderProgressListener<FileMetaData>) {
        AsyncUploadRequest(this, FileMethods.UPLOAD_STREAM_FILENAME.method, listener,
                filename, `is`).execute()
    }

    @Throws(IOException::class)
    fun remove(metadata: FileMetaData, callback: KinveyDeleteCallback) {
        AsyncRequest(this, FileMethods.REMOVE_ID.method, callback,
                metadata).execute()
    }

    @Throws(IOException::class)
    fun download(metadata: FileMetaData, os: OutputStream,
                 progressListener: AsyncDownloaderProgressListener<FileMetaData>, cachedClientCallback: KinveyCachedClientCallback<FileMetaData>?) {
        download(metadata, os, progressListener, null, cachedClientCallback)
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun download(metadata: FileMetaData, os: OutputStream,
                 progressListener: AsyncDownloaderProgressListener<FileMetaData>, cachedOs: OutputStream? = null, cachedClientCallback: KinveyCachedClientCallback<FileMetaData>? = null) {
        AsyncDownloadRequest(this, FileMethods.CACHED_DOWNLOAD_METADATA.method, progressListener,
                metadata, os, cachedOs, getWrappedCacheCallback(cachedClientCallback)).execute()
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun refresh(metadata: FileMetaData, metaCallback: KinveyClientCallback<FileMetaData>, cachedClientCallback: KinveyCachedClientCallback<FileMetaData>? = null) {
        AsyncRequest(this, FileMethods.REFRESH_FILE.method, metaCallback, metadata, cachedClientCallback).execute()
    }

    @JvmOverloads
    fun find(q: Query, metaCallback: KinveyClientCallback<Array<FileMetaData>>, cachedClientCallback: KinveyCachedClientCallback<Array<FileMetaData>>? = null) {
        AsyncRequest(this, FileMethods.FIND_QUERY.method, metaCallback, q, cachedClientCallback).execute()
    }

    fun cachedFile(fileId: String): FileMetaData? {
        return cache?.get(fileId)
    }

    fun cachedFile(fileMetaData: FileMetaData): FileMetaData? {
        return if (fileMetaData.id == null) {
            //"File.fileId is required"
            null
        } else fileMetaData.id?.let { cache?.get(it) }
    }

    private class ThreadedKinveyCachedClientCallback<T> internal constructor(private val callback: KinveyCachedClientCallback<T>) : KinveyCachedClientCallback<T> {

        internal var handler: KinveyCallbackHandler<T>


        init {
            handler = KinveyCallbackHandler()
        }

        override fun onSuccess(result: T) {
            handler.onResult(result, callback)
        }

        override fun onFailure(error: Throwable) {
            handler.onFailure(error, callback)

        }
    }

    fun clearCache() {
        cache?.clear()
    }

    companion object {

        private fun <T> getWrappedCacheCallback(callback: KinveyCachedClientCallback<T>?): KinveyCachedClientCallback<T>? {
            var ret: KinveyCachedClientCallback<T>? = null
            if (callback != null) {
                ret = ThreadedKinveyCachedClientCallback(callback)
            }
            return ret
        }

    }

}
