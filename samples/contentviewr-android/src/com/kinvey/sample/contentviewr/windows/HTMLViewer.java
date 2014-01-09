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
package com.kinvey.sample.contentviewr.windows;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MetaDownloadProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.sample.contentviewr.R;
import com.kinvey.sample.contentviewr.file.FileCache;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author edwardf
 */
public class HTMLViewer extends Viewer {


    private WebView webview;

    @Override
    public int getViewID() {
        return R.layout.fragment_webviewer;
    }

    @Override
    public void bindViews(View v){
        Log.i("HTML Viewer", "binding views");
        webview = (WebView) v.findViewById(R.id.webview);

        String url = content.getSource().getReference();

//        if (url.startsWith("http")){
            Log.i("HTML Viewer", "it's a website");
            webview.loadUrl(url);


//        }else{
//            Log.i("HTML Viewer", "it's a pdf");
//            loadPDF();
//
//
//
//        }




        //webview.loadUrl(content.getSource().getReference());
    }

    private void loadPDF(){

            if (content == null){
                return;
            }

            final ByteArrayOutputStream out = new ByteArrayOutputStream();

            FileCache cache = new FileCache();
        String filename = cache.getFilenameForID(getSherlockActivity().getApplicationContext(), content.getSource().getReference());
        if (filename != null){


            String url = getClient().getContext().getCacheDir() + filename;
//

                String googleDocs = "https://docs.google.com/viewer?url=";
                webview.loadUrl(googleDocs + url);

            return;
        }
//            FileInputStream in = cache.get(getSherlockActivity().getApplicationContext(), content.getSource().getReference());
//            if (in != null){
//                if (webview == null){
//                    Log.i("zoomImage", "nulled out");
//                    return;
//                }
//
//
//                String url = getClient().getContext().getCacheDir() + cache.().getFileName() ;
//
//                String googleDocs = "https://docs.google.com/viewer?url=";
//                webview.loadUrl(googleDocs + url);
//                return;
//
//            }


            FileMetaData meta = new FileMetaData(content.getSource().getReference());
            getClient().file().download(meta, out, new MetaDownloadProgressListener() {
                @Override
                public void progressChanged(MediaHttpDownloader downloader) throws IOException {}

                @Override
                public void onSuccess(Void result) {
                    if (webview == null){
                        Log.i("zoomImage", "nulled out");

                        return;
                    }
                    Log.i("zoomImage", "set from service");
                    FileCache cache = new FileCache();
                    byte[] outarray = out.toByteArray();
                    if (getMetadata() != null){
                        cache.save(getClient().getContext(), getClient(), getMetadata(), outarray);
                    }

                    String url = getClient().getContext().getCacheDir() + getMetadata().getFileName();
                    Log.i("htmlviewer", url );
                    String googleDocs = "https://docs.google.com/viewer?url=";
                    webview.loadUrl(googleDocs + url);


                }

                @Override
                public void onFailure(Throwable error) {
                    Log.i("zoomImage", "failure " + error );
                    error.printStackTrace();

                }
            });


    }

    @Override
    public String getTitle() {

        return "WebViewer";

    }
}
