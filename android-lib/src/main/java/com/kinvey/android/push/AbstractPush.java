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

import android.app.Application;
import android.content.BroadcastReceiver;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.android.Client;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.core.KinveyClientCallback;


/**
 * This class defines the behaviour of a Push implementation, and should be extended within the library to support with new providers.
 *
 * @author mjsalinger
 * @author edwardf
 * @since 2.0
 */
public abstract class AbstractPush {

    public static final String TAG = AbstractPush.class.getCanonicalName();

    private Class<? extends BroadcastReceiver> pushReceiverClass;

    protected Client client;

    private AbstractPush() {
    }

    protected AbstractPush(Client client) {
        this.client = client;
    }

    protected Client getClient() {
        return client;
    }

    public abstract AbstractPush initialize(Application currentApp, Class pushServiceClass);

    public abstract String getPushId();

    public abstract boolean isPushEnabled();

    public abstract void disablePush();

    public abstract boolean isInProduction();

    public abstract String[] getSenderIDs();


    /**
     * This class manages metadata necessary for registering a device for push notifications.
     *
     */
    public static class PushRegistration extends GenericJson {

        @Key
        private String platform = "android";

        @Key
        private String deviceId;

        public PushRegistration() {
        }

        public PushRegistration(String deviceId) {
            this.deviceId = deviceId;
        }


    }

    /**
     * Async wrapper for enabling push notification for the current user
     *
     * @param callback - a callback with results of registration
     * @param deviceID - the current device's unique id from GCM
     */
    public abstract void enablePushViaRest(KinveyClientCallback callback, String deviceID);

    /**
     * Async wrapper for disabling push notifications for the current user
     *
     * @param callback - a callback with results of unregistration
     * @param deviceID - the current device's unique id from GCM
     */
    public abstract void disablePushViaRest(KinveyClientCallback callback, String deviceID);


    /**
     * Request object for posting to the REST endpoint to register a user for push notifications
     */
    public class RegisterPush extends AbstractKinveyJsonClientRequest<PushRegistration> {

        private static final String REST_PATH = "push/{appKey}/register-device";


        RegisterPush(PushRegistration entity) {
            super(getClient(), "POST", REST_PATH, entity, PushRegistration.class);
        }
    }

    /**
     * Request object for posting to REST endpoint to unregister a user from push notifications
     */
    public class UnregisterPush extends AbstractKinveyJsonClientRequest {

        private static final String REST_PATH = "push/{appKey}/unregister-device";

        UnregisterPush(PushRegistration entity) {
            super(getClient(), "POST", REST_PATH, entity, PushRegistration.class);
        }

    }


}
