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

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key
import com.google.firebase.iid.FirebaseInstanceId
import com.kinvey.android.AsyncClientRequest
import com.kinvey.android.Client
import com.kinvey.android.callback.KinveyUserCallback
import com.kinvey.android.model.User
import com.kinvey.android.store.UserStore.Companion.retrieve
import com.kinvey.java.AbstractClient
import com.kinvey.java.KinveyException
import com.kinvey.java.Logger.Companion.ERROR
import com.kinvey.java.Logger.Companion.INFO
import com.kinvey.java.core.KinveyClientCallback
import java.io.IOException
import java.util.*

/**
 *
 *
 *
 * This functionality can be accessed through the [com.kinvey.android.Client.push] ()} convenience method.
 *
 *
 *
 * This class manages FCM Push for the current logged in user.  Use `fcm.enabled=true` in the `kinvey.properties` file to enable FCM.
 *
 * sample usage:
 * <pre>
 * kinveyClient.push().initialize(getApplicationContext());
</pre> *
 *
 *
 * This code snippet will enable push notifications through FCM for the current logged in user.
 *
 *
 * @author edwardf
 * @since 3.0
 */
class FCMPush(client: Client<*>?, inProduction: Boolean, vararg senderIDs: String?) : AbstractPush(client) {
    /**
     * Initialize FCM by registering the current user with both FCM as well as your backend at Kinvey.
     *
     * Note these operations are performed asynchronously, however there is no callback.  Instead, updates
     * are delegated to your custom `KinveyFCMService` which will handle any responses.
     *
     * @param currentApp - The current valid application context.
     * @return an instance of FCM push, initialized for the current user.
     */
    override fun initialize(currentApp: Application?): FCMPush? {
        if (client?.isUserLoggedIn == false) {
            throw KinveyException("No user is currently logged in", "call UserStore.login(...) first to login", "Registering for Push Notifications needs a logged in user")
        }
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(currentApp) != ConnectionResult.SUCCESS) {
            throw KinveyException("Google Play Services is not available on the current device", "The device needs Google Play Services", "FCM for push notifications requires Google Play Services")
        }
        AsyncRegisterFCM(object : KinveyClientCallback<Any?> {
            override fun onSuccess(result: Any?) {
                ERROR("FCM - successful register CGM")
            }

            override fun onFailure(error: Throwable) {
                ERROR("FCM - unsuccessful register CGM: " + error.message)
            }
        }).execute()
        return this
    }

    fun registerWithKinvey(fcmRegID: String?, register: Boolean) {
        INFO("about to register with Kinvey")
        if (client == null) {
            ERROR("FCMService got garbage collected, cannot complete registration!")
            return
        }
        if (client?.isUserLoggedIn == false) {
            ERROR("Need to login a current user before registering for push!")
            return
        }
        if (register) {
            pushServiceClass?.let { cls ->
                client?.let { c ->
                    c.push(cls)?.enablePushViaRest(object : KinveyClientCallback<Any?> {
                        override fun onSuccess(result: Any?) {
                            retrieve(c as AbstractClient<User>, object : KinveyUserCallback<User> {
                                override fun onSuccess(result: User) {
                                    client?.activeUser?.let {
                                        it["_messaging"] = result["_messaging"]
                                    }
                                }
                                override fun onFailure(error: Throwable) {
                                    ERROR("FCM - user update error: $error")
                                }
                            })
                        }
                        override fun onFailure(error: Throwable) {
                            ERROR("FCM - user update error: $error")
                        }
                    }, fcmRegID)
                }
            }
        } else {
            client?.push(pushServiceClass)?.disablePushViaRest(object : KinveyClientCallback<Any?> {
                override fun onSuccess(result: Any?) {
                    ERROR("FCM - user update success")
                }

                override fun onFailure(error: Throwable) {
                    ERROR("FCM - user update error: $error")
                }
            }, fcmRegID)
        }
    }

    /**
     * Get the InstanceID from FCM for the Client's current application context.
     *
     * Note if the current user is not registered, the registration ID will be an empty string.
     *
     * @return - the current user's FCM InstanceID or an empty string ""
     */
    override val pushId: String?
        get() = if (client == null || client?.context == null) { "" }
                else client?.context
                ?.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
                ?.getString(PREF_REG_ID, "")

    /**
     * Check to see if the current user is registered for FCM.  This checks both with FCM directly as well as with a Kinvey backend.
     *
     * @return true if current user is registered, false if they are not.
     */
    override val isPushEnabled: Boolean
        get() {
            if (client == null || client?.context == null) {
                return false
            }
            val gcmID = client?.context?.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)?.getString(PREF_REG_ID, "")
            if (gcmID == null || gcmID == "") {
                return false
            }
            if (client?.activeUser?.containsKey("_messaging") == true) {
                val pushField = client?.activeUser!!["_messaging"] as AbstractMap<String, Any>?
                if (pushField?.containsKey("pushTokens") == true) {
                    val gcmField = pushField["pushTokens"] as ArrayList<AbstractMap<String, Any>>?
                    for (gcm in gcmField!!) {
                        if (gcm["platform"] == "android") {
                            if (gcm["token"] == gcmID) {
                                return true
                            }
                        }
                    }
                }
            }
            return false
        }

    /**
     * Unregisters the current user with FCM
     *
     * Unregistration is asynchronous, so use the `KinveyFCMService` to receive notification when unregistration has completed.
     *
     */
    override fun disablePush() {
        if (client == null || client?.context == null) {
            return
        }
        val regid = client?.context?.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)?.getString(PREF_REG_ID, "")
        val pref = client?.context?.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)?.edit()
        pref?.remove(PREF_REG_ID)
        pref?.apply()
        if (regid?.isEmpty() == false) {
            registerWithKinvey(regid, false)
        }
        AsyncUnRegisterFCM(object : KinveyClientCallback<Any?> {
            override fun onSuccess(result: Any?) {
                ERROR("FCM - successful unregister FCM")
            }
            override fun onFailure(error: Throwable) {
                ERROR("FCM - unsuccessful unregister FCM: " + error.message)
            }
        }).execute()
