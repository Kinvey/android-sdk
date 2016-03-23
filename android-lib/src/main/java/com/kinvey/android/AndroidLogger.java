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
package com.kinvey.android;

import android.util.Log;

import com.kinvey.java.Logger.KinveyLogger;

/**
 * This class provides a logging implementation for the Android Library, using `android.util.Log.x`.  The TAG is "Kinvey - Client".
 *
 *
 * @author edwardf
 * @since 2.9.1
 */
public class AndroidLogger implements KinveyLogger{

    //Creates an instance of the Android Specific loggin mechanism
    public AndroidLogger(){}

    /**
     * Delegate an info message to Log.i
     * @param message the message to log
     */
	@Override
	public void info(String message) {
		Log.i(Client.TAG, message);
	}

    /**
     * Delegate an info message to Log.d
     * @param message the message to log
     */
	@Override
	public void debug(String message) {
		Log.d(Client.TAG, message);		
	}

    /**
     * Delegate an info message to Log.v
     * @param message the message to log
     */
	@Override
	public void trace(String message) {
		Log.v(Client.TAG, message);		
	}

    /**
     * Delegate an info message to Log.w
     * @param message the message to log
     */
	@Override
	public void warning(String message) {
		Log.w(Client.TAG, message);		
	}

    /**
     * Delegate an info message to Log.e
     * @param message the message to log
     */
	@Override
	public void error(String message) {
		Log.e(Client.TAG, message);		
	}
}
