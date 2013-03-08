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
package com.kinvey.android;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import com.google.api.client.json.GenericJson;
import com.google.common.base.Preconditions;

import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyDeleteResponse;

import java.util.ArrayList;

/**
 * @author edwardf
 * @since 2.0
 *
 *
 * This IntentService can be used to execute App Data client requests.  The current implementation is coupled with an
 * OfflineStore, -- this class is listening for an intent OFFLINE_SYNC that is kicked off by the store when a new client
 * request is queued up.
 *
 * After receiving that intent from the Store, if the device is connected, it will begin sync.
 *
 * There are two other intents-- Wifi Network State Change and Connectivity State Change.  Listening for both of these
 * allows the class to determine if sync should only occur when connected to WIFI or if normal cell data networks will suffice.
 *
 * This executor pulls from the store in 'batches', and executes a batch of client requests asynchronously.
 * The size of this batch represents the number of service calls to kick off at the same time, and is configurable.
 *
 * This Service also supports staggering calls, so that there is a delay between kicking off batches.
 *
 *
 *
 */
public class OfflineAppDataService extends IntentService {

    //Intent used to acknowledge offline sync.
    public static final String ACTION_OFFLINE_SYNC = "ACTION_OFFLINE_SYNC";

    //The number of milliseconds between each batch of client requests being executed.
    private long staggerTime = 1000L;
    //a flag indicating if the service should only execute calls on WIFI or if any network will suffice.
    private boolean requireWIFI = false;
    //The size of a batch, indicating how many async requests are executed at the same time.
    private int batchSize = 3;
    //a flag indicating if there is any pending work, currently tied to an OfflineStore.
    private boolean needsSync = false;

    //a list of RequestInfo of the client requests that succeeded.
    private ArrayList<OfflineStore.RequestInfo> successfulCalls;
    //a list of RequestInfo of the client requests that failed.
    private ArrayList<OfflineStore.RequestInfo> failedCalls;


    //This class maintains it's own Kinvey AbstractClient-- which needs an AppKey and an AppSecret.
    private String appKey;
    private String appSecret;

    //Every call to Kinvey's AppData API needs an associated collection and response class.
    private String collectionName;
    private Class responseClass;

    public OfflineAppDataService() {
        super("Kinvey - Executor Service");
    }

    /**
     * This class is called by the Android OS, when a registered intent is fired off.
     *
     * Check <intent-filter> elements in the Manifest.xml to see which intents are handled by this method.
     *
     * @param intent the intent to act upon.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        if (action.equals(ACTION_OFFLINE_SYNC)) {
            this.needsSync = true;
            if (isOnline()) {
                OfflineStore curStore = new OfflineStore(getApplicationContext(), collectionName);
                getFromStoreAndExecute();
            }
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (this.needsSync) {
                if (!requireWIFI) {
                    if (isOnline()) {
                        getFromStoreAndExecute();
                    }
                }
            }
        } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            if (this.needsSync) {
                if (isOnline()) {
                    getFromStoreAndExecute();
                }
            }


        }

    }


    /**
     * This method pulls a RequestInfo from an instance of an OfflineStore.
     *
     * If there are no queued requests in the Store, this service will set the needsSync flag to false and end itself.
     *
     * If there are RequestInfo's queued up, this method will grab the first one to fire, and make an async request
     * using Kinvey's AbstractClient appData API and manage the response internally.
     */
    private <T> void getFromStoreAndExecute() {

        final OfflineStore curStore = new OfflineStore(getApplicationContext(), collectionName);
        Client client = new Client.Builder(appKey, appSecret, getApplicationContext()).build();
//        client.appData(collectionName, responseClass);

        for (int i = 0; i < batchSize; i++) {

            OfflineStore.RequestInfo cur = curStore.pop();

            if(cur == null){
                //if a call to pop() on the store returns null, then there is nothing left in the queue
                //so syncing is done.
                this.needsSync = false;
                this.stopSelf();
                return;
            }

            if (cur.getHttpVerb().equals("PUT") || cur.getHttpVerb().equals(("POST"))) {
                client.appData(collectionName, responseClass).save(curStore.GetEntityFromDataStore(cur.getEntityID()), new RequestInfoCallback<T>(cur) {
                    @Override
                    public void onSuccess(T result) {
                        OfflineAppDataService.this.storeCompletedRequestInfo(true, this.getInfo());
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        OfflineAppDataService.this.storeCompletedRequestInfo(false, this.getInfo());
                    }
                });
            } else if (cur.getHttpVerb().equals("DELETE")) {
                client.appData(collectionName, responseClass).delete(cur.getEntityID(), new DeleteRequestInfoCallback(cur) {

                    @Override
                    public void onSuccess(KinveyDeleteResponse result) {
                        OfflineAppDataService.this.storeCompletedRequestInfo(true, this.getInfo());
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        OfflineAppDataService.this.storeCompletedRequestInfo(false, this.getInfo());
                    }
                });


            } else if (cur.getHttpVerb().equals("GET")) {
                client.appData(collectionName, responseClass).getEntity(cur.getEntityID(), new RequestInfoCallback<T>(cur) {
                    @Override
                    public void onSuccess(T result) {

                        OfflineAppDataService.this.storeCompletedRequestInfo(true, this.getInfo());
                        curStore.addToStore(((GenericJson) result).get("_id").toString(), result);
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        OfflineAppDataService.this.storeCompletedRequestInfo(false, this.getInfo());
                    }
                });

            } else {
                Preconditions.checkState(false, "Unsupported Http Verb in the store");
            }
        }
    }

    private void storeCompletedRequestInfo(boolean success, OfflineStore.RequestInfo info) {
        if (successfulCalls == null) {
            this.successfulCalls = new ArrayList<OfflineStore.RequestInfo>();
        }
        if (failedCalls == null) {
            this.failedCalls = new ArrayList<OfflineStore.RequestInfo>();
        }


        if (success) {
            this.successfulCalls.add(info);
        } else {
            this.failedCalls.add(info);
        }

    }


    public boolean isOnline() {

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;


    }


    //------------------offline callbacks maintaining RequestInfo

    private abstract class RequestInfoCallback<T> implements KinveyClientCallback<T> {

        private OfflineStore.RequestInfo info;

        public RequestInfoCallback(OfflineStore.RequestInfo info) {
            super();
            this.info = info;
        }

        public OfflineStore.RequestInfo getInfo() {
            return this.info;
        }


    }

    private abstract class DeleteRequestInfoCallback implements KinveyDeleteCallback {

        private OfflineStore.RequestInfo info;

        public DeleteRequestInfoCallback(OfflineStore.RequestInfo info) {
            super();
            this.info = info;
        }

        public OfflineStore.RequestInfo getInfo() {
            return this.info;
        }


    }


}
