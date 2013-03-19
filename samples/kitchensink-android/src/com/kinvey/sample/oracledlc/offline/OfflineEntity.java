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
package com.kinvey.sample.oracledlc.offline;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.io.*;

/**
 * @author edwardf
 * @since 2.0
 */
public class OfflineEntity extends GenericJson implements Serializable{

    static final long serialVersionUID =5305109690724274634L;

    @Key("_id")
    private String id;

    @Key("Test")
    private String test = "This is a hard-coded test!";


    public OfflineEntity(){}


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTest(){return this.test;}

    public void setTest(String t){
        this.test = t;
    }
}
