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
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.network.NetworkFileManager;
import com.kinvey.java.store.file.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


abstract public class FileStore {

    private static final String CACHE_FILE_PATH = "KinveyCachePath";

    private final NetworkFileManager networkFileManager;
    private final ICache<FileMetadataWithPath> cache;
    private StoreType storeType;

    public FileStore(NetworkFileManager networkFileManager, ICacheManager cacheManager, Long ttl, StoreType storeType){

        this.networkFileManager = networkFileManager;
        this.cache = cacheManager.getCache("__KinveyFile__", FileMetadataWithPath.class, ttl);
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
        Preconditions.checkNotNull(metadata.getId());
        FileMetaData resultMetadata = null;
        NetworkFileManager.DownloadMetadataAndFile download = networkFileManager.prepDownloadBlocking(metadata);
        File file = null;
        switch (storeType.readPolicy){
            case FORCE_LOCAL:
                resultMetadata = cache.get(metadata.getId());
                file = null;
                if (resultMetadata != null){
                    file = getCachedFile(metadata);
                }
                if (file == null || resultMetadata == null){
                    progressListener.onFailure(
                            new KinveyException("FileNotFound", "Kinvey file with such id does not exist", "Make sure that you request existing file")
                    );
                } else {
                    FileUtils.copyStreams(new FileInputStream(file), os);
                    progressListener.onSuccess(null);
                }

                break;
            case FORCE_NETWORK:
                resultMetadata = download.execute();
                if (resultMetadata != null){
                    downloadFromServer(resultMetadata, os, progressListener);
                }
                break;
            case PREFER_LOCAL:
                resultMetadata = cache.get(metadata.getId());
                if (resultMetadata == null){
                    resultMetadata = download.execute();
                }



                break;
            case PREFER_NETWORK:
                try {
                    resultMetadata = download.execute();
                } catch (IOException e){

                }
                if (resultMetadata == null){
                    resultMetadata = cache.get(metadata.getId());
                }

                break;
        }


        if (resultMetadata == null){
            throw new KinveyException("FileNotFound", "Kinvey file with such id does not exist", "Make sure that you request existing file");
        }

        if (progressListener != null && (progressListener instanceof MetaDownloadProgressListener)) {
            ((MetaDownloadProgressListener)progressListener).metaDataRetrieved(resultMetadata);
        }

        File cacheFile = null;

        if (resultMetadata.containsKey(CACHE_FILE_PATH)){
            String cacheFilePath = resultMetadata.get(CACHE_FILE_PATH).toString();
            cacheFile = new File(cacheFilePath);
        }

        if (cacheFile == null || !cacheFile.exists()){

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

    private File getCachedFile(FileMetaData metadata){
        File ret = null;
        if (metadata.containsKey(CACHE_FILE_PATH)){
            String cacheFilePath = metadata.get(CACHE_FILE_PATH).toString();
            ret = new File(cacheFilePath);

        }
        return ret == null || !ret.exists() ? null : ret ;
    }



    private static class FileMetadataWithPath extends FileMetaData{
        @Key(CACHE_FILE_PATH)
        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    protected abstract String getCacheFolder();

}
