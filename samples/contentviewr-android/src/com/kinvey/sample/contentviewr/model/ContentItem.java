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
package com.kinvey.sample.contentviewr.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.android.Client;
import com.kinvey.java.LinkedResources.LinkedGenericJson;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.sample.contentviewr.ContentListAdapter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author edwardf
 */
public class ContentItem extends LinkedGenericJson {

    @Key
    private String name;

    @Key
    private String blurb;

    @Key
    private String location;

    @Key
    private ArrayList<String> target;

    @Key
    private String thumbnail;

    @Key
    private String type;

    private Bitmap thumbnailImage;
    public static final String attachmentName = "kinvey_attachment";


    public ContentItem(){
        putFile(attachmentName);
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArrayList<String> getTarget() {
        return target;
    }

    public void setTarget(ArrayList<String> target) {
        this.target = target;
    }

    /**
     * Get the thumbnail from the LinkedResource
     *
     * Note it closes the output stream.
     *
     * @return null or the image attachment
     */
    public Bitmap getThumbnailImage() {
        return thumbnailImage;
    }

    public void loadThumbnail(ContentListAdapter adapter){
        new loadThumbnailTask().execute(adapter);


    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    private class loadThumbnailTask extends AsyncTask<ContentListAdapter, Void, ContentListAdapter> {


        @Override
        protected ContentListAdapter doInBackground(ContentListAdapter ... adapter) {
            if (thumbnailImage == null) {
                thumbnailImage = getBitmapFromURL(thumbnail);
                //and there is an actual LinkedFile behind the Key
//                if (getFile(attachmentName) != null) {
//                    //Then decode from the output stream and get the image.
//                    thumbnailImage = BitmapFactory.decodeByteArray(getFile(attachmentName).getOutput().toByteArray(), 0, getFile(attachmentName).getOutput().toByteArray().length);
//                    try {
//                        //close the output stream
//                        getFile(attachmentName).getOutput().close();
//                    } catch (Exception e) {
//
//                    }
//                }
            }
            return adapter[0];
        }

        @Override
        protected void onPostExecute(ContentListAdapter adapter){
            if (adapter != null){
                adapter.notifyDataSetChanged();
            }

        }


    };

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

    public String getType(){
        return this.type;
    }

    public void setType(String type){
        this.type = type;
    }

}
