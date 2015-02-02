/**
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
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

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.Client;
import com.kinvey.java.KinveyException;
import com.kinvey.java.core.KinveyClientCallback;



/**
 *
 * <p>
 * This functionality can be accessed through the {@link com.kinvey.android.Client#push()} convenience method.
 * </p>
 *
 * <p>This class manages GCM Push for the current logged in user.  Use `gcm.enabled=true` in the `kinvey.properties` file to enable GCM.</p>
 *
 * sample usage:
 * <pre>
 kinveyClient.push().initialize(getApplicationContext());
 * </pre>
 *
 *<p>This code snippet will enable push notifications through GCM for the current logged in user.</p>
 *
 *
 * @author edwardf
 * @since 2.2
 */
public class GCMPush extends AbstractPush {

    public static String[] senderIDs = new String[0];
    private static boolean inProduction = false;


    public GCMPush(Client client, boolean inProduction, String ... senderIDs) {
        super(client);
        GCMPush.senderIDs = senderIDs;
        GCMPush.inProduction = inProduction;
    }




    /**
     * Initialize GCM by registering the current user with both GCM as well as your backend at Kinvey.
     *
     * Note these operations are performed asynchronously, however there is no callback.  Instead, updates
     * are delegated to your custom `KinveyGCMService` which will handle any responses.
     *
     * @param currentApp - The current valid application context.
     * @return an instance of GCM push, initialized for the current user.
     */
    @Override
    public GCMPush initialize(Application currentApp) {
        if (!getClient().user().isUserLoggedIn()){
            throw new KinveyException("No user is currently logged in", "call myClient.User().login(...) first to login", "Registering for Push Notifications needs a logged in user");
        }
        
        
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(currentApp) != ConnectionResult.SUCCESS){
        	throw new KinveyException("Google Play Services is not available on the current device", "The device needs Google Play Services", "GCM for push notifications requires Google Play Services");
        	
        }
        
        final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(currentApp);
    
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
    
                try {
                	Log.i("GCM", "bout to register");            	
                	Client c = null;//
                	
                    final String regid = gcm.register(c.push().getSenderIDs());
                    
                    Log.i("GCM", "regid is " + regid);

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    
                    registerWithKinvey(c, regid, true);

                    
      
                } catch (IOException ex) {
                	Log.e(TAG, "unable to register with GCM: " + ex.getMessage());
                	ex.printStackTrace();
                }
				return null;
      
            }

        }.execute(null, null, null);

       
       
        
