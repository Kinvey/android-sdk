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
 * An interface for receiving progress notifications for downloads.
 *
 *
 *
 * Sample usage:
 *
 *
 * <pre>
 * public static class MyDownloadProgressListener implements DownloaderProgressListener {
 *
 * public void progressChanged(MediaHttpDownloader downloader) throws IOException {
 * switch (downloader.getDownloadState()) {
 * case DOWNLOAD_IN_PROGRESS:
 * //Download in progress
 * //Download percentage:  + downloader.getProgress()
 * break;
 * case DOWNLOAD_COMPLETE:
 * //Download Completed!
 * break;
 * }
 * }
 *
 * }
</pre> *
 *
 */
interface DownloaderProgressListener {

    /**
     * Called to notify that progress has been changed.
     *
     *
     *
     * This method is called multiple times depending on how many chunks are downloaded. Once the
     * download completes it is called one final time.
     *
     *
     *
     *
     * The download state can be queried by calling [MediaHttpDownloader.getDownloadState] and
     * the progress by calling [MediaHttpDownloader.getProgress].
     *
     *
     * @param downloader Media HTTP downloader
     */
    @Throws(IOException::class)
    fun progressChanged(downloader: MediaHttpDownloader?)
}
