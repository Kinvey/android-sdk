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

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.kinvey.sample.contentviewr.core.ContentFragment;

/**
 * @author edwardf
 */
public class LoginFragment extends ContentFragment implements View.OnClickListener {

    Button login;
    Button register;
    EditText username;
    EditText password;


    @Override
    public int getViewID() {
        return R.layout.fragment_login;
    }

    @Override
    public void bindViews(View v) {
        login = (Button) v.findViewById(R.id.login_go);
        register = (Button) v.findViewById(R.id.login_register);
        username = (EditText) v.findViewById(R.id.login_username);
        password = (EditText) v.findViewById(R.id.login_password);
        login.setOnClickListener(this);
        register.setOnClickListener(this);
    }

    @Override
    public String getTitle() {
        return "Login";
    }

    @Override
    public void onClick(View v) {
        if (v == login){
            login(username.getText().toString(), password.getText().toString());
        }else if(v == register){
            register(username.getText().toString(), password.getText().toString());
        }
    }

    private void login(String username, String password){

    }

    private void register(String username, String password){

    }
}
