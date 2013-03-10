/*
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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
package com.kinvey.java.LinkedResources;

import com.google.api.client.http.InputStreamContent;
import com.kinvey.java.cache.AbstractKinveyCachedClientRequest;
import com.kinvey.java.core.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Implementation of a Client Request, which can download linked resources.
 * <p>
 * On the call to execute, if a file is a LinkedResource, then first it gets the entity.  Then it iterates through all the attachments and downloads them.
 * Once all files have been downloaded, the entity is returned
 * </p>
 * <p>
 * This class is currently EXCLUSIVELY for an appData get request, as it relies a call to super.execute()
 * </p>
 * <p>
 * call setDownloadProgressListener to get callbacks for all file downloads.
 * </p>
 *
 * @author edwardf
 * @since 2.0.7
 */
public class GetLinkedResourceClientRequest<T> extends AbstractKinveyCachedClientRequest<T> {

    private DownloaderProgressListener download;


    /**
     * @param abstractKinveyJsonClient kinvey credential JSON client
     * @param requestMethod            HTTP Method
     * @param uriTemplate              URI template for the path relative to the base URL. If it starts with a "/"
     *                                 the base path from the base URL will be stripped out. The URI template can also be a
     *                                 full URL. URI template expansion is done using
     *                                 {@link com.google.api.client.http.UriTemplate#expand(String, String, Object, boolean)}
     * @param jsonContent              POJO that can be serialized into JSON content or {@code null} for none
     * @param responseClass            response class to parse into
     */
    protected GetLinkedResourceClientRequest(AbstractKinveyJsonClient abstractKinveyJsonClient, String requestMethod, String uriTemplate, Object jsonContent, Class<T> responseClass) {
        super(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonContent, responseClass);
    }

    @Override
    public T execute() throws IOException {


        if (getResponseClass().isAssignableFrom(LinkedResource.class)) {
            T entity = super.execute();
            System.out.println("Kinvey - LR, " + "linked resource found, file count at: " + ((LinkedResource) entity).getAllFiles().keySet().size());

            for (final String key : ((LinkedResource) entity).getAllFiles().keySet()) {
                System.out.println("Kinvey - LR, " + "getting a LinkedResource: " + key + " -> " + ((LinkedResource)entity).getFile(key).getFileName());

                getAbstractKinveyClient().file().setDownloaderProgressListener(download);
                getAbstractKinveyClient().file().download(((LinkedResource)entity).getFile(key).getFileName()).execute();
           }


            return entity;
        } else {
            return super.execute();
        }


    }

    public DownloaderProgressListener getDownloadProgressListener() {
        return download;
    }

    public void setDownloadProgressListener(DownloaderProgressListener download) {
        this.download = download;
    }
}