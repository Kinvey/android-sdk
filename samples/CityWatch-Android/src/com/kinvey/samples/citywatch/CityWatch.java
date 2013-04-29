/*
* Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.http.HttpTransport;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.Query;

/**
*
* @author edwardf
* @since 2.0
*/
public class CityWatch extends SherlockFragmentActivity implements ActionBar.TabListener, LocationListener {

    public static final String TAG = CityWatchApplication.TAG;
    // when downloading images, they are downsampled to the below size.
    public static final int MAX_W = 512;
    public static final int MAX_H = 512;

    // reference the daialog used to display legal info for using google maps
    private AlertDialog mLegalDialog = null;

    private Geocoder geocoder;
    private LocationManager locationmanager;
    private int lastTab = -1;

    private CityWatchEntity curEntity;
    public List<Address> nearbyAddress;

    public List<CityWatchEntity> getNearbyEntities() {
        return nearbyEntities;
    }

    public void setNearbyEntities(List<CityWatchEntity> nearbyEntities) {
        this.nearbyEntities = nearbyEntities;
    }

    private List<CityWatchEntity> nearbyEntities;

    public SherlockFragment[] mFragments;

    private Client kinveyClient;

    private static final Level LOGGING_LEVEL = Level.FINEST;
    private Location lastKnown;

    // This Activity manages two fragments -- the list and the map.
    // This Activity's Action bar is a tab group.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.getLogger(HttpTransport.class.getName()).setLevel(LOGGING_LEVEL);

        kinveyClient = ((CityWatchApplication) getApplication()).getClient();

        if (!kinveyClient.user().isUserLoggedIn()) {
            login();
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Note there is no call to SetContentView(...) here.
            // Check out onTabSelected()-- the selected tab and associated fragment
            // is
            // given
            // android.R.id.content as it's parent view, which *is* the content
            // view. Basically, this approach gives the fragments under the tabs the
            // complete window available to our activity without any unnecessary
            // layout inflation.
            setUpTabs();

            curEntity = new CityWatchEntity();
            nearbyAddress = new ArrayList<Address>();
            nearbyEntities = new ArrayList<CityWatchEntity>();

            locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationmanager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 100, this);

            if (Geocoder.isPresent()) {
                geocoder = new Geocoder(this);
            }

