package com.kinvey.android.authentication;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class KinveyAuthenticatorService extends Service {

    private static final String TAG = "KinveyAccountService";
    private static final String ACCOUNT_TYPE = "com.kinvey.android.authentication";
    public static final String ACCOUNT_NAME = "Kinvey";

    // Instance field that stores the authenticator object
    private KinveyAuthenticator mAuthenticator;
    @Override
    public void onCreate() {
        // Create a new authenticator object
        Log.i(TAG, "KinveyAuthenticatorService created");
        mAuthenticator = new KinveyAuthenticator(this);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "KinveyAuthenticatorService destroyed");
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
