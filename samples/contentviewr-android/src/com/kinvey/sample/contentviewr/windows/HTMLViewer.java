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
import com.kinvey.sample.contentviewr.Contentviewr;
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
        webview.loadUrl(url);


    }


    @Override
    public String getTitle() {

        return "WebViewer";

    }
}
