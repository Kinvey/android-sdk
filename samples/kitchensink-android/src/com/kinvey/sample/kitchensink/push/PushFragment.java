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
package com.kinvey.sample.kitchensink.push;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kinvey.android.push.PushOptions;
import com.kinvey.android.push.PushRegistrationException;
import com.kinvey.android.push.UrbanAirshipPushOptions;
import com.kinvey.sample.kitchensink.R;
import com.kinvey.sample.kitchensink.UseCaseFragment;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class PushFragment extends UseCaseFragment implements View.OnClickListener {

    Button bRegister;
    TextView pushStatus;

    @Override
    public int getViewID() {
        return R.layout.feature_push;
    }

    @Override
    public void bindViews(View v) {
        bRegister = (Button) v.findViewById(R.id.push_register_button);
        bRegister.setOnClickListener(this);
        pushStatus = (TextView) v.findViewById(R.id.push_status);
        pushStatus.setText(getPushStatus());

    }

    @Override
    public void onClick(View v) {
        if (v == bRegister) {
            registerPush();
        }
    }

    public void registerPush() {
        PushOptions options = getApplicationContext().getClient().push().getPushOptions("5ZD39dqgRGaOFygj1pqGnQ","JLSUy-bpQBWhzESnxtMJYA",
                false);
        getApplicationContext().getClient().push().initialize(options, getActivity().getApplication());

        try {
            getApplicationContext().getClient().user().registerPush();
        } catch (PushRegistrationException ex) {
            Toast.makeText(this.getActivity(), ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public String getPushStatus() {

        PushOptions options = getApplicationContext().getClient().push().getPushOptions("5ZD39dqgRGaOFygj1pqGnQ","JLSUy-bpQBWhzESnxtMJYA",
                false);
        getApplicationContext().getClient().push().initialize(options, getActivity().getApplication());
        return Boolean.toString(getApplicationContext().getClient().push().isPushEnabled());

    }

    @Override
    public String getTitle() {
        return "Push!";
    }
}
