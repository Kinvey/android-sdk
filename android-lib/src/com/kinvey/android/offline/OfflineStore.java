/*
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
package com.kinvey.android.offline;

import android.content.Context;
import android.util.Log;
import com.google.api.client.json.GenericJson;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * <p>OfflineStore class.</p>
 * <p>
 * The instance can be accessed through OfflineStore.getInstance().
 * It maintains both the latest state of all entities as well as a queue of REST requests made while offline.
 * </p>
 * <p>
 * The store persists to disk, and this class provides methods to force reading and writing.This class does two things:
 * </p>
 * <p>
 * store a copy of the local state of data and queue REST requests for later online execution.
 * </p>
 *
 * @author edwardf
 * @since 2.0
 * @version $Id: $
 */
public class OfflineStore<T> extends Observable {

    private int versionCode = 1;  //Increment this number every time edits are made to persistence.

    /**
     * The datastore maps an Entity's _id to an instance of an entity.  This represents the latest local state of the Entity,
     * which can be synced.
     */
    private HashMap<String, T> dataStore;

    /**
     * The requestStore is a queue of the requests performed while in offline mode.
     * <p/>
     * The RequestInfo class maintains an HTTP Verb as well as a String _id of the associated entity.
     */
    private Queue<OfflineRequestInfo> requestStore;

    /**
     * The queryStore maps a Query string to a list of Entity's _id.  Queries can only be performed on Get operations, so it is the
     * responsibility of the OfflineAppDataService to iterate through all results and update them in the dataStore.
     */
    private HashMap<String, List<String>> queryStore;



    //Query and aggregate support require another two HashMaps, one for each, mapping a String to a List of Strings.
    //The String Key is the query(aggregrate) itself, and the list of Strings (Value) represent all _ids associated with the query/aggregate.



    //When persisting to disk the Collection name is appended to form a unique file name in local storage
    private static final String FILENAME = "Kinvey_offline_";

    //this name of the collection this instance is associated with
    private String collectionName;

    private Context context;

    private OfflineSettings settings;




    public Class getMyClass() {
        return myClass;
    }

    public void setMyClass(Class myClass) {
        this.myClass = myClass;
    }

    private Class myClass;

    //a list of RequestInfo of the client requests that succeeded.
    private ArrayList<OfflineRequestInfo> successfulCalls;
    //a list of RequestInfo of the client requests that failed.
    private ArrayList<OfflineRequestInfo> failedCalls;

    private static HashMap<String, OfflineStore> storeMap;

    public static OfflineStore getStore(Context context, String collectionName, Class myClass){
        if (storeMap == null){
            storeMap = new HashMap<String, OfflineStore>();
        }

        if (!storeMap.containsKey(collectionName)){
            storeMap.put(collectionName, new OfflineStore(context, collectionName, myClass));
        }

        return storeMap.get(collectionName);


    }



    /**
     * <p>Constructor for OfflineStore.</p>
     *
     * @param context a {@link android.content.Context} object.
     * @param collectionName a {@link java.lang.String} object.
     */
    private OfflineStore(Context context, String collectionName, Class myClass) {
//        this.dataStore = new HashMap<String, T>();
//        this.requestStore = new LinkedList<RequestInfo>();
        this.collectionName = collectionName;
        this.context = context;
        this.myClass = myClass;
        loadOfflineSettings();
        this.settings.getCollectionSet().add(collectionName);
        this.settings.savePreferences();

        loadOrCreateStore();
        Log.v(Client.TAG,  "Offline Store constructor finished.");

    }

    private void loadOfflineSettings() {
        this.settings = OfflineSettings.getInstance(context);
    }


