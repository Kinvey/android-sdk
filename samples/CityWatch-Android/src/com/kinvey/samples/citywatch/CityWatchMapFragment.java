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

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.MapView;
import com.kinvey.android.Client;
import com.kinvey.samples.citywatch.R;

/**
* @author mjsalinger
* @since 2.0
*/
public class CityWatchMapFragment extends SherlockFragment {

	// reference the View object which renders the map itself
	private MapView mMap = null;

	private static final String TAG = CityWatchMapFragment.class
			.getSimpleName();

    private Client kinveyClient;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group,
			Bundle saved) {
		View v = inflater.inflate(R.layout.fragment_map, group, false);
        setHasOptionsMenu(true);

		// ensure the current device can even support running google services,
		// which are required for using google maps.
		int googAvailable = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getSherlockActivity());
		if (googAvailable != ConnectionResult.SUCCESS) {
			Log.i(TAG, "googAvailable fail!");
			GooglePlayServicesUtil.getErrorDialog(googAvailable,
					getSherlockActivity(), 0).show();
		} else {
			bindViews(v);
			mMap.onCreate(saved);
			mMap.getMap().setMyLocationEnabled(true);

			// setListeners();

			// loading up Kinvey app settings from the property file, located at
			// assets/kinvey.properties
			// KinveySettings settings = KinveySettings
			// .loadFromProperties(getApplicationContext());
			// mKinveyClient = KCSClient.getInstance(getApplicationContext(),
			// settings);
			//
			// ma = mKinveyClient.mappeddata(GeoTagEntity.class,
			// COLLECTION_NAME);
			// // fire off the ping call to ensure we can communicate with
			// Kinvey
			// testKinveyService();

            kinveyClient = ((CityWatchApplication) getSherlockActivity().getApplication()).getClient();

		}

		return v;
	}

	private void bindViews(View v) {
		mMap = (MapView) v.findViewById(R.id.map_main);

	}

	/**
	 * When using google's MapView, the standard lifecycle callbacks *must* be
	 * made to the MapView.
	 *
	 * note that map.onCreate(...) is called during onCreateView(...), the
	 * onCreate callback is made before the onCreateView callback-- we cannot
	 * establish a MapView without having a ContentView, so this tweak is
	 * necessary when working with maps + fragments.
	 *
	 */

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mMap != null) {
			mMap.onDestroy();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mMap != null) {
			mMap.onResume();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mMap != null) {
			mMap.onPause();
		}
	}

	public static CityWatchMapFragment newInstance() {
		return new CityWatchMapFragment();
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new:
                ((CityWatch) getSherlockActivity()).showEditDetailsFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

