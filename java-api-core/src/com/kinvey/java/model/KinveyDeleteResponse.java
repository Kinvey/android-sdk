/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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

/**
 * Used in request to client to delete appdata.
 *
 * @author edwardf
 * @since 2.0
 */
public class KinveyDeleteResponse extends GenericJson {

    @Key
    private int count;

    /**
     * @return The number of objects successfully deleted.
     */
    public int getCount() {
        return count;
    }

    public void setCount(int count){
        this.count = count;
    }

}
