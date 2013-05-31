package com.kinvey.android.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.kinvey.android.Client;


public class GCMLoggingReceiver extends AbstractGCMReceiver {
    @Override
    public void onMessage(String message) {
        Log.i(Client.TAG, "GCM - onMessage: " + message);
    }

    @Override
    public void onError(String error) {
        Log.i(Client.TAG, "GCM - onError: " + error);
    }

    @Override
    public void onDelete(int deleteCount) {
        Log.i(Client.TAG, "GCM - onDelete, message deleted count: " + deleteCount);
    }

    @Override
    public void onRegister(String gcmID) {
        Log.i(Client.TAG, "GCM - onRegister, new gcmID is: " + gcmID);
    }

    @Override
    public void onUnregister() {
        Log.i(Client.TAG, "GCM - onUnregister");
    }




}
