/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.kinvey.java.cache.AbstractKinveyCachedClientRequest;
import com.kinvey.java.cache.Cache;
import com.kinvey.java.cache.CachePolicy;
import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.model.AggregateEntity;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.query.MongoQueryFilter;

/**
 * This class allows extensible use of various features provided by the AppData() API and variations
 * <p>
 *   There are various `set*()` methods, which can be chained together to create a builder.  Once the builder has been configured,
 * a call to `myBuilder.build()` will return a blocking synchronous request.  By calling `myBuilder.build().execute()` the
 * the request will be constructed and executed.
 * </p>
 * <p>
 * This class supports CRUD operations on any AppData collection.
 * </p>
 * <p>
 * The code below will build and execute a blocking get entity request resolving two kinvey references up to a depth of 2..
 *
 * </p>
 * <p>
 *
 *     MyEntity myEntity = new BlockingGetEntityBuilder("myCollection", MyEntity.class, AppData.this)
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
public class AppDataOperation {


    /**
     * Abstract App Data Request Builder parent maintains collection, class, and appdata instance.
     */
    protected static abstract class AppDataRequestBuilder {


        //Required for all AppData operations.
        protected String collection;
        protected Class myClass;
        protected AppData appData;

        /**
         * @param client an active Kinvey client
         * @param collectionName the Name of the collection this builder's request will be accessing
         * @param myClass a {@code GenericJson} representing the object model
         */
        public AppDataRequestBuilder(AbstractClient client, String collectionName, Class myClass) {
            this.collection = collectionName;
            this.myClass = myClass;
            this.appData = client.appData(collectionName, myClass);
        }


        public AbstractKinveyClientRequest build(AbstractKinveyClientRequest req) {
            try {
                this.appData.getClient().initializeRequest(req);
            } catch (IOException e) {
                e.printStackTrace();
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
            this.query = new Query().equals(AppData.ID_FIELD_NAME, id);
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
            String sourceID = (String) jsonEntity.get(AppData.ID_FIELD_NAME);

            if (sourceID != null) {
                ret = this.appData.new Save(this.myEntity, myClass, sourceID, AppData.SaveMode.PUT);
            } else {
                ret = this.appData.new Save(this.myEntity, myClass, AppData.SaveMode.POST);
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
            this.query = new Query().equals(AppData.ID_FIELD_NAME, entityID);
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
                Preconditions.checkNotNull(null, "Cannot use DELETE without either calling setEntityID(...) or setQuery(...)");
                return null;
            }
            return super.build(ret);
        }
    }


}