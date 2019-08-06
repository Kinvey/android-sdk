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
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
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
    public static final String META = "_meta";
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
                metaData.put(LMT, String.format(Locale.US, Constants.TIME_FORMAT,
                        Calendar.getInstance(TimeZone.getTimeZone(Constants.Z))));
            } else {
                metaData.put(LMT, kmd.get(LMT));
            }
            if (!kmd.containsKey(ECT) || kmd.get(ECT) == null) {
                metaData.put(ECT, String.format(Locale.US, Constants.TIME_FORMAT,
                        Calendar.getInstance(TimeZone.getTimeZone(Constants.Z))));
            } else {
                metaData.put(ECT, kmd.get(ECT));
            }
        }
        return metaData;
    }

    public static class AccessControlList extends GenericJson{

//        public static final String JSON_FIELD_NAME = "_acl";
        public static final String ACL = "_acl";
        private static final String CREATOR = "creator";
        private static final String GR = "gr";
        private static final String GW = "gw";

//        private static final String R = "r";
//        private static final String W = "w";
//        private static final String GROUPS = "groups";

        @Key(CREATOR)
        private String creator = null;
//        @Key(GR)
//        private Boolean globallyReadable;
//        @Key(GW)
//        private Boolean globallyWritable;

//        @Key(R)
//        private ArrayList<String> read;
//        @Key(W)
//        private ArrayList<String> write;

//        @Key(GROUPS)
//        private ArrayList<AclGroups> groups;
//
//        public static class AclGroups extends GenericJson {
//            @Key(R)
//            String read;
//            @Key(W)
//            String write;
//
//            public AclGroups(){}
//
//            public String getRead() {
//                return read;
//            }
//
//            public void setRead(String read) {
//                this.read = read;
//            }
//
//            public String getWrite() {
//                return write;
//            }
//
//            public void setWrite(String write) {
//                this.write = write;
//            }
//        }

        public static AccessControlList fromMap(Map acl) {
            AccessControlList accessControlList = new AccessControlList();
            if (acl != null) {
                if (!acl.containsKey(CREATOR) || acl.get(CREATOR) == null) {
                    accessControlList.put(CREATOR, AbstractClient.sharedInstance().getActiveUser().getId());
                } else {
                    accessControlList.put(CREATOR, acl.get(CREATOR));
                }

//                if (acl.containsKey(GR) && acl.get(GR) != null) {
//                    accessControlList.put(GR, acl.get(GR));
//                }
//                if (acl.containsKey(GW) && acl.get(GW) != null) {
//                    accessControlList.put(GW, acl.get(GW));
//                }

//                if (acl.containsKey(R) && acl.get(R) != null) {
//                    accessControlList.put(R, acl.get(R));
//                }
//                if (acl.containsKey(W) && acl.get(W) != null) {
//                    accessControlList.put(W, acl.get(W));
//                }

//                ArrayList<AclGroups> aclGroupses = new ArrayList<>();
//                if (acl.containsKey(GROUPS) && acl.get(GROUPS) != null) {
//                    for (AclGroups groups : (ArrayList<AclGroups>)acl.get(GROUPS)) {
//                        AclGroups aclGroups = new AclGroups();
//                        if (groups.containsKey(R) && groups.get(R) != null) {
//                            aclGroups.put(R, acl.get(R));
//                        }
//                        if (groups.containsKey(W) && groups.get(W) != null) {
//                            aclGroups.put(W, acl.get(W));
//                        }
//                        aclGroupses.add(aclGroups);
//                    }
//                } else {
//                    aclGroupses.add(new AclGroups());
//                }
//                accessControlList.put(GROUPS, aclGroupses);

            }
            return accessControlList;
        }

        public boolean isGloballyReadable() {
            if (containsKey(GR) && get(GR) != null) {
                return (boolean) get(GR);
            }
            return false;
        }

        public void setGloballyReadable(boolean globallyReadable) {
            set(GR, globallyReadable);
        }

        public boolean isGloballyWritable() {
            if (containsKey(GW) && get(GW) != null) {
                return (boolean) get(GW);
            }
            return false;
        }

        public void setGloballyWritable(boolean globallyWritable) {
            set(GW, globallyWritable);
        }

//        public ArrayList<String> getRead() {
//            return read;
//        }
//
//        public void setRead(ArrayList<String> read) {
//            this.read = read;
//        }
//
//        public ArrayList<String> getWrite() {
//            return write;
//        }
//
//        public void setWrite(ArrayList<String> write) {
//            this.write = write;
//        }

//        public ArrayList<AclGroups> getGroups() {
//            return groups;
//        }
//
//        public void setGroups(ArrayList<AclGroups> groups) {
//            this.groups = groups;
//        }

        public AccessControlList(){}

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }
    }
}
