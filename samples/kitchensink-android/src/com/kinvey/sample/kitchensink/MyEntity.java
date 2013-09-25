/** 
 * Copyright (c) 2013 Kinvey Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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

    @Key("aggregateField")
    private int aggField;


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

    public void setAggField(int agg){
        this.aggField = agg;
    }

    public int getAggField(){
        return this.aggField;
    }
}
