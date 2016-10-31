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

package com.kinvey.java.store;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.cache.ICacheManager;
import com.kinvey.java.cache.KinveyCachedClientCallback;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MetaDownloadProgressListener;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.network.NetworkFileManager;
import com.kinvey.java.store.file.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;


public class BaseFileStore {

    private static final String CACHE_FILE_PATH = "KinveyCachePath";

    private final NetworkFileManager networkFileManager;
    private final ICacheManager cacheManager;
    private final Long ttl;
    private final String cacheFolder;
    protected final ICache<FileMetadataWithPath> cache;
    private StoreType storeType;

    /**
     * File store constructor
     * @param networkFileManager manager for the network file requests
     * @param cacheManager manager for local cache file request
     * @param ttl Time to Live for cached files
     * @param storeType Store type to be used
     * @param cacheFolder local cache folder to be used on device
     */
    public BaseFileStore(NetworkFileManager networkFileManager, ICacheManager cacheManager, Long ttl, StoreType storeType, String cacheFolder){
        this.networkFileManager = networkFileManager;
        this.cacheManager = cacheManager;
        this.ttl = ttl;
        this.cacheFolder = cacheFolder;
        this.cache = cacheManager.getCache("__KinveyFile__", FileMetadataWithPath.class, ttl);
        this.storeType = storeType;
    }

    /**
     * Uploading file using specified StoreType
     * @param file File to upload
     * @param listener progress listener to be used to track file upload progress
     * @return FileMetadata object that contains usefull informaton about file
     * @throws IOException
     */
    public FileMetaData upload(File file, UploaderProgressListener listener) throws IOException {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(listener);
        FileMetaData fm = new FileMetaData();
        fm.setFileName(file.getName());
        return upload(file, fm, listener);
    };


    /**
     * Uploading file with specific metadata, where you can define additional fields to be stored along the file
     * @param file File to be uploaded
     * @param metadata additional metadata to be stored
     * @param listener progress listener to be used to track file upload progress
     * @return FileMetadata object that contains usefull informaton about file
     * @throws IOException
     */
    public FileMetaData upload(File file, FileMetaData metadata,
                               UploaderProgressListener listener) throws IOException {
        Preconditions.checkNotNull(file, "file must not be null");
        Preconditions.checkNotNull(metadata, "metadata must not be null");
        Preconditions.checkNotNull(listener, "listener must not be null");

        FileMetadataWithPath fileMetadataWithPath = new FileMetadataWithPath();
        fileMetadataWithPath.putAll(metadata);

        if (fileMetadataWithPath.getId() == null){
            fileMetadataWithPath.setId(UUID.randomUUID().toString());
        }

        NetworkFileManager.UploadMetadataAndFile upload =
                networkFileManager.prepUploadBlocking(fileMetadataWithPath,
                        new FileContent(fileMetadataWithPath.getMimetype(), file), listener);

        switch (storeType.writePolicy){
            case FORCE_LOCAL:
                saveCacheFile(new FileInputStream(file), fileMetadataWithPath);
                metadata = fileMetadataWithPath;
                break;
            case FORCE_NETWORK:
                metadata = upload.execute();
                break;
            case LOCAL_THEN_NETWORK:
                saveCacheFile(new FileInputStream(file), fileMetadataWithPath);
                try {
                    upload.execute();
                } catch (Exception e){
                    e.printStackTrace();
                }
                metadata = fileMetadataWithPath;
        }

        return metadata;
    };

    /**
     * Upload specified stream with additional metadata to the store
     * @param is Input stream to be uploaded
     * @param metadata Matadata to be stored along with file
     * @param listener progress listener to be used to track file upload progress
     * @return FileMetadata object that contains usefull informaton about file
     * @throws IOException
     */
    public FileMetaData upload(InputStream is, FileMetaData metadata, UploaderProgressListener listener) throws IOException {
        Preconditions.checkNotNull(is, "inputStream must not be null");
        Preconditions.checkNotNull(metadata, "metadata must not be null");
        Preconditions.checkNotNull(listener, "listener must not be null");
        NetworkFileManager.UploadMetadataAndFile upload =
                networkFileManager.prepUploadBlocking(metadata, new InputStreamContent(null, is), listener);

        return upload.execute();
    };


