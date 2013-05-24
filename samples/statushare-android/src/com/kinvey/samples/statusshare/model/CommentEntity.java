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
package com.kinvey.samples.statusshare.model;


import android.util.Log;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.android.Client;
import com.kinvey.java.model.KinveyMetaData;
import com.kinvey.java.model.KinveyReference;
import com.kinvey.samples.statusshare.StatusShare;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This class maintains a Comment which can be persisted with Kinvey.
 *
 * @author edwardf
 * @since 2.0
 */
public class CommentEntity extends GenericJson {



    @Key("_id")
    private String id;
    @Key("text")
    private String text;
    @Key(KinveyMetaData.JSON_FIELD_NAME)
    private KinveyMetaData meta;
    @Key("_acl")
    private KinveyMetaData.AccessControlList acl;
    @Key("author")
    private String author;


    public CommentEntity(){
//        meta = new KinveyMetaData();
//        acl = new KinveyMetaData.AccessControlList();
    }

    public CommentEntity(String name){
        meta = new KinveyMetaData();
        acl = new KinveyMetaData.AccessControlList();
        this.text = name;
    }




    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public KinveyMetaData getMeta() {
        return meta;
    }

    public void setMeta(KinveyMetaData meta) {
        this.meta = meta;
    }

    public KinveyMetaData.AccessControlList getAcl() {
        return acl;
    }

    public void setAcl(KinveyMetaData.AccessControlList acl) {
        this.acl = acl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }




}
