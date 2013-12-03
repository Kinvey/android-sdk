package com.kinvey.sample.contentviewr;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.sample.contentviewr.model.ContentType;
import com.kinvey.sample.contentviewr.model.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Contentviewr extends SherlockFragmentActivity{

    public static final String TAG = "contentviewr";
    public static final String TARGET_COLLECTION = "Targets";
    public static final String TYPE_COLLECTION = "ContentTypes";
    private static final int PRELOAD_COUNT = 2; //this is used as semaphore, should match number of collections to preload

    private Client client;
    private int preLoadSemaphore;

    private List<Target> targets;
    private List<ContentType> contentTypes;

    public Client getClient(){
        if (client == null){
            client = new Client.Builder(getApplicationContext()).build();
        }
        return client;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.contentviewr);

        client = new Client.Builder(getApplicationContext()).setRetrieveUserCallback(new KinveyUserCallback() {
            @Override
            public void onSuccess(User result) {
                preload();
            }

            @Override
            public void onFailure(Throwable error) {
                showLogin();
            }
        }).build();
        client.enableDebugLogging();

        if (!client.user().isUserLoggedIn()){
            showLogin();;
        }



    }

    private void preload(){
        preLoadSemaphore = PRELOAD_COUNT;

        client.appData(TARGET_COLLECTION, Target.class).get(new KinveyListCallback<Target>() {
            @Override
            public void onSuccess(Target[] result) {
                targets = Arrays.asList(result);
                preLoadSemaphore = preLoadSemaphore -1;
                if (preLoadSemaphore == 0){
                    showPager();
                }

            }

            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(Contentviewr.this, "Something went wrong -> " + error, Toast.LENGTH_SHORT);
                Log.e(TAG, "something went wrong -> " + error);
                error.printStackTrace();
            }
        });

        client.appData(TYPE_COLLECTION, ContentType.class).get(new KinveyListCallback<ContentType>() {
            @Override
            public void onSuccess(ContentType[] result) {
                contentTypes = Arrays.asList(result);
                preLoadSemaphore = preLoadSemaphore - 1;
                if (preLoadSemaphore == 0){
                    showPager();
                }
            }

            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(Contentviewr.this, "Something went wrong -> " + error, Toast.LENGTH_SHORT);
                Log.e(TAG, "something went wrong -> " + error);
                error.printStackTrace();
            }
        });





    }

    private void showPager(){
        replaceFragment(new ContentTypePager(), false);
    }

    private void showLogin(){
        //replaceFragment(new LoginFragment(), false);
          client.user().login("hello", "hello", new KinveyUserCallback() {
              @Override
              public void onSuccess(User result) {
                  preload();
              }

              @Override
              public void onFailure(Throwable error) {
                  //To change body of implemented methods use File | Settings | File Templates.
              }
          })    ;


    }

    public void replaceFragment(SherlockFragment newOne, boolean backstack){

        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
        tr.replace(android.R.id.content, newOne, "content");
        if (backstack){
            tr.addToBackStack("back");
        }
        tr.commit();

    }

    public List<Target> getTargets(){
        return this.targets;
    }

    public List<ContentType> getContentTypes(){
        return this.contentTypes;
    }


}
