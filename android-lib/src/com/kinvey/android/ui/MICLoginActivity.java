package com.kinvey.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import com.kinvey.android.Client;
import com.kinvey.android.R;

/***
 * Provides a WebView for easy logging into MIC.
 */
public class MICLoginActivity extends Activity {

    public static final String KEY_LOGIN_URL = "loginURL";

    private Client myKinveyClient;
    private WebView micView;

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_miclogin);

        Intent i = getIntent();
        String loginURL = i.getStringExtra(KEY_LOGIN_URL);

        myKinveyClient = new Client.Builder(getApplicationContext()).build();
        micView = (WebView) findViewById(R.id.mic_loginview);

        loadLoginPage(loginURL);
    }

    private void loadLoginPage(String url){
        micView.loadUrl(url);
    }


    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        myKinveyClient.user().onOAuthCallbackRecieved(intent);
    }
}
