package com.kinvey.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import com.kinvey.android.Client;
import com.kinvey.android.R;

/***
 * Provides a WebView for easy logging into MIC.
 */
public class MICLoginActivity extends Activity {

    public static final String KEY_LOGIN_URL = "loginURL";

    private WebView micView;

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_miclogin);

        Intent i = getIntent();
        String loginURL = i.getStringExtra(KEY_LOGIN_URL);
        Log.i("WTF", "ok loginurl is: " + loginURL);
        
        if (loginURL == null){
        	Log.i("WTF", "null enough to finish");
        	onNewIntent(this.getIntent());
        	return;
        }else{
        	Log.i("WTF", "not null enough");
        }
        
        
        micView = (WebView) findViewById(R.id.mic_loginview);
        Log.i("WTF", "ok micWebview is good: " + (micView != null));
        loadLoginPage(loginURL);
    }

    private void loadLoginPage(String url){
    	
        micView.loadUrl(url);
    }


    @Override
    public void onNewIntent(Intent intent){
        Log.i("WTF", "ok New Itnent");

        super.onNewIntent(intent);
        Client.sharedInstance().user().onOAuthCallbackRecieved(intent);
        this.finish();
    }
}
