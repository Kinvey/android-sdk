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
 * This class will also re-broadcast all received intents to a custom broadcast receiver, using the intent: com.kinvey.android.push.KINVEY_GCM_MESSAGE
 * </p>
 *
 *
 * @author edwardf
 * @since 2.0
 */
public abstract class KinveyGCMService extends GCMBaseIntentService {

    public static final String KINVEY_GCM_MESSAGE = "com.kinvey.android.push.KINVEY_GCM_MESSAGE";
    public static final String EXTRA_MESSAGE = "Extra_Message";
    public static final String EXTRA_TYPE = "Extra_type";

    public static final String MESSAGE_REGISTERED = "registered";
    public static final String MESSAGE_UNREGISTERED = "unregistered";
    public static final String MESSAGE_DELETE = "deleted";
    public static final String MESSAGE_ERROR = "error";
    public static final String MESSAGE_FROM_GCM = "message";
    public static final String MESSAGE_DELETE_COUNT = "delete_count";

    @SuppressWarnings("hiding")
    private static final String TAG = "Kinvey - GCM";

    public KinveyGCMService() {
        super("GCM PUSH");
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        Client myClient = new Client.Builder(context).build();
        registerWithKinvey(myClient, registrationId, true);

        onRegistered(registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        Client myClient = new Client.Builder(context).build();
        registerWithKinvey(myClient, registrationId, false);
        onUnregistered(registrationId);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        String message = intent.getStringExtra(MESSAGE_FROM_GCM);
        Log.i(TAG, "Received message -> " + message);

        onMessage(message);

    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        onDelete(total);

    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
        onError(errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        Log.i(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }



    public static void registerWithKinvey(Client client, String gcmRegID, boolean register) {
        //registered on GCM but not on Kinvey?
        Log.v(Client.TAG , "about to register with Kinvey");

        if (!client.user().isUserLoggedIn()) {
            Log.e(Client.TAG, "Need to login a current user before registering for push!");
            return;
        }
        if (register) {
            //send registration to Kinvey
            GCMPushOptions.PushConfig config = new GCMPushOptions.PushConfig();
            GCMPushOptions.PushConfigField active = new GCMPushOptions.PushConfigField();
            active.setIds(client.push().getSenderIDs());
            active.setNotificationKey(gcmRegID);
            if (client.push().isInDevMode()) {
                config.setGcmDev(active);
            } else {
                config.setGcm(active);
            }
            client.user().put("_push", config);
        } else {
            //remove push from user object
            client.user().remove("_push");
        }

        client.user().update(new KinveyUserCallback() {
            @Override
            public void onSuccess(User result) {
                Log.v(Client.TAG , "GCM - user updated successfully -> " + result.containsKey("_user"));
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



    public abstract void onMessage(String message);


    public abstract void onError(String error);


    public abstract void onDelete(int deleteCount);

    public abstract void onRegistered(String gcmID);

    public abstract void onUnregistered(String oldID) ;

//    static void notifyReceiverOfRegistrationState(Context context, String message) {
//        Intent intent = new Intent(KINVEY_GCM_MESSAGE);
//        intent.putExtra(EXTRA_TYPE, message);
//        context.sendBroadcast(intent);
//    }
//
//    static void notifyReceiverOfPushMessage(Context context, String message) {
//        Intent intent = new Intent(KINVEY_GCM_MESSAGE);
//        intent.putExtra(EXTRA_TYPE, MESSAGE_FROM_GCM);
//        intent.putExtra(MESSAGE_FROM_GCM, message);
//        context.sendBroadcast(intent);
//    }
//
//    static void notifyReceiverOfDeletion(Context context, int count) {
//        Intent intent = new Intent(KINVEY_GCM_MESSAGE);
//        intent.putExtra(EXTRA_TYPE, MESSAGE_DELETE);
//        intent.putExtra(MESSAGE_DELETE_COUNT, count);
//        context.sendBroadcast(intent);
//    }
//
//    static void notifyReceiverOfError(Context context, String errorMessage) {
//        Intent intent = new Intent(KINVEY_GCM_MESSAGE);
//        intent.putExtra(EXTRA_TYPE, MESSAGE_ERROR);
//        intent.putExtra(MESSAGE_FROM_GCM, errorMessage);
//        context.sendBroadcast(intent);
//    }


}
