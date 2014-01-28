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
import com.kinvey.android.offline.FileCache;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MetaDownloadProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.sample.contentviewr.Contentviewr;
import com.kinvey.sample.contentviewr.component.ZoomImageView;
import com.kinvey.sample.contentviewr.R;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author edwardf
 */
public class ImageViewer extends Viewer  {

    ByteArrayOutputStream out = new ByteArrayOutputStream();



    private ZoomImageView image;

    @Override
    public int getViewID() {
        return R.layout.fragment_imageviewer;
    }

    @Override
    public void bindViews(View v){
        image = (ZoomImageView) v.findViewById(R.id.imageview);
        getImage();



    }


    private void getImage(){
        if (content == null){
            return;
        }



        FileCache cache = new FileCache(Contentviewr.cacheLocation);
        FileInputStream in = cache.get(getSherlockActivity().getApplicationContext(), content.getSource().getReference());
        if (in != null){
            if (image == null){
                Log.i("zoomImage", "nulled out");
                return;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap ret = BitmapFactory.decodeStream(in, null, options);
            image.setImageBitmap(ret);
            Log.i("zoomImage", "set from cache");
            return;

        }


        FileMetaData meta = new FileMetaData(content.getSource().getReference());
        getClient().file().download(meta, out, new MetaDownloadProgressListener() {
            @Override
            public void progressChanged(MediaHttpDownloader downloader) throws IOException {}

            @Override
            public void onSuccess(Void result) {
                if (image == null || getSherlockActivity() == null){
                    Log.i("zoomImage", "nulled out");

                    return;
                }
                Log.i("zoomImage", "set from service");
                FileCache cache = new FileCache(Contentviewr.cacheLocation);
                byte[] outarray = out.toByteArray();
                if (getMetadata() != null){
                    Log.e("WAT", "" + (getSherlockActivity() != null));
                    Log.e("WAT", "" + (getSherlockActivity().getApplicationContext() != null));
                    Log.e("WAT", "" + (getClient() != null));
                    Log.e("WAT", "" + (getMetadata() != null));
                    Log.e("WAT", "" + (outarray != null));
                    cache.save(getSherlockActivity().getApplicationContext(), getClient(), getMetadata(), outarray);
                }

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap ret = BitmapFactory.decodeByteArray(outarray, 0, outarray.length, options);

                image.setImageBitmap(ret);
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

        return "ImageViewer";

    }


}
