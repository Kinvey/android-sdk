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
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.model.User;
import com.kinvey.android.store.UserStore;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Logger;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.store.BaseUserStore;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;


/**
 *
 * <p>
 * This functionality can be accessed through the {@link com.kinvey.android.Client#push(Class)} ()} convenience method.
 * </p>
 *
 * <p>This class manages FCM Push for the current logged in user.  Use `fcm.enabled=true` in the `kinvey.properties` file to enable FCM.</p>
 *
 * sample usage:
 * <pre>
 kinveyClient.push().initialize(getApplicationContext());
 * </pre>
 *
 *<p>This code snippet will enable push notifications through FCM for the current logged in user.</p>
 *
 *
 * @author edwardf
 * @since 3.0
 */
public class FCMPush extends AbstractPush {

    public static String[] senderIDs = new String[0];
    private static boolean inProduction = false;
    public static final String shared_pref = "Kinvey_Push";
    public static final String pref_regid = "reg_id";

    public FCMPush(Client client, boolean inProduction, String ... senderIDs) {
        super(client);
        FCMPush.senderIDs = senderIDs;
        FCMPush.inProduction = inProduction;
    }


    /**
     * Initialize FCM by registering the current user with both FCM as well as your backend at Kinvey.
     *
     * Note these operations are performed asynchronously, however there is no callback.  Instead, updates
     * are delegated to your custom `KinveyFCMService` which will handle any responses.
     *
     * @param currentApp - The current valid application context.
     * @return an instance of FCM push, initialized for the current user.
     */
    @Override
    public FCMPush initialize(final Application currentApp) {
        if (!getClient().isUserLoggedIn()) {
            throw new KinveyException("No user is currently logged in", "call UserStore.login(...) first to login", "Registering for Push Notifications needs a logged in user");
        }

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(currentApp) != ConnectionResult.SUCCESS){
            throw new KinveyException("Google Play Services is not available on the current device", "The device needs Google Play Services", "FCM for push notifications requires Google Play Services");
        }

        new AsyncRegisterFCM(new KinveyClientCallback() {
            @Override
            public void onSuccess(Object result) {
                Logger.ERROR("FCM - successful register CGM");
            }

            @Override
            public void onFailure(Throwable error) {
                Logger.ERROR("FCM - unsuccessful register CGM: " + error.getMessage());
            }
        }).execute();

