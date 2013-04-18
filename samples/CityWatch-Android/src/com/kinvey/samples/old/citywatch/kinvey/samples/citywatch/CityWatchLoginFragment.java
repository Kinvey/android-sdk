//package com.kinvey.samples.old.citywatch.kinvey.samples.citywatch;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//
//import com.actionbarsherlock.app.SherlockFragment;
//import com.actionbarsherlock.view.Menu;
//import com.actionbarsherlock.view.MenuInflater;
//import com.kinvey.samples.citywatch.R;
//
//public class CityWatchLoginFragment extends SherlockFragment {
//
//	private EditText mUsername;
//	private EditText mPassword;
//
//	private Button mKinvey;
//	private Button mFacebook;
//	private Button mTwitter;
//
//	public static CityWatchLoginFragment newInstance() {
//		return new CityWatchLoginFragment();
//	}
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setHasOptionsMenu(true);
//	}
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup group,
//			Bundle saved) {
//		View v = inflater.inflate(R.layout.fragment_login, group, false);
//		bindViews(v);
//		return v;
//	}
//
//	private void bindViews(View v) {
//		mUsername = (EditText) v.findViewById(R.id.login_username);
//		mPassword = (EditText) v.findViewById(R.id.login_password);
//		mKinvey = (Button) v.findViewById(R.id.login_kinvey);
//		mFacebook = (Button) v.findViewById(R.id.login_facebook);
//		mTwitter = (Button) v.findViewById(R.id.login_twitter);
//
//	}
//
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//
//		inflater.inflate(R.menu.fragment_login, menu);
//
//	}
//
//}