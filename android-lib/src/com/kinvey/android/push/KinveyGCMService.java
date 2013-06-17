/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kinvey.android.push;



import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;

/**
 * IntentService responsible for handling GCM messages.
 * <p>
 * Upon successful registration/unregistration with GCM, this class will perform the appropriate action with Kinvey as well.
 * </p>
 * <p>
 * To use GCM for push notifications, extend this class and implement the provided abstract methods.
 * <p>
 *
 * @author edwardf
 * @since 2.0
 */
public abstract class KinveyGCMService extends GCMBaseIntentService {

    public static final String MESSAGE_FROM_GCM = "msg";

    private Client client;


    /**
     * Public Constructor used by operating system.
     */
    public KinveyGCMService() {
        super("GCM PUSH");
    }

    @Override
    protected void onRegistered(Context context, final String registrationId) {
        Log.v(TAG, "Device registered: regId = " + registrationId);


        Client.Builder builder;
        if (getAppKey() == null){
            builder = new Client.Builder(context);
        }else{
            builder = new Client.Builder(getAppKey(), getAppSecret(), context);
        }

        if (gcmEnabled()){
            builder.setSenderIDs(getSenderIDs());
            builder.setGcmInProduction(inProduction());
            builder.enableGCM(gcmEnabled());
        }
        if (getBaseURL() != null){
            builder.setBaseUrl(getBaseURL());
        }

        builder.setRetrieveUserCallback(new KinveyUserCallback() {
            @Override
            public void onSuccess(User result) {
                registerWithKinvey(registrationId, true);
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "GCM registration failed to retrieve user!");
            }
        });

        client = builder.build();



    }

    public String getSenderIDs(){
        return null;
    }

    public String getAppKey(){
        return null;
    }

    public String getAppSecret(){
        return null;
    }

    public boolean gcmEnabled(){
        return false;
    }

    public boolean inProduction(){
        return true;
    }

    public String getBaseURL(){
        return null;
    }




    @Override
    protected void onUnregistered(Context context, final String registrationId) {
        Log.v(TAG, "Device unregistered");

        Client.Builder builder;
        if (getAppKey() == null){
            builder = new Client.Builder(context);
        }else{
            builder = new Client.Builder(getAppKey(), getAppSecret(), context);
        }

        if (gcmEnabled()){
            builder.setSenderIDs(getSenderIDs());
            builder.setGcmInProduction(inProduction());
            builder.enableGCM(gcmEnabled());
        }
        if (getBaseURL() != null){
            builder.setBaseUrl(getBaseURL());
        }

        builder.setRetrieveUserCallback(new KinveyUserCallback() {
            @Override
            public void onSuccess(User result) {
                registerWithKinvey(registrationId, false);
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "GCM registration failed to retrieve user!");
            }
        });

        client = builder.build();

    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        String message = intent.getStringExtra(MESSAGE_FROM_GCM);
        Log.v(TAG, "Received message -> " + message);
        onMessage(message);

    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.v(TAG, "Received deleted messages notification");
        onDelete(total);

    }

    @Override
    public void onError(Context context, String errorId) {
        Log.v(TAG, "Received error: " + errorId);
        onError(errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        Log.v(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }



    private void registerWithKinvey(String gcmID, boolean register){
        registerWithKinvey(client, gcmID, register);
    }

    public void registerWithKinvey(Client client, final String gcmRegID, boolean register) {
        //registered on GCM but not on Kinvey?
        Log.v(Client.TAG , "about to register with Kinvey");
        if (client == null){
            Log.e(Client.TAG, "GCMService got garbage collected, cannot complete registration!");
            return;
        }

        if (!client.user().isUserLoggedIn()) {
            Log.e(Client.TAG, "Need to login a current user before registering for push!");
            return;
        }

        if (register) {
            //send registration to Kinvey
            GCMPush.PushConfig config = new GCMPush.PushConfig();
            GCMPush.PushConfigField active = new GCMPush.PushConfigField();
            //TODO -- don't just set IDs, get it, add new one, and then save it (multiple devices -> multiple GCM ids)
            active.setIds(new String[]{gcmRegID});
            //active.setNotificationKey(gcmRegID);
            if (!client.push().isInProduction()) {
                config.setGcmDev(active);
            } else {
                config.setGcm(active);
            }
            client.user().put("_push", config);
        } else {
            //remove push from user object
            client.user().remove("_push");
            client.user().put("_push", null);
        }
        client.user().update(new KinveyUserCallback() {
            @Override
            public void onSuccess(User result) {
                Log.v(Client.TAG , "GCM - user updated successfully, push exists? -> " + result.containsKey("_push"));
                if (result.containsKey("_push")){
                    KinveyGCMService.this.onRegistered(gcmRegID);

                }else{
                    KinveyGCMService.this.onUnregistered(gcmRegID);

                }


            }

            @Override
            public void onFailure(Throwable error) {
                Log.v(Client.TAG , "GCM - user update error: " + error);
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        GCMRegistrar.onDestroy(getApplicationContext());
    }


    /**
     * This method is called when a message is received through GCM via Kinvey.
     *
     * @param message the text of the message
     */
    public abstract void onMessage(String message);


    /**
     * This method is called when an error occurs with GCM.
     *
     * @param error the text of the error message
     */
    public abstract void onError(String error);

    /**
     * This method is called when GCM messages are deleted.
     *
     * @param deleteCount the number of deleted messages
     */
    public abstract void onDelete(int deleteCount);

    /**
     * This method is called after successful registration.  This includes both registering with GCM as well as Kinvey.
     *
     * @param gcmID the new user's unique GCM registration ID
     */
    public abstract void onRegistered(String gcmID);

    /**
     * This method is called after successful unregistration.  This includes removing push from both GCM as well as Kinvey.
     *
     * @param oldID the old GCM registration ID of the now unregistered user.
     */
    public abstract void onUnregistered(String oldID) ;




}
