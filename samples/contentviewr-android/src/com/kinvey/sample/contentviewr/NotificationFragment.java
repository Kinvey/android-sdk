/*
 * Copyright (c) 2014, Kinvey, Inc.
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
package com.kinvey.sample.contentviewr;

import java.io.IOException;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.push.KinveyGCMService;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.sample.contentviewr.core.ContentFragment;

/**
 * @author edwardf
 */
public class NotificationFragment extends ContentFragment {


    TextView notifyTitle;
    TextView updatesLabel;
    Switch updates;

    private Typeface roboto;

    

    @Override
    public int getViewID() {
        return R.layout.fragment_notification;
    }

    @Override
    public void bindViews(View v) {
        roboto = Typeface.createFromAsset(getSherlockActivity().getAssets(), "Roboto-Thin.ttf");

        notifyTitle = (TextView) v.findViewById(R.id.notify_update_title);
        updatesLabel = (TextView) v.findViewById(R.id.notify_update_label);
        updates = (Switch) v.findViewById(R.id.notify_updates);

        notifyTitle.setTypeface(roboto);
        updatesLabel.setTypeface(roboto);

        updates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                	client().push().initialize(getSherlockActivity().getApplication());
                	
                    updates.setChecked(true);
                }else{
                	client().push().disablePush();
                    updates.setChecked(false);
                }
            }
        });

        updates.setChecked(client().push().isPushEnabled());
     
    }
    
    public void registerWithKinvey(final Client client, final String gcmRegID, boolean register) {
        //registered on GCM but not on Kinvey?
        Log.v(Client.TAG, "about to register with Kinvey");
        if (client == null) {
            Log.e(Client.TAG, "GCMService got garbage collected, cannot complete registration!");
            return;
        }

        if (!client.user().isUserLoggedIn()) {
            Log.e(Client.TAG, "Need to login a current user before registering for push!");
            return;
        }

        if (register) {

            client.push().enablePushViaRest(new KinveyClientCallback() {
                @Override
                public void onSuccess(Object result) {
                	Log.i("GCM", "registered with Kinvey");
                }

                @Override
                public void onFailure(Throwable error) {
                    Log.v(Client.TAG, "GCM - user update error: " + error);
                }
            }, gcmRegID);

        } else {
            client.push().disablePushViaRest(new KinveyClientCallback() {
                @Override
                public void onSuccess(Object result) {
                 
                }

                @Override
                public void onFailure(Throwable error) {
                    Log.v(Client.TAG, "GCM - user update error: " + error);
                }
            }, gcmRegID);
            
        }
        
    }


    @Override
    public String getTitle() {
        return "Notifications";
    }

    private Client client(){
        return ((ContentViewrApplication)((SettingsActivity) getSherlockActivity()).getApplicationContext()).getClient();
    }


}
