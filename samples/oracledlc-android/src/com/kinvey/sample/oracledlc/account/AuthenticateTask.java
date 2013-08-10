/** 
 * Copyright (c) 2013 Kinvey Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.kinvey.sample.oracledlc.account;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Key;

import java.io.IOException;

import com.kinvey.sample.oracledlc.OracleDLC;

/**
* @author m0rganic
* @since 2.0
*/
abstract class AuthenticateTask extends AsyncTask<String, Void, AuthnResponse> {

    private HttpTransport transport = AndroidHttp.newCompatibleTransport();
    private boolean refresh;

    private Activity activity;

    public AuthenticateTask(Activity activity) {
        this.activity = activity;
    }

    public AuthenticateTask(Activity activity, boolean refresh) {
        this(activity);
        this.refresh = refresh;
    }

    @Override
    protected AuthnResponse doInBackground(String... params) {
        JsonHttpContent authnPayload = null;

        if (refresh) {
            authnPayload = newRefreshTokenPayload(params[0]);
        } else {
            String userId = params[0];
            String pass = params[1];
            authnPayload = newUserPassCredentials(userId, pass);
        }

        HttpRequest authnRequest;
        HttpResponse response = null;
        try {
            authnRequest = buildHttpRequest(authnPayload);
            response = authnRequest.execute();

            if (response.isSuccessStatusCode())
                return response.parseAs(AuthnResponse.class);

        } catch (IOException e) {
            Log.e(OracleDLC.TAG, "failed to authenticate with auth link", e);
            this.activity.runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Failed login.", Toast.LENGTH_LONG).show();
                }
            });
        } finally {
            if (response != null) {
                try {
                    response.getContent().close();
                } catch (IOException e) {
                    Log.e(OracleDLC.TAG, "failed to close response", e);

                }
            }
        }
        return null;
    }

    private HttpRequest buildHttpRequest(JsonHttpContent authnPayload) throws IOException {
        HttpRequest authnRequest;
        GenericUrl endpoint = new GenericUrl(LoginActivity.AUTHORIZE_BASE_URL + (refresh? "/token":"/authorize"));
        authnRequest = transport.createRequestFactory().buildPostRequest(endpoint, authnPayload);
        authnRequest.getHeaders().setContentType("application/json");
        authnRequest.getHeaders().set("x-auth-key", LoginActivity.AUTH_API_KEY);
        authnRequest.setParser(new JsonObjectParser(new GsonFactory()));
        authnRequest.setEnableGZipContent(false);
        return authnRequest;
    }



    private JsonHttpContent newUserPassCredentials(String userId, String password) {
        return new JsonHttpContent(new GsonFactory(),
                new AuthnPayload(userId, password));
    }

    private JsonHttpContent newRefreshTokenPayload(String refreshToken) {
        return new JsonHttpContent(new GsonFactory(),
                new AuthnRefreshPayload(refreshToken));
    }

    public class AuthnRefreshPayload extends GenericJson {

        @Key("grant_type")
        private static final String grantType = "refresh_token";

        @Key("refresh_token")
        private String refreshToken;


        public AuthnRefreshPayload(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }

    protected class AuthnPayload extends GenericJson {

        @Key("userId")
        private String userId;

        @Key("password")
        private String password;

        public AuthnPayload(String userId, String password) {
            this.userId = userId;
            this.password = password;
        }

    }

}
