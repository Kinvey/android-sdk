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

package com.kinvey.java.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key
import com.kinvey.java.AbstractClient
import com.kinvey.java.Constants

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


/**
 * Maintain a JSON representation of the metadata of an entity stored in a collection with Kinvey.
 *
 * Every Entity persisted with Kinvey maintains additional metadata,
 *
 * @author edwardf
 * @since 2.0
 */
open class KinveyMetaData : GenericJson() {

    @Key(LMT)
    var lastModifiedTime: String? = null

    @Key(ECT)
    var entityCreationTime: String? = null

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

    class AccessControlList : GenericJson() {

        @Key(CREATOR)
        private var creator: String? = null
        @Key(GR)
        private var globallyReadable: Boolean? = null
        @Key(GW)
        private var globallyWritable: Boolean? = null

        val isGloballyReadable: Boolean
            get() = globallyReadable ?: false

        val isGloballyWritable: Boolean
            get() = globallyWritable ?: false

        fun setGloballyReadable(globallyReadable: Boolean): AccessControlList {
            this.globallyReadable = globallyReadable
            return this
        }

        fun setGloballyWritable(globallyWritable: Boolean): AccessControlList {
            this.globallyWritable = globallyWritable
            return this
        }

        fun getCreator(): String? {
            return creator
        }

        fun setCreator(creator: String): AccessControlList {
            this.creator = creator
            return this
        }

        companion object {

            //const val JSON_FIELD_NAME = "_acl";
            const val ACL = "_acl"
            const val CREATOR = "creator"
            const val GR = "gr"
            const val GW = "gw"
            const val R = "r"
            const val W = "w"
            const val GROUPS = "groups"

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

            @JvmStatic
            fun fromMap(acl: Map<*, *>?): AccessControlList {
                val accessControlList = AccessControlList()
                if (acl != null) {
                    if (!acl.containsKey(CREATOR) || acl[CREATOR] == null) {
                        accessControlList[CREATOR] = AbstractClient.sharedInstance?.activeUser?.id
                    } else {
                        accessControlList[CREATOR] = acl[CREATOR]
                    }

                    if (acl.containsKey(GR) && acl[GR] != null) {
                        accessControlList[GR] = acl[GR]
                    }
                    if (acl.containsKey(GW) && acl[GW] != null) {
                        accessControlList[GW] = acl[GW]
                    }

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
                return accessControlList
            }
        }
    }

    companion object {
        const val KMD = "_kmd"
        const val LMT = "lmt"
        const val META = "_meta"
        const val ECT = "ect"

        @JvmStatic
        fun fromMap(kmd: Map<String, Any>?): KinveyMetaData {
            val metaData = KinveyMetaData()
            if (kmd != null) {
                if (!kmd.containsKey(LMT) || kmd[LMT] == null) {
                    metaData[LMT] = String.format(Locale.US, Constants.TIME_FORMAT,
                            Calendar.getInstance(TimeZone.getTimeZone(Constants.Z)))
                } else {
                    metaData[LMT] = kmd[LMT]
                }
                if (!kmd.containsKey(ECT) || kmd[ECT] == null) {
                    metaData[ECT] = String.format(Locale.US, Constants.TIME_FORMAT,
                            Calendar.getInstance(TimeZone.getTimeZone(Constants.Z)))
                } else {
                    metaData[ECT] = kmd[ECT]
                }
            }
            return metaData
        }
    }
}
