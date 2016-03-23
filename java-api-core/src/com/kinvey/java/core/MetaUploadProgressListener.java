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

package com.kinvey.java.core;

import com.kinvey.java.Logger;
import com.kinvey.java.model.FileMetaData;

/**
 * This class is an extension of the {@link DownloaderProgressListener}, which also provides a method for the retrieval of the metadata.
 *
 *
 * @author edwardf
 */
public abstract class MetaUploadProgressListener implements UploaderProgressListener{

    private FileMetaData metadata;
    public MetaUploadProgressListener(){}


    /**
     * Called when metadata has been successfully retrieved from blob, this will occur before the actual file is downloaded.
     *
     * @param meta the metadata object of the file being downloaded.
     */
    public void metaDataRetrieved(FileMetaData meta){
//        this.metadata = meta;
        this.metadata = new FileMetaData();
        this.metadata.setFileName(new String(meta.getFileName()));
        this.metadata.setId(new String(meta.getId()));
        Logger.INFO("cache meta: " + this.metadata.getFileName() + " and id: " + this.metadata.getId());
    }

    /**
     * Get the {@link FileMetaData} object associated with this download
     *
     * @return the FileMetaData object
     */
    public FileMetaData getMetadata(){
        return metadata;
    }


}
