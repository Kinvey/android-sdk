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
package com.kinvey.java.network;

import com.google.api.client.json.GenericJson;
import com.google.common.base.Preconditions;

import java.io.IOException;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.core.AbstractKinveyClientRequest;

/**
 * This class allows extensible use of various features provided by the NetworkManager() API and variations
 * <p>
 *   There are various `set*()` methods, which can be chained together to create a builder.  Once the builder has been configured,
 * a call to `myBuilder.build()` will return a blocking synchronous request.  By calling `myBuilder.build().execute()` the
 * the request will be constructed and executed.
 * </p>
 * <p>
 * This class supports CRUD operations on any NetworkManager collection.
 * </p>
 * <p>
 * The code below will build and execute a blocking get entity request resolving two kinvey references up to a depth of 2..
 *
 * </p>
 * <p>
 *
 *     MyEntity myEntity = new BlockingGetEntityBuilder("myCollection", MyEntity.class, NetworkManager.this)
 *                             .setEntityID(myEntity.getId());
 *                             .setResolves(new String[]{"myOtherCollectionReference1", myOtherCollectionReference2})
 *                             .setResolveDepth(2)
 *                             .build()
 *                             .execute();
 * </p>
 *
 *
 * @author edwardf
 * @since 2.3.0
 */
public class NetworkOperation {


    /**
     * Abstract App Data Request Builder parent maintains collection, class, and appdata instance.
     */
    protected static abstract class AppDataRequestBuilder {


        //Required for all NetworkManager operations.
        protected String collection;
        protected Class myClass;
        protected NetworkManager appData;

        /**
         * @param client an active Kinvey client
         * @param collectionName the Name of the collection this builder's request will be accessing
         * @param myClass a {@code GenericJson} representing the object model
         */
        public AppDataRequestBuilder(AbstractClient client, String collectionName, Class myClass) {
            this.collection = collectionName;
            this.myClass = myClass;
            this.appData = client.networkStore(collectionName, myClass);
        }


        public AbstractKinveyClientRequest build(AbstractKinveyClientRequest req) {
            try {
                this.appData.getClient().initializeRequest(req);
            } catch (IOException e) {
//                e.printStackTrace();
                //TODO edwardf don't want to necessarily catch this here.
            }
            return req;
        }
    }



    /**
     * Builder for creating new GET requests with the core App Data API.
     * <p>
     * This builder supports Kinvey References.
     * </p>
     */
    public static class BlockingGetBuilder extends AppDataRequestBuilder {
        protected Query query = null;
        //Kinvey Reference Support
        protected String[] resolves = null;
        //set defaults for kinvey reference
        //note the above `resolves != null` determines if Kinvey References are used.
        protected int resolveDepth = 1;
        protected boolean retainReference = true;


        public BlockingGetBuilder(AbstractClient client, String collectionName, Class myClass) {
            super(client, collectionName, myClass);
        }


        public AppDataRequestBuilder setQuery(Query query) {
            this.query = query;
            return this;
        }

        public AppDataRequestBuilder setID(String id){
            this.query = new Query().equals(NetworkManager.ID_FIELD_NAME, id);
            return this;
        }

        public AbstractKinveyClientRequest build() {
            AbstractKinveyClientRequest ret = null;
            if (this.query == null) {
                this.query = new Query();
            }

            if (resolves == null) {
                ret = this.appData.new Get(this.query, this.myClass);
            } else {
                ret = this.appData.new Get(this.query, this.myClass, resolves, resolveDepth, retainReference);
            }

            return super.build(ret);
        }

        public AppDataRequestBuilder setResolves(String[] resolves) {
            this.resolves = resolves;
            return this;
        }

        public AppDataRequestBuilder setResolveDepth(int depth) {
            this.resolveDepth = depth;
            return this;
        }

        public AppDataRequestBuilder setRetainReferences(boolean retain) {
            this.retainReference = retain;
            return this;
        }
    }



    /**
     * Builder for creating new SAVE requests with the core App Data API.
     */
    public static class BlockingSaveBuilder extends AppDataRequestBuilder {
        protected Object myEntity = null;

        public BlockingSaveBuilder(AbstractClient client, String collectionName, Class myClass) {
            super(client, collectionName, myClass);
        }

        public AppDataRequestBuilder setEntity(Object myEntity) {
            this.myEntity = myEntity;
            return this;
        }

        public AbstractKinveyClientRequest build() {
            Preconditions.checkNotNull(this.myEntity, "Cannot use SAVE without first calling setEntity(myEntity)");


            AbstractKinveyClientRequest ret = null;


            GenericJson jsonEntity = (GenericJson) this.myEntity;
            String sourceID = (String) jsonEntity.get(NetworkManager.ID_FIELD_NAME);

            if (sourceID != null) {
                ret = this.appData.new Save(this.myEntity, myClass, sourceID, NetworkManager.SaveMode.PUT);
            } else {
                ret = this.appData.new Save(this.myEntity, myClass, NetworkManager.SaveMode.POST);
            }
            return super.build(ret);

        }

    }


    /**
     * Builder for creating new DELETE requests with the core App Data API.
     */
    public static class BlockingDeleteBuilder extends AppDataRequestBuilder {
        protected Query query = null;

        public BlockingDeleteBuilder(AbstractClient client, String collectionName, Class myClass) {
            super(client, collectionName, myClass);
        }


        public AppDataRequestBuilder setEntityID(String entityID) {
            this.query = new Query().equals(NetworkManager.ID_FIELD_NAME, entityID);
            return this;
        }


        public AppDataRequestBuilder setQuery(Query query) {
            this.query = query;
            return this;
        }

        public AbstractKinveyClientRequest build() {
            AbstractKinveyClientRequest ret = null;

            if (this.query != null) {
                ret = this.appData.new Delete(this.query);
            } else {
                Preconditions.checkArgument((this.query != null), "Cannot use DELETE without either calling setEntityID(...) or setQuery(...)");
                return null;
            }
            return super.build(ret);
        }
    }


}