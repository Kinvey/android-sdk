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
