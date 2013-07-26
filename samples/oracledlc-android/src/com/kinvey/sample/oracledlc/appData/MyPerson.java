/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
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
