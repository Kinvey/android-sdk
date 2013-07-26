/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
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
