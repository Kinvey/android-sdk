/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.sample.ticketview.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;

import com.kinvey.sample.ticketview.R;
import android.view.View;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class LoginFragment extends SherlockFragment implements View.OnClickListener {

    private EditText eUserName;
    private EditText ePassword;
    private Button bLogin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return inflater.inflate(R.layout.ticket_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews();
    }

    private void bindViews() {
        eUserName = (EditText) getActivity().findViewById(R.id.login_user_name);
        ePassword = (EditText) getActivity().findViewById(R.id.login_user_password);
        bLogin = (Button) getActivity().findViewById(R.id.login_button);
        bLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!((LoginActivity)getActivity()).isUserLoggedIn()) {
            ((LoginActivity) getActivity()).login(eUserName.getText().toString(), ePassword.getText().toString());
        }
    }

    private void showNotLoggedInToast(Throwable error) {
        if (error != null)
            Toast.makeText(getActivity(), "couldn't login -> " + error.getMessage(), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getActivity(), "couldn't login", Toast.LENGTH_LONG).show();
    }

}