        return this;
    }

    public void registerWithKinvey(final String fcmRegID, boolean register) {
        Logger.INFO("about to register with Kinvey");
        if (client == null) {
            Logger.ERROR("FCMService got garbage collected, cannot complete registration!");
            return;
        }

        if (!client.isUserLoggedIn()) {
            Logger.ERROR("Need to login a current user before registering for push!");
            return;
        }
        if (register) {

            client.push(pushServiceClass).enablePushViaRest(new KinveyClientCallback() {
                @Override
                public void onSuccess(Object result) {
                    UserStore.retrieve(client, new KinveyUserCallback<User>() {

                        @Override
                        public void onSuccess(User result) {
                            client.getActiveUser().put("_messaging", result.get("_messaging"));
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            Logger.ERROR("FCM - user update error: " + error);
                        }
                    });
                }

                @Override
                public void onFailure(Throwable error) {
                    Logger.ERROR("FCM - user update error: " + error);
                }
            }, fcmRegID);

        } else {
            client.push(pushServiceClass).disablePushViaRest(new KinveyClientCallback() {
                @Override
                public void onSuccess(Object result) {
                    Logger.ERROR("FCM - user update success");
                }

                @Override
                public void onFailure(Throwable error) {
                    Logger.ERROR("FCM - user update error: " + error);
                }
            }, fcmRegID);

        }
    }


    /**
     * Get the InstanceID from FCM for the Client's current application context.
     *
     * Note if the current user is not registered, the registration ID will be an empty string.
     *
     * @return - the current user's FCM InstanceID or an empty string ""
     */
    @Override
    public String getPushId() {
        if (getClient() == null || getClient().getContext() == null){
            return "";
        }
        String regid = getClient().getContext().getSharedPreferences(shared_pref, Context.MODE_PRIVATE).getString(pref_regid, "");
        return regid;
    }

    /**
     * Check to see if the current user is registered for FCM.  This checks both with FCM directly as well as with a Kinvey backend.
     *
     * @return true if current user is registered, false if they are not.
     */
    @Override
    public boolean isPushEnabled() {
        if (getClient() == null || getClient().getContext() == null){
            return false;
        }
        String gcmID = getClient().getContext().getSharedPreferences(shared_pref, Context.MODE_PRIVATE).getString(pref_regid, "");
        if (gcmID == null || gcmID.equals("")){
            return false;
        }
        if (getClient().getActiveUser().containsKey("_messaging")){
            AbstractMap<String, Object> pushField = (AbstractMap<String, Object>) getClient().getActiveUser().get("_messaging");
            if (pushField.containsKey("pushTokens")){
                ArrayList<AbstractMap<String, Object>> gcmField = (ArrayList<AbstractMap<String, Object>>) pushField.get("pushTokens");
                for(AbstractMap<String, Object> gcm : gcmField){
                    if (gcm.get("platform").equals("android")){
                        if (gcm.get("token").equals(gcmID)){
                            return true;

                        }
                    }

                }
            }
        }
        return false;
    }

    /**
     * Unregisters the current user with FCM
     *
     * Unregistration is asynchronous, so use the `KinveyFCMService` to receive notification when unregistration has completed.
     *
     */
    @Override
    public void disablePush() {
        if (getClient() == null || getClient().getContext() == null){
            return;
        }
        String regid = getClient().getContext().getSharedPreferences(shared_pref, Context.MODE_PRIVATE).getString(pref_regid, "");

        SharedPreferences.Editor pref = getClient().getContext().getSharedPreferences(shared_pref, Context.MODE_PRIVATE).edit();
        pref.remove(pref_regid);
        pref.commit();

        if (!regid.isEmpty()){
            registerWithKinvey(regid, false);
        }

        new AsyncUnRegisterFCM(new KinveyClientCallback() {
            @Override
            public void onSuccess(Object result) {
                Logger.ERROR("FCM - successful unregister FCM");
            }

            @Override
            public void onFailure(Throwable error) {
                Logger.ERROR("FCM - unsuccessful unregister FCM: " + error.getMessage());
            }
        }).execute();
//        GCMRegistrar.unregister(getClient().getContext());


    }


    /**
     * Is FCM Push configured for production or a dev environment?
     *
     * @return true if in production mode, false if not
     */
    @Override
    public boolean isInProduction() {
        return inProduction;
    }

    /**
     * Get a list of all sender IDs as an array
     *
     * @return an array of sender IDs
     */
    @Override
    public String[] getSenderIDs() {
        return senderIDs;
    }

    /**
     * This class is used to maintain metadata about the current GCM push configuration in the User collection.
     *
     *
     */
    public static class PushConfig extends GenericJson {

        @Key("GCM")
        private PushConfigField gcm;
        @Key("GCM_dev")
        private PushConfigField gcmDev;

        public PushConfig(){}


        public PushConfigField getGcm() {
            return gcm;
        }

        public void setGcm(PushConfigField gcm) {
            this.gcm = gcm;
        }

        public PushConfigField getGcmDev() {
            return gcmDev;
        }

        public void setGcmDev(PushConfigField gcmDev) {
            this.gcmDev = gcmDev;
        }
    }

    /**
     * Manages ids and notificationKeys for {@code PushConfig}
     *
     */
    public static class PushConfigField extends GenericJson{
        @Key
        private String[] ids;
        @Key("notification_key")
        private String notificationKey;

        public PushConfigField(){}

        public String[] getIds() {
            return ids;
        }

        public void setIds(String[] ids) {
            this.ids = ids;
        }

        public String getNotificationKey() {
            return notificationKey;
        }

        public void setNotificationKey(String notificationKey) {
            this.notificationKey = notificationKey;
        }
    }


    @Override
    public void enablePushViaRest(KinveyClientCallback callback, String deviceID){
        createAsyncEnablePushRequest(callback, deviceID).execute();
    }

    @Override
    public void disablePushViaRest(KinveyClientCallback callback, String deviceID){
        createAsyncDisablePushRequest(callback, deviceID).execute();
    }


    private class AsyncRegisterFCM extends AsyncClientRequest{

        AsyncRegisterFCM(KinveyClientCallback callback) {
            super(callback);
        }

        @Override
        protected User executeAsync() throws IOException {
            try {
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "getInstanceId failed", task.getException());
                                    return;
                                }
                                if (task.getResult() != null) {
                                    final String regid = task.getResult().getToken();
                                    Logger.INFO("regid is " + regid);
                                    SharedPreferences.Editor pref = getClient().getContext().getSharedPreferences(shared_pref, Context.MODE_PRIVATE).edit();
                                    pref.putString(pref_regid, regid);
                                    pref.apply();
                                    registerWithKinvey(regid, true);
                                }
                            }
                        });


            } catch (Exception ex) {
                Logger.ERROR("unable to register with FCM: " + ex.getMessage());
            }
            return null;
        }
    }

    private class AsyncUnRegisterFCM extends AsyncClientRequest{

        AsyncUnRegisterFCM(KinveyClientCallback callback) {
            super(callback);
        }

        @Override
        protected User executeAsync() throws IOException {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException ex) {
                Logger.ERROR("unable to register with FCM: " + ex.getMessage());
            }
            return null;
        }
    }

    private class AsyncEnablePush extends AsyncClientRequest{

        String deviceID;

        public AsyncEnablePush(KinveyClientCallback callback, String deviceID) {
            super(callback);
            this.deviceID = deviceID;
        }

        @Override
        protected User executeAsync() throws IOException {

            PushRegistration ent = new PushRegistration(deviceID);
            RegisterPush p = createRegisterPushRequest(ent);
            getClient().initializeRequest(p);
            p.execute();


            return null;
        }
    }


    private class AsyncDisablePush extends AsyncClientRequest {

        String deviceID;

        public AsyncDisablePush(KinveyClientCallback callback, String deviceID) {
            super(callback);
            this.deviceID = deviceID;
        }

        @Override
        protected User executeAsync() throws IOException {

            PushRegistration ent = new PushRegistration(deviceID);
            UnregisterPush p = createUnregisterPushRequest(ent);
            getClient().initializeRequest(p);
            p.execute();

            return null;
        }
    }

    private AsyncDisablePush createAsyncDisablePushRequest(KinveyClientCallback callback, String deviceID) {
        return new AsyncDisablePush(callback, deviceID);
    }

    private AsyncEnablePush createAsyncEnablePushRequest(KinveyClientCallback callback, String deviceID) {
        return new AsyncEnablePush(callback, deviceID);
    }

}