//        GCMRegistrar.unregister(getClient().getContext());
    }

    /**
     * Get a list of all sender IDs as an array
     *
     * @return an array of sender IDs
     */
    override var senderIDs: Array<String>? = senderIDs as Array<String>?

    /**
     * This class is used to maintain metadata about the current GCM push configuration in the User collection.
     *
     *
     */
    class PushConfig : GenericJson() {
        @Key("GCM")
        var gcm: PushConfigField? = null
        @Key("GCM_dev")
        var gcmDev: PushConfigField? = null

    }

    /**
     * Manages ids and notificationKeys for `PushConfig`
     *
     */
    class PushConfigField : GenericJson() {
        @Key
        var ids: Array<String>? = null
        @Key("notification_key")
        var notificationKey: String? = null
    }

    override fun enablePushViaRest(callback: KinveyClientCallback<Any?>?, deviceID: String?) {
        createAsyncEnablePushRequest(callback, deviceID)?.execute()
    }

    override fun disablePushViaRest(callback: KinveyClientCallback<Any?>?, deviceID: String?) {
        createAsyncDisablePushRequest(callback, deviceID)?.execute()
    }

    private inner class AsyncRegisterFCM(callback: KinveyClientCallback<Any?>?) : AsyncClientRequest<Any?>(callback) {
        @Throws(IOException::class)
        override fun executeAsync(): User? {
            try {
                FirebaseInstanceId.getInstance().instanceId
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.w(TAG, "getInstanceId failed", task.exception)
                                return@OnCompleteListener
                            }
                            if (task.result != null) {
                                val regid = task.result?.token
                                INFO("regid is $regid")
                                val pref = client?.context?.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)?.edit()
                                pref?.putString(PREF_REG_ID, regid)
                                pref?.apply()
                                registerWithKinvey(regid, true)
                            }
                        })
            } catch (ex: Exception) {
                ERROR("unable to register with FCM: " + ex.message)
            }
            return null
        }
    }

    private inner class AsyncUnRegisterFCM(callback: KinveyClientCallback<Any?>?)
        : AsyncClientRequest<Any?>(callback) {
        @Throws(IOException::class)
        override fun executeAsync(): User? {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId()
            } catch (ex: IOException) {
                ERROR("unable to register with FCM: " + ex.message)
            }
            return null
        }
    }

    private inner class AsyncEnablePush(callback: KinveyClientCallback<Any?>?, var deviceID: String?)
        : AsyncClientRequest<Any?>(callback) {
        @Throws(IOException::class)
        override fun executeAsync(): User? {
            val ent = PushRegistration(deviceID)
            val p = createRegisterPushRequest(ent)
            client?.initializeRequest(p)
            p?.execute()
            return null
        }

    }

    private inner class AsyncDisablePush(callback: KinveyClientCallback<Any?>?, var deviceID: String?)
        : AsyncClientRequest<Any?>(callback) {
        @Throws(IOException::class)
        override fun executeAsync(): User? {
            val ent = PushRegistration(deviceID)
            val p = createUnregisterPushRequest(ent)
            client?.initializeRequest(p)
            p?.execute()
            return null
        }

    }

    private fun createAsyncDisablePushRequest(callback: KinveyClientCallback<Any?>?, deviceID: String?): AsyncDisablePush? {
        return AsyncDisablePush(callback, deviceID)
    }

    private fun createAsyncEnablePushRequest(callback: KinveyClientCallback<Any?>?, deviceID: String?): AsyncEnablePush? {
        return AsyncEnablePush(callback, deviceID)
    }

    /**
     * Is FCM Push configured for production or a dev environment?
     *
     * @return true if in production mode, false if not
     */
    override var isInProduction: Boolean = false
        get() {
            return field
        }


    companion object {
        const val SHARED_PREF = "Kinvey_Push"
        const val PREF_REG_ID = "reg_id"
    }
}