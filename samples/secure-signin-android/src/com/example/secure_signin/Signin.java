package com.example.secure_signin;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.kinvey.android.secure.Crypto;
import android.util.Base64;


public class Signin extends Activity {

    private static final String TAG = "secure-signin";
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        secureArbitraryString();
    }


    public void secureArbitraryString(){
        try{
            String id = "how about this";
            String encrypted = Crypto.encrypt(id, "passcode");
            String decrypted = Crypto.decrypt(encrypted, "passcode");

            String b64 = Base64.encodeToString(id.getBytes(), Base64.DEFAULT);



            Log.i(TAG, "**********");
            Log.i(TAG, "**********");
            Log.i(TAG, id);
            Log.i(TAG, encrypted);
            Log.i(TAG, b64);
            Log.i(TAG, decrypted);
            Log.i(TAG, "**********");
            Log.i(TAG, "**********");
        }catch (Exception e){
            e.printStackTrace();
        }





    }



}
