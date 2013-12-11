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
package com.kinvey.sample.contentviewr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;
import com.kinvey.android.Client;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.sample.contentviewr.model.ContentItem;
import com.kinvey.sample.contentviewr.model.SourceType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author edwardf
 */
public class SourceFactory {


    public static void asyncLoadThumbnail(Client client, final ContentItem item, final ArrayAdapter adapter){
        switch(item.getThumbnail().getType()){
            case FILE:
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                FileMetaData meta = new FileMetaData(item.getThumbnail().getReference());
                client.file().download(meta, out, new DownloaderProgressListener() {
                    @Override
                    public void progressChanged(MediaHttpDownloader downloader) throws IOException {}

                    @Override
                    public void onSuccess(Void result) {
                        if (adapter == null || item == null){
                            return;
                        }
                        Bitmap ret = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.toByteArray().length);
                        item.setThumbnailImage(ret);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Throwable error) {}
                });

                break;
            case WEBSITE:

                new AsyncTask<ArrayAdapter, Void, ArrayAdapter>(){

                    @Override
                    protected ArrayAdapter doInBackground(ArrayAdapter ... adapter) {
                        item.setThumbnailImage(getBitmapFromURL(item.getThumbnail().getReference()));
                        return adapter[0];

                    }

                    @Override
                    protected void onPostExecute(ArrayAdapter adapter){
                        if (adapter != null){
                            adapter.notifyDataSetChanged();
                        }
                    }


                }.execute(adapter);

                break;

        }



    }


    private static void loadFile(Client client, final ContentItem item, final ArrayAdapter adapter){}
    private static void loadWebsite(Client client, final ContentItem item, final ArrayAdapter adapter){}

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            Log.i(Client.TAG, "got image, for setting image " + src);
            return myBitmap;
        } catch (IOException e) {
            Log.i(Client.TAG, "cant be setting image!" + e);
            e.printStackTrace();
            return null;
        }
    }



}
