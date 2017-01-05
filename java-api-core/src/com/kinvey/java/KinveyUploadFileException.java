package com.kinvey.java;


import com.kinvey.java.model.FileMetaData;

public class KinveyUploadFileException extends KinveyException{

    private FileMetaData uploadedFileMetaData;

    public KinveyUploadFileException(String reason, String fix, String explanation, FileMetaData metadata) {
        super(reason, fix, explanation);
        this.uploadedFileMetaData = metadata;
    }

    public KinveyUploadFileException(String reason, FileMetaData metadata) {
        super(reason);
        this.uploadedFileMetaData = metadata;
    }

    public FileMetaData getUploadedFileMetaData() {
        return uploadedFileMetaData;
    }

    public void setUploadedFileMetaData(FileMetaData uploadedFileMetaData) {
        this.uploadedFileMetaData = uploadedFileMetaData;
    }
}
