/*
 * Copyright (c) 2013 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.java.core;

import com.kinvey.java.model.FileMetaData;

/**
 * This class is an extension of the {@link DownloaderProgressListener}, which also provides a method for the retrieval of the metadata.
 *
 *
 * @author edwardf
 */
public abstract class MetaDownloadProgressListener implements DownloaderProgressListener {


    private FileMetaData metadata;
    public MetaDownloadProgressListener(){}


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
        System.out.println("cache meta: " + this.metadata.getFileName() + " and id: " + this.metadata.getId());
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
