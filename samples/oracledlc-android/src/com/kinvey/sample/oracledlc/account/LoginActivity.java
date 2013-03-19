/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.sample.oracledlc.account;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.sample.oracledlc.*;

/**
 *
 */
public class LoginActivity extends Activity implements View.OnClickListener {

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

            AuthenticateTask authenticate = new AuthenticateTask();
            authenticate.execute(eUserName.getText().toString(), ePassword.getText().toString());


//            getClient().user().login(eUserName.getText().toString(), ePassword.getText().toString(), new KinveyUserCallback() {
//                @Override
//                public void onSuccess(User result) {
//                    Intent feature = new Intent(LoginActivity.this, OracleDLC.class);
//                    startActivity(feature);
//                }
//
//                @Override
//                public void onFailure(Throwable error) {
//                    AndroidUtil.toast(LoginActivity.this, "couldn't login -> " + error.getMessage());
//                }
//            });

        }

    }

    private void showNotLoggedInToast(Throwable error) {
        if (error != null)
            AndroidUtil.toast(LoginActivity.this, "couldn't login -> " + error.getMessage());
        else
            AndroidUtil.toast(LoginActivity.this, "couldn't login");
    }

    private void loginSucceeded() {
        Intent feature = new Intent(LoginActivity.this, OracleDLC.class);
        startActivity(feature);
    }

    private boolean isUserLoggedIn() {
        return getClient().user().isUserLoggedIn();
    }

    private Client getClient() {
        return ((OracleDLCApplication) getApplicationContext()).getClient();
    }

    protected static class AuthnPayload extends GenericJson {

        @Key("userId")
        private String userId;

        @Key("password")
        private String password;

        public AuthnPayload(String userId, String password) {
            this.userId = userId;
            this.password = password;
        }

    }

    public static class AuthnResponse extends GenericJson {
        @Key ("access_token")
        private String accessToken;

        @Key ("refresh_token")
        private  String refreshToken;

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }


    private static HttpTransport transport = AndroidHttp.newCompatibleTransport();
    private class AuthenticateTask extends AsyncTask<String, Void, AuthnResponse> {

        @Override
        protected AuthnResponse doInBackground(String... params) {

            String userId = params[0];
            String pass = params[1];
            JsonHttpContent authnPayload = newJsonHttpContent(userId, pass);
            HttpRequest authnRequest;
            try {
                authnRequest = transport.createRequestFactory().buildPostRequest(new GenericUrl("https://rdsdw.kinvey.com/ldap-auth-link/authorize"), authnPayload);
                authnRequest.getHeaders().setContentType("application/json");
                authnRequest.getHeaders().set("x-auth-key", "8b4b1d7-8b3a-11e2-be63-3c075415b1e5~");
                authnRequest.setParser(new JsonObjectParser(new GsonFactory()));
                authnRequest.setEnableGZipContent(false);
                HttpResponse response = authnRequest.execute();

                if (response.isSuccessStatusCode())
                    return response.parseAs(AuthnResponse.class);

            } catch (IOException e) {
                Log.e(OracleDLC.TAG, "failed to authenticate with auth link", e);
            }
            return null;
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
                        LoginActivity.this.loginSucceeded();
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        LoginActivity.this.showNotLoggedInToast(error);
                    }
                });

            }


        }


    }

    private static JsonHttpContent newJsonHttpContent(String userId, String password) {
        return new JsonHttpContent(new GsonFactory(),
                new LoginActivity.AuthnPayload(userId, password));
    }
}