    private void loadOrCreateStore() {
        Log.v(Client.TAG, "Loading or Creating Store!");

        FileInputStream fis = null;
        try {
            fis = context.openFileInput(FILENAME + collectionName);
        } catch (FileNotFoundException e) {
            //Offline store doesn't exist so we need to create it.
            Log.v(Client.TAG, "offline store file doesn't exist, creating it!");
            this.dataStore = new HashMap<String, T>();
            this.requestStore = new LinkedList<OfflineRequestInfo>();
            this.successfulCalls = new ArrayList<OfflineRequestInfo>();
            this.failedCalls = new ArrayList<OfflineRequestInfo>();
            this.queryStore = new HashMap<String, List<String>>();
            writeStore(this.context);
            return;
        }
        Log.v(Client.TAG, "offline store already exists, so attempting to load");

        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(fis);
            this.dataStore = (HashMap<String, T>) ois.readObject();
            this.requestStore = (Queue<OfflineRequestInfo>) ois.readObject();
            this.successfulCalls = (ArrayList<OfflineRequestInfo>) ois.readObject();
            this.failedCalls = (ArrayList<OfflineRequestInfo>) ois.readObject();
            try{
                this.myClass = (Class) ois.readObject();
            }catch (Exception e){}
            try{
                this.queryStore = (HashMap<String, List<String>>) ois.readObject();
            }catch (Exception e){
                this.queryStore = new HashMap<String, List<String>>();
            }


            Log.v(Client.TAG, "read in datastore and request store! -> " + this.dataStore.size() + ", " + this.requestStore.size());

        } catch (IOException e) {
            Log.e(Client.TAG, "" + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.e(Client.TAG, "" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (ois != null)
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }


    }

    /**
     * writes the store to disk
     *
     * @param context the context associated with internal storage on the Android device.
     */
    private void writeStore(Context context) {
        try {
            Log.v(Client.TAG, "about to write datastore and request store! -> " + this.dataStore.size() + ", " + this.requestStore.size());
            FileOutputStream out = context.openFileOutput((FILENAME + collectionName), Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(out);
            settings.savePreferences();
            os.writeObject(this.dataStore);
            os.writeObject(this.requestStore);
            os.writeObject(this.successfulCalls);
            os.writeObject(this.failedCalls);
            if (this.myClass != null){
                os.writeObject(this.myClass);
            }else{
                Log.e(Client.TAG, "offline class is set to null");
            }
            os.writeObject(this.queryStore);

            Log.v(Client.TAG, "wrote datastore and request store! -> " + this.dataStore.size() + ", " + this.requestStore.size());
            os.flush();
            out.getFD().sync();
            os.close();

        } catch (FileNotFoundException e) {
            Log.e(Client.TAG, "Error accessing internal storage!");
        } catch (IOException e) {
            Log.e(Client.TAG, "IOException while creating objectOutputStream to save offline store!");
            e.printStackTrace();
        }
    }

    /**
     * Queue a GET request for execution.
     *
     * The callback onSuccess() indicates successful retrieval from the offline store.
     * The callback onFailure() indicated failed retrieval from the offline store.
     *
     * The GET request is then queued for execution when a connection is restored.
     *
     * @param entityID - the unique ID of the entity to retrieve
     * @param callback - Used to indicate result of offline storage (not online execution).
     */
    public void getEntity(String entityID, KinveyClientCallback callback) {
        loadOrCreateStore();

        Log.v(Client.TAG, "getting entity from offline store, which currently has: " + this.dataStore.size());
        T curState = this.dataStore.get(entityID);
        if (callback != null) {
            callback.onSuccess(curState);
        }
        //if entity is null, call onFailure?

        addToQueue("GET", entityID );
        writeStore(this.context);
    }

    public void get(Query q, String jsonQuery, KinveyListCallback callback){
        loadOrCreateStore();

        Log.v(Client.TAG, "getting entity from offline store, which currently has: " + this.dataStore.size());
        List<String> ids = this.queryStore.get(jsonQuery);

        if(ids != null){
            if (callback != null){
                callback.onSuccess(null);//TODO edwardf gotta find a better way.
            }



        List<T> values = new ArrayList<T>();

        for (String id : ids){
            values.add(this.dataStore.get(id));
        }

//        T curState = this.dataStore.get(entityID);
        if (callback != null) {
//            Class responseArray;
////                java.lang.reflect.Array.newInstance(responseClass, 0).getClass();
//            try {
//                responseArray = Class.forName("[L" + getMyClass().getName() + ";");
//            } catch (Exception e) {
//                Log.i(Client.TAG, "NOPE ON CREATING THAT ARRAY");
//                responseArray = getMyClass();
//            }
            T[] ret = (T[]) Array.newInstance(myClass, 0);



            callback.onSuccess(values.toArray(ret));
        }
        }else{
            callback.onSuccess(null);
        }
        //if entity is null, call onFailure?

        addToQueue("GETQUERY", q, jsonQuery);
        writeStore(this.context);

    }


    /**
     * returns an entity directly from the datastore
     *
     * @param id - the unique id of the entity
     * @return the entity OR {@code null}
     */
    public T GetEntityFromDataStore(String id) {
        loadOrCreateStore();

        return this.dataStore.get(id);

    }

    /**
     * Queue a PUT request for execution.
     *
     * The callback onSuccess() indicates successful insertion into the offline store.
     * The callback onFailure() indicated failed insertion into the offline store.
     *
     * The PUT request is then queued for execution when a connection is restored.
     *
     * @param entity - the entity to put in the offline store
     * @param callback - Used to indicate result of offline storage (not online execution).
     */
    public void save(T entity, KinveyClientCallback callback) {
        loadOrCreateStore();

        String id = (String) ((GenericJson) entity).get("_id");
        if (id == null) {
            id = generateMongoDBID();
        }
        Log.v(Client.TAG, "offline saving -> " + id);

        addToQueue("PUT", id);

        addToStore(id, entity);
        T curState = this.dataStore.get(id);
        if (callback != null){
            callback.onSuccess(curState);
        }
        writeStore(this.context);
    }


    /**
     * put an entity directly in the data store.
     * <p>
     * This method will notify all observers.
     * </p>
     *
     * @param entityID - the unique id is used as a key
     * @param toAdd - the entity to put in the store.
     */
    public void addToStore(String entityID, T toAdd){
        loadOrCreateStore();
        this.dataStore.put(entityID, toAdd);
        Log.i(Client.TAG, "added to the store " + entityID + " and " + this.dataStore.size());
        writeStore(this.context);
    }

    public void addQuery(Query query, String querystring, List<String> ids){

        loadOrCreateStore();
        this.queryStore.put(querystring, ids);
        Log.i(Client.TAG, "added to the store " + querystring + " and " + this.queryStore.size());
        writeStore(this.context);


    }


    /**
     * put an entity directly in the request queue for later execution.
     * <p>
     * This method will notify all observers.
     * </p>
     *
     * @param httpVerb - The verb of the pending request.
     * @param entityID - the ID of the entity to apply this verb too
     */
    public void addToQueue(String httpVerb, String entityID){
        loadOrCreateStore();

        this.requestStore.add(new OfflineRequestInfo(httpVerb, entityID));
        writeStore(this.context);
    }

    /**
     * put an entity directly in the request queue for later execution.
     * <p>
     * This method will notify all observers.
     * </p>
     *
     * @param httpVerb - The verb of the pending request.
     * @param query - a query to execute
     */
    public void addToQueue(String httpVerb, Query query, String queryjson){
        loadOrCreateStore();

        this.requestStore.add(new OfflineRequestInfo(httpVerb, query, queryjson));
        writeStore(this.context);
    }


    /**
     * Queue a DELETE request for execution.
     *
     * The callback onSuccess() indicates successful removal from the offline store.
     * The callback onFailure() indicated failed removal from the offline store.
     *
     * The DELETE request is then queued for execution when a connection is restored.
     *
     * @param entityID - the ID entity to delete
     * @param callback - Used to indicate result of offline storage (not online execution).
     */
    public void delete(String entityID, KinveyDeleteCallback callback) {
        loadOrCreateStore();
        T curState = this.dataStore.get(entityID);
        addToQueue("Delete", entityID);
        addToStore(entityID, null);
        writeStore(this.context);
    }

    /**
     * This method return the RequestInfo instance at the top of the queue.
     *
     * @return information about the first client request that has been queued OR {@code null} if empty.
     */
    public OfflineRequestInfo pop() {
        loadOrCreateStore();
        OfflineRequestInfo ret =  this.requestStore.poll();
        writeStore(this.context);
        return ret;
    }

    public OfflineSettings getSettings() {
        return settings;
    }

    public void setSettings(OfflineSettings settings) {
        this.settings = settings;
    }






    private static String generateMongoDBID() {
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


    public int getRequestStoreCount(){
        return this.requestStore.size();
    }

    public int getEntityStoreCount(){
        return this.dataStore.size();
    }

    public void notifyExecution(String collection, boolean success, OfflineRequestInfo info, Object response) {
        if (success) {
            this.successfulCalls.add(info);
        } else {
            this.failedCalls.add(info);
        }
        writeStore(this.context);
        setChanged();
        notifyObservers(new OfflineResponseInfo(info, response, success, collection));

    }

    public ArrayList<OfflineRequestInfo> getFailedCalls() {
        return failedCalls;
    }



    public ArrayList<OfflineRequestInfo> getSuccessfulCalls() {
        return successfulCalls;
    }




}
