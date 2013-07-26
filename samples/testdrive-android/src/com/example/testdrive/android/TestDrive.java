/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.example.testdrive.android;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.testdrive.android.model.Entity;
import com.google.api.client.http.HttpTransport;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.callback.KinveyUserManagementCallback;
import com.kinvey.android.offline.SqlLiteOfflineStore;
import com.kinvey.java.Query;
import com.kinvey.android.Client;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.model.KinveyReference;
import com.kinvey.java.offline.OfflinePolicy;

public class TestDrive extends Activity {

	public static final String TAG = "TestDrive";
    private static final Level LOGGING_LEVEL = Level.FINEST;

    private ProgressBar bar;

	private Client kinveyClient;

    private SqlLiteOfflineStore store;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_drive);

        // run the following comamnd to turn on verbose logging:
        //
        // adb shell setprop log.tag.HttpTransport DEBUG
        //
        Logger.getLogger(HttpTransport.class.getName()).setLevel(LOGGING_LEVEL);

        this.store = new SqlLiteOfflineStore(getApplicationContext());

        bar = (ProgressBar) findViewById(R.id.refresh_progress);
        bar.setIndeterminate(true);

        kinveyClient = new Client.Builder(this).build();
        kinveyClient.enableDebugLogging();

        if (!kinveyClient.user().isUserLoggedIn()) {
            bar.setVisibility(View.VISIBLE);
            kinveyClient.user().login("ok", "ok", new KinveyUserCallback() {
                @Override
                public void onSuccess(User result) {
                    bar.setVisibility(View.GONE);
                    Log.i(TAG,"Logged in successfully as " + result.getId());
                    Toast.makeText(TestDrive.this, "New implicit user logged in successfully as " + result.getId(),
                            Toast.LENGTH_LONG).show();
                }
                @Override
                public void onFailure(Throwable error) {
                    bar.setVisibility(View.GONE);
                    Log.e(TAG, "Login Failure", error);
                    Toast.makeText(TestDrive.this, "Login error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }   else {
            Toast.makeText(this, "Using cached implicit user " + kinveyClient.user().getId(), Toast.LENGTH_LONG).show();
        }
	}

	public void onLoadClick(View view) {
        bar.setVisibility(View.VISIBLE);
        AsyncAppData<Entity> ad = kinveyClient.appData("entityCollection", Entity.class);
        ad.setOffline(OfflinePolicy.SYNC_ANYTIME, this.store);
        ad.getEntity("myEntity", new KinveyClientCallback<Entity>() {
            @Override
            public void onSuccess(Entity result) {
                bar.setVisibility(View.GONE);
                if (result == null){
                    Toast.makeText(TestDrive.this, "got callback but it's null!", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(TestDrive.this, "Entity Retrieved\nTitle: " + result.getTitle()
                        + "\nDescription: " + result.get("Description"), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Throwable error) {
                bar.setVisibility(View.GONE);
                Log.e(TAG, "AppData.getEntity Failure", error);
                Toast.makeText(TestDrive.this, "Get Entity error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onQueryClick(View view) {
        bar.setVisibility(View.VISIBLE);
        Query myQuery = kinveyClient.query();
        myQuery.equals("_id","myEntity");


//        kinveyClient.linkedData("myCollect", myEntity.class).get(new Query(), null, null, new String[] {"fieldNameOfReference"}, 1, true );


        KinveyReference ret = new KinveyReference()    ;
        AsyncAppData<Entity> ad = kinveyClient.appData("entityCollection", Entity.class);
//        ad.setOffline(OfflinePolicy.SYNC_ANYTIME, this.store);
        ad.get(myQuery, new KinveyListCallback<Entity>() {
            @Override
            public void onSuccess(Entity[] result) {
                bar.setVisibility(View.GONE);
                if(result != null){
                for (Entity entity : result) {
                    Toast.makeText(TestDrive.this,"Entity Retrieved\nTitle: " + entity.getTitle()
                            + "\nDescription: " + entity.get("Description"), Toast.LENGTH_LONG).show();
                }
                }

            }

            @Override
            public void onFailure(Throwable error) {
                bar.setVisibility(View.GONE);
                Log.e(TAG, "AppData.get by Query Failure", error);
                Toast.makeText(TestDrive.this, "Get by Query error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onLoadAllClick(View view) {
        bar.setVisibility(View.VISIBLE);
        AsyncAppData<Entity> ad = kinveyClient.appData("entityCollection", Entity.class);
        ad.setOffline(OfflinePolicy.SYNC_ANYTIME, this.store);
        ad.get(new Query(), new KinveyListCallback<Entity>() {
            @Override
            public void onSuccess(Entity[] result) {
                bar.setVisibility(View.GONE);
                if (result != null){
                for (Entity entity : result) {
                    Toast.makeText(TestDrive.this,"Entity Retrieved\nTitle: " + entity.getTitle()
                            + "\nDescription: " + entity.get("Description"), Toast.LENGTH_LONG).show();
                }
                }
            }

            @Override
            public void onFailure(Throwable error) {
                bar.setVisibility(View.GONE);
                Log.e(TAG, "AppData.get all Failure", error);
                Toast.makeText(TestDrive.this, "Get All error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

	public void onSaveClick(View view) {
        bar.setVisibility(View.VISIBLE);
        Entity entity = new Entity("myEntity");
        entity.put("Description", "This is a description of an offline entity!");
        entity.setOk(Entity.test.ONE);
        AsyncAppData<Entity> ad = kinveyClient.appData("entityCollection", Entity.class);
        ad.setOffline(OfflinePolicy.SYNC_ANYTIME, this.store);
        ad.save(entity, new KinveyClientCallback<Entity>() {
            @Override
            public void onSuccess(Entity result) {
                bar.setVisibility(View.GONE);
                Toast.makeText(TestDrive.this, "Entity Saved", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Throwable error) {
                bar.setVisibility(View.GONE);
                Log.e(TAG, "AppData.save Failure", error);
                Toast.makeText(TestDrive.this, "Save All error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onDeleteClick(View view) {
        bar.setVisibility(View.VISIBLE);


        AsyncAppData<Entity> ad = kinveyClient.appData("entityCollection", Entity.class);
        ad.setOffline(OfflinePolicy.SYNC_ANYTIME, this.store);
        ad.delete("myEntity", new KinveyDeleteCallback() {
            @Override
            public void onSuccess(KinveyDeleteResponse result) {
                bar.setVisibility(View.GONE);
                Toast.makeText(TestDrive.this,"Number of Entities Deleted: " + result.getCount(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Throwable error) {
                bar.setVisibility(View.GONE);
                Log.e(TAG, "AppData.delete Failure", error);
                Toast.makeText(TestDrive.this, "Delete error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }



}