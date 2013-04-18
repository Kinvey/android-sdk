//package com.kinvey.samples.old.citywatch.kinvey.samples.citywatch;
//
//import java.io.ByteArrayOutputStream;
//import java.util.ArrayList;
//import java.util.List;
//
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.location.Location;
//import android.os.Bundle;
//import android.text.InputFilter.LengthFilter;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.Spinner;
//import android.widget.Toast;
//
//import com.actionbarsherlock.app.SherlockFragment;
//import com.actionbarsherlock.view.Menu;
//import com.actionbarsherlock.view.MenuInflater;
//import com.actionbarsherlock.view.MenuItem;
//import com.facebook.Request;
//import com.facebook.Response;
//import com.facebook.Session;
//import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.kinvey.samples.citywatch.CityWatchData.Category;
//import com.kinvey.samples.citywatch.CityWatchData.Risk;
//import com.kinvey.samples.citywatch.CityWatchData.Severity;
//import com.kinvey.samples.citywatch.CityWatchEntity;
//import com.kinvey.samples.citywatch.R;
//import com.kinvey.util.ScalarCallback;
//
//public class CityWatchEditDetailsFragment extends SherlockFragment {
//
//	private static final String TAG = CityWatchEditDetailsFragment.class.getSimpleName();
//
//	public static final int CAMERA_REQUEST = 1;
//
//	private ImageView mImage;
//	private EditText mName;
//	private Spinner mCategory;
//	private EditText mDescription;
//	private EditText mLocation;
//	private Spinner mRisk;
//	private Spinner mSeverity;
//
//	private Bitmap photo;
//
//	private AlertDialog confirmOG = null;
//
//	public static CityWatchEditDetailsFragment newInstance() {
//		return new CityWatchEditDetailsFragment();
//	}
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setHasOptionsMenu(true);
//	}
//
//	@Override
//	public void onPause() {
//		super.onPause();
//		if (confirmOG != null) {
//			confirmOG.dismiss();
//		}
//	}
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
//		View v = inflater.inflate(R.layout.fragment_edit_details, group, false);
//		bindViews(v);
//		populateSpinners();
//		setListeners();
//		return v;
//	}
//
//	private void bindViews(View v) {
//		mImage = (ImageView) v.findViewById(R.id.edit_details_image);
//		mName = (EditText) v.findViewById(R.id.edit_details_name);
//		mCategory = (Spinner) v.findViewById(R.id.edit_details_category);
//		mDescription = (EditText) v.findViewById(R.id.edit_details_description);
//		mLocation = (EditText) v.findViewById(R.id.edit_details_location);
//		mRisk = (Spinner) v.findViewById(R.id.edit_details_risk);
//		mSeverity = (Spinner) v.findViewById(R.id.edit_details_severity);
//	}
//
//	private void populateSpinners() {
//		// for each of the three spinners in this view
//		// load up a list of all the possible enum values
//		// create the adapter, set the view and the dropdown view
//		// then set the adapter on the Spinner view object.
//
//		// severity
//		List<String> sev = new ArrayList<String>();
//		for (Severity s : CityWatchData.Severity.values()) {
//			sev.add(s.getDisplayName());
//		}
//		ArrayAdapter<String> sevAdapter = new ArrayAdapter<String>(getSherlockActivity(),
//				android.R.layout.simple_spinner_item, sev);
//		sevAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		mSeverity.setAdapter(sevAdapter);
//		// risk
//		List<String> risk = new ArrayList<String>();
//		for (Risk r : CityWatchData.Risk.values()) {
//			risk.add(r.getDisplayName());
//		}
//		ArrayAdapter<String> riskAdapter = new ArrayAdapter<String>(getSherlockActivity(),
//				android.R.layout.simple_spinner_item, sev);
//		riskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//		mRisk.setAdapter(riskAdapter);
//		// category
//		List<String> cat = new ArrayList<String>();
//		for (Category c : CityWatchData.Category.values()) {
//			cat.add(c.getDisplayName());
//		}
//		ArrayAdapter<String> catAdapter = new ArrayAdapter<String>(getSherlockActivity(),
//				android.R.layout.simple_spinner_item, cat);
//		catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//		mCategory.setAdapter(catAdapter);
//
//	}
//
//	private void setListeners() {
//		mImage.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//				startActivityForResult(cameraIntent, CAMERA_REQUEST);
//			}
//		});
//
//	}
//
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//
//		inflater.inflate(R.menu.fragment_edit, menu);
//
//	}
//
//	// depending on which option is tapped, act accordingly
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.menu_item_cancel:
//			getSherlockActivity().finish();
//			return true;
//		case R.id.menu_item_save:
//			saveToKinvey();
//			return true;
//		default:
//			return super.onOptionsItemSelected(item);
//		}
//	}
//
//	private void saveToKinvey() {
//		CityWatchEntity ent = ((CityWatchDetailsActivity) getSherlockActivity()).curEntity;
//		ent.setTitle(mName.getText().toString());
//		ent.setCategory(mCategory.getSelectedItem().toString());
//		ent.setSeverity(mSeverity.getSelectedItem().toString());
//		ent.setRisk(mRisk.getSelectedItem().toString());
//		ent.setDescription(mDescription.getText().toString());
//		ent.setAddress(mLocation.getText().toString());
//
//		// TODO get Repeat working, so users can "RECONFIRM" an event
//		// ASSUMING lat/long have been set by location manager
//		ent.setRepeat(1);
//		Location l = new Location(TAG);
//		l.setLatitude(ent.getLatitude());
//		l.setLongitude(ent.getLongitude());
//		ent.setCoords(l);
//
//		// upload the entity
//		KinveyService.getInstance(getSherlockActivity()).addEntity(ent, new ScalarCallback<CityWatchEntity>() {
//
//			@Override
//			public void onSuccess(CityWatchEntity r) {
//				// upload the photo
//
//				Log.i(TAG, "appdata success, bout to upload image.");
//				saveImage(r.getObjectId());
//
//			}
//
//			@Override
//			public void onFailure(Throwable t) {
//				String msg = String.format("Save failed%nerror: %s", t.getMessage());
//				Log.e(TAG, msg);
//				Toast.makeText(CityWatchEditDetailsFragment.this.getSherlockActivity(), "", Toast.LENGTH_LONG).show();
//
//			}
//
//		});
//
//	}
//
//	private void saveImage(String id) {
//
//		ByteArrayOutputStream stream = new ByteArrayOutputStream();
//		photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
//
//		String filename = KinveyService.getFilename(id);
//		KinveyService.getInstance(getSherlockActivity()).addPicture(stream.toByteArray(), filename,
//				new ScalarCallback<Void>() {
//
//					@Override
//					public void onSuccess(Void r) {
//						Log.i(TAG, "image saved successfully");
//
//						getOGDialog();
//
//					}
//				});
//
//	}
//
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		Log.i(TAG, "activity result");
//		if (requestCode == CAMERA_REQUEST && resultCode == getSherlockActivity().RESULT_OK) {
//			Log.i(TAG, "it's a camera, and it's OK!");
//
//			photo = (Bitmap) data.getExtras().get("data");
//			mImage.setImageBitmap(photo);
//
//		} else {
//			Log.i(TAG, "uh oh! -> " + requestCode + " and result is: " + resultCode);
//
//		}
//	}
//
//	private void getOGDialog() {
//		if (confirmOG == null) {
//			confirmOG = new AlertDialog.Builder(getSherlockActivity()).create();
//			confirmOG.setTitle(getResources().getString(R.string.menu_legal));
//			confirmOG
//					.setMessage("Do you want to publish this event to Facebook Open Graph?\n You have to be logged into your facebook account!");
//			confirmOG.setButton(Dialog.BUTTON_NEGATIVE, "Nope", new Dialog.OnClickListener() {
//
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					confirmOG.cancel();
//					getSherlockActivity().finish();
//				}
//			});
//			confirmOG.setButton(Dialog.BUTTON_POSITIVE, "Yeah!", new Dialog.OnClickListener() {
//
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					pushToOpenGraph();
//				}
//			});
//
//		}
//		confirmOG.show();
//
//	}
//
//	private void pushToOpenGraph() {
//
//		final String message = "Hello Open Graph!";
//
//		 Request request = Request
//                 .newStatusUpdateRequest(Session.getActiveSession(), message, new Request.Callback() {
//                     @Override
//                     public void onCompleted(Response response) {
//                         Toast.makeText(getSherlockActivity(), message, Toast.LENGTH_LONG).show();
//                         Log.i(TAG, "Facebook says-> "+  response.getError());
//                     }
//                 });
//         request.executeAsync();
//
//
//
//	}
//}
