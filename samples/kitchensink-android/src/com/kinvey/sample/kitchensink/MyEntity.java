/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.sample.kitchensink;

import android.location.Location;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.java.model.KinveyMetaData;

/**
 * @author edwardf
 * @since 2.0
 */
public class MyEntity extends GenericJson{


    @Key("_id")
    private String id;

    @Key("name")
    private String name;

    @Key("_acl")
    private KinveyMetaData.AccessControlList acl;

    public MyEntity(){
        this.acl = new KinveyMetaData.AccessControlList();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public KinveyMetaData.AccessControlList getAccess() {
        return acl;
    }

    public void setAccess(KinveyMetaData.AccessControlList acl) {
        this.acl = acl;
    }
}
