package com.example.secure_signin;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.kinvey.android.secure.Crypto;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;


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
            output.setText(id);

            String encrypted = Crypto.encrypt(id, "someuserID");
            output.setText(output.getText() + "\n\n" + encrypted);
            String decrypted = Crypto.decrypt(encrypted, "someuserID");
            output.setText(output.getText() + "\n\n" + decrypted);

            Log.i(TAG, "**********");
            Log.i(TAG, "**********");
            Log.i(TAG, id);
            Log.i(TAG, encrypted);
            Log.i(TAG, decrypted);
            Log.i(TAG, "**********");
            Log.i(TAG, "**********");
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "uh oh -> " + e.getMessage(), Toast.LENGTH_SHORT);
        }





        InitializeSQLCipher();
    }

    private void InitializeSQLCipher() {
        SQLiteDatabase.loadLibs(this);
        File databaseFile = getDatabasePath("demo.db");
        databaseFile.mkdirs();
        databaseFile.delete();
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, "test123", null);
        database.execSQL("create table t1(a, b)");
        database.execSQL("insert into t1(a, b) values(?, ?)", new Object[]{"one for the money",
                "two for the show"});
    }



}
