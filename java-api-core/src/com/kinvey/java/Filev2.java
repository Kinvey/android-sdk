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
package com.kinvey.java;


import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;

import java.io.IOException;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyHeaders;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.UriLocResponse;

/**
 Wraps the {@link com.kinvey.java.File} public methods in asynchronous functionality using native Android AsyncTask.
 *
 * <p>
 * This class is constructed via {@link com.kinvey.java.AbstractClient#file()} factory method.
 * </p>
 *
 * <p>
 * The callback mechanism for this api is extended to include the {@link UploaderProgressListener#progressChanged(com.kinvey.java.core.MediaHttpUploader)}
 * method, which receives notifications as the upload process transitions through and progresses with the upload.
 * process.
 * </p>
 *
 * <p>
 * Sample usage:
 * <pre>
 * {@code
 *    mKinveyClient.file().uploadBlocking(file,  new UploaderProgressListener() {
 *        @Override
 *        public void onSuccess(Void result) {
 *            Log.i(TAG, "successfully upload file");
 *        }
 *        @Override
 *        public void onFailure(Throwable error) {
 *            Log.e(TAG, "failed to upload file.", error);
 *        }
 *        @Override
 *        public void progressChanged(MediaHttpUploader uploader) throws IOException {
 *            Log.i(TAG, "upload progress: " + uploader.getUploadState());
 *            switch (uploader.getUploadState()) {
 *                case INITIATION_STARTED:
 *                    Log.i(TAG, "Initiation Started");
 *                    break;
 *                case INITIATION_COMPLETE:
 *                    Log.i(TAG, "Initiation Completed");
 *                    break;
 *                case DOWNLOAD_IN_PROGRESS:
 *                    Log.i(TAG, "Upload in progress");
 *                    Log.i(TAG, "Upload percentage: " + uploader.getProgress());
 *                    break;
 *                case DOWNLOAD_COMPLETE:
 *                    Log.i(TAG, "Upload Completed!");
 *                    break;
 *            }
 *    });
 * }
 *
 * </pre>
 *
 * </p>
 * @author edwardf
 * @since 2.0
 */
public class Filev2 {

}