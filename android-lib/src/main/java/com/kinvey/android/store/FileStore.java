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

package com.kinvey.android.store;

import com.kinvey.android.async.AsyncDownloadRequest;
import com.kinvey.android.async.AsyncRequest;
import com.kinvey.android.async.AsyncUploadRequest;
import com.kinvey.android.callback.AsyncDownloaderProgressListener;
import com.kinvey.android.callback.AsyncUploaderProgressListener;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICacheManager;
import com.kinvey.java.cache.KinveyCachedClientCallback;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.network.NetworkFileManager;
import com.kinvey.java.store.BaseFileStore;
import com.kinvey.java.store.StoreType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by Prots on 2/22/16.
 */
public class FileStore extends BaseFileStore {




    private enum FileMethods{
        UPLOAD_FILE,
        UPLOAD_FILE_METADATA,
        UPLOAD_STREAM_METADATA,
        UPLOAD_STREAM_FILENAME,
        REMOVE_ID,
        DOWNLOAD_METADATA,
        DOWNLOAD_QUERY,
        DOWNLOAD_FILENAME,
        REFRESH_FILE,
        FIND_QUERY
    }

    private static HashMap<FileMethods, Method> asyncMethods =
            new HashMap<FileMethods, Method>();

    static {
        try {
            //UPLOAD METHODS
            asyncMethods.put(FileMethods.UPLOAD_FILE,
                    BaseFileStore.class.getDeclaredMethod("upload", File.class, UploaderProgressListener.class));
            asyncMethods.put(FileMethods.UPLOAD_FILE_METADATA,
                    BaseFileStore.class.getDeclaredMethod("upload", File.class,
                            FileMetaData.class,
                            UploaderProgressListener.class));
            asyncMethods.put(FileMethods.UPLOAD_STREAM_METADATA,
                    BaseFileStore.class.getDeclaredMethod("upload", InputStream.class,
                            FileMetaData.class,
                            UploaderProgressListener.class));
            asyncMethods.put(FileMethods.UPLOAD_STREAM_FILENAME,
                    BaseFileStore.class.getDeclaredMethod("upload", String.class,
                            InputStream.class,
                            UploaderProgressListener.class));

            //DELETE METHODS

            asyncMethods.put(FileMethods.REMOVE_ID,
                    BaseFileStore.class.getDeclaredMethod("remove", FileMetaData.class));

            //DOWNLOAD METHODS
            asyncMethods.put(FileMethods.DOWNLOAD_FILENAME,
                    BaseFileStore.class.getDeclaredMethod("download", String.class, String.class, KinveyCachedClientCallback.class, DownloaderProgressListener.class));

            asyncMethods.put(FileMethods.DOWNLOAD_METADATA,
                    BaseFileStore.class.getDeclaredMethod("download", FileMetaData.class, OutputStream.class, KinveyCachedClientCallback.class, DownloaderProgressListener.class));
            asyncMethods.put(FileMethods.DOWNLOAD_QUERY,
                    BaseFileStore.class.getDeclaredMethod("download", Query.class, String.class, KinveyCachedClientCallback.class, DownloaderProgressListener.class));

            //REFRESH
            asyncMethods.put(FileMethods.REFRESH_FILE,
                    BaseFileStore.class.getDeclaredMethod("refresh", FileMetaData.class, KinveyCachedClientCallback.class));

            //FIND
            asyncMethods.put(FileMethods.FIND_QUERY,
                    BaseFileStore.class.getDeclaredMethod("find", Query.class, KinveyCachedClientCallback.class));

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }



    public FileStore(NetworkFileManager networkFileManager,
                          ICacheManager cacheManager, Long ttl, StoreType storeType, String cacheFolder) {
        super(networkFileManager, cacheManager, ttl, storeType, cacheFolder);
    }

    public void upload(File file, final AsyncUploaderProgressListener<FileMetaData> listener) throws IOException, KinveyException {
        new AsyncUploadRequest<FileMetaData>(this, asyncMethods.get(FileMethods.UPLOAD_FILE), listener, file).execute();
    }

    public void upload(File file, FileMetaData metadata,
                            AsyncUploaderProgressListener<FileMetaData> listener) throws IOException {

        new AsyncUploadRequest<FileMetaData>(this, asyncMethods.get(FileMethods.UPLOAD_FILE_METADATA), listener,
                file, metadata).execute();
    }

    public void upload(InputStream is, FileMetaData metadata, AsyncUploaderProgressListener<FileMetaData> listener) throws IOException {
        new AsyncUploadRequest<FileMetaData>(this, asyncMethods.get(FileMethods.UPLOAD_STREAM_METADATA), listener,
                is, metadata).execute();
    }

    public void upload(String filename, InputStream is,
                            AsyncUploaderProgressListener<FileMetaData> listener) throws IOException {
        new AsyncUploadRequest<FileMetaData>(this, asyncMethods.get(FileMethods.UPLOAD_STREAM_FILENAME), listener,
                filename, is).execute();
    }

    public void remove(FileMetaData metadata, KinveyDeleteCallback callback) throws IOException {
        new AsyncRequest<Integer>(this, asyncMethods.get(FileMethods.REMOVE_ID), callback,
                metadata).execute();
    }

    public void download(FileMetaData metadata, OutputStream os,
                              AsyncDownloaderProgressListener<FileMetaData> progressListener, KinveyCachedClientCallback<FileMetaData> cachedClientCallback) throws IOException {
        new AsyncDownloadRequest<FileMetaData>(this, asyncMethods.get(FileMethods.DOWNLOAD_METADATA), progressListener,
                metadata, os, cachedClientCallback).execute();
    }

    public void download(Query q, String dst,
                              AsyncDownloaderProgressListener<FileMetaData[]> progressListener, KinveyCachedClientCallback<FileMetaData[]> cachedClientCallback) throws IOException {
        new AsyncDownloadRequest<FileMetaData[]>(this, asyncMethods.get(FileMethods.DOWNLOAD_QUERY), progressListener,
                q, dst, cachedClientCallback).execute();

    }

    public void download(String filename, String dst,
                              AsyncDownloaderProgressListener<FileMetaData[]> progressListener, KinveyCachedClientCallback<FileMetaData[]> cachedClientCallback) throws IOException {
        new AsyncDownloadRequest<FileMetaData[]>(this, asyncMethods.get(FileMethods.DOWNLOAD_FILENAME), progressListener,
                filename, dst, cachedClientCallback).execute();
    }

    public void refresh(FileMetaData metadata,  KinveyClientCallback<FileMetaData> metaCallback, KinveyCachedClientCallback<FileMetaData> cachedClientCallback) throws IOException {
        new AsyncRequest<FileMetaData>(this, asyncMethods.get(FileMethods.REFRESH_FILE), metaCallback, metadata, cachedClientCallback).execute();
    }

    public void find(Query q, KinveyClientCallback<FileMetaData[]> metaCallback, KinveyCachedClientCallback<FileMetaData[]> cachedClientCallback) {
        new AsyncRequest<FileMetaData[]>(this, asyncMethods.get(FileMethods.FIND_QUERY), metaCallback, q, cachedClientCallback).execute();
    }

    public FileMetaData cachedFile(String fileId) {
        return cache.get(fileId);
    }

    public FileMetaData cachedFile(FileMetaData fileMetaData) {
        if (fileMetaData.getId() == null) {
            //"File.fileId is required"
            return null;
        }
        return cache.get(fileMetaData.getId());
    }

    public void clearCache() {
        cache.clear();
    }
}
