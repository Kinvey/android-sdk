package com.kinvey.android.authentication;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KinveyAuthenticatorService extends Service {


    // Instance field that stores the authenticator object
    private KinveyAuthenticator mAuthenticator;
    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new KinveyAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
