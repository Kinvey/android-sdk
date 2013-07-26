/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.sample.oracledlc.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.sample.oracledlc.*;

/**
 *
 */
public class LoginActivity extends Activity implements View.OnClickListener {

//    public static final String AUTHORIZE_BASE_URL = "https://rdsdw.kinvey.com/ldap-auth-link";
    public static final String AUTHORIZE_BASE_URL = "http://192.168.1.23:5000";
    public static final String AUTH_API_KEY = "8b4b1d7-8b3a-11e2-be63-3c075415b1e5~";
    private static final String BUNDLE_KEY_REFRESH_TOKEN = "refreshToken";

    private EditText eUserName;
    private EditText ePassword;
    private Button bLogin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        bindViews();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void bindViews() {
        eUserName = (EditText) findViewById(R.id.login_user_name);
        ePassword = (EditText) findViewById(R.id.login_user_password);
        bLogin = (Button) findViewById(R.id.login_button);

        bLogin.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (!isUserLoggedIn()) {
            AuthenticateTask authenticate = new LoginAuthenticateTask(this);
            authenticate.execute(eUserName.getText().toString(), ePassword.getText().toString());
        }
    }

    private void showNotLoggedInToast(Throwable error) {
        if (error != null)
            AndroidUtil.toast(LoginActivity.this, "couldn't login -> " + error.getMessage());
        else
            AndroidUtil.toast(LoginActivity.this, "couldn't login");
    }

    public class LoginAuthenticateTask extends AuthenticateTask {

        public LoginAuthenticateTask(Activity activity) {
            super(activity);
        }

        @Override
        protected void onPostExecute(AuthnResponse authnResponse) {
            if (authnResponse == null) {
                LoginActivity.this.showNotLoggedInToast(null);
            }
            else {
                getClient().user().loginAuthLink(authnResponse.getAccessToken(), authnResponse.getRefreshToken(), new KinveyUserCallback() {
                    @Override
                    public void onSuccess(User result) {
                        Intent feature = new Intent(LoginActivity.this, OracleDLC.class);
                        startActivity(feature);
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        LoginActivity.this.showNotLoggedInToast(error);
                    }
                });

            }
        }
    }

    private void loginSucceeded(String refreshToken) {
        Intent feature = new Intent(LoginActivity.this, OracleDLC.class);
        feature.getExtras().putString(BUNDLE_KEY_REFRESH_TOKEN, refreshToken);
        startActivity(feature);
    }

    private boolean isUserLoggedIn() {
        return getClient().user().isUserLoggedIn();
    }

    private Client getClient() {
        return ((OracleDLCApplication) getApplicationContext()).getClient();
    }


}
