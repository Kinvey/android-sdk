/*
 * Copyright (c) 2013 Kinvey Inc.
 *
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
package com.kinvey.sample.ticketview.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.sample.ticketview.TicketViewActivity;
import com.kinvey.sample.ticketview.TicketViewApplication;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class LoginActivity extends SherlockFragmentActivity {
    public static final String AUTHORIZE_BASE_URL = "https://daemos.kinvey.com/";
    public static final String AUTH_API_KEY = "8b4b1d7-8b3a-11e2-be63-3c075415b1e5~";

    private static final String BUNDLE_KEY_REFRESH_TOKEN = "refreshToken";

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateLoginFragment();
    }

    private void inflateLoginFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, new LoginFragment());
        ft.commit();
    }

    private void loginSucceeded(String refreshToken) {
        Intent feature = new Intent(LoginActivity.this, TicketViewActivity.class);
        feature.getExtras().putString(BUNDLE_KEY_REFRESH_TOKEN, refreshToken);
        startActivity(feature);
    }

    boolean isUserLoggedIn() {
        return getClient().user().isUserLoggedIn();
    }

    Client getClient() {
        return ((TicketViewApplication) getApplicationContext()).getClient();
    }

    void showNotLoggedInToast(Throwable error) {
        if (error != null)
            Toast.makeText(this, "couldn't login -> " + error.getMessage(), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "couldn't login", Toast.LENGTH_LONG).show();
    }

    void login(String username, String password) {
        this.username = username;
        AuthTask authenticate = new LoginActivity.LoginAuthenticateTask(this);
        authenticate.execute(username, password);
    }

    void logout() {
        getClient().user().logout();
    }

    void updateUserName(User user) {
        String name = user.get("name").toString();
        if (name == null || !name.equals(this.username)) {
            getClient().user().set("name",this.username);
            getClient().user().update(new KinveyUserCallback() {
                @Override
                public void onSuccess(User result) {

                    Log.i("myTag", "User updated");
                    Intent feature = new Intent(LoginActivity.this, TicketViewActivity.class);
                    startActivity(feature);
                }

                @Override
                public void onFailure(Throwable error) {
                    Log.e("myTag", "user not updated");
                }
            });
        } else {
            Intent feature = new Intent(LoginActivity.this, TicketViewActivity.class);
            startActivity(feature);
        }
    }

    public class LoginAuthenticateTask extends AuthTask {

        public LoginAuthenticateTask(Activity activity) {
            super(activity);
        }

        @Override
        protected void onPostExecute(AuthResponse authnResponse) {
            if (authnResponse == null) {
                LoginActivity.this.showNotLoggedInToast(null);
            }
            else {

                getClient().user().loginAuthLink(authnResponse.getAccessToken(), authnResponse.getRefreshToken(), new KinveyUserCallback() {
                    @Override
                    public void onSuccess(User result) {
                        LoginActivity.this.updateUserName(result);
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        LoginActivity.this.showNotLoggedInToast(error);
                    }
                });

            }
        }
    }

}