//
//        //First check runtime and grab current registration ID
//        GCMRegistrar.checkDevice(currentApp);
//        GCMRegistrar.checkManifest(currentApp);
//
//        final String regId = GCMRegistrar.getRegistrationId(currentApp);
//
//        //if we are not registered on GCM, then register
//        if (regId.equals("")) {
//            Log.v(Client.TAG, "GCM - Registration Id is empty, about to use GCMRegistrar for SenderIDs:");
//            for (String s: senderIDs){
//                Log.v(Client.TAG, "GCM Id -> " + s);
//            }
//            //Registration comes back via an intent
//            GCMRegistrar.register(currentApp, senderIDs);
//            Log.v(Client.TAG, "GCM - just registered with the GCMRegistrar");
//        }else{
//            Log.v(Client.TAG, "Client is already initialized with GCMid: " + regId);
//            if (isPushEnabled()){
//                Log.v(Client.TAG, "Client is fully initialized and push is enabled");
//            }else{
//                Log.v(Client.TAG, "About to register with Kinvey");
//
//                getClient().push().enablePushViaRest(new KinveyClientCallback() {
//                    @Override
//                    public void onSuccess(Object result) {
//                        Log.v(TAG, "User is registered for push!");
//                    }
//
//                    @Override
//                    public void onFailure(Throwable error) {
//                        Log.v(Client.TAG, "GCM - user update error: " + error);
//                    }
//                }, regId);
//            }
//        }
        return this;
    }

    
    private void registerWithKinvey(String gcmID, boolean register) {
        registerWithKinvey(client, gcmID, register);
    }

    public void registerWithKinvey(final Client client, final String gcmRegID, boolean register) {
        //registered on GCM but not on Kinvey?
        Log.v(Client.TAG, "about to register with Kinvey");
        if (client == null) {
            Log.e(Client.TAG, "GCMService got garbage collected, cannot complete registration!");
            return;
        }

        if (!client.user().isUserLoggedIn()) {
            Log.e(Client.TAG, "Need to login a current user before registering for push!");
            return;
        }

        if (register) {

            client.push().enablePushViaRest(new KinveyClientCallback() {
                @Override
                public void onSuccess(Object result) {
                	Intent reg = new Intent(client.getContext(), KinveyGCMService.class);
                	reg.putExtra(KinveyGCMService.TRIGGER, KinveyGCMService.REGISTERED);
                	client.getContext().startService(reg);
                }

                @Override
                public void onFailure(Throwable error) {
                    Log.v(Client.TAG, "GCM - user update error: " + error);
                }
            }, gcmRegID);

        } else {
            client.push().disablePushViaRest(new KinveyClientCallback() {
                @Override
                public void onSuccess(Object result) {
                	Intent reg = new Intent(client.getContext(), KinveyGCMService.class);
                	reg.putExtra(KinveyGCMService.TRIGGER, KinveyGCMService.UNREGISTERED);
                	client.getContext().startService(reg);                }

                @Override
                public void onFailure(Throwable error) {
                    Log.v(Client.TAG, "GCM - user update error: " + error);
                }
            }, gcmRegID);

        }
    }




    /**
     * Get the Registration ID from GCM for the Client's current application context.
     *
     * Note if the current user is not registered, the registration ID will be an empty string.
     *
     * @return - the current user's GCM registration ID or an empty string ""
     */
    @Override
    public String getPushId() {
        if (getClient().getContext() == null){
            return "";
        }else{
            return GCMRegistrar.getRegistrationId(getClient().getContext());
        }
    }

    /**
     * Check to see if the current user is registered for GCM.  This checks both with GCM directly as well as with a Kinvey backend.
     *
     * As registration occurs asynchronously, ensure your `KinveyGCMService` has received the onRegister call first.
     *
     * @return true if current user is registered, false if they are not.
     */
    @Override
    public boolean isPushEnabled() {
        if (getClient() == null || getClient().getContext() == null){
            return false;
        }
        String gcmID = GCMRegistrar.getRegistrationId(getClient().getContext());

        if (getClient().user().containsKey("_push")){
            AbstractMap pushField = (AbstractMap) getClient().user().get("_push");
            if (pushField.containsKey("GCM")){
                AbstractMap gcmField = (AbstractMap) pushField.get("GCM");
                if (gcmField.containsKey("ids")){
                    List<String> ids = (ArrayList<String>) gcmField.get("ids");
                    for (String s : ids){
                        if (s.equals(gcmID)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
      //  */
    }

    /**
     * Unregisters the current user with GCM
     *
     * Unregistration is asynchronous, so use the `KinveyGCMService` to receive notification when unregistration has completed.
     *
     */
    @Override
    public void disablePush() {
        GCMRegistrar.unregister(getClient().getContext());
    }


    /**
     * Is GCM Push configured for production or a dev environment?
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
        new AsyncEnablePush(callback, deviceID).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);



    }

    @Override
    public void disablePushViaRest(KinveyClientCallback callback, String deviceID){
        new AsyncDisablePush(callback, deviceID).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);


    }


    private class AsyncEnablePush extends AsyncClientRequest{

        String deviceID;

        public AsyncEnablePush(KinveyClientCallback callback, String deviceID) {
            super(callback);
            this.deviceID = deviceID;
        }

        @Override
        protected Void executeAsync() throws IOException {

            PushRegistration ent = new PushRegistration(deviceID);
            RegisterPush p = new RegisterPush(ent);
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
        protected Void executeAsync() throws IOException {

            PushRegistration ent = new PushRegistration(deviceID);
            UnregisterPush p = new UnregisterPush(ent);
            getClient().initializeRequest(p);
            p.execute();

            return null;
        }
    }

}
