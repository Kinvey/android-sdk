package com.kinvey.java.store;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.util.Key;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MetaDownloadProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.network.NetworkFileManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class FileStore {


    private final NetworkFileManager networkFileManager;
    private final ICache<FileMetaData> cache;
    private StoreType storeType;

    public FileStore(NetworkFileManager networkFileManager, ICache<FileMetaData> cache, StoreType storeType){

        this.networkFileManager = networkFileManager;
        this.cache = cache;
        this.storeType = storeType;
    }

    public FileMetaData upload(File file) throws IOException {
        FileMetaData fm = new FileMetaData();
        fm.setFileName(file.getName());
        NetworkFileManager.UploadMetadataAndFile upload = networkFileManager.prepUploadBlocking(fm, new FileContent(null, file));
        return upload.execute();
    };
    public FileMetaData upload(File file, FileMetaData metadata) throws IOException {
        NetworkFileManager.UploadMetadataAndFile upload = networkFileManager.prepUploadBlocking(metadata, new FileContent(null, file));
        return upload.execute();
    };


    public FileMetaData upload(InputStream is, FileMetaData metadata) throws IOException {
        NetworkFileManager.UploadMetadataAndFile upload =
                networkFileManager.prepUploadBlocking(metadata, new InputStreamContent(null, is));

        return upload.execute();
    };

    public FileMetaData upload(String filename, InputStream is) throws IOException {
        FileMetaData fm = new FileMetaData();
        fm.setFileName(filename);
        NetworkFileManager.UploadMetadataAndFile upload =
                networkFileManager.prepUploadBlocking(fm, new InputStreamContent(null, is));
        return upload.execute();
    };

    public Integer delete(String id) throws IOException {
        return networkFileManager.deleteBlocking(id).execute().getCount();
    };


    public FileMetaData getFileMetadata(String id) throws IOException {
        NetworkFileManager.DownloadMetadata download = networkFileManager.downloadMetaDataBlocking(id);
        return download.execute();
    };

    public void download(FileMetaData metadata, OutputStream os, DownloaderProgressListener progressListener) throws IOException {
        NetworkFileManager.DownloadMetadataAndFile download = networkFileManager.prepDownloadBlocking(metadata);
        FileMetaData resultMetadata = download.execute();

        if (progressListener != null && (progressListener instanceof MetaDownloadProgressListener)) {
            ((MetaDownloadProgressListener)progressListener).metaDataRetrieved(resultMetadata);
        }
    };

    public void download(Query q, String dst, DownloaderProgressListener progressListener) throws IOException {
        NetworkFileManager.DownloadMetadataAndFileQuery download = networkFileManager.prepDownloadBlocking(q);
        FileMetaData[] resultMetadata = download.execute();
        downloadFromServer(resultMetadata, dst, progressListener);

    };

    public void download(String filename, String dst, DownloaderProgressListener progressListener) throws IOException {
        NetworkFileManager.DownloadMetadataAndFileQuery download = networkFileManager.prepDownloadBlocking(filename);
        FileMetaData[] resultMetadata = download.execute();

        downloadFromServer(resultMetadata, dst, progressListener);
    };

    public void download(String id, Query q , String dst, DownloaderProgressListener progressListener) throws IOException {
        NetworkFileManager.DownloadMetadataAndFileQuery download = networkFileManager.prepDownloadBlocking(id, q);
        FileMetaData[] resultMetadata = download.execute();
        downloadFromServer(resultMetadata, dst, progressListener);
    };



    private void downloadFromServer(FileMetaData metadata, OutputStream os, DownloaderProgressListener listener) throws IOException {
        AbstractClient client = networkFileManager.getClient();
        MediaHttpDownloader downloader = new MediaHttpDownloader(client.getRequestFactory().getTransport(),
                client.getRequestFactory().getInitializer());
        downloader.setProgressListener(listener);
        downloader.download(metadata, os);
    }

    private void downloadFromServer(FileMetaData[] metadata, String dst, DownloaderProgressListener listener) throws IOException {
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
            downloadFromServer(meta, new FileOutputStream(out), listener);
        }
    }

    private static class FileMetadataWithPath extends FileMetaData{
        @Key("KinveyCachePath")
        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }




}
