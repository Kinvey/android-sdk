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

package com.kinvey.java.core


import com.kinvey.java.model.FileMetaData

import java.io.IOException


/**
 * An interface for receiving progress notifications for uploads.
 *
 *
 *
 * Sample usage:
 *
 *
 * <pre>
 * public static class MyUploadProgressListener implements UploaderProgressListener {
 *
 * public void progressChanged(MediaHttpUploader uploader) throws IOException {
 * switch (uploader.getUploadState()) {
 * case INITIATION_STARTED:
 * //Initiation Started
 * break;
 * case INITIATION_COMPLETE:
 * //Initiation Completed
 * break;
 * case DOWNLOAD_IN_PROGRESS:
 * //Upload in progress
 * //Upload percentage: + uploader.getProgress()
 * break;
 * case DOWNLOAD_COMPLETE:
 * //Upload Completed!
 * break;
 * }
 * }
 * }
</pre> *
 *
 */
interface UploaderProgressListener {

    /**
     * Called to notify that progress has been changed.
     *
     *
     *
     *
     * This method is called once before and after the initiation request. For media uploads it is
     * called multiple times depending on how many chunks are uploaded. Once the upload completes it
     * is called one final time.
     *
     *
     *
     *
     *
     * The upload state can be queried by calling [MediaHttpUploader.getUploadState] and the
     * progress by calling [MediaHttpUploader.getProgress].
     *
     *
     * @param uploader Media HTTP uploader
     */
    @Throws(IOException::class)
    fun progressChanged(uploader: MediaHttpUploader?)
}
