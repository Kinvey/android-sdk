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
package com.kinvey.sample.kitchensink.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.sample.kitchensink.*;

/**
 * @author edwardf
 * @since 2.0
 */
public class LoginActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private EditText eUserName;
    private EditText ePassword;
    private Button bLogin;
    private CheckBox cImplicit;

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
        cImplicit = (CheckBox) findViewById(R.id.login_anon_checkbox);

        bLogin.setOnClickListener(this);
        cImplicit.setOnCheckedChangeListener(this);

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        eUserName.setText("");
        eUserName.setEnabled(!isChecked);
        ePassword.setText("");
        ePassword.setEnabled(!isChecked);

    }

    @Override
    public void onClick(View v) {

        if (!isUserLoggedIn()) {

            if (cImplicit.isChecked()) {
                getClient().user().login(new KinveyUserCallback() {
                    @Override
                    public void onSuccess(User result) {
                        Intent feature = new Intent(LoginActivity.this, KitchenSink.class);
                        startActivity(feature);
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        AndroidUtil.toast(LoginActivity.this, "couldn't login -> " + error.getMessage());
                    }

                });

            } else {
                getClient().user().login(eUserName.getText().toString(), ePassword.getText().toString(), new KinveyUserCallback() {
                    @Override
                    public void onSuccess(User result) {
                        Intent feature = new Intent(LoginActivity.this, KitchenSink.class);
                        startActivity(feature);
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        AndroidUtil.toast(LoginActivity.this, "couldn't login -> " + error.getMessage());
                    }
                });

            }
        }

    }

    private boolean isUserLoggedIn() {
        return getClient().user().isUserLoggedIn();
    }

    private Client getClient() {
        return ((KitchenSinkApplication) getApplicationContext()).getClient();
    }
}
