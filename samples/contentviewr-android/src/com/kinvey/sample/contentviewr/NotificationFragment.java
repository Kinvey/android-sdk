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
import android.view.View;
import android.widget.*;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kinvey.android.Client;
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
                	
                    final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(NotificationFragment.this.getActivity());
                	//final String regid;
                    
                    new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground(Void... params) {
                            String msg = "";
                            try {
//                                if (gcm == null) {
//                                    gcm = GoogleCloudMessaging.getInstance(NotificationFragment.this.getActivity());
//                                }
                                String regid = gcm.register("");
                                msg = "Device registered, registration ID=" + regid;

                                // You should send the registration ID to your server over HTTP,
                                // so it can use GCM/HTTP or CCS to send messages to your app.
                                // The request to your server should be authenticated if your app
                                // is using accounts.
                                client().push().initialize(getSherlockActivity().getApplication());

                                // For this demo: we don't need to send it because the device
                                // will send upstream messages to a server that echo back the
                                // message using the 'from' address in the message.

                                // Persist the regID - no need to register again.
                               // storeRegistrationId(NotificationFragment.this.getActivity(), regid);
                            } catch (IOException ex) {
                                msg = "Error :" + ex.getMessage();
                                // If there is an error, don't just keep trying to register.
                                // Require the user to click a button again, or perform
                                // exponential back-off.
                            }
                            return msg;
                        }

                        @Override
                        protected void onPostExecute(String msg) {
                            //mDisplay.append(msg + "\n");
                        }
                    }.execute(null, null, null);
                    
                    
                    //client().push().initialize(getSherlockActivity().getApplication());
                    updates.setChecked(true);
                }else{
                    client().user().remove("_push");
                    updates.setChecked(false);

                }
            }
        });



        if (client().user().containsKey("_push")){
            updates.setChecked(true);
        }else{
            updates.setChecked(false);
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
