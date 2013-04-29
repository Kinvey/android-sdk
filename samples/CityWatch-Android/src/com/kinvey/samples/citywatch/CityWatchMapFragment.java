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

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.Query;
import com.kinvey.java.query.MongoQueryFilter;

/**
* @author mjsalinger
* @since 2.0
*/
public class CityWatchMapFragment extends SherlockFragment implements
        GoogleMap.OnMapClickListener, GoogleMap.OnInfoWindowClickListener {

	// reference the View object which renders the map itself
	private MapView mMap = null;
    // reference to the currently selected map marker
    private Marker mCurMarker = null;

	private static final String TAG = CityWatchApplication.TAG;

    private HashMap<String, CityWatchEntity> markerEntities;

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
        kinveyClient = ((CityWatchApplication) getSherlockActivity().getApplication()).getClient();

		// ensure the current device can even support running google services,
		// which are required for using google maps.
		int googAvailable = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getSherlockActivity());
		if (googAvailable != ConnectionResult.SUCCESS) {
			Log.i(TAG, "googAvailable fail!");
			GooglePlayServicesUtil.getErrorDialog(googAvailable,
					getSherlockActivity(), 0).show();
		} else {

            try {
                MapsInitializer.initialize(getActivity());
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }

			bindViews(v);
			mMap.onCreate(saved);
			mMap.getMap().setMyLocationEnabled(true);
            mMap.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(convertLocationToLatLng(((CityWatch) getSherlockActivity()).getLastKnown()), 15));

			// setListeners();

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
        mMap.getMap().setOnInfoWindowClickListener(this);
        populateMap();
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

    // listen for clicks on the map, clearing the current marker (if there is
    // one)
    // and setting a new marker, showing the default info window for adding a
    // note.
    @Override
    public void onMapClick(LatLng latlng) {
        if (mCurMarker != null && mCurMarker.isVisible()) {
            mCurMarker.remove();
        }
        mCurMarker = mMap.getMap().addMarker(
                new MarkerOptions()
                        .position(latlng)
                        .title(getResources()
                                .getString(R.string.marker_default))
                        .draggable(true));
        mCurMarker.showInfoWindow();

    }

    // listen for clicks on the info window, lazily instantiating the
    // "edit marker" dialog
    @Override
    public void onInfoWindowClick(final Marker mark) {
            ((CityWatch) getSherlockActivity()).setCurEntity(markerEntities.get(mark.getId()));
            ((CityWatch) getSherlockActivity()).showViewDetailsFragment();
    }


    public void populateMap() {
        markerEntities = new HashMap<String, CityWatchEntity>();

        List<CityWatchEntity> nearbyEntities = ((CityWatch) getSherlockActivity()).getNearbyEntities();

        for (CityWatchEntity entity : nearbyEntities) {
            Marker curMarker = mMap.getMap()
                    .addMarker(
                            new MarkerOptions()
                                    .position(
                                            convertDoublesToLatLng(entity.getLatitude(), entity.getLongitude()))
                                    .title(entity.getTitle())
                                    .draggable(false));
            markerEntities.put(curMarker.getId(), entity);
            curMarker.showInfoWindow();
        }
         /*
        // first get world coordinates that are being drawn on screen
        LatLng topleft = mMap.getMap().getProjection().getVisibleRegion().farLeft;
        LatLng btmRight = mMap.getMap().getProjection().getVisibleRegion().nearRight;
        // now that we have a bounding box of what's on screen, use a
        // SimpleQuery to query Kinvey's backend `withinBox`
        Query geoquery = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());

        geoquery.withinBox("_geoloc", topleft.latitude, topleft.longitude, btmRight.latitude, btmRight.longitude);

        kinveyClient.appData("CityWatch", CityWatchEntity.class).get(geoquery, new KinveyListCallback<CityWatchEntity>() {
            @Override
            public void onSuccess(CityWatchEntity[] result) {
                List<CityWatchEntity> resultList = Arrays.asList(result);
                String msg = "query Sucessful, with a size of -> " + resultList.size();
                Log.i(TAG, msg);
                for (CityWatchEntity entity : resultList) {

                    Marker curMarker = mMap.getMap()
                            .addMarker(
                                    new MarkerOptions()
                                            .position(
                                                    convertDoublesToLatLng(entity.getLatitude(), entity.getLongitude()))
                                            .title(entity.getTitle())
                                            .draggable(false));
                    markerEntities.put(curMarker.getId(), entity);
                    curMarker.showInfoWindow();
                }
            }

            @Override
            public void onFailure(Throwable error) {
                String msg = "kinvey query fetch failed, " + error.getMessage();
                Toast.makeText(getSherlockActivity(), msg, Toast.LENGTH_LONG)
                        .show();
                Log.e(TAG, msg, error);
            }
        });             */


    }

    public static Location convertDoublesToLocation(double lat, double lng) {
        Location loc = new Location(TAG);
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        return loc;
    }

    public static LatLng convertLocationToLatLng(Location loc) {
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    public static LatLng convertDoublesToLatLng(double lat, double lng) {
        return new LatLng(lat, lng);
    }

}

