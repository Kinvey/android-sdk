package com.example.testdrive.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.testdrive.android.model.Entity;

import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.Query;
import com.kinvey.android.Client;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyDeleteResponse;

public class TestDrive extends Activity {

	public static final String TAG = "TestDrive";

    private ProgressBar bar;

	private Client kinveyClient;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_drive);
        bar = (ProgressBar) findViewById(R.id.refresh_progress);
        bar.setIndeterminate(true);

        kinveyClient = new Client.Builder(this).build();
        if (!kinveyClient.user().isUserLoggedIn()) {
            bar.setVisibility(View.VISIBLE);
            kinveyClient.user().login(new KinveyUserCallback() {
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
        kinveyClient.appData("entityCollection", Entity.class).getEntity("myEntity", new KinveyClientCallback<Entity>() {
            @Override
            public void onSuccess(Entity result) {
                bar.setVisibility(View.GONE);
                Toast.makeText(TestDrive.this,"Entity Retrieved\nTitle: " + result.getTitle()
                + "\nDescription: " + result.get("Description"), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Throwable error) {
                bar.setVisibility(View.GONE);
                Log.e(TAG, "AppData.getEntityBlocking Failure", error);
                Toast.makeText(TestDrive.this, "Get Entity error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onQueryClick(View view) {
        bar.setVisibility(View.VISIBLE);
        Query myQuery = kinveyClient.query();
        myQuery.equals("_id","myEntity");
        kinveyClient.appData("entityCollection", Entity.class).get(myQuery, new KinveyListCallback<Entity>() {
            @Override
            public void onSuccess(Entity[] result) {
                bar.setVisibility(View.GONE);
                for (Entity entity : result) {
                    Toast.makeText(TestDrive.this,"Entity Retrieved\nTitle: " + entity.getTitle()
                            + "\nDescription: " + entity.get("Description"), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Throwable error) {
                bar.setVisibility(View.GONE);
                Log.e(TAG, "AppData.getBlocking by Query Failure", error);
                Toast.makeText(TestDrive.this, "Get by Query error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onLoadAllClick(View view) {
        bar.setVisibility(View.VISIBLE);
        kinveyClient.appData("entityCollection", Entity.class).get(new Query(), new KinveyListCallback<Entity>() {
            @Override
            public void onSuccess(Entity[] result) {
                bar.setVisibility(View.GONE);
                for (Entity entity : result) {
                    Toast.makeText(TestDrive.this,"Entity Retrieved\nTitle: " + entity.getTitle()
                            + "\nDescription: " + entity.get("Description"), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Throwable error) {
                bar.setVisibility(View.GONE);
                Log.e(TAG, "AppData.getBlocking all Failure", error);
                Toast.makeText(TestDrive.this, "Get All error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

	public void onSaveClick(View view) {
        bar.setVisibility(View.VISIBLE);
        Entity entity = new Entity("myEntity");
        entity.put("Description","This is a description of a dynamically-added Entity property.");
        kinveyClient.appData("entityCollection", Entity.class).save(entity, new KinveyClientCallback<Entity>() {
            @Override
            public void onSuccess(Entity result) {
                bar.setVisibility(View.GONE);
                Toast.makeText(TestDrive.this,"Entity Saved\nTitle: " + result.getTitle()
                        + "\nDescription: " + result.get("Description"), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Throwable error) {
                bar.setVisibility(View.GONE);
                Log.e(TAG, "AppData.saveBlocking Failure", error);
                Toast.makeText(TestDrive.this, "Save All error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onDeleteClick(View view) {
        bar.setVisibility(View.VISIBLE);
        kinveyClient.appData("entityCollection", Entity.class).delete("myEntity", new KinveyDeleteCallback() {
            @Override
            public void onSuccess(KinveyDeleteResponse result) {
                bar.setVisibility(View.GONE);
                Toast.makeText(TestDrive.this,"Number of Entities Deleted: " + result.getCount(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Throwable error) {
                bar.setVisibility(View.GONE);
                Log.e(TAG, "AppData.deleteBlocking Failure", error);
                Toast.makeText(TestDrive.this, "Delete error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}