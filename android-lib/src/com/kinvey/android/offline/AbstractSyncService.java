/*
 * Copyright (c) 2014, Kinvey, Inc.
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
package com.kinvey.android.offline;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.json.GenericJson;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.Query;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyDeleteResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author edwardf
 */
public abstract class AbstractSyncService extends IntentService{
    protected final String TAG = "Kinvey - SyncService";
    public static final String ACTION_OFFLINE_SYNC = "com.kinvey.android.ACTION_OFFLINE_SYNC";
    private static final String shared_pref = "Kinvey_Offline_Sync";
    private static final String pref_last_failure_at = "last_failure";
    private static final String pref_last_batch_at = "last_batch";

    private Client client;



    public AbstractSyncService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Received intent: " + intent);
        if (isOnline()){
            initClientAndKickOffSync();
        }
    }


    /**
     * Check if the device is connected.
     *
     * @return true if device has connection, false if not
     */
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

    /**
     *  This method creates a client, and after login, will start the sync process.
     */
    private void initClientAndKickOffSync() {

        if (client == null || !client.user().isUserLoggedIn()) {
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

            client.enableDebugLogging();


        } else {
            getFromStoreAndExecute();
        }

    }

    /**
     * This method grabs a list of all collection names from the db helper, iterates through them, and pops all their queues.
     */
    public void getFromStoreAndExecute() {
        new AsyncTask<Void, Void, Void>(){


            @Override
            protected Void doInBackground(Void... voids) {
                DatabaseHandler dbHelper = getDatabaseHandler(client.user().getId());
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
                return null;
            }
        }.execute();


    }

    /**
     * This method uses the AnsycAppData API to execute requests.  It contains an if/else block for the verb of the request
     * and then calls the appropriate appdata method.  As it uses the Async API, every request has a callback.
     * <p/>
     * Dependant on the verb and the result, this method will also update the local database.
     *
     * @param dbHelper wrapper of database implementation
     * @param cur the current offline request to be executed
     * @param collectionName the name of the collection for this request
     */
    private void executeRequest(final DatabaseHandler dbHelper, final OfflineRequestInfo cur, final String collectionName) {


        if (cur.getHttpVerb().equals("PUT") || cur.getHttpVerb().equals(("POST"))) {
            GenericJson entity = dbHelper.getEntity(client, client.appData(collectionName, GenericJson.class), cur.getEntityID());
            if (entity != null){

                client.appData(collectionName, GenericJson.class).save(entity, new KinveyClientCallback<GenericJson>() {
                    @Override
                    public void onSuccess(GenericJson result) {
                        dbHelper.getTable(collectionName).insertEntity(dbHelper, client, result);
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        AbstractSyncService.this.storeCompletedRequestInfo(collectionName, false, cur, error);
                    }
                });
            }else{
                AbstractSyncService.this.storeCompletedRequestInfo(collectionName, false, cur, new NullPointerException());
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
                    AbstractSyncService.this.storeCompletedRequestInfo(collectionName, false, cur, error);
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
                    AbstractSyncService.this.storeCompletedRequestInfo(collectionName, false, cur, error);
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
                    AbstractSyncService.this.storeCompletedRequestInfo(collectionName, false, cur, error);
                    //dbHelper.getTable(collectionName).deleteEntity(dbHelper, cur.getEntityID());
                }
            });
        }
    }


    /**
     * This method is executed after a request has been completed.
     *
     *
     * @param collectionName the name of the collection for this request
     * @param success a boolean indicating if the request succeeded or failed
     * @param info the id and varb of the request
     * @param error instance of an error
     */
    private void storeCompletedRequestInfo(final String collectionName,final boolean success, final OfflineRequestInfo info,final Throwable error) {

        new AsyncTask<Void, Void, Void>(){


            @Override
            protected Void doInBackground(Void... voids) {
                //if request failed on client side, re-queue it
                if (!success && error != null && !(error instanceof HttpResponseException)){
                    Log.i(TAG, "requeing request");
                    DatabaseHandler dbHelper = getDatabaseHandler(client.user().getId());
                    dbHelper.getTable(collectionName).enqueueRequest(dbHelper, info.getHttpVerb(), info.getEntityID());
                    registerFailure();
                }else{
                    Log.i(TAG, "not requeing request");

                }
                return null;
            }
        }.execute();


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
     *
     *
     * @return the time the last batch of requests was executed
     */
    private Long getLastBatchTime(){
        SharedPreferences pref = getSharedPreferences(shared_pref, Context.MODE_PRIVATE);
        long lastBatch = pref.getLong(pref_last_batch_at, Calendar.getInstance().getTimeInMillis() - client.getBatchRate() - 100);
        return lastBatch;

    }

    /**
     * register completion of a batch of sync operations
     *
     */
    private void registerSync(){
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        SharedPreferences.Editor pref = getSharedPreferences(shared_pref, Context.MODE_PRIVATE).edit();
        pref.putLong(pref_last_batch_at, currentTime);
        pref.commit();
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
     * Compare current time to last failure time, and also check sync rate to determine if sync should occur
     *
     * @return true if it has been long enough since last failure to attempt sync again, and the sync rate has passed
     */
    private boolean safeToAttempExecution(){
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        Long lastFail = getLastFailureTime();

        boolean safe = ((lastFail + client.getSyncRate()) < currentTime);
        if (!safe){
            //can short circuit here because it hasn't been long enough since last failure
            return false;
        }
        //sync hasn't failed recently, so check batch timing

        long lastBatch = getLastBatchTime();

        return (lastBatch + client.getBatchRate() < currentTime);

    }

    protected abstract DatabaseHandler getDatabaseHandler(String userid);


}
