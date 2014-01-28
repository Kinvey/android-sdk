/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.Key;

import java.lang.reflect.Constructor;

/**
 * A KinveyReference allows for entities to include relational data from other collections and entities.
 * <p>
 * To use this class within an Entities' GenericJson implementation, just define a field's type as a KinveyReference.
 * </p>
 * <p>
 * Then, when, making a GET call through the AppData API various parameters can be used to specify how the backend should
 * handle these mappings.
 * </p>
 * <p>
 *  The 'resolve' attribute can be passed a list of Strings containing all the KinveyReference fields that should be mapped.
 *  The appropriate field in the Response Object should also be a KinveyReference,
 *  but the relational mapping can be followed by accessing the returnObject field declared below.
 * </p>
 * <p>
 *  'resolve_depth' is an int argument for GET requests through the AppData API, and will resolve all KinveyReferences up to N levels.
 *  Keep in mind a KinveyReference.returnObject can contain other embedded KinveyReferences, and so on, allowing for multiple mappings
 *  without explicitly declaring all fields.
 *  </p>
 * <p>
 *  `retain_references` is also supported, and can be used to pull the contents of returnObject up to replace the KinveyReference.
 *  This flag defaults to true, but can be explicitly set to false if an end user only cares about the 'returnObject' value.
 * </p>
 *
 * @author edwardf
 * @since 2.0
 *
 *
 */
public class KinveyReference extends GenericJson{

    /**
     *
     * @deprecated moved to {@link com.kinvey.java.User#USER_COLLECTION_NAME}
     */
    @Deprecated
    public static final String USER_COLLECTION = "user";
    public static final String RESOLVED_KEY = "_obj";

    @Key("_type")
    private String type = "KinveyRef";

    @Key("_id")
    private String id;

    @Key("_collection")
    private String collection;


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

    public GenericJson getResolvedObject() {
        ArrayMap direct = (ArrayMap) get("_obj");
        if (direct == null){
            return null;
        }

        GenericJson ret = new GenericJson();
        ret.putAll(direct);
        return ret;
    }

    public <T extends GenericJson> T getTypedObject(Class<T> clazz){
        ArrayMap direct = (ArrayMap) get("_obj");
        if (direct == null){
            return null;
        }
        T ret = null;
        try {
            ret = clazz.newInstance();
            ret.putAll(direct);
        } catch (Exception e) {
            System.out.println("unable to instantiate class!");
            e.printStackTrace();
            return null;
        }

        return ret;
    }



    public String getType(){
        return this.type;
    }

    public void setType(String type){
        //do nothing, looks like json library needs this setter available....?
    }


}
