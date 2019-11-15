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
import android.content.BroadcastReceiver
import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key
import com.kinvey.android.Client
import com.kinvey.java.core.AbstractKinveyJsonClientRequest
import com.kinvey.java.core.KinveyClientCallback

/**
 * This class defines the behaviour of a Push implementation, and should be extended within the library to support with new providers.
 *
 * @author mjsalinger
 * @author edwardf
 * @since 2.0
 */
abstract class AbstractPush(var client: Client<*>?) {

    private val pushReceiverClass: Class<out BroadcastReceiver?>? = null

    var pushServiceClass: Class<*>? = null

    abstract fun initialize(currentApp: Application?): AbstractPush?
    abstract val pushId: String?
    abstract val isPushEnabled: Boolean
    abstract fun disablePush()
    abstract var isInProduction: Boolean
    abstract var senderIDs: Array<String>?

    /**
     * This class manages metadata necessary for registering a device for push notifications.
     *
     */
    class PushRegistration : GenericJson {
        @Key
        private val platform: String? = "android"
        @Key
        private val service: String? = "firebase"
        @Key
        private var deviceId: String? = null

        constructor() {}
        constructor(deviceId: String?) {
            this.deviceId = deviceId
        }
    }

    /**
     * Async wrapper for enabling push notification for the current user
     *
     * @param callback - a callback with results of registration
     * @param deviceID - the current device's unique id from GCM
     */
    abstract fun enablePushViaRest(callback: KinveyClientCallback<Any?>?, deviceID: String?)

    /**
     * Async wrapper for disabling push notifications for the current user
     *
     * @param callback - a callback with results of unregistration
     * @param deviceID - the current device's unique id from GCM
     */
    abstract fun disablePushViaRest(callback: KinveyClientCallback<Any?>?, deviceID: String?)

    /**
     * Request object for posting to the REST endpoint to register a user for push notifications
     */
    inner class RegisterPush constructor(entity: PushRegistration?)
        : AbstractKinveyJsonClientRequest<PushRegistration>(client, "POST", REGISTER_PUSH_REST_PATH, entity, PushRegistration::class.java) {
    }

    /**
     * Request object for posting to REST endpoint to unregister a user from push notifications
     */
    inner class UnregisterPush constructor(entity: PushRegistration?)
        : AbstractKinveyJsonClientRequest<PushRegistration>(client, "POST", UNREGISTER_PUSH_REST_PATH, entity, PushRegistration::class.java) {
    }

    fun createRegisterPushRequest(pushRegistration: PushRegistration?): RegisterPush? {
        return RegisterPush(pushRegistration)
    }

    fun createUnregisterPushRequest(pushRegistration: PushRegistration?): UnregisterPush? {
        return UnregisterPush(pushRegistration)
    }

    companion object {
        @JvmField
        val TAG = AbstractPush::class.java.canonicalName
        const val UNREGISTER_PUSH_REST_PATH = "push/{appKey}/unregister-device"
        const val REGISTER_PUSH_REST_PATH = "push/{appKey}/register-device"
    }
}