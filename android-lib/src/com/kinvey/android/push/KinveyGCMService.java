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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;

/**
 * IntentService responsible for handling GCM messages.
 * <p>
 * Upon successful registration/unregistration with GCM, this class will perform the appropriate action with Kinvey as well.
 * </p>
 * <p/>
 * To use GCM for push notifications, extend this class and implement the provided abstract methods.  When GCM related events occur, they relevant method will be called by the library.
 * <p/>
 *
 * @author edwardf
 * @since 2.0
 */
public abstract class KinveyGCMService extends IntentService {

    public static final String MESSAGE_FROM_GCM = "msg";
    public static final String TAG = "KINVEY-GCM";

    private Client client;


    /**
     * Public Constructor used by operating system.
     */
    public KinveyGCMService() {
        super("GCM PUSH");
    }
    
	@Override
	protected void onHandleIntent(Intent intent) {
    
    new AsyncTask<Void, Void, String>() {
        @Override
        protected String doInBackground(Void... params) {
        	Log.i("GCM", "doinbackground");
            String msg = "";
            try {
            	
            	
//                if (gcm == null) {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
//                }
            	Log.i("GCM", "bout to register");
            	
            	Client c = null;//
            	
                final String regid = gcm.register(c.push().getSenderIDs());
                msg = "Device registered, registration ID=" + regid;
                Log.i("GCM", "regid is " + regid);

                // You should send the registration ID to your server over HTTP,
                // so it can use GCM/HTTP or CCS to send messages to your app.
                // The request to your server should be authenticated if your app
                // is using accounts.
                
                registerWithKinvey(c, regid, true);

                
                
                //client().push().initialize(getSherlockActivity().getApplication());
                

                // For this demo: we don't need to send it because the device
                // will send upstream messages to a server that echo back the
                // message using the 'from' address in the message.

                // Persist the regID - no need to register again.
               // storeRegistrationId(NotificationFragment.this.getActivity(), regid);
            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();
                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) {
            //mDisplay.append(msg + "\n");
        }
    }.execute(null, null, null);

    
	}
    
    
    
    
    
    
    protected void onRegistered(Context context, final String registrationId) {
        Log.v(TAG, "Device registered: regId = " + registrationId);


        Client.Builder builder;
        if (getAppKey() == null) {
            builder = new Client.Builder(context);
        } else {
            builder = new Client.Builder(getAppKey(), getAppSecret(), context);
        }

        if (gcmEnabled()) {
            builder.setSenderIDs(getSenderIDs());
            builder.setGcmInProduction(inProduction());
            builder.enableGCM(gcmEnabled());
        }
        if (getBaseURL() != null) {
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

    public String getSenderIDs() {
        return null;
    }

    public String getAppKey() {
        return null;
    }

    public String getAppSecret() {
        return null;
    }

    public boolean gcmEnabled() {
        return false;
    }

    public boolean inProduction() {
        return true;
    }

    public String getBaseURL() {
        return null;
    }


    
    protected void onUnregistered(Context context, final String registrationId) {
        Log.v(TAG, "Device unregistered");

        Client.Builder builder;
        if (getAppKey() == null) {
            builder = new Client.Builder(context);
        } else {
            builder = new Client.Builder(getAppKey(), getAppSecret(), context);
        }

        if (gcmEnabled()) {
            builder.setSenderIDs(getSenderIDs());
            builder.setGcmInProduction(inProduction());
            builder.enableGCM(gcmEnabled());
        }
        if (getBaseURL() != null) {
            builder.setBaseUrl(getBaseURL());
        }

        builder.setRetrieveUserCallback(new KinveyUserCallback() {
            @Override
            public void onSuccess(User result) {
                Log.v(TAG, "retrieved user, about to register with Kinvey.");
                registerWithKinvey(registrationId, false);
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "GCM registration failed to retrieve user!");
            }
        });

        client = builder.build();

    }

    
    protected void onMessage(Context context, Intent intent) {
        String message = intent.getStringExtra(MESSAGE_FROM_GCM);
        Log.v(TAG, "Received message -> " + message);
        onMessage(message);

    }

    
    protected void onDeletedMessages(Context context, int total) {
        Log.v(TAG, "Received deleted messages notification");
        onDelete(total);

    }

    
    public void onError(Context context, String errorId) {
        Log.v(TAG, "Received error: " + errorId);
        onError(errorId);
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
                    KinveyGCMService.this.onRegistered(gcmRegID);
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
                    KinveyGCMService.this.onUnregistered(gcmRegID);
                }

                @Override
                public void onFailure(Throwable error) {
                    Log.v(Client.TAG, "GCM - user update error: " + error);
                }
            }, gcmRegID);

        }
    }

    @Override
    public void onDestroy() {
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
    public abstract void onUnregistered(String oldID);


}
