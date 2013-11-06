package com.example.secure_signin;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.kinvey.android.secure.Crypto;


public class Signin extends Activity {

    private static final String TAG = "secure-signin";
    private TextView output;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        output = (TextView) findViewById(R.id.output);
        secureArbitraryString();
    }


    public void secureArbitraryString(){
        try{
            String id = "how about this";
            output.setText("Securing arbitrary string: " + id);

            String encrypted = Crypto.encrypt(id);
            output.setText(output.getText() + "\n" + encrypted);
            String decrypted = Crypto.decrypt(encrypted);
            output.setText(output.getText() + "\n" + decrypted);





            Log.i(TAG, "**********");
            Log.i(TAG, "**********");
            Log.i(TAG, id);
            Log.i(TAG, encrypted);
            Log.i(TAG, decrypted);
            Log.i(TAG, "**********");
            Log.i(TAG, "**********");
        }catch (Exception e){
            e.printStackTrace();
        }





    }



}
