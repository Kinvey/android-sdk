package com.example.secure_signin;

import android.accounts.AccountAuthenticatorActivity;
import android.app.Activity;
import android.os.Bundle;

public class Signin extends Activity {
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
        String id = "how about this";


    }
}
