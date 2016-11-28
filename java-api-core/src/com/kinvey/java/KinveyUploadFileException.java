package com.kinvey.java;


import com.kinvey.java.model.FileMetaData;

public class KinveyUploadFileException extends KinveyException{

    private FileMetaData uploadedFileMetaData;

    public KinveyUploadFileException(String reason, String fix, String explanation) {
        super(reason, fix, explanation);
    }

    public KinveyUploadFileException(String reason) {
        super(reason);
    }

    public FileMetaData getUploadedFileMetaData() {
        return uploadedFileMetaData;
    }

    public void setUploadedFileMetaData(FileMetaData uploadedFileMetaData) {
        this.uploadedFileMetaData = uploadedFileMetaData;
    }
}
