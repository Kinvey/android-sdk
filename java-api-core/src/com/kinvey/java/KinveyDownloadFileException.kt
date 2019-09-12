package com.kinvey.java


import com.kinvey.java.model.FileMetaData

class KinveyDownloadFileException : KinveyException {

    var downloadedFileMetaData: FileMetaData? = null

    constructor(reason: String, fix: String, explanation: String) : super(reason, fix, explanation)

    constructor(reason: String) : super(reason)
}
