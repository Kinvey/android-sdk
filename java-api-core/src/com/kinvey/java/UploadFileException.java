package com.kinvey.java;


import com.kinvey.java.model.FileMetaData;

public class UploadFileException extends KinveyException{

    private FileMetaData uploadedFileMetaData;

    public UploadFileException(String reason, String fix, String explanation) {
        super(reason, fix, explanation);
    }

    public UploadFileException(String reason) {
        super(reason);
    }

    public FileMetaData getUploadedFileMetaData() {
        return uploadedFileMetaData;
    }

    public void setUploadedFileMetaData(FileMetaData uploadedFileMetaData) {
        this.uploadedFileMetaData = uploadedFileMetaData;
    }
}
