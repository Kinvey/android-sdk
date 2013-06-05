package com.kinvey.android;

import com.google.api.client.json.GenericJson;
import com.google.common.base.Preconditions;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.AppData;
import com.kinvey.java.AppDataOperation;
import com.kinvey.java.Query;
import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.core.KinveyClientCallback;

import java.io.IOException;

/**
 * Builder for asynchronous app data requests.
 * <p>
 * This class uses the Builder pattern to allow extensible use of all the features of our Android async app data API.  There are
 * various `set*()` methods, which can be chained together to create a builder.  Once the builder has been configured,
 * a call to `myBuilder.build()` will return an asyncronous request.  By calling `myBuilder.build().execute()` the
 * the request will be constructed and executed on a new thread.  The builders provides `setCallback(new {@code KinveyClientCallback}{...})`
 * for getting results of asynchronous operaitons.
 * </p>
 * <p>
 * `myBuilder.build()` returns an asyncronous request for app data from a collection.  This class provides multiple
 * implementations for various CRUD interactions.
 * </p>
 * <p>
 * The code below will build and execute an asyncronous get entity request.
 *
 * </p>
 * <p>
 * <pre>
 * {@code
 *     MyEntity myEntity = new BlockingGetEntityBuilder("myCollection", MyEntity.class, AppData.this)
 *             .setEntityID(myEntity.getId());
 *             .setResolves(new String[]{"myOtherCollectionReference1", myOtherCollectionReference2})
 *             .setResolveDepth(2)
 *             .setCallback(new KinveyClientCallback<MyEntity> {
 *                 public void onSuccess(MyEntity result) {
 *                     Log.i(TAG, "got it!");
 *                 }
 *
 *                 public void onFailure(Throwable error) {
 *                     Log.i(TAG, "oh no!");
 *                 }
 *             })
 *             .buildAndExecute();
 * }
 * </pre>
 * </p>
 *
 * @author edwardf
 * @since 2.2.0
 *
 */
public class AsyncAppDataOperation extends AppDataOperation {

    private abstract class AsyncAppDataRequestBuilder extends AppDataRequestBuilder {

        private KinveyClientCallback callback;

        public AsyncAppDataRequestBuilder(Client client, String collectionName, Class myClass) {

            super(client, collectionName, myClass);
        }

        public AsyncAppDataRequestBuilder setCallback(KinveyClientCallback callback) {
            this.callback = callback;
            return this;
        }

