/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.samples.citywatch;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class FacebookEntity extends GenericJson {
    @Key
    public String entityId;                // Required
    @Key("_id")
    public String objectType;            // Required
    // Getters and Setters


    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }
}
