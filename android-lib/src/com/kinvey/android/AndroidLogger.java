package com.kinvey.android;

import android.util.Log;

import com.kinvey.java.Logger.KinveyLogger;

public class AndroidLogger implements KinveyLogger{

	@Override
	public void info(String message) {
		Log.i(Client.TAG, message);
	}

	@Override
	public void debug(String message) {
		Log.d(Client.TAG, message);		
	}

	@Override
	public void trace(String message) {
		Log.v(Client.TAG, message);		
	}

	@Override
	public void warning(String message) {
		Log.w(Client.TAG, message);		
	}

	@Override
	public void error(String message) {
		Log.e(Client.TAG, message);		
	}
}