    /**
     * Upload specified stream with specified file name to the store
     * @param is Input stream to be uploaded
     * @param filename will be stored as file name to saved stream
     * @param listener progress listener to be used to track file upload progress
     * @return FileMetadata object that contains usefull informaton about file
     * @throws IOException
     */
    public FileMetaData upload(String filename, InputStream is, UploaderProgressListener listener) throws IOException {
        Preconditions.checkNotNull(filename, "filename must not be null");
        Preconditions.checkNotNull(is, "inputStream must not be null");
        Preconditions.checkNotNull(listener, "listener must not be null");
        FileMetaData fm = new FileMetaData();
        fm.setFileName(filename);
        NetworkFileManager.UploadMetadataAndFile upload =
                networkFileManager.prepUploadBlocking(fm, new InputStreamContent(null, is), listener);
        return upload.execute();
    };


    /**
     * Remove file from the store
     * @param metadata of the file to be deleted, the id field of metadata is required
     * @return number of removed items 1 if file was removed and 0 if file was not found
     * @throws IOException
     */
    public Integer remove(FileMetaData metadata) throws IOException {
        Preconditions.checkNotNull(metadata.getId(), "metadata must not be null");
        switch (storeType.writePolicy) {
            case FORCE_LOCAL:
                cache.delete(metadata.getId());
                return 1;
            case LOCAL_THEN_NETWORK:
                cache.delete(metadata.getId());
            case FORCE_NETWORK:
                return networkFileManager.deleteBlocking(metadata.getId()).execute().getCount();
            default:
                return 0;
        }
    }

    /**
     * Query server for matching files
     * @param q query to be executed against Files store
     * @param cachedCallback - callback to be executed if StoreType.CACHE is used
     * @return Array of file metadata that matches the query
     * @throws IOException
     */
    public FileMetaData[] find(Query q,
                               KinveyCachedClientCallback<FileMetaData[]> cachedCallback) throws IOException {
        Preconditions.checkNotNull(q, "query must not be null");
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE");
        NetworkFileManager.DownloadMetadataQuery download = networkFileManager.prepDownloadBlocking(q);
        FileMetaData[] metaData = null;
        switch (storeType.readPolicy) {
            case FORCE_LOCAL:
                metaData = getFileMetaDataFromCache(q);
                break;
            case BOTH:
                if (cachedCallback != null) {
                    cachedCallback.onSuccess(getFileMetaDataFromCache(q));
                }
            case FORCE_NETWORK:
                metaData = download.execute();
                break;
        }
        return metaData;
    };

    /**
     * Query cache for matching files
     * @param q query to be executed against Cached store
     * @return Array of file metadata that matches the query
     * @throws IOException
     */
    private FileMetaData[] getFileMetaDataFromCache(Query q) {
        FileMetaData[] metaData = null;
        List<FileMetadataWithPath> list = cache.get(q);
        metaData = new FileMetaData[list.size()];
        list.toArray(metaData);
        return metaData;
    }

    /**
     * Query server for matching file
     * @param id id of file we are looking for
     * @param cachedCallback - callback to be executed if StoreType.CACHE is used
     * @return Array of file metadata that matches the query
     * @throws IOException
     */
    public FileMetaData find(String id,
                             KinveyCachedClientCallback<FileMetaData> cachedCallback) throws IOException {
        Preconditions.checkNotNull(id, "id must not be null");
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE");
        NetworkFileManager.DownloadMetadata download = networkFileManager.downloadMetaDataBlocking(id);
        FileMetaData metaData = null;
        switch (storeType.readPolicy) {
            case FORCE_LOCAL:
                metaData = cache.get(id);
                break;
            case BOTH:
                if (storeType == StoreType.CACHE && cachedCallback != null) {
                    metaData = cache.get(id);
                    cachedCallback.onSuccess(metaData);
                }
            case FORCE_NETWORK:
                metaData = download.execute();
                break;
        }

        return metaData;

    }

    /**
     * Refreshes metadata on local cache by grabbing latest info from server
     * @param fileMetaData metadata that we want to refresh
     * @param cachedCallback - callback to be executed if StoreType.CACHE is used
     * @return Array of file metadata that matches the query
     * @throws IOException
     */
    public FileMetaData refresh(FileMetaData fileMetaData,
                                KinveyCachedClientCallback<FileMetaData> cachedCallback) throws IOException {
        Preconditions.checkNotNull(fileMetaData, "metadata must not be null");
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE");
        return find(fileMetaData.getId(), cachedCallback);
    }

