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
package com.kinvey.sample.contentviewr;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.sample.contentviewr.core.ContentFragment;

/**
 * @author edwardf
 */
public class LoginFragment extends ContentFragment implements View.OnClickListener {

    Button login;
    //Button register;
    EditText username;
    EditText password;
    TextView userLabel;
    TextView passLabel;
    private Typeface roboto;


    @Override
    public void onCreate(Bundle saved){
        super.onCreate(saved);
        setHasOptionsMenu(true);
    }

    @Override
    public int getViewID() {
        return R.layout.fragment_login;
    }

    @Override
    public void bindViews(View v) {
        roboto = Typeface.createFromAsset(getSherlockActivity().getAssets(), "Roboto-Thin.ttf");

        login = (Button) v.findViewById(R.id.login_go);
        //register = (Button) v.findViewById(R.id.login_register);
        username = (EditText) v.findViewById(R.id.login_username);
        password = (EditText) v.findViewById(R.id.login_password);
        userLabel = (TextView) v.findViewById(R.id.login_userlabel);
        passLabel = (TextView) v.findViewById(R.id.login_passlabel);

//        login.setTypeface(roboto);
//        register.setTypeface(roboto);
//        username.setTypeface(roboto);
//        password.setTypeface(roboto);
        passLabel.setTypeface(roboto);
        userLabel.setTypeface(roboto);

        if (client().user().isUserLoggedIn()){
            login.setText("Logout");
        }


        login.setOnClickListener(this);
        //register.setOnClickListener(this);
    }

    @Override
    public String getTitle() {
        return "Login";
    }

    @Override
    public void onClick(View v) {
        if (v == login){
            login(username.getText().toString(), password.getText().toString());
//        }else if(v == register){
//            register(username.getText().toString(), password.getText().toString());
        }
    }

    private void login(String username, String password){

        if (client().user().isUserLoggedIn()){
            client().user().logout().execute();
            getSherlockActivity().finish();
            return;
        }

        client().user().login(username, password, new KinveyUserCallback() {
            @Override
            public void onSuccess(User result) {
                if (getSherlockActivity() == null){
                    return;

                }
                ((SettingsActivity) getSherlockActivity()).showContent();
            }

            @Override
            public void onFailure(Throwable error) {
                if (getSherlockActivity() == null){
                    return;
                }
                Toast.makeText(getSherlockActivity(), "Couldn't login: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

    }

    private void register(String username, String password){
        client().user().create(username, password, new KinveyUserCallback() {
            @Override
            public void onSuccess(User result) {
                if (getSherlockActivity() == null){
                    return;
                }
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onFailure(Throwable error) {
                if (getSherlockActivity() == null){
                    return;
                }
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_login, menu);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sign_out:
                client().user().logout().execute();
                getSherlockActivity().finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Client client(){
        return ((ContentViewrApplication)((SettingsActivity) getSherlockActivity()).getApplicationContext()).getClient();
    }
}
