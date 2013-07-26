/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
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
        Bitmap image = ent.getBitmap();
		if (image != null) {
			mImage.setImageBitmap(image);
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
