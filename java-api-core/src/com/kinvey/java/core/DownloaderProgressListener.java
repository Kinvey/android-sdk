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

import java.io.IOException;


/**
 * An interface for receiving progress notifications for downloads.
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
  public static class MyDownloadProgressListener implements DownloaderProgressListener {

    public void progressChanged(MediaHttpDownloader downloader) throws IOException {
      switch (downloader.getDownloadState()) {
        case DOWNLOAD_IN_PROGRESS:
          System.out.println("Download in progress");
          System.out.println("Download percentage: " + downloader.getProgress());
          break;
        case DOWNLOAD_COMPLETE:
          System.out.println("Download Completed!");
          break;
      }
    }

  }
 * </pre>
 *
 */
public interface DownloaderProgressListener extends KinveyClientCallback<Void> {

  /**
   * Called to notify that progress has been changed.
   *
   * <p>
   * This method is called multiple times depending on how many chunks are downloaded. Once the
   * download completes it is called one final time.
   * </p>
   *
   * <p>
   * The download state can be queried by calling {@link MediaHttpDownloader#getDownloadState} and
   * the progress by calling {@link MediaHttpDownloader#getProgress}.
   * </p>
   *
   * @param downloader Media HTTP downloader
   */
  public void progressChanged(MediaHttpDownloader downloader) throws IOException;
}
