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


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kinvey.android.Client;

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
    
    public static final String TRIGGER = "KINVEY_ACTION";
    public static final String REG_ID = "REGID";
    public static final String REGISTERED = "REGISTERED";
    public static final String UNREGISTERED = "UNREGISTERED";
    
    		
    		

    private Client client;


    /**
     * Public Constructor used by operating system.
     */
    public KinveyGCMService() {
        super("GCM PUSH");
    }
    
	@Override
	protected void onHandleIntent(Intent intent) {
		
		if (intent.getExtras().getString(TRIGGER, "default").equals(REGISTERED)){
			String regID = intent.getExtras().getString(REG_ID);
			onRegistered(regID);
			
		}else if (intent.getExtras().getString(TRIGGER, "default").equals(REGISTERED)){
			String regID = intent.getExtras().getString(REG_ID);
			onUnregistered(regID);
		}
		
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        
        String messageType = gcm.getMessageType(intent);
        Log.i("GCM", "handling intent");

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.i("GCM", "Send error: " + extras.toString());
                onError(extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            	Log.i("GCM", "Deleted messages on server: " + extras.toString());
            	onDelete(extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.i("GCM", "Received: " + extras.toString());
                onMessage(extras.toString());
            }
        }
        
        try {
        	Method complete = getReceiver().getMethod("completeWakefulIntent", Intent.class);
			complete.invoke(null, intent);
		} catch (Exception e) {
			Log.e(TAG, "couldn't complete wakeful intent!");
			e.printStackTrace();
		}
        
    
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
    public abstract void onDelete(String deleted);

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
    
	public abstract Class getReceiver() ;
    	
    


}
