//package com.kinvey.samples.old.citywatch.kinvey.samples.citywatch;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.actionbarsherlock.app.SherlockFragment;
//import com.actionbarsherlock.view.Menu;
//import com.actionbarsherlock.view.MenuInflater;
//import com.kinvey.samples.citywatch.*;
//
//public class CityWatchViewDetailsFragment extends SherlockFragment {
//
//	private static final String TAG = CityWatchViewDetailsFragment.class.getSimpleName();
//
//	private ImageView mImage;
//	private TextView mName;
//	private TextView mCatagory;
//	private TextView mLocation;
//	private TextView mDescription;
//	private TextView mSeverity;
//	private TextView mRisk;
//
//	public static CityWatchViewDetailsFragment newInstance() {
//		return new CityWatchViewDetailsFragment();
//	}
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setHasOptionsMenu(true);
//	}
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
//		View v = inflater.inflate(R.layout.fragment_view_details, group, false);
//		bindViews(v);
//		populateViews(((CityWatchDetailsActivity) getSherlockActivity()).curEntity);
//		return v;
//	}
//
//	private void bindViews(View v) {
//		mImage = (ImageView) v.findViewById(R.id.view_details_image);
//		mName = (TextView) v.findViewById(R.id.view_details_name);
//		mCatagory = (TextView) v.findViewById(R.id.view_details_category);
//		mLocation = (TextView) v.findViewById(R.id.view_details_location);
//		mDescription = (TextView) v.findViewById(R.id.view_details_description);
//		mSeverity = (TextView) v.findViewById(R.id.view_details_severity);
//		mRisk = (TextView) v.findViewById(R.id.view_details_risk);
//
//	}
//
//	private void populateViews(com.kinvey.samples.citywatch.CityWatchEntity ent) {
//
//		if (ent.getImage() != null) {
//			Bitmap bitmap = BitmapFactory.decodeByteArray(ent.getImage(), 0, ent.getImage().length);
//			mImage.setImageBitmap(bitmap);
//		}else{
//			Log.i(TAG, "no image");
//		}
//		mName.setText(ent.getTitle());
//		mCatagory.setText(ent.getCategory());
//		mLocation.setText(ent.getAddress());
//		mDescription.setText(ent.getDescription());
//		mSeverity.setText(ent.getSeverity());
//		mRisk.setText(ent.getRisk());
//
//	}
//
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//
//		inflater.inflate(R.menu.fragment_view, menu);
//
//	}
//
//}
