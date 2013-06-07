package com.kinvey.sample.kitchensink.push;


import android.util.Log;
import com.kinvey.android.Client;
import com.kinvey.android.push.KinveyGCMService;


public class GCMLoggingReceiver extends KinveyGCMService {
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
    public void onRegistered(String gcmID) {
        Log.i(Client.TAG, "GCM - onRegister, new gcmID is: " + gcmID);

    }

    @Override
    public void onUnregistered(String oldID) {
        Log.i(Client.TAG, "GCM - onUnregister");
    }






}
