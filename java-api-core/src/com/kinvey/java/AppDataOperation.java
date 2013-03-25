/*
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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
import com.kinvey.java.model.AggregateEntity;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.query.MongoQueryFilter;

/**
 * Builder for advanced App Data requests
 *
 * @author edwardf
 * @since 2.0.2
 */
public class AppDataOperation<T> {


    private abstract class AppDataRequestBuilder {


        //Required
        protected String collection;
        protected Class myClass;
        protected AppData<T> appData;


        public AppDataRequestBuilder(String collectionName, Class myClass, AppData appData) {
            this.collection = collectionName;
            this.myClass = myClass;

            this.appData = appData;
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

    private abstract class KRAppDataRequestBuilder extends AppDataRequestBuilder {

        //Kinvey Reference Support
        protected String[] resolves = null;
        //set defaults for kinvey reference
        //note the above `resolves` determines if Kinvey References are used.
        protected int resolveDepth = 2;
        protected boolean retainReference = true;

        public KRAppDataRequestBuilder(String collectionName, Class myClass, AppData appData) {
            super(collectionName, myClass, appData);
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

    public class GetBuilder extends KRAppDataRequestBuilder {
        protected Query query = null;

        public GetBuilder(String collectionName, Class myClass, AppData appData) {
            super(collectionName, myClass, appData);
        }


        public AppDataRequestBuilder setQuery(Query query) {
            this.query = query;
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


    }

    public class GetEntityBuilder extends KRAppDataRequestBuilder {
        protected String entityID = null;

        public GetEntityBuilder(String collectionName, Class myClass, AppData appData) {
            super(collectionName, myClass, appData);
        }

        public AppDataRequestBuilder setEntityID(String entityID) {
            this.entityID = entityID;
            return this;
        }

        public AbstractKinveyClientRequest build() {
            AbstractKinveyClientRequest ret = null;


            if (this.entityID != null) {
                if (resolves == null) {
                    ret = this.appData.new GetEntity(this.entityID, this.myClass);
                } else {
                    ret = this.appData.new GetEntity(this.entityID, this.myClass, resolves, resolveDepth, retainReference);
                }
            }


            return super.build(ret);
        }

    }

    public class SaveBuilder extends AppDataRequestBuilder {
        protected T myEntity = null;

        public SaveBuilder(String collectionName, Class myClass, AppData appData) {
            super(collectionName, myClass, appData);
        }

        public AppDataRequestBuilder setEntity(T myEntity) {
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

    public class DeleteBuilder extends AppDataRequestBuilder {
        protected String entityID = null;
        protected Query query = null;

        public DeleteBuilder(String collectionName, Class myClass, AppData appData) {
            super(collectionName, myClass, appData);
        }


        public AppDataRequestBuilder setEntityID(String entityID) {
            this.entityID = entityID;
            return this;
        }


        public AppDataRequestBuilder setQuery(Query query) {
            this.query = query;
            return this;
        }

        public AbstractKinveyClientRequest build() {

            AbstractKinveyClientRequest ret = null;

            if (this.entityID != null) {
                ret = this.appData.new Delete(this.entityID);

            } else if (this.query != null) {
                ret = this.appData.new Delete(this.query);
            } else {
                Preconditions.checkNotNull(null, "Cannot use DELETE without either calling setEntityID() or setQuery()");
                return null;
            }

            return super.build(ret);

        }
    }


}