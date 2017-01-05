package com.kinvey.java;


import com.kinvey.java.model.FileMetaData;

public class KinveyDownloadFileException extends KinveyException{

    private FileMetaData downloadedFileMetaData;

    public KinveyDownloadFileException(String reason, String fix, String explanation) {
        super(reason, fix, explanation);
    }

    public KinveyDownloadFileException(String reason) {
        super(reason);
    }

    public FileMetaData getDownloadedFileMetaData() {
        return downloadedFileMetaData;
    }

    public void setDownloadedFileMetaData(FileMetaData downloadedFileMetaData) {
        this.downloadedFileMetaData = downloadedFileMetaData;
    }
}
