/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 *
 */

package com.kinvey.java.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;


/**
 * Maintain a JSON representation of the metadata of an entity stored in a collection with Kinvey.
 *
 * Every Entity persisted with Kinvey maintains additional metadata,
 *
 * @author edwardf
 * @since 2.0
 *
 */
public class KinveyMetaData extends GenericJson{

    public static final String KMD = "_kmd";
    public static final String LMT = "lmt";
    public static final String ECT = "ect";

    @Key(LMT)
    private String lastModifiedTime;

    @Key(ECT)
    private String entityCreationTime;

    public KinveyMetaData(){}

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public String getEntityCreationTime(){
        return entityCreationTime;
    }

    public static KinveyMetaData fromMap(Map kmd) {
        KinveyMetaData metaData = new KinveyMetaData();
        if (kmd != null) {
            if (!kmd.containsKey(LMT) || kmd.get(LMT) == null) {
                metaData.put(LMT, String.format("%tFT%<tTZ",
                        Calendar.getInstance(TimeZone.getTimeZone("Z"))));
                System.out.println("LMT_TEST_TEST_TEST");
            } else {
                metaData.put(LMT, kmd.get(LMT));
            }
            if (!kmd.containsKey(ECT) || kmd.get(ECT) == null) {
                metaData.put(ECT, String.format("%tFT%<tTZ",
                        Calendar.getInstance(TimeZone.getTimeZone("Z"))));
                System.out.println("ECT_TEST_TEST_TEST");
            } else {
                metaData.put(ECT, kmd.get(ECT));
            }
        }
        return metaData;
    }

    public static class AccessControlList extends GenericJson{

        public static final String JSON_FIELD_NAME = "_acl";

        @Key
        private String creator;
        @Key("gr")
        private boolean globallyReadable;
        @Key("gw")
        private boolean globallyWriteable;
        @Key("r")
        private ArrayList<String> read;
        @Key("w")
        private ArrayList<String> write;
        @Key("groups")
        private ArrayList<AclGroups> groups;

        public static class AclGroups extends GenericJson {
            @Key("r")
            String read;
            @Key("w")
            String write;

            public AclGroups(){}


            public String getRead() {
                return read;
            }

            public void setRead(String read) {
                this.read = read;
            }

            public String getWrite() {
                return write;
            }

            public void setWrite(String write) {
                this.write = write;
            }
        }


        public AccessControlList(){}

        public boolean isGloballyReadable() {
            return globallyReadable;
        }

        public void setGloballyReadable(boolean globallyReadable) {
            this.globallyReadable = globallyReadable;
        }

        public boolean isGloballyWriteable() {
            return globallyWriteable;
        }

        public void setGloballyWriteable(boolean globallyWriteable) {
            this.globallyWriteable = globallyWriteable;
        }

        public ArrayList<String> getRead() {
            return read;
        }

        public void setRead(ArrayList<String> read) {
            this.read = read;
        }

        public ArrayList<String> getWrite() {
            return write;
        }

        public void setWrite(ArrayList<String> write) {
            this.write = write;
        }

        public ArrayList<AclGroups> getGroups() {
            return groups;
        }

        public void setGroups(ArrayList<AclGroups> groups) {
            this.groups = groups;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }
    }



}
