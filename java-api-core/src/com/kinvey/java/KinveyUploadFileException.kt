package com.kinvey.java


import com.kinvey.java.model.FileMetaData

class KinveyUploadFileException : KinveyException {

    var uploadedFileMetaData: FileMetaData? = null

    constructor(reason: String?, fix: String?, explanation: String?, metadata: FileMetaData?) : super(reason, fix, explanation) {
        this.uploadedFileMetaData = metadata
    }

    constructor(reason: String, metadata: FileMetaData) : super(reason) {
        this.uploadedFileMetaData = metadata
    }
}
