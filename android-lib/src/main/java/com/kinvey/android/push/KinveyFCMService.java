/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
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
package com.kinvey.android.push;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.kinvey.android.Client;

/**
 * Firebase Messaging Service responsible for handling FCM messages.
 * <p>
 * To use FCM for push notifications, extend this class and implement the provided abstract methods. When FCM related events occur, they relevant method will be called by the library.
 * <p/>
 *
 * @author edwardf
 * @since 3.0
 */
public abstract class KinveyFCMService extends FirebaseMessagingService {

    public static final String MESSAGE_FROM_FCM = "msg";
    public static final String TAG = "KINVEY-FCM";
    public static final String TRIGGER = "KINVEY_ACTION";
    public static final String REG_ID = "REGID";

    private Client client = Client.sharedInstance;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData() != null && remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            onMessage((String) remoteMessage.getData().values().toArray()[0]);
        }
        if (remoteMessage.getData() == null && remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            onMessage(remoteMessage.getNotification().getBody());
        }
    }

    public abstract void onMessage(String r);

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "New FCM InstanceID token");
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                String regid = Client.sharedInstance.getContext().getSharedPreferences(FCMPush.shared_pref, Context.MODE_PRIVATE).getString(FCMPush.pref_regid, "");
                if (Client.sharedInstance.isUserLoggedIn() && !regid.isEmpty()) {
                    Client.sharedInstance.push(Client.sharedInstance.getPushServiceClass()).initialize(getApplication());
                }
            }
        });
    }
}