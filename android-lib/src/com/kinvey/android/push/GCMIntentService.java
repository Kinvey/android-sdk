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
 */
public class GCMIntentService extends GCMBaseIntentService {

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

    public GCMIntentService() {
        super(GCMPush.senderIDs);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        Client myClient = new Client.Builder(context).build();
        registerWithKinvey(myClient, registrationId, true);

        notifyReceiverOfRegistrationState(context, MESSAGE_REGISTERED);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        Client myClient = new Client.Builder(context).build();
        registerWithKinvey(myClient, registrationId, false);
        notifyReceiverOfRegistrationState(context, MESSAGE_UNREGISTERED);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        String message = intent.getStringExtra(MESSAGE_FROM_GCM);
        notifyReceiverOfPushMessage(context, message);

    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        notifyReceiverOfDeletion(context, total);

    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
        notifyReceiverOfError(context, errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        Log.i(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }



    public static void registerWithKinvey(Client client, String gcmRegID, boolean register) {
        //registered on GCM but not on Kinvey?
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
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onFailure(Throwable error) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        GCMRegistrar.onDestroy(getApplicationContext());
    }

    static void notifyReceiverOfRegistrationState(Context context, String message) {
        Intent intent = new Intent(KINVEY_GCM_MESSAGE);
        intent.putExtra(EXTRA_TYPE, message);
        context.sendBroadcast(intent);
    }

    static void notifyReceiverOfPushMessage(Context context, String message) {
        Intent intent = new Intent(KINVEY_GCM_MESSAGE);
        intent.putExtra(EXTRA_TYPE, MESSAGE_FROM_GCM);
        intent.putExtra(MESSAGE_FROM_GCM, message);
        context.sendBroadcast(intent);
    }

    static void notifyReceiverOfDeletion(Context context, int count) {
        Intent intent = new Intent(KINVEY_GCM_MESSAGE);
        intent.putExtra(EXTRA_TYPE, MESSAGE_DELETE);
        intent.putExtra(MESSAGE_DELETE_COUNT, count);
        context.sendBroadcast(intent);
    }

    static void notifyReceiverOfError(Context context, String errorMessage) {
        Intent intent = new Intent(KINVEY_GCM_MESSAGE);
        intent.putExtra(EXTRA_TYPE, MESSAGE_ERROR);
        intent.putExtra(MESSAGE_FROM_GCM, errorMessage);
        context.sendBroadcast(intent);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
//    private static void generateNotification(Context context, String message) {
//        int icon = R.drawable.ic_btn_speak_now;
//        long when = System.currentTimeMillis();
//        NotificationManager notificationManager = (NotificationManager)
//                context.getSystemService(Context.NOTIFICATION_SERVICE);
//        Notification notification = new Notification(icon, message, when);
//        Intent notificationIntent = new Intent(context, DemoActivity.class);
//        // set intent so it does not start a new activity
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent intent =
//                PendingIntent.getActivity(context, 0, notificationIntent, 0);
//        notification.setLatestEventInfo(context, "Title", message, intent);
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//        notificationManager.notify(0, notification);
//    }

//    static void notifyClient(Context context, String message) {
//        Intent intent = new Intent(KINVEY_GCM_MESSAGE);
//        intent.putExtra(EXTRA_MESSAGE, message);
//        context.sendBroadcast(intent);
//    }

}
