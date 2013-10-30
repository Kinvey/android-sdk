package com.example.secure_signin;

import android.accounts.AccountAuthenticatorActivity;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.kinvey.android.Crypto;
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
            String encrypted = Crypto.encrypt(id);
            String decrypted = Crypto.decrypt(encrypted);

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

//    private Context ctx;
//
//    public void  DataSec(Context ctx)
//    {
//        this.ctx = ctx;
//    }
//
//    public void genKey() throws Exception
//    {
//        SecretKey key = KeyGenerator.getInstance("AES").generateKey();
//
//        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
//        ks.load(null, "clavedekey".toCharArray());
//
//        KeyStore.PasswordProtection pass = new KeyStore.PasswordProtection("fedsgjk".toCharArray());
//        KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(key);
//        ks.setEntry("secretKeyAlias", skEntry, pass);
//
//        FileOutputStream fos = ctx.openFileOutput("bs.keystore", Context.MODE_PRIVATE);
//        ks.store(fos, "clavedekey".toCharArray());
//        fos.close();
//    }

}
