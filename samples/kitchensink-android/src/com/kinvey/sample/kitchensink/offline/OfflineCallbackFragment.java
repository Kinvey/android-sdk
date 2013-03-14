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
package com.kinvey.sample.kitchensink.offline;


import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.actionbarsherlock.R;
import com.kinvey.android.Client;
import com.kinvey.android.offline.KinveyOfflineCallback;
import com.kinvey.android.offline.OfflineAppData;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.sample.kitchensink.UseCaseFragment;

/**
 * @author edwardf
 * @since 2.0
 */
public class OfflineCallbackFragment extends UseCaseFragment implements View.OnClickListener, KinveyOfflineCallback {


    private Button saveButton;
    private EditText saveID;
    private Button getButton;
    private EditText getID;

    private TextView queueSize;
    private TextView storeSize;

    private String collectionName = "OfflineTest";


    private OfflineAppData<OfflineEntity> offline;


    @Override
    public int getViewID() {
        return R.layout.feature_offline_callback;
    }

    @Override
    public void bindViews(View v) {
        saveButton = (Button) v.findViewById(com.kinvey.sample.kitchensink.R.id.offline_save_button);
        saveID = (EditText) v.findViewById(com.kinvey.sample.kitchensink.R.id.offline_save_id);
        getButton = (Button) v.findViewById(com.kinvey.sample.kitchensink.R.id.offline_get_button);
        getID = (EditText) v.findViewById(com.kinvey.sample.kitchensink.R.id.offline_get_id);

        queueSize = (TextView) v.findViewById(com.kinvey.sample.kitchensink.R.id.offline_store_queue_size);
        storeSize = (TextView) v.findViewById(com.kinvey.sample.kitchensink.R.id.offline_store_entity_count);

        saveButton.setOnClickListener(this);
        getButton.setOnClickListener(this);

        offline = getClient().offlineAppData(collectionName, OfflineEntity.class, getApplicationContext());
        offline.setCallback(this);
    }

    @Override
    public String getTitle() {
        return "Callbacks";  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void saveOffline() {
        getClient().offlineAppData(collectionName, OfflineEntity.class, getApplicationContext()).save(new OfflineEntity(), new KinveyClientCallback<OfflineEntity>() {
            @Override
            public void onSuccess(OfflineEntity result) {
                Log.i(Client.TAG, "entity saved offline");
                saveID.setText(result.getId());
                updateStoreState();
            }

            @Override
            public void onFailure(Throwable error) {
                updateStoreState();
            }
        });

        updateStoreState();
    }

    private void getOffline() {

        updateStoreState();
    }

    private void updateStoreState() {
        String qs = String.valueOf(getClient().offlineAppData(collectionName, OfflineEntity.class, getApplicationContext()).getQueueSize());
        String ec = String.valueOf(getClient().offlineAppData(collectionName, OfflineEntity.class, getApplicationContext()).getEntityCount());

        queueSize.setText(qs);
        storeSize.setText(ec);

    }


    @Override
    public void onClick(View v) {
        if (v == saveButton) {
            saveOffline();
        } else if (v == getButton) {
            getOffline();
        }
    }

    @Override
    public void onSuccess(String httpVerb, String entityID) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onFailure(String httpVerb, String entityID) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
