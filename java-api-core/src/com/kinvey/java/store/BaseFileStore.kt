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

package com.kinvey.java.store

import com.google.api.client.http.FileContent
import com.google.api.client.http.InputStreamContent
import com.google.api.client.util.Key
import com.google.common.base.Preconditions
import com.google.common.io.Closer
import com.kinvey.java.AbstractClient
import com.kinvey.java.KinveyException
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.cache.ICacheManager
import com.kinvey.java.cache.KinveyCachedClientCallback
import com.kinvey.java.core.DownloaderProgressListener
import com.kinvey.java.core.MediaHttpDownloader
import com.kinvey.java.core.MediaHttpUploader
import com.kinvey.java.core.MetaDownloadProgressListener
import com.kinvey.java.core.UploaderProgressListener
import com.kinvey.java.model.FileMetaData
import com.kinvey.java.network.NetworkFileManager
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.file.FileUtils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


open class BaseFileStore
/**
 * File store constructor
 * @param networkFileManager manager for the network file requests
 * @param cacheManager manager for local cache file request
 * @param ttl Time to Live for cached files
 * @param storeType Store type to be used
 * @param cacheFolder local cache folder to be used on device
 */
(private val networkFileManager: NetworkFileManager, private val cacheManager: ICacheManager, private val ttl: Long?, private var storeType: StoreType?, protected val cacheFolder: String) {
    protected val cache: ICache<FileMetadataWithPath>
    private var downloader: MediaHttpDownloader? = null
    private var uploader: MediaHttpUploader? = null

    init {
        this.cache = cacheManager.getCache("__KinveyFile__", FileMetadataWithPath::class.java, ttl)
    }

    /**
     * Uploading file using specified StoreType
     * @param file File to upload
     * @param listener progress listener to be used to track file upload progress
     * @return FileMetadata object that contains usefull informaton about file
     * @throws IOException
     */
    @Throws(IOException::class)
    fun upload(file: File, listener: UploaderProgressListener): FileMetaData {
        Preconditions.checkNotNull(file)
        Preconditions.checkNotNull(listener)
        val fm = FileMetaData()
        fm.fileName = file.name
        return upload(file, fm, listener)
    }


    /**
     * Uploading file with specific metadata, where you can define additional fields to be stored along the file
     * @param file File to be uploaded
     * @param metadata additional metadata to be stored
     * @param listener progress listener to be used to track file upload progress
     * @return FileMetadata object that contains usefull informaton about file
     * @throws IOException
     */
    @Throws(IOException::class)
    fun upload(file: File, metadata: FileMetaData,
               listener: UploaderProgressListener): FileMetaData {
        var metadata = metadata
        Preconditions.checkNotNull(file, "file must not be null")
        Preconditions.checkNotNull(metadata, "metadata must not be null")
        Preconditions.checkNotNull(listener, "listener must not be null")

        metadata!!.fileSize = file.length()

        val fileMetadataWithPath = FileMetadataWithPath()
        fileMetadataWithPath.putAll(metadata)

        if (fileMetadataWithPath.id == null) {
            fileMetadataWithPath.id = UUID.randomUUID().toString()
        }

        val upload = networkFileManager.prepUploadBlocking(fileMetadataWithPath,
                FileContent(fileMetadataWithPath.mimetype, file), listener)
        upload.uploader?.let { setUploader(it) }

        when (storeType!!.writePolicy) {
            WritePolicy.FORCE_LOCAL -> {
                saveCacheFile(FileInputStream(file), fileMetadataWithPath)
                metadata = fileMetadataWithPath
            }
            WritePolicy.FORCE_NETWORK -> upload.execute()?.let { metadata = it }
            WritePolicy.LOCAL_THEN_NETWORK -> {
                try {
                    upload.execute()?.let { metadata = it }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                fileMetadataWithPath.putAll(metadata!!)
                saveCacheFile(FileInputStream(file), fileMetadataWithPath)
            }
        }

        return metadata
    }

    /**
     * Upload specified stream with additional metadata to the store
     * @param is Input stream to be uploaded
     * @param metadata Matadata to be stored along with file
     * @param listener progress listener to be used to track file upload progress
     * @return FileMetadata object that contains usefull informaton about file
     * @throws IOException
     */
    @Throws(IOException::class)
    fun upload(`is`: InputStream, metadata: FileMetaData, listener: UploaderProgressListener): FileMetaData {
        var metadata = metadata
        Preconditions.checkNotNull(`is`, "inputStream must not be null")
        Preconditions.checkNotNull(metadata, "metadata must not be null")
        Preconditions.checkNotNull(listener, "listener must not be null")

        val fileMetadataWithPath = FileMetadataWithPath()
        fileMetadataWithPath.putAll(metadata!!)

        if (fileMetadataWithPath.id == null) {
            fileMetadataWithPath.id = UUID.randomUUID().toString()
        }

        val upload = networkFileManager.prepUploadBlocking(fileMetadataWithPath, InputStreamContent(null, `is`), listener)
        upload.uploader?.let { setUploader(it) }

        when (storeType!!.writePolicy) {
            WritePolicy.FORCE_LOCAL -> {
                saveCacheFile(`is`, fileMetadataWithPath)
                metadata = fileMetadataWithPath
            }
            WritePolicy.FORCE_NETWORK -> upload.execute()?.let { metadata = it }
            WritePolicy.LOCAL_THEN_NETWORK -> {
                saveCacheFile(`is`, fileMetadataWithPath)
                try {
                    upload.execute()?.let { metadata = it }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                fileMetadataWithPath.putAll(metadata!!)
                cache.save(fileMetadataWithPath)
            }
        }

        return metadata
    }


    /**
     * Upload specified stream with specified file name to the store
     * @param is Input stream to be uploaded
     * @param filename will be stored as file name to saved stream
     * @param listener progress listener to be used to track file upload progress
     * @return FileMetadata object that contains usefull informaton about file
     * @throws IOException
     */
    @Throws(IOException::class)
    fun upload(filename: String, `is`: InputStream, listener: UploaderProgressListener): FileMetaData {
        Preconditions.checkNotNull(filename, "filename must not be null")
        Preconditions.checkNotNull(`is`, "inputStream must not be null")
        Preconditions.checkNotNull(listener, "listener must not be null")

        val fileMetaData = FileMetaData()
        fileMetaData.fileName = filename

        return upload(`is`, fileMetaData, listener)
    }


    /**
     * Remove file from the store
     * @param metadata of the file to be deleted, the id field of metadata is required
     * @return number of removed items 1 if file was removed and 0 if file was not found
     * @throws IOException
     */
    @Throws(IOException::class)
    fun remove(metadata: FileMetaData): Int? {
        Preconditions.checkNotNull(metadata.id, "metadata must not be null")
        return metadata.id?.let {
            when (storeType!!.writePolicy) {
                WritePolicy.FORCE_LOCAL -> {
                    cache.delete(it)
                    return 1
                }
                WritePolicy.LOCAL_THEN_NETWORK -> {
                    cache.delete(it)
                    return networkFileManager.deleteBlocking(it).execute()!!.count
                }
                WritePolicy.FORCE_NETWORK -> return networkFileManager.deleteBlocking(it).execute()!!.count
                else -> return 0
            }
        }
    }

    /**
     * Query server for matching files
     * @param q query to be executed against Files store
     * @param cachedCallback - callback to be executed if StoreType.CACHE is used
     * @return Array of file metadata that matches the query
     * @throws IOException
     */
    @Throws(IOException::class)
    fun find(q: Query,
             cachedCallback: KinveyCachedClientCallback<Array<FileMetaData>>?): Array<FileMetaData>? {
        Preconditions.checkNotNull(q, "query must not be null")
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE")
        val download = networkFileManager.prepDownloadBlocking(q)
        var metaData: Array<FileMetaData>? = null
        when (storeType!!.readPolicy) {
            ReadPolicy.FORCE_LOCAL -> metaData = getFileMetaDataFromCache(q)
            ReadPolicy.BOTH -> {
                cachedCallback?.onSuccess(getFileMetaDataFromCache(q))
                metaData = download.execute()
            }
            ReadPolicy.FORCE_NETWORK -> metaData = download.execute()
            ReadPolicy.NETWORK_OTHERWISE_LOCAL -> {
                var networkException: IOException? = null
                try {
                    metaData = download.execute()
                } catch (e: IOException) {
                    if (NetworkManager.checkNetworkRuntimeExceptions(e)) {
                        throw e
                    }
                    networkException = e
                }

                // if the network request fails, fetch data from local cache
                if (networkException != null) {
                    metaData = getFileMetaDataFromCache(q)
                }
            }
        }
        return metaData
    }

    /**
     * Query cache for matching files
     * @param q query to be executed against Cached store
     * @return Array of file metadata that matches the query
     * @throws IOException
     */
    private fun getFileMetaDataFromCache(q: Query): Array<FileMetaData> {
        val list = ArrayList(cache.get(q))
        val metaData: Array<FileMetaData?> = arrayOfNulls(list.size)
        return list.toArray(metaData)
    }

    /**
     * Query server for matching file
     * @param id id of file we are looking for
     * @param cachedCallback - callback to be executed if StoreType.CACHE is used
     * @return Array of file metadata that matches the query
     * @throws IOException
     */
    @Throws(IOException::class)
    fun find(id: String,
             cachedCallback: KinveyCachedClientCallback<FileMetaData>?): FileMetaData? {
        Preconditions.checkNotNull(id, "id must not be null")
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE")
        val download = networkFileManager.downloadMetaDataBlocking(id)
        var metaData: FileMetaData? = null
        when (storeType!!.readPolicy) {
            ReadPolicy.FORCE_LOCAL -> metaData = cache.get(id)
            ReadPolicy.BOTH -> {
                if (storeType == StoreType.CACHE && cachedCallback != null) {
                    metaData = cache.get(id)
                    cachedCallback.onSuccess(metaData)
                }
                metaData = download.execute()
            }
            ReadPolicy.FORCE_NETWORK -> metaData = download.execute()
            ReadPolicy.NETWORK_OTHERWISE_LOCAL -> {
                var networkException: IOException? = null
                try {
                    metaData = download.execute()
                } catch (e: IOException) {
                    if (NetworkManager.checkNetworkRuntimeExceptions(e)) {
                        throw e
                    }
                    networkException = e
                }

                // if the network request fails, fetch data from local cache
                if (networkException != null) {
                    metaData = cache.get(id)
                }
            }
        }
        return metaData
    }

    /**
     * Refreshes metadata on local cache by grabbing latest info from server
     * @param fileMetaData metadata that we want to refresh
     * @param cachedCallback - callback to be executed if StoreType.CACHE is used
     * @return Array of file metadata that matches the query
     * @throws IOException
     */
    @Throws(IOException::class)
    fun refresh(fileMetaData: FileMetaData,
                cachedCallback: KinveyCachedClientCallback<FileMetaData>?): FileMetaData? {
        Preconditions.checkNotNull(fileMetaData, "metadata must not be null")
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE")
        return fileMetaData.id?.let {
            return find(it, cachedCallback)
        }
    }

    /**
     * download file with given metadata to specified OutputStream
     * @param metadata metadata of the file we want to download
     * @param os OutputStream where file content should be streamed
     * @param cachedOs OutputStream where cached file content should be streamed
     * @param cachedCallback - callback to be executed if StoreType.CACHE is used
     * @return metadata of the file we are downloading
     * @throws IOException
     */
    @Throws(IOException::class)
    @JvmOverloads
    fun download(metadata: FileMetaData,
                 os: OutputStream,
                 cachedOs: OutputStream? = null,
                 cachedCallback: KinveyCachedClientCallback<FileMetaData>?,
                 progressListener: DownloaderProgressListener): FileMetaData? {
        Preconditions.checkNotNull(metadata, "metadata must not be null")
        Preconditions.checkNotNull(metadata.id, "metadata.getId must not be null")
        Preconditions.checkNotNull(progressListener, "listener must not be null")
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE")
        val resultMetadata: FileMetaData?
        if (metadata.resumeDownloadData != null) {
            resultMetadata = metadata
        } else {
            resultMetadata = metadata.id?.let { find(it, null) }
        }
        sendMetadata(resultMetadata, progressListener)
        return getFile(resultMetadata, os, storeType!!.readPolicy, progressListener, cachedOs, cachedCallback)
    }

    fun cancelDownloading(): Boolean {
        if (downloader != null) {
            downloader!!.cancel()
            return true
        } else {
            return false
        }
    }

    fun cancelUploading(): Boolean {
        if (this.uploader != null) {
            this.uploader!!.cancel()
            return true
        } else {
            return false
        }
    }

    private fun cacheStorage(): File {
        val f = File(cacheFolder)
        if (!f.exists()) {
            f.mkdirs()
        } else if (!f.isDirectory) {
            throw KinveyException("InvalidCachedFolder", "file with name already exists", "")
        }
        return f
    }


    @Throws(IOException::class)
    private fun getNetworkFile(metadata: FileMetaData?, os: OutputStream, listener: DownloaderProgressListener): FileMetaData? {
        val client = networkFileManager.client
        val downloader = MediaHttpDownloader(client.requestFactory.transport,
                client.requestFactory.initializer)
        downloader.progressListener = listener
        setDownloader(downloader)
        return metadata?.let { downloader.download(it, os) }
    }

    /*    private FileMetaData getNetworkFile(FileMetaData metadata, String dst, DownloaderProgressListener listener) throws IOException {
        File f = new File(dst);
        if (!f.exists()) {
            f.mkdirs();
        } else if (!f.isDirectory()) {
            throw new KinveyException("dst is not a dirrectory", "Please provide valid path to file destination", "");
        }

        File out = new File(f, metadata.getId());
        if (!out.exists()) {
                out.createNewFile();
            }
        return getNetworkFile(metadata, new FileOutputStream(out), listener);
    }*/

    private fun getCachedFile(metadata: FileMetaData?): File? {
        Preconditions.checkNotNull(metadata, "metadata must not be null")
        var ret: File? = null
        if (metadata!!.containsKey(CACHE_FILE_PATH)) {
            val cacheFilePath = metadata[CACHE_FILE_PATH]!!.toString()
            ret = File(cacheFilePath)
        }
        if (ret == null && metadata.containsKey("_id") && metadata.id != null) {
            ret = File(cacheStorage(), metadata.id)
        }
        return if (ret == null || !ret.exists()) null else ret
    }

    @Throws(IOException::class)
    private fun getFile(metadata: FileMetaData?,
                        os: OutputStream,
                        readPolicy: ReadPolicy,
                        listener: DownloaderProgressListener,
                        cachedOs: OutputStream?,
                        cachedCallback: KinveyCachedClientCallback<FileMetaData>?): FileMetaData? {
        Preconditions.checkArgument(cachedCallback == null || readPolicy == ReadPolicy.BOTH, "KinveyCachedClientCallback can only be used with StoreType.CACHE")
        var f: File? = null

        when (readPolicy) {
            ReadPolicy.FORCE_LOCAL -> {
                f = getCachedFile(metadata)
                if (f == null) {
                    throw KinveyException("FileMissing", "File Missing in cache", "")
                } else {
                    FileUtils.copyStreams(FileInputStream(f), os)
                    return metadata
                }
            }
            ReadPolicy.BOTH -> {
                val cachedFile = getCachedFile(metadata)
                if (cachedFile == null) {
                    cachedCallback?.onFailure(KinveyException("FileMissing", "File Missing in cache", ""))
                } else {
                    if (cachedCallback != null) {
                        if (cachedOs != null) {
                            FileUtils.copyStreams(FileInputStream(cachedFile), cachedOs)
                        } else {
                            metadata!!.path = cachedFile.absolutePath
                        }
                        cachedCallback.onSuccess(metadata)
                    }
                }
                val fmd = getNetworkFile(metadata, os, listener)
                if (fmd != null) {
                    f = File(cacheStorage(), metadata!!.id)
                    if (!f.exists()) {
                        f.createNewFile()
                    }
                    val fmdWithPath = FileMetadataWithPath()
                    os.write(f.absolutePath.toByteArray())
                    fmdWithPath.putAll(fmd)
                    fmdWithPath.path = f.absolutePath
                    cache.save(fmdWithPath)
                }
                return fmd
            }
            ReadPolicy.FORCE_NETWORK -> return getNetworkFile(metadata, os, listener)
            ReadPolicy.NETWORK_OTHERWISE_LOCAL -> {
                var networkException: IOException? = null
                try {
                    val fm = getNetworkFile(metadata, os, listener)
                    if (fm != null) {
                        f = File(cacheStorage(), metadata!!.id)
                        if (!f.exists()) {
                            f.createNewFile()
                        }
                        val fmdWithPath = FileMetadataWithPath()
                        os.write(f.absolutePath.toByteArray())
                        fmdWithPath.putAll(fm)
                        fmdWithPath.path = f.absolutePath
                        cache.save(fmdWithPath)
                    }
                    return fm
                } catch (e: IOException) {
                    if (NetworkManager.checkNetworkRuntimeExceptions(e)) {
                        throw e
                    }
                    networkException = e
                }

                // if the network request fails, fetch data from local cache
                if (networkException != null) {
                    f = getCachedFile(metadata)
                    if (f == null) {
                        throw KinveyException("FileMissing", "File Missing in cache", "")
                    } else {
                        FileUtils.copyStreams(FileInputStream(f), os)
                        return metadata
                    }
                }
            }
        }
        return null
    }

    private fun sendMetadata(metadata: FileMetaData?, listener: DownloaderProgressListener?) {
        if (listener != null && listener is MetaDownloadProgressListener) {
            metadata?.let {
                listener.metaDataRetrieved(it)
            }
        }
    }

    @Throws(IOException::class)
    private fun saveCacheFile(`is`: InputStream, metadata: FileMetadataWithPath) {
        val f = File(cacheStorage(), metadata.id)
        if (!f.exists()) {
            f.createNewFile()
        }

        metadata.path = f.absolutePath
        val closer = Closer.create()
        try {
            val stream = closer.register(FileOutputStream(f))
            FileUtils.copyStreams(`is`, stream)
        } catch (e: Throwable) {
            throw closer.rethrow(e)
        } finally {
            closer.close()
        }

        cache.save(metadata)

    }

    fun setStoreType(storeType: StoreType) {
        this.storeType = storeType
    }

    class FileMetadataWithPath : FileMetaData() {
        @Key(CACHE_FILE_PATH)
        override var path: String? = null
    }

    private fun setDownloader(downloader: MediaHttpDownloader) {
        this.downloader = downloader
    }

    private fun setUploader(uploader: MediaHttpUploader) {
        this.uploader = uploader
    }

    companion object {

        private val CACHE_FILE_PATH = "KinveyCachePath"
    }
}
