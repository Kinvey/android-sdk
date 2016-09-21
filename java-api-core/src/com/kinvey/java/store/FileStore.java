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
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MetaDownloadProgressListener;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.network.NetworkFileManager;
import com.kinvey.java.query.MongoQueryFilter;
import com.kinvey.java.store.file.FileUtils;

import java.util.List;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class FileStore {

    private static final String CACHE_FILE_PATH = "KinveyCachePath";

    private final NetworkFileManager networkFileManager;
    private final ICacheManager cacheManager;
    private final Long ttl;
    private final String cacheFolder;
    protected final ICache<FileMetadataWithPath> cache;
    private StoreType storeType;

    public FileStore(NetworkFileManager networkFileManager, ICacheManager cacheManager, Long ttl, StoreType storeType, String cacheFolder){

        this.networkFileManager = networkFileManager;
        this.cacheManager = cacheManager;
        this.ttl = ttl;
        this.cacheFolder = cacheFolder;
        this.cache = cacheManager.getCache("__KinveyFile__", FileMetadataWithPath.class, ttl);
        this.storeType = storeType;
    }

    public FileMetaData upload(File file, UploaderProgressListener listener) throws IOException {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(listener);
        FileMetaData fm = new FileMetaData();
        fm.setFileName(file.getName());
        return upload(file, fm, listener);
    };




    public FileMetaData upload(File file, FileMetaData metadata,
                               UploaderProgressListener listener) throws IOException {
        Preconditions.checkNotNull(file, "file must not be null");
        Preconditions.checkNotNull(metadata, "metadata must not be null");
        Preconditions.checkNotNull(listener, "listener must not be null");
        NetworkFileManager.UploadMetadataAndFile upload = networkFileManager.prepUploadBlocking(metadata,
                new FileContent(null, file), listener);

        FileMetadataWithPath fileMetadataWithPath = new FileMetadataWithPath();
        fileMetadataWithPath.putAll(metadata);

        if (fileMetadataWithPath.getId() == null){
            fileMetadataWithPath.setId(UUID.randomUUID().toString());
        }



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

    public FileMetaData upload(InputStream is, FileMetaData metadata, UploaderProgressListener listener) throws IOException {
        Preconditions.checkNotNull(is, "inputStream must not be null");
        Preconditions.checkNotNull(metadata, "metadata must not be null");
        Preconditions.checkNotNull(listener, "listener must not be null");
        NetworkFileManager.UploadMetadataAndFile upload =
                networkFileManager.prepUploadBlocking(metadata, new InputStreamContent(null, is), listener);

        return upload.execute();
    };

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

    public Integer remove(FileMetaData metadata) throws IOException {
        Preconditions.checkNotNull(metadata, "metadata must not be null");
        switch (storeType.writePolicy){
            case FORCE_LOCAL:
                cache.delete(metadata.getId());
                return 1;
            case LOCAL_THEN_NETWORK:
                cache.delete(metadata.getId());
            case FORCE_NETWORK:
                return networkFileManager.deleteBlocking(metadata.getId()).execute().getCount();
            //TODO check return default value
            default:
                return 0;
        }
    }


    public FileMetaData[] find(Query q) throws IOException {
        Preconditions.checkNotNull(q, "query must not be null");
        NetworkFileManager.DownloadMetadataQuery download = networkFileManager.prepDownloadBlocking(q);
        FileMetaData[] metaData = null;
        switch (storeType.readPolicy){
            case FORCE_LOCAL:
                List<FileMetadataWithPath> list = cache.get(q);
                metaData = new FileMetaData[list.size()];
                list.toArray(metaData);
                break;
            case PREFER_LOCAL:
                List<FileMetadataWithPath> listPreferLocal = cache.get(q);
                metaData = new FileMetaData[listPreferLocal.size()];
                listPreferLocal.toArray(metaData);
                if (metaData.length == 0) {
                    metaData = download.execute();
                }
                break;
            case FORCE_NETWORK:
                metaData = download.execute();
                break;
            case PREFER_NETWORK:
                metaData = download.execute();
                if (metaData == null || metaData.length == 0) {
                    List<FileMetadataWithPath> listPreferNetwork = cache.get(q);
                    metaData = new FileMetaData[listPreferNetwork.size()];
                    listPreferNetwork.toArray(metaData);
                }
                break;

        }

        return metaData;


    };

    public FileMetaData find(String id) throws IOException {
        Preconditions.checkNotNull(id, "id must not be null");
        NetworkFileManager.DownloadMetadata download = networkFileManager.downloadMetaDataBlocking(id);
        FileMetaData metaData = null;
        switch (storeType.readPolicy){
            case FORCE_LOCAL:
                metaData = cache.get(id);
                break;
            case PREFER_LOCAL:
                metaData = cache.get(id);
                if (metaData == null) {
                    metaData = download.execute();
                }
                break;
            case FORCE_NETWORK:
                metaData = download.execute();
                break;
            case PREFER_NETWORK:
                metaData = download.execute();
                if (metaData == null) {
                    metaData = cache.get(id);
                }
                break;
        }

        return metaData;


    };

    public FileMetaData refresh(FileMetaData fileMetaData) throws IOException {
        Preconditions.checkNotNull(fileMetaData, "metadata must not be null");
        return find(fileMetaData.getId());
    }



    public void download(FileMetaData metadata, OutputStream os, DownloaderProgressListener progressListener) throws IOException {
        Preconditions.checkNotNull(metadata, "metadata must not be null");
        Preconditions.checkNotNull(metadata.getId(), "metadata.getId must not be null");
        Preconditions.checkNotNull(progressListener, "listener must not be null");
        FileMetaData resultMetadata = find(metadata.getId());

        if (resultMetadata == null){
            progressListener.onFailure(new KinveyException("FileMetadataMissing", "Missing FileMetaData in cache", ""));
        } else {
            sendMetadata(resultMetadata, progressListener);
            getFile(resultMetadata, os, progressListener);
        }

    };

    public void download(Query q, String dst, DownloaderProgressListener progressListener) throws IOException {
        Preconditions.checkNotNull(q, "query must not be null");
        Preconditions.checkNotNull(dst, "dst must not be null");
        Preconditions.checkNotNull(progressListener, "listener must not be null");
        FileMetaData[] resultMetadata = find(q);
        if (resultMetadata == null || resultMetadata.length == 0){
            progressListener.onFailure(new KinveyException("FileMetadataMissing", "Missing FileMetaData in cache", ""));
        } else {

            for (FileMetaData meta : resultMetadata) {
                getFile(meta, new FileOutputStream(new File(cacheStorage(), meta.getId())), progressListener);
                getNetworkFile(resultMetadata, dst, progressListener);
            }
        }

    };

    public void download(Query q, OutputStream dst, DownloaderProgressListener progressListener) throws IOException {
        Preconditions.checkNotNull(q, "query must not be null");
        Preconditions.checkNotNull(dst, "dst must not be null");
        Preconditions.checkNotNull(progressListener, "listener must not be null");
        FileMetaData[] resultMetadata = find(q);

        if (resultMetadata == null || resultMetadata.length == 0){
            progressListener.onFailure(new KinveyException("FileMetadataMissing", "Missing FileMetaData in cache", ""));
        } else {
            getFile(resultMetadata[0], dst, progressListener);
        }

    };

    public void download(String filename, String dst, DownloaderProgressListener progressListener) throws IOException {
        Preconditions.checkNotNull(filename, "filename must not be null");
        Preconditions.checkNotNull(dst, "dst must not be null");
        Preconditions.checkNotNull(progressListener, "listener must not be null");
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.equals("_filename", filename);
        download(q, dst, progressListener);
    };

    public void download(String filename, OutputStream dst, DownloaderProgressListener progressListener) throws IOException {
        Preconditions.checkNotNull(filename, "filename must not be null");
        Preconditions.checkNotNull(dst, "dst must not be null");
        Preconditions.checkNotNull(progressListener, "listener must not be null");
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.equals("_filename", filename);
        download(q, dst, progressListener);
    };

    private File cacheStorage(){
        File f = new File(getCacheFolder());
        if (!f.exists()){
            f.mkdirs();
        } else if(!f.isDirectory()){
            throw new KinveyException("InvalidCachedFolder", "file with name already exists", "");
        }
        return f;
    }


    private void getNetworkFile(FileMetaData metadata, OutputStream os, DownloaderProgressListener listener) throws IOException {
        AbstractClient client = networkFileManager.getClient();
        MediaHttpDownloader downloader = new MediaHttpDownloader(client.getRequestFactory().getTransport(),
                client.getRequestFactory().getInitializer());
        downloader.setProgressListener(listener);
        downloader.download(metadata, os);
    }

    private void getNetworkFile(FileMetaData[] metadata, String dst, DownloaderProgressListener listener) throws IOException {
        File f = new File(dst);
        if (!f.exists()){
            f.mkdirs();
        } else if (!f.isDirectory()){
            throw new KinveyException("dst is not a dirrectory", "Please provide valid path to file destination", "");
        }

        for (FileMetaData meta : metadata){
            File out = new File(f, meta.getId());
            if (!out.exists()){
                out.createNewFile();
            }
            getNetworkFile(meta, new FileOutputStream(out), listener);
        }
    }

    private File getCachedFile(FileMetaData metadata){
        File ret = null;
        if (metadata.containsKey(CACHE_FILE_PATH)){
            String cacheFilePath = metadata.get(CACHE_FILE_PATH).toString();
            ret = new File(cacheFilePath);

        }
        return ret == null || !ret.exists() ? null : ret ;
    }

    private void getFile(final FileMetaData metadata, final OutputStream os, final DownloaderProgressListener listener) throws IOException {
        File f = new File(cacheStorage(), metadata.getId());


        File cacheStorage = cacheStorage();

        switch (storeType.readPolicy){
            case FORCE_LOCAL:
                f = getCachedFile(metadata);
                if (f == null){
                    listener.onFailure(new KinveyException("FileMissing", "File Missing in cache", ""));
                } else {
                    FileUtils.copyStreams(new FileInputStream(f), os);
                    listener.onSuccess(metadata);
                }

                break;
            case FORCE_NETWORK:
                getNetworkFile(metadata, os, listener);
                break;
            case PREFER_LOCAL:
                f = getCachedFile(metadata);

                if (f == null){
                    f = new File(cacheStorage, metadata.getId());

                    getNetworkFile(metadata, new FileOutputStream(f), listener);
                }
                if (f.exists()) {
                    FileUtils.copyStreams(new FileInputStream(f), os);
                    listener.onSuccess(metadata);
                }
                break;
            case PREFER_NETWORK:

                DownloaderProgressListener wrappedListener = new DownloaderProgressListener() {
                    @Override
                    public void progressChanged(MediaHttpDownloader downloader) throws IOException {
                        listener.progressChanged(downloader);
                    }

                    @Override
                    public void onSuccess(FileMetaData result) {
                        listener.onSuccess(result);
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        File f = getCachedFile(metadata);
                        try {
                            if (f == null) {
                                listener.onFailure(new KinveyException("FileMissing", "File Missing in cache", ""));
                            } else {
                                FileUtils.copyStreams(new FileInputStream(f), os);
                                listener.onSuccess(null);
                            }
                        } catch (IOException e){
                            listener.onFailure(e);
                        }
                    }
                };

                getNetworkFile(metadata, new FileOutputStream(f), wrappedListener);

                FileMetadataWithPath fileMetadataWithPath = new FileMetadataWithPath();
                fileMetadataWithPath.putAll(metadata);
                fileMetadataWithPath.setPath(f.getAbsolutePath());

                cache.save(fileMetadataWithPath);

                if (f.exists()){
                    FileUtils.copyStreams(new FileInputStream(f), os);
                    listener.onSuccess(null);
                }

                listener.onSuccess(null);

                break;
        }
    }

    private void sendMetadata(FileMetaData metadata, DownloaderProgressListener listener){
        if (listener != null && (listener instanceof MetaDownloadProgressListener)) {
            ((MetaDownloadProgressListener)listener).metaDataRetrieved(metadata);
        }
    }

    private void saveCacheFile(InputStream is, FileMetadataWithPath metadata) throws IOException {
        File f = new File(cacheStorage(), metadata.getId());
        if (!f.exists()){
            f.createNewFile();
        }

        metadata.setPath(f.getAbsolutePath());

        FileUtils.copyStreams(is, new FileOutputStream(f));

        cache.save(metadata);

    }

    public void setStoreType(StoreType storeType) {
        this.storeType = storeType;
    }

    protected String getCacheFolder(){
        return cacheFolder;
    };

    public static class FileMetadataWithPath extends FileMetaData{
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
