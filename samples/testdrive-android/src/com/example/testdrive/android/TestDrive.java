/** 
 * Copyright (c) 2014, Kinvey, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.example.testdrive.android;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.testdrive.android.model.Entity;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyMICCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.offline.SqlLiteOfflineStore;
import com.kinvey.java.Query;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyDeleteResponse;
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

        // run the following commandf to turn on verbose logging:
        //
        // adb shell setprop log.tag.HttpTransport DEBUG
        //
        Logger.getLogger(HttpTransport.class.getName()).setLevel(LOGGING_LEVEL);

        
        this.store = new SqlLiteOfflineStore(getApplicationContext());

        bar = (ProgressBar) findViewById(R.id.refresh_progress);
        bar.setIndeterminate(true);

        kinveyClient = new Client.Builder(this).build();
        
        if (kinveyClient.user().isUserLoggedIn()){
          kinveyClient.user().logout().execute();
        }
        
        
        //set globally
        kinveyClient.setClientAppVersion("123");
        kinveyClient.setClientAppVersion(1, 2, 3);
        kinveyClient.setCustomRequestProperty("region", "FR");
        
        
                
        kinveyClient.enableDebugLogging();

        if (!kinveyClient.user().isUserLoggedIn()) {
            bar.setVisibility(View.VISIBLE);

            kinveyClient.user().put("email", "myemail@domain.com");
            
//			kinveyClient.user().loginWithAuthorizationCodeAPI("mjs", "demo", "kinveyAuthDeo://", new KinveyUserCallback() {
//
//						@Override
//						public void onSuccess(User result) {
//							Toast.makeText(TestDrive.this, "did it!", Toast.LENGTH_SHORT).show();
//							bar.setVisibility(View.GONE);
//						}
//
//						@Override
//						public void onFailure(Throwable error) {
//							Toast.makeText(TestDrive.this, "nope! " + error.getMessage(), Toast.LENGTH_SHORT).show();
//							error.printStackTrace();
//							bar.setVisibility(View.GONE);
//
//						}
//					});
            
            //kinveyClient.file().downloadBlocking(new Query()).executeAndDownloadTo(myOutpustStream);
//            kinveyClient.user().loginWithAuthorizationCodeLoginPage("kinveyAuthDemo://", new KinveyMICCallback() {
//        
//        @Override
//        public void onSuccess(User result) {
//          Toast.makeText(TestDrive.this, "did it! with: " + result.getId(), Toast.LENGTH_SHORT).show();
//          bar.setVisibility(View.GONE);         
//        }
//        
//        @Override
//        public void onFailure(Throwable error) {
//          Toast.makeText(TestDrive.this, "nope!", Toast.LENGTH_SHORT).show();
//          error.printStackTrace();
//          bar.setVisibility(View.GONE);         
//        }
//        
//        @Override
//        public void onReadyToRender(String myURLToRender) {
//           Uri uri = Uri.parse(myURLToRender);
//           Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//           startActivity(intent);
//          
//        }
//      });
//            
            kinveyClient.user().login("push", "push", new KinveyUserCallback() {
                @Override
                public void onSuccess(User result) {
                    bar.setVisibility(View.GONE);
                    Log.i(TAG,"Logged in successfully as " + result.getId());
                    Toast.makeText(TestDrive.this, "New implicit user logged in successfully as " + result.getId(),
                            Toast.LENGTH_LONG).show();
                    
                    boolean ispush = kinveyClient.push().isPushEnabled();
                    Toast.makeText(TestDrive.this, "push init? " + ispush, Toast.LENGTH_LONG).show();
                    if (!ispush){
                    	kinveyClient.push().initialize(TestDrive.this.getApplication());
                    }
                    
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
        //ad.setOffline(OfflinePolicy.LOCAL_FIRST, this.store);
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
        //myQuery.startsWith("_id", "my");
        //myQuery.addSort("_id", SortOrder.ASC);
        AsyncAppData<Entity> ad = kinveyClient.appData("entityCollection", Entity.class);
        //ad.setOffline(OfflinePolicy.LOCAL_FIRST, store);
        
        ad.setClientAppVersion(1, 1, 1);
        
        GenericJson customheaders = new GenericJson();
        customheaders.put("hello", "hey");
        
        ad.setCustomRequestProperties(null);
        
        ad.get(myQuery, new KinveyListCallback<Entity>() {
            @Override
            public void onSuccess(Entity[] result) {
                bar.setVisibility(View.GONE);
                if(result != null){
                    Toast.makeText(TestDrive.this,"Retrieved " + result.length + " entities!", Toast.LENGTH_LONG).show();
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
        ad.get(new Query(), new KinveyListCallback<Entity>() {
            @Override
            public void onSuccess(Entity[] result) {
                bar.setVisibility(View.GONE);
                if (result != null){
                    Toast.makeText(TestDrive.this,"Retrieved " + result.length + " entities!", Toast.LENGTH_LONG).show();
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
        
        //byte[] b = new byte[]{(byte)0xe0, 0x4f, (byte)0xd0, (byte)0xea, (byte)0x20};
        
        kinveyClient.push().disablePush();
        
        boolean ispush = kinveyClient.push().isPushEnabled();
        Toast.makeText(TestDrive.this, "push init? " + ispush, Toast.LENGTH_LONG).show();

        //entity.put("bytearray", b);


//        AsyncAppData<Entity> ad = kinveyClient.appData("entityCollection", Entity.class);
//        //ad.setOffline(OfflinePolicy.LOCAL_FIRST, this.store);
//        ad.save(entity, new KinveyClientCallback<Entity>() {
//            @Override
//            public void onSuccess(Entity result) {
//                Log.e(TAG, "AppData.saved " + result.get("_id"));
//                bar.setVisibility(View.GONE);
//                Toast.makeText(TestDrive.this, "Entity Saved", Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onFailure(Throwable error) {
//                bar.setVisibility(View.GONE);
//                Log.e(TAG, "AppData.save Failure", error);
//                Toast.makeText(TestDrive.this, "Save All error: " + error.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });

    }

    public void onDeleteClick(View view) {
        bar.setVisibility(View.VISIBLE);


        AsyncAppData<Entity> ad = kinveyClient.appData("entityCollection", Entity.class);
        ad.setOffline(OfflinePolicy.LOCAL_FIRST, this.store);
        Query q = new Query().equals("offline", "doit");
        ad.delete(q, new KinveyDeleteCallback() {
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
        
//        ad.delete("toDelete", new KinveyDeleteCallback() {
//            @Override
//            public void onSuccess(KinveyDeleteResponse result) {
//                bar.setVisibility(View.GONE);
//                Toast.makeText(TestDrive.this,"Deleted it: " + result.getCount(), Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onFailure(Throwable error) {
//                bar.setVisibility(View.GONE);
//                Log.e(TAG, "AppData.delete Failure", error);
//                Toast.makeText(TestDrive.this, "Delete error: " + error.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
//        
        
    }
    
    @Override
    public void onNewIntent(Intent intent){
      super.onNewIntent(intent);
      kinveyClient.user().onOAuthCallbackRecieved(intent);
    }
    




}