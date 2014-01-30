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
package com.kinvey.java.offline;

import com.google.api.client.http.UriTemplate;
import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.cache.Cache;
import com.kinvey.java.core.AbstractKinveyJsonClient;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.core.KinveyClientCallback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Implementation of a Client Request, which can either pull a response from a Cache instance or from online.
 * <p>
 * Behavior is determined by a {@link com.kinvey.java.cache.CachePolicy}, which must be set along with an instance of a {@link com.kinvey.java.cache.Cache}.
 * </p>
 * <p>
 * This class provides all available functionality through public methods, but most of the implementation is handled by
 * the specified cache policy.
 * </p>
 *
 * @author edwardf
 * @since 2.5
 *
 */
public class AbstractKinveyOfflineClientRequest<T> extends AbstractKinveyJsonClientRequest<T> {

    private OfflinePolicy policy = OfflinePolicy.ALWAYS_ONLINE;

    private OfflineStore<T> store = null;

    private Object lock = new Object();

    private String collectionName;


    /**
     * @param abstractKinveyJsonClient kinvey credential JSON client
     * @param requestMethod HTTP Method
     * @param uriTemplate URI template for the path relative to the base URL. If it starts with a "/"
     *        the base path from the base URL will be stripped out. The URI template can also be a
     *        full URL. URI template expansion is done using
     *        {@link com.google.api.client.http.UriTemplate#expand(String, String, Object, boolean)}
     * @param jsonContent POJO that can be serialized into JSON content or {@code null} for none
     * @param responseClass response class to parse into
     * @param collectionName the name of the collection this request is associated with
     */
    protected AbstractKinveyOfflineClientRequest(AbstractKinveyJsonClient abstractKinveyJsonClient,
                                                String requestMethod, String uriTemplate, Object jsonContent, Class<T> responseClass, String collectionName) {
        super(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonContent,
                responseClass);
        this.collectionName = collectionName;
    }


    /**
     * use this method to set a cache and a caching policy for this specific client request.
     *
     * @param store - an implementation of an offline store to use
     * @param policy - the offline policy to use for this individual request.
     */
    protected void setStore(OfflineStore store, OfflinePolicy policy){
        this.policy = policy;
        this.store = store;
    }

    /**
     * This method retrieves an entity from the offline store.
     * <P/>
     * This method is synchronized on an object lock, providing threadsafe access to the store.
     * <P/>
     * @return an entity or null, from the store
     * @throws IOException
     */
    public T offlineFromStore() throws IOException{
        synchronized (lock) {
            String verb = getRequestMethod();
            T ret = null;
            if (verb.equals("GET")){
                ret = this.store.executeGet((AbstractClient)getAbstractKinveyClient(), ((AbstractClient) getAbstractKinveyClient()).appData(this.collectionName, this.getResponseClass()), this);
            }else if (verb.equals("PUT")){
                ret = this.store.executeSave((AbstractClient)getAbstractKinveyClient(), ((AbstractClient) getAbstractKinveyClient()).appData(this.collectionName, this.getResponseClass()), this);
            }else if (verb.equals("POST") && !UriTemplate.expand(getAbstractKinveyClient().getBaseUrl(), this.getUriTemplate(), this, false).contains("_group")){
                //generate and add id
                ((GenericJson) this.getJsonContent()).put("_id", getUUID());
                ret = this.store.executeSave((AbstractClient)getAbstractKinveyClient(), ((AbstractClient) getAbstractKinveyClient()).appData(this.collectionName, this.getResponseClass()), this);
            }else if (verb.equals("DELETE")){
                ret = (T) this.store.executeDelete((AbstractClient)getAbstractKinveyClient(), ((AbstractClient) getAbstractKinveyClient()).appData(this.collectionName, this.getResponseClass()), this);
            }else{
                System.out.println("Kinvey Offline, unrecognized verb in store! -> " + verb );
            }

            return ret;



        }
    }


    /**
     * This method retrieves an entity from the service.  If assumeOnline is true, then the method will throw any connection errors.
     * If assumeOnline is false, then the method will catch the error and attempt to return the entity from the store.
     *
     * @param assumeOnline - should execution assume the user is online (and return errors if they aren't)?
     * @return an entity from the online collection
     * @throws IOException
     */
    public T offlineFromService(boolean assumeOnline) throws IOException{
        this.store.kickOffSync();
        if (assumeOnline){

            return super.execute();
        }

        T ret = null;
        if(((AbstractClient) getAbstractKinveyClient()).appData(collectionName, getResponseClass()).isOnline()){
            System.out.println("Offline Request - Online execution!");
           
                ret = super.execute();
                if (ret != null){
                    if (ret.getClass().isArray()){
                        this.offlineFromStore();
                    }else{
                        this.store.insertEntity((AbstractClient)getAbstractKinveyClient(), ((AbstractClient) getAbstractKinveyClient()).appData(this.collectionName, this.getResponseClass()), ret);
                    }

                }
       
         }

        return ret;
    }

    @Override
    public T execute() throws IOException{

        T ret =  policy.execute(this);
        this.store.kickOffSync();
        return ret;
    }

    /**
     * Generate a unique mongodb style id.
     * <p/>
     * Offline requires all entities to have an `_id` field, so if one doesn't exist this method can be used to generate one
     *
     * @return a unique String id
     */
    public static String generateMongoDBID() {
        //from: https://github.com/mongodb/mongo-java-driver/blob/master/src/main/org/bson/types/ObjectId.java

        int _time = (int) (System.currentTimeMillis() / 1000);
        int _machine = 1;
        int _inc = 25;


        byte b[] = new byte[12];
        ByteBuffer bb = ByteBuffer.wrap(b);
        // by default BB is big endian like we need
        bb.putInt(_time);
        bb.putInt(_machine);
        bb.putInt(_inc);

        StringBuilder buf = new StringBuilder(24);

        for (int i = 0; i < b.length; i++) {
            int x = b[i] & 0xFF;
            String s = Integer.toHexString(x);
            if (s.length() == 1)
                buf.append("0");
            buf.append(s);
        }
        return buf.toString();
    }

    public static String getUUID(){
        String id = UUID.randomUUID().toString();
        String ret = id.replace("-", "");
        return ret;

    }




}
