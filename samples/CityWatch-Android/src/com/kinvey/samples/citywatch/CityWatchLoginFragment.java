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
package com.kinvey.samples.citywatch;

/**
 * @author mjsalinger
 * @since 2.0
 */

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

public class CityWatchLoginFragment extends SherlockFragment {

	private EditText mUsername;
	private EditText mPassword;

	private Button mKinvey;
	private Button mFacebook;
	private Button mTwitter;

	public static CityWatchLoginFragment newInstance() {
		return new CityWatchLoginFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group,
			Bundle saved) {
		View v = inflater.inflate(R.layout.fragment_login, group, false);
		bindViews(v);
		return v;
	}

	private void bindViews(View v) {
		mUsername = (EditText) v.findViewById(R.id.login_username);
		mPassword = (EditText) v.findViewById(R.id.login_password);
		mKinvey = (Button) v.findViewById(R.id.login_kinvey);
		mFacebook = (Button) v.findViewById(R.id.login_facebook);
		mTwitter = (Button) v.findViewById(R.id.login_twitter);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.fragment_login, menu);

	}

}
