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
package com.kinvey.android.push

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kinvey.android.Client
import com.kinvey.android.Client.Companion.sharedInstance

/**
 * Firebase Messaging Service responsible for handling FCM messages.
 *
 *
 * To use FCM for push notifications, extend this class and implement the provided abstract methods. When FCM related events occur, they relevant method will be called by the library.
 *
 *
 *
 * @author edwardf
 * @since 3.0
 */
abstract class KinveyFCMService : FirebaseMessagingService() {
    private val client: Client<*> = sharedInstance()
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.from)
        if (remoteMessage.data != null && remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            onMessage(remoteMessage.data.values.toTypedArray()[0] as String)
        }
        if (remoteMessage.data == null && remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification?.body)
            onMessage(remoteMessage.notification?.body)
        }
    }

    abstract fun onMessage(r: String?)
    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM InstanceID token")
        Handler(Looper.getMainLooper()).post {
            val regid = sharedInstance().context
                ?.getSharedPreferences(FCMPush.SHARED_PREF, Context.MODE_PRIVATE)?.getString(FCMPush.PREF_REG_ID, "")
            if (sharedInstance().isUserLoggedIn && !regid.isNullOrEmpty()) {
                sharedInstance().push(sharedInstance().pushServiceClass)?.initialize(application)
            }
        }
    }

    companion object {
        const val MESSAGE_FROM_FCM = "msg"
        const val TAG = "KINVEY-FCM"
        const val TRIGGER = "KINVEY_ACTION"
        const val REG_ID = "REGID"
    }
}