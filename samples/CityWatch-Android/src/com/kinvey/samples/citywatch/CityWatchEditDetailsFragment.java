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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import com.kinvey.android.Client;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.samples.citywatch.CityWatchData.Category;
import com.kinvey.samples.citywatch.CityWatchData.Risk;
import com.kinvey.samples.citywatch.CityWatchData.Severity;

public class CityWatchEditDetailsFragment extends SherlockFragment {

	private static final String TAG = CityWatchApplication.TAG;

	public static final int CAMERA_REQUEST = 1;

	private ImageView mImage;
	private EditText mName;
	private Spinner mCategory;
	private EditText mDescription;
	private EditText mLocation;
	private Spinner mRisk;
	private Spinner mSeverity;
    private Client kinveyClient;

	private Bitmap photo;

    private CityWatchEntity ent;

	private AlertDialog confirmOG = null;

    private static Typeface robotoThin;

	public static CityWatchEditDetailsFragment newInstance() {
		return new CityWatchEditDetailsFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (confirmOG != null) {
			confirmOG.dismiss();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
		View v = inflater.inflate(R.layout.fragment_edit_details, group, false);
        kinveyClient = ((CityWatchApplication) getSherlockActivity().getApplication()).getClient();
		bindViews(v);
		populateSpinners();
		setListeners();
        getSherlockActivity().getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		return v;
	}

	private void bindViews(View v) {
        robotoThin = Typeface.createFromAsset(getSherlockActivity().getAssets(), "Roboto-Thin.ttf");
        ent = ((CityWatch)getSherlockActivity()).getCurEntity();
		mImage = (ImageView) v.findViewById(R.id.edit_details_image);
		mName = (EditText) v.findViewById(R.id.edit_details_name);
		mCategory = (Spinner) v.findViewById(R.id.edit_details_category);
		mDescription = (EditText) v.findViewById(R.id.edit_details_description);
		mLocation = (EditText) v.findViewById(R.id.edit_details_location);
		mRisk = (Spinner) v.findViewById(R.id.edit_details_risk);
		mSeverity = (Spinner) v.findViewById(R.id.edit_details_severity);
        TextView header = (TextView) v.findViewById(R.id.header_edit_details);
        header.setTypeface(robotoThin);
	}

	private void populateSpinners() {
		// for each of the three spinners in this view
		// load up a list of all the possible enum values
		// create the adapter, set the view and the dropdown view
		// then set the adapter on the Spinner view object.

		// severity
		List<String> sev = new ArrayList<String>();
		for (Severity s : CityWatchData.Severity.values()) {
			sev.add(s.getDisplayName());
		}
		ArrayAdapter<String> sevAdapter = new ArrayAdapter<String>(getSherlockActivity(),
				android.R.layout.simple_spinner_item, sev);
		sevAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSeverity.setAdapter(sevAdapter);
		// risk
		List<String> risk = new ArrayList<String>();
		for (Risk r : CityWatchData.Risk.values()) {
			risk.add(r.getDisplayName());
		}
		ArrayAdapter<String> riskAdapter = new ArrayAdapter<String>(getSherlockActivity(),
				android.R.layout.simple_spinner_item, sev);
		riskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mRisk.setAdapter(riskAdapter);
		// category
		List<String> cat = new ArrayList<String>();
		for (Category c : CityWatchData.Category.values()) {
			cat.add(c.getDisplayName());
		}
		ArrayAdapter<String> catAdapter = new ArrayAdapter<String>(getSherlockActivity(),
				android.R.layout.simple_spinner_item, cat);
		catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mCategory.setAdapter(catAdapter);

	}

	private void setListeners() {
		mImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(cameraIntent, CAMERA_REQUEST);
			}
		});

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_edit, menu);


	}

	// depending on which option is tapped, act accordingly
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_cancel:
			getSherlockActivity().finish();
			return true;
		case R.id.menu_item_save:
			saveToKinvey();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void saveToKinvey() {
		ent.setTitle(mName.getText().toString());
		ent.setCategory(mCategory.getSelectedItem().toString());
		ent.setSeverity(mSeverity.getSelectedItem().toString());
		ent.setRisk(mRisk.getSelectedItem().toString());
		ent.setDescription(mDescription.getText().toString());
		ent.setAddress(mLocation.getText().toString());

		// TODO get Repeat working, so users can "RECONFIRM" an event
		// ASSUMING lat/long have been set by location manager
		ent.setRepeat(1);
		Location l = new Location(TAG);
		l.setLatitude(ent.getLatitude());
		l.setLongitude(ent.getLongitude());
		ent.setCoords(l);
        saveImage();
    }

    private void saveEntity() {
        kinveyClient.appData("CityWatch", CityWatchEntity.class).save(ent, new KinveyClientCallback<CityWatchEntity>() {
            @Override
            public void onSuccess(CityWatchEntity result) {
                Log.i(TAG, "appdata success, ready to publish to OpenGraph.");
                ent = result;
                getOGDialog();
            }

            @Override
            public void onFailure(Throwable error) {
                String msg = String.format("Save failed%nerror: %s", error.getMessage());
                Log.e(TAG, msg, error);
                Toast.makeText(CityWatchEditDetailsFragment.this.getSherlockActivity(), msg, Toast.LENGTH_LONG).show();
            }
        });
	}

	private void saveImage() {

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
        UUID imageUUID = UUID.randomUUID();
        String filename = "Reports_" + imageUUID + "_IMAGE.png";
        ent.setImageURL(filename);
        InputStream inputStream=new ByteArrayInputStream(stream.toByteArray());
        kinveyClient.file().upload(filename, inputStream, new UploaderProgressListener() {
            @Override
            public void progressChanged(MediaHttpUploader uploader) throws IOException {

            }

            @Override
            public void onSuccess(Void result) {
                Log.i(TAG, "image saved successfully");
                saveEntity();
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "Image save unsuccessful. ", error);
            }
        });
	}

    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "activity result");
		if (requestCode == CAMERA_REQUEST && resultCode == getSherlockActivity().RESULT_OK) {
			Log.i(TAG, "it's a camera, and it's OK!");

			photo = (Bitmap) data.getExtras().get("data");
			mImage.setImageBitmap(photo);

		} else {
			Log.i(TAG, "uh oh! -> " + requestCode + " and result is: " + resultCode);

		}
	}

	private void getOGDialog() {
		if (confirmOG == null) {
			confirmOG = new AlertDialog.Builder(getSherlockActivity()).create();
			confirmOG.setTitle(getResources().getString(R.string.menu_legal));
			confirmOG
					.setMessage("Do you want to publish this event to Facebook Open Graph?");
			confirmOG.setButton(Dialog.BUTTON_NEGATIVE, "Nope", new Dialog.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					confirmOG.cancel();
                    ((CityWatch) getSherlockActivity()).returnHome();
				}
			});
			confirmOG.setButton(Dialog.BUTTON_POSITIVE, "Yeah!", new Dialog.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					pushToOpenGraph();
				}
			});

		}
		confirmOG.show();

	}

	private void pushToOpenGraph() {

        // TODO:  Implement Push To OpenGraph through Kinvey
        FacebookEntity ogPush = new FacebookEntity();
        ogPush.setEntityId(ent.getObjectId());
        ogPush.setObjectType("kinveycitywatch:" + ent.getCategory().toLowerCase());
        kinveyClient.appData("kinveycitywatch:report", FacebookEntity.class).save(ogPush, new KinveyClientCallback<FacebookEntity>() {
            @Override
            public void onSuccess(FacebookEntity result) {
                Log.i(TAG, "Save to OpenGraph Successful");
                Toast.makeText(getSherlockActivity(), "Saved to OpenGraph", Toast.LENGTH_LONG).show();
                ((CityWatch) getSherlockActivity()).returnHome();
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "Failed to post to OpenGraph", error);
                Toast.makeText(getSherlockActivity(), "Saved to OpenGraph", Toast.LENGTH_LONG).show();
            }
        });




	}
}

