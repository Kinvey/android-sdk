/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */

package com.kinvey.sample.signin;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Service for KinveyAccountAuthenticator Requests
 */
public class KinveyAuthenticationService extends Service {
	private static final String TAG = "AccountAuthenticatorService";
	private static KinveyAccountAuthenticator sAccountAuthenticator = null;
	
	@Override
	public IBinder onBind(Intent intent) {
		IBinder ret = null;
		if (intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
			ret = new KinveyAccountAuthenticator(this).getIBinder();
		}
		return ret;	
	}
	
	private KinveyAccountAuthenticator getAuthenticator()
	{
		if (sAccountAuthenticator == null) {
			sAccountAuthenticator = new KinveyAccountAuthenticator(this);
		}
		
	return sAccountAuthenticator;
	}

}
