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

import com.kinvey.android.async.AsyncRequest;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICacheManager;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.network.NetworkFileManager;
import com.kinvey.java.store.FileStore;
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
public class AsyncFileStore extends FileStore {




    private enum FileMethods{
        UPLOAD_FILE,
        UPLOAD_FILE_METADATA,
        UPLOAD_STREAM_METADATA,
        UPLOAD_STREAM_FILENAME,
        REMOVE_ID,
        DOWNLOAD_METADATA,
        DOWNLOAD_QUERY,
        DOWNLOAD_FILENAME
    }

    private static HashMap<FileMethods, Method> asyncMethods =
            new HashMap<FileMethods, Method>();

    static {
        try {
            //UPLOAD METHODS
            asyncMethods.put(FileMethods.UPLOAD_FILE,
                    FileStore.class.getDeclaredMethod("upload", File.class, UploaderProgressListener.class));
            asyncMethods.put(FileMethods.UPLOAD_FILE_METADATA,
                    FileStore.class.getDeclaredMethod("upload", File.class,
                            FileMetaData.class,
                            UploaderProgressListener.class));
            asyncMethods.put(FileMethods.UPLOAD_STREAM_METADATA,
                    FileStore.class.getDeclaredMethod("upload", InputStream.class,
                            FileMetaData.class,
                            UploaderProgressListener.class));
            asyncMethods.put(FileMethods.UPLOAD_STREAM_FILENAME,
                    FileStore.class.getDeclaredMethod("upload", String.class,
                            InputStream.class,
                            UploaderProgressListener.class));

            //REMOVE METHODS

            asyncMethods.put(FileMethods.REMOVE_ID,
                    FileStore.class.getDeclaredMethod("remove", FileMetaData.class));

            //DOWNLOAD METHODS
            asyncMethods.put(FileMethods.DOWNLOAD_FILENAME,
                    FileStore.class.getDeclaredMethod("download", String.class, String.class, DownloaderProgressListener.class));

            asyncMethods.put(FileMethods.DOWNLOAD_METADATA,
                    FileStore.class.getDeclaredMethod("download", FileMetaData.class, OutputStream.class, DownloaderProgressListener.class));
            asyncMethods.put(FileMethods.DOWNLOAD_QUERY,
                    FileStore.class.getDeclaredMethod("download", Query.class, String.class, DownloaderProgressListener.class));

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }



    public AsyncFileStore(NetworkFileManager networkFileManager,
                          ICacheManager cacheManager, Long ttl, StoreType storeType, String cacheFolder) {
        super(networkFileManager, cacheManager, ttl, storeType, cacheFolder);
    }

    public void upload(File file, KinveyClientCallback<FileMetaData> metaCallback, UploaderProgressListener listener) throws IOException {
        new AsyncRequest<FileMetaData>(this, asyncMethods.get(FileMethods.UPLOAD_FILE), metaCallback, file, listener )
                .execute();
    }

    public void upload(File file, FileMetaData metadata, KinveyClientCallback<FileMetaData> metaCallback,
                               UploaderProgressListener listener) throws IOException {

        new AsyncRequest<FileMetaData>(this, asyncMethods.get(FileMethods.UPLOAD_FILE_METADATA), metaCallback,
                file, metadata, listener ).execute();
    }

    public void upload(InputStream is, FileMetaData metadata, KinveyClientCallback<FileMetaData> metaCallback, UploaderProgressListener listener) throws IOException {
        new AsyncRequest<FileMetaData>(this, asyncMethods.get(FileMethods.UPLOAD_STREAM_METADATA), metaCallback,
                is, metadata, listener ).execute();
    }

    public void upload(String filename, InputStream is, KinveyClientCallback<FileMetaData> metaCallback,
                       UploaderProgressListener listener) throws IOException {
        new AsyncRequest<FileMetaData>(this, asyncMethods.get(FileMethods.UPLOAD_STREAM_FILENAME), metaCallback,
                filename, is, listener ).execute();
    }

    public void remove(FileMetaData metadata, KinveyDeleteCallback callback) throws IOException {
        new AsyncRequest<Integer>(this, asyncMethods.get(FileMethods.REMOVE_ID), callback,
                metadata ).execute();
    }

    public void download(FileMetaData metadata, OutputStream os, KinveyClientCallback<FileMetaData> metaCallback,
                         DownloaderProgressListener progressListener) throws IOException {
        new AsyncRequest<FileMetaData>(this, asyncMethods.get(FileMethods.DOWNLOAD_METADATA), metaCallback,
                metadata, os, progressListener ).execute();
    }

    public void download(Query q, String dst, KinveyClientCallback<FileMetaData> metaCallback,
                         DownloaderProgressListener progressListener) throws IOException {

        new AsyncRequest<FileMetaData>(this, asyncMethods.get(FileMethods.DOWNLOAD_QUERY), metaCallback,
                q, dst, progressListener ).execute();

    }

    public void download(String filename, String dst, KinveyClientCallback<FileMetaData> metaCallback,
                         DownloaderProgressListener progressListener) throws IOException {
        new AsyncRequest<FileMetaData>(this, asyncMethods.get(FileMethods.DOWNLOAD_FILENAME), metaCallback,
                filename, dst, progressListener ).execute();
    }

    public FileMetaData refresh(FileMetaData fileMetaData) throws IOException {
        return getFileMetadata(fileMetaData.getId());
    }

    public void clear() {
        cache.clear();
    }
}
