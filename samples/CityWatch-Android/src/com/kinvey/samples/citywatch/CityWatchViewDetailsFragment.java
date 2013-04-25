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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

public class CityWatchViewDetailsFragment extends SherlockFragment {

	private static final String TAG = CityWatchViewDetailsFragment.class.getSimpleName();

	private ImageView mImage;
	private TextView mName;
	private TextView mCatagory;
	private TextView mLocation;
	private TextView mDescription;
	private TextView mSeverity;
	private TextView mRisk;
    private static Typeface robotoThin;

	public static CityWatchViewDetailsFragment newInstance() {
		return new CityWatchViewDetailsFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
		View v = inflater.inflate(R.layout.fragment_view_details, group, false);
		bindViews(v);
		populateViews(((CityWatch) getSherlockActivity()).getCurEntity());
        getSherlockActivity().getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		return v;
	}

	private void bindViews(View v) {
		mImage = (ImageView) v.findViewById(R.id.view_details_image);
		mName = (TextView) v.findViewById(R.id.view_details_name);
		mCatagory = (TextView) v.findViewById(R.id.view_details_category);
		mLocation = (TextView) v.findViewById(R.id.view_details_location);
		mDescription = (TextView) v.findViewById(R.id.view_details_description);
		mSeverity = (TextView) v.findViewById(R.id.view_details_severity);
		mRisk = (TextView) v.findViewById(R.id.view_details_risk);

        robotoThin = Typeface.createFromAsset(getSherlockActivity().getAssets(), "Roboto-Thin.ttf");
        TextView header = (TextView) v.findViewById(R.id.header_report_details);
        header.setTypeface(robotoThin);

	}

	private void populateViews(com.kinvey.samples.citywatch.CityWatchEntity ent) {

		if (ent.getImage() != null) {
			Bitmap bitmap = BitmapFactory.decodeByteArray(ent.getImage(), 0, ent.getImage().length);
			mImage.setImageBitmap(bitmap);
		}else{
			Log.i(TAG, "no image");
		}
		mName.setText(ent.getTitle());
		mCatagory.setText(ent.getCategory());
		mLocation.setText(ent.getAddress());
		mDescription.setText(ent.getDescription());
		mSeverity.setText(ent.getSeverity());
		mRisk.setText(ent.getRisk());

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.fragment_view, menu);

	}

}
