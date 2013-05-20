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
package com.kinvey.java.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.util.ArrayList;


/**
 * Maintain a JSON representation of the metadata of an entity stored in a collection with Kinvey.
 *
 * If metadata is needed, a custom response class can extend this to allow for automatic handling of both
 * the Access Control List as well as the relevant Kinvey metadata.
 *
 * @author edwardf
 * @since 2.0
 *
 */
public class KinveyMetaData extends GenericJson{
//
//    @Key("_acl")
//    private AccessControlList accessControlList;

    public static final String JSON_FIELD_NAME = "_kmd";

    @Key("lmt")
    private String lastModifiedTime;

    public KinveyMetaData(){}

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }


    //-------------- getters and setters

//    public AccessControlList getAccessControlList() {
//        return accessControlList;
//    }
//
//    public void setAccessControlList(AccessControlList accessControlList) {
//        this.accessControlList = accessControlList;
//    }
//
//    public String getCreator() {
//        return accessControlList.getCreator();
//    }
//
//    public boolean isGloballyReadable() {
//        return accessControlList.isGloballyReadable();
//    }
//
//    public void setGloballyReadable(boolean readable) {
//        accessControlList.setGloballyReadable(readable);
//    }
//
//    public boolean isGloballyWriteable() {
//        return accessControlList.isGloballyWriteable();
//    }
//
//    public void setGloballyWriteable(boolean writeable) {
//        accessControlList.setGloballyWriteable(writeable);
//    }
//
//    public ArrayList<String> getRead() {
//        return accessControlList.getRead();
//    }
//
//    public void setRead(ArrayList<String> read) {
//        accessControlList.setRead(read);
//    }
//
//    public void addRead(String read) {
//        accessControlList.getRead().add(read);
//    }
//
//    public void removeRead(int index) {
//        accessControlList.getRead().remove(index);
//    }
//
//    public ArrayList<String> getWrite() {
//        return accessControlList.getWrite();
//    }
//
//    public void addWrite(String write) {
//        accessControlList.getWrite().add(write);
//    }
//
//    public void removeWrite(int index) {
//        accessControlList.remove(index);
//    }
//
//    public void setWrite(ArrayList<String> write) {
//        accessControlList.setWrite(write);
//    }
//
//    public ArrayList<com.kinvey.java.model.KinveyMetaData.AccessControlList.AclGroups> getAclGroups() {
//        return accessControlList.getGroups();
//    }
//
//    public void setAclGroups(ArrayList<com.kinvey.java.model.KinveyMetaData.AccessControlList.AclGroups> groups) {
//        accessControlList.setGroups(groups);
//    }

    //----------private inner classes

//    public static class MetaData extends GenericJson{
//
//        @Key("lmt")
//        private String lastModifiedTime;
//
//        public MetaData(){}
//

//    }

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
