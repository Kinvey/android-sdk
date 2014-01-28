/*
 * Copyright (c) 2014, Kinvey, Inc.
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

import android.os.Parcel;
import android.os.Parcelable;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.api.client.util.Value;

import java.io.Serializable;

/**
 * @author edwardf
 */
public class ContentType extends GenericJson implements Parcelable {

    @Key
    private String displayName;
    @Key
    private String name;

    @Key
    private WINDOWTYPE windowstyle;

    private boolean isLabel = false;
    private boolean isSetting = false;
    private long uniqueID;

    public ContentType(){}


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLabel() {
        return isLabel;
    }

    public void setLabel(boolean label) {
        isLabel = label;
    }

    public boolean isSetting() {
        return isSetting;
    }

    public void setSetting(boolean setting) {
        isSetting = setting;
    }

    public long getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(long uniqueID) {
        this.uniqueID = uniqueID;
    }


    // Parcelling part
    public ContentType(Parcel in){

        this.displayName = in.readString();
        this.name = in.readString();
        boolean[] whatitis = new boolean[2];
        in.readBooleanArray(whatitis);
        this.isLabel = whatitis[0];
        this.isSetting = whatitis[1];
        this.uniqueID = in.readLong();

    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.displayName);
        dest.writeString(this.name);
        dest.writeBooleanArray(new boolean[]{isLabel, isSetting});
        dest.writeLong(uniqueID);
    }

    public static final Parcelable.Creator<ContentType> CREATOR = new Parcelable.Creator<ContentType>() {
        public ContentType createFromParcel(Parcel in) {
            return new ContentType(in);
        }
        public ContentType[] newArray(int size) {
            return new ContentType[size];
        }
    };

    public WINDOWTYPE getWindowstyle() {
        return windowstyle;
    }

    public void WINDOWTYPE(WINDOWTYPE windowstyle) {
        this.windowstyle = windowstyle;
    }

    public enum WINDOWTYPE{

        @Value
        IMAGE,
        @Value
        WEB,
        @Value
        PDF;

    }
}