        public AbstractKinveyClientRequest buildAndExecute(AbstractKinveyClientRequest req) {
//            try {
                 //TODO edwardf figure out how much can be abstracted into just one call here.  Might only need save declaration.
//
//               // this.appData.getClient().initializeRequest(req);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            return null;
        }
    }

//    /**
//     * Abstract KR (Kinvey Reference) App Data Request Builder parent introduces resolves, resolvedepth and retain references.
//     */
//    private static abstract class AsyncKRAppDataRequestBuilder extends KRAppDataRequestBuilder {
//
//        //Kinvey Reference Support
//        protected String[] resolves = null;
//        //set defaults for kinvey reference
//        //note the above `resolves != null` determines if Kinvey References are used.
//        protected int resolveDepth = 1;
//        protected boolean retainReference = true;
//
//        public AsyncKRAppDataRequestBuilder(String collectionName, Class myClass, AsyncAppData appData) {
//            super(collectionName, myClass, appData);
//        }
//
//        public AppDataRequestBuilder setResolves(String[] resolves) {
//            this.resolves = resolves;
//            return this;
//        }
//
//        public AppDataRequestBuilder setResolveDepth(int depth) {
//            this.resolveDepth = depth;
//            return this;
//        }
//
//        public AppDataRequestBuilder setRetainReferences(boolean retain) {
//            this.retainReference = retain;
//            return this;
//        }
//
//
//    }
//
//
//    /**
//     * Builder for creating new GET requests with the core App Data API.
//     */
//    public static class GetBuilder extends AsyncKRAppDataRequestBuilder {
//        protected Query query = null;
//
//        public GetBuilder(String collectionName, Class myClass, AsyncAppData appData) {
//            super(collectionName, myClass, appData);
//        }
//
//
//        public AppDataRequestBuilder setQuery(Query query) {
//            this.query = query;
//            return this;
//        }
//
//        public AbstractKinveyClientRequest build() {
//            AbstractKinveyClientRequest ret = null;
//            if (this.query == null) {
//                this.query = new Query();
//            }
//
//            if (resolves == null) {
//                ret = this.appData.new Get(this.query, this.myClass);
//            } else {
//                ret = this.appData.new Get(this.query, this.myClass, resolves, resolveDepth, retainReference);
//            }
//
//
//            return super.build(ret);
//        }
//
//
//    }
//
//
//
//    /**
//     * Builder for creating new GET ENTITY requests with the core App Data API.
//     */
//    public static class GetEntityBuilder extends AsyncKRAppDataRequestBuilder {
//        protected String entityID = null;
//
//        public GetEntityBuilder(String collectionName, Class myClass, AsyncAppData appData) {
//            super(collectionName, myClass, appData);
//        }
//
//        public AppDataRequestBuilder setEntityID(String entityID) {
//            this.entityID = entityID;
//            return this;
//        }
//
//        public AbstractKinveyClientRequest build() {
//            AbstractKinveyClientRequest ret = null;
//
//
//            if (this.entityID != null) {
//                if (resolves == null) {
//                    ret = this.appData.new GetEntity(this.entityID, this.myClass);
//                } else {
//                    ret = this.appData.new GetEntity(this.entityID, this.myClass, resolves, resolveDepth, retainReference);
//                }
//            } else{
//                Preconditions.checkNotNull(null, "Cannot use GET ENTITY without calling setEntityID()");
//                return null;
//            }
//
//
//            return super.build(ret);
//        }
//
//    }
//
//
//    /**
//     * Builder for creating new SAVE requests with the core App Data API.
//     */
//    public static class SaveBuilder extends AsyncAppDataRequestBuilder {
//        protected Object myEntity = null;
//
//        public SaveBuilder(String collectionName, Class myClass, AsyncAppData appData) {
//            super(collectionName, myClass, appData);
//        }
//
//        public AppDataRequestBuilder setEntity(Object myEntity) {
//            this.myEntity = myEntity;
//            return this;
//        }
//
//        public AsyncClientRequest build() {
//            Preconditions.checkNotNull(this.myEntity, "Cannot use SAVE without first calling setEntity(myEntity)");
//
//
//            AbstractKinveyClientRequest ret = null;
//
//
//            GenericJson jsonEntity = (GenericJson) this.myEntity;
//            String sourceID = (String) jsonEntity.get(AppData.ID_FIELD_NAME);
//
//            if (sourceID != null) {
//                ret = this.appData.new Save(this.myEntity, myClass, sourceID, AppData.SaveMode.PUT);
//            } else {
//                ret = this.appData.new Save(this.myEntity, myClass, AppData.SaveMode.POST);
//            }
//            return super.build(ret);
//
//        }
//
//    }
//
//
//    /**
//     * Builder for creating new DELETE requests with the core App Data API.
//     */
//    public static class DeleteBuilder extends AsyncAppDataRequestBuilder {
//        protected String entityID = null;
//        protected Query query = null;
//
//        public DeleteBuilder(String collectionName, Class myClass, AsyncAppData appData) {
//            super(collectionName, myClass, appData);
//        }
//
//
//        public AppDataRequestBuilder setEntityID(String entityID) {
//            this.entityID = entityID;
//            return this;
//        }
//
//
//        public AppDataRequestBuilder setQuery(Query query) {
//            this.query = query;
//            return this;
//        }
//
//        public AbstractKinveyClientRequest build() {
//
//            AbstractKinveyClientRequest ret = null;
//
//            if (this.entityID != null) {
//                ret = this.appData.new Delete(this.entityID);
//
//            } else if (this.query != null) {
//                ret = this.appData.new Delete(this.query);
//            } else {
//                Preconditions.checkNotNull(null, "Cannot use DELETE without either calling setEntityID() or setQuery()");
//                return null;
//            }
//
//            return super.build(ret);
//
//        }
//
//
//        public AsyncAppDataRequestBuilder setCallback(KinveyDeleteCallback callback) {
//            this.callback = callback;
//            return this;
//        }
//
//
//    }
//
//
//











}
