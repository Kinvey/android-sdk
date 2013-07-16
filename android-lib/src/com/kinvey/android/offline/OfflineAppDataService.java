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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.google.common.base.Preconditions;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.offline.OfflineGenericJson;

import java.util.*;

/**
 *
 * This IntentService can be used to execute App Data client requests.  The current implementation is coupled with an
 * OfflineStorage, -- this class is listening for an intent OFFLINE_SYNC that is kicked off by the store when a new client
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
 * @author edwardf
 * @since 2.0
 *
 */
public class OfflineAppDataService extends IntentService {

    //Intent used to acknowledge offline sync.
    public static final String ACTION_OFFLINE_SYNC = "com.kinvey.android.ACTION_OFFLINE_SYNC";



    //TODO NOTE this class cannot maintain ANY state!  It is GC REGULARLY!

    //The number of milliseconds between each batch of client requests being executed.
    private long staggerTime;
    //a flag indicating if the service should only execute calls on WIFI or if any network will suffice.
    private boolean requireWIFI;
    //The size of a batch, indicating how many async requests are executed at the same time.
    private int batchSize;
    //a flag indicating if there is any pending work, currently tied to an OfflineStorage.
    private boolean needsSync;

//    //Every call to Kinvey's AppData API needs an associated collection and response class.
    private Set<String> collectionSet;
    private Class responseClass = OfflineGenericJson[].class;

    public OfflineAppDataService() {
        super("Kinvey - Executor Service");

    }


    public void onCreate() {
        super.onCreate();
        OfflineSettings settings = OfflineSettings.getInstance(this);
        batchSize = settings.getBatchSize();
        requireWIFI = settings.isRequireWIFI();
        staggerTime = settings.getStaggerTime();
        needsSync = settings.isNeedsSync();
        collectionSet = settings.getCollectionSet();
        Log.d(Client.TAG, "Offline Executor created");
    }