    /**
     * download file with given metadata to specified OutputStream
     * @param metadata metadata of the file we want to download
     * @param os OutputStream where file content should be streamed
     * @param cachedCallback - callback to be executed if StoreType.CACHE is used
     * @return metadata of the file we are downloading
     * @throws IOException
     */
    public FileMetaData download(FileMetaData metadata,
                                 OutputStream os,
                                 KinveyCachedClientCallback<FileMetaData> cachedCallback,
                                 DownloaderProgressListener progressListener) throws IOException {
        Preconditions.checkNotNull(metadata, "metadata must not be null");
        Preconditions.checkNotNull(metadata.getId(), "metadata.getId must not be null");
        Preconditions.checkNotNull(progressListener, "listener must not be null");
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE");
        FileMetaData resultMetadata = find(metadata.getId(), null);
            sendMetadata(resultMetadata, progressListener);
        return getFile(resultMetadata, os, storeType.readPolicy, progressListener, cachedCallback);
    }

    private File cacheStorage() {
        File f = new File(getCacheFolder());
        if (!f.exists()) {
            f.mkdirs();
        } else if (!f.isDirectory()) {
            throw new KinveyException("InvalidCachedFolder", "file with name already exists", "");
        }
        return f;
    }


    private FileMetaData getNetworkFile(FileMetaData metadata, OutputStream os, DownloaderProgressListener listener) throws IOException {
        AbstractClient client = networkFileManager.getClient();
        MediaHttpDownloader downloader = new MediaHttpDownloader(client.getRequestFactory().getTransport(),
                client.getRequestFactory().getInitializer());
        downloader.setProgressListener(listener);
        return downloader.download(metadata, os);
    }

    private FileMetaData getNetworkFile(FileMetaData metadata, String dst, DownloaderProgressListener listener) throws IOException {
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
    }

    private File getCachedFile(FileMetaData metadata) {
        Preconditions.checkNotNull(metadata, "metadata must not be null");
        File ret = null;
        if (metadata.containsKey(CACHE_FILE_PATH)) {
            String cacheFilePath = metadata.get(CACHE_FILE_PATH).toString();
            ret = new File(cacheFilePath);

        }
        return ret == null || !ret.exists() ? null : ret;
    }

    private FileMetaData getFile(final FileMetaData metadata,
                                 final OutputStream os,
                                 ReadPolicy readPolicy,
                                 final DownloaderProgressListener listener,
                                 KinveyCachedClientCallback<FileMetaData> cachedCallback) throws IOException {
        Preconditions.checkArgument(cachedCallback == null || readPolicy == ReadPolicy.BOTH, "KinveyCachedClientCallback can only be used with StoreType.CACHE");
        File f = new File(cacheStorage(), metadata.getId());

        File cacheStorage = cacheStorage();

        switch (readPolicy) {
            case FORCE_LOCAL:
                f = getCachedFile(metadata);
                if (f == null){
                    throw new KinveyException("FileMissing", "File Missing in cache", "");
                } else {
                    FileUtils.copyStreams(new FileInputStream(f), os);
                    return metadata;
                }
            case BOTH:
                    f = getCachedFile(metadata);
                    if (f == null) {
                        if (cachedCallback != null) {
                            cachedCallback.onFailure(new KinveyException("FileMissing", "File Missing in cache", ""));
                        }
                    } else {
                        FileUtils.copyStreams(new FileInputStream(f), os);
                        if (cachedCallback != null) {
                            cachedCallback.onSuccess(metadata);
                        }
                    }
            case FORCE_NETWORK:
                return getNetworkFile(metadata, os, listener);
        }
        return null;
    }

    private void sendMetadata(FileMetaData metadata, DownloaderProgressListener listener) {
        if (listener != null && (listener instanceof MetaDownloadProgressListener)) {
            ((MetaDownloadProgressListener) listener).metaDataRetrieved(metadata);
        }
    }

    private void saveCacheFile(InputStream is, FileMetadataWithPath metadata) throws IOException {
        File f = new File(cacheStorage(), metadata.getId());
        if (!f.exists()) {
            f.createNewFile();
        }

        metadata.setPath(f.getAbsolutePath());

        FileUtils.copyStreams(is, new FileOutputStream(f));

        cache.save(metadata);

    }

    public void setStoreType(StoreType storeType) {
        this.storeType = storeType;
    }

    protected String getCacheFolder() {
        return cacheFolder;
    }

    public static class FileMetadataWithPath extends FileMetaData {
        @Key(CACHE_FILE_PATH)
        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

}
