/*
 * Copyright (c) 2013 Kinvey Inc.
 * Copyright (c) 2012 Google Inc.
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

/*
 * Copyright (c) 2013 Kinvey Inc.
 */
package com.kinvey.java.core;


import com.kinvey.java.model.FileMetaData;

import java.io.IOException;


/**
 * An interface for receiving progress notifications for uploads.
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
  public static class MyUploadProgressListener implements UploaderProgressListener {

    public void progressChanged(MediaHttpUploader uploader) throws IOException {
      switch (uploader.getUploadState()) {
        case INITIATION_STARTED:
          System.out.println("Initiation Started");
          break;
        case INITIATION_COMPLETE:
          System.out.println("Initiation Completed");
          break;
        case DOWNLOAD_IN_PROGRESS:
          System.out.println("Upload in progress");
          System.out.println("Upload percentage: " + uploader.getProgress());
          break;
        case DOWNLOAD_COMPLETE:
          System.out.println("Upload Completed!");
          break;
      }
    }
  }
 * </pre>
 *
 */
public interface UploaderProgressListener extends KinveyClientCallback<Void> {

    /**
     * Called to notify that progress has been changed.
     * <p/>
     * <p>
     * This method is called once before and after the initiation request. For media uploads it is
     * called multiple times depending on how many chunks are uploaded. Once the upload completes it
     * is called one final time.
     * </p>
     * <p/>
     * <p>
     * The upload state can be queried by calling {@link MediaHttpUploader#getUploadState} and the
     * progress by calling {@link MediaHttpUploader#getProgress}.
     * </p>
     *
     * @param uploader Media HTTP uploader
     */
    public void progressChanged(MediaHttpUploader uploader) throws IOException;

    /**
     * Called to notify that metadata has been successfully uploaded to the /blob/
     * <p/>
     * <p>
     * This method is called once, before the file upload actually begins but after the metadata has been set in the
     * blob collection.  This metadata is used by the File API to determine the upload URL, and contains the id of the file.
     * </p>
     *
     * @param metaData - The File MetaData associated wtih the upload about to occur.
     */
    public void metaDataUploaded(FileMetaData metaData);
}
