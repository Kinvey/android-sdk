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

import com.google.common.base.Preconditions;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.core.MediaHttpDownloader;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author edwardf
 */
public enum FilePolicy {
    ALWAYS_ONLINE {
        @Override
        public void download(AbstractKinveyClientRequest initiationClientRequest, OutputStream out) throws IOException {
            MediaOfflineDownloader downloader = getDownloader(initiationClientRequest);
            if (downloader != null){
                downloader.fromService(initiationClientRequest, out);
            }
        }
    },


    LOCAL_FIRST {
        @Override
        public void download(AbstractKinveyClientRequest initiationClientRequest, OutputStream out) throws IOException {
            MediaOfflineDownloader downloader = getDownloader(initiationClientRequest);
            if (downloader != null){
                downloader.fromCache((AbstractClient) initiationClientRequest.getAbstractKinveyClient(), initiationClientRequest, out);
                if (downloader.getDownloadState() != MediaHttpDownloader.DownloadState.DOWNLOAD_COMPLETE){
                    downloader.fromService(initiationClientRequest, out);
                }
            }
        }


    },

    ONLINE_FIRST {
        @Override
        public void download(AbstractKinveyClientRequest initiationClientRequest, OutputStream out) throws IOException {
            MediaOfflineDownloader downloader = getDownloader(initiationClientRequest);
            if (downloader != null){
                try{
                    downloader.fromService(initiationClientRequest, out);
                }catch(Exception e){
                    downloader.fromCache((AbstractClient) initiationClientRequest.getAbstractKinveyClient(), initiationClientRequest, out);
                }

            }
        }

    };


    public abstract void download(AbstractKinveyClientRequest initiationClientRequest, OutputStream out) throws IOException;

    private static MediaOfflineDownloader getDownloader(AbstractKinveyClientRequest request){
        MediaHttpDownloader down = request.getDownloader();
        if (down != null && down instanceof MediaOfflineDownloader){
            return (MediaOfflineDownloader) down;
        }
        return null;
    }

}