            // get last known location for a quick, rough start. Try GPS, if that
            // fails, try network. If that fails wait for fresh data
            lastKnown = locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnown == null) {
                lastKnown = locationmanager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (lastKnown == null) {
                // if the device has never known it's location, start at 0...?
                lastKnown = new Location(TAG);
                lastKnown.setLatitude(0.0);
                lastKnown.setLongitude(0.0);
            }
            Log.i(TAG, "lastKnown -> " + lastKnown.getLatitude() + ", " + lastKnown.getLongitude());
            setLocationInEntity(lastKnown);
        }

    }

    // on pause force-dismisses dialogs to avoid window leaks.
    @Override
    public void onPause() {
        super.onPause();
        if (mLegalDialog != null) {
            mLegalDialog.dismiss();
        }
    }

    public void setUpTabs() {
        mFragments = new SherlockFragment[2];
        getSupportActionBar().setHomeButtonEnabled(false);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.Tab listTab = getSupportActionBar().newTab().setText("List").setTabListener(this);
        getSupportActionBar().addTab(listTab);

        ActionBar.Tab mapTab = getSupportActionBar().newTab().setText("Map").setTabListener(this);
        getSupportActionBar().addTab(mapTab);

    }

    public void returnHome() {
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        getSupportActionBar().setHomeButtonEnabled(false);
        if (lastTab == -1) {
            lastTab = 0;
        }
            getSupportActionBar().selectTab(getSupportActionBar().getTabAt(lastTab));
    }

    public void showViewDetailsFragment() {
        lastTab = getSupportActionBar().getSelectedTab().getPosition();
        getSupportActionBar().setHomeButtonEnabled(true);

        CityWatchViewDetailsFragment frag = CityWatchViewDetailsFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.replace(android.R.id.content, frag);
        transaction.commit();
    }

    public void showEditDetailsFragment() {
        lastTab = getSupportActionBar().getSelectedTab().getPosition();
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        CityWatchEditDetailsFragment frag = CityWatchEditDetailsFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.replace(android.R.id.content, frag);
        transaction.commit();
    }

    // Using ActionbarSherlock to handle options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_city_watch, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // depending on which option is tapped, act accordingly
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                returnHome();
                return true;
            case R.id.menu_item_legal:
                legal();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void legal() {
        if (mLegalDialog == null) {
            mLegalDialog = new AlertDialog.Builder(this).create();
            mLegalDialog.setTitle(getResources().getString(R.string.menu_legal));
            mLegalDialog.setMessage(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this));
            mLegalDialog.setButton(Dialog.BUTTON_POSITIVE, "Close", new Dialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mLegalDialog.cancel();
                }
            });
        }
        mLegalDialog.show();
    }

    private void login() {

        Intent details = new Intent(this, CityWatchLoginActivity.class);
        startActivity(details);
        this.finish();
    }

    // -------------Actionbar.TabListener methods
    // Fragments are instantiated on demand.
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        if (tab.getPosition() == 0) {
            if (mFragments[0] == null) {
                mFragments[0] = CityWatchListFragment.newInstance();
            }

            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            lastTab = -1;

        } else {
            if (mFragments[1] == null) {
                mFragments[1] = CityWatchMapFragment.newInstance();
            }

            if (lastTab == 1) {
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            } else {
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            }
            lastTab = -1;
        }

        ft.replace(android.R.id.content, mFragments[tab.getPosition()]);

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    // -----------Location Listener methods

    @Override
    public void onLocationChanged(Location location) {
        if (Geocoder.isPresent()) {
            try {
                nearbyAddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 10);
                setLocationInEntity(location);

            } catch (IOException e) {
                Log.e(TAG,
                        "OH NO!  There's a problem with the geocoder, so location is being disabled -> "
                                + e.getMessage());
                locationmanager.removeUpdates(this);

            }

        } else {
            Log.e(TAG, "OH NO!  There's a problem with the geocoder, so location is being disabled");
            locationmanager.removeUpdates(this);

        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    public Location getLastKnown() {
        return lastKnown;
    }

    private void setLocationInEntity(Location location) {
        // curEntity.setCoords(location);
        curEntity.setLatitude(location.getLatitude());
        curEntity.setLongitude(location.getLongitude());

        lastKnown = location;

        Query cityWatchQuery = kinveyClient.query();
        cityWatchQuery.nearSphere("_geoloc", curEntity.getLatitude(), curEntity.getLongitude());

        kinveyClient.appData("CityWatch",CityWatchEntity.class).get(cityWatchQuery, new KinveyListCallback<CityWatchEntity>() {

            @Override
            public void onSuccess(CityWatchEntity[] result) {
                List<CityWatchEntity> cityWatchList = Arrays.asList(result);
                Log.i(TAG, "Success, there are -> " + cityWatchList.size());
                nearbyEntities = cityWatchList;
                if (mFragments[0] != null) {
                    ((CityWatchListFragment) mFragments[0]).notifyNewData(nearbyEntities);
                }
                if (mFragments[1] != null) {
                    // udpate map next
                }

                if (cityWatchList.size() > 0) {
                    new GetThumbnailTask().execute();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Failed to get city watch list.", t);
            }
        });
    }

    public CityWatchEntity getCurEntity() {
        return curEntity;
    }

    public void setCurEntity(CityWatchEntity curEntity) {
        this.curEntity = curEntity;
    }

    private class GetThumbnailTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;

                int scaleFactor = 0;

                URL url;
                for (int i=0; i < nearbyEntities.size(); i++) {
                    scaleFactor = 0;

                    CityWatchEntity e = nearbyEntities.get(i);
                    if (e.getImageURL() != null) {
                        url = new URL(kinveyClient.file().getDownloadUrlBlocking(e.getImageURL()).execute().getBlobTemporaryUri());
                        /*do {
                            opts.inSampleSize = (int) Math.pow(2, scaleFactor++);
                            BitmapFactory.decodeStream((InputStream) url.getContent(), null, opts);
                        } while (opts.outWidth > MAX_W || opts.outHeight > MAX_H);  */

                        opts.inJustDecodeBounds = false;
                        e.setBitmap(BitmapFactory.decodeStream((InputStream) url.getContent(), null, opts));

                        int bytes = e.getBitmap().getWidth() * e.getBitmap().getHeight() * 4;
                        ByteBuffer buffer = ByteBuffer.allocate(bytes);
                        e.getBitmap().copyPixelsToBuffer(buffer);

                        byte[] array = buffer.array();
                        e.setImage(array);

                        Log.i(TAG, "Setting bitmap in entity, (true is set) -> " + (e.getBitmap() != null));
                    } else {
                        Log.i(TAG, "URL is invalid.  Skipping.");
                    }
                    publishProgress(null);
                }
                return true;

            } catch (MalformedURLException e) {
                Log.e(TAG,"Failed to download image; Bad URL.  ",e);
            } catch (IOException e) {
                Log.e(TAG,"Failed to download iamge; IO Exception.  ",e);
            } catch (Exception e) {
                Log.e(TAG,"Failed to download image. ",e);
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            notifyFragments();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                notifyFragments();
            }
        }

    }

    private void notifyFragments() {

        if (mFragments[0] != null) {
            ((CityWatchListFragment) mFragments[0]).notifyNewData(nearbyEntities);
        }
        if (mFragments[1] != null) {
            ((CityWatchMapFragment) mFragments[1]).populateMap();
        }

    }
}
