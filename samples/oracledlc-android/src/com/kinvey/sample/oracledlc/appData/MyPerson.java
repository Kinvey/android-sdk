/** 
 * Copyright (c) 2014, Kinvey, Inc.
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
package com.kinvey.sample.oracledlc.appData;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class MyPerson extends GenericJson{


    @Key("_id")
    private Integer id;

    @Key("firstName")
    private String name;

    @Key("email")
    private String email = "null@nullzy.com";

    @Key("lastName")
    private String lastname = "nully";

    public MyPerson(){}



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public Location getLocation() {
//        return location;
//    }
//
//    public void setLocation(Location location) {
//        this.location = location;
//    }
}
