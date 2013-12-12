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
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.sample.contentviewr.model.ContentItem;
import com.kinvey.sample.contentviewr.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author edwardf
 */
public class ImageViewer extends Viewer  {

    private ImageView image;

    private ContentItem content;

    public static ImageViewer newInstance(ContentItem item){
        ImageViewer frag = new ImageViewer();
        frag.content = item;
        return frag;

    }


    @Override
    public int getViewID() {
        return R.layout.fragment_imageviewer;
    }

    @Override
    public void bindViews(View v){
        image = (ImageView) v.findViewById(R.id.imageview);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileMetaData meta = new FileMetaData(content.getSource().getReference());
        getClient().file().download(meta, out, new DownloaderProgressListener() {
            @Override
            public void progressChanged(MediaHttpDownloader downloader) throws IOException {}

            @Override
            public void onSuccess(Void result) {
                if (image == null){
                    return;
                }
                Bitmap ret = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.toByteArray().length);
                image.setImageBitmap(ret);
            }

            @Override
            public void onFailure(Throwable error) {}
        });

    }

    @Override
    public String getTitle() {

        return "ImageViewer";

    }


}
