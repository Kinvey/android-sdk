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

/**
 * A KinveyReference allows for entities to include relational data from other collections and entities.
 *
 * To use this class within an Entities' GenericJson implementation, just define a field's type as a KinveyReference.
 *
 * Then, when, making a GET call through the AppData API various parameters can be used to specify how the backend should
 * handle these mappings.
 *
 *  The 'resolve' attribute can be passed a list of Strings containing all the KinveyReference fields that should be mapped.
 *  The appropriate field in the Response Object should also be a KinveyReference,
 *  but the relational mapping can be followed by accessing the returnObject field declared below.
 *
 *  'resolve_depth' is an int argument for GET requests through the AppData API, and will resolve all KinveyReferences up to N levels.
 *  Keep in mind a KinveyReference.returnObject can contain other embedded KinveyReferences, and so on, allowing for multiple mappings
 *  without explicitly declaring all fields.
 *
 *  `retain_references` is also supported, and can be used to pull the contents of returnObject up to replace the KinveyReference.
 *  This flag defaults to true, but can be explicitly set to false if an end user only cares about the 'returnObject' value.
 *
 *
 *  Note when making requests with a KinveyReference this field should be left as null.
 *
 *
 * @author edwardf
 * @since 2.0
 *
 *
 */
public class KinveyReference extends GenericJson{

    public static final String USER_COLLECTION = "_user";

    @Key("_type")
    private static final String type = "KinveyRef";

    @Key("_id")
    private String id;

    @Key("_collection")
    private String collection;

    @Key("_obj")
    private GenericJson returnObject;

    public KinveyReference(){}

    public KinveyReference(String collectionName, String id) {
        this.collection = collectionName;
        this.id = id;
    }

    //-------------getters and setters

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GenericJson getReturnObject() {
        return returnObject;
    }

    public void setReturnObject(GenericJson returnObject) {
        this.returnObject = returnObject;
    }
}