    public void onDestroy(){
        super.onDestroy();
        Log.d(Client.TAG, "Offline Executor destroyed");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, startId, startId);
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        return START_STICKY;
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
        Log.i(Client.TAG, "Offline Executor Intent received ->" + intent.getAction());
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        if (action.equals(ACTION_OFFLINE_SYNC)) {
            this.needsSync = true;
            if (isOnline()) {
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
     * This method pulls a RequestInfo from an instance of an OfflineStorage.
     *
     * If there are no queued requests in the Store, this service will set the needsSync flag to false and end itself.
     *
     * If there are RequestInfo's queued up, this method will grab the first one to fire, and make an async request
     * using Kinvey's AbstractClient appData API and manage the response internally.
     */
    private <T> void getFromStoreAndExecute() {
        Log.v(Client.TAG, "About to get from store and execute it, collection set size: " + collectionSet.size());
        int done = 0;

        for (final String collectionName : collectionSet) {
            Log.v(Client.TAG, "Current Collection is: " + collectionName);

            final OfflineStorage curStore = OfflineStorage.getStore(getApplicationContext(), collectionName);
            if(curStore.getMyClass() == null){
                return;
            }
            this.responseClass = curStore.getMyClass();
            Log.v(Client.TAG, "ok response class is: " + responseClass.getSimpleName());
            Client client = new Client.Builder(getApplicationContext()).build();
//        client.appData(collectionName, responseClass);

            for (int i = 0; i < batchSize; i++) {

                final OfflineRequestInfo cur = curStore.pop();

                if (cur != null) {

                Log.v(Client.TAG, "request to execute of type: " + cur.getHttpVerb());

                if (cur.getHttpVerb().equals("PUT") || cur.getHttpVerb().equals(("POST"))) {
                    client.appData(collectionName, responseClass).save(curStore.GetEntityFromDataStore(cur.getEntityID()), new RequestInfoCallback<T>(cur) {
                        @Override
                        public void onSuccess(T result) {
                            OfflineAppDataService.this.storeCompletedRequestInfo(collectionName, true, this.getInfo(), result, curStore);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            OfflineAppDataService.this.storeCompletedRequestInfo(collectionName, false, this.getInfo(), error, curStore);
                        }
                    });
                } else if (cur.getHttpVerb().equals("DELETE")) {
                    client.appData(collectionName, responseClass).delete(cur.getEntityID(), new DeleteRequestInfoCallback(cur) {

                        @Override
                        public void onSuccess(KinveyDeleteResponse result) {
                            OfflineAppDataService.this.storeCompletedRequestInfo(collectionName, true, this.getInfo(), result, curStore);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            OfflineAppDataService.this.storeCompletedRequestInfo(collectionName, false, this.getInfo(), error, curStore);
                        }
                    });


                } else if (cur.getHttpVerb().equals("GETQUERY")){
//                    Class responseArray;
//                    try {
//                        responseArray = Class.forName("[L" + responseClass.getName() + ";");
//                    } catch (Exception e) {
//                        Log.i(Client.TAG, "NOPE ON CREATING THAT ARRAY");
//                        responseArray = responseClass;
//                    }

//
//                    client.appData(collectionName, responseClass).get(cur.getQuery(), new RequestInfoListCallback<T>(cur) {
//                        @Override
//                        public void onSuccess(T[] result) {
//                            Log.i(Client.TAG, "GET query onSuccess callback");
//
//                            ArrayList<String> ids = new ArrayList<String>();
//
//                            for (int i = 0; i < result.length; i++) {
//                                String id = ((OfflineGenericJson) result[i]).get("_id").toString();
//                                curStore.addToStore(id, result[i]);
//                                ids.add(id);
//
//                            }
//
////                            curStore.add
//
////                            cur.getQuery();
//                            curStore.addQuery(cur.getQuery(), cur.getEntityID(), ids) ;
//
//                            OfflineAppDataService.this.storeCompletedRequestInfo(collectionName, true, this.getInfo(), result, curStore);
//
//                        }
//
//                        @Override
//                        public void onFailure(Throwable error) {
//                            Log.i(Client.TAG, "GET request onFailure callback");
//                            OfflineAppDataService.this.storeCompletedRequestInfo(collectionName, false, this.getInfo(), error, curStore);
//                        }
//                    });





                } else if (cur.getHttpVerb().equals("GET")) {
                    Class responseArray;
//                java.lang.reflect.Array.newInstance(responseClass, 0).getClass();
                    try {
                        responseArray = Class.forName("[L" + responseClass.getName() + ";");
                    } catch (Exception e) {
                        Log.i(Client.TAG, "NOPE ON CREATING THAT ARRAY");
                        responseArray = responseClass;
                    }


                    client.appData(collectionName, responseArray).getEntity(cur.getEntityID(), new RequestInfoCallback<T[]>(cur) {
                        @Override
                        public void onSuccess(T[] result) {
                            Log.i(Client.TAG, "GET request onSuccess callback");

                            for (int i = 0; i < result.length; i++) {
                                curStore.addToStore(((OfflineGenericJson) result[i]).get("_id").toString(), result[i]);

                            }

                            OfflineAppDataService.this.storeCompletedRequestInfo(collectionName, true, this.getInfo(), result, curStore);

                        }

                        @Override
                        public void onFailure(Throwable error) {
                            Log.i(Client.TAG, "GET request onFailure callback");
                            OfflineAppDataService.this.storeCompletedRequestInfo(collectionName, false, this.getInfo(), error, curStore);
                        }
                    });

                } else {
                    Preconditions.checkNotNull(null, "Unsupported Http Verb in the store");
                }
            }   else{ done++;}
            }
        }

    }

    private void storeCompletedRequestInfo(String collectionName, boolean success, OfflineRequestInfo info, Object returnValue, OfflineStorage store) {
        store.notifyExecution(collectionName, success, info, returnValue);
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


    //------------------offline callbacks maintaining RequestInfo

    private abstract class RequestInfoCallback<T> implements KinveyClientCallback<T> {

        private OfflineRequestInfo info;

        public RequestInfoCallback(OfflineRequestInfo info) {
            super();
            this.info = info;
        }

        public OfflineRequestInfo getInfo() {
            return this.info;
        }


    }

    private abstract class RequestInfoListCallback<T> implements KinveyListCallback<T> {

        private OfflineRequestInfo info;

        public RequestInfoListCallback(OfflineRequestInfo info) {
            super();
            this.info = info;
        }

        public OfflineRequestInfo getInfo() {
            return this.info;
        }


    }


    private abstract class DeleteRequestInfoCallback implements KinveyDeleteCallback {

        private OfflineRequestInfo info;

        public DeleteRequestInfoCallback(OfflineRequestInfo info) {
            super();
            this.info = info;
        }

        public OfflineRequestInfo getInfo() {
            return this.info;
        }


    }


}
