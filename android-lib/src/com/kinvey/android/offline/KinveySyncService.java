/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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
package com.kinvey.android.offline;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonGenerator;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.Query;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.offline.*;

import java.io.StringWriter;
import java.util.*;

/**
 *
 * This Android Service listens for intents and uses the {@link com.kinvey.android.AsyncAppData} API to execute requests
 *
 * @author edwardf
 * @since 2.0
 */
public class KinveySyncService extends IntentService {

    public static final String ACTION_OFFLINE_SYNC = "com.kinvey.android.ACTION_OFFLINE_SYNC";
    private static final String shared_pref = "Kinvey_Offline_Sync";
    private static final String pref_last_failure_at = "last_failure";

    //allows clients to bind
    private final IBinder mBinder = new KBinder();
    private final String TAG = "Kinvey - SyncService";

    private Client client;

    public KinveySyncService(String name) {
        super(name);

    }

    public KinveySyncService() {
        super("Kinvey Sync Service");

    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void pingService() {
        Log.i(TAG, "\"Hi!\" said the Kinvey Sync Service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, startId, startId);
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        onHandleIntent(intent);

        return START_STICKY;
    }

    /**
     * This class is called by the Android OS, when a registered intent is fired off.
     * <p/>
     * Check <intent-filter> elements in the Manifest.xml to see which intents are handled by this method.
     *
     * @param intent the intent to act upon.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (isOnline()) {
            initClientAndKickOffSync();
        }
    }

    public boolean isOnline() {

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            Log.v(Client.TAG, "Kinvey Sync Execution is gonna happen!  Device connected.");
            return true;
        }
        Log.v(Client.TAG, "Kinvey Sync Execution is not happening, device is offline.");

        return false;


    }

    private void initClientAndKickOffSync() {

        if (client == null) {
            client = new Client.Builder(getApplicationContext()).setRetrieveUserCallback(new KinveyUserCallback() {
                @Override
                public void onSuccess(User result) {
                    Log.i(TAG, "offline Logged in as -> " + client.user().getUsername() + " (" + client.user().getId() +")");
                    getFromStoreAndExecute();
                }

                @Override
                public void onFailure(Throwable error) {
                    Log.e(TAG, "offline Unable to login from Kinvey Sync Service! -> " + error);
                }
            }).build();
            //client.enableDebugLogging();


        } else {
            getFromStoreAndExecute();
        }

    }

    public void getFromStoreAndExecute() {
        OfflineHelper dbHelper = OfflineHelper.getInstance(getApplicationContext());
        List<String> collectionNames = dbHelper.getCollectionTables();


        for (String s : collectionNames) {
            boolean done = false;

            while (!done && safeToAttempExecution()){
                OfflineRequestInfo req = dbHelper.getTable(s).popSingleQueue(dbHelper);
                if (req == null){
                    done = true;
                }else{
                    executeRequest(dbHelper, req, s);
                }
            }
        }
    }

    private void executeRequest(final OfflineHelper dbHelper, final OfflineRequestInfo cur, final String collectionName) {


        if (cur.getHttpVerb().equals("PUT") || cur.getHttpVerb().equals(("POST"))) {
            GenericJson entity = dbHelper.getEntity(client, client.appData(collectionName, GenericJson.class), cur.getEntityID());
            if (entity != null){

                client.appData(collectionName, GenericJson.class).save(entity, new KinveyClientCallback<GenericJson>() {
                    @Override
                    public void onSuccess(GenericJson result) {
//                      KinveySyncService.this.storeCompletedRequestInfo(collectionName, true, cur, result);
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        KinveySyncService.this.storeCompletedRequestInfo(collectionName, false, cur, error);
                    }
                });
            }else{
                KinveySyncService.this.storeCompletedRequestInfo(collectionName, false, cur, new NullPointerException());
            }
        } else if (cur.getHttpVerb().equals("GET")){
            client.appData(collectionName, GenericJson.class).getEntity(cur.getEntityID(), new KinveyClientCallback<GenericJson>() {
                @Override
                public void onSuccess(GenericJson result) {
//                    KinveySyncService.this.storeCompletedRequestInfo(collectionName, true, cur, result);
                    //update datastore with response
                    dbHelper.getTable(collectionName).insertEntity(dbHelper, client, result);
                }

                @Override
                public void onFailure(Throwable error) {
                    KinveySyncService.this.storeCompletedRequestInfo(collectionName, false, cur, error);
                }
            });
        } else if (cur.getHttpVerb().equals("DELETE")){

            client.appData(collectionName, GenericJson.class).delete(cur.getEntityID(), new KinveyDeleteCallback() {
                @Override
                public void onSuccess(KinveyDeleteResponse result) {
//                    KinveySyncService.this.storeCompletedRequestInfo(collectionName, true, cur, result);
                }

                @Override
                public void onFailure(Throwable error) {
                    KinveySyncService.this.storeCompletedRequestInfo(collectionName, false, cur, error);
                }
            });
        }else if (cur.getHttpVerb().equals("QUERY")){

            Query q = new Query();
            q.setQueryString(cur.getEntityID());

            client.appData(collectionName, GenericJson.class).get(q, new KinveyListCallback<GenericJson>()
            {
                @Override
                public void onSuccess(GenericJson[] result) {
                    List<String> resultIds = new ArrayList<String>();

                    for (GenericJson res : result){
//                        KinveySyncService.this.storeCompletedRequestInfo(collectionName, true, cur, res);
                        //update datastore with response
                        dbHelper.getTable(collectionName).insertEntity(dbHelper, client, res);
                        resultIds.add(res.get("_id").toString());
                    }

                    dbHelper.getTable(collectionName).storeQueryResults(dbHelper, cur.getEntityID(), resultIds);
                }

                @Override
                public void onFailure(Throwable error) {
                    KinveySyncService.this.storeCompletedRequestInfo(collectionName, false, cur, error);
                    //dbHelper.getTable(collectionName).deleteEntity(dbHelper, cur.getEntityID());
                }
            });
        }
    }

    private void storeCompletedRequestInfo(String collectionName, boolean success, OfflineRequestInfo info, Throwable error) {
         //  Might want this someday but not yet

        OfflineHelper dbHelper = OfflineHelper.getInstance(getApplicationContext());
//        dbHelper.getTable(collectionName).storeCompletedRequestInfo(dbHelper, collectionName, success, info, returnValue);

        //if request failed on client side, re-queue it
        if (!success && !(error instanceof HttpResponseException)){
            Log.i(TAG, "requeing request");
            dbHelper.getTable(collectionName).enqueueRequest(dbHelper, info.getHttpVerb(), info.getEntityID());
            registerFailure();
        }

    }

//    @Deprecated
//    private void storeCompletedRequestInfo(String collectionName, boolean success, OfflineRequestInfo info, GenericJson returnValue) {
//        Might want this someday but not yet
//        String jsonResult = "";
//        StringWriter writer = new StringWriter();
//        try {
//            JsonGenerator generator = client.getJsonFactory().createJsonGenerator(writer);
//            generator.serialize(returnValue);
//            generator.flush();
//            jsonResult = writer.toString();
//        } catch (Exception ex) {
//            Log.e(TAG, "unable to serialize JSON! -> " + ex);
//        }
//
//        storeCompletedRequestInfo(collectionName, success, info, jsonResult);


//    }


    /**
     * Binder coupled with this Service
     *
     */
    public class KBinder extends Binder{
        public KinveySyncService getService(){
            return KinveySyncService.this;
        }

    }

    private HashMap<String, String> getSyncSettings(){
        HashMap<String, String> ret = new HashMap<String, String>();

        SharedPreferences pref = getSharedPreferences(shared_pref, Context.MODE_PRIVATE);




        return ret;
    }

    /**
     * @return the time of the last failure, or a safe number if there haven't been any
     */
    private Long getLastFailureTime(){

        SharedPreferences pref = getSharedPreferences(shared_pref, Context.MODE_PRIVATE);
        long lastFail = pref.getLong(pref_last_failure_at, Calendar.getInstance().getTimeInMillis() - client.getSyncRate() - 100);

        return lastFail;
    }

    /**
     * Register a retryable (client-side) failure time
     *
     */
    private void registerFailure(){
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        SharedPreferences.Editor pref = getSharedPreferences(shared_pref, Context.MODE_PRIVATE).edit();
        pref.putLong(pref_last_failure_at, currentTime);
        pref.commit();

    }

    /**
     * Comapre current time to last failure and sync rate to determine if sync should occur
     *
     * @return
     */
    private boolean safeToAttempExecution(){
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        Long lastFail = getLastFailureTime();

        boolean safe = ((lastFail + client.getSyncRate()) < currentTime);
        //Log.e("OK", "is it safe -> " + safe);
        return safe;

    }

}
