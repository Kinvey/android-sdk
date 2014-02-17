/*
 * Copyright (c) 2014, Kinvey, Inc.
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
package com.kinvey.java.offline;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.util.ByteStreams;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.core.MediaHttpDownloader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author edwardf
 */
public class MediaOfflineDownloader extends MediaHttpDownloader {


    private FilePolicy policy = FilePolicy.ALWAYS_ONLINE;

    private FileCache cache;

    private Object lock = new Object();

    /**
     * Construct the {@link com.kinvey.java.core.MediaHttpDownloader}.
     *
     * @param transport              The transport to use for requests
     * @param httpRequestInitializer The initializer to use when creating an {@link com.google.api.client.http.HttpRequest} or
     *                               {@code null} for none
     */
    public MediaOfflineDownloader(HttpTransport transport, HttpRequestInitializer httpRequestInitializer, FilePolicy policy, FileCache cache) {
        super(transport, httpRequestInitializer);
        this.policy = policy;
        this.cache = cache;
    }

    public void fromService(AbstractKinveyClientRequest request, OutputStream out) throws IOException {
        super.download(request, out);
    }

    public void fromCache(AbstractClient client, AbstractKinveyClientRequest request, OutputStream out) throws IOException{
        String targetURI = UriTemplate.expand(client.getBaseUrl(), request.getUriTemplate(), request, true);
        int idIndex = targetURI.lastIndexOf("/" + 1);

        FileInputStream fis = cache.get(client, targetURI.substring(idIndex, targetURI.length()));
        ByteStreams.copy(fis, out);
        updateStateAndNotifyListener(DownloadState.DOWNLOAD_COMPLETE);
    }

    @Override
    public void download(AbstractKinveyClientRequest request, OutputStream out) throws IOException {
        policy.download(request, out);
    }

}
