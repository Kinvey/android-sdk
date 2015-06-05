package com.example.testdrive.android;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.kinvey.android.push.KinveyGCMService;

public class GCMService extends KinveyGCMService {

	@Override
	public void onMessage(String message) {
		displayNotification(message);
	}

	@Override
	public void onError(String error) {
		displayNotification(error);
	}

	@Override
	public void onDelete(String deleted) {
		displayNotification(deleted);
	}

	@Override
	public void onRegistered(String gcmID) {
		displayNotification(gcmID);
		
	}

	@Override
	public void onUnregistered(String oldID) {
		displayNotification(oldID);
	}

	public Class getReceiver() {
		return GCMReceiver.class;
	}
	
	
	
	private void displayNotification(String message){
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle(getApplicationContext().getResources().getString(R.string.app_name))
			.setContentText(message);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(1, mBuilder.build());
	}

}
