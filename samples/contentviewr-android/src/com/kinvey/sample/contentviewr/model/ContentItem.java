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
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.android.Client;
import com.kinvey.java.LinkedResources.LinkedGenericJson;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.sample.contentviewr.ContentListAdapter;
import com.kinvey.sample.contentviewr.SourceFactory;

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
    private SourceType source;

    @Key
    private ArrayList<String> target;

    @Key
    private SourceType thumbnail;

    @Key
    private ArrayList<String> groups;

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

    public ArrayList<String> getTarget() {
        return target;
    }

    public void setTarget(ArrayList<String> target) {
        this.target = target;
    }

    public Bitmap getThumbnailImage() {
        return thumbnailImage;
    }

    public void setThumbnailImage(Bitmap thumb){
        this.thumbnailImage = thumb;
    }

    public void loadThumbnail(Client client, ContentListAdapter adapter){
        //new loadThumbnailTask().execute(adapter);
        SourceFactory.asyncLoadThumbnail(client, this, adapter);


    }

    public SourceType getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(SourceType thumbnail) {
        this.thumbnail = thumbnail;
    }

    public SourceType getSource() {
        return source;
    }

    public void setSource(SourceType source) {
        this.source = source;
    }



    public String getType(){
        return this.type;
    }

    public void setType(String type){
        this.type = type;
    }

    public ArrayList<String> getGroups(){
        return groups;
    }

    public void setGroups(ArrayList<String> groups){
        this.groups = groups;
    }


}
