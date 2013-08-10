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
package com.kinvey.sample.kitchensink.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.sample.kitchensink.*;

/**
 * @author edwardf
 * @since 2.0
 */
public class LoginFragment extends UseCaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private EditText username;
    private EditText password;
    private Button login;
    private TextView usernameLabel;
    private TextView passwordLabel;
    private TextView implicitLabel;
    private CheckBox implicit;

    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MIN_PASSWORD_LENGTH = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public int getViewID() {
        return R.layout.fragment_login;
    }

    @Override
    public void bindViews(View v) {
        username = (EditText) v.findViewById(R.id.et_login);
        password = (EditText) v.findViewById(R.id.et_password);

        usernameLabel = (TextView) v.findViewById(R.id.login_label_username);
        passwordLabel = (TextView) v.findViewById(R.id.login_label_password);
        implicitLabel = (TextView) v.findViewById(R.id.login_label_implicit);

        login = (Button) v.findViewById(R.id.login);

        implicit = (CheckBox) v.findViewById(R.id.login_anon_checkbox);


        login.setTypeface(getRoboto());
        usernameLabel.setTypeface(getRoboto());
        passwordLabel.setTypeface(getRoboto());
        implicitLabel.setTypeface(getRoboto());
        login.setOnClickListener(this);
        implicit.setOnCheckedChangeListener(this);

        addEditListeners();


    }

    @Override
    public String getTitle() {
        return "Login";
    }


    @Override
    public void onClick(View v) {
        if (v == login) {

            if (implicit.isChecked()) {
                ((KitchenSinkApplication) getSherlockActivity().getApplicationContext()).getClient().user().login(new KinveyUserCallback() {
                    @Override
                    public void onSuccess(User result) {
                        if (getSherlockActivity() == null) {
                            return;
                        }
                        CharSequence text = "Logged in " + result.get("username") + ".";
                        Toast.makeText(getSherlockActivity().getApplicationContext(), text, Toast.LENGTH_LONG).show();
                        loggedIn();
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        if (getSherlockActivity() == null) {
                            return;
                        }
                        CharSequence text = "Something went wrong -> " + error;
                        Toast toast = Toast.makeText(getSherlockActivity().getApplicationContext(), text, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.show();
                    }

                });

            } else {
                ((KitchenSinkApplication) getSherlockActivity().getApplicationContext()).getClient()
                .user().login(username.getText().toString(), password.getText().toString(), new KinveyUserCallback() {
                    @Override
                    public void onSuccess(User result) {
                        if (getSherlockActivity() == null) {
                            return;
                        }
                        CharSequence text = "Logged in " + result.get("username") + ".";
                        Toast.makeText(getSherlockActivity().getApplicationContext(), text, Toast.LENGTH_LONG).show();
                        loggedIn();
                    }

                    public void onFailure(Throwable t) {
                        if (getSherlockActivity() == null) {
                            return;
                        }
                        CharSequence text = "Something went wrong -> " + t;
                        Toast toast = Toast.makeText(getSherlockActivity().getApplicationContext(), text, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.show();
                    }
                });
            }


        }
    }

    protected void addEditListeners() {
        login.setEnabled(validateInput());

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                login.setEnabled(validateInput());
            }
        });

        username.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if ((actionId == EditorInfo.IME_ACTION_NEXT
                                || actionId == EditorInfo.IME_ACTION_DONE
                                || (event.getAction() == KeyEvent.ACTION_DOWN
                                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                                && username.getText().length() < MIN_USERNAME_LENGTH
                                ) {

                            CharSequence text = "User name must contain at least " + MIN_USERNAME_LENGTH + " characters";
                            Toast.makeText(getSherlockActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                login.setEnabled(validateInput());
            }
        });

        password.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if ((actionId == EditorInfo.IME_ACTION_NEXT
                                || actionId == EditorInfo.IME_ACTION_DONE
                                || (event.getAction() == KeyEvent.ACTION_DOWN
                                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                                && password.getText().length() < MIN_USERNAME_LENGTH
                                ) {
                            CharSequence text = "Password must contain at least " + MIN_PASSWORD_LENGTH + " characters";
                            Toast.makeText(getSherlockActivity(), text, Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                });


    }

    public boolean validateInput() {
        return ((username.toString().length() >= MIN_USERNAME_LENGTH
                && password.getText().length() >= MIN_PASSWORD_LENGTH
        )) || implicit.isChecked();
    }

    private void loggedIn() {

        if (getSherlockActivity() != null && getSherlockActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(getSherlockActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getSherlockActivity().getCurrentFocus().getWindowToken(), 0);
        }
        getSherlockActivity().finish();


    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_login, menu);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_sign_up:
                FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(android.R.id.content, new RegisterFragment());
                ft.addToBackStack("register");
                ft.commit();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        username.setText("");
        username.setEnabled(!isChecked);
        password.setText("");
        password.setEnabled(!isChecked);
    }
}